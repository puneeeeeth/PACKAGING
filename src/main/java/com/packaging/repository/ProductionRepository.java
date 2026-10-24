package com.packaging.repository;

import com.packaging.model.ViewModels.ProductionRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ProductionRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public ProductionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductionRecord> records(LocalDate date, LocalDate fromDate, LocalDate toDate,
                                          Long partId, Long markId, Long shiftId) {
        return records(date, fromDate, toDate, partId, markId, shiftId, null);
    }

    public List<ProductionRecord> records(LocalDate date, LocalDate fromDate, LocalDate toDate,
                                          Long partId, Long markId, Long shiftId, String operatorName) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.production_id, p.production_date, p.operator_name, p.part_id, part.part_number,
                       p.mark_id, mark.mark_name, p.shift_id, shift.shift_name,
                       p.production_quantity, p.remarks, p.created_at, p.updated_at
                FROM production_entries p
                JOIN parts part ON part.part_id = p.part_id
                JOIN marks mark ON mark.mark_id = p.mark_id
                LEFT JOIN shifts shift ON shift.shift_id = p.shift_id
                WHERE 1 = 1
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (date != null) {
            sql.append(" AND p.production_date = :date");
            params.addValue("date", date);
        } else {
            if (fromDate != null) {
                sql.append(" AND p.production_date >= :fromDate");
                params.addValue("fromDate", fromDate);
            }
            if (toDate != null) {
                sql.append(" AND p.production_date <= :toDate");
                params.addValue("toDate", toDate);
            }
        }
        if (partId != null) {
            sql.append(" AND p.part_id = :partId");
            params.addValue("partId", partId);
        }
        if (markId != null) {
            sql.append(" AND p.mark_id = :markId");
            params.addValue("markId", markId);
        }
        if (shiftId != null) {
            sql.append(" AND p.shift_id = :shiftId");
            params.addValue("shiftId", shiftId);
        }
        if (operatorName != null && !operatorName.isBlank()) {
            sql.append(" AND LOWER(p.operator_name) LIKE :operatorName");
            params.addValue("operatorName", "%" + operatorName.trim().toLowerCase() + "%");
        }
        sql.append(" ORDER BY p.production_date DESC, p.production_id DESC");
        return jdbc.query(sql.toString(), params, (rs, row) -> new ProductionRecord(
                rs.getLong("production_id"), rs.getDate("production_date").toLocalDate(),
                rs.getLong("part_id"), rs.getString("part_number"),
                rs.getLong("mark_id"), rs.getString("mark_name"),
                rs.getObject("shift_id", Long.class), rs.getString("shift_name"),
                rs.getString("operator_name"),
                rs.getInt("production_quantity"), rs.getString("remarks"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()));
    }

    public long save(Long id, LocalDate date, String operatorName, long partId, long markId, Long shiftId,
                     int quantity, String remarks) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date).addValue("operatorName", operatorName)
                .addValue("partId", partId).addValue("markId", markId)
                .addValue("shiftId", shiftId).addValue("quantity", quantity).addValue("remarks", remarks);
        if (id == null) {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.update("INSERT INTO production_entries " +
                            "(production_date, operator_name, part_id, mark_id, shift_id, production_quantity, remarks) " +
                            "VALUES (:date, :operatorName, :partId, :markId, :shiftId, :quantity, :remarks)", params, keys,
                    new String[]{"production_id"});
            Number generatedId = keys.getKey();
            if (generatedId == null) {
                throw new IllegalStateException("The database did not return the new production record ID.");
            }
            return generatedId.longValue();
        }
        params.addValue("id", id);
        int updated = jdbc.update("UPDATE production_entries SET production_date = :date, operator_name = :operatorName, part_id = :partId, " +
                "mark_id = :markId, shift_id = :shiftId, production_quantity = :quantity, remarks = :remarks, " +
                "updated_at = CURRENT_TIMESTAMP WHERE production_id = :id", params);
        if (updated == 0) {
            throw new IllegalArgumentException("Production record was not found.");
        }
        return id;
    }

    public void delete(long id) {
        int deleted = jdbc.update("DELETE FROM production_entries WHERE production_id = :id",
                new MapSqlParameterSource("id", id));
        if (deleted == 0) {
            throw new IllegalArgumentException("Production record was not found.");
        }
    }

    public boolean activePart(long id) {
        return isActive("parts", "part_id", id);
    }

    public boolean activeMark(long id) {
        return isActive("marks", "mark_id", id);
    }

    public boolean activeShift(long id) {
        return isActive("shifts", "shift_id", id);
    }

    public boolean entryUsesLookup(long productionId, String type, long lookupId) {
        String table;
        String idColumn;
        switch (type) {
            case "part" -> {
                table = "parts";
                idColumn = "part_id";
            }
            case "mark" -> {
                table = "marks";
                idColumn = "mark_id";
            }
            case "shift" -> {
                table = "shifts";
                idColumn = "shift_id";
            }
            default -> throw new IllegalArgumentException("Unknown production lookup type.");
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM production_entries " +
                        "WHERE production_id = :productionId AND " + type + "_id = :lookupId",
                new MapSqlParameterSource().addValue("productionId", productionId).addValue("lookupId", lookupId),
                Integer.class);
        return count != null && count > 0;
    }

    private boolean isActive(String table, String idColumn, long id) {
        List<Boolean> values = jdbc.query("SELECT is_active FROM " + table + " WHERE " + idColumn + " = :id",
                new MapSqlParameterSource("id", id), (rs, row) -> rs.getBoolean(1));
        return !values.isEmpty() && values.get(0);
    }
}
