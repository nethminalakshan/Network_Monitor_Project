✅ Features You Implement Now
1. Device Discovery

Scan the local network

List:

IP Address

MAC Address

Device status (Up / Down)

2. Real-Time Monitoring

For each detected device:

📊 Bandwidth usage (bytes sent/received)

⏱️ Latency (ping response time)

❌ Packet loss (% failed pings)

🔄 Uptime (continuous availability)

5️⃣ Updated Implementation Plan (What To Do Step-by-Step)
🔹 Phase 1: Network Device Discovery

Goal: Detect all devices in LAN

Steps:

Get local subnet (e.g. 192.168.1.0/24)

Use ARP scan (Scapy)

Extract:

IP address

MAC address

Store devices in a Python list/dictionary

📌 Output example:

IP: 192.168.1.5  | MAC: 08:00:27:xx:xx:xx
🔹 Phase 2: Latency & Packet Loss Monitoring

Goal: Check connectivity quality

Steps:

Send ICMP ping to each device

Measure:

Response time (ms)

Failed responses

Calculate:

Average latency

Packet loss %

📌 Output example:

192.168.1.5 → Latency: 23ms | Packet Loss: 0%
🔹 Phase 3: Bandwidth Monitoring

Goal: Monitor traffic usage

Steps:

Use psutil.net_io_counters()

Measure:

Bytes sent

Bytes received

Calculate usage per second

📌 Output example:

Download: 120 KB/s | Upload: 45 KB/s
🔹 Phase 4: Uptime Tracking

Goal: Detect device availability

Steps:

Ping device every X seconds

If no response → mark as DOWN

Track how long device stays UP

📌 Output example:

192.168.1.5 → UP (15 min)
🔹 Phase 5: Integration & Testing

Goal: Make it work as one tool

Steps:

Combine discovery + monitoring

Run monitoring in loop (every 2–5 sec)

Test on:

Laptop

Mobile hotspot

Router

6️⃣ Final Deliverable (What Your Project Produces)

✅ Terminal-based real-time monitoring tool
✅ Automatic LAN device discovery
✅ Live monitoring of:

Bandwidth

Latency

Packet loss

Uptime

💡 This is more than enough for:

Network Programming

Computer Networks

Systems / OS projects