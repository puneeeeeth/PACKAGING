package com.packaging.controller;

import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.service.ProductionService;
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
public class ProductionController {
    private final ProductionService productionService;
    private final ReferenceService referenceService;

    public ProductionController(ProductionService productionService, ReferenceService referenceService) {
        this.productionService = productionService;
        this.referenceService = referenceService;
    }

    @GetMapping("/production")
    public String production(@RequestParam(required = false) LocalDate productionDate,
                             @RequestParam(required = false) LocalDate fromDate,
                             @RequestParam(required = false) LocalDate toDate,
                             @RequestParam(required = false) Long partId,
                             @RequestParam(required = false) Long markId,
                             @RequestParam(required = false) Long shiftId,
                             @RequestParam(required = false) String operatorName,
                             @RequestParam(required = false) Long editId,
                             Model model) {
        List<ProductionRecord> rows = List.of();
        String error = null;
        try {
            rows = productionService.records(productionDate, fromDate, toDate, partId, markId, shiftId, operatorName);
        } catch (IllegalArgumentException ex) {
            error = ex.getMessage();
        }
        ProductionRecord editRecord = editId == null ? null : productionService.records(null, null, null,
                null, null, null).stream().filter(record -> record.id() == editId).findFirst().orElse(null);
        model.addAttribute("productionDate", productionDate == null ? LocalDate.now() : productionDate);
        model.addAttribute("filterDate", productionDate);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("partIdFilter", partId);
        model.addAttribute("markIdFilter", markId);
        model.addAttribute("shiftIdFilter", shiftId);
        model.addAttribute("operatorNameFilter", operatorName);
        model.addAttribute("productionRecords", rows);
        model.addAttribute("productionTotal", rows.stream().mapToLong(ProductionRecord::quantity).sum());
        model.addAttribute("editRecord", editRecord);
        model.addAttribute("parts", referenceService.parts(true));
        model.addAttribute("marks", referenceService.marks(true));
        model.addAttribute("operators", referenceService.employees(null, true));
        model.addAttribute("shifts", referenceService.shifts(true));
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "production";
    }

    @PostMapping("/production/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam(required = false) LocalDate date,
                       @RequestParam(required = false) String operatorName,
                       @RequestParam(required = false) Long partId,
                       @RequestParam(required = false) Long markId,
                       @RequestParam(required = false) Long shiftId,
                       @RequestParam(required = false) String quantity,
                       @RequestParam(required = false) String remarks,
                       RedirectAttributes redirect) {
        try {
            Integer parsedQuantity;
            try {
                parsedQuantity = quantity == null || quantity.isBlank() ? null : Integer.valueOf(quantity.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Production quantity must be a non-negative whole number.");
            }
            productionService.save(id, date, operatorName, partId, markId, shiftId, parsedQuantity, remarks);
            redirect.addFlashAttribute("success", id == null ? "Production entry saved." : "Production entry updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/production";
    }

    @PostMapping("/production/{id}/delete")
    public String delete(@PathVariable long id, RedirectAttributes redirect) {
        try {
            productionService.delete(id);
            redirect.addFlashAttribute("success", "Production entry deleted.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/production";
    }

    @PostMapping("/production/config/part")
    public String addPart(@RequestParam(required = false) String partNumber,
                          @RequestParam(required = false) String partName,
                          RedirectAttributes redirect) {
        runConfig(() -> referenceService.addPart(partNumber, partName), redirect);
        return "redirect:/production#configuration";
    }

    @PostMapping("/production/config/mark")
    public String addMark(@RequestParam(required = false) String markName,
                          RedirectAttributes redirect) {
        runConfig(() -> referenceService.addMark(markName), redirect);
        return "redirect:/production#configuration";
    }

    @PostMapping("/production/config/shift")
    public String addShift(@RequestParam(required = false) String shiftName,
                           RedirectAttributes redirect) {
        runConfig(() -> referenceService.addShift(shiftName), redirect);
        return "redirect:/production#configuration";
    }

    @PostMapping("/production/config/{type}/{id}/active")
    public String setLookupActive(@PathVariable String type, @PathVariable long id,
                                  @RequestParam boolean active, RedirectAttributes redirect) {
        runConfig(() -> referenceService.setLookupActive(type, id, active), redirect);
        return "redirect:/production#configuration";
    }

    private void runConfig(Runnable action, RedirectAttributes redirect) {
        try {
            action.run();
            redirect.addFlashAttribute("success", "Configuration saved.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
    }
}
