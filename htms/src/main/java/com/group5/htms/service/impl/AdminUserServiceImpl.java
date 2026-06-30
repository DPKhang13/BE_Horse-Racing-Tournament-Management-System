package com.group5.htms.service.impl;

import com.group5.htms.dto.admin.request.AdminUserCreateRequest;
import com.group5.htms.dto.admin.request.AdminUserResetPasswordRequest;
import com.group5.htms.dto.admin.request.AdminUserStatusUpdateRequest;
import com.group5.htms.dto.admin.request.AdminUserUpdateRequest;
import com.group5.htms.dto.admin.response.AdminUserDetailResponse;
import com.group5.htms.dto.admin.response.AdminUserListResponse;
import com.group5.htms.dto.admin.response.AdminUserResetPasswordResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.JockeyStatus;
import com.group5.htms.enums.RoleStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.AdminUserMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AdminUserService;
import com.group5.htms.service.AuthService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private final UsersRepository usersRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final RefereeProfilesRepository refereeProfilesRepository;
    private final WalletsRepository walletsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserMapper adminUserMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public AdminUserDetailResponse createUser(AdminUserCreateRequest request) {
        String username = cleanRequired(request.getUsername(), "Username is required");
        String email = normalizeEmail(request.getEmail());
        RoleType roleType = parseRoleType(cleanRequired(request.getRoleType(), "Role type is required"));

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

        return adminUserMapper.toDetailResponse(user, ownerProfile, jockeyProfile, refereeProfile, findWallet(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListResponse> getUsers(String roleType, String status, String keyword, Pageable pageable) {
        validateRoleTypeIfPresent(roleType);
        validateUserStatusIfPresent(status);

        return usersRepository.findAll(buildUserSpecification(roleType, status, keyword), pageable)
                .map(user -> adminUserMapper.toListResponse(
                        user,
                        findOwnerProfile(user),
                        findJockeyProfile(user),
                        findRefereeProfile(user)
                ));
    }


    @Override
    @Transactional(readOnly = true)
    public List<AdminUserListResponse> getHorseOwners() {
        return getUsersByRole(RoleType.HORSE_OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserListResponse> getJockeys() {
        return getUsersByRole(RoleType.JOCKEY);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserListResponse> getReferees() {
        return getUsersByRole(RoleType.RACE_REFEREE);
    }
    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserById(Integer userId) {
        Users user = findUser(userId);
        return toDetailResponse(user);
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUser(Integer userId, AdminUserUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("User update request is required");
        }

        Users user = findUser(userId);
        ensureNoManagedUpdateFields(request);
        updateBaseUser(user, request);
        updateProfile(user, request);

        return toDetailResponse(usersRepository.save(user));
    }


    @Override
    @Transactional
    public AdminUserDetailResponse updateHorseOwnerProfile(Integer userId, AdminUserUpdateRequest request) {
        Users user = findUserByRole(userId, RoleType.HORSE_OWNER);
        updateOwnerProfile(user, request);
        return toDetailResponse(user);
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateJockeyProfile(Integer userId, AdminUserUpdateRequest request) {
        Users user = findUserByRole(userId, RoleType.JOCKEY);
        updateJockeyProfile(user, request);
        return toDetailResponse(user);
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateRefereeProfile(Integer userId, AdminUserUpdateRequest request) {
        Users user = findUserByRole(userId, RoleType.RACE_REFEREE);
        updateRefereeProfile(user, request);
        return toDetailResponse(user);
    }
    @Override
    @Transactional
    public AdminUserDetailResponse updateStatus(Integer userId, AdminUserStatusUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Status update request is required");
        }

        Users user = findUser(userId);
        String status = normalizeUserStatus(request.getStatus());

        if (UserStatus.INACTIVE.getValue().equals(status)) {
            ensureCanDeactivateUser(user);
        }

        user.setStatus(status);
        return toDetailResponse(usersRepository.save(user));
    }


    @Override
    @Transactional
    public AdminUserDetailResponse banUser(Integer userId) {
        Users user = findManagedRoleUser(userId);
        ensureCanDeactivateUser(user);
        user.setStatus(UserStatus.INACTIVE.getValue());
        return toDetailResponse(usersRepository.save(user));
    }
    @Override
    @Transactional
    public AdminUserResetPasswordResponse resetPassword(Integer userId, AdminUserResetPasswordRequest request) {
        if (request == null) {
            throw new BadRequestException("Reset password request is required");
        }

        Users user = findUser(userId);
        if (isCurrentUser(user)) {
            throw new BadRequestException("Admin cannot reset their own password here");
        }

        user.setPasswordHash(passwordEncoder.encode(cleanRequired(request.getNewPassword(), "New password is required")));
        usersRepository.save(user);

        return AdminUserResetPasswordResponse.builder()
                .message("Password reset successfully")
                .build();
    }


    private List<AdminUserListResponse> getUsersByRole(RoleType roleType) {
        return usersRepository.findByRoleTypeIgnoreCaseOrderByFullNameAsc(roleType.getValue())
                .stream()
                .map(user -> adminUserMapper.toListResponse(
                        user,
                        findOwnerProfile(user),
                        findJockeyProfile(user),
                        findRefereeProfile(user)
                ))
                .toList();
    }
    private Specification<Users> buildUserSpecification(String roleType, String status, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            String cleanedRoleType = cleanLower(roleType);
            if (cleanedRoleType != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("roleType")), cleanedRoleType));
            }

            String cleanedStatus = cleanLower(status);
            if (cleanedStatus != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), cleanedStatus));
            }

            String cleanedKeyword = cleanLower(keyword);
            if (cleanedKeyword != null) {
                String pattern = "%" + cleanedKeyword + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void updateBaseUser(Users user, AdminUserUpdateRequest request) {
        String username = clean(request.getUsername());
        if (username != null && !username.equals(user.getUsername())) {
            if (usersRepository.existsByUsernameAndIdNot(username, user.getId())) {
                throw new BadRequestException("Username already exists");
            }
            user.setUsername(username);
        }

        String email = clean(request.getEmail());
        if (email != null) {
            email = normalizeEmail(email);
            if (!email.equalsIgnoreCase(user.getEmail())) {
                if (usersRepository.existsByEmailAndIdNot(email, user.getId())) {
                    throw new BadRequestException("Email already exists");
                }
                user.setEmail(email);
            }
        }

        String fullName = clean(request.getFullName());
        if (fullName != null) {
            user.setFullName(fullName);
        }

        if (request.getPhone() != null) {
            user.setPhone(clean(request.getPhone()));
        }
    }

    private void updateProfile(Users user, AdminUserUpdateRequest request) {
        String roleType = cleanLower(user.getRoleType());

        if (RoleType.HORSE_OWNER.getValue().equals(roleType)) {
            updateOwnerProfile(user, request);
            return;
        }

        if (RoleType.JOCKEY.getValue().equals(roleType)) {
            updateJockeyProfile(user, request);
            return;
        }

        if (RoleType.RACE_REFEREE.getValue().equals(roleType)) {
            updateRefereeProfile(user, request);
        }
    }

    private void updateOwnerProfile(Users user, AdminUserUpdateRequest request) {
        HorseOwnerProfiles profile = horseOwnerProfilesRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException("Horse owner profile not found"));

        if (request.getStableName() != null) {
            profile.setStableName(clean(request.getStableName()));
        }
        if (request.getLicenseNumber() != null) {
            profile.setLicenseNumber(clean(request.getLicenseNumber()));
        }
        if (request.getAddress() != null) {
            profile.setAddress(clean(request.getAddress()));
        }
        if (request.getFavoriteJockeyId() != null) {
            profile.setFavoriteJockey(findAvailableJockeyProfile(request.getFavoriteJockeyId()));
        }
        if (hasText(request.getProfileStatus())) {
            profile.setStatus(normalizeRoleStatus(request.getProfileStatus()));
        }

        horseOwnerProfilesRepository.save(profile);
    }

    private void updateJockeyProfile(Users user, AdminUserUpdateRequest request) {
        JockeyProfiles profile = jockeyProfilesRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException("Jockey profile not found"));

        if (request.getLicenseNumber() != null) {
            profile.setLicenseNumber(clean(request.getLicenseNumber()));
        }
        if (request.getExperienceYears() != null) {
            profile.setExperienceYears(request.getExperienceYears());
        }
        if (hasText(request.getProfileStatus())) {
            profile.setStatus(normalizeJockeyStatus(request.getProfileStatus()));
        }

        jockeyProfilesRepository.save(profile);
    }

    private void updateRefereeProfile(Users user, AdminUserUpdateRequest request) {
        RefereeProfiles profile = refereeProfilesRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException("Referee profile not found"));

        if (request.getLicenseNumber() != null) {
            profile.setLicenseNumber(clean(request.getLicenseNumber()));
        }
        if (request.getAddress() != null) {
            profile.setAddress(clean(request.getAddress()));
        }
        if (hasText(request.getProfileStatus())) {
            profile.setStatus(normalizeRoleStatus(request.getProfileStatus()));
        }

        refereeProfilesRepository.save(profile);
    }

    private void ensureNoManagedUpdateFields(AdminUserUpdateRequest request) {
        if (hasText(request.getRoleType())) {
            throw new BadRequestException("Role type cannot be changed after user creation");
        }
        if (hasText(request.getStatus())) {
            throw new BadRequestException("Use status endpoint to update user status");
        }
    }

    private void ensureCanDeactivateUser(Users user) {
        if (isCurrentUser(user)) {
            throw new BadRequestException("Admin cannot deactivate their own account");
        }

        if (RoleType.ADMIN.getValue().equalsIgnoreCase(user.getRoleType())) {
            long activeAdmins = usersRepository.countByRoleTypeIgnoreCaseAndStatusIgnoreCase(
                    RoleType.ADMIN.getValue(),
                    UserStatus.ACTIVE.getValue()
            );
            if (activeAdmins <= 1) {
                throw new BadRequestException("Cannot deactivate the last active admin");
            }
        }
    }

    private boolean isCurrentUser(Users user) {
        Integer currentUserId = authService.getCurrentUserId();
        return currentUserId != null && currentUserId.equals(user.getId());
    }

    private AdminUserDetailResponse toDetailResponse(Users user) {
        return adminUserMapper.toDetailResponse(
                user,
                findOwnerProfile(user),
                findJockeyProfile(user),
                findRefereeProfile(user),
                findWallet(user.getId())
        );
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
            profile.setFavoriteJockey(findAvailableJockeyProfile(request.getFavoriteJockeyId()));
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

    private JockeyProfiles findAvailableJockeyProfile(Integer jockeyId) {
        JockeyProfiles jockey = jockeyProfilesRepository.findById(jockeyId)
                .orElseThrow(() -> new BadRequestException("Favorite jockey not found"));
        if (jockey.getUsers() == null
                || !RoleType.JOCKEY.getValue().equalsIgnoreCase(jockey.getUsers().getRoleType())) {
            throw new BadRequestException("Favorite jockey must be a jockey user");
        }
        String status = jockey.getStatus();
        if (!JockeyStatus.AVAILABLE.getValue().equalsIgnoreCase(status)
                && !JockeyStatus.ACTIVE.getValue().equalsIgnoreCase(status)) {
            throw new BadRequestException("Favorite jockey profile is not active");
        }
        return jockey;
    }


    private Users findUserByRole(Integer userId, RoleType roleType) {
        Users user = findUser(userId);
        if (user.getRoleType() == null || !roleType.getValue().equalsIgnoreCase(user.getRoleType())) {
            throw new BadRequestException("User does not have required role");
        }
        return user;
    }

    private Users findManagedRoleUser(Integer userId) {
        Users user = findUser(userId);
        if (user.getRoleType() == null
                || (!RoleType.HORSE_OWNER.getValue().equalsIgnoreCase(user.getRoleType())
                && !RoleType.JOCKEY.getValue().equalsIgnoreCase(user.getRoleType())
                && !RoleType.RACE_REFEREE.getValue().equalsIgnoreCase(user.getRoleType()))) {
            throw new BadRequestException("Only horse owner, jockey, or referee users can be banned here");
        }
        return user;
    }
    private Users findUser(Integer userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }

        return usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private HorseOwnerProfiles findOwnerProfile(Users user) {
        if (user == null || !RoleType.HORSE_OWNER.getValue().equalsIgnoreCase(user.getRoleType())) {
            return null;
        }
        return horseOwnerProfilesRepository.findById(user.getId()).orElse(null);
    }

    private JockeyProfiles findJockeyProfile(Users user) {
        if (user == null || !RoleType.JOCKEY.getValue().equalsIgnoreCase(user.getRoleType())) {
            return null;
        }
        return jockeyProfilesRepository.findById(user.getId()).orElse(null);
    }

    private RefereeProfiles findRefereeProfile(Users user) {
        if (user == null || !RoleType.RACE_REFEREE.getValue().equalsIgnoreCase(user.getRoleType())) {
            return null;
        }
        return refereeProfilesRepository.findById(user.getId()).orElse(null);
    }

    private Wallets findWallet(Integer userId) {
        return walletsRepository.findByUsersId(userId).orElse(null);
    }

    private RoleType parseRoleType(String roleType) {
        try {
            return RoleType.fromValue(roleType);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role type");
        }
    }

    private void validateRoleTypeIfPresent(String roleType) {
        if (clean(roleType) != null) {
            parseRoleType(roleType);
        }
    }

    private void validateUserStatusIfPresent(String status) {
        if (clean(status) != null) {
            normalizeUserStatus(status);
        }
    }

    private String normalizeUserStatus(String status) {
        String normalized = cleanLower(status);
        if (!UserStatus.ACTIVE.getValue().equals(normalized)
                && !UserStatus.INACTIVE.getValue().equals(normalized)) {
            throw new BadRequestException("Status must be active or inactive");
        }
        return normalized;
    }

    private String normalizeRoleStatus(String status) {
        try {
            return RoleStatus.fromValue(cleanRequired(status, "Profile status is required")).getValue();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid profile status");
        }
    }

    private String normalizeJockeyStatus(String status) {
        String normalized = cleanLower(status);
        for (JockeyStatus jockeyStatus : JockeyStatus.values()) {
            if (jockeyStatus.getValue().equals(normalized)) {
                return normalized;
            }
        }
        throw new BadRequestException("Invalid jockey profile status");
    }

    private String normalizeEmail(String email) {
        return cleanRequired(email, "Email is required").toLowerCase();
    }

    private String cleanRequired(String value, String message) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new BadRequestException(message);
        }

        return cleaned;
    }

    private String cleanLower(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
