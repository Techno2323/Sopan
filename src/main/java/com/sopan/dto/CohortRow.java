package com.sopan.dto;

import com.sopan.model.enums.MasteryLevel;

import java.util.Map;

public class CohortRow {

    private final int studentId;
    private final String studentName;
    private final String rollNo;
    private final Map<Integer, Double> conceptMastery;
    private final Map<Integer, MasteryLevel> conceptLevels;
    private final boolean stalled;
    private final int stalledConceptsCount;

    public CohortRow(int studentId, String studentName, String rollNo,
                     Map<Integer, Double> conceptMastery,
                     Map<Integer, MasteryLevel> conceptLevels,
                     boolean stalled, int stalledConceptsCount) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.conceptMastery = conceptMastery;
        this.conceptLevels = conceptLevels;
        this.stalled = stalled;
        this.stalledConceptsCount = stalledConceptsCount;
    }

    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getRollNo() { return rollNo; }
    public Map<Integer, Double> getConceptMastery() { return conceptMastery; }
    public Map<Integer, MasteryLevel> getConceptLevels() { return conceptLevels; }
    public boolean isStalled() { return stalled; }
    public int getStalledConceptsCount() { return stalledConceptsCount; }

    public MasteryLevel getLevel(int conceptId) {
        return conceptLevels.getOrDefault(conceptId, MasteryLevel.UNSEEN);
    }

    public Double getMastery(int conceptId) {
        return conceptMastery.get(conceptId);
    }
}
