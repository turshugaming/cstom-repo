package com.terramonic.security;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Security utilities for TerraMonic Launcher
 * Implements security best practices for file downloads and extraction
 */
public class SecurityUtils {
    
    // Whitelist of trusted domains for downloads
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
        "maven.fabricmc.net",
        "modrinth.com",
        "cdn.modrinth.com",
        "github.com",
        "raw.githubusercontent.com"
        // Add your trusted domains here
    );
    
    // Maximum file size for downloads (100MB)
    private static final long MAX_DOWNLOAD_SIZE = 100 * 1024 * 1024;
    
    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jar", ".zip", ".json", ".png", ".jpg", ".jpeg", ".gif", ".txt", ".md"
    );
    
    /**
     * Validates if a URL is safe for downloading
     * @param urlString The URL to validate
     * @return true if the URL is considered safe
     */
    public static boolean isValidDownloadUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            
            // Must use HTTPS
            if (!"https".equals(url.getProtocol())) {
                System.err.println("⚠️ Security: Non-HTTPS URL rejected: " + urlString);
                return false;
            }
            
            // Check if domain is in whitelist
            String host = url.getHost().toLowerCase();
            boolean domainTrusted = TRUSTED_DOMAINS.stream()
                .anyMatch(trusted -> host.equals(trusted) || host.endsWith("." + trusted));
            
            if (!domainTrusted) {
                System.err.println("⚠️ Security: Untrusted domain rejected: " + host);
                return false;
            }
            
            // Check file extension if present
            String path = url.getPath();
            if (path.contains(".")) {
                String extension = path.substring(path.lastIndexOf(".")).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    System.err.println("⚠️ Security: Disallowed file extension: " + extension);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("⚠️ Security: Invalid URL format: " + urlString);
            return false;
        }
    }
    
    /**
     * Sanitizes a path to prevent directory traversal attacks
     * @param targetDir The target directory
     * @param entryName The entry name from ZIP
     * @return Sanitized path
     * @throws IOException if path is unsafe
     */
    public static Path sanitizePath(Path targetDir, String entryName) throws IOException {
        // Normalize the entry name to remove .. and other path traversal attempts
        Path normalizedEntry = Paths.get(entryName).normalize();
        
        // Resolve against target directory
        Path destPath = targetDir.resolve(normalizedEntry).normalize();
        
        // Ensure the destination is within the target directory
        if (!destPath.startsWith(targetDir)) {
            throw new IOException("🚨 Security: Path traversal attempt detected: " + entryName);
        }
        
        // Additional check for suspicious paths
        String pathString = normalizedEntry.toString();
        if (pathString.contains("..") || pathString.startsWith("/") || pathString.contains("\\..\\")) {
            throw new IOException("🚨 Security: Suspicious path detected: " + entryName);
        }
        
        return destPath;
    }
    
    /**
     * Validates a ZIP entry before extraction
     * @param entry The ZIP entry to validate
     * @param targetDir The target extraction directory
     * @return Sanitized path for extraction
     * @throws IOException if entry is unsafe
     */
    public static Path validateZipEntry(ZipEntry entry, Path targetDir) throws IOException {
        String entryName = entry.getName();
        
        // Check for zip bomb (compressed ratio)
        if (entry.getSize() > MAX_DOWNLOAD_SIZE) {
            throw new IOException("🚨 Security: File too large: " + entryName + " (" + entry.getSize() + " bytes)");
        }
        
        // Check for excessive compression ratio (potential zip bomb)
        if (entry.getCompressedSize() > 0) {
            long ratio = entry.getSize() / entry.getCompressedSize();
            if (ratio > 100) { // More than 100:1 compression ratio is suspicious
                throw new IOException("🚨 Security: Suspicious compression ratio: " + ratio + ":1 for " + entryName);
            }
        }
        
        // Sanitize the path
        return sanitizePath(targetDir, entryName);
    }
    
    /**
     * Validates file size during download
     * @param downloadedSize Current downloaded size
     * @throws IOException if size exceeds limit
     */
    public static void validateDownloadSize(long downloadedSize) throws IOException {
        if (downloadedSize > MAX_DOWNLOAD_SIZE) {
            throw new IOException("🚨 Security: Download size limit exceeded: " + downloadedSize + " bytes");
        }
    }
    
    /**
     * Generates a secure temporary file name
     * @param prefix Prefix for the temp file
     * @param suffix Suffix for the temp file
     * @return Secure temporary file name
     */
    public static String generateSecureTempName(String prefix, String suffix) {
        // Use current timestamp and random component for uniqueness
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return prefix + "_" + timestamp + "_" + random + suffix;
    }
    
    /**
     * Validates JSON content for basic safety
     * @param jsonContent The JSON string to validate
     * @return true if JSON appears safe
     */
    public static boolean isValidJsonContent(String jsonContent) {
        // Basic size check
        if (jsonContent.length() > 1024 * 1024) { // 1MB limit for JSON
            System.err.println("⚠️ Security: JSON content too large");
            return false;
        }
        
        // Check for suspicious content
        String[] suspiciousPatterns = {
            "<script", "javascript:", "eval(", "function(", "exec("
        };
        
        String lowerContent = jsonContent.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerContent.contains(pattern)) {
                System.err.println("⚠️ Security: Suspicious pattern detected in JSON: " + pattern);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Example usage showing how to integrate with the existing launcher code
     */
    public static class SecureDownloadExample {
        
        public static void secureDownloadFile(String url, Path target) throws IOException {
            // Validate URL first
            if (!SecurityUtils.isValidDownloadUrl(url)) {
                throw new IOException("🚨 Security: URL validation failed for: " + url);
            }
            
            // Rest of download logic...
            System.out.println("✅ Security: URL validated, proceeding with download: " + url);
            
            // Your existing download implementation here
            // But add size validation during download:
            // SecurityUtils.validateDownloadSize(downloadedBytes);
        }
        
        public static void secureExtractZip(Path zipPath, Path targetDir) throws IOException {
            // Your existing ZIP extraction logic, but with security validation:
            /*
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    // Use security validation
                    Path destPath = SecurityUtils.validateZipEntry(entry, targetDir);
                    
                    if (entry.isDirectory()) {
                        Files.createDirectories(destPath);
                    } else {
                        Files.createDirectories(destPath.getParent());
                        Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }
            */
        }
    }
}