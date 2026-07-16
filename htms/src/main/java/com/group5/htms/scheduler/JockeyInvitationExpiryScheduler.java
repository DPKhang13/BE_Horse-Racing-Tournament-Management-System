package com.group5.htms.scheduler;

import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JockeyInvitationExpiryScheduler {
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expirePendingInvitations() {
        Instant now = Instant.now();
        List<JockeyHorseAssignments> expiredAssignments = jockeyHorseAssignmentsRepository
                .findByStatusIgnoreCaseAndResponseDeadlineLessThanEqual(
                        JockeyAssignmentStatus.PENDING.getValue(),
                        now
                );

        expiredAssignments.forEach(assignment -> {
            assignment.setStatus(JockeyAssignmentStatus.EXPIRED.getValue());
            assignment.setExpiredAt(now);
        });

        if (!expiredAssignments.isEmpty()) {
            jockeyHorseAssignmentsRepository.saveAll(expiredAssignments);
        }
    }
}
