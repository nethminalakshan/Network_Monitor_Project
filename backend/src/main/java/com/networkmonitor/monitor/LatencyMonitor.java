package com.networkmonitor.monitor;

import com.networkmonitor.model.Device;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 2: Latency & Packet Loss Monitoring
 * Monitors network latency and packet loss for devices
 */
public class LatencyMonitor {
    
    private static final int PING_COUNT = 10;
    private static final int PING_TIMEOUT = 1000; // 1 second
    
    /**
     * Measure latency and packet loss for a device
     */
    public static void measureLatency(Device device) {
        String ip = device.getIpAddress();
        List<Long> responseTimes = new ArrayList<>();
        int failedPings = 0;
        
        try {
            InetAddress address = InetAddress.getByName(ip);
            
            for (int i = 0; i < PING_COUNT; i++) {
                long startTime = System.currentTimeMillis();
                boolean reachable = address.isReachable(PING_TIMEOUT);
                long endTime = System.currentTimeMillis();
                
                if (reachable) {
                    long responseTime = endTime - startTime;
                    responseTimes.add(responseTime);
                } else {
                    failedPings++;
                }
                
                // Small delay between pings
                Thread.sleep(100);
            }
            
            // Calculate average latency
            if (!responseTimes.isEmpty()) {
                double avgLatency = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
                device.setLatency(avgLatency);
                device.setUp(true);
            } else {
                device.setUp(false);
                device.setLatency(0.0);
            }
            
            // Calculate packet loss percentage
            double packetLoss = (failedPings * 100.0) / PING_COUNT;
            device.setPacketLoss(packetLoss);
            
        } catch (IOException | InterruptedException e) {
            device.setUp(false);
            device.setLatency(0.0);
            device.setPacketLoss(100.0);
        }
    }
    
    /**
     * Quick latency check (single ping)
     */
    public static void quickPing(Device device) {
        String ip = device.getIpAddress();
        
        try {
            InetAddress address = InetAddress.getByName(ip);
            long startTime = System.currentTimeMillis();
            boolean reachable = address.isReachable(PING_TIMEOUT);
            long endTime = System.currentTimeMillis();
            
            if (reachable) {
                device.setLatency(endTime - startTime);
                device.setUp(true);
                device.setPacketLoss(0.0);
            } else {
                device.setUp(false);
                device.setPacketLoss(100.0);
            }
            
        } catch (IOException e) {
            device.setUp(false);
            device.setLatency(0.0);
            device.setPacketLoss(100.0);
        }
    }
    
    /**
     * Monitor latency for all devices
     */
    public static void monitorAllDevices(List<Device> devices) {
        for (Device device : devices) {
            quickPing(device);
        }
    }
}
