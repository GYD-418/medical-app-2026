package com.example.yiliaoapp.network.dto;

public class SyncRequest {
    public String taskType;
    public String payload;
    public long timestamp;

    public SyncRequest(String taskType, String payload, long timestamp) {
        this.taskType = taskType;
        this.payload = payload;
        this.timestamp = timestamp;
    }
}
