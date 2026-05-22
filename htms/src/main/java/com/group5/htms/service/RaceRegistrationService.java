package com.group5.htms.service;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;

import java.util.List;

public interface RaceRegistrationService {
    List<RaceRegistrationResponse> getAllRegistrations();

    RaceRegistrationResponse getRegistrationById(Integer id);

    RaceRegistrationResponse createRegistration(RaceRegistrationCreateRequest request);

    RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request);

    RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApprovalRequest request);

    void deleteRegistration(Integer id);
}
