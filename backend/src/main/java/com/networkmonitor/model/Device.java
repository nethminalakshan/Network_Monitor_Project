package com.networkmonitor.model;

import java.time.LocalDateTime;

/**
 * Represents a network device with monitoring metrics
 */
public class Device {
    private String ipAddress;
    private String macAddress;
    private boolean isUp;
    private double latency;
    private double packetLoss;
    private LocalDateTime lastSeen;
    private LocalDateTime firstSeen;
    private long bytesReceived;
    private long bytesSent;
    
    public Device(String ipAddress, String macAddress) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.isUp = true;
        this.lastSeen = LocalDateTime.now();
        this.firstSeen = LocalDateTime.now();
        this.latency = 0.0;
        this.packetLoss = 0.0;
        this.bytesReceived = 0;
        this.bytesSent = 0;
    }
    
    // Getters and Setters
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getMacAddress() {
        return macAddress;
    }
    
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    public boolean isUp() {
        return isUp;
    }
    
    public void setUp(boolean up) {
        isUp = up;
    }
    
    public double getLatency() {
        return latency;
    }
    
    public void setLatency(double latency) {
        this.latency = latency;
    }
    
    public double getPacketLoss() {
        return packetLoss;
    }
    
    public void setPacketLoss(double packetLoss) {
        this.packetLoss = packetLoss;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public LocalDateTime getFirstSeen() {
        return firstSeen;
    }
    
    public void setFirstSeen(LocalDateTime firstSeen) {
        this.firstSeen = firstSeen;
    }
    
    public long getBytesReceived() {
        return bytesReceived;
    }
    
    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public long getBytesSent() {
        return bytesSent;
    }
    
    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    /**
     * Calculate uptime in minutes since first seen
     */
    public long getUptimeMinutes() {
        return java.time.Duration.between(firstSeen, LocalDateTime.now()).toMinutes();
    }
    
    @Override
    public String toString() {
        return String.format("IP: %-15s | MAC: %-17s | Status: %-4s | Latency: %6.2fms | Loss: %5.1f%% | Uptime: %dm",
            ipAddress, macAddress, isUp ? "UP" : "DOWN", latency, packetLoss, getUptimeMinutes());
    }
}
