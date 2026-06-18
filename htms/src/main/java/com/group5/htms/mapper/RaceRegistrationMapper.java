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
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
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
                .jockey(toNullableJockey(request.getJockeyId()))
                .status("pending")
                .ownerConfirmationStatus("pending")
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
        if (request.getOwnerId() != null) {
            registration.setOwner(toOwner(request.getOwnerId()));
        }
        if (request.getJockeyId() != null) {
            registration.setJockey(toJockey(request.getJockeyId()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            registration.setStatus(request.getStatus().trim());
        }
        if (request.getOwnerConfirmationStatus() != null && !request.getOwnerConfirmationStatus().isBlank()) {
            registration.setOwnerConfirmationStatus(request.getOwnerConfirmationStatus().trim());
        }
        if (request.getOwnerConfirmedAt() != null) {
            registration.setOwnerConfirmedAt(request.getOwnerConfirmedAt());
        }
        if (request.getRegisteredAt() != null) {
            registration.setRegisteredAt(request.getRegisteredAt());
        }
        if (request.getApprovedAt() != null) {
            registration.setApprovedAt(request.getApprovedAt());
        }
        if (request.getApprovedById() != null) {
            registration.setApprovedBy(toUser(request.getApprovedById()));
        }
    }

    public RaceRegistrationResponse toResponse(RaceRegistrations registration) {
        JockeyProfiles jockey = registration.getJockey();
        Users approvedBy = registration.getApprovedBy();

        return RaceRegistrationResponse.builder()
                .id(registration.getId())
                .regId(registration.getId())
                .tournamentId(registration.getTournaments().getId())
                .raceId(registration.getRaces().getId())
                .horseId(registration.getHorses().getId())
                .ownerId(registration.getOwner().getId())
                .jockeyId(registration.getJockey() == null ? null : registration.getJockey().getId())
                .status(registration.getStatus())
                .ownerConfirmationStatus(registration.getOwnerConfirmationStatus())
                .ownerConfirmedAt(registration.getOwnerConfirmedAt())
                .registeredAt(registration.getRegisteredAt())
                .approvedAt(registration.getApprovedAt())
                .approvedById(approvedBy == null ? null : approvedBy.getId())
                .tournamentName(registration.getTournaments().getName())
                .raceName(registration.getRaces().getName())
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

        return RaceRegistrationListResponse.builder()
                .regId(registration.getId())
                .tournamentId(registration.getTournaments().getId())
                .raceId(registration.getRaces().getId())
                .horseId(registration.getHorses().getId())
                .ownerId(registration.getOwner().getId())
                .jockeyId(jockey == null ? null : jockey.getId())
                .status(registration.getStatus())
                .ownerConfirmationStatus(registration.getOwnerConfirmationStatus())
                .registeredAt(registration.getRegisteredAt())
                .tournamentName(registration.getTournaments().getName())
                .raceName(registration.getRaces().getName())
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

    private JockeyProfiles toJockey(Integer id) {
        JockeyProfiles jockey = new JockeyProfiles();
        jockey.setId(id);
        return jockey;
    }

    private JockeyProfiles toNullableJockey(Integer id) {
        return id == null ? null : toJockey(id);
    }

    private Users toUser(Integer id) {
        Users user = new Users();
        user.setId(id);
        return user;
    }

    private Users toNullableUser(Integer id) {
        return id == null ? null : toUser(id);
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.now() : value;
    }
}
