package com.group5.htms.service;

import com.group5.htms.dto.refereereport.request.RefereeReportCreateRequest;
import com.group5.htms.dto.refereereport.response.RefereeAssignedRaceResponse;
import com.group5.htms.dto.refereereport.response.RefereeReportResponse;

import java.util.List;

public interface RefereeReportService {
    RefereeReportResponse submitReport(Integer raceId, RefereeReportCreateRequest request);

    List<RefereeAssignedRaceResponse> getMyAssignedRaces();

    List<RefereeReportResponse> getRaceReports(Integer raceId);
}
