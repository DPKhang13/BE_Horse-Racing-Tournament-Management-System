package com.group5.htms.service;

import com.group5.htms.dto.refereeassignment.request.RefereeAssignmentCreateRequest;
import com.group5.htms.dto.refereeassignment.response.RefereeAssignmentResponse;

import java.util.List;

public interface RefereeAssignmentService {

    RefereeAssignmentResponse assignRefereeToRace(
            Integer raceId,
            RefereeAssignmentCreateRequest request
    );

    List<RefereeAssignmentResponse> getRefereesByRace(Integer raceId);
}
