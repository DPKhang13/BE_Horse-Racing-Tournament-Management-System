package com.group5.htms.service;

import com.group5.htms.dto.dashboard.response.AdminDashboardSummaryResponse;

import java.time.LocalDate;

public interface AdminDashboardService {

    AdminDashboardSummaryResponse getSummary(
            LocalDate from,
            LocalDate to,
            Integer tournamentId,
            String period
    );
}
