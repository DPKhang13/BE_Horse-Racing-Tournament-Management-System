package com.group5.htms.service;

import com.group5.htms.dto.admin.request.AdminUserCreateRequest;
import com.group5.htms.dto.auth.response.UserMeResponse;

public interface AdminUserService {
    UserMeResponse createUser(AdminUserCreateRequest request);
}
