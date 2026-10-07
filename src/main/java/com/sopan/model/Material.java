package com.sopan.model;

import com.sopan.model.enums.MaterialType;

import java.time.LocalDateTime;

public class Material {

    private int materialId;
    private int conceptId;
    private String title;
    private MaterialType type;
    private String location;
    private LocalDateTime createdAt;

    public int getMaterialId()              { return materialId; }
    public void setMaterialId(int id)       { this.materialId = id; }

    public int getConceptId()               { return conceptId; }
    public void setConceptId(int id)        { this.conceptId = id; }

    public String getTitle()                { return title; }
    public void setTitle(String t)          { this.title = t; }

    public MaterialType getType()           { return type; }
    public void setType(MaterialType t)     { this.type = t; }

    public String getLocation()             { return location; }
    public void setLocation(String l)       { this.location = l; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime t){ this.createdAt = t; }
}
