package org.bn.sensation.core.activity.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneRoundResultEntity;
import org.bn.sensation.core.milestoneresult.repository.MilestoneResultRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityReportService {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ActivityRepository activityRepository;
    private final MilestoneResultRepository milestoneResultRepository;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;

    @Transactional(readOnly = true)
    public byte[] generateActivityReport(Long activityId) {
        Preconditions.checkArgument(activityId != null, "ID активности не может быть null");
        ActivityEntity activity = activityRepository.getByIdWithActivityUserOrThrow(activityId);
        log.info("Генерация Excel-отчета для активности: id={}, название={}", activityId, activity.getName());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Activity");
            int rowIndex = 0;

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle labelStyle = createLabelStyle(workbook);
            CellStyle valueStyle = createValueStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Title
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Отчет по активности " + activity.getName());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Summary section header
            Row summaryHeaderRow = sheet.createRow(rowIndex++);
            Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
            summaryHeaderCell.setCellValue("Основная информация");
            summaryHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 3));

            rowIndex = createSummaryRow(sheet, activity, rowIndex, labelStyle, valueStyle, dateStyle);

            // Judges / Activity users section
            if (!activity.getActivityUsers().isEmpty()) {
                Row acUsersHeaderRow = sheet.createRow(rowIndex++);
                Cell acUsersHeaderCell = acUsersHeaderRow.createCell(0);
                acUsersHeaderCell.setCellValue("Команда");
                acUsersHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 3));

                Row headings = sheet.createRow(rowIndex++);
                createCell(headings, 0, "#", headerStyle);
                createCell(headings, 1, "ФИО", headerStyle);
                createCell(headings, 2, "Роль", headerStyle);

                int counter = 1;
                for (ActivityUserEntity activityUser : activity.getActivityUsers().stream()
                        .sorted(Comparator
                                .comparing((ActivityUserEntity au) -> partnerSideOrder(au.getPartnerSide()))
                                .thenComparing(au -> Optional.ofNullable(au.getUser())
                                        .map(user -> buildSortableName(user.getPerson()))
                                        .orElse("")))
                        .toList()) {
                    Row dataRow = sheet.createRow(rowIndex++);
                    createCell(dataRow, 0, counter++, valueStyle);
                    Person person = activityUser.getUser() != null ? activityUser.getUser().getPerson() : null;
                    createCell(dataRow, 1, formatPersonName(person), valueStyle);
                    createCell(dataRow, 2, mapPartnerSide(activityUser.getPartnerSide()), valueStyle);
                }
                rowIndex++;
            }

            rowIndex = createParticipantsSection(sheet, activity, rowIndex, headerStyle, valueStyle);
            rowIndex = createMilestoneResultsSection(sheet, activity, rowIndex, headerStyle, valueStyle);
            rowIndex = createActivityResultsSection(sheet, activity, rowIndex, headerStyle, valueStyle);

            autoSizeColumns(sheet);

            workbook.write(outputStream);
            log.info("Excel-отчет по активности id={} успешно сформирован", activityId);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Не удалось сформировать Excel-отчет для активности id={}", activityId, ex);
            throw new RuntimeException("Не удалось сформировать Excel-отчет", ex);
        }
    }

    public String getContentType() {
        return EXCEL_CONTENT_TYPE;
    }

    private int createSummaryRow(
            Sheet sheet,
            ActivityEntity activity,
            int startRow,
            CellStyle labelStyle,
            CellStyle valueStyle,
            CellStyle dateStyle) {
        int rowIndex = startRow;

        rowIndex = createKeyValueRow(sheet, rowIndex, "Название", activity.getName(), labelStyle, valueStyle);
        rowIndex = createKeyValueRow(sheet, rowIndex, "Описание", activity.getDescription(), labelStyle, valueStyle);

        if (activity.getStartDateTime() != null) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, "Дата начала", labelStyle);
            Cell cell = row.createCell(1);
            cell.setCellValue(toDate(activity.getStartDateTime()));
            cell.setCellStyle(dateStyle);
        }

        if (activity.getEndDateTime() != null) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, "Дата завершения", labelStyle);
            Cell cell = row.createCell(1);
            cell.setCellValue(toDate(activity.getEndDateTime()));
            cell.setCellStyle(dateStyle);
        }

        rowIndex = createKeyValueRow(sheet, rowIndex, "Количество участников",
                activity.getParticipants().size(), labelStyle, valueStyle);
        rowIndex = createKeyValueRow(sheet, rowIndex, "Количество этапов",
                activity.getMilestones().size(), labelStyle, valueStyle);

        return rowIndex + 1; // leave an empty row before next section
    }

    private int createKeyValueRow(
            Sheet sheet,
            int rowIndex,
            String label,
            Object value,
            CellStyle labelStyle,
            CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex++);
        createCell(row, 0, label, labelStyle);
        if (value instanceof Number number) {
            Cell cell = row.createCell(1);
            cell.setCellValue(number.doubleValue());
            cell.setCellStyle(valueStyle);
        } else {
            createCell(row, 1, value != null ? value.toString() : "—", valueStyle);
        }
        return rowIndex;
    }

    private int createParticipantsSection(
            Sheet sheet,
            ActivityEntity activity,
            int startRow,
            CellStyle headerStyle,
            CellStyle valueStyle) {
        if (activity.getParticipants().isEmpty()) {
            return startRow;
        }

        int rowIndex = startRow;
        Row sectionHeader = sheet.createRow(rowIndex++);
        Cell sectionCell = sectionHeader.createCell(0);
        sectionCell.setCellValue("Участники активности");
        sectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(sectionHeader.getRowNum(), sectionHeader.getRowNum(), 0, 3));

        for (PartnerSide side : sortedPartnerSides()) {
            List<ParticipantEntity> participants = activity.getParticipants().stream()
                    .filter(participant -> participant.getPartnerSide() == side)
                    .sorted(Comparator.comparing(ParticipantEntity::getNumber, Comparator.nullsLast(String::compareTo)))
                    .toList();

            if (participants.isEmpty()) {
                continue;
            }

            Row sideRow = sheet.createRow(rowIndex++);
            Cell sideCell = sideRow.createCell(0);
            sideCell.setCellValue("Сторона: " + side.name());
            sideCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(sideRow.getRowNum(), sideRow.getRowNum(), 0, 3));

            Row tableHeader = sheet.createRow(rowIndex++);
            createCell(tableHeader, 0, "#", headerStyle);
            createCell(tableHeader, 1, "Имя Фамилия", headerStyle);
            createCell(tableHeader, 2, "Номер", headerStyle);
            createCell(tableHeader, 3, "Зарегистрирован", headerStyle);

            int counter = 1;
            for (ParticipantEntity participant : participants) {
                Row dataRow = sheet.createRow(rowIndex++);
                createCell(dataRow, 0, counter++, valueStyle);
                createCell(dataRow, 1, buildParticipantFullName(participant), valueStyle);
                createCell(dataRow, 2, defaultString(participant.getNumber()), valueStyle);
                createCell(dataRow, 3, Boolean.TRUE.equals(participant.getIsRegistered()) ? "Да" : "Нет", valueStyle);
            }

            rowIndex++;
        }

        List<ParticipantEntity> withoutSide = activity.getParticipants().stream()
                .filter(participant -> participant.getPartnerSide() == null)
                .sorted(Comparator.comparing(ParticipantEntity::getNumber, Comparator.nullsLast(String::compareTo)))
                .toList();

        if (!withoutSide.isEmpty()) {
            Row sideRow = sheet.createRow(rowIndex++);
            Cell sideCell = sideRow.createCell(0);
            sideCell.setCellValue("Сторона: —");
            sideCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(sideRow.getRowNum(), sideRow.getRowNum(), 0, 3));

            Row tableHeader = sheet.createRow(rowIndex++);
            createCell(tableHeader, 0, "#", headerStyle);
            createCell(tableHeader, 1, "Имя Фамилия", headerStyle);
            createCell(tableHeader, 2, "Номер", headerStyle);
            createCell(tableHeader, 3, "Зарегистрирован", headerStyle);

            int counter = 1;
            for (ParticipantEntity participant : withoutSide) {
                Row dataRow = sheet.createRow(rowIndex++);
                createCell(dataRow, 0, counter++, valueStyle);
                createCell(dataRow, 1, buildParticipantFullName(participant), valueStyle);
                createCell(dataRow, 2, defaultString(participant.getNumber()), valueStyle);
                createCell(dataRow, 3, Boolean.TRUE.equals(participant.getIsRegistered()) ? "Да" : "Нет", valueStyle);
            }

            rowIndex++;
        }

        return rowIndex;
    }

    private int createMilestoneResultsSection(
            Sheet sheet,
            ActivityEntity activity,
            int startRow,
            CellStyle headerStyle,
            CellStyle valueStyle) {
        if (activity.getMilestones().isEmpty()) {
            return startRow;
        }

        int rowIndex = startRow;
        List<MilestoneEntity> milestones = activity.getMilestones().stream()
                .sorted(Comparator.comparing(
                        (MilestoneEntity milestone) -> Optional.ofNullable(milestone.getMilestoneOrder()).orElse(Integer.MIN_VALUE),
                        Comparator.reverseOrder()))
                .toList();

        for (MilestoneEntity milestone : milestones) {
            Row sectionHeader = sheet.createRow(rowIndex++);
            Cell sectionCell = sectionHeader.createCell(0);
            sectionCell.setCellValue("Результаты этапа: " + defaultString(milestone.getName()));
            sectionCell.setCellStyle(headerStyle);

            List<MilestoneResultEntity> milestoneResults = milestoneResultRepository.findAllByMilestoneId(milestone.getId());
            List<JudgeMilestoneResultEntity> judgeResults = judgeMilestoneResultRepository.findByMilestoneId(milestone.getId());
            List<JudgeColumn> judgeColumns = buildJudgeColumns(judgeResults);

            int judgeColumnsCount = judgeColumns.stream()
                    .mapToInt(column -> Math.max(1, column.criteria().size()))
                    .sum();
            int headerEndColumn = Math.max(3, 3 + judgeColumnsCount);
            sheet.addMergedRegion(new CellRangeAddress(sectionHeader.getRowNum(), sectionHeader.getRowNum(), 0, headerEndColumn));

            if (milestoneResults.isEmpty()) {
                Row emptyRow = sheet.createRow(rowIndex++);
                createCell(emptyRow, 0, "Нет данных по результатам", valueStyle);
                rowIndex++;
                continue;
            }

            Map<Long, BigDecimal> totalsByParticipant = new HashMap<>();
            for (MilestoneResultEntity result : milestoneResults) {
                ParticipantEntity participant = result.getParticipant();
                if (participant == null) {
                    continue;
                }
                totalsByParticipant.put(participant.getId(), calculateTotalScore(result));
            }

            List<MilestoneResultEntity> sortedResults = milestoneResults.stream()
                    .filter(result -> result.getParticipant() != null)
                    .sorted(Comparator
                            .comparing((MilestoneResultEntity result) -> partnerSideOrder(result.getParticipant().getPartnerSide()))
                            .thenComparing(result -> totalsByParticipant.getOrDefault(result.getParticipant().getId(), BigDecimal.ZERO),
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(result -> buildSortableName(result.getParticipant().getPerson())))
                    .toList();

            Map<Long, Map<Long, Map<Long, Integer>>> participantScores = aggregateJudgeScores(judgeResults);

            for (PartnerSide side : sortedPartnerSides()) {
                List<MilestoneResultEntity> resultsForSide = sortedResults.stream()
                        .filter(result -> result.getParticipant() != null
                                && Objects.equals(result.getParticipant().getPartnerSide(), side))
                        .toList();

                if (resultsForSide.isEmpty()) {
                    continue;
                }

                Row sideHeaderRow = sheet.createRow(rowIndex++);
                Cell sideHeaderCell = sideHeaderRow.createCell(0);
                sideHeaderCell.setCellValue("Сторона: " + mapPartnerSide(side));
                sideHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(sideHeaderRow.getRowNum(), sideHeaderRow.getRowNum(), 0, 3));

                List<JudgeColumn> judgeColumnsForSide = judgeColumns.stream()
                        .filter(column -> isJudgeRelevantForSide(column.judge(), side))
                        .toList();
                if (judgeColumnsForSide.isEmpty()) {
                    continue;
                }

                Row judgeHeader = sheet.createRow(rowIndex++);
                Row criteriaHeader = sheet.createRow(rowIndex++);

                int colIndex = 0;
                colIndex = createMergedHeader(judgeHeader, criteriaHeader, colIndex, "#", headerStyle);
                colIndex = createMergedHeader(judgeHeader, criteriaHeader, colIndex, "Участник", headerStyle);
                colIndex = createMergedHeader(judgeHeader, criteriaHeader, colIndex, "Сумма баллов", headerStyle);
                colIndex = createMergedHeader(judgeHeader, criteriaHeader, colIndex, "Финально утвержден", headerStyle);

                for (JudgeColumn judgeColumn : judgeColumnsForSide) {
                    List<MilestoneCriterionEntity> criteria = judgeColumn.criteria();

                    int startCol = colIndex;
                    Cell judgeCell = judgeHeader.createCell(colIndex);
                    judgeCell.setCellValue(buildJudgeDisplayName(judgeColumn.judge()));
                    judgeCell.setCellStyle(headerStyle);

                    if (criteria.isEmpty()) {
                        Cell criterionCell = criteriaHeader.createCell(colIndex++);
                        criterionCell.setCellValue("—");
                        criterionCell.setCellStyle(headerStyle);
                    } else {
                        for (MilestoneCriterionEntity criterion : criteria) {
                            Cell criterionCell = criteriaHeader.createCell(colIndex++);
                            criterionCell.setCellValue(defaultString(criterion.getCriterion() != null
                                    ? criterion.getCriterion().getName()
                                    : null));
                            criterionCell.setCellStyle(headerStyle);
                        }
                    }

                    if (colIndex - startCol > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(
                                judgeHeader.getRowNum(),
                                judgeHeader.getRowNum(),
                                startCol,
                                colIndex - 1));
                    }
                }

                int participantIndex = 1;
                for (MilestoneResultEntity result : resultsForSide) {
                    ParticipantEntity participant = result.getParticipant();
                    if (participant == null) {
                        continue;
                    }

                    Row dataRow = sheet.createRow(rowIndex++);
                    int cellIndex = 0;
                    createCell(dataRow, cellIndex++, participantIndex++, valueStyle);
                    createCell(dataRow, cellIndex++, buildParticipantFullName(participant), valueStyle);

                    BigDecimal totalScore = totalsByParticipant.getOrDefault(participant.getId(), BigDecimal.ZERO);
                    createCell(dataRow, cellIndex++, totalScore, valueStyle);

                    createCell(dataRow, cellIndex++, Boolean.TRUE.equals(result.getFinallyApproved()) ? "Да" : "", valueStyle);

                    Map<Long, Map<Long, Integer>> judgeScoreMap = participantScores.getOrDefault(participant.getId(), Map.of());
                    for (JudgeColumn judgeColumn : judgeColumnsForSide) {
                        Map<Long, Integer> criterionScores = judgeScoreMap.getOrDefault(judgeColumn.judge().getId(), Map.of());
                        List<MilestoneCriterionEntity> criteria = judgeColumn.criteria();
                        if (criteria.isEmpty()) {
                            createCell(dataRow, cellIndex++, "", valueStyle);
                        } else {
                            for (MilestoneCriterionEntity criterion : criteria) {
                                Integer score = criterionScores.get(criterion.getId());
                                createCell(dataRow, cellIndex++, score, valueStyle);
                            }
                        }
                    }
                }

                rowIndex++;
            }
        }

        return rowIndex;
    }

    private int createActivityResultsSection(
            Sheet sheet,
            ActivityEntity activity,
            int startRow,
            CellStyle headerStyle,
            CellStyle valueStyle) {
        Comparator<ActivityResultEntity> activityResultComparator = Comparator
                .comparing((ActivityResultEntity result) -> partnerSideOrder(
                        result.getParticipant() != null ? result.getParticipant().getPartnerSide() : null))
                .thenComparing(ActivityResultEntity::getPlace, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(result -> buildSortableName(result.getParticipant() != null ? result.getParticipant().getPerson() : null));

        List<ActivityResultEntity> results = activity.getActivityResults().stream()
                .sorted(activityResultComparator)
                .toList();

        if (results.isEmpty()) {
            return startRow;
        }

        int rowIndex = startRow;
        Row headerRow = sheet.createRow(rowIndex++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Результаты активности");
        headerCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum(), headerRow.getRowNum(), 0, 3));

        Row tableHeader = sheet.createRow(rowIndex++);
        createCell(tableHeader, 0, "Место", headerStyle);
        createCell(tableHeader, 1, "Участник", headerStyle);
        createCell(tableHeader, 2, "Номер", headerStyle);
        createCell(tableHeader, 3, "Сторона", headerStyle);

        for (ActivityResultEntity result : results) {
            ParticipantEntity participant = result.getParticipant();
            Row dataRow = sheet.createRow(rowIndex++);
            createCell(dataRow, 0, result.getPlace() != null ? result.getPlace() : 0, valueStyle);
            createCell(dataRow, 1, participant != null ? buildParticipantFullName(participant) : "—", valueStyle);
            createCell(dataRow, 2, participant != null ? defaultString(participant.getNumber()) : "—", valueStyle);
            createCell(dataRow, 3, participant != null ? mapPartnerSide(participant.getPartnerSide()) : "—", valueStyle);
        }

        rowIndex++;
        return rowIndex;
    }

    private Map<Long, Map<Long, Map<Long, Integer>>> aggregateJudgeScores(List<JudgeMilestoneResultEntity> judgeResults) {
        Map<Long, Map<Long, Map<Long, Integer>>> participantScores = new HashMap<>();
        for (JudgeMilestoneResultEntity result : judgeResults) {
            if (result.getParticipant() == null || result.getActivityUser() == null || result.getMilestoneCriterion() == null) {
                continue;
            }

            Long participantId = result.getParticipant().getId();
            Long judgeId = result.getActivityUser().getId();
            Long criterionId = result.getMilestoneCriterion().getId();

            participantScores
                    .computeIfAbsent(participantId, id -> new HashMap<>())
                    .computeIfAbsent(judgeId, id -> new HashMap<>())
                    .merge(criterionId, result.getScore() != null ? result.getScore() : 0, Integer::sum);
        }
        return participantScores;
    }

    private List<JudgeColumn> buildJudgeColumns(List<JudgeMilestoneResultEntity> judgeResults) {
        Map<Long, ActivityUserEntity> judges = new LinkedHashMap<>();
        Map<Long, Map<Long, MilestoneCriterionEntity>> criteriaByJudge = new LinkedHashMap<>();

        for (JudgeMilestoneResultEntity result : judgeResults) {
            if (result.getActivityUser() == null || result.getMilestoneCriterion() == null) {
                continue;
            }

            Long judgeId = result.getActivityUser().getId();
            judges.putIfAbsent(judgeId, result.getActivityUser());

            criteriaByJudge.computeIfAbsent(judgeId, id -> new LinkedHashMap<>())
                    .putIfAbsent(result.getMilestoneCriterion().getId(), result.getMilestoneCriterion());
        }

        return judges.values().stream()
                .sorted(Comparator.comparing(this::buildJudgeDisplayName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(judge -> {
                    List<MilestoneCriterionEntity> criteria = criteriaByJudge.getOrDefault(judge.getId(), Map.of())
                            .values().stream()
                            .sorted(Comparator.comparing(
                                    criterion -> defaultString(criterion.getCriterion() != null ? criterion.getCriterion().getName() : null),
                                    Comparator.nullsLast(String::compareToIgnoreCase)))
                            .toList();
                    return new JudgeColumn(judge, criteria);
                })
                .toList();
    }

    private int createMergedHeader(Row topRow, Row bottomRow, int column, String value, CellStyle style) {
        Cell topCell = topRow.createCell(column);
        topCell.setCellValue(value);
        topCell.setCellStyle(style);

        Cell bottomCell = bottomRow.createCell(column);
        bottomCell.setCellStyle(style);

        topRow.getSheet().addMergedRegion(new CellRangeAddress(topRow.getRowNum(), bottomRow.getRowNum(), column, column));
        return column + 1;
    }

    private int partnerSideOrder(PartnerSide partnerSide) {
        if (partnerSide == null) {
            return Integer.MAX_VALUE;
        }
        return switch (partnerSide) {
            case LEADER -> 0;
            case FOLLOWER -> 1;
            default -> partnerSide.ordinal() + 10;
        };
    }
    private List<PartnerSide> sortedPartnerSides() {
        return Arrays.stream(PartnerSide.values())
                .sorted(Comparator.comparingInt(this::partnerSideOrder))
                .toList();
    }

    private String buildParticipantFullName(ParticipantEntity participant) {
        if (participant == null || participant.getPerson() == null) {
            return "—";
        }
        Person person = participant.getPerson();
        return formatPersonName(person);
    }

    private String buildJudgeDisplayName(ActivityUserEntity judge) {
        if (judge == null || judge.getUser() == null || judge.getUser().getPerson() == null) {
            return "Судья";
        }
        String name = formatPersonName(judge.getUser().getPerson());
        return name.equals("—") ? "Судья" : name;
    }
    private boolean isJudgeRelevantForSide(ActivityUserEntity judge, PartnerSide side) {
        if (judge == null) {
            return true;
        }
        PartnerSide judgeSide = judge.getPartnerSide();
        if (judgeSide == null) {
            return true;
        }
        return judgeSide == side;
    }

    private String formatPersonName(Person person) {
        if (person == null) {
            return "—";
        }
        String fullName = (defaultString(person.getSurname()) + " " + defaultString(person.getName())).trim();
        if (fullName.isEmpty()) {
            fullName = (defaultString(person.getName()) + " " + defaultString(person.getSurname())).trim();
        }
        return fullName.isEmpty() ? "—" : fullName;
    }

    private String buildSortableName(Person person) {
        if (person == null) {
            return "";
        }
        return (defaultString(person.getSurname()) + " " + defaultString(person.getName())).trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal calculateTotalScore(MilestoneResultEntity result) {
        if (result == null || result.getRoundResults() == null) {
            return BigDecimal.ZERO;
        }
        return result.getRoundResults().stream()
                .map(MilestoneRoundResultEntity::getTotalScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private static class JudgeColumn {
        private final ActivityUserEntity judge;
        private final List<MilestoneCriterionEntity> criteria;

        JudgeColumn(ActivityUserEntity judge, List<MilestoneCriterionEntity> criteria) {
            this.judge = judge;
            this.criteria = criteria != null ? new ArrayList<>(criteria) : new ArrayList<>();
        }

        ActivityUserEntity judge() {
            return judge;
        }

        List<MilestoneCriterionEntity> criteria() {
            return criteria;
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

    private void createCell(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(style);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
    }

    private void createCell(Row row, int column, Integer value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(style);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private String mapPartnerSide(PartnerSide partnerSide) {
        return partnerSide != null ? partnerSide.name() : "—";
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

    private CellStyle createLabelStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createValueStyle(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd.MM.yyyy HH:mm"));
        return style;
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private Date toDate(java.time.LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

