package com.group5.htms.service.impl;

import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RoleType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.HorseMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorseServiceImplTest {
    @Mock
    private HorsesRepository horsesRepository;

    @Mock
    private HorseOwnerProfilesRepository horseOwnerProfilesRepository;

    @Mock
    private AuthService authService;

    private HorseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HorseServiceImpl(
                horsesRepository,
                horseOwnerProfilesRepository,
                authService,
                new HorseMapper()
        );
    }

    @Test
    void createHorseShouldCreateMissingOwnerProfileForHorseOwner() {
        Users ownerUser = Users.builder()
                .id(10)
                .username("owner")
                .email("owner@example.com")
                .fullName("Horse Owner")
                .roleType(RoleType.HORSE_OWNER.getValue())
                .status("active")
                .build();

        when(authService.getCurrentUser()).thenReturn(ownerUser);
        when(horseOwnerProfilesRepository.findById(10)).thenReturn(Optional.empty());
        when(horseOwnerProfilesRepository.save(any(HorseOwnerProfiles.class))).thenAnswer(invocation -> {
            HorseOwnerProfiles profile = invocation.getArgument(0);
            profile.setId(ownerUser.getId());
            return profile;
        });
        when(horsesRepository.save(any(Horses.class))).thenAnswer(invocation -> {
            Horses horse = invocation.getArgument(0);
            horse.setId(99);
            return horse;
        });

        HorseCreateRequest request = new HorseCreateRequest();
        request.setName("Workflow Thunder");
        request.setBreed("Arabian");
        request.setAge(4);
        request.setWeightKg(BigDecimal.valueOf(450.5));
        request.setRankGroup("A");
        request.setAvatarUrl("https://example.com/horses/workflow-thunder.png");

        HorseResponse response = service.createHorse(request);

        ArgumentCaptor<HorseOwnerProfiles> profileCaptor = ArgumentCaptor.forClass(HorseOwnerProfiles.class);
        verify(horseOwnerProfilesRepository).save(profileCaptor.capture());
        HorseOwnerProfiles createdProfile = profileCaptor.getValue();
        assertThat(createdProfile.getUsers()).isSameAs(ownerUser);
        assertThat(createdProfile.getStatus()).isEqualTo("active");
        assertThat(createdProfile.getCreatedAt()).isNotNull();

        ArgumentCaptor<Horses> horseCaptor = ArgumentCaptor.forClass(Horses.class);
        verify(horsesRepository).save(horseCaptor.capture());
        assertThat(horseCaptor.getValue().getOwner().getId()).isEqualTo(10);

        assertThat(response.getHorseId()).isEqualTo(99);
        assertThat(response.getOwnerId()).isEqualTo(10);
        assertThat(response.getName()).isEqualTo("Workflow Thunder");
    }

    @Test
    void createHorseShouldRejectNonHorseOwner() {
        Users user = Users.builder()
                .id(20)
                .roleType(RoleType.SPECTATOR.getValue())
                .build();

        when(authService.getCurrentUser()).thenReturn(user);

        HorseCreateRequest request = new HorseCreateRequest();
        request.setName("Workflow Thunder");

        assertThatThrownBy(() -> service.createHorse(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Current user must be horse owner");

        verify(horseOwnerProfilesRepository, never()).save(any(HorseOwnerProfiles.class));
        verify(horsesRepository, never()).save(any(Horses.class));
    }
}
