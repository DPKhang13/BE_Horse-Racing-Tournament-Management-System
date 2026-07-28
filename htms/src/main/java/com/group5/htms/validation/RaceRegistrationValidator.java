package com.group5.htms.validation;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.HorseStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RaceRegistrationValidator {
    public void ensureRaceBelongsToTournament(Races race, Integer tournamentId) {
        if (race == null || race.getSchedule() == null || race.getSchedule().getTournaments() == null
                || !Objects.equals(race.getSchedule().getTournaments().getId(), tournamentId)) {
            throw new BadRequestException("Race does not belong to this tournament");
        }
    }

    public void ensureHorseBelongsToOwner(Horses horse, Integer ownerId) {
        if (horse == null || horse.getOwner() == null || !Objects.equals(horse.getOwner().getId(), ownerId)) {
            throw new BadRequestException("Horse does not belong to current owner");
        }
    }

    public void ensureHorseRankGroupMatchesRace(Horses horse, Races race) {
        if (horse == null || race == null || !Objects.equals(horse.getRankGroup(), race.getRankGroup())) {
            throw new BadRequestException("Horse rank group does not match race rank group");
        }
    }

    public void ensureHorseActive(Horses horse) {
        if (horse == null || !HorseStatus.ACTIVE.getValue().equalsIgnoreCase(horse.getStatus())) {
            throw new BadRequestException("Horse must be active to register for race");
        }
    }

    public void ensureOwnerCanManageRegistration(RaceRegistrations registration, Integer ownerId) {
        if (registration == null || registration.getOwner() == null
                || !Objects.equals(registration.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this race registration");
        }
    }

    public void ensureRegistrationOpen(Tournaments tournament, Races race) {
        if (tournament == null || !TournamentStatus.REGISTRATION_OPEN.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Registration is not open for this tournament");
        }
        if (race == null || !RaceStatus.REGISTRATION_OPEN.equalsValue(race.getStatus())) {
            throw new BadRequestException("Registration is not open for this race");
        }
    }

    public void ensureNoWorkflowFields(RaceRegistrationUpdateRequest request) {
        if (request.getOwnerId() != null) {
            throw new BadRequestException("Owner is taken from current authenticated user");
        }
        if (request.getJockeyId() != null) {
            throw new BadRequestException("Use jockey assignment workflow to update jockey");
        }
        if (hasText(request.getStatus())) {
            throw new BadRequestException("Use workflow transition APIs to update status");
        }
        if (hasText(request.getOwnerConfirmationStatus())) {
            throw new BadRequestException("Use workflow transition APIs to update owner confirmation status");
        }
        if (request.getRegisteredAt() != null
                || request.getOwnerConfirmedAt() != null
                || request.getApprovedAt() != null
                || request.getApprovedById() != null) {
            throw new BadRequestException("Workflow timestamps are managed by backend");
        }
    }

    public void ensureApproveStatusRequested(String status) {
        if (!RaceRegistrationStatus.APPROVED.equalsValue(status)) {
            throw new BadRequestException("Use workflow transition APIs to update status");
        }
    }

    public void ensureHorseNotRegisteredInRace(boolean exists) {
        if (exists) {
            throw new BadRequestException("Horse is already registered in this race");
        }
    }

    public void ensureHorseHasNoScheduleConflict(boolean exists) {
        if (exists) {
            throw new BadRequestException("Horse is already registered in another race at the same time");
        }
    }

    public void ensureCanApprove(RaceRegistrations registration) {
        if (!RaceRegistrationStatus.PENDING.equalsValue(registration.getStatus())) {
            throw new BadRequestException("Only pending registrations can be approved");
        }
        if (registration.getJockey() == null) {
            throw new BadRequestException("Registration must have a confirmed jockey before admin approval");
        }
        if (!RaceRegistrationStatus.CONFIRMED.equalsValue(registration.getOwnerConfirmationStatus())) {
            throw new BadRequestException("Owner must confirm jockey assignment before admin approval");
        }
    }

    public void ensureCanReject(RaceRegistrations registration) {
        if (!RaceRegistrationStatus.PENDING.equalsValue(registration.getStatus())) {
            throw new BadRequestException("Only pending registrations can be rejected");
        }
    }

    public void ensureCanCancel(RaceRegistrations registration) {
        if (RaceRegistrationStatus.REJECTED.equalsValue(registration.getStatus())
                || RaceRegistrationStatus.CANCELLED.equalsValue(registration.getStatus())) {
            throw new BadRequestException("Registration is already closed");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

