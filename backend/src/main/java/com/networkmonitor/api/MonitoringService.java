package com.networkmonitor.api;

import com.networkmonitor.model.Device;
import com.networkmonitor.discovery.DeviceDiscovery;
import com.networkmonitor.monitor.LatencyMonitor;
import com.networkmonitor.monitor.BandwidthMonitor;
import com.networkmonitor.monitor.UptimeTracker;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service class to manage network monitoring operations
 */
public class MonitoringService {
    
    private static MonitoringService instance;
    private List<Device> devices;
    private boolean isMonitoring;
    private ScheduledExecutorService scheduler;
    private BandwidthMonitor.BandwidthRate currentBandwidth;
    private int iteration;
    
    private MonitoringService() {
        this.devices = new ArrayList<>();
        this.isMonitoring = false;
        this.iteration = 0;
        this.currentBandwidth = new BandwidthMonitor.BandwidthRate(0, 0);
    }
    
    public static synchronized MonitoringService getInstance() {
        if (instance == null) {
            instance = new MonitoringService();
        }
        return instance;
    }
    
    /**
     * Start monitoring with specified mode
     */
    public synchronized void startMonitoring(String mode, String[] customIps) {
        if (isMonitoring) {
            stopMonitoring();
        }
        
        // Discover devices based on mode
        switch (mode.toLowerCase()) {
            case "full":
                String subnet = DeviceDiscovery.getLocalSubnet();
                devices = DeviceDiscovery.scanNetwork(subnet);
                break;
                
            case "custom":
                devices = DeviceDiscovery.quickScan(customIps);
                break;
                
            case "quick":
            default:
                try {
                    java.net.InetAddress localAddr = com.networkmonitor.discovery.DeviceDiscovery.getPrimaryIPv4Address();
                    String localIP = localAddr != null ? localAddr.getHostAddress() : java.net.InetAddress.getLocalHost().getHostAddress();
                    devices = DeviceDiscovery.quickScan(new String[]{localIP, "8.8.8.8", "1.1.1.1"});
                } catch (Exception e) {
                    devices = DeviceDiscovery.quickScan(new String[]{"8.8.8.8", "1.1.1.1"});
                }
                break;
        }
        
        // Initial latency measurement
        for (Device device : devices) {
            LatencyMonitor.measureLatency(device);
        }
        
        isMonitoring = true;
        iteration = 0;
        
        // Start periodic monitoring
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateMonitoring();
            } catch (Exception e) {
                System.err.println("Error during monitoring: " + e.getMessage());
            }
        }, 3, 3, TimeUnit.SECONDS);
    }
    
    /**
     * Stop monitoring
     */
    public synchronized void stopMonitoring() {
        isMonitoring = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        devices.clear();
        iteration = 0;
    }
    
    /**
     * Update monitoring data
     */
    private void updateMonitoring() {
        iteration++;
        
        // Update latency for all devices
        LatencyMonitor.monitorAllDevices(devices);
        
        // Update uptime tracking
        UptimeTracker.trackAllDevices(devices);
        
        // Get bandwidth statistics
        BandwidthMonitor.NetworkStats stats = BandwidthMonitor.getSystemBandwidth();
        currentBandwidth = BandwidthMonitor.calculateBandwidthRate(
            "system", stats.bytesReceived, stats.bytesSent);
    }
    
    /**
     * Get current monitoring status
     */
    public synchronized MonitoringStatus getStatus() {
        return new MonitoringStatus(devices, currentBandwidth, iteration, isMonitoring);
    }
    
    /**
     * Check if monitoring is active
     */
    public boolean isMonitoring() {
        return isMonitoring;
    }
    
    /**
     * Inner class to hold monitoring status
     */
    public static class MonitoringStatus {
        public List<DeviceDTO> devices;
        public BandwidthDTO bandwidth;
        public int iteration;
        public boolean isActive;
        
        public MonitoringStatus(List<Device> devices, BandwidthMonitor.BandwidthRate bandwidth, int iteration, boolean isActive) {
            this.devices = new ArrayList<>();
            for (Device device : devices) {
                this.devices.add(new DeviceDTO(device));
            }
            this.bandwidth = new BandwidthDTO(bandwidth);
            this.iteration = iteration;
            this.isActive = isActive;
        }
    }
    
    /**
     * Device Data Transfer Object for JSON serialization
     */
    public static class DeviceDTO {
        public String ipAddress;
        public String macAddress;
        public boolean isUp;
        public double latency;
        public double packetLoss;
        public String uptime;
        
        public DeviceDTO(Device device) {
            this.ipAddress = device.getIpAddress();
            this.macAddress = device.getMacAddress();
            this.isUp = device.isUp();
            this.latency = device.getLatency();
            this.packetLoss = device.getPacketLoss();
            this.uptime = UptimeTracker.getFormattedUptime(device);
        }
    }
    
    /**
     * Bandwidth Data Transfer Object for JSON serialization
     */
    public static class BandwidthDTO {
        public double download;
        public double upload;
        
        public BandwidthDTO(BandwidthMonitor.BandwidthRate rate) {
            this.download = rate.downloadRate;
            this.upload = rate.uploadRate;
        }
    }
}
