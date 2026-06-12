package com.group5.htms.service;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;

import java.util.List;

public interface JockeyAssignmentService {
    List<JockeyAssignmentResponse> getAllAssignments();

    List<JockeyAssignmentResponse> getMyInvitations(String status);

    List<JockeyAssignmentResponse> getSentInvitations(String status);

    JockeyAssignmentResponse getAssignmentById(Integer id);

    JockeyAssignmentResponse createInvitation(JockeyInvitationCreateRequest request);

    JockeyAssignmentResponse updateInvitation(Integer id, JockeyInvitationUpdateRequest request);

    JockeyAssignmentResponse respondInvitation(Integer id, JockeyInvitationResponseRequest request);

    void deleteAssignment(Integer id);
}
