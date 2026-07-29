package com.group5.htms.mapper;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationListResponse;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.ChiefInspectionStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RaceRegistrationMapper {
    public RaceRegistrations toEntity(RaceRegistrationCreateRequest request) {
        return RaceRegistrations.builder()
                .tournaments(toTournament(request.getTournamentId()))
                .races(toRace(request.getRaceId()))
                .horses(toHorse(request.getHorseId()))
                .owner(toOwner(request.getOwnerId()))
                .gateNumber(request.getGateNumber())
                .status(RaceRegistrationStatus.PENDING.getValue())
                .ownerConfirmationStatus(RaceRegistrationStatus.PENDING.getValue())
                .chiefInspectionStatus(ChiefInspectionStatus.PENDING.getValue())
                .registeredAt(Instant.now())
                .build();
    }

    public void updateRegistration(RaceRegistrations registration, RaceRegistrationUpdateRequest request) {
        if (request.getTournamentId() != null) {
            registration.setTournaments(toTournament(request.getTournamentId()));
        }
        if (request.getRaceId() != null) {
            registration.setRaces(toRace(request.getRaceId()));
        }
        if (request.getHorseId() != null) {
            registration.setHorses(toHorse(request.getHorseId()));
        }
        if (request.getGateNumber() != null) {
            registration.setGateNumber(request.getGateNumber());
        }

    }

    public RaceRegistrationResponse toResponse(RaceRegistrations registration) {
        JockeyProfiles jockey = registration.getJockey();
        Users approvedBy = registration.getApprovedBy();
        RefereeProfiles chiefInspector = registration.getChiefInspectedBy();
        Users adminReviewer = registration.getAdminReviewedBy();

        return RaceRegistrationResponse.builder()
                .id(registration.getId())
                .regId(registration.getId())
                .tournamentId(registration.getTournaments().getId())
                .raceId(registration.getRaces().getId())
                .horseId(registration.getHorses().getId())
                .ownerId(registration.getOwner().getId())
                .jockeyId(registration.getJockey() == null ? null : registration.getJockey().getId())
                .gateNumber(registration.getGateNumber())
                .status(registration.getStatus())
                .ownerConfirmationStatus(registration.getOwnerConfirmationStatus())
                .ownerConfirmedAt(registration.getOwnerConfirmedAt())
                .chiefInspectionStatus(registration.getChiefInspectionStatus())
                .chiefInspectedById(chiefInspector == null ? null : chiefInspector.getId())
                .chiefInspectedByFullName(chiefInspector == null || chiefInspector.getUsers() == null
                        ? null : chiefInspector.getUsers().getFullName())
                .chiefInspectedAt(registration.getChiefInspectedAt())
                .chiefInspectionNote(registration.getChiefInspectionNote())
                .registeredAt(registration.getRegisteredAt())
                .approvedAt(registration.getApprovedAt())
                .approvedById(approvedBy == null ? null : approvedBy.getId())
                .adminReviewedById(adminReviewer == null ? null : adminReviewer.getId())
                .adminReviewedByFullName(adminReviewer == null ? null : adminReviewer.getFullName())
                .adminReviewedAt(registration.getAdminReviewedAt())
                .adminReviewNote(registration.getAdminReviewNote())
                .tournamentName(registration.getTournaments().getName())
                .raceName(registration.getRaces().getName())
                .raceStatus(registration.getRaces().getStatus())
                .raceNumber(registration.getRaces().getRaceNumber())
                .scheduledAt(registration.getRaces().getScheduledAt())
                .horseName(registration.getHorses().getName())
                .horseAvatarUrl(registration.getHorses().getAvatarUrl())
                .ownerFullName(registration.getOwner().getUsers().getFullName())
                .ownerStableName(registration.getOwner().getStableName())
                .jockeyFullName(jockey == null ? null : jockey.getUsers().getFullName())
                .jockeyStatus(jockey == null ? null : jockey.getStatus())
                .approvedByFullName(approvedBy == null ? null : approvedBy.getFullName())
                .build();
    }

    public RaceRegistrationListResponse toListResponse(RaceRegistrations registration) {
        JockeyProfiles jockey = registration.getJockey();
        RefereeProfiles chiefInspector = registration.getChiefInspectedBy();
        Users adminReviewer = registration.getAdminReviewedBy();

        return RaceRegistrationListResponse.builder()
                .regId(registration.getId())
                .tournamentId(registration.getTournaments().getId())
                .raceId(registration.getRaces().getId())
                .horseId(registration.getHorses().getId())
                .ownerId(registration.getOwner().getId())
                .jockeyId(jockey == null ? null : jockey.getId())
                .gateNumber(registration.getGateNumber())
                .status(registration.getStatus())
                .ownerConfirmationStatus(registration.getOwnerConfirmationStatus())
                .chiefInspectionStatus(registration.getChiefInspectionStatus())
                .chiefInspectedById(chiefInspector == null ? null : chiefInspector.getId())
                .chiefInspectedByFullName(chiefInspector == null || chiefInspector.getUsers() == null
                        ? null : chiefInspector.getUsers().getFullName())
                .chiefInspectedAt(registration.getChiefInspectedAt())
                .chiefInspectionNote(registration.getChiefInspectionNote())
                .adminReviewedById(adminReviewer == null ? null : adminReviewer.getId())
                .adminReviewedByFullName(adminReviewer == null ? null : adminReviewer.getFullName())
                .adminReviewedAt(registration.getAdminReviewedAt())
                .adminReviewNote(registration.getAdminReviewNote())
                .registeredAt(registration.getRegisteredAt())
                .tournamentName(registration.getTournaments().getName())
                .raceName(registration.getRaces().getName())
                .raceStatus(registration.getRaces().getStatus())
                .raceNumber(registration.getRaces().getRaceNumber())
                .scheduledAt(registration.getRaces().getScheduledAt())
                .horseName(registration.getHorses().getName())
                .horseAvatarUrl(registration.getHorses().getAvatarUrl())
                .ownerFullName(registration.getOwner().getUsers().getFullName())
                .ownerStableName(registration.getOwner().getStableName())
                .jockeyFullName(jockey == null ? null : jockey.getUsers().getFullName())
                .build();
    }

    private Tournaments toTournament(Integer id) {
        Tournaments tournament = new Tournaments();
        tournament.setId(id);
        return tournament;
    }

    private Races toRace(Integer id) {
        Races race = new Races();
        race.setId(id);
        return race;
    }

    private Horses toHorse(Integer id) {
        Horses horse = new Horses();
        horse.setId(id);
        return horse;
    }

    private HorseOwnerProfiles toOwner(Integer id) {
        HorseOwnerProfiles owner = new HorseOwnerProfiles();
        owner.setId(id);
        return owner;
    }


}

