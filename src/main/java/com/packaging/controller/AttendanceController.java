package com.packaging.controller;

import com.packaging.model.ViewModels.AttendanceRecord;
import com.packaging.model.ViewModels.Employee;
import com.packaging.service.AttendanceService;
import com.packaging.service.ReferenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final ReferenceService referenceService;

    public AttendanceController(AttendanceService attendanceService, ReferenceService referenceService) {
        this.attendanceService = attendanceService;
        this.referenceService = referenceService;
    }

    @GetMapping("/attendance")
    public String attendance(@RequestParam(required = false) LocalDate entryDate,
                             @RequestParam(required = false) Long entryTeamId,
                             @RequestParam(required = false) LocalDate recordDate,
                             @RequestParam(required = false) Long recordTeamId,
                             @RequestParam(required = false) String employeeSearch,
                             @RequestParam(required = false) Long editEmployeeId,
                             Model model) {
        LocalDate selectedEntryDate = entryDate == null ? LocalDate.now() : entryDate;
        LocalDate selectedRecordDate = recordDate == null ? selectedEntryDate : recordDate;
        var teams = referenceService.teams();
        if (entryTeamId == null && !teams.isEmpty()) {
            entryTeamId = teams.get(0).id();
        }
        List<AttendanceRecord> rows = attendanceService.records(selectedRecordDate, null, null,
                recordTeamId, employeeSearch, null);
        model.addAttribute("entryDate", selectedEntryDate);
        model.addAttribute("entryTeamId", entryTeamId);
        model.addAttribute("recordDate", selectedRecordDate);
        model.addAttribute("recordTeamId", recordTeamId);
        model.addAttribute("employeeSearchFilter", employeeSearch);
        model.addAttribute("attendanceRecords", rows);
        model.addAttribute("attendanceTotals", attendanceService.totals(rows));
        model.addAttribute("teams", teams);
        model.addAttribute("employees", referenceService.employees(null, false));
        Employee editEmployee = editEmployeeId == null ? null : referenceService.employees(null, false).stream()
                .filter(employee -> employee.id() == editEmployeeId).findFirst().orElse(null);
        model.addAttribute("editEmployee", editEmployee);
        return "attendance";
    }

    @PostMapping("/attendance/save")
    public String saveAttendance(@RequestParam(required = false) LocalDate date,
                                 @RequestParam(required = false) Long teamId,
                                 @RequestParam(required = false) String employeeNumber,
                                 @RequestParam(required = false) String employeeName,
                                 @RequestParam(required = false) String status,
                                 RedirectAttributes redirect) {
        try {
            attendanceService.submit(date, teamId, employeeNumber, employeeName, status);
            redirect.addFlashAttribute("success", "Attendance submitted.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return attendanceRedirect(date, teamId);
    }

    @PostMapping("/attendance/update")
    public String updateAttendance(@RequestParam(required = false) LocalDate date,
                                   @RequestParam(required = false) Long teamId,
                                   @RequestParam(required = false) List<Long> employeeIds,
                                   @RequestParam(required = false) List<String> statuses,
                                   RedirectAttributes redirect) {
        try {
            int count = attendanceService.update(date, teamId, employeeIds, statuses);
            redirect.addFlashAttribute("success", count + " attendance record(s) updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return attendanceRedirect(date, teamId);
    }

    @PostMapping("/attendance/employee/save")
    public String saveEmployee(@RequestParam(required = false) Long employeeId,
                               @RequestParam(required = false) String employeeNumber,
                               @RequestParam(required = false) String employeeName,
                               @RequestParam(required = false) Long teamId,
                               RedirectAttributes redirect) {
        try {
            referenceService.saveEmployee(employeeId, employeeNumber, employeeName, teamId);
            redirect.addFlashAttribute("success", employeeId == null
                    ? "Employee added." : "Employee details updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/attendance#employees";
    }

    @PostMapping("/attendance/team/save")
    public String saveTeam(@RequestParam long teamId, @RequestParam String teamName,
                           RedirectAttributes redirect) {
        try {
            referenceService.renameTeam(teamId, teamName);
            redirect.addFlashAttribute("success", "Team name updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/attendance#teams";
    }

    @PostMapping("/attendance/employee/{id}/active")
    public String setEmployeeActive(@PathVariable long id, @RequestParam boolean active,
                                    RedirectAttributes redirect) {
        try {
            referenceService.setEmployeeActive(id, active);
            redirect.addFlashAttribute("success", active ? "Employee activated." : "Employee deactivated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/attendance#employees";
    }

    private String attendanceRedirect(LocalDate date, Long teamId) {
        String path = "redirect:/attendance?entryDate=" + (date == null ? LocalDate.now() : date);
        if (teamId != null) {
            path += "&entryTeamId=" + teamId;
        }
        return path;
    }
}
