package com.group5.htms.service.impl;

import com.group5.htms.dto.prize.response.PrizeAwardResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.PrizeAwards;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.PrizeAwardStatus;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.PrizeAwardMapper;
import com.group5.htms.repository.PrizeAwardsRepository;
import com.group5.htms.repository.PrizeRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.PrizeAwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrizeAwardServiceImpl implements PrizeAwardService {

    private static final String REF_TYPE_TOURNAMENT_PRIZE = "tournament_prize";

    private final TournamentsRepository tournamentsRepository;
    private final PrizeRepository prizeRepository;
    private final PrizeAwardsRepository prizeAwardsRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final PrizeAwardMapper prizeAwardMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public List<PrizeAwardResponse> awardTournamentPrizes(Integer tournamentId) {
        Tournaments tournament = getTournament(tournamentId);

        validateTournamentCompleted(tournament);
        validateNotAwarded(tournament.getId());

        List<PrizeDistributions> prizes = prizeRepository.findByTournamentsIdOrderByFinishPositionAsc(tournament.getId());
        Map<Integer, PrizeDistributions> prizesByPosition = validatePrizeDistributions(tournament, prizes);

        List<RaceResults> eligibleResults = findEligibleResults(tournament.getId());
        List<StandingEntry> standing = buildStanding(eligibleResults);
        if (standing.size() < 3) {
            throw new BadRequestException("Not enough eligible horses to award top 3 tournament prizes");
        }

        Users admin = authService.getCurrentUser();
        Instant now = Instant.now();

        List<PrizeAwards> awards = List.of(
                createAward(tournament, prizesByPosition.get(1), standing.get(0), 1, now),
                createAward(tournament, prizesByPosition.get(2), standing.get(1), 2, now),
                createAward(tournament, prizesByPosition.get(3), standing.get(2), 3, now)
        );

        List<PrizeAwards> savedAwards = prizeAwardsRepository.saveAll(awards);
        savedAwards.forEach(award -> creditOwnerWallet(award, admin));

        return savedAwards.stream()
                .map(prizeAwardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrizeAwardResponse> getTournamentPrizeAwards(Integer tournamentId) {
        getTournament(tournamentId);

        return prizeAwardsRepository.findByTournaments_IdOrderByFinishPositionAsc(tournamentId)
                .stream()
                .map(prizeAwardMapper::toResponse)
                .toList();
    }

    private Tournaments getTournament(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        return tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));
    }

    private void validateTournamentCompleted(Tournaments tournament) {
        if (!TournamentStatus.COMPLETED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Tournament must be completed before awarding prizes");
        }
    }

    private void validateNotAwarded(Integer tournamentId) {
        if (prizeAwardsRepository.existsByTournaments_Id(tournamentId)) {
            throw new BadRequestException("Tournament prizes have already been awarded");
        }
    }

    private Map<Integer, PrizeDistributions> validatePrizeDistributions(
            Tournaments tournament,
            List<PrizeDistributions> prizes
    ) {
        Map<Integer, PrizeDistributions> prizesByPosition = prizes.stream()
                .filter(prize -> prize.getFinishPosition() != null)
                .collect(Collectors.toMap(
                        PrizeDistributions::getFinishPosition,
                        Function.identity(),
                        (left, right) -> left
                ));

        if (!prizesByPosition.keySet().containsAll(List.of(1, 2, 3))) {
            throw new BadRequestException("Tournament prize distributions must include positions 1, 2 and 3");
        }

        BigDecimal total = prizesByPosition.values()
                .stream()
                .filter(prize -> prize.getFinishPosition() >= 1 && prize.getFinishPosition() <= 3)
                .map(PrizeDistributions::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal prizePool = safeMoney(tournament.getPrizePool());
        if (total.compareTo(prizePool) != 0) {
            throw new BadRequestException("Tournament prize distribution total must equal tournament prize pool");
        }

        return prizesByPosition;
    }

    private List<RaceResults> findEligibleResults(Integer tournamentId) {
        List<RaceResults> eligibleResults = raceResultsRepository
                .findPublishedResultsByTournamentId(tournamentId, RaceResultStatus.PUBLISHED.getValue())
                .stream()
                .filter(result -> !Boolean.TRUE.equals(result.getIsDisqualified()))
                .filter(result -> result.getFinishPosition() != null)
                .filter(result -> result.getHorses() != null)
                .filter(result -> result.getOwner() != null)
                .toList();

        if (eligibleResults.isEmpty()) {
            throw new BadRequestException("No published race results found for this tournament");
        }

        return eligibleResults;
    }

    private List<StandingEntry> buildStanding(List<RaceResults> eligibleResults) {
        Map<Integer, StandingEntry> entriesByHorseId = new HashMap<>();

        for (RaceResults result : eligibleResults) {
            Horses horse = result.getHorses();
            HorseOwnerProfiles owner = result.getOwner();

            entriesByHorseId
                    .computeIfAbsent(horse.getId(), ignored -> new StandingEntry(horse, owner))
                    .addResult(result);
        }

        return entriesByHorseId.values()
                .stream()
                .sorted(standingComparator())
                .toList();
    }

    private Comparator<StandingEntry> standingComparator() {
        return Comparator
                .comparingInt(StandingEntry::getTotalPoints).reversed()
                .thenComparing(Comparator.comparingInt(StandingEntry::getWinCount).reversed())
                .thenComparingInt(StandingEntry::getBestFinishPosition)
                .thenComparing(
                        StandingEntry::getBestPaceSecondsPerMeter,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .thenComparingInt(entry -> entry.getHorse().getId());
    }

    private PrizeAwards createAward(
            Tournaments tournament,
            PrizeDistributions prize,
            StandingEntry entry,
            Integer tournamentRank,
            Instant now
    ) {
        RaceResults bestResult = entry.getBestResult();

        return PrizeAwards.builder()
                .prize(prize)
                .tournaments(tournament)
                .race(bestResult.getRaces())
                .result(bestResult)
                .horse(entry.getHorse())
                .owner(entry.getOwner())
                .finishPosition(tournamentRank)
                .amount(safeMoney(prize.getAmount()))
                .status(PrizeAwardStatus.AWARDED.getValue())
                .awardedAt(now)
                .build();
    }

    private void creditOwnerWallet(PrizeAwards award, Users admin) {
        BigDecimal reward = safeMoney(award.getAmount());
        if (reward.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (walletTransactionsRepository.existsByRefTypeAndRefIdAndTxTypeIgnoreCase(
                REF_TYPE_TOURNAMENT_PRIZE,
                award.getId(),
                WalletTransactionType.REWARD.getValue()
        )) {
            throw new BadRequestException("Tournament prize has already been credited");
        }

        Users ownerUser = award.getOwner().getUsers();
        Wallets wallet = getOrCreateOwnerWallet(ownerUser);
        Wallets lockedWallet = walletsRepository.findFirstById(wallet.getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found for horse owner"));

        if (!WalletStatus.ACTIVE.getValue().equalsIgnoreCase(lockedWallet.getStatus())) {
            throw new BadRequestException("Wallet is not active");
        }

        BigDecimal pointsBefore = safeMoney(lockedWallet.getPointBalance());
        BigDecimal pointsAfter = pointsBefore.add(reward);
        lockedWallet.setPointBalance(pointsAfter);

        WalletTransactions transaction = WalletTransactions.builder()
                .wallets(lockedWallet)
                .users(ownerUser)
                .txType(WalletTransactionType.REWARD.getValue())
                .cashAmount(BigDecimal.ZERO)
                .pointsAmount(reward)
                .exchangeRate(BigDecimal.ONE)
                .pointsBefore(pointsBefore)
                .pointsAfter(pointsAfter)
                .status(WalletTransactionStatus.COMPLETED.getValue())
                .refType(REF_TYPE_TOURNAMENT_PRIZE)
                .refId(award.getId())
                .createdBy(admin)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        walletsRepository.save(lockedWallet);
        walletTransactionsRepository.save(transaction);
    }

    private Wallets getOrCreateOwnerWallet(Users ownerUser) {
        return walletsRepository.findByUsersId(ownerUser.getId())
                .orElseGet(() -> walletsRepository.save(Wallets.builder()
                        .users(ownerUser)
                        .pointBalance(BigDecimal.ZERO)
                        .status(WalletStatus.ACTIVE.getValue())
                        .createdAt(Instant.now())
                        .build()));
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static class StandingEntry {
        private final Horses horse;
        private final HorseOwnerProfiles owner;
        private int totalPoints;
        private int winCount;
        private int bestFinishPosition = Integer.MAX_VALUE;
        private BigDecimal bestPaceSecondsPerMeter;
        private RaceResults bestResult;

        private StandingEntry(Horses horse, HorseOwnerProfiles owner) {
            this.horse = horse;
            this.owner = owner;
        }

        private void addResult(RaceResults result) {
            int pointsAwarded = result.getPointsAwarded() == null ? 0 : result.getPointsAwarded();
            int finishPosition = result.getFinishPosition();

            this.totalPoints += pointsAwarded;
            if (finishPosition == 1) {
                this.winCount++;
            }
            this.bestFinishPosition = Math.min(this.bestFinishPosition, finishPosition);

            BigDecimal resultPace = paceSecondsPerMeter(result);
            if (resultPace != null
                    && (this.bestPaceSecondsPerMeter == null
                    || resultPace.compareTo(this.bestPaceSecondsPerMeter) < 0)) {
                this.bestPaceSecondsPerMeter = resultPace;
            }

            if (this.bestResult == null || isBetterBestResult(result, this.bestResult)) {
                this.bestResult = result;
            }
        }

        private boolean isBetterBestResult(RaceResults candidate, RaceResults currentBest) {
            int candidatePoints = candidate.getPointsAwarded() == null ? 0 : candidate.getPointsAwarded();
            int currentPoints = currentBest.getPointsAwarded() == null ? 0 : currentBest.getPointsAwarded();
            if (candidatePoints != currentPoints) {
                return candidatePoints > currentPoints;
            }

            int candidateFinish = candidate.getFinishPosition();
            int currentFinish = currentBest.getFinishPosition();
            if (candidateFinish != currentFinish) {
                return candidateFinish < currentFinish;
            }

            BigDecimal candidatePace = paceSecondsPerMeter(candidate);
            BigDecimal currentPace = paceSecondsPerMeter(currentBest);
            if (candidatePace != null && currentPace != null
                    && candidatePace.compareTo(currentPace) != 0) {
                return candidatePace.compareTo(currentPace) < 0;
            }
            if (candidatePace != null) {
                return true;
            }
            if (currentPace != null) {
                return false;
            }

            return candidate.getId() < currentBest.getId();
        }

        private static BigDecimal paceSecondsPerMeter(RaceResults result) {
            if (result == null
                    || result.getFinishTimeSec() == null
                    || result.getRaces() == null
                    || result.getRaces().getDistanceM() == null
                    || result.getRaces().getDistanceM() <= 0
                    || !Double.isFinite(result.getRaces().getDistanceM())) {
                return null;
            }

            return result.getFinishTimeSec()
                    .divide(BigDecimal.valueOf(result.getRaces().getDistanceM()), MathContext.DECIMAL64);
        }

        private Horses getHorse() {
            return horse;
        }

        private HorseOwnerProfiles getOwner() {
            return owner;
        }

        private int getTotalPoints() {
            return totalPoints;
        }

        private int getWinCount() {
            return winCount;
        }

        private int getBestFinishPosition() {
            return bestFinishPosition;
        }

        private BigDecimal getBestPaceSecondsPerMeter() {
            return bestPaceSecondsPerMeter;
        }

        private RaceResults getBestResult() {
            return bestResult;
        }
    }
}
