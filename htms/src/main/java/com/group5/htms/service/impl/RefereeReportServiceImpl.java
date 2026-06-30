package com.group5.htms.service.impl;

import com.group5.htms.dto.refereereport.request.RefereeReportCreateRequest;
import com.group5.htms.dto.refereereport.response.RefereeAssignedRaceResponse;
import com.group5.htms.dto.refereereport.response.RefereeReportResponse;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.RefereeReports;
import com.group5.htms.entity.Users;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.RefereeReportService;
import com.group5.htms.validation.RefereeReportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefereeReportServiceImpl implements RefereeReportService {
    private static final String REPORT_TYPE_FINAL = "final";
    private static final String VERDICT_CLEAN = "clean";
    private static final String VERDICT_VIOLATION = "violation";

    private final RacesRepository racesRepository;
    private final RefereeProfilesRepository refereeProfilesRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RefereeReportsRepository refereeReportsRepository;
    private final AuthService authService;
    private final RefereeReportValidator refereeReportValidator;

    @Override
    @Transactional
    public RefereeReportResponse submitReport(Integer raceId, RefereeReportCreateRequest request) {
        refereeReportValidator.ensureReportRequestExists(request);

        RefereeProfiles referee = getCurrentReferee();
        Races race = getRace(raceId);
        refereeReportValidator.ensureRaceInProgressForReport(race);
        ensureAssignedReferee(race.getId(), referee.getId());

        String reportType = cleanOrDefault(request.getReportType(), REPORT_TYPE_FINAL);
        String verdict = cleanOrDefault(request.getVerdict(), VERDICT_CLEAN);
        refereeReportValidator.validateVerdict(verdict, request.getViolationNotes());

        if (REPORT_TYPE_FINAL.equals(reportType)
                && refereeReportsRepository.existsByRaces_IdAndReferee_IdAndReportTypeIgnoreCase(
                race.getId(),
                referee.getId(),
                REPORT_TYPE_FINAL
        )) {
            throw new BadRequestException("Final report already exists for this referee and race");
        }

        RefereeReports report = RefereeReports.builder()
                .races(race)
                .referee(referee)
                .reportType(reportType)
                .inspectionNotes(clean(request.getInspectionNotes()))
                .violationNotes(clean(request.getViolationNotes()))
                .resultNotes(clean(request.getResultNotes()))
                .verdict(verdict)
                .submittedAt(Instant.now())
                .build();

        return toResponse(refereeReportsRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefereeAssignedRaceResponse> getMyAssignedRaces() {
        RefereeProfiles referee = getCurrentReferee();

        return raceRefereeAssignmentsRepository.findByReferee_IdOrderByAssignedAtDesc(referee.getId())
                .stream()
                .map(this::toAssignedRaceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefereeReportResponse> getRaceReports(Integer raceId) {
        RefereeProfiles referee = getCurrentReferee();
        Races race = getRace(raceId);
        ensureAssignedReferee(race.getId(), referee.getId());

        return refereeReportsRepository.findByRaces_IdOrderBySubmittedAtDesc(race.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RefereeProfiles getCurrentReferee() {
        Users user = authService.getCurrentUser();
        refereeReportValidator.ensureCurrentUserIsRaceReferee(user);

        return refereeProfilesRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Referee profile not found"));
    }

    private Races getRace(Integer raceId) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }

        return racesRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private RaceRefereeAssignments ensureAssignedReferee(Integer raceId, Integer refereeId) {
        return raceRefereeAssignmentsRepository.findByRaces_IdAndReferee_Id(raceId, refereeId)
                .orElseThrow(() -> new BadRequestException("Only assigned referees can submit reports for this race"));
    }

    private RefereeAssignedRaceResponse toAssignedRaceResponse(RaceRefereeAssignments assignment) {
        Races race = assignment.getRaces();
        return RefereeAssignedRaceResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .status(race.getStatus())
                .scheduledAt(race.getScheduledAt())
                .predictionClosesAt(race.getPredictionClosesAt())
                .refereeRole(assignment.getRefereeRole())
                .assignmentId(assignment.getId())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private RefereeReportResponse toResponse(RefereeReports report) {
        return RefereeReportResponse.builder()
                .reportId(report.getId())
                .raceId(report.getRaces().getId())
                .raceName(report.getRaces().getName())
                .refereeId(report.getReferee().getId())
                .refereeFullName(report.getReferee().getUsers().getFullName())
                .reportType(report.getReportType())
                .inspectionNotes(report.getInspectionNotes())
                .violationNotes(report.getViolationNotes())
                .resultNotes(report.getResultNotes())
                .verdict(report.getVerdict())
                .submittedAt(report.getSubmittedAt())
                .build();
    }

    private String cleanOrDefault(String value, String defaultValue) {
        String cleaned = clean(value);
        return cleaned == null ? defaultValue : cleaned.toLowerCase();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

}
