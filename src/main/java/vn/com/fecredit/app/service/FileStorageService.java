package vn.com.fecredit.app.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface FileStorageService {

    /**
     * Initialize storage
     */
    void init();

    /**
     * Store file
     */
    String store(MultipartFile file);

    /**
     * Store file with specific name
     */
    String store(MultipartFile file, String filename);

    /**
     * Load file as Resource
     */
    Resource loadAsResource(String filename);

    /**
     * Load file as Path
     */
    Path load(String filename);

    /**
     * Load all files
     */
    Stream<Path> loadAll();

    /**
     * Delete file
     */
    void delete(String filename);

    /**
     * Delete all files
     */
    void deleteAll();

    /**
     * Get file URL
     */
    String getFileUrl(String filename);

    /**
     * Check if file exists
     */
    boolean exists(String filename);

    /**
     * Get file size
     */
    long getFileSize(String filename);

    /**
     * Get file content type
     */
    String getContentType(String filename);

    /**
     * Copy file
     */
    void copy(String sourceFilename, String targetFilename);

    /**
     * Move file
     */
    void move(String sourceFilename, String targetFilename);

    /**
     * Create directory
     */
    void createDirectory(String directoryPath);

    /**
     * List files in directory
     */
    List<String> listFiles(String directoryPath);

    /**
     * Get file extension
     */
    String getFileExtension(String filename);

    /**
     * Generate unique filename
     */
    String generateUniqueFilename(String originalFilename);

    /**
     * Validate file
     */
    void validateFile(MultipartFile file);

    /**
     * Get file metadata
     */
    FileMetadata getFileMetadata(String filename);

    /**
     * Check storage space
     */
    StorageStats getStorageStats();

    /**
     * Clean up temporary files
     */
    void cleanupTempFiles();

    /**
     * File metadata class
     */
    @lombok.Data
    class FileMetadata {
        private final String filename;
        private final String contentType;
        private final long size;
        private final String path;
        private final java.time.LocalDateTime createdAt;
        private final java.time.LocalDateTime lastModified;
    }

    /**
     * Storage statistics class
     */
    @lombok.Data
    class StorageStats {
        private final long totalSpace;
        private final long usedSpace;
        private final long freeSpace;
        private final int fileCount;
        private final int directoryCount;
    }
}
