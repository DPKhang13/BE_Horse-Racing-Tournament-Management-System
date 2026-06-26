package com.group5.htms.validation;

import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.entity.Horses;
import com.group5.htms.exception.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class HorseValidator {
    public void ensureOwnerCanManageHorse(Horses horse, Integer ownerId) {
        if (horse == null || horse.getOwner() == null || !Objects.equals(horse.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this horse");
        }
    }

    public void ensureNoBackendManagedFields(HorseUpdateRequest request) {
        if (request.getOwnerId() != null) {
            throw new BadRequestException("Owner is taken from current authenticated user");
        }
        if (request.getRankingPoints() != null
                || request.getTotalWins() != null
                || hasText(request.getStatus())
                || request.getRegisteredAt() != null) {
            throw new BadRequestException("Horse ranking, wins, status and registered time are managed by backend");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}