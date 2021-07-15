package com.aiitec.openapi.db.entity;

public class IndexEntity {

    private String columnName;
    private String orderBy;

    public IndexEntity(){}
    public IndexEntity(String columnName, String orderBy){
        this.columnName = columnName;
        this.orderBy = orderBy;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
