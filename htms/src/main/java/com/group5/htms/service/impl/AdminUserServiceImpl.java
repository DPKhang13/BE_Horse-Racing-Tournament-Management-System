package com.group5.htms.service.impl;

import com.group5.htms.dto.admin.request.AdminUserCreateRequest;
import com.group5.htms.dto.auth.response.UserMeResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.enums.RoleStatus;
import com.group5.htms.enums.JockeyStatus;
import com.group5.htms.enums.HorseStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.AuthMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private final UsersRepository usersRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final RefereeProfilesRepository refereeProfilesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public UserMeResponse createUser(AdminUserCreateRequest request) {
        String username = cleanRequired(request.getUsername(), "Username is required");
        String email = cleanRequired(request.getEmail(), "Email is required").toLowerCase();
        RoleType roleType = RoleType.fromValue(cleanRequired(request.getRoleType(), "Role type is required"));

        if (usersRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }
        if (usersRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        Users user = Users.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(cleanRequired(request.getFullName(), "Full name is required"))
                .phone(clean(request.getPhone()))
                .roleType(roleType.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .createdAt(Instant.now())
                .build();
        usersRepository.save(user);

        HorseOwnerProfiles ownerProfile = null;
        JockeyProfiles jockeyProfile = null;
        RefereeProfiles refereeProfile = null;

        switch (roleType) {
            case HORSE_OWNER -> ownerProfile = createOwnerProfile(user, request);
            case JOCKEY -> jockeyProfile = createJockeyProfile(user, request);
            case RACE_REFEREE -> refereeProfile = createRefereeProfile(user, request);
            case ADMIN, SPECTATOR -> {
                // No extra profile table for this role.
            }
        }

        return authMapper.toUserMeResponse(user, ownerProfile, jockeyProfile, refereeProfile);
    }

    private HorseOwnerProfiles createOwnerProfile(Users user, AdminUserCreateRequest request) {
        HorseOwnerProfiles profile = HorseOwnerProfiles.builder()
                .users(user)
                .stableName(clean(request.getStableName()))
                .licenseNumber(clean(request.getLicenseNumber()))
                .address(clean(request.getAddress()))
                .status(RoleStatus.ACTIVE.getValue())
                .createdAt(Instant.now())
                .build();

        if (request.getFavoriteJockeyId() != null) {
            JockeyProfiles favoriteJockey = jockeyProfilesRepository.findById(request.getFavoriteJockeyId())
                    .orElseThrow(() -> new BadRequestException("Favorite jockey not found"));
            profile.setFavoriteJockey(favoriteJockey);
        }

        return horseOwnerProfilesRepository.save(profile);
    }

    private JockeyProfiles createJockeyProfile(Users user, AdminUserCreateRequest request) {
        JockeyProfiles profile = JockeyProfiles.builder()
                .users(user)
                .licenseNumber(clean(request.getLicenseNumber()))
                .rankingPoints(0)
                .totalWins(0)
                .experienceYears(request.getExperienceYears() == null ? 0 : Math.max(request.getExperienceYears(), 0))
                .status(JockeyStatus.AVAILABLE.getValue())
                .build();

        return jockeyProfilesRepository.save(profile);
    }

    private RefereeProfiles createRefereeProfile(Users user, AdminUserCreateRequest request) {
        RefereeProfiles profile = RefereeProfiles.builder()
                .users(user)
                .licenseNumber(clean(request.getLicenseNumber()))
                .address(clean(request.getAddress()))
                .status(RoleStatus.ACTIVE.getValue())
                .createdAt(Instant.now())
                .build();

        return refereeProfilesRepository.save(profile);
    }

    private String cleanRequired(String value, String message) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new BadRequestException(message);
        }

        return cleaned;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }
}

