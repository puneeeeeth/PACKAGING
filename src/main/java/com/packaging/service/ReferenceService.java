package com.packaging.service;

import com.packaging.model.ViewModels.Employee;
import com.packaging.model.ViewModels.LookupOption;
import com.packaging.model.ViewModels.Team;
import com.packaging.repository.ReferenceRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReferenceService {
    private final ReferenceRepository repository;

    public ReferenceService(ReferenceRepository repository) {
        this.repository = repository;
    }

    public List<Team> teams() {
        return repository.teams();
    }

    @Transactional
    public void renameTeam(long id, String name) {
        String teamName = required(name, "Team name");
        if (teamName.length() > 80 || repository.teams().stream().noneMatch(team -> team.id() == id)) {
            throw new IllegalArgumentException("Choose one of the four teams and enter a name of 80 characters or fewer.");
        }
        try {
            repository.renameTeam(id, teamName);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("That team name is already in use.");
        }
    }

    public List<Employee> employees(Long teamId, boolean activeOnly) {
        return repository.employees(teamId, activeOnly);
    }

    @Transactional
    public void saveEmployee(Long id, String employeeNumber, String name, Long teamId) {
        String number = required(employeeNumber, "Employee number").toUpperCase();
        String employeeName = required(name, "Employee name");
        if (number.length() > 40 || employeeName.length() > 150) {
            throw new IllegalArgumentException("Employee number or name exceeds the allowed length.");
        }
        if (teamId == null || repository.teams().stream().noneMatch(team -> team.id() == teamId)) {
            throw new IllegalArgumentException("Select one of the four configured teams.");
        }
        try {
            repository.saveEmployee(id, number, employeeName, teamId);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("That employee number is already configured.");
        }
    }

    @Transactional
    public void setEmployeeActive(long id, boolean active) {
        repository.setEmployeeActive(id, active);
    }

    public List<LookupOption> parts(boolean includeInactive) {
        return repository.parts(includeInactive);
    }

    public List<LookupOption> marks(boolean includeInactive) {
        return repository.marks(includeInactive);
    }

    public List<LookupOption> shifts(boolean includeInactive) {
        return repository.shifts(includeInactive);
    }

    @Transactional
    public void addPart(String partNumber, String partName) {
        String number = required(partNumber, "Part number").toUpperCase();
        String name = partName == null ? "" : partName.trim();
        if (number.length() > 80 || name.length() > 150) {
            throw new IllegalArgumentException("Part number or name exceeds the allowed length.");
        }
        try {
            repository.addPart(number, name.isEmpty() ? null : name);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("That part number is already configured.");
        }
    }

    @Transactional
    public void addMark(String markName) {
        String name = required(markName, "Mark");
        if (name.length() > 80) {
            throw new IllegalArgumentException("Mark must be 80 characters or fewer.");
        }
        try {
            repository.addMark(name);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("That mark is already configured.");
        }
    }

    @Transactional
    public void addShift(String shiftName) {
        String name = required(shiftName, "Shift");
        if (name.length() > 80) {
            throw new IllegalArgumentException("Shift must be 80 characters or fewer.");
        }
        try {
            repository.addShift(name);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("That shift is already configured.");
        }
    }

    @Transactional
    public void setLookupActive(String type, long id, boolean active) {
        repository.setLookupActive(type, id, active);
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
