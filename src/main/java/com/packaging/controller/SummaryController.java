package com.packaging.controller;

import com.packaging.model.ViewModels.SummaryData;
import com.packaging.service.ReferenceService;
import com.packaging.service.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class SummaryController {
    private final SummaryService summaryService;
    private final ReferenceService referenceService;

    public SummaryController(SummaryService summaryService, ReferenceService referenceService) {
        this.summaryService = summaryService;
        this.referenceService = referenceService;
    }

    @GetMapping("/summary")
    public String summary(@RequestParam(required = false) LocalDate selectedDate,
                          @RequestParam(required = false) LocalDate fromDate,
                          @RequestParam(required = false) LocalDate toDate,
                          @RequestParam(required = false) Long teamId,
                          @RequestParam(required = false) Long partId,
                          @RequestParam(required = false) Long markId,
                          @RequestParam(required = false) Long shiftId,
                          Model model) {
        SummaryData summary = null;
        try {
            summary = summaryService.calculate(selectedDate, fromDate, toDate, teamId, partId, markId, shiftId);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        model.addAttribute("summary", summary);
        model.addAttribute("selectedDate", selectedDate);
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
        return "summary";
    }
}
