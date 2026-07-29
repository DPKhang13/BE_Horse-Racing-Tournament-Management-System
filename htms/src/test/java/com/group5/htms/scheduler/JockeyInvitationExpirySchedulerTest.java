package com.group5.htms.scheduler;

import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JockeyInvitationExpirySchedulerTest {
    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Test
    void expirePendingInvitationsMarksExpiredAssignments() {
        JockeyHorseAssignments assignment = JockeyHorseAssignments.builder()
                .id(50)
                .status(JockeyAssignmentStatus.PENDING.getValue())
                .responseDeadline(Instant.now().minusSeconds(1))
                .build();
        when(jockeyHorseAssignmentsRepository.findByStatusIgnoreCaseAndResponseDeadlineLessThanEqual(
                eq(JockeyAssignmentStatus.PENDING.getValue()),
                any(Instant.class)
        )).thenReturn(List.of(assignment));
        JockeyInvitationExpiryScheduler scheduler = new JockeyInvitationExpiryScheduler(jockeyHorseAssignmentsRepository);

        scheduler.expirePendingInvitations();

        assertThat(assignment.getStatus()).isEqualTo(JockeyAssignmentStatus.EXPIRED.getValue());
        assertThat(assignment.getExpiredAt()).isNotNull();
        verify(jockeyHorseAssignmentsRepository).saveAll(List.of(assignment));
    }
}

