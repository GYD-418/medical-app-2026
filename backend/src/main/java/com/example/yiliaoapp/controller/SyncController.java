package com.example.yiliaoapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    @PostMapping("/task")
    public ResponseEntity<Map<String, Object>> uploadTask(@RequestBody SyncRequest request) {
        System.out.println("[SYNC] 收到任务: type=" + request.taskType + ", payload=" + request.payload);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "任务已接收"
        ));
    }

    public static class SyncRequest {
        public String taskType;
        public String payload;
        public long timestamp;
    }
}
