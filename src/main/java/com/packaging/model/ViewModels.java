package com.packaging.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ViewModels {
    private ViewModels() {
    }

    public record Team(long id, String name) {
    }

    public record Employee(long id, String employeeNumber, String name, long teamId,
                           String teamName, boolean active) {
    }

    public record LookupOption(long id, String code, String name, boolean active) {
    }

    public record AttendanceEntryRow(long employeeId, String employeeNumber, String employeeName,
                                     long teamId, String teamName, boolean active,
                                     String status, Long attendanceId) {
    }

    public record AttendanceRecord(long id, LocalDate date, long employeeId,
                                   String employeeNumber, String employeeName,
                                   long teamId, String teamName, String status,
                                   boolean employeeActive) {
    }

    public record AttendanceTotals(int manpower, int present, int absent, double percentage) {
    }

    public record TeamAttendance(String teamName, int manpower, int present, int absent,
                                 double percentage) {
    }

    public record ProductionRecord(long id, LocalDate date, long partId, String partNumber,
                                   long markId, String markName, long shiftId, String shiftName,
                                   int quantity, String remarks,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record ProductionGroup(String name, long quantity) {
    }

    public record ProductionTotals(long total, List<ProductionGroup> byPart,
                                   List<ProductionGroup> byMark, List<ProductionGroup> byShift) {
    }

    public record SummaryData(AttendanceTotals attendance, List<TeamAttendance> teams,
                              ProductionTotals production) {
    }

    public record ReportData(String type, LocalDate fromDate, LocalDate toDate,
                             LocalDateTime generatedAt,
                             List<AttendanceRecord> attendance,
                             Map<LocalDate, AttendanceTotals> dailyAttendance,
                             AttendanceTotals attendanceTotals,
                             List<ProductionRecord> production,
                             long productionTotal) {
    }
}
