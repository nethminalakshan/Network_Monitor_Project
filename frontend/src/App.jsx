import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'

function App() {
  const [isMonitoring, setIsMonitoring] = useState(false)
  const [scanMode, setScanMode] = useState('quick')
  const [customIps, setCustomIps] = useState('8.8.8.8, 1.1.1.1')
  const [devices, setDevices] = useState([])
  const [bandwidth, setBandwidth] = useState({ download: 0, upload: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [iteration, setIteration] = useState(0)

  // Fetch monitoring data periodically
  useEffect(() => {
    if (isMonitoring) {
      const interval = setInterval(async () => {
        try {
          const response = await axios.get('/api/status')
          setDevices(response.data.devices || [])
          setBandwidth(response.data.bandwidth || { download: 0, upload: 0 })
          setIteration(response.data.iteration || 0)
          setError(null)
        } catch (err) {
          console.error('Error fetching status:', err)
          setError('Failed to fetch monitoring data')
        }
      }, 3000)

      return () => clearInterval(interval)
    }
  }, [isMonitoring])

  const handleStart = async () => {
    setLoading(true)
    setError(null)
    
    try {
      const response = await axios.post('/api/start', {
        mode: scanMode,
        ips: scanMode === 'custom' ? customIps.split(',').map(ip => ip.trim()) : []
      })
      
      setDevices(response.data.devices || [])
      setIsMonitoring(true)
      setError(null)
    } catch (err) {
      console.error('Error starting monitor:', err)
      setError(err.response?.data?.message || 'Failed to start monitoring. Make sure the backend is running.')
    } finally {
      setLoading(false)
    }
  }

  const handleStop = async () => {
    try {
      await axios.post('/api/stop')
      setIsMonitoring(false)
      setDevices([])
      setBandwidth({ download: 0, upload: 0 })
      setIteration(0)
    } catch (err) {
      console.error('Error stopping monitor:', err)
      setError('Failed to stop monitoring')
    }
  }

  const formatBytes = (bytes) => {
    if (bytes < 1024) return bytes.toFixed(0) + ' B/s'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB/s'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB/s'
  }

  const getStatusColor = (status) => {
    return status ? '#10b981' : '#ef4444'
  }

  const getLatencyColor = (latency) => {
    if (latency < 10) return '#10b981'
    if (latency < 50) return '#f59e0b'
    return '#ef4444'
  }

  return (
    <div className="App">
      <header className="header">
        <div className="header-content">
          <h1>🌐 Network Monitor</h1>
          <p>Real-Time Network Device Monitoring</p>
        </div>
      </header>

      <div className="container">
        {/* Control Panel */}
        <div className="card control-panel">
          <h2>🎛️ Control Panel</h2>
          
          {!isMonitoring ? (
            <div className="controls">
              <div className="form-group">
                <label>Scan Mode:</label>
                <select 
                  value={scanMode} 
                  onChange={(e) => setScanMode(e.target.value)}
                  className="select"
                >
                  <option value="quick">Quick Scan (Local + DNS)</option>
                  <option value="custom">Custom IPs</option>
                  <option value="full">Full Network Scan</option>
                </select>
              </div>

              {scanMode === 'custom' && (
                <div className="form-group">
                  <label>IP Addresses (comma-separated):</label>
                  <input
                    type="text"
                    value={customIps}
                    onChange={(e) => setCustomIps(e.target.value)}
                    placeholder="192.168.1.1, 8.8.8.8, 1.1.1.1"
                    className="input"
                  />
                </div>
              )}

              <button 
                onClick={handleStart} 
                disabled={loading}
                className="btn btn-primary"
              >
                {loading ? '⏳ Starting...' : '▶️ Start Monitoring'}
              </button>
            </div>
          ) : (
            <div className="controls">
              <div className="status-badge">
                <span className="status-indicator"></span>
                <span>Monitoring Active - Iteration #{iteration}</span>
              </div>
              <button 
                onClick={handleStop}
                className="btn btn-danger"
              >
                ⏹️ Stop Monitoring
              </button>
            </div>
          )}

          {error && (
            <div className="error-message">
              ⚠️ {error}
            </div>
          )}
        </div>

        {/* Bandwidth Stats */}
        {isMonitoring && (
          <div className="card bandwidth-card">
            <h2>📊 System Bandwidth</h2>
            <div className="bandwidth-stats">
              <div className="stat">
                <span className="stat-label">⬇️ Download</span>
                <span className="stat-value">{formatBytes(bandwidth.download)}</span>
              </div>
              <div className="stat">
                <span className="stat-label">⬆️ Upload</span>
                <span className="stat-value">{formatBytes(bandwidth.upload)}</span>
              </div>
            </div>
          </div>
        )}

        {/* Device List */}
        {devices.length > 0 && (
          <div className="card devices-card">
            <h2>📱 Detected Devices ({devices.length})</h2>
            <div className="devices-grid">
              {devices.map((device, index) => (
                <div key={index} className="device-card">
                  <div className="device-header">
                    <span 
                      className="device-status"
                      style={{ backgroundColor: getStatusColor(device.isUp) }}
                    >
                      {device.isUp ? '🟢 UP' : '🔴 DOWN'}
                    </span>
                    <span className="device-ip">{device.ipAddress}</span>
                  </div>
                  
                  <div className="device-details">
                    <div className="detail-row">
                      <span className="detail-label">MAC:</span>
                      <span className="detail-value">{device.macAddress}</span>
                    </div>
                    
                    <div className="detail-row">
                      <span className="detail-label">Latency:</span>
                      <span 
                        className="detail-value"
                        style={{ color: getLatencyColor(device.latency) }}
                      >
                        {device.latency.toFixed(2)} ms
                      </span>
                    </div>
                    
                    <div className="detail-row">
                      <span className="detail-label">Packet Loss:</span>
                      <span className="detail-value">
                        {device.packetLoss.toFixed(1)}%
                      </span>
                    </div>
                    
                    <div className="detail-row">
                      <span className="detail-label">Uptime:</span>
                      <span className="detail-value">{device.uptime}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Empty State */}
        {!isMonitoring && devices.length === 0 && !loading && (
          <div className="card empty-state">
            <div className="empty-content">
              <span className="empty-icon">🔍</span>
              <h3>No Active Monitoring</h3>
              <p>Start monitoring to discover devices on your network</p>
            </div>
          </div>
        )}
      </div>

      <footer className="footer">
        <p>Network Monitor v1.0 | Java + React Edition</p>
      </footer>
    </div>
  )
}

export default App
