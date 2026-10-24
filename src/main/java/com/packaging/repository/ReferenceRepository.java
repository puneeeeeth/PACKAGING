package com.packaging.repository;

import com.packaging.model.ViewModels.Employee;
import com.packaging.model.ViewModels.LookupOption;
import com.packaging.model.ViewModels.Team;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReferenceRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public ReferenceRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Team> teams() {
        return jdbc.query("SELECT team_id, team_name FROM teams ORDER BY team_id",
                (rs, row) -> new Team(rs.getLong("team_id"), rs.getString("team_name")));
    }

    public void renameTeam(long id, String name) {
        int updated = jdbc.update("UPDATE teams SET team_name = :name WHERE team_id = :id",
                new MapSqlParameterSource().addValue("id", id).addValue("name", name));
        if (updated == 0) {
            throw new IllegalArgumentException("Team was not found.");
        }
    }

    public List<Employee> employees(Long teamId, boolean activeOnly) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.employee_id, e.employee_number, e.employee_name, e.team_id,
                       t.team_name, e.is_active
                FROM employees e JOIN teams t ON t.team_id = e.team_id
                WHERE 1 = 1
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (teamId != null) {
            sql.append(" AND e.team_id = :teamId");
            params.addValue("teamId", teamId);
        }
        if (activeOnly) {
            sql.append(" AND e.is_active = TRUE");
        }
        sql.append(" ORDER BY t.team_id, e.employee_number");
        return jdbc.query(sql.toString(), params,
                (rs, row) -> new Employee(rs.getLong("employee_id"), rs.getString("employee_number"),
                        rs.getString("employee_name"), rs.getLong("team_id"),
                        rs.getString("team_name"), rs.getBoolean("is_active")));
    }

    public long saveEmployee(Long id, String number, String name, long teamId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("number", number)
                .addValue("name", name)
                .addValue("teamId", teamId);
        if (id == null) {
            jdbc.update("INSERT INTO employees (employee_number, employee_name, team_id) " +
                    "VALUES (:number, :name, :teamId)", params);
            return jdbc.queryForObject("SELECT employee_id FROM employees WHERE employee_number = :number",
                    params, Long.class);
        }
        params.addValue("id", id);
        int updated = jdbc.update("UPDATE employees SET employee_number = :number, " +
                "employee_name = :name, team_id = :teamId WHERE employee_id = :id", params);
        if (updated == 0) {
            throw new IllegalArgumentException("Employee record was not found.");
        }
        return id;
    }

    public void setEmployeeActive(long id, boolean active) {
        int updated = jdbc.update("UPDATE employees SET is_active = :active WHERE employee_id = :id",
                new MapSqlParameterSource().addValue("id", id).addValue("active", active));
        if (updated == 0) {
            throw new IllegalArgumentException("Employee record was not found.");
        }
    }

    public List<LookupOption> parts(boolean includeInactive) {
        return lookup("parts", "part_id", "part_number", "part_name", includeInactive);
    }

    public List<LookupOption> marks(boolean includeInactive) {
        return lookup("marks", "mark_id", "mark_name", "mark_name", includeInactive);
    }

    public List<LookupOption> shifts(boolean includeInactive) {
        return lookup("shifts", "shift_id", "shift_name", "shift_name", includeInactive);
    }

    public void addPart(String number, String name) {
        jdbc.update("INSERT INTO parts (part_number, part_name) VALUES (:code, :name)",
                new MapSqlParameterSource().addValue("code", number).addValue("name", name));
    }

    public void addMark(String name) {
        jdbc.update("INSERT INTO marks (mark_name) VALUES (:name)",
                new MapSqlParameterSource("name", name));
    }

    public void addShift(String name) {
        jdbc.update("INSERT INTO shifts (shift_name) VALUES (:name)",
                new MapSqlParameterSource("name", name));
    }

    public void setLookupActive(String type, long id, boolean active) {
        String table;
        String column;
        switch (type) {
            case "part" -> {
                table = "parts";
                column = "part_id";
            }
            case "mark" -> {
                table = "marks";
                column = "mark_id";
            }
            case "shift" -> {
                table = "shifts";
                column = "shift_id";
            }
            default -> throw new IllegalArgumentException("Unknown configuration type.");
        }
        int updated = jdbc.update("UPDATE " + table + " SET is_active = :active WHERE " + column + " = :id",
                new MapSqlParameterSource().addValue("active", active).addValue("id", id));
        if (updated == 0) {
            throw new IllegalArgumentException("Configuration record was not found.");
        }
    }

    private List<LookupOption> lookup(String table, String idColumn, String codeColumn,
                                      String nameColumn, boolean includeInactive) {
        String sql = "SELECT " + idColumn + ", " + codeColumn + ", " + nameColumn + ", is_active " +
                "FROM " + table + (includeInactive ? "" : " WHERE is_active = TRUE") +
                " ORDER BY " + codeColumn;
        return jdbc.getJdbcTemplate().query(sql,
                (rs, row) -> new LookupOption(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)));
    }
}
