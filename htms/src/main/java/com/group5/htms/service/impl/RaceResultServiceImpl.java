package com.group5.htms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.htms.dto.raceresult.request.RaceResultCancelRequest;
import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultDraftItemRequest;
import com.group5.htms.dto.raceresult.request.RaceResultDraftRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RacePublishResponse;
import com.group5.htms.dto.raceresult.response.RaceResultDraftResponse;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.dto.raceresult.response.TournamentRaceResultGroupResponse;
import com.group5.htms.dto.raceresult.response.TournamentResultHistoryResponse;
import com.group5.htms.dto.raceresult.response.TournamentResultResponse;
import com.group5.htms.dto.raceresult.response.TournamentStandingResponse;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.Notifications;
import com.group5.htms.entity.RacePointRules;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.RaceResultAdminEditAudit;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.RaceRounds;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.RefereeReports;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.BetStatus;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.NotificationsRepository;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RaceResultAdminEditAuditRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RaceRoundsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.RaceResultService;
import com.group5.htms.validation.RaceResultValidator;
import com.group5.htms.util.RankGroupUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RaceResultServiceImpl implements RaceResultService {
    private static final Logger log = LoggerFactory.getLogger(RaceResultServiceImpl.class);

    private static final String REF_TYPE_BET = "bet";
    private static final String REF_TYPE_RACE_RESULT = "race_result";
    private static final String NOTIFICATION_TYPE_RACE_RESULT = "race_result";
    private static final String NOTIFICATION_TYPE_BET_RESULT = "bet_result";
    private static final ObjectMapper AUDIT_OBJECT_MAPPER = new ObjectMapper();

    private final RaceResultsRepository raceResultsRepository;
    private final RaceRoundsRepository raceRoundsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RacePointRulesRepository racePointRulesRepository;
    private final RaceResultAdminEditAuditRepository raceResultAdminEditAuditRepository;
    private final RefereeReportsRepository refereeReportsRepository;
    private final TournamentsRepository tournamentsRepository;
    private final RacesRepository racesRepository;
    private final BetsRepository betsRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final NotificationsRepository notificationsRepository;
    private final UsersRepository usersRepository;
    private final AuthService authService;
    private final RaceResultMapper raceResultMapper;
    private final RaceResultValidator raceResultValidator;
    private final RefereeRaceAuthorizationService refereeRaceAuthorizationService;

    @Override
    @Transactional(readOnly = true)
    public List<RaceResultListResponse> getAllResults() {
        return raceResultsRepository.findAll()
                .stream()
                .map(raceResultMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceResultResponse> getResultById(Integer id) {
        Races race = getRace(id);
        return raceResultsRepository.findByRaces_IdOrderByFinishPositionAsc(race.getId())
                .stream()
                .map(raceResultMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceResultListResponse> getResultsByRace(Integer raceId) {
        Races race = getRace(raceId);
        return raceResultsRepository.findByRaces_IdOrderByFinishPositionAsc(race.getId())
                .stream()
                .sorted(resultComparator())
                .map(raceResultMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional
    public RaceResultResponse createResult(RaceResultCreateRequest request) {
        raceResultValidator.ensureNoManagedCreateFields(request);
        JockeyHorseAssignments assignment = validateCreateReferences(request);
        RaceResults result = raceResultMapper.toEntity(request, assignment);
        raceResultValidator.validateSingleResult(result);

        RaceResults savedResult = raceResultsRepository.save(result);
        rerankRaceResults(savedResult.getRaces().getId());

        return raceResultMapper.toResponse(savedResult);
    }

    @Override
    @Transactional
    public RaceResultResponse updateResult(Integer id, RaceResultUpdateRequest request) {
        raceResultValidator.ensureNoManagedUpdateFields(request);
        RaceResults result = findResult(id);
        raceResultValidator.ensureNotPublished(result);
        JockeyHorseAssignments assignment = validateUpdateReferences(request);
        raceResultMapper.updateResult(result, request, assignment);
        raceResultValidator.validateSingleResult(result);

        RaceResults savedResult = raceResultsRepository.save(result);
        rerankRaceResults(savedResult.getRaces().getId());

        return raceResultMapper.toResponse(savedResult);
    }

    @Override
    @Transactional
    public RaceResultResponse publishResult(Integer id, RaceResultPublishRequest request) {
        throw new BadRequestException("Use race result publish workflow to publish results");
    }

    @Override
    @Transactional
    public List<RaceResultListResponse> calculateResultsFromRounds(Integer raceId) {
        Races race = getRace(raceId);
        if (raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(race.getId(), RaceResultStatus.PUBLISHED.getValue())) {
            throw new BadRequestException("Race results are already published");
        }
        if (raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(race.getId(), RaceResultStatus.CONFIRMED.getValue())) {
            throw new BadRequestException("Confirmed race results cannot be recalculated from laps");
        }

        List<RaceRounds> rounds = raceRoundsRepository.findByRaces_IdOrderByAssignment_IdAscRoundNumberAsc(raceId);
        if (rounds.isEmpty()) {
            throw new BadRequestException("Race has no rounds to calculate results");
        }

        Map<Integer, AssignmentLapSummary> summariesByAssignment = new LinkedHashMap<>();
        for (RaceRounds round : rounds) {
            if (round.getLapTimeSec() == null) {
                throw new BadRequestException("All race rounds must have lap time before calculating results");
            }

            Integer assignmentId = round.getAssignment().getId();
            AssignmentLapSummary summary = summariesByAssignment.computeIfAbsent(
                    assignmentId,
                    ignored -> new AssignmentLapSummary(round.getAssignment())
            );
            summary.totalTimeSec = summary.totalTimeSec.add(round.getLapTimeSec());
            summary.updateLatestRound(round);
        }

        List<AssignmentLapSummary> rankedSummaries = new ArrayList<>(summariesByAssignment.values());
        rankedSummaries.sort(Comparator
                .comparing(AssignmentLapSummary::getTotalTimeSec)
                .thenComparingInt(summary -> summary.assignment.getId()));

        List<RaceResults> results = new ArrayList<>();
        for (AssignmentLapSummary summary : rankedSummaries) {
            RaceResults result = raceResultsRepository
                    .findByRaces_IdAndAssignment_Id(raceId, summary.assignment.getId())
                    .orElseGet(() -> newResultFromAssignment(summary.assignment, null));

            result.setFinalRound(summary.latestRoundNumber);
            result.setFinishTimeSec(summary.totalTimeSec);
            if (result.getStatus() == null || result.getStatus().isBlank()) {
                result.setStatus(RaceResultStatus.DRAFT.getValue());
            }
            if (result.getRecordedAt() == null) {
                result.setRecordedAt(Instant.now());
            }

            results.add(result);
        }

        assignFinishPositionsByTime(results);
        calculatePoints(race.getId(), results);
        List<RaceResults> savedResults = raceResultsRepository.saveAll(results);

        return savedResults.stream()
                .sorted(resultComparator())
                .map(raceResultMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional
    public RaceResultDraftResponse createDraft(Integer raceId, RaceResultDraftRequest request) {
        Races race = getRace(raceId);
        RaceRefereeAssignments refereeAssignment = refereeRaceAuthorizationService.requireChiefReferee(race.getId());
        RefereeProfiles referee = refereeAssignment.getReferee();
        raceResultValidator.ensureRaceInProgressForResults(race);
        raceResultValidator.validateDraftRequest(request);

        if (hasActiveResults(race.getId())) {
            throw new BadRequestException("Race result draft already exists");
        }

        List<RaceResults> savedResults = saveDraftResults(race, referee, request);
        return toDraftResponse(race, refereeAssignment, request.getReportId(), savedResults);
    }

    @Override
    @Transactional
    public RaceResultDraftResponse replaceDraft(Integer raceId, RaceResultDraftRequest request) {
        Races race = getRace(raceId);
        RaceRefereeAssignments refereeAssignment = refereeRaceAuthorizationService.requireChiefReferee(race.getId());
        RefereeProfiles referee = refereeAssignment.getReferee();
        raceResultValidator.ensureRaceInProgressForResults(race);
        raceResultValidator.validateDraftRequest(request);
        raceResultValidator.ensureNoPublishedResults(race.getId());

        List<RaceResults> existingResults = activeResults(race.getId());
        if (existingResults.stream().anyMatch(result -> RaceResultStatus.CONFIRMED.equalsValue(result.getStatus()))) {
            throw new BadRequestException("Only draft results can be replaced");
        }
        raceResultsRepository.deleteAll(existingResults);
        raceResultsRepository.flush();

        List<RaceResults> savedResults = saveDraftResults(race, referee, request);
        return toDraftResponse(race, refereeAssignment, request.getReportId(), savedResults);
    }

    @Override
    @Transactional
    public RaceResultDraftResponse replaceDraftByAdmin(Integer raceId, RaceResultDraftRequest request) {
        Races race = getRace(raceId);
        raceResultValidator.ensureRaceInProgressForResults(race);
        raceResultValidator.validateDraftRequest(request);
        raceResultValidator.ensureNoPublishedResults(race.getId());

        List<RaceResults> existingResults = activeResults(race.getId());
        if (existingResults.isEmpty()) {
            throw new ResourceNotFoundException("Race result draft not found");
        }
        boolean isConfirmedResult = existingResults.stream()
                .allMatch(result -> RaceResultStatus.CONFIRMED.equalsValue(result.getStatus()));
        boolean isDraftResult = existingResults.stream()
                .allMatch(result -> RaceResultStatus.DRAFT.equalsValue(result.getStatus()));
        if (!isConfirmedResult && !isDraftResult) {
            throw new BadRequestException("Only draft or chief-confirmed results can be edited");
        }

        String previousResultsJson = isConfirmedResult ? toAuditSnapshot(existingResults) : null;
        RefereeReports report = resolveAdminDraftReport(request, existingResults, race.getId());
        raceResultsRepository.deleteAll(existingResults);
        raceResultsRepository.flush();

        List<RaceResults> savedResults = saveDraftResultsForAdmin(
                race,
                report,
                request,
                isConfirmedResult ? RaceResultStatus.CONFIRMED.getValue() : RaceResultStatus.DRAFT.getValue()
        );
        if (isConfirmedResult) {
            saveAdminEditAudit(race, previousResultsJson, toAuditSnapshot(savedResults));
        }
        return toAdminDraftResponse(race, report, savedResults);
    }

    @Override
    @Transactional(readOnly = true)
    public RaceResultDraftResponse getDraft(Integer raceId) {
        Races race = getRace(raceId);
        RaceRefereeAssignments assignment = refereeRaceAuthorizationService.requireAssignedReferee(race.getId());
        List<RaceResults> results = activeResults(race.getId());
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Race result draft not found");
        }

        Integer reportId = results.stream()
                .map(RaceResults::getReport)
                .filter(Objects::nonNull)
                .map(RefereeReports::getId)
                .findFirst()
                .orElse(null);
        return toDraftResponse(race, assignment, reportId, results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceResultResponse> getAdminResults(Integer raceId) {
        Races race = getRace(raceId);
        return raceResultsRepository.findByRaces_IdOrderByFinishPositionAsc(race.getId())
                .stream()
                .sorted(resultComparator())
                .map(raceResultMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<RaceResultResponse> confirmResults(Integer raceId) {
        Races race = getRace(raceId);
        refereeRaceAuthorizationService.requireChiefReferee(race.getId());
        raceResultValidator.ensureRaceInProgressForResults(race);
        raceResultValidator.ensureNoPublishedResults(race.getId());

        List<RaceResults> results = activeResults(race.getId());
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Race result draft not found");
        }
        if (results.stream().anyMatch(result -> !RaceResultStatus.DRAFT.equalsValue(result.getStatus()))) {
            throw new BadRequestException("Only draft results can be confirmed");
        }

        raceResultValidator.validateResultCompleteness(confirmedAssignments(race.getId()), results);
        assignFinishPositionsByTime(results);
        raceResultValidator.validateResultPositions(results);
        calculatePoints(race.getId(), results);
        raceResultValidator.ensureExactlyOneValidWinner(results, "Race must have exactly one valid winner before confirming");

        results.forEach(result -> result.setStatus(RaceResultStatus.CONFIRMED.getValue()));
        return raceResultsRepository.saveAll(results)
                .stream()
                .sorted(resultComparator())
                .map(raceResultMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelResults(Integer raceId, RaceResultCancelRequest request) {
        Races race = getRace(raceId);
        raceResultValidator.ensureRaceInProgressForResults(race);
        raceResultValidator.ensureNoPublishedResults(race.getId());

        List<RaceResults> results = activeResults(race.getId());
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Race result draft not found");
        }
        if (results.stream().anyMatch(result -> RaceResultStatus.PUBLISHED.equalsValue(result.getStatus()))) {
            throw new BadRequestException("Published results cannot be cancelled");
        }

        results.forEach(result -> result.setStatus(RaceResultStatus.CANCELLED.getValue()));
        raceResultsRepository.saveAll(results);
    }

    @Override
    @Transactional
    public RacePublishResponse publishRaceResults(Integer raceId) {
        Races race = getRace(raceId);
        if (RaceStatus.COMPLETED.equalsValue(race.getStatus())
                || raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(race.getId(), RaceResultStatus.PUBLISHED.getValue())) {
            throw new BadRequestException("Race results have already been published");
        }
        raceResultValidator.ensureRaceInProgressForResults(race);

        List<RaceResults> results = activeResults(race.getId());
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Race result draft not found");
        }
        if (results.stream().anyMatch(result -> !RaceResultStatus.CONFIRMED.equalsValue(result.getStatus()))) {
            throw new BadRequestException("Only confirmed results can be published");
        }

        raceResultValidator.validateResultCompleteness(confirmedAssignments(race.getId()), results);
        assignFinishPositionsByTime(results);
        raceResultValidator.validateResultPositions(results);
        calculatePoints(race.getId(), results);
        RaceResults winner = raceResultValidator.ensureExactlyOneValidWinner(results, "Race must have exactly one valid winner before publishing");

        Instant publishedAt = Instant.now();
        for (RaceResults result : results) {
            result.setStatus(RaceResultStatus.PUBLISHED.getValue());
            result.setPublishedAt(publishedAt);
            applyRanking(result);
        }

        BetSettlementSummary settlementSummary = settleBets(race, winner);
        race.setStatus(RaceStatus.COMPLETED.getValue());

        raceResultsRepository.saveAll(results);
        racesRepository.save(race);
        sendPublishNotifications(race, results, settlementSummary);

        return RacePublishResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .raceStatus(race.getStatus())
                .publishedAt(publishedAt)
                .totalResults(results.size())
                .winnerHorseId(winner.getHorses().getId())
                .winnerHorseName(winner.getHorses().getName())
                .winnerJockeyId(winner.getAssignment().getJockey().getId())
                .winnerJockeyName(winner.getAssignment().getJockey().getUsers().getFullName())
                .totalBetsSettled(settlementSummary.totalBetsSettled)
                .totalRewardsPaid(settlementSummary.totalRewardsPaid)
                .message("Race results published successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceResultResponse> getPublicResults(Integer raceId) {
        Races race = getRace(raceId);
        List<RaceResults> results = raceResultsRepository
                .findByRaces_IdAndStatusIgnoreCaseOrderByFinishPositionAsc(race.getId(), RaceResultStatus.PUBLISHED.getValue());
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Published results are not available for this race");
        }

        return results.stream()
                .sorted(resultComparator())
                .map(raceResultMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResultResponse getPublicTournamentResults(Integer tournamentId) {
        Tournaments tournament = getTournament(tournamentId);
        List<RaceResults> results = raceResultsRepository
                .findByTournamentIdAndStatusOrderByRaceAndFinishPosition(
                        tournament.getId(),
                        RaceResultStatus.PUBLISHED.getValue()
                );

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Published results are not available for this tournament");
        }

        return buildTournamentResultResponse(tournament, results);
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResultResponse getAdminTournamentResults(Integer tournamentId, String status) {
        Tournaments tournament = getTournament(tournamentId);
        List<RaceResults> results;

        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.trim().toLowerCase();
            if (!RaceResultStatus.isValid(normalizedStatus)) {
                throw new BadRequestException("Invalid race result status");
            }
            results = raceResultsRepository.findByTournamentIdAndStatusOrderByRaceAndFinishPosition(
                    tournament.getId(),
                    normalizedStatus
            );
        } else {
            results = raceResultsRepository.findByTournamentIdOrderByRaceAndFinishPosition(tournament.getId());
        }

        return buildTournamentResultResponse(tournament, results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentResultHistoryResponse> getAdminTournamentResultHistory(String status, String resultStatus) {
        String tournamentStatusFilter = clean(status);
        if (tournamentStatusFilter != null) {
            tournamentStatusFilter = tournamentStatusFilter.toLowerCase();
            if (!TournamentStatus.isValid(tournamentStatusFilter)) {
                throw new BadRequestException("Invalid tournament status");
            }
        }

        String resultStatusFilter = clean(resultStatus);
        if (resultStatusFilter != null) {
            resultStatusFilter = resultStatusFilter.toLowerCase();
            if (!RaceResultStatus.isValid(resultStatusFilter)) {
                throw new BadRequestException("Invalid race result status");
            }
        }

        Map<Integer, List<RaceResults>> resultsByTournamentId = raceResultsRepository.findAll()
                .stream()
                .filter(result -> result.getRaces() != null
                        && result.getRaces().getSchedule() != null
                        && result.getRaces().getSchedule().getTournaments() != null)
                .collect(Collectors.groupingBy(result -> result.getRaces().getSchedule().getTournaments().getId()));

        String finalTournamentStatusFilter = tournamentStatusFilter;
        String finalResultStatusFilter = resultStatusFilter;
        return tournamentsRepository.findAllById(resultsByTournamentId.keySet())
                .stream()
                .filter(tournament -> finalTournamentStatusFilter == null
                        || finalTournamentStatusFilter.equalsIgnoreCase(tournament.getStatus()))
                .filter(tournament -> finalResultStatusFilter == null
                        || resultsByTournamentId.getOrDefault(tournament.getId(), List.of())
                        .stream()
                        .anyMatch(result -> finalResultStatusFilter.equalsIgnoreCase(result.getStatus())))
                .map(tournament -> toTournamentResultHistoryResponse(
                        tournament,
                        resultsByTournamentId.getOrDefault(tournament.getId(), List.of())
                ))
                .sorted(Comparator
                        .comparing(TournamentResultHistoryResponse::getLatestPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TournamentResultHistoryResponse::getEndDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TournamentResultHistoryResponse::getTournamentId, Comparator.reverseOrder()))
                .toList();
    }

    private List<RaceResults> saveDraftResults(Races race, RefereeProfiles referee, RaceResultDraftRequest request) {
        RefereeReports report = request.getReportId() == null ? null : getReportForDraft(request.getReportId(), race.getId(), referee.getId());
        List<JockeyHorseAssignments> confirmedAssignments = confirmedAssignments(race.getId());
        raceResultValidator.validateDraftItems(request.getResults(), confirmedAssignments);

        Map<Integer, JockeyHorseAssignments> assignmentsById = new HashMap<>();
        confirmedAssignments.forEach(assignment -> assignmentsById.put(assignment.getId(), assignment));

        List<RaceResults> results = new ArrayList<>();
        for (RaceResultDraftItemRequest item : request.getResults()) {
            JockeyHorseAssignments assignment = assignmentsById.get(item.getAssignmentId());
            RaceResults result = newResultFromAssignment(assignment, report);
            result.setFinishPosition(null);
            result.setFinishTimeSec(item.getFinishTimeSec());
            result.setIsDisqualified(Boolean.TRUE.equals(item.getIsDisqualified()));
            result.setDisqualifyReason(clean(item.getDisqualifyReason()));
            result.setPointsAwarded(0);
            results.add(result);
        }

        assignFinishPositionsByTime(results);
        return raceResultsRepository.saveAll(results);
    }

    private List<RaceResults> saveDraftResultsForAdmin(
            Races race,
            RefereeReports report,
            RaceResultDraftRequest request,
            String resultStatus
    ) {
        List<JockeyHorseAssignments> confirmedAssignments = confirmedAssignments(race.getId());
        raceResultValidator.validateDraftItems(request.getResults(), confirmedAssignments);

        Map<Integer, JockeyHorseAssignments> assignmentsById = new HashMap<>();
        confirmedAssignments.forEach(assignment -> assignmentsById.put(assignment.getId(), assignment));

        List<RaceResults> results = new ArrayList<>();
        for (RaceResultDraftItemRequest item : request.getResults()) {
            JockeyHorseAssignments assignment = assignmentsById.get(item.getAssignmentId());
            RaceResults result = newResultFromAssignment(assignment, report);
            result.setFinishPosition(null);
            result.setFinishTimeSec(item.getFinishTimeSec());
            result.setIsDisqualified(Boolean.TRUE.equals(item.getIsDisqualified()));
            result.setDisqualifyReason(clean(item.getDisqualifyReason()));
            result.setPointsAwarded(0);
            result.setStatus(resultStatus);
            results.add(result);
        }

        assignFinishPositionsByTime(results);
        return raceResultsRepository.saveAll(results);
    }

    private RefereeReports resolveAdminDraftReport(
            RaceResultDraftRequest request,
            List<RaceResults> existingResults,
            Integer raceId
    ) {
        if (request.getReportId() != null) {
            return getReportForAdminDraft(request.getReportId(), raceId);
        }

        return existingResults.stream()
                .map(RaceResults::getReport)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void calculatePoints(Integer raceId, List<RaceResults> results) {
        for (RaceResults result : results) {
            if (Boolean.TRUE.equals(result.getIsDisqualified())) {
                result.setPointsAwarded(0);
                continue;
            }

            Integer finishPosition = result.getFinishPosition();
            Integer points = racePointRulesRepository.findByRace_IdAndFinishPosition(raceId, finishPosition)
                    .map(RacePointRules::getPoints)
                    .orElseThrow(() -> new BadRequestException("Missing point rule for finish position"));
            result.setPointsAwarded(points == null ? 0 : points);
        }
    }

    private void saveAdminEditAudit(Races race, String previousResultsJson, String updatedResultsJson) {
        Users admin = authService.getCurrentUser();
        if (admin == null || admin.getId() == null) {
            throw new BadRequestException("Current admin user is required");
        }

        raceResultAdminEditAuditRepository.save(RaceResultAdminEditAudit.builder()
                .race(race)
                .editedBy(admin)
                .previousResultsJson(previousResultsJson)
                .updatedResultsJson(updatedResultsJson)
                .editedAt(Instant.now())
                .build());
    }

    private String toAuditSnapshot(List<RaceResults> results) {
        List<Map<String, Object>> snapshot = results.stream()
                .sorted(resultComparator())
                .map(result -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("resultId", result.getId());
                    item.put("assignmentId", result.getAssignment().getId());
                    item.put("horseId", result.getHorses().getId());
                    item.put("reportId", result.getReport() == null ? null : result.getReport().getId());
                    item.put("finishPosition", result.getFinishPosition());
                    item.put("finishTimeSec", result.getFinishTimeSec());
                    item.put("pointsAwarded", result.getPointsAwarded());
                    item.put("isDisqualified", result.getIsDisqualified());
                    item.put("disqualifyReason", result.getDisqualifyReason());
                    item.put("status", result.getStatus());
                    return item;
                })
                .toList();
        try {
            return AUDIT_OBJECT_MAPPER.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize race result audit", exception);
        }
    }

    private void rerankRaceResults(Integer raceId) {
        List<RaceResults> results = activeResults(raceId);
        assignFinishPositionsByTime(results);
        raceResultsRepository.saveAll(results);
    }

    private void assignFinishPositionsByTime(List<RaceResults> results) {
        results.stream()
                .filter(result -> Boolean.TRUE.equals(result.getIsDisqualified()))
                .forEach(result -> {
                    result.setFinishPosition(null);
                    result.setPointsAwarded(0);
                });

        boolean hasMissingFinishTime = results.stream()
                .filter(result -> !Boolean.TRUE.equals(result.getIsDisqualified()))
                .anyMatch(result -> result.getFinishTimeSec() == null);
        if (hasMissingFinishTime) {
            throw new BadRequestException("Finish time is required for non-disqualified result");
        }

        List<RaceResults> rankedResults = results.stream()
                .filter(result -> !Boolean.TRUE.equals(result.getIsDisqualified()))
                .sorted(Comparator
                        .comparing(RaceResults::getFinishTimeSec)
                        .thenComparing(result -> result.getAssignment().getId())
                        .thenComparing(RaceResults::getId, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        for (int index = 0; index < rankedResults.size(); index++) {
            rankedResults.get(index).setFinishPosition(index + 1);
        }
    }

    private void applyRanking(RaceResults result) {
        int points = result.getPointsAwarded() == null ? 0 : result.getPointsAwarded();
        Horses horse = result.getHorses();
        JockeyProfiles jockey = result.getAssignment().getJockey();

        horse.setRankingPoints((horse.getRankingPoints() == null ? 0 : horse.getRankingPoints()) + points);
        jockey.setRankingPoints((jockey.getRankingPoints() == null ? 0 : jockey.getRankingPoints()) + points);

        if (!Boolean.TRUE.equals(result.getIsDisqualified()) && Integer.valueOf(1).equals(result.getFinishPosition())) {
            horse.setTotalWins((horse.getTotalWins() == null ? 0 : horse.getTotalWins()) + 1);
            jockey.setTotalWins((jockey.getTotalWins() == null ? 0 : jockey.getTotalWins()) + 1);
        }
    }

    private BetSettlementSummary settleBets(Races race, RaceResults winner) {
        List<Bets> pendingBets = betsRepository.findByOption_Races_IdAndStatusIgnoreCase(
                race.getId(),
                BetStatus.PENDING.getValue()
        );
        BetSettlementSummary summary = new BetSettlementSummary();

        for (Bets bet : pendingBets) {
            boolean won = Objects.equals(bet.getOption().getHorses().getId(), winner.getHorses().getId());
            bet.setStatus(won ? BetStatus.WON.getValue() : BetStatus.LOST.getValue());
            bet.setSettledAt(Instant.now());

            if (won) {
                BigDecimal reward = safeMoney(bet.getBetPoints()).multiply(safeMoney(bet.getBetRate()));
                bet.setRewardPoints(reward);
                payReward(bet, reward);
                summary.totalRewardsPaid = summary.totalRewardsPaid.add(reward);
            } else {
                bet.setRewardPoints(BigDecimal.ZERO);
            }

            summary.totalBetsSettled++;
        }

        betsRepository.saveAll(pendingBets);
        return summary;
    }

    private void payReward(Bets bet, BigDecimal reward) {
        if (reward.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (walletTransactionsRepository.existsByRefTypeAndRefIdAndTxTypeIgnoreCase(
                REF_TYPE_BET,
                bet.getId(),
                WalletTransactionType.REWARD.getValue()
        )) {
            throw new BadRequestException("Bet settlement has already been completed");
        }

        Wallets wallet = walletsRepository.findByUsersId(bet.getUsers().getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found for spectator"));
        Wallets lockedWallet = walletsRepository.findFirstById(wallet.getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found for spectator"));

        if (!WalletStatus.ACTIVE.getValue().equalsIgnoreCase(lockedWallet.getStatus())) {
            throw new BadRequestException("Wallet is not active");
        }

        BigDecimal pointsBefore = safeMoney(lockedWallet.getPointBalance());
        BigDecimal pointsAfter = pointsBefore.add(reward);
        lockedWallet.setPointBalance(pointsAfter);

        WalletTransactions transaction = WalletTransactions.builder()
                .wallets(lockedWallet)
                .users(bet.getUsers())
                .txType(WalletTransactionType.REWARD.getValue())
                .cashAmount(BigDecimal.ZERO)
                .pointsAmount(reward)
                .exchangeRate(BigDecimal.ONE)
                .pointsBefore(pointsBefore)
                .pointsAfter(pointsAfter)
                .status(WalletTransactionStatus.COMPLETED.getValue())
                .refType(REF_TYPE_BET)
                .refId(bet.getId())
                .createdBy(bet.getUsers())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        walletsRepository.save(lockedWallet);
        walletTransactionsRepository.save(transaction);
    }

    private void sendPublishNotifications(Races race, List<RaceResults> results, BetSettlementSummary settlementSummary) {
        for (RaceResults result : results) {
            try {
                createNotification(
                        result.getOwner().getUsers().getId(),
                        "Race result published",
                        "Horse " + result.getHorses().getName()
                                + " finished position " + result.getFinishPosition()
                                + " and received " + result.getPointsAwarded() + " points.",
                        NOTIFICATION_TYPE_RACE_RESULT,
                        race.getId(),
                        REF_TYPE_RACE_RESULT
                );
                createNotification(
                        result.getAssignment().getJockey().getUsers().getId(),
                        "Race result published",
                        "Race " + race.getName()
                                + " result published. Position: " + result.getFinishPosition()
                                + ", points: " + result.getPointsAwarded() + ".",
                        NOTIFICATION_TYPE_RACE_RESULT,
                        race.getId(),
                        REF_TYPE_RACE_RESULT
                );
            } catch (Exception ex) {
                log.warn("Failed to create participant result notification for race {}", race.getId(), ex);
            }
        }

        try {
            for (Bets bet : betsRepository.findByOption_Races_IdAndStatusIgnoreCase(race.getId(), BetStatus.WON.getValue())) {
                createNotification(
                        bet.getUsers().getId(),
                        "Bet won",
                        "Your bet on race " + race.getName() + " won. Reward: " + bet.getRewardPoints() + " points.",
                        NOTIFICATION_TYPE_BET_RESULT,
                        bet.getId(),
                        REF_TYPE_BET
                );
            }
            for (Bets bet : betsRepository.findByOption_Races_IdAndStatusIgnoreCase(race.getId(), BetStatus.LOST.getValue())) {
                createNotification(
                        bet.getUsers().getId(),
                        "Bet lost",
                        "Your bet on race " + race.getName() + " did not win.",
                        NOTIFICATION_TYPE_BET_RESULT,
                        bet.getId(),
                        REF_TYPE_BET
                );
            }
        } catch (Exception ex) {
            log.warn("Failed to create bet result notifications for race {}", race.getId(), ex);
        }
    }

    private void createNotification(Integer userId, String title, String message, String type, Integer refId, String refType) {
        Users user = usersRepository.getReferenceById(userId);
        Notifications notification = Notifications.builder()
                .users(user)
                .title(title)
                .message(message)
                .type(type)
                .refId(refId)
                .refType(refType)
                .isRead(false)
                .createdAt(Instant.now())
                .build();
        notificationsRepository.save(notification);
    }

    private RaceResults newResultFromAssignment(JockeyHorseAssignments assignment, RefereeReports report) {
        return RaceResults.builder()
                .assignment(assignment)
                .races(assignment.getRaces())
                .horses(assignment.getReg().getHorses())
                .owner(assignment.getReg().getOwner())
                .report(report)
                .pointsAwarded(0)
                .isDisqualified(false)
                .status(RaceResultStatus.DRAFT.getValue())
                .recordedAt(Instant.now())
                .build();
    }

    private RaceResultDraftResponse toDraftResponse(
            Races race,
            RaceRefereeAssignments refereeAssignment,
            Integer reportId,
            List<RaceResults> results
    ) {
        return RaceResultDraftResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .status(results.isEmpty() ? null : results.get(0).getStatus())
                .reportId(reportId)
                .submittedByRefereeId(refereeAssignment.getReferee().getId())
                .results(results.stream().sorted(resultComparator()).map(raceResultMapper::toResponse).toList())
                .build();
    }

    private RaceResultDraftResponse toAdminDraftResponse(
            Races race,
            RefereeReports report,
            List<RaceResults> results
    ) {
        return RaceResultDraftResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .status(results.isEmpty() ? null : results.get(0).getStatus())
                .reportId(report == null ? null : report.getId())
                .submittedByRefereeId(report == null || report.getReferee() == null ? null : report.getReferee().getId())
                .results(results.stream().sorted(resultComparator()).map(raceResultMapper::toResponse).toList())
                .build();
    }

    private RaceResults findResult(Integer id) {
        return raceResultsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race result not found"));
    }

    private JockeyHorseAssignments validateCreateReferences(RaceResultCreateRequest request) {
        JockeyHorseAssignments assignment = getAssignment(request.getAssignmentId());
        if (request.getReportId() != null) {
            raceResultValidator.ensureReportExists(request.getReportId());
        }
        return assignment;
    }

    private JockeyHorseAssignments validateUpdateReferences(RaceResultUpdateRequest request) {
        if (request.getReportId() != null) {
            raceResultValidator.ensureReportExists(request.getReportId());
        }
        if (request.getAssignmentId() != null) {
            return getAssignment(request.getAssignmentId());
        }
        return null;
    }

    private JockeyHorseAssignments getAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private Races getRace(Integer raceId) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }
        return racesRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private Tournaments getTournament(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }
        return tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
    }

    private boolean hasActiveResults(Integer raceId) {
        return activeResults(raceId).stream().findAny().isPresent();
    }

    private List<RaceResults> activeResults(Integer raceId) {
        return raceResultsRepository.findByRaces_IdOrderByFinishPositionAsc(raceId)
                .stream()
                .filter(result -> !RaceResultStatus.CANCELLED.equalsValue(result.getStatus()))
                .toList();
    }

    private List<JockeyHorseAssignments> confirmedAssignments(Integer raceId) {
        return jockeyHorseAssignmentsRepository.findByRaces_IdAndStatusIgnoreCase(
                raceId,
                JockeyAssignmentStatus.CONFIRMED.getValue()
        );
    }

    private TournamentResultResponse buildTournamentResultResponse(Tournaments tournament, List<RaceResults> results) {
        Map<Integer, List<RaceResults>> resultsByRaceId = results.stream()
                .collect(Collectors.groupingBy(
                        result -> result.getRaces().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TournamentRaceResultGroupResponse> raceGroups = resultsByRaceId.values()
                .stream()
                .map(this::toTournamentRaceResultGroup)
                .toList();

        return TournamentResultResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .status(tournament.getStatus())
                .location(tournament.getLocation())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .races(raceGroups)
                .standings(buildTournamentStandings(results))
                .build();
    }

    private TournamentRaceResultGroupResponse toTournamentRaceResultGroup(List<RaceResults> raceResults) {
        Races race = raceResults.get(0).getRaces();

        return TournamentRaceResultGroupResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .raceNumber(race.getRaceNumber())
                .raceStatus(race.getStatus())
                .scheduledAt(race.getScheduledAt())
                .distanceM(race.getDistanceM())
                .trackType(race.getTrackType())
                .results(raceResults.stream()
                        .sorted(resultComparator())
                        .map(raceResultMapper::toResponse)
                        .toList())
                .build();
    }

    private TournamentResultHistoryResponse toTournamentResultHistoryResponse(
            Tournaments tournament,
            List<RaceResults> results
    ) {
        long totalRaces = racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournament.getId()).size();
        long racesWithResults = results.stream()
                .map(RaceResults::getRaces)
                .filter(Objects::nonNull)
                .map(Races::getId)
                .distinct()
                .count();

        return TournamentResultHistoryResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .status(tournament.getStatus())
                .location(tournament.getLocation())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .totalRaces(totalRaces)
                .racesWithResults(racesWithResults)
                .draftResultCount(countResultsByStatus(results, RaceResultStatus.DRAFT))
                .confirmedResultCount(countResultsByStatus(results, RaceResultStatus.CONFIRMED))
                .publishedResultCount(countResultsByStatus(results, RaceResultStatus.PUBLISHED))
                .cancelledResultCount(countResultsByStatus(results, RaceResultStatus.CANCELLED))
                .latestPublishedAt(latestPublishedAt(results))
                .build();
    }

    private long countResultsByStatus(List<RaceResults> results, RaceResultStatus status) {
        return results.stream()
                .filter(result -> status.equalsValue(result.getStatus()))
                .count();
    }

    private Instant latestPublishedAt(List<RaceResults> results) {
        return results.stream()
                .filter(result -> RaceResultStatus.PUBLISHED.equalsValue(result.getStatus()))
                .map(RaceResults::getPublishedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private List<TournamentStandingResponse> buildTournamentStandings(List<RaceResults> results) {
        Map<Integer, StandingEntry> entriesByHorseId = new HashMap<>();

        results.stream()
                .filter(result -> RaceResultStatus.PUBLISHED.equalsValue(result.getStatus()))
                .filter(result -> !Boolean.TRUE.equals(result.getIsDisqualified()))
                .filter(result -> result.getFinishPosition() != null)
                .filter(result -> result.getHorses() != null)
                .filter(result -> result.getOwner() != null)
                .forEach(result -> entriesByHorseId
                        .computeIfAbsent(
                                result.getHorses().getId(),
                                ignored -> new StandingEntry(result.getHorses(), result.getOwner())
                        )
                        .addResult(result));

        List<StandingEntry> standingEntries = entriesByHorseId.values()
                .stream()
                .sorted(standingComparator())
                .toList();

        List<TournamentStandingResponse> standings = new ArrayList<>();
        for (int index = 0; index < standingEntries.size(); index++) {
            standings.add(toStandingResponse(standingEntries.get(index), index + 1));
        }

        return standings;
    }

    private TournamentStandingResponse toStandingResponse(StandingEntry entry, int rank) {
        Horses horse = entry.getHorse();
        HorseOwnerProfiles owner = entry.getOwner();
        Users ownerUser = owner.getUsers();

        return TournamentStandingResponse.builder()
                .rank(rank)
                .horseId(horse.getId())
                .horseName(horse.getName())
                .horseAvatarUrl(horse.getAvatarUrl())
                .ownerId(owner.getId())
                .ownerFullName(ownerUser == null ? null : ownerUser.getFullName())
                .ownerStableName(owner.getStableName())
                .totalPoints(entry.getTotalPoints())
                .winCount(entry.getWinCount())
                .bestFinishPosition(entry.getBestFinishPosition())
                .bestPaceSecondsPerMeter(entry.getBestPaceSecondsPerMeter())
                .build();
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

    private RefereeReports getReportForDraft(Integer reportId, Integer raceId, Integer refereeId) {
        RefereeReports report = refereeReportsRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Referee report not found"));
        raceResultValidator.ensureReportBelongsToRaceAndReferee(report, raceId, refereeId);
        return report;
    }

    private RefereeReports getReportForAdminDraft(Integer reportId, Integer raceId) {
        RefereeReports report = refereeReportsRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Referee report not found"));
        if (report.getRaces() == null || !Objects.equals(report.getRaces().getId(), raceId)) {
            throw new BadRequestException("Report does not belong to this race");
        }
        return report;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Comparator<RaceResults> resultComparator() {
        return Comparator
                .comparing(RaceResults::getFinishPosition, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(RaceResults::getId, Comparator.nullsLast(Integer::compareTo));
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private static class AssignmentLapSummary {
        private final JockeyHorseAssignments assignment;
        private BigDecimal totalTimeSec = BigDecimal.ZERO;
        private Integer latestRoundNumber;

        private AssignmentLapSummary(JockeyHorseAssignments assignment) {
            this.assignment = assignment;
        }

        private void updateLatestRound(RaceRounds round) {
            if (latestRoundNumber == null || round.getRoundNumber() > latestRoundNumber) {
                latestRoundNumber = round.getRoundNumber();
            }
        }

        private BigDecimal getTotalTimeSec() {
            return totalTimeSec;
        }
    }

    private static class StandingEntry {
        private final Horses horse;
        private final HorseOwnerProfiles owner;
        private int totalPoints;
        private int winCount;
        private int bestFinishPosition = Integer.MAX_VALUE;
        private BigDecimal bestPaceSecondsPerMeter;

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
    }

    private static class BetSettlementSummary {
        private Integer totalBetsSettled = 0;
        private BigDecimal totalRewardsPaid = BigDecimal.ZERO;
    }
}



