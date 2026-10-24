package com.packaging.service;

import com.packaging.model.ViewModels.ProductionGroup;
import com.packaging.model.ViewModels.ProductionRecord;
import com.packaging.model.ViewModels.ReportData;
import com.packaging.model.ViewModels.SummaryData;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public byte[] summary(SummaryData summary, LocalDate date) {
        StringBuilder rows = new StringBuilder();
        row(rows, string("Packaging Management System - Summary"));
        row(rows, string("Date"), string(date == null ? "All dates" : DATE_FORMAT.format(date)));
        row(rows, string("Attendance Summary"));
        row(rows, string("Total Manpower"), number(summary.attendance().manpower()));
        row(rows, string("Present"), number(summary.attendance().present()));
        row(rows, string("Absent"), number(summary.attendance().absent()));
        row(rows, string("Attendance Percentage"), number(summary.attendance().percentage()));
        row(rows, string("Total Manpower Availability"), number(summary.manpowerAvailability()));
        row(rows, string("Productivity (production / available manpower)"), number(summary.productivity()));
        row(rows, string("Market-wise Production"));
        row(rows, string("Market"), string("Quantity"));
        for (ProductionGroup market : summary.production().byMark()) {
            row(rows, string(market.name()), number(market.quantity()));
        }
        row(rows, string("Grand Total Production"), number(summary.production().total()));
        return workbook("Summary", rows.toString());
    }

    public byte[] report(ReportData report) {
        StringBuilder rows = new StringBuilder();
        row(rows, string("Packaging Management System - Production Report"));
        row(rows, string("From date"), string(report.fromDate() == null ? "All dates" : DATE_FORMAT.format(report.fromDate())));
        row(rows, string("To date"), string(report.toDate() == null ? "All dates" : DATE_FORMAT.format(report.toDate())));
        row(rows, string("Generated"), string(report.generatedAt().toString()));
        row(rows, string("Name"), string("Part Number"), string("Market"), string("Quantity"));
        for (ProductionRecord record : report.production()) {
            row(rows, string(record.operatorName()), string(record.partNumber()), string(record.markName()),
                    number(record.quantity()));
        }
        row(rows, string("Grand Total Production"), string(""), string(""), number(report.productionTotal()));
        return workbook("Production Report", rows.toString());
    }

    private byte[] workbook(String sheetName, String rows) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<?mso-application progid=\"Excel.Sheet\"?>" +
                "<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" " +
                "xmlns:o=\"urn:schemas-microsoft-com:office:office\" " +
                "xmlns:x=\"urn:schemas-microsoft-com:office:excel\" " +
                "xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">" +
                "<Worksheet ss:Name=\"" + escape(sheetName) + "\"><Table>" + rows +
                "</Table></Worksheet></Workbook>";
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private void row(StringBuilder output, Cell... cells) {
        output.append("<Row>");
        for (Cell cell : cells) {
            output.append("<Cell><Data ss:Type=\"").append(cell.type()).append("\">")
                    .append(escape(cell.value())).append("</Data></Cell>");
        }
        output.append("</Row>");
    }

    private Cell string(String value) {
        return new Cell("String", value == null ? "" : value);
    }

    private Cell number(Number value) {
        return new Cell("Number", value == null ? "0" : value.toString());
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private record Cell(String type, String value) {
    }
}
