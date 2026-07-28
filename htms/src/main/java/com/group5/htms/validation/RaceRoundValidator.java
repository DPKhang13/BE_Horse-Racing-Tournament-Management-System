package com.group5.htms.validation;

import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class RaceRoundValidator {
    public void ensureRoundNumberWithinLapCount(Integer roundNumber, JockeyHorseAssignments assignment) {
        Integer lapCount = assignment.getRaces().getLapCount();
        if (lapCount != null && roundNumber > lapCount) {
            throw new BadRequestException("Round number must not be greater than race lap count");
        }
    }

    public void ensureUniqueAssignmentRound(boolean duplicateAssignmentRound) {
        if (duplicateAssignmentRound) {
            throw new BadRequestException("This assignment already has a result for this round");
        }
    }

    public void ensureUniqueRoundPosition(boolean duplicatePosition) {
        if (duplicatePosition) {
            throw new BadRequestException("This position is already used in this race round");
        }
    }
}
