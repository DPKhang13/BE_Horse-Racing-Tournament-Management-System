package com.group5.htms.mapper;

import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class HorseMapper {
    public Horses toEntity(HorseCreateRequest request) {
        return Horses.builder()
                .owner(toOwner(request.getOwnerId()))
                .name(request.getName())
                .breed(trim(request.getBreed()))
                .age(request.getAge())
                .weightKg(request.getWeightKg())
                .rankGroup(trim(request.getRankGroup()))
                .rankingPoints(defaultZero(request.getRankingPoints()))
                .avatarUrl(trim(request.getAvatarUrl()))
                .totalWins(defaultZero(request.getTotalWins()))
                .status(defaultStatus(request.getStatus()))
                .registeredAt(defaultRegisteredAt(request.getRegisteredAt()))
                .build();
    }

    public void updateHorse(Horses horse, HorseUpdateRequest request) {
        if (request.getOwnerId() != null) {
            horse.setOwner(toOwner(request.getOwnerId()));
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            horse.setName(request.getName().trim());
        }
        if (request.getBreed() != null) {
            horse.setBreed(trim(request.getBreed()));
        }
        if (request.getAge() != null) {
            horse.setAge(request.getAge());
        }
        if (request.getWeightKg() != null) {
            horse.setWeightKg(request.getWeightKg());
        }
        if (request.getRankGroup() != null) {
            horse.setRankGroup(trim(request.getRankGroup()));
        }
        if (request.getRankingPoints() != null) {
            horse.setRankingPoints(request.getRankingPoints());
        }
        if (request.getAvatarUrl() != null) {
            horse.setAvatarUrl(trim(request.getAvatarUrl()));
        }
        if (request.getTotalWins() != null) {
            horse.setTotalWins(request.getTotalWins());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            horse.setStatus(request.getStatus().trim());
        }
        if (request.getRegisteredAt() != null) {
            horse.setRegisteredAt(request.getRegisteredAt());
        }
    }

    public HorseResponse toResponse(Horses horse) {
        return HorseResponse.builder()
                .id(horse.getId())
                .ownerId(horse.getOwner().getId())
                .name(horse.getName())
                .breed(horse.getBreed())
                .age(horse.getAge())
                .weightKg(horse.getWeightKg())
                .rankGroup(horse.getRankGroup())
                .rankingPoints(horse.getRankingPoints())
                .avatarUrl(horse.getAvatarUrl())
                .totalWins(horse.getTotalWins())
                .status(horse.getStatus())
                .registeredAt(horse.getRegisteredAt())
                .build();
    }

    private HorseOwnerProfiles toOwner(Integer ownerId) {
        HorseOwnerProfiles owner = new HorseOwnerProfiles();
        owner.setId(ownerId);
        return owner;
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultStatus(String value) {
        return value == null || value.isBlank() ? "active" : value.trim();
    }

    private Instant defaultRegisteredAt(Instant value) {
        return value == null ? Instant.now() : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
