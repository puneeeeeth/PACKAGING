package com.packaging.service;

import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.repository.ProductionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductionService {
    private final ProductionRepository repository;

    public ProductionService(ProductionRepository repository) {
        this.repository = repository;
    }

    public List<ProductionRecord> records(LocalDate date, LocalDate fromDate, LocalDate toDate,
                                          Long partId, Long markId, Long shiftId) {
        validateRange(fromDate, toDate);
        return repository.records(date, fromDate, toDate, partId, markId, shiftId);
    }

    public List<ProductionRecord> records(LocalDate date, LocalDate fromDate, LocalDate toDate,
                                          Long partId, Long markId, Long shiftId, String operatorName) {
        validateRange(fromDate, toDate);
        return repository.records(date, fromDate, toDate, partId, markId, shiftId, operatorName);
    }

    @Transactional
    public long save(Long id, LocalDate date, Long partId, Long markId, Long shiftId,
                     Integer quantity, String remarks) {
        return save(id, date, "Unassigned", partId, markId, shiftId, quantity, remarks);
    }

    @Transactional
    public long save(Long id, LocalDate date, String operatorName, Long partId, Long markId, Long shiftId,
                     Integer quantity, String remarks) {
        if (date == null) {
            throw new IllegalArgumentException("Production date is required.");
        }
        if (operatorName == null || operatorName.isBlank() || operatorName.trim().length() > 150) {
            throw new IllegalArgumentException("Enter an operator name of 150 characters or fewer.");
        }
        if (partId == null || (!repository.activePart(partId) &&
                (id == null || !repository.entryUsesLookup(id, "part", partId)))) {
            throw new IllegalArgumentException("Select an active part number.");
        }
        if (markId == null || (!repository.activeMark(markId) &&
                (id == null || !repository.entryUsesLookup(id, "mark", markId)))) {
            throw new IllegalArgumentException("Select an active market.");
        }
        if (shiftId != null && (!repository.activeShift(shiftId) &&
                (id == null || !repository.entryUsesLookup(id, "shift", shiftId)))) {
            throw new IllegalArgumentException("Select an active shift.");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Production quantity must be a non-negative integer.");
        }
        if (remarks != null && remarks.length() > 1000) {
            throw new IllegalArgumentException("Remarks must be 1,000 characters or fewer.");
        }
        return repository.save(id, date, operatorName.trim(), partId, markId, shiftId, quantity,
                remarks == null || remarks.isBlank() ? null : remarks.trim());
    }

    @Transactional
    public void delete(long id) {
        repository.delete(id);
    }

    public void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be on or before To date.");
        }
    }
}
