package com.group5.htms.mapper;

import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseListResponse;
import com.group5.htms.dto.horse.response.HorseRankingResponse;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.HorseStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
                .rankingPoints(0)
                .avatarUrl(trim(request.getAvatarUrl()))
                .totalWins(0)
                .status(HorseStatus.ACTIVE.getValue())
                .registeredAt(Instant.now())
                .build();
    }

    public void updateHorse(Horses horse, HorseUpdateRequest request) {
        if (request.getOwnerId() != null) {
            horse.setOwner(toOwner(request.getOwnerId()));
        }
        if (hasUpdateValue(request.getName())) {
            horse.setName(request.getName().trim());
        }
        if (hasUpdateValue(request.getBreed())) {
            horse.setBreed(trim(request.getBreed()));
        }
        if (hasPositiveUpdateValue(request.getAge())) {
            horse.setAge(request.getAge());
        }
        if (hasPositiveUpdateValue(request.getWeightKg())) {
            horse.setWeightKg(request.getWeightKg());
        }
        if (hasUpdateValue(request.getRankGroup())) {
            horse.setRankGroup(trim(request.getRankGroup()));
        }
        if (hasPositiveUpdateValue(request.getRankingPoints())) {
            horse.setRankingPoints(request.getRankingPoints());
        }
        if (hasUpdateValue(request.getAvatarUrl())) {
            horse.setAvatarUrl(trim(request.getAvatarUrl()));
        }
        if (hasPositiveUpdateValue(request.getTotalWins())) {
            horse.setTotalWins(request.getTotalWins());
        }
        if (hasUpdateValue(request.getStatus())) {
            horse.setStatus(request.getStatus().trim());
        }
        if (request.getRegisteredAt() != null) {
            horse.setRegisteredAt(request.getRegisteredAt());
        }
    }

    public HorseResponse toResponse(Horses horse) {
        HorseOwnerProfiles owner = horse.getOwner();
        Users ownerUser = getOwnerUser(owner);

        return HorseResponse.builder()
                .id(horse.getId())
                .horseId(horse.getId())
                .ownerId(owner == null ? null : owner.getId())
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
                .ownerFullName(ownerUser == null ? null : ownerUser.getFullName())
                .ownerEmail(ownerUser == null ? null : ownerUser.getEmail())
                .ownerPhone(ownerUser == null ? null : ownerUser.getPhone())
                .ownerStableName(owner == null ? null : owner.getStableName())
                .ownerLicenseNumber(owner == null ? null : owner.getLicenseNumber())
                .build();
    }

    public HorseListResponse toListResponse(Horses horse) {
        HorseOwnerProfiles owner = horse.getOwner();
        Users ownerUser = getOwnerUser(owner);

        return HorseListResponse.builder()
                .horseId(horse.getId())
                .ownerId(owner == null ? null : owner.getId())
                .name(horse.getName())
                .breed(horse.getBreed())
                .rankGroup(horse.getRankGroup())
                .rankingPoints(horse.getRankingPoints())
                .avatarUrl(horse.getAvatarUrl())
                .totalWins(horse.getTotalWins())
                .status(horse.getStatus())
                .ownerFullName(ownerUser == null ? null : ownerUser.getFullName())
                .ownerStableName(owner == null ? null : owner.getStableName())
                .build();
    }

    public HorseRankingResponse toRankingResponse(Horses horse, Integer rank) {
        HorseOwnerProfiles owner = horse.getOwner();
        Users ownerUser = getOwnerUser(owner);

        return HorseRankingResponse.builder()
                .rank(rank)
                .id(horse.getId())
                .horseId(horse.getId())
                .ownerId(owner == null ? null : owner.getId())
                .name(horse.getName())
                .breed(horse.getBreed())
                .rankGroup(horse.getRankGroup())
                .rankingPoints(horse.getRankingPoints())
                .totalWins(horse.getTotalWins())
                .avatarUrl(horse.getAvatarUrl())
                .status(horse.getStatus())
                .ownerFullName(ownerUser == null ? null : ownerUser.getFullName())
                .ownerEmail(ownerUser == null ? null : ownerUser.getEmail())
                .ownerPhone(ownerUser == null ? null : ownerUser.getPhone())
                .ownerStableName(owner == null ? null : owner.getStableName())
                .ownerLicenseNumber(owner == null ? null : owner.getLicenseNumber())
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
        return value == null || value.isBlank() ? HorseStatus.ACTIVE.getValue() : value.trim();
    }

    private Instant defaultRegisteredAt(Instant value) {
        return value == null ? Instant.now() : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasUpdateValue(String value) {
        return value != null && !value.isBlank() && !"string".equalsIgnoreCase(value.trim());
    }

    private boolean hasPositiveUpdateValue(Integer value) {
        return value != null && value > 0;
    }

    private boolean hasPositiveUpdateValue(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private Users getOwnerUser(HorseOwnerProfiles owner) {
        return owner == null ? null : owner.getUsers();
    }
}

