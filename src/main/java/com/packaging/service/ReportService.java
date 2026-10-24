package com.packaging.service;

import com.packaging.model.ViewModels.AttendanceRecord;
import com.packaging.model.ViewModels.AttendanceTotals;
import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.model.ViewModels.ReportData;
import com.packaging.repository.AttendanceRepository;
import com.packaging.repository.ProductionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReportService {
    private final AttendanceRepository attendanceRepository;
    private final ProductionRepository productionRepository;
    private final AttendanceService attendanceService;

    public ReportService(AttendanceRepository attendanceRepository,
                         ProductionRepository productionRepository,
                         AttendanceService attendanceService) {
        this.attendanceRepository = attendanceRepository;
        this.productionRepository = productionRepository;
        this.attendanceService = attendanceService;
    }

    public ReportData generate(String type, LocalDate fromDate, LocalDate toDate,
                               Long teamId, Long partId, Long markId, Long shiftId) {
        return generate(type, fromDate, toDate, teamId, partId, markId, shiftId, null);
    }

    public ReportData generate(String type, LocalDate fromDate, LocalDate toDate,
                               Long teamId, Long partId, Long markId, Long shiftId, String operatorName) {
        String reportType = type == null ? "ATTENDANCE" : type.toUpperCase();
        if (!List.of("ATTENDANCE", "PRODUCTION", "COMBINED").contains(reportType)) {
            throw new IllegalArgumentException("Choose a valid report type.");
        }
        attendanceService.validateRange(fromDate, toDate);
        List<AttendanceRecord> attendance = "PRODUCTION".equals(reportType) ? List.of()
                : attendanceRepository.records(null, fromDate, toDate, teamId, null, null);
        List<ProductionRecord> production = "ATTENDANCE".equals(reportType) ? List.of()
                : productionRepository.records(null, fromDate, toDate, partId, markId, shiftId, operatorName);
        Map<LocalDate, AttendanceTotals> daily = new TreeMap<>();
        attendance.stream().collect(java.util.stream.Collectors.groupingBy(AttendanceRecord::date))
                .forEach((date, rows) -> daily.put(date, attendanceService.totals(rows)));
        long productionTotal = production.stream().mapToLong(ProductionRecord::quantity).sum();
        return new ReportData(reportType, fromDate, toDate, LocalDateTime.now(), attendance,
                daily, attendanceService.totals(attendance), production, productionTotal);
    }
}
