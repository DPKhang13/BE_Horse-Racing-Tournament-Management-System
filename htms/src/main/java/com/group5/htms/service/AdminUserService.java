package com.group5.htms.service;

import com.group5.htms.dto.admin.request.AdminUserCreateRequest;
import com.group5.htms.dto.admin.request.AdminUserResetPasswordRequest;
import com.group5.htms.dto.admin.request.AdminUserStatusUpdateRequest;
import com.group5.htms.dto.admin.request.AdminUserUpdateRequest;
import com.group5.htms.dto.admin.response.AdminUserDetailResponse;
import com.group5.htms.dto.admin.response.AdminUserListResponse;
import com.group5.htms.dto.admin.response.AdminUserResetPasswordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminUserService {
    AdminUserDetailResponse createUser(AdminUserCreateRequest request);

    Page<AdminUserListResponse> getUsers(String roleType, String status, String keyword, Pageable pageable);

    List<AdminUserListResponse> getHorseOwners();

    List<AdminUserListResponse> getJockeys();

    List<AdminUserListResponse> getReferees();

    AdminUserDetailResponse getUserById(Integer userId);

    AdminUserDetailResponse updateUser(Integer userId, AdminUserUpdateRequest request);

    AdminUserDetailResponse updateHorseOwnerProfile(Integer userId, AdminUserUpdateRequest request);

    AdminUserDetailResponse updateJockeyProfile(Integer userId, AdminUserUpdateRequest request);

    AdminUserDetailResponse updateRefereeProfile(Integer userId, AdminUserUpdateRequest request);

    AdminUserDetailResponse updateStatus(Integer userId, AdminUserStatusUpdateRequest request);

    AdminUserDetailResponse banUser(Integer userId);

    AdminUserResetPasswordResponse resetPassword(Integer userId, AdminUserResetPasswordRequest request);
}
