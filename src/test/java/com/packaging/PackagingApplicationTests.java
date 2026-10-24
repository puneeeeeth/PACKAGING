package com.packaging;

import com.packaging.model.ViewModels.AttendanceRecord;
import com.packaging.model.ViewModels.AttendanceTotals;
import com.packaging.model.ViewModels.LookupOption;
import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.model.ViewModels.ReportData;
import com.packaging.model.ViewModels.Team;
import com.packaging.repository.AttendanceRepository;
import com.packaging.repository.ProductionRepository;
import com.packaging.service.AttendanceService;
import com.packaging.service.ProductionService;
import com.packaging.service.ReferenceService;
import com.packaging.service.ReportService;
import com.packaging.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PackagingApplicationTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ReferenceService referenceService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ProductionService productionService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ProductionRepository productionRepository;

    @BeforeEach
    void clearData() {
        jdbc.update("DELETE FROM production_entries");
        jdbc.update("DELETE FROM attendance");
        jdbc.update("DELETE FROM employees");
        jdbc.update("DELETE FROM parts");
        jdbc.update("DELETE FROM marks");
        jdbc.update("DELETE FROM shifts");
    }

    @Test
    void allFourModulesAndHomeRender() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Packaging Management System")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Production Entry")));
        mvc.perform(get("/attendance")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attendance Entry")));
        mvc.perform(get("/production")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Production Entry")));
        mvc.perform(get("/summary")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attendance Summary")));
        mvc.perform(get("/report")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Report Filters")));
    }

    @Test
    void teamsAndAttendanceSaveUpdateDuplicateAndCalculate() {
        List<Team> teams = referenceService.teams();
        assertEquals(4, teams.size());
        assertEquals(0.0, attendanceService.totals(List.of()).percentage());
        long teamId = teams.get(0).id();
        long firstEmployee = createEmployee("E-101", "Worker One", teamId);
        long secondEmployee = createEmployee("E-102", "Worker Two", teamId);
        LocalDate date = LocalDate.of(2026, 3, 10);

        assertEquals(2, attendanceService.save(date, teamId,
                List.of(firstEmployee, secondEmployee), List.of("P", "A")));
        assertThrows(IllegalArgumentException.class, () -> attendanceService.save(date, teamId,
                List.of(firstEmployee, secondEmployee), List.of("A", "P")));
        assertEquals(1, attendanceService.update(date, teamId,
                List.of(firstEmployee), List.of("A")));
        assertEquals(1, attendanceService.update(date, teamId,
                List.of(firstEmployee), List.of("P")));

        List<AttendanceRecord> rows = attendanceService.records(date, null, null, teamId, null, null);
        AttendanceTotals totals = attendanceService.totals(rows);
        assertEquals(2, totals.manpower());
        assertEquals(1, totals.present());
        assertEquals(1, totals.absent());
        assertEquals(50.0, totals.percentage());
        assertEquals(2, attendanceRepository.records(date, null, null, teamId, null, null).size());
    }

    @Test
    void teamNamesCanBeConfiguredWithoutChangingTeamCount() {
        Team team = referenceService.teams().get(3);
        referenceService.renameTeam(team.id(), "Packing Team");
        assertEquals("Packing Team", referenceService.teams().get(3).name());
        assertEquals(4, referenceService.teams().size());
        referenceService.renameTeam(team.id(), team.name());
    }

    @Test
    void productionCrudValidationAndFilteredTotals() {
        LookupOption part = referenceService.parts(true).stream().findFirst().orElse(null);
        if (part == null) {
            referenceService.addPart("P-100", "Box");
            part = referenceService.parts(false).get(0);
        }
        LookupOption configuredPart = part;
        referenceService.addMark("M-1");
        referenceService.addShift("Day");
        LookupOption mark = referenceService.marks(false).get(0);
        LookupOption shift = referenceService.shifts(false).get(0);
        LocalDate date = LocalDate.of(2026, 3, 10);

        long id = productionService.save(null, date, configuredPart.id(), mark.id(), shift.id(), 25, "First run");
        assertThrows(IllegalArgumentException.class,
                () -> productionService.save(null, date, configuredPart.id(), mark.id(), shift.id(), -1, "Invalid"));
        referenceService.setLookupActive("part", configuredPart.id(), false);
        assertThrows(IllegalArgumentException.class,
                () -> productionService.save(null, date, configuredPart.id(), mark.id(), shift.id(), 5, "Inactive part"));
        productionService.save(id, date, configuredPart.id(), mark.id(), shift.id(), 30, "Updated");

        List<ProductionRecord> rows = productionService.records(date, null, null,
                configuredPart.id(), mark.id(), shift.id());
        assertEquals(1, rows.size());
        assertEquals(30, rows.get(0).quantity());
        assertEquals(30, summaryService.productionTotals(rows).total());
        assertEquals(1, productionRepository.records(date, null, null,
                configuredPart.id(), mark.id(), shift.id()).size());

        productionService.delete(id);
        assertEquals(0, productionService.records(date, null, null,
                configuredPart.id(), mark.id(), shift.id()).size());
    }

    @Test
    void reportFiltersAndExportsUseTheSavedRecords() throws Exception {
        long teamId = referenceService.teams().get(0).id();
        long employeeId = createEmployee("R-201", "Report Worker", teamId);
        LocalDate date = LocalDate.of(2026, 3, 10);
        attendanceService.save(date, teamId, List.of(employeeId), List.of("P"));
        referenceService.addPart("R-BOX", "Report Box");
        referenceService.addMark("R-MARK");
        referenceService.addShift("R-SHIFT");
        LookupOption part = referenceService.parts(false).get(0);
        LookupOption mark = referenceService.marks(false).get(0);
        LookupOption shift = referenceService.shifts(false).get(0);
        productionService.save(null, date, part.id(), mark.id(), shift.id(), 17, "Report run");

        ReportData report = reportService.generate("COMBINED", date, date, teamId,
                part.id(), mark.id(), shift.id());
        assertEquals(1, report.attendance().size());
        assertEquals(1, report.production().size());
        assertEquals(17, report.productionTotal());
        assertEquals(1, report.dailyAttendance().get(date).present());

        mvc.perform(get("/report/export/excel")
                        .param("type", "COMBINED")
                        .param("fromDate", date.toString())
                        .param("toDate", date.toString())
                        .param("teamId", Long.toString(teamId))
                        .param("partId", Long.toString(part.id()))
                        .param("markId", Long.toString(mark.id()))
                        .param("shiftId", Long.toString(shift.id())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("R-BOX")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("17")));

        mvc.perform(get("/report/print").param("type", "COMBINED")
                        .param("fromDate", date.toString()).param("toDate", date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Production Report")));
    }

    @Test
    void dateRangeFiltersRejectReversedDates() {
        assertThrows(IllegalArgumentException.class, () -> summaryService.calculate(null,
                LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 10), null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> reportService.generate("ATTENDANCE",
                LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 10), null, null, null, null));
    }

    private long createEmployee(String number, String name, long teamId) {
        referenceService.saveEmployee(null, number, name, teamId);
        return referenceService.employees(teamId, false).stream()
                .filter(employee -> employee.employeeNumber().equals(number))
                .findFirst().orElseThrow().id();
    }
}
