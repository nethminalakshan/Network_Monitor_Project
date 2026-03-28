package com.networkmonitor.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 3: Bandwidth Monitoring
 * Monitors network bandwidth usage
 */
public class BandwidthMonitor {
    
    private static Map<String, Long> previousBytesReceived = new HashMap<>();
    private static Map<String, Long> previousBytesSent = new HashMap<>();
    private static long lastMeasurementTime = System.currentTimeMillis();
    
    /**
     * Get total network bandwidth statistics for the system
     */
    public static NetworkStats getSystemBandwidth() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                return getWindowsBandwidth();
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                return getUnixBandwidth();
            }
        } catch (Exception e) {
            System.err.println("Error reading bandwidth: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new NetworkStats(0, 0);
    }
    
    /**
     * Get bandwidth statistics on Windows using PowerShell
     */
    private static NetworkStats getWindowsBandwidth() throws Exception {
        // Use full path to PowerShell
        String powerShellPath = System.getenv("SystemRoot") + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            powerShellPath, 
            "-Command",
            "Get-NetAdapterStatistics | ForEach-Object { $_.ReceivedBytes; $_.SentBytes }"
        );
        
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        long totalReceived = 0;
        long totalSent = 0;
        
        String line;
        List<Long> values = new ArrayList<>();
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                try {
                    long value = Long.parseLong(line);
                    values.add(value);
                } catch (NumberFormatException e) {
                    // Skip non-numeric lines
                    System.err.println("Skipping non-numeric line: " + line);
                }
            }
        }
        
        // Check for errors
        StringBuilder errors = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errors.append(line).append("\n");
        }
        if (errors.length() > 0) {
            System.err.println("PowerShell errors: " + errors.toString());
        }
        
        reader.close();
        errorReader.close();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            System.err.println("PowerShell command failed with exit code: " + exitCode);
        }
        
        // Sum all received bytes (even indices) and sent bytes (odd indices)
        for (int i = 0; i < values.size(); i++) {
            if (i % 2 == 0) {
                totalReceived += values.get(i);
            } else {
                totalSent += values.get(i);
            }
        }
        
        return new NetworkStats(totalReceived, totalSent);
    }
    
    /**
     * Get bandwidth statistics on Unix/Linux using /proc/net/dev or ifconfig
     */
    private static NetworkStats getUnixBandwidth() throws Exception {
        Process process = Runtime.getRuntime().exec("cat /proc/net/dev");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String line;
        long totalReceived = 0;
        long totalSent = 0;
        
        while ((line = reader.readLine()) != null) {
            // Skip header lines
            if (line.contains("face") || line.contains("|")) {
                continue;
            }
            
            // Parse interface statistics
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 10) {
                try {
                    // Bytes received is column 1, bytes sent is column 9
                    totalReceived += Long.parseLong(parts[1]);
                    totalSent += Long.parseLong(parts[9]);
                } catch (NumberFormatException e) {
                    // Skip invalid lines
                }
            }
        }
        
        reader.close();
        return new NetworkStats(totalReceived, totalSent);
    }
    
    /**
     * Calculate bandwidth usage per second
     */
    public static BandwidthRate calculateBandwidthRate(String key, long currentReceived, long currentSent) {
        long currentTime = System.currentTimeMillis();
        double timeElapsed = (currentTime - lastMeasurementTime) / 1000.0; // in seconds
        
        long prevReceived = previousBytesReceived.getOrDefault(key, currentReceived);
        long prevSent = previousBytesSent.getOrDefault(key, currentSent);
        
        double downloadRate = (currentReceived - prevReceived) / timeElapsed;
        double uploadRate = (currentSent - prevSent) / timeElapsed;
        
        // Update previous values
        previousBytesReceived.put(key, currentReceived);
        previousBytesSent.put(key, currentSent);
        lastMeasurementTime = currentTime;
        
        return new BandwidthRate(downloadRate, uploadRate);
    }
    
    /**
     * Format bytes to human-readable format
     */
    public static String formatBytes(double bytes) {
        if (bytes < 1024) {
            return String.format("%.0f B/s", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB/s", bytes / 1024);
        } else {
            return String.format("%.2f MB/s", bytes / (1024 * 1024));
        }
    }
    
    /**
     * Inner class to hold network statistics
     */
    public static class NetworkStats {
        public long bytesReceived;
        public long bytesSent;
        
        public NetworkStats(long bytesReceived, long bytesSent) {
            this.bytesReceived = bytesReceived;
            this.bytesSent = bytesSent;
        }
    }
    
    /**
     * Inner class to hold bandwidth rate
     */
    public static class BandwidthRate {
        public double downloadRate;
        public double uploadRate;
        
        public BandwidthRate(double downloadRate, double uploadRate) {
            this.downloadRate = downloadRate;
            this.uploadRate = uploadRate;
        }
        
        @Override
        public String toString() {
            return String.format("Download: %s | Upload: %s", 
                formatBytes(downloadRate), formatBytes(uploadRate));
        }
    }
}
