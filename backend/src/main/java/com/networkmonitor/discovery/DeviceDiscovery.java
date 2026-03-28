package com.networkmonitor.discovery;

import com.networkmonitor.model.Device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 1: Network Device Discovery
 * Discovers devices on the local network
 */
public class DeviceDiscovery {
    
    /**
     * Get the local subnet address (e.g., 192.168.1.0/24)
     */
    public static String getLocalSubnet() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // We want IPv4 addresses only
                    if (addr.getAddress().length == 4) {
                        String ip = addr.getHostAddress();
                        // Get subnet by replacing last octet with 0
                        String subnet = ip.substring(0, ip.lastIndexOf('.')) + ".0/24";
                        return subnet;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting local subnet: " + e.getMessage());
        }
        
        // Default fallback
        return "192.168.1.0/24";
    }
    
    /**
     * Scan the local network for active devices
     * Uses ping and ARP to discover devices
     */
    public static List<Device> scanNetwork(String subnet) {
        List<Device> devices = new ArrayList<>();
        String baseIP = subnet.substring(0, subnet.lastIndexOf('.') + 1);
        
        System.out.println("🔍 Scanning network: " + subnet);
        System.out.println("This may take a moment...\n");
        
        // Scan IP range (e.g., 192.168.1.1 to 192.168.1.254)
        for (int i = 1; i < 255; i++) {
            String ip = baseIP + i;
            
            try {
                // First try ping command (more reliable than isReachable)
                boolean reachable = pingHost(ip, 1000);
                
                if (reachable) {
                    String mac = getMacAddress(ip);
                    Device device = new Device(ip, mac);
                    devices.add(device);
                    System.out.println("✓ Found device: IP: " + ip + " | MAC: " + mac);
                }
            } catch (Exception e) {
                // Host not reachable, skip
            }
        }
        
        System.out.println("\n✅ Discovery complete. Found " + devices.size() + " device(s)\n");
        return devices;
    }
    
    /**
     * Get MAC address for a given IP using ARP
     */
    private static String getMacAddress(String ip) {
        try {
            // Try to get MAC address from ARP cache
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            
            if (os.contains("win")) {
                // Windows: use "arp -a <ip>"
                process = Runtime.getRuntime().exec("arp -a " + ip);
            } else {
                // Linux/Mac: use "arp -n <ip>"
                process = Runtime.getRuntime().exec("arp -n " + ip);
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            // Pattern to match MAC address (xx:xx:xx:xx:xx:xx or xx-xx-xx-xx-xx-xx)
            Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");
            
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find() && line.contains(ip)) {
                    return matcher.group().replace('-', ':').toUpperCase();
                }
            }
            
            reader.close();
            process.waitFor();
            
        } catch (Exception e) {
            // MAC address not available
        }
        
        return "Unknown";
    }
    
    /**
     * Quick scan - only check specific IPs (faster for testing)
     */
    public static List<Device> quickScan(String[] ips) {
        List<Device> devices = new ArrayList<>();
        
        System.out.println("🔍 Quick scanning specified IPs...\n");
        
        for (String ip : ips) {
            try {
                boolean reachable = pingHost(ip, 2000);
                
                if (reachable) {
                    String mac = getMacAddress(ip);
                    Device device = new Device(ip, mac);
                    devices.add(device);
                    System.out.println("✓ Found device: IP: " + ip + " | MAC: " + mac);
                } else {
                    System.out.println("✗ Device unreachable: " + ip);
                }
            } catch (Exception e) {
                System.out.println("✗ Device unreachable: " + ip);
            }
        }
        
        System.out.println("\n✅ Quick scan complete. Found " + devices.size() + " device(s)\n");
        return devices;
    }
    
    /**
     * Ping a host using system ping command (more reliable than InetAddress.isReachable)
     */
    private static boolean pingHost(String ip, int timeoutMs) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows: ping -n 1 -w <timeout_ms> <ip>
                processBuilder = new ProcessBuilder("ping", "-n", "1", "-w", String.valueOf(timeoutMs), ip);
            } else {
                // Linux/Mac: ping -c 1 -W <timeout_sec> <ip>
                int timeoutSec = Math.max(1, timeoutMs / 1000);
                processBuilder = new ProcessBuilder("ping", "-c", "1", "-W", String.valueOf(timeoutSec), ip);
            }
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (Exception e) {
            return false;
        }
    }
}
