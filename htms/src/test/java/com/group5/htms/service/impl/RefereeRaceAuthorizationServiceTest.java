package com.group5.htms.service.impl;

import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.RoleStatus;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefereeRaceAuthorizationServiceTest {
    @Mock
    private AuthService authService;
    @Mock
    private RefereeProfilesRepository refereeProfilesRepository;
    @Mock
    private RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;

    private RefereeRaceAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new RefereeRaceAuthorizationService(
                authService,
                refereeProfilesRepository,
                raceRefereeAssignmentsRepository
        );
    }

    @Test
    void requireChiefRefereeReturnsOnlyChiefAssignmentForTargetRace() {
        Users user = refereeUser(7);
        RefereeProfiles referee = RefereeProfiles.builder()
                .id(7)
                .users(user)
                .status(RoleStatus.ACTIVE.getValue())
                .build();
        RaceRefereeAssignments assignment = RaceRefereeAssignments.builder()
                .referee(referee)
                .refereeRole(RefereeRaceAuthorizationService.ROLE_CHIEF_REFEREE)
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(refereeProfilesRepository.findById(7)).thenReturn(Optional.of(referee));
        when(raceRefereeAssignmentsRepository.findByRaces_IdAndReferee_Id(10, 7))
                .thenReturn(Optional.of(assignment));

        assertThat(service.requireChiefReferee(10)).isSameAs(assignment);
    }

    @Test
    void requireChiefRefereeRejectsMainRefereeForSameRace() {
        Users user = refereeUser(7);
        RefereeProfiles referee = RefereeProfiles.builder()
                .id(7)
                .users(user)
                .status(RoleStatus.ACTIVE.getValue())
                .build();
        RaceRefereeAssignments assignment = RaceRefereeAssignments.builder()
                .referee(referee)
                .refereeRole(RefereeRaceAuthorizationService.ROLE_MAIN_REFEREE)
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(refereeProfilesRepository.findById(7)).thenReturn(Optional.of(referee));
        when(raceRefereeAssignmentsRepository.findByRaces_IdAndReferee_Id(10, 7))
                .thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service.requireChiefReferee(10))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only the chief referee assigned to this race can perform this action");
    }

    private Users refereeUser(Integer id) {
        return Users.builder()
                .id(id)
                .roleType(RoleType.RACE_REFEREE.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .build();
    }
}
