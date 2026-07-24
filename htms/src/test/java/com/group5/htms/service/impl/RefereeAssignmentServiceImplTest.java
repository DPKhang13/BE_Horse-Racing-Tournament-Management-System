package com.group5.htms.service.impl;

import com.group5.htms.dto.refereeassignment.request.RefereeAssignmentCreateRequest;
import com.group5.htms.dto.refereeassignment.response.RefereeAssignmentResponse;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.mapper.RefereeAssignmentMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefereeAssignmentServiceImplTest {

    @Mock
    private RacesRepository racesRepository;

    @Mock
    private RefereeProfilesRepository refereeProfilesRepository;

    @Mock
    private RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;

    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Mock
    private RefereeAssignmentMapper refereeAssignmentMapper;

    private RefereeAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RefereeAssignmentServiceImpl(
                racesRepository,
                refereeProfilesRepository,
                raceRefereeAssignmentsRepository,
                jockeyHorseAssignmentsRepository,
                refereeAssignmentMapper
        );
    }

    @Test
    void assignRefereeSucceedsWhenBettingIsOpen() {
        Races race = Races.builder()
                .id(10)
                .status(RaceStatus.OPEN_FOR_BETTING.getValue())
                .maxReferees(3)
                .build();
        RefereeProfiles referee = RefereeProfiles.builder()
                .id(7)
                .status(RoleStatus.ACTIVE.getValue())
                .users(Users.builder()
                        .id(7)
                        .status(RoleStatus.ACTIVE.getValue())
                        .roleType(RoleType.RACE_REFEREE.getValue())
                        .build())
                .build();
        RefereeAssignmentCreateRequest request = RefereeAssignmentCreateRequest.builder()
                .refereeId(7)
                .refereeRole("chief_referee")
                .build();
        RaceRefereeAssignments assignment = RaceRefereeAssignments.builder().build();
        RaceRefereeAssignments savedAssignment = RaceRefereeAssignments.builder()
                .id(99)
                .races(race)
                .referee(referee)
                .refereeRole("chief_referee")
                .build();
        RefereeAssignmentResponse expected = RefereeAssignmentResponse.builder()
                .id(99)
                .raceId(10)
                .refereeId(7)
                .refereeRole("chief_referee")
                .build();

        when(racesRepository.findById(10)).thenReturn(Optional.of(race));
        when(refereeProfilesRepository.findById(7)).thenReturn(Optional.of(referee));
        when(jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(
                10,
                JockeyAssignmentStatus.CONFIRMED.getValue()
        )).thenReturn(1L);
        when(raceRefereeAssignmentsRepository.existsByRaces_IdAndReferee_Id(10, 7))
                .thenReturn(false);
        when(raceRefereeAssignmentsRepository.countByRaces_Id(10)).thenReturn(0L);
        when(raceRefereeAssignmentsRepository.existsByRaces_IdAndRefereeRoleIgnoreCase(
                10,
                "chief_referee"
        )).thenReturn(false);
        when(refereeAssignmentMapper.toEntity(request)).thenReturn(assignment);
        when(raceRefereeAssignmentsRepository.save(assignment)).thenReturn(savedAssignment);
        when(refereeAssignmentMapper.toResponse(savedAssignment)).thenReturn(expected);

        RefereeAssignmentResponse response = service.assignRefereeToRace(10, request);

        assertThat(response).isSameAs(expected);
        assertThat(assignment.getRaces()).isSameAs(race);
        assertThat(assignment.getReferee()).isSameAs(referee);
        assertThat(assignment.getRefereeRole()).isEqualTo("chief_referee");
        verify(raceRefereeAssignmentsRepository).save(assignment);
    }
}
