package com.sopan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MasterySnapshot {

    private long snapshotId;
    private int studentId;
    private int conceptId;
    private BigDecimal mastery;
    private int evidenceCount;
    private LocalDateTime computedAt;

    public long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(long snapshotId) { this.snapshotId = snapshotId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getConceptId() { return conceptId; }
    public void setConceptId(int conceptId) { this.conceptId = conceptId; }

    public BigDecimal getMastery() { return mastery; }
    public void setMastery(BigDecimal mastery) { this.mastery = mastery; }

    public int getEvidenceCount() { return evidenceCount; }
    public void setEvidenceCount(int evidenceCount) { this.evidenceCount = evidenceCount; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
