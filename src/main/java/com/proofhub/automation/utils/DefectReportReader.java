package com.proofhub.automation.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the "Defect Report" xlsx (uploaded as a Jenkins file parameter) into {@link DefectRow}
 * records, one per defect. Columns are located by header name — the sheet's title row and any
 * blank rows above the header ("Defect ID", "Defect Description", ...) are skipped automatically,
 * so column order/spacing in the workbook doesn't matter as long as the header names match.
 */
public final class DefectReportReader {

    private DefectReportReader() {
    }

    /** Reads every defect row in the sheet. */
    public static List<DefectRow> read(Path xlsxPath) {
        return read(xlsxPath, null, null);
    }

    /**
     * Reads defect rows restricted to an inclusive Excel row range (1-indexed, exactly as row
     * numbers appear in Excel's own row headers — e.g. {@code startRowExcel=3, endRowExcel=8}
     * reads only rows 3 through 8). Pass {@code null} for either bound to leave that end
     * unrestricted (start defaults to the row right after the header; end defaults to the
     * sheet's last row).
     */
    public static List<DefectRow> read(Path xlsxPath, Integer startRowExcel, Integer endRowExcel) {
        List<DefectRow> rows = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(xlsxPath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = findHeaderRow(sheet, xlsxPath);
            Map<String, Integer> columnIndex = mapColumns(headerRow);

            int firstDataRow = headerRow.getRowNum() + 1;
            int lastDataRow = sheet.getLastRowNum();

            // Convert the user-facing, 1-indexed Excel row numbers to POI's 0-indexed rows.
            int start = startRowExcel != null ? startRowExcel - 1 : firstDataRow;
            int end = endRowExcel != null ? endRowExcel - 1 : lastDataRow;

            if (start < firstDataRow) {
                throw new IllegalStateException(String.format(
                        "[DefectReportReader] startRow %d is at or above the header row (Excel row %d) in: %s",
                        startRowExcel, headerRow.getRowNum() + 1, xlsxPath));
            }
            if (end < start) {
                throw new IllegalStateException(String.format(
                        "[DefectReportReader] endRow %d is before startRow %d in: %s",
                        endRowExcel, startRowExcel, xlsxPath));
            }

            for (int i = start; i <= end && i <= lastDataRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String defectId = cellValue(row, columnIndex.get("Defect ID"));
                String description = cellValue(row, columnIndex.get("Defect Description"));
                if (defectId.isBlank() && description.isBlank()) {
                    continue; // skip blank spacer rows
                }

                rows.add(new DefectRow(
                        defectId,
                        description,
                        cellValue(row, columnIndex.get("Module/Page")),
                        cellValue(row, columnIndex.get("Preconditions")),
                        cellValue(row, columnIndex.get("Test Data")),
                        cellValue(row, columnIndex.get("Step to Reproduce")),
                        cellValue(row, columnIndex.get("Expected Result")),
                        cellValue(row, columnIndex.get("Actual Result")),
                        cellValue(row, columnIndex.get("Severity")),
                        cellValue(row, columnIndex.get("Priority")),
                        cellValue(row, columnIndex.get("Reported By"))
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "[DefectReportReader] Failed to read defect report: " + xlsxPath, e);
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                    "[DefectReportReader] No defect rows found in: " + xlsxPath);
        }
        return rows;
    }

    private static Row findHeaderRow(Sheet sheet, Path xlsxPath) {
        for (Row row : sheet) {
            if ("Defect ID".equalsIgnoreCase(cellValue(row, 0))) {
                return row;
            }
        }
        throw new IllegalStateException(String.format(
                "[DefectReportReader] Could not find a header row starting with \"Defect ID\" in: %s",
                xlsxPath));
    }

    private static Map<String, Integer> mapColumns(Row headerRow) {
        Map<String, Integer> columns = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim();
            if (!header.isEmpty()) {
                columns.put(header, cell.getColumnIndex());
            }
        }
        return columns;
    }

    private static String cellValue(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
