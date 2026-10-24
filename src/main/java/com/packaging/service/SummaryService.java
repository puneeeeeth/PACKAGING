package com.packaging.service;

import com.packaging.model.ViewModels.AttendanceRecord;
import com.packaging.model.ViewModels.AttendanceTotals;
import com.packaging.model.ViewModels.ProductionGroup;
import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.model.ViewModels.ProductionTotals;
import com.packaging.model.ViewModels.SummaryData;
import com.packaging.model.ViewModels.TeamAttendance;
import com.packaging.repository.AttendanceRepository;
import com.packaging.repository.ProductionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SummaryService {
    private final AttendanceRepository attendanceRepository;
    private final ProductionRepository productionRepository;
    private final AttendanceService attendanceService;

    public SummaryService(AttendanceRepository attendanceRepository,
                          ProductionRepository productionRepository,
                          AttendanceService attendanceService) {
        this.attendanceRepository = attendanceRepository;
        this.productionRepository = productionRepository;
        this.attendanceService = attendanceService;
    }

    public SummaryData calculate(LocalDate selectedDate, LocalDate fromDate, LocalDate toDate,
                                 Long teamId, Long partId, Long markId, Long shiftId) {
        attendanceService.validateRange(fromDate, toDate);
        if (selectedDate != null) {
            fromDate = null;
            toDate = null;
        }
        List<AttendanceRecord> attendance = attendanceRepository.records(selectedDate, fromDate, toDate,
                teamId, null, null);
        List<ProductionRecord> production = productionRepository.records(selectedDate, fromDate, toDate,
                partId, markId, shiftId);
        AttendanceTotals attendanceTotals = attendanceService.totals(attendance);
        List<TeamAttendance> teams = attendanceService.byTeam(attendance);
        ProductionTotals productionTotals = productionTotals(production);
        int availability = calculateAvailability(attendanceTotals);
        double productivity = availability == 0 ? 0.0
                : Math.round(productionTotals.total() * 10.0 / availability) / 10.0;
        return new SummaryData(attendanceTotals, teams, productionTotals, availability, productivity);
    }

    public ProductionTotals productionTotals(List<ProductionRecord> records) {
        long total = records.stream().mapToLong(ProductionRecord::quantity).sum();
        return new ProductionTotals(total,
                groups(records, ProductionRecord::partNumber),
                groups(records, ProductionRecord::markName),
                groups(records, ProductionRecord::shiftName));
    }

    public int calculateAvailability(AttendanceTotals attendance) {
        return attendance.present();
    }

    private List<ProductionGroup> groups(List<ProductionRecord> records,
                                         Function<ProductionRecord, String> name) {
        Map<String, Long> totals = records.stream().collect(Collectors.groupingBy(
                record -> {
                    String group = name.apply(record);
                    return group == null || group.isBlank() ? "Unspecified" : group;
                }, Collectors.summingLong(ProductionRecord::quantity)));
        return totals.entrySet().stream()
                .map(entry -> new ProductionGroup(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ProductionGroup::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
