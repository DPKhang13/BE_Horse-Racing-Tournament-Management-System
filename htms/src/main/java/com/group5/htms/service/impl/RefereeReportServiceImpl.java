package com.group5.htms.service.impl;

import com.group5.htms.dto.refereereport.request.RefereeReportCreateRequest;
import com.group5.htms.dto.refereereport.response.RefereeAssignedRaceResponse;
import com.group5.htms.dto.refereereport.response.RefereeReportResponse;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.RefereeReports;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import com.group5.htms.service.RefereeReportService;
import com.group5.htms.validation.RefereeReportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefereeReportServiceImpl implements RefereeReportService {
    private static final String REPORT_TYPE_FINAL = "final";
    private static final String REPORT_TYPE_INSPECTION = "inspection";
    private static final String REPORT_TYPE_VIOLATION = "violation";
    private static final Set<String> MAIN_REFEREE_REPORT_TYPES = Set.of(
            REPORT_TYPE_INSPECTION,
            REPORT_TYPE_VIOLATION
    );
    private static final String VERDICT_CLEAN = "clean";
    private static final String VERDICT_VIOLATION = "violation";

    private final RacesRepository racesRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RefereeReportsRepository refereeReportsRepository;
    private final RefereeReportValidator refereeReportValidator;
    private final RefereeRaceAuthorizationService refereeRaceAuthorizationService;

    @Override
    @Transactional
    public RefereeReportResponse submitReport(Integer raceId, RefereeReportCreateRequest request) {
        refereeReportValidator.ensureReportRequestExists(request);

        RaceRefereeAssignments assignment = refereeRaceAuthorizationService.requireAssignedReferee(raceId);
        RefereeProfiles referee = assignment.getReferee();
        Races race = getRace(raceId);
        refereeReportValidator.ensureRaceInProgressForReport(race);

        String reportType = cleanLower(request.getReportType());
        validateReportTypeForAssignment(reportType, assignment);
        String verdict = cleanOrDefault(request.getVerdict(), VERDICT_CLEAN);
        refereeReportValidator.validateVerdict(verdict, request.getViolationNotes());

        if (refereeReportsRepository.existsByRaces_IdAndReferee_IdAndReportTypeIgnoreCase(
                race.getId(),
                referee.getId(),
                reportType
        )) {
            throw new BadRequestException("This report type already exists for this referee and race");
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
        RefereeProfiles referee = refereeRaceAuthorizationService.getCurrentReferee();

        return raceRefereeAssignmentsRepository.findByReferee_IdOrderByAssignedAtDesc(referee.getId())
                .stream()
                .map(this::toAssignedRaceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefereeReportResponse> getRaceReports(Integer raceId) {
        Races race = getRace(raceId);
        refereeRaceAuthorizationService.requireAssignedReferee(race.getId());
        Map<Integer, String> refereeRolesByRefereeId = refereeRolesByRefereeId(race.getId());

        return refereeReportsRepository.findByRaces_IdOrderBySubmittedAtDesc(race.getId())
                .stream()
                .map(report -> toResponse(report, refereeRolesByRefereeId))
                .toList();
    }

    private Races getRace(Integer raceId) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }

        return racesRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private RefereeAssignedRaceResponse toAssignedRaceResponse(RaceRefereeAssignments assignment) {
        Races race = assignment.getRaces();
        return RefereeAssignedRaceResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .tournamentName(tournamentName(race))
                .status(race.getStatus())
                .scheduledAt(race.getScheduledAt())
                .predictionClosesAt(race.getPredictionClosesAt())
                .refereeRole(assignment.getRefereeRole())
                .assignmentId(assignment.getId())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private RefereeReportResponse toResponse(RefereeReports report) {
        return toResponse(report, refereeRole(report.getRaces().getId(), report.getReferee().getId()));
    }

    private RefereeReportResponse toResponse(RefereeReports report, Map<Integer, String> refereeRolesByRefereeId) {
        return toResponse(report, refereeRolesByRefereeId.get(report.getReferee().getId()));
    }

    private RefereeReportResponse toResponse(RefereeReports report, String refereeRole) {
        return RefereeReportResponse.builder()
                .reportId(report.getId())
                .raceId(report.getRaces().getId())
                .raceName(report.getRaces().getName())
                .refereeId(report.getReferee().getId())
                .refereeFullName(report.getReferee().getUsers().getFullName())
                .refereeRole(refereeRole)
                .reportType(report.getReportType())
                .inspectionNotes(report.getInspectionNotes())
                .violationNotes(report.getViolationNotes())
                .resultNotes(report.getResultNotes())
                .verdict(report.getVerdict())
                .submittedAt(report.getSubmittedAt())
                .build();
    }

    private Map<Integer, String> refereeRolesByRefereeId(Integer raceId) {
        return raceRefereeAssignmentsRepository.findByRaces_IdOrderByIdAsc(raceId)
                .stream()
                .filter(assignment -> assignment.getReferee() != null)
                .collect(Collectors.toMap(
                        assignment -> assignment.getReferee().getId(),
                        RaceRefereeAssignments::getRefereeRole,
                        (first, ignored) -> first
                ));
    }

    private String refereeRole(Integer raceId, Integer refereeId) {
        return raceRefereeAssignmentsRepository.findByRaces_IdAndReferee_Id(raceId, refereeId)
                .map(RaceRefereeAssignments::getRefereeRole)
                .orElse(null);
    }

    private String tournamentName(Races race) {
        if (race == null || race.getSchedule() == null || race.getSchedule().getTournaments() == null) {
            return null;
        }
        return race.getSchedule().getTournaments().getName();
    }

    private String cleanOrDefault(String value, String defaultValue) {
        String cleaned = clean(value);
        return cleaned == null ? defaultValue : cleaned.toLowerCase();
    }

    private void validateReportTypeForAssignment(String reportType, RaceRefereeAssignments assignment) {
        if (reportType == null) {
            throw new BadRequestException("Report type is required");
        }

        String refereeRole = cleanLower(assignment.getRefereeRole());
        if (REPORT_TYPE_FINAL.equals(reportType)) {
            if (!RefereeRaceAuthorizationService.ROLE_CHIEF_REFEREE.equals(refereeRole)) {
                throw new BadRequestException("Only the chief referee assigned to this race can submit final reports");
            }
            return;
        }

        if (!MAIN_REFEREE_REPORT_TYPES.contains(reportType)) {
            throw new BadRequestException("Report type must be final, inspection or violation");
        }
        if (!RefereeRaceAuthorizationService.ROLE_MAIN_REFEREE.equals(refereeRole)) {
            throw new BadRequestException(
                    "Only the main referee assigned to this race can submit inspection or violation reports"
            );
        }
    }

    private String cleanLower(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

}
