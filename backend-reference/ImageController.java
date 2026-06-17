package com.example.yiliaoapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Value("${image.storage.path:./upload/images}")
    private String storagePath;

    // 内存索引：imageId -> 文件路径
    private final Map<Long, ImageRecord> imageIndex = new ConcurrentHashMap<>();
    private long idCounter = 0;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("inspectionId") long inspectionId,
            @RequestParam("createdAt") long createdAt) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "文件为空"));
        }

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path dir = Paths.get(storagePath, dateDir);
            Files.createDirectories(dir);

            long imageId = ++idCounter;
            String ext = getExtension(file.getOriginalFilename());
            String fileName = imageId + "_" + inspectionId + "_" + createdAt + ext;
            Path dest = dir.resolve(fileName);
            file.transferTo(dest);

            ImageRecord record = new ImageRecord(imageId, inspectionId,
                    dest.toString(), createdAt);
            imageIndex.put(imageId, record);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "imageId", imageId,
                    "imageUrl", "/api/image/" + imageId
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "存储失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> download(@PathVariable long imageId) {
        ImageRecord record = imageIndex.get(imageId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(record.filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = fis.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable long imageId) {
        ImageRecord record = imageIndex.remove(imageId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Files.deleteIfExists(Path.of(record.filePath));
        } catch (IOException ignored) {
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "已删除"));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }

    static class ImageRecord {
        final long imageId;
        final long inspectionId;
        final String filePath;
        final long createdAt;

        ImageRecord(long imageId, long inspectionId, String filePath, long createdAt) {
            this.imageId = imageId;
            this.inspectionId = inspectionId;
            this.filePath = filePath;
            this.createdAt = createdAt;
        }
    }
}
