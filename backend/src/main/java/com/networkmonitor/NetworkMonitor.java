package com.networkmonitor;

import com.networkmonitor.model.Device;
import com.networkmonitor.discovery.DeviceDiscovery;
import com.networkmonitor.monitor.LatencyMonitor;
import com.networkmonitor.monitor.BandwidthMonitor;
import com.networkmonitor.monitor.UptimeTracker;

import java.util.List;
import java.util.Scanner;

/**
 * Phase 5: Network Monitor - Main Integration Class
 * Real-time network device monitoring tool
 * 
 * Features:
 * - Device Discovery (IP, MAC, Status)
 * - Latency Monitoring (ping response time)
 * - Packet Loss Detection
 * - Bandwidth Monitoring
 * - Uptime Tracking
 */
public class NetworkMonitor {
    
    private static final int MONITOR_INTERVAL = 3000; // 3 seconds
    private static boolean running = true;
    
    public static void main(String[] args) {
        printBanner();
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("🌐 Network Monitor - Real-Time Monitoring Tool");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Scan mode selection
        System.out.println("Select scan mode:");
        System.out.println("1. Full network scan (slower, scans all IPs)");
        System.out.println("2. Quick scan (faster, specify IPs)");
        System.out.println("3. Monitor local machine only");
        System.out.print("\nEnter choice (1-3): ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        List<Device> devices = null;
        
        switch (choice) {
            case 1:
                // Full network scan
                String subnet = DeviceDiscovery.getLocalSubnet();
                System.out.println("\n📡 Detected local subnet: " + subnet);
                devices = DeviceDiscovery.scanNetwork(subnet);
                break;
                
            case 2:
                // Quick scan with specified IPs
                System.out.print("\nEnter IP addresses (comma-separated): ");
                String ipInput = scanner.nextLine();
                String[] ips = ipInput.split(",");
                
                // Trim whitespace from IPs
                for (int i = 0; i < ips.length; i++) {
                    ips[i] = ips[i].trim();
                }
                
                devices = DeviceDiscovery.quickScan(ips);
                break;
                
            case 3:
                // Monitor local machine only
                try {
                    java.net.InetAddress localAddr = com.networkmonitor.discovery.DeviceDiscovery.getPrimaryIPv4Address();
                    String localIP = localAddr != null ? localAddr.getHostAddress() : java.net.InetAddress.getLocalHost().getHostAddress();
                    devices = DeviceDiscovery.quickScan(new String[]{localIP, "8.8.8.8", "1.1.1.1"});
                } catch (Exception e) {
                    System.err.println("Error detecting local IP: " + e.getMessage());
                    return;
                }
                break;
                
            default:
                System.out.println("Invalid choice. Exiting.");
                return;
        }
        
        if (devices == null || devices.isEmpty()) {
            System.out.println("❌ No devices found. Exiting.");
            return;
        }
        
        System.out.println("\n🚀 Starting real-time monitoring...");
        System.out.println("Press Ctrl+C to stop monitoring\n");
        System.out.println("=".repeat(120));
        
        // Add shutdown hook for graceful exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\n👋 Monitoring stopped. Goodbye!");
        }));
        
        // Initial detailed latency measurement
        System.out.println("\n📊 Performing initial latency measurement (this may take a moment)...\n");
        for (Device device : devices) {
            LatencyMonitor.measureLatency(device);
        }
        
        // Main monitoring loop
        int iteration = 0;
        while (running) {
            try {
                iteration++;
                
                // Clear screen (works on Windows)
                if (iteration > 1) {
                    clearScreen();
                }
                
                System.out.println("\n🔄 Monitoring Iteration #" + iteration + " - " + 
                    java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                System.out.println("=".repeat(120));
                
                // Update latency for all devices
                LatencyMonitor.monitorAllDevices(devices);
                
                // Update uptime tracking
                UptimeTracker.trackAllDevices(devices);
                
                // Get bandwidth statistics
                BandwidthMonitor.NetworkStats stats = BandwidthMonitor.getSystemBandwidth();
                BandwidthMonitor.BandwidthRate rate = BandwidthMonitor.calculateBandwidthRate(
                    "system", stats.bytesReceived, stats.bytesSent);
                
                // Display bandwidth
                System.out.println("\n📊 System Bandwidth: " + rate);
                System.out.println();
                
                // Display device information
                System.out.printf("%-20s %-20s %-8s %-12s %-12s %-15s%n", 
                    "IP Address", "MAC Address", "Status", "Latency", "Packet Loss", "Uptime");
                System.out.println("-".repeat(120));
                
                for (Device device : devices) {
                    System.out.printf("%-20s %-20s %-8s %-12s %-12s %-15s%n",
                        device.getIpAddress(),
                        device.getMacAddress(),
                        device.isUp() ? "🟢 UP" : "🔴 DOWN",
                        String.format("%.2f ms", device.getLatency()),
                        String.format("%.1f%%", device.getPacketLoss()),
                        UptimeTracker.getFormattedUptime(device));
                }
                
                System.out.println("=".repeat(120));
                System.out.println("\n⏳ Next update in " + (MONITOR_INTERVAL / 1000) + " seconds...");
                
                // Wait before next iteration
                Thread.sleep(MONITOR_INTERVAL);
                
            } catch (InterruptedException e) {
                System.out.println("\n\n⚠️  Monitoring interrupted.");
                break;
            } catch (Exception e) {
                System.err.println("Error during monitoring: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        scanner.close();
    }
    
    /**
     * Print application banner
     */
    private static void printBanner() {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                           ║");
        System.out.println("║          🌐 NETWORK MONITOR - JAVA EDITION 🌐            ║");
        System.out.println("║                                                           ║");
        System.out.println("║          Real-Time Network Device Monitoring              ║");
        System.out.println("║                                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Clear console screen
     */
    private static void clearScreen() {
        try {
            String os = System.getProperty("os.name");
            
            if (os.contains("Windows")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix/Linux/Mac
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clear fails, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}
