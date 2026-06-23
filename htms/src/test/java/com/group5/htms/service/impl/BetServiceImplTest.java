package com.group5.htms.service.impl;

import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.repository.BetOptionsRepository;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.BetOptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BetServiceImplTest {
    @Mock
    private BetsRepository betsRepository;

    @Mock
    private BetOptionsRepository betOptionsRepository;

    @Mock
    private WalletsRepository walletsRepository;

    @Mock
    private WalletTransactionsRepository walletTransactionsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private AuthService authService;

    @Mock
    private BetOptionService betOptionService;

    @Mock
    private BetMapper betMapper;

    private BetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BetServiceImpl(
                betsRepository,
                betOptionsRepository,
                walletsRepository,
                walletTransactionsRepository,
                usersRepository,
                authService,
                betOptionService,
                betMapper
        );
    }

    @Test
    void createBetFailsIfRaceStatusIsRegistrationOpen() {
        BetCreateRequest request = new BetCreateRequest();
        request.setOptionId(99);
        request.setBetPoints(new BigDecimal("10.00"));

        Users user = new Users();
        user.setId(1);

        Races race = Races.builder()
                .id(10)
                .status(RaceStatus.REGISTRATION_OPEN.getValue())
                .build();
        BetOptions option = BetOptions.builder()
                .id(99)
                .races(race)
                .currentRate(new BigDecimal("2.00"))
                .totalBetPoints(BigDecimal.ZERO)
                .totalBetCount(0)
                .updatedAt(Instant.now())
                .build();

        when(authService.getCurrentUserId()).thenReturn(1);
        when(usersRepository.findById(1)).thenReturn(Optional.of(user));
        when(betOptionsRepository.findFirstById(99)).thenReturn(Optional.of(option));

        assertThatThrownBy(() -> service.createBet(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Race is not open for betting");
    }
}
