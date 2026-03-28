package com.networkmonitor.monitor;

/**
 * Quick test for bandwidth monitoring
 */
public class BandwidthTest {
    public static void main(String[] args) {
        System.out.println("Testing Bandwidth Monitor...\n");
        
        // Get initial statistics
        BandwidthMonitor.NetworkStats stats1 = BandwidthMonitor.getSystemBandwidth();
        System.out.println("Initial Stats:");
        System.out.println("  Received: " + stats1.bytesReceived + " bytes");
        System.out.println("  Sent: " + stats1.bytesSent + " bytes");
        
        // Calculate rate
        BandwidthMonitor.BandwidthRate rate = BandwidthMonitor.calculateBandwidthRate(
            "test", stats1.bytesReceived, stats1.bytesSent);
        
        System.out.println("\nBandwidth Rate:");
        System.out.println("  Download: " + BandwidthMonitor.formatBytes(rate.downloadRate));
        System.out.println("  Upload: " + BandwidthMonitor.formatBytes(rate.uploadRate));
        
        // Wait 3 seconds
        try {
            System.out.println("\nWaiting 3 seconds...");
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
        
        // Get second measurement
        BandwidthMonitor.NetworkStats stats2 = BandwidthMonitor.getSystemBandwidth();
        System.out.println("\nSecond Stats:");
        System.out.println("  Received: " + stats2.bytesReceived + " bytes");
        System.out.println("  Sent: " + stats2.bytesSent + " bytes");
        
        // Calculate new rate
        BandwidthMonitor.BandwidthRate rate2 = BandwidthMonitor.calculateBandwidthRate(
            "test", stats2.bytesReceived, stats2.bytesSent);
        
        System.out.println("\nNew Bandwidth Rate:");
        System.out.println("  Download: " + BandwidthMonitor.formatBytes(rate2.downloadRate));
        System.out.println("  Upload: " + BandwidthMonitor.formatBytes(rate2.uploadRate));
        
        System.out.println("\n✅ Test complete!");
    }
}
