package com.group5.htms.service;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationApproveRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationRejectRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationListResponse;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;

import java.util.List;

public interface RaceRegistrationService {
    List<RaceRegistrationListResponse> getAllRegistrations();

    List<RaceRegistrationListResponse> getMyRegistrations();

    RaceRegistrationResponse getRegistrationById(Integer id);

    RaceRegistrationResponse createRegistration(RaceRegistrationCreateRequest request);

    RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request);

    RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApprovalRequest request);

    RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApproveRequest request);

    RaceRegistrationResponse rejectRegistration(Integer id, RaceRegistrationRejectRequest request);

    void deleteRegistration(Integer id);
}
