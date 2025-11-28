package org.bn.sensation.core.milestoneresult.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bn.sensation.core.contestant.service.dto.ContestParticipantDto;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneRoundResultDto;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneResultReportService {
    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public byte[] generateMilestoneResultReport(Map<Integer, List<MilestoneResultDto>> milestoneResultsByOrder) {
        Preconditions.checkArgument(milestoneResultsByOrder != null, "Данные результатов этапов не могут быть null");
        log.info("Генерация Excel-отчета для результатов этапов");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Milestone Results");
            int rowIndex = 0;

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle valueStyle = createValueStyle(workbook);

            // Title
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Отчет по результатам этапов");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // Сортируем этапы по порядку (от финала к отборочным)
            List<Map.Entry<Integer, List<MilestoneResultDto>>> sortedMilestones = milestoneResultsByOrder.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey, Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            for (Map.Entry<Integer, List<MilestoneResultDto>> milestoneEntry : sortedMilestones) {
                Integer milestoneOrder = milestoneEntry.getKey();
                List<MilestoneResultDto> results = milestoneEntry.getValue();

                if (results.isEmpty()) {
                    continue;
                }

                // Получаем название этапа из первого результата
                String milestoneName = results.get(0).getMilestone() != null
                        ? results.get(0).getMilestone().getValue()
                        : "Этап " + milestoneOrder;

                // Section header
                Row sectionHeader = sheet.createRow(rowIndex++);
                Cell sectionCell = sectionHeader.createCell(0);
                sectionCell.setCellValue("Результаты этапа: " + milestoneName);
                sectionCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(sectionHeader.getRowNum(), sectionHeader.getRowNum(), 0, 10));

                // Table headers
                Row headerRow = sheet.createRow(rowIndex++);
                createCell(headerRow, 0, "#", headerStyle);
                createCell(headerRow, 1, "ID конкурсанта", headerStyle);
                createCell(headerRow, 2, "Номер конкурсанта", headerStyle);
                createCell(headerRow, 3, "Участники", headerStyle);
                createCell(headerRow, 4, "ID этапа", headerStyle);
                createCell(headerRow, 5, "Название этапа", headerStyle);
                createCell(headerRow, 6, "Финально утвержден", headerStyle);
                createCell(headerRow, 7, "Прошел по оценкам", headerStyle);
                createCell(headerRow, 8, "Количество раундов", headerStyle);
                createCell(headerRow, 9, "Результаты раундов", headerStyle);
                createCell(headerRow, 10, "ID результата", headerStyle);

                // Sort results by contestant number
                List<MilestoneResultDto> sortedResults = results.stream()
                        .sorted(Comparator.comparing(
                                result -> result.getContestant() != null
                                        ? defaultString(result.getContestant().getNumber())
                                        : "",
                                Comparator.nullsLast(String::compareTo)))
                        .collect(Collectors.toList());

                int counter = 1;
                for (MilestoneResultDto result : sortedResults) {
                    Row dataRow = sheet.createRow(rowIndex++);
                    int cellIndex = 0;

                    createCell(dataRow, cellIndex++, counter++, valueStyle);

                    // Contestant info
                    ContestantDto contestant = result.getContestant();
                    if (contestant != null) {
                        createCell(dataRow, cellIndex++, contestant.getId() != null ? String.valueOf(contestant.getId()) : "", valueStyle);
                        createCell(dataRow, cellIndex++, defaultString(contestant.getNumber()), valueStyle);

                        // Participants: только фамилии и имена
                        String participantsInfo = "";
                        if (contestant.getParticipants() != null && !contestant.getParticipants().isEmpty()) {
                            participantsInfo = contestant.getParticipants().stream()
                                    .sorted(Comparator.comparing(
                                            ContestParticipantDto::getPartnerSide,
                                            Comparator.nullsLast(Enum::compareTo))
                                            .thenComparing(ContestParticipantDto::getNumber,
                                                    Comparator.nullsLast(String::compareTo)))
                                    .map(p -> {
                                        String name = formatParticipantName(p.getName(), p.getSurname());
                                        return name;
                                    })
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.joining("; "));
                        }
                        createCell(dataRow, cellIndex++, participantsInfo.isEmpty() ? "—" : participantsInfo, valueStyle);
                    } else {
                        createCell(dataRow, cellIndex++, "", valueStyle);
                        createCell(dataRow, cellIndex++, "", valueStyle);
                        createCell(dataRow, cellIndex++, "—", valueStyle);
                    }

                    // Milestone info
                    if (result.getMilestone() != null) {
                        createCell(dataRow, cellIndex++, result.getMilestone().getId() != null ? String.valueOf(result.getMilestone().getId()) : "", valueStyle);
                        createCell(dataRow, cellIndex++, defaultString(result.getMilestone().getValue()), valueStyle);
                    } else {
                        createCell(dataRow, cellIndex++, "", valueStyle);
                        createCell(dataRow, cellIndex++, "", valueStyle);
                    }

                    // Finally approved
                    createCell(dataRow, cellIndex++, Boolean.TRUE.equals(result.getFinallyApproved()) ? "Да" : "Нет", valueStyle);

                    // Judge passed
                    createCell(dataRow, cellIndex++, result.getJudgePassed() != null ? result.getJudgePassed().name() : "—", valueStyle);

                    // Round results count
                    int roundResultsCount = result.getMilestoneRoundResults() != null ? result.getMilestoneRoundResults().size() : 0;
                    createCell(dataRow, cellIndex++, roundResultsCount, valueStyle);

                    // Round results details
                    String roundResultsInfo = "";
                    if (result.getMilestoneRoundResults() != null && !result.getMilestoneRoundResults().isEmpty()) {
                        roundResultsInfo = result.getMilestoneRoundResults().stream()
                                .sorted(Comparator.comparing(
                                        MilestoneRoundResultDto::getRoundOrder,
                                        Comparator.nullsLast(Integer::compareTo)))
                                .map(roundResult -> {
                                    StringBuilder sb = new StringBuilder();
                                    if (roundResult.getRound() != null) {
                                        sb.append("Раунд: ").append(defaultString(roundResult.getRound().getValue()));
                                    }
                                    if (roundResult.getRoundOrder() != null) {
                                        sb.append(" (порядок: ").append(roundResult.getRoundOrder()).append(")");
                                    }
                                    if (roundResult.getTotalScore() != null) {
                                        sb.append(", баллы: ").append(roundResult.getTotalScore());
                                    }
                                    if (roundResult.getJudgePassed() != null) {
                                        sb.append(", прошел: ").append(roundResult.getJudgePassed().name());
                                    }
                                    return sb.toString();
                                })
                                .collect(Collectors.joining("; "));
                    }
                    createCell(dataRow, cellIndex++, roundResultsInfo.isEmpty() ? "—" : roundResultsInfo, valueStyle);

                    // Result ID
                    createCell(dataRow, cellIndex++, result.getId() != null ? String.valueOf(result.getId()) : "", valueStyle);
                }

                rowIndex++; // Empty row between milestones
            }

            autoSizeColumns(sheet);

            workbook.write(outputStream);
            log.info("Excel-отчет по результатам этапов успешно сформирован");
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Не удалось сформировать Excel-отчет для результатов этапов", ex);
            throw new RuntimeException("Не удалось сформировать Excel-отчет", ex);
        }
    }

    public String getContentType() {
        return EXCEL_CONTENT_TYPE;
    }

    private String formatParticipantName(String name, String surname) {
        String fullName = (defaultString(surname) + " " + defaultString(name)).trim();
        if (fullName.isEmpty()) {
            fullName = (defaultString(name) + " " + defaultString(surname)).trim();
        }
        return fullName.isEmpty() ? "—" : fullName;
    }

    private void autoSizeColumns(Sheet sheet) {
        int maxColumn = 0;
        for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row != null && row.getLastCellNum() > maxColumn) {
                maxColumn = row.getLastCellNum();
            }
        }
        for (int colIdx = 0; colIdx < maxColumn; colIdx++) {
            sheet.autoSizeColumn(colIdx);
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setThinBorders(style);
        return style;
    }

    private CellStyle createValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorders(style);
        style.setWrapText(true);
        return style;
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}

