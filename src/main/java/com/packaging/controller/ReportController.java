package com.packaging.controller;

import com.packaging.model.ViewModels.AttendanceRecord;
import com.packaging.model.ViewModels.AttendanceTotals;
import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.model.ViewModels.ReportData;
import com.packaging.service.ReferenceService;
import com.packaging.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ReportController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final ReportService reportService;
    private final ReferenceService referenceService;

    public ReportController(ReportService reportService, ReferenceService referenceService) {
        this.reportService = reportService;
        this.referenceService = referenceService;
    }

    @GetMapping("/report")
    public String report(@RequestParam(defaultValue = "ATTENDANCE") String type,
                         @RequestParam(required = false) LocalDate fromDate,
                         @RequestParam(required = false) LocalDate toDate,
                         @RequestParam(required = false) Long teamId,
                         @RequestParam(required = false) Long partId,
                         @RequestParam(required = false) Long markId,
                         @RequestParam(required = false) Long shiftId,
                         @RequestParam(defaultValue = "false") boolean generate,
                         Model model) {
        ReportData data = null;
        if (generate) {
            try {
                data = reportService.generate(type, fromDate, toDate, teamId, partId, markId, shiftId);
            } catch (IllegalArgumentException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        addFilters(model, type, fromDate, toDate, teamId, partId, markId, shiftId);
        model.addAttribute("report", data);
        model.addAttribute("generated", generate && data != null);
        return "report";
    }

    @GetMapping("/report/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(defaultValue = "ATTENDANCE") String type,
                                              @RequestParam(required = false) LocalDate fromDate,
                                              @RequestParam(required = false) LocalDate toDate,
                                              @RequestParam(required = false) Long teamId,
                                              @RequestParam(required = false) Long partId,
                                              @RequestParam(required = false) Long markId,
                                              @RequestParam(required = false) Long shiftId) {
        ReportData data = reportService.generate(type, fromDate, toDate, teamId, partId, markId, shiftId);
        String csv = csv(data);
        byte[] bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=packaging-report-" +
                        LocalDate.now() + ".csv")
                .body(bytes);
    }

    @GetMapping("/report/print")
    public String print(@RequestParam(defaultValue = "ATTENDANCE") String type,
                        @RequestParam(required = false) LocalDate fromDate,
                        @RequestParam(required = false) LocalDate toDate,
                        @RequestParam(required = false) Long teamId,
                        @RequestParam(required = false) Long partId,
                        @RequestParam(required = false) Long markId,
                        @RequestParam(required = false) Long shiftId,
                        Model model) {
        ReportData data = reportService.generate(type, fromDate, toDate, teamId, partId, markId, shiftId);
        model.addAttribute("report", data);
        model.addAttribute("generated", true);
        return "report-print";
    }

    private void addFilters(Model model, String type, LocalDate fromDate, LocalDate toDate,
                            Long teamId, Long partId, Long markId, Long shiftId) {
        model.addAttribute("type", type);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("teamId", teamId);
        model.addAttribute("partId", partId);
        model.addAttribute("markId", markId);
        model.addAttribute("shiftId", shiftId);
        model.addAttribute("teams", referenceService.teams());
        model.addAttribute("parts", referenceService.parts(true));
        model.addAttribute("marks", referenceService.marks(true));
        model.addAttribute("shifts", referenceService.shifts(true));
    }

    private String csv(ReportData report) {
        List<String> lines = new ArrayList<>();
        lines.add(csvRow("Packaging Management System - " + title(report.type())));
        lines.add(csvRow("From date", report.fromDate() == null ? "All dates" : DATE_FORMAT.format(report.fromDate()),
                "To date", report.toDate() == null ? "All dates" : DATE_FORMAT.format(report.toDate())));
        lines.add(csvRow("Generated", report.generatedAt().toString()));
        lines.add("");
        if (!report.attendance().isEmpty() || "ATTENDANCE".equals(report.type()) || "COMBINED".equals(report.type())) {
            lines.add(csvRow("Attendance"));
            lines.add(csvRow("Date", "Employee Number", "Employee Name", "Team", "Status",
                    "Total Manpower", "Present Count", "Absent Count", "Attendance Percentage"));
            for (AttendanceRecord row : report.attendance()) {
                AttendanceTotals totals = report.dailyAttendance().get(row.date());
                lines.add(csvRow(DATE_FORMAT.format(row.date()), row.employeeNumber(), row.employeeName(),
                        row.teamName(), row.status(), Integer.toString(totals.manpower()),
                        Integer.toString(totals.present()), Integer.toString(totals.absent()),
                        Double.toString(totals.percentage()) + "%"));
            }
            lines.add(csvRow("Attendance totals", Integer.toString(report.attendanceTotals().manpower()),
                    Integer.toString(report.attendanceTotals().present()),
                    Integer.toString(report.attendanceTotals().absent()),
                    Double.toString(report.attendanceTotals().percentage()) + "%"));
            if (report.attendance().isEmpty()) {
                lines.add(csvRow("No attendance records matched the selected filters."));
            }
            lines.add("");
        }
        if (!report.production().isEmpty() || "PRODUCTION".equals(report.type()) || "COMBINED".equals(report.type())) {
            lines.add(csvRow("Production"));
            lines.add(csvRow("Date", "Part Number", "Mark", "Shift", "Production Quantity"));
            for (ProductionRecord row : report.production()) {
                lines.add(csvRow(DATE_FORMAT.format(row.date()), row.partNumber(), row.markName(),
                        row.shiftName(), Integer.toString(row.quantity())));
            }
            lines.add(csvRow("Production total", Long.toString(report.productionTotal())));
            if (report.production().isEmpty()) {
                lines.add(csvRow("No production records matched the selected filters."));
            }
        }
        return String.join("\r\n", lines) + "\r\n";
    }

    private String title(String type) {
        return switch (type) {
            case "PRODUCTION" -> "Production Report";
            case "COMBINED" -> "Combined Attendance and Production Report";
            default -> "Attendance Report";
        };
    }

    private String csvRow(String... values) {
        List<String> cells = new ArrayList<>(values.length);
        for (String value : values) {
            String safe = value == null ? "" : value;
            if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
                safe = "'" + safe;
            }
            cells.add("\"" + safe.replace("\"", "\"\"") + "\"");
        }
        return String.join(",", cells);
    }
}
