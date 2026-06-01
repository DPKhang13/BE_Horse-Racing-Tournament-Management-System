package com.group5.htms.service.impl;

import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.mapper.TournamentMapper;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentsRepository tournamentsRepository;
    private final UsersRepository usersRepository;
    private final TournamentMapper tournamentMapper;

    @Override
    @Transactional
    public TournamentResponse createTournament(TournamentCreateRequest request) {
        validateCreateRequest(request);

        if (tournamentsRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Tournament name already exists");
        }

        Users currentUser = getCurrentUser();

        Tournaments tournament = tournamentMapper.toEntity(request);
        tournament.setCreatedBy(currentUser);

        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return tournamentMapper.toResponse(savedTournament);
    }

    @Override
    @Transactional
    public TournamentResponse updateTournament(Integer tournamentId, TournamentUpdateRequest request) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        validateUpdateRequest(tournament, request);

        tournamentMapper.updateEntity(tournament, request);

        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return tournamentMapper.toResponse(savedTournament);
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResponse getTournamentById(Integer tournamentId) {
        return tournamentMapper.toResponse(getTournamentEntity(tournamentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentResponse> getAllTournaments(String status) {
        List<Tournaments> tournaments;

        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.trim().toLowerCase();

            if (!TournamentStatus.isValid(normalizedStatus)) {
                throw new BadRequestException("Invalid tournament status");
            }

            tournaments = tournamentsRepository
                    .findByStatusIgnoreCaseOrderByStartDateAsc(normalizedStatus);
        } else {
            tournaments = tournamentsRepository.findAllByOrderByStartDateAsc();
        }

        return tournaments.stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

//    @Override
//    @Transactional
//    public void deleteTournament(Integer tournamentId) {
//        Tournaments tournament = getTournamentEntity(tournamentId);
//
//        tournamentsRepository.delete(tournament);
//    }

    @Override
    @Transactional
    public TournamentResponse cancelTournament(Integer tournamentId) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        if (TournamentStatus.CANCELLED.getValue().equalsIgnoreCase(tournament.getStatus())) {
            throw new BadRequestException("Tournament is already cancelled");
        }

        if (TournamentStatus.COMPLETED.getValue().equalsIgnoreCase(tournament.getStatus())) {
            throw new BadRequestException("Completed tournament cannot be cancelled");
        }

        tournament.setStatus(TournamentStatus.CANCELLED.getValue());

        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return tournamentMapper.toResponse(savedTournament);
    }

    private Tournaments getTournamentEntity(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        return tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private void validateCreateRequest(TournamentCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Tournament request is required");
        }

        validateDateRange(request.getStartDate(), request.getEndDate());

        if (request.getStatus() != null
                && !request.getStatus().isBlank()
                && !TournamentStatus.isValid(request.getStatus())) {
            throw new BadRequestException("Invalid tournament status");
        }
    }

    private void validateUpdateRequest(
            Tournaments existingTournament,
            TournamentUpdateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException("Tournament update request is required");
        }

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : existingTournament.getStartDate();

        LocalDate endDate = request.getEndDate() != null
                ? request.getEndDate()
                : existingTournament.getEndDate();

        validateDateRange(startDate, endDate);

        if (request.getStatus() != null
                && !request.getStatus().isBlank()
                && !TournamentStatus.isValid(request.getStatus())) {
            throw new BadRequestException("Invalid tournament status");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BadRequestException("Start date is required");
        }

        if (endDate == null) {
            throw new BadRequestException("End date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after or equal to start date");
        }
    }
}