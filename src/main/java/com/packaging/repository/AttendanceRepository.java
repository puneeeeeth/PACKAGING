package com.packaging.repository;

import com.packaging.model.ViewModels.AttendanceEntryRow;
import com.packaging.model.ViewModels.AttendanceRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AttendanceRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public AttendanceRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AttendanceEntryRow> entryRows(LocalDate date, Long teamId) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.employee_id, e.employee_number, e.employee_name, e.team_id,
                       t.team_name, e.is_active, a.status, a.attendance_id
                FROM employees e
                JOIN teams t ON t.team_id = e.team_id
                LEFT JOIN attendance a ON a.employee_id = e.employee_id AND a.attendance_date = :date
                WHERE (e.is_active = TRUE OR a.attendance_id IS NOT NULL)
                """);
        MapSqlParameterSource params = new MapSqlParameterSource("date", date);
        if (teamId != null) {
            sql.append(" AND e.team_id = :teamId");
            params.addValue("teamId", teamId);
        }
        sql.append(" ORDER BY t.team_id, e.employee_number");
        return jdbc.query(sql.toString(), params, (rs, row) -> {
            long attendanceId = rs.getLong("attendance_id");
            Long savedId = rs.wasNull() ? null : attendanceId;
            return new AttendanceEntryRow(rs.getLong("employee_id"), rs.getString("employee_number"),
                    rs.getString("employee_name"), rs.getLong("team_id"), rs.getString("team_name"),
                    rs.getBoolean("is_active"), rs.getString("status"), savedId);
        });
    }

    public List<AttendanceRecord> records(LocalDate date, LocalDate fromDate, LocalDate toDate,
                                         Long teamId, String employeeNumber, String employeeName) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.attendance_id, a.attendance_date, e.employee_id, e.employee_number,
                       e.employee_name, e.team_id, t.team_name, a.status, e.is_active
                FROM attendance a
                JOIN employees e ON e.employee_id = a.employee_id
                JOIN teams t ON t.team_id = e.team_id
                WHERE 1 = 1
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (date != null) {
            sql.append(" AND a.attendance_date = :date");
            params.addValue("date", date);
        } else {
            if (fromDate != null) {
                sql.append(" AND a.attendance_date >= :fromDate");
                params.addValue("fromDate", fromDate);
            }
            if (toDate != null) {
                sql.append(" AND a.attendance_date <= :toDate");
                params.addValue("toDate", toDate);
            }
        }
        if (teamId != null) {
            sql.append(" AND e.team_id = :teamId");
            params.addValue("teamId", teamId);
        }
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            sql.append(" AND LOWER(e.employee_number) LIKE :employeeNumber");
            params.addValue("employeeNumber", "%" + employeeNumber.trim().toLowerCase() + "%");
        }
        if (employeeName != null && !employeeName.isBlank()) {
            sql.append(" AND LOWER(e.employee_name) LIKE :employeeName");
            params.addValue("employeeName", "%" + employeeName.trim().toLowerCase() + "%");
        }
        sql.append(" ORDER BY a.attendance_date DESC, t.team_id, e.employee_number");
        return jdbc.query(sql.toString(), params, (rs, row) -> new AttendanceRecord(
                rs.getLong("attendance_id"), rs.getDate("attendance_date").toLocalDate(),
                rs.getLong("employee_id"), rs.getString("employee_number"), rs.getString("employee_name"),
                rs.getLong("team_id"), rs.getString("team_name"), rs.getString("status"),
                rs.getBoolean("is_active")));
    }

    public void insert(long employeeId, LocalDate date, String status) {
        jdbc.update("INSERT INTO attendance (employee_id, attendance_date, status) " +
                        "VALUES (:employeeId, :date, :status)",
                new MapSqlParameterSource().addValue("employeeId", employeeId)
                        .addValue("date", date).addValue("status", status));
    }

    public int update(long employeeId, LocalDate date, String status) {
        return jdbc.update("UPDATE attendance SET status = :status, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE employee_id = :employeeId AND attendance_date = :date",
                new MapSqlParameterSource().addValue("employeeId", employeeId)
                        .addValue("date", date).addValue("status", status));
    }

    public boolean employeeExists(long employeeId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM employees WHERE employee_id = :id",
                new MapSqlParameterSource("id", employeeId), Integer.class);
        return count != null && count > 0;
    }

    public boolean employeeIsActive(long employeeId) {
        Boolean active = jdbc.queryForObject("SELECT is_active FROM employees WHERE employee_id = :id",
                new MapSqlParameterSource("id", employeeId), Boolean.class);
        return Boolean.TRUE.equals(active);
    }

    public Long employeeTeam(long employeeId) {
        List<Long> values = jdbc.query("SELECT team_id FROM employees WHERE employee_id = :id",
                new MapSqlParameterSource("id", employeeId), (rs, row) -> rs.getLong(1));
        return values.isEmpty() ? null : values.get(0);
    }
}
