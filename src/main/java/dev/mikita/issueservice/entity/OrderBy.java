package dev.mikita.issueservice.entity;

public enum OrderBy {
    CREATION_DATE("creationDate"),
    STATUS("status"),
    TITLE("title"),
    CATEGORY("category"),
    LIKES("likes");

    private final String fieldName;

    OrderBy(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
