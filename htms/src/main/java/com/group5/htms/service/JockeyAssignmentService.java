package com.group5.htms.service;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentListResponse;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;

import java.util.List;

public interface JockeyAssignmentService {
    List<JockeyAssignmentListResponse> getAllAssignments();

    List<JockeyAssignmentListResponse> getMyInvitations(String status);

    List<JockeyAssignmentListResponse> getSentInvitations(String status);

    JockeyAssignmentResponse getAssignmentById(Integer id);

    JockeyAssignmentResponse createInvitation(JockeyInvitationCreateRequest request);

    JockeyAssignmentResponse updateInvitation(Integer id, JockeyInvitationUpdateRequest request);

    JockeyAssignmentResponse respondInvitation(Integer id, JockeyInvitationResponseRequest request);

    JockeyAssignmentResponse cancelInvitation(Integer id);

    JockeyAssignmentResponse confirmAssignment(Integer id);

}
