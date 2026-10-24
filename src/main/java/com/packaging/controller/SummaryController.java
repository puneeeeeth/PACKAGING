package com.packaging.controller;

import com.packaging.model.ViewModels.SummaryData;
import com.packaging.service.ExcelExportService;
import com.packaging.service.ReferenceService;
import com.packaging.service.SummaryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class SummaryController {
    private final SummaryService summaryService;
    private final ReferenceService referenceService;
    private final ExcelExportService excelExportService;

    public SummaryController(SummaryService summaryService, ReferenceService referenceService,
                             ExcelExportService excelExportService) {
        this.summaryService = summaryService;
        this.referenceService = referenceService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/summary")
    public String summary(@RequestParam(required = false) LocalDate selectedDate,
                          @RequestParam(required = false) Long teamId,
                          @RequestParam(required = false) Long partId,
                          @RequestParam(required = false) Long markId,
                          Model model) {
        LocalDate date = selectedDate == null ? LocalDate.now() : selectedDate;
        SummaryData data = summaryService.calculate(date, null, null, teamId, partId, markId, null);
        model.addAttribute("summary", data);
        model.addAttribute("selectedDate", date);
        model.addAttribute("teamId", teamId);
        model.addAttribute("partId", partId);
        model.addAttribute("markId", markId);
        model.addAttribute("teams", referenceService.teams());
        model.addAttribute("parts", referenceService.parts(true));
        model.addAttribute("markets", referenceService.marks(true));
        return "summary";
    }

    @GetMapping("/summary/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) LocalDate selectedDate,
                                              @RequestParam(required = false) Long teamId,
                                              @RequestParam(required = false) Long partId,
                                              @RequestParam(required = false) Long markId) {
        LocalDate date = selectedDate == null ? LocalDate.now() : selectedDate;
        SummaryData data = summaryService.calculate(date, null, null, teamId, partId, markId, null);
        return excelResponse(excelExportService.summary(data, date), "packaging-summary-" + date + ".xls");
    }

    private ResponseEntity<byte[]> excelResponse(byte[] content, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}
