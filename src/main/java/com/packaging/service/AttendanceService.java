package com.packaging.service;

import com.packaging.model.ViewModels.AttendanceEntryRow;
import com.packaging.model.ViewModels.AttendanceRecord;
import com.packaging.model.ViewModels.AttendanceTotals;
import com.packaging.model.ViewModels.TeamAttendance;
import com.packaging.repository.AttendanceRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceRepository repository;
    private final ReferenceService referenceService;

    public AttendanceService(AttendanceRepository repository, ReferenceService referenceService) {
        this.repository = repository;
        this.referenceService = referenceService;
    }

    @Transactional
    public void submit(LocalDate date, Long teamId, String employeeNumber, String employeeName, String status) {
        if (date == null) {
            throw new IllegalArgumentException("Attendance date is required.");
        }
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        if (!"P".equals(normalizedStatus) && !"A".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Choose Present (P) or Absent (A).");
        }
        long employeeId = referenceService.registerAttendanceEmployee(employeeNumber, employeeName, teamId);
        try {
            repository.saveOrUpdate(employeeId, date, normalizedStatus);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Attendance could not be saved because this employee already has a record for the selected date.");
        }
    }

    public List<AttendanceEntryRow> entryRows(LocalDate date, Long teamId) {
        return repository.entryRows(date, teamId);
    }

    public List<AttendanceRecord> records(LocalDate date, LocalDate fromDate, LocalDate toDate,
                                          Long teamId, String employeeNumber, String employeeName) {
        validateRange(fromDate, toDate);
        return repository.records(date, fromDate, toDate, teamId, employeeNumber, employeeName);
    }

    @Transactional
    public int save(LocalDate date, Long teamId, List<Long> employeeIds, List<String> statuses) {
        return persist(date, teamId, employeeIds, statuses, false);
    }

    @Transactional
    public int update(LocalDate date, Long teamId, List<Long> employeeIds, List<String> statuses) {
        return persist(date, teamId, employeeIds, statuses, true);
    }

    public AttendanceTotals totals(List<AttendanceRecord> records) {
        int present = (int) records.stream().filter(row -> "P".equals(row.status())).count();
        int absent = (int) records.stream().filter(row -> "A".equals(row.status())).count();
        return totals(present, absent);
    }

    public List<TeamAttendance> byTeam(List<AttendanceRecord> records) {
        return records.stream().collect(java.util.stream.Collectors.groupingBy(
                        AttendanceRecord::teamName, java.util.TreeMap::new, java.util.stream.Collectors.toList()))
                .entrySet().stream().map(entry -> {
                    int present = (int) entry.getValue().stream().filter(row -> "P".equals(row.status())).count();
                    int absent = (int) entry.getValue().stream().filter(row -> "A".equals(row.status())).count();
                    AttendanceTotals totals = totals(present, absent);
                    return new TeamAttendance(entry.getKey(), totals.manpower(), totals.present(),
                            totals.absent(), totals.percentage());
                }).toList();
    }

    public AttendanceTotals totals(int present, int absent) {
        int manpower = present + absent;
        double percentage = manpower == 0 ? 0.0 : Math.round(present * 1000.0 / manpower) / 10.0;
        return new AttendanceTotals(manpower, present, absent, percentage);
    }

    public void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be on or before To date.");
        }
    }

    private int persist(LocalDate date, Long teamId, List<Long> employeeIds,
                        List<String> statuses, boolean update) {
        if (date == null) {
            throw new IllegalArgumentException("Attendance date is required.");
        }
        if (employeeIds == null || statuses == null || employeeIds.size() != statuses.size()) {
            throw new IllegalArgumentException("Attendance form data is incomplete. Reload the page and try again.");
        }
        int changed = 0;
        for (int index = 0; index < employeeIds.size(); index++) {
            Long employeeId = employeeIds.get(index);
            String status = statuses.get(index);
            if (employeeId == null || status == null || status.isBlank()) {
                continue;
            }
            status = status.trim().toUpperCase();
            if (!"P".equals(status) && !"A".equals(status)) {
                throw new IllegalArgumentException("Attendance status must be P or A.");
            }
            Long employeeTeam = repository.employeeTeam(employeeId);
            if (employeeTeam == null) {
                throw new IllegalArgumentException("An employee in the attendance form no longer exists.");
            }
            if (teamId != null && !teamId.equals(employeeTeam)) {
                throw new IllegalArgumentException("An employee does not belong to the selected team.");
            }
            if (!update && !repository.employeeIsActive(employeeId)) {
                throw new IllegalArgumentException("Inactive employees cannot receive new attendance records.");
            }
            if (update) {
                if (repository.update(employeeId, date, status) == 0) {
                    throw new IllegalArgumentException("No saved attendance exists for employee ID " +
                            employeeId + " on " + date + ". Use Save Attendance for new records.");
                }
            } else {
                try {
                    repository.insert(employeeId, date, status);
                } catch (DuplicateKeyException ex) {
                    throw new IllegalArgumentException("Attendance already exists for an employee on " + date +
                            ". Use Update Attendance to change saved records.");
                }
            }
            changed++;
        }
        if (changed == 0) {
            throw new IllegalArgumentException("Choose P or A for at least one employee before saving.");
        }
        return changed;
    }
}
