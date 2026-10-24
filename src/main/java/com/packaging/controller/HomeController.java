package com.packaging.controller;

import com.packaging.service.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class HomeController {
    private final SummaryService summaryService;

    public HomeController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        model.addAttribute("todaySummary", summaryService.calculate(today, null, null, null,
                null, null, null));
        return "home";
    }
}
