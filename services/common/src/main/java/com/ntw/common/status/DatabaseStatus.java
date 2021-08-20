package com.ntw.common.status;

import com.google.gson.Gson;

public class DatabaseStatus {
    private String database;
    private String databaseTime;

    public DatabaseStatus() {
    }

    public DatabaseStatus(String database) {
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabaseTime() {
        return databaseTime;
    }

    public void setDatabaseTime(String databaseTime) {
        this.databaseTime = databaseTime;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
