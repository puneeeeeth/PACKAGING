package com.packaging.controller;

import com.packaging.model.ViewModels.ReportData;
import com.packaging.service.ExcelExportService;
import com.packaging.service.ReferenceService;
import com.packaging.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ReportController {
    private final ReportService reportService;
    private final ReferenceService referenceService;
    private final ExcelExportService excelExportService;

    public ReportController(ReportService reportService, ReferenceService referenceService,
                            ExcelExportService excelExportService) {
        this.reportService = reportService;
        this.referenceService = referenceService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/report")
    public String report(@RequestParam(required = false) LocalDate fromDate,
                         @RequestParam(required = false) LocalDate toDate,
                         @RequestParam(required = false) Long partId,
                         @RequestParam(required = false) Long markId,
                         @RequestParam(required = false) String operatorName,
                         Model model) {
        try {
            ReportData data = generate(fromDate, toDate, partId, markId, operatorName);
            addFilters(model, fromDate, toDate, partId, markId, operatorName);
            model.addAttribute("report", data);
            return "report";
        } catch (IllegalArgumentException ex) {
            addFilters(model, fromDate, toDate, partId, markId, operatorName);
            model.addAttribute("error", ex.getMessage());
            return "report";
        }
    }

    @GetMapping("/report/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) LocalDate fromDate,
                                              @RequestParam(required = false) LocalDate toDate,
                                              @RequestParam(required = false) Long partId,
                                              @RequestParam(required = false) Long markId,
                                              @RequestParam(required = false) String operatorName) {
        ReportData data = generate(fromDate, toDate, partId, markId, operatorName);
        byte[] content = excelExportService.report(data);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"packaging-report-" +
                        LocalDate.now() + ".xls\"")
                .body(content);
    }

    @GetMapping("/report/print")
    public String print(@RequestParam(required = false) LocalDate fromDate,
                        @RequestParam(required = false) LocalDate toDate,
                        @RequestParam(required = false) Long partId,
                        @RequestParam(required = false) Long markId,
                        @RequestParam(required = false) String operatorName,
                        Model model) {
        model.addAttribute("report", generate(fromDate, toDate, partId, markId, operatorName));
        return "report-print";
    }

    private ReportData generate(LocalDate fromDate, LocalDate toDate, Long partId,
                                Long markId, String operatorName) {
        return reportService.generate("PRODUCTION", fromDate, toDate, null,
                partId, markId, null, operatorName);
    }

    private void addFilters(Model model, LocalDate fromDate, LocalDate toDate, Long partId,
                            Long markId, String operatorName) {
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("partId", partId);
        model.addAttribute("markId", markId);
        model.addAttribute("operatorName", operatorName);
        model.addAttribute("parts", referenceService.parts(true));
        model.addAttribute("markets", referenceService.marks(true));
    }
}
