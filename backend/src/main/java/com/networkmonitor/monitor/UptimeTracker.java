package com.networkmonitor.monitor;

import com.networkmonitor.model.Device;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 4: Uptime Tracking
 * Tracks device availability and uptime
 */
public class UptimeTracker {
    
    private static Map<String, LocalDateTime> lastSeenTimes = new HashMap<>();
    private static Map<String, Boolean> previousStatus = new HashMap<>();
    
    /**
     * Update uptime status for a device
     */
    public static void updateUptime(Device device) {
        String ip = device.getIpAddress();
        LocalDateTime now = LocalDateTime.now();
        
        // Update last seen time if device is up
        if (device.isUp()) {
            device.setLastSeen(now);
            lastSeenTimes.put(ip, now);
            
            // If device was down before, it just came back up
            if (Boolean.FALSE.equals(previousStatus.get(ip))) {
                System.out.println("🟢 Device " + ip + " is back ONLINE");
                device.setFirstSeen(now); // Reset uptime counter
            }
        } else {
            // Device is down
            if (Boolean.TRUE.equals(previousStatus.get(ip))) {
                System.out.println("🔴 Device " + ip + " went OFFLINE");
            }
        }
        
        // Update previous status
        previousStatus.put(ip, device.isUp());
    }
    
    /**
     * Track uptime for all devices
     */
    public static void trackAllDevices(List<Device> devices) {
        for (Device device : devices) {
            updateUptime(device);
        }
    }
    
    /**
     * Get formatted uptime string
     */
    public static String getFormattedUptime(Device device) {
        if (!device.isUp()) {
            return "DOWN";
        }
        
        Duration uptime = Duration.between(device.getFirstSeen(), LocalDateTime.now());
        
        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    /**
     * Check for devices that haven't been seen in a while
     */
    public static void checkStaleDevices(List<Device> devices, int timeoutMinutes) {
        LocalDateTime now = LocalDateTime.now();
        
        for (Device device : devices) {
            LocalDateTime lastSeen = lastSeenTimes.get(device.getIpAddress());
            
            if (lastSeen != null) {
                long minutesSinceLastSeen = Duration.between(lastSeen, now).toMinutes();
                
                if (minutesSinceLastSeen > timeoutMinutes) {
                    device.setUp(false);
                    System.out.println("⚠️  Device " + device.getIpAddress() + 
                        " hasn't responded in " + minutesSinceLastSeen + " minutes");
                }
            }
        }
    }
}
