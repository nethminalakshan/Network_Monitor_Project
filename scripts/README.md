# Network Monitor

Full-stack network monitoring tool with a Java backend (CLI + REST API) and a React/Vite web UI.

## Features
- Device discovery (IP + MAC), latency, packet loss, bandwidth, uptime
- CLI mode and Web UI (auto-refresh every 3s)
- Scan modes: full subnet, quick/custom IP list, local-only test
- REST API (`/api/start`, `/api/status`, `/api/stop`)

## Requirements
- Java JDK 8+ (verify with `java -version`)
- Node.js 16+ (verify with `node -version`)
- Windows, macOS, or Linux; admin/root improves ICMP/ARP access

## Project Structure
```
Network_Monitor/
├── backend/                                 # Java sources + compiled classes
│   ├── src/main/java/com/networkmonitor/
│   └── out/
├── frontend/                                # React + Vite app
├── scripts/                                 # All helper scripts + this README
│   ├── README.md                            # You are here
│   ├── start-app.bat                        # One-click web stack
│   ├── start-backend.bat / start-frontend.bat
│   ├── compile-backend.bat / compile.sh
│   ├── run.bat / run.sh                     # CLI launcher
│   └── test-bandwidth.bat
└── readme.md.txt                            # Original requirements
```

## One-Click Web App (Windows)
```bash
cd scripts
start-app.bat
```
- Opens backend on http://localhost:8080 and frontend on http://localhost:3000.
- If first run, installs frontend deps and compiles backend.

## Web App (manual)
```bash
cd scripts
./compile-backend.bat         # build backend (classes in backend\out)
./start-backend.bat           # run API on :8080 (keep open)
./start-frontend.bat          # run React dev server on :3000 (opens browser)
```

## CLI App
```bash
cd scripts
./run.bat           # Windows
./run.sh            # Linux/Mac (sudo may be needed)

# Manual (Windows)
cd ..\backend
javac -d ..\out -cp ..\out src\main\java\com\networkmonitor\model\Device.java src\main\java\com\networkmonitor\discovery\DeviceDiscovery.java src\main\java\com\networkmonitor\monitor\*.java src\main\java\com\networkmonitor\api\*.java src\main\java\com\networkmonitor\NetworkMonitor.java
java -cp ..\out com.networkmonitor.NetworkMonitor

# Manual (Linux/Mac)
cd ../backend
javac -d ../out -cp ../out src/main/java/com/networkmonitor/model/Device.java src/main/java/com/networkmonitor/discovery/DeviceDiscovery.java src/main/java/com/networkmonitor/monitor/*.java src/main/java/com/networkmonitor/api/*.java src/main/java/com/networkmonitor/NetworkMonitor.java
java -cp ../out com.networkmonitor.NetworkMonitor
```

## Scan Modes (CLI & API)
- Full network scan: scans local /24
- Quick/custom: supply IP list (e.g., 192.168.1.1, 8.8.8.8)
- Local-only: self + common DNS (fast test)

## API Endpoints
- `POST /api/start` `{ "mode": "quick|custom|full", "ips": ["8.8.8.8"] }`
- `GET /api/status` current devices/bandwidth/iteration/isActive
- `POST /api/stop` stop monitoring

## Troubleshooting
- Run terminals as Administrator (Windows) or use sudo (Linux/Mac) for ICMP/ARP.
- If port 8080/3000 busy, stop other services or change ports in scripts/config.
- If MAC shows as Unknown, ping device first and rerun with elevated privileges.

## At a Glance
- Backend: Java 8+ HttpServer, ScheduledExecutorService, no external deps
- Frontend: React 18 + Vite, Axios, pure CSS, dev server on 3000
- Output cadence: 3s refresh; bandwidth, latency, packet loss, uptime per device

## License
Academic use. Modify freely for coursework and demos.
