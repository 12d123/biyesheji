import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

class WebSocketService {
  constructor() {
    this.socket = null
    this.stompClient = null
    this.isConnected = false
    this.callbacks = {
      onConnect: [],
      onDisconnect: [],
      onSensorData: []
    }
  }

  // 连接WebSocket
  connect() {
    return new Promise((resolve, reject) => {
      try {
        // 创建SockJS连接
        this.socket = new SockJS('http://localhost:8085/ws')
        
        // 创建STOMP客户端
        this.stompClient = Stomp.over(this.socket)
        
        // 连接回调
        this.stompClient.connect({}, () => {
          console.log('WebSocket连接成功')
          this.isConnected = true
          
          // 订阅传感器数据主题
          this.stompClient.subscribe('/topic/sensor/data', (message) => {
            try {
              const data = JSON.parse(message.body)
              console.log('收到传感器数据:', data)
              this.callbacks.onSensorData.forEach(callback => callback(data))
            } catch (error) {
              console.error('解析传感器数据失败:', error)
            }
          })
          
          // 触发连接成功回调
          this.callbacks.onConnect.forEach(callback => callback())
          resolve()
        }, (error) => {
          console.error('WebSocket连接失败:', error)
          this.isConnected = false
          this.callbacks.onDisconnect.forEach(callback => callback())
          reject(error)
        })
        
        // 连接关闭回调
        this.socket.onclose = () => {
          console.log('WebSocket连接关闭')
          this.isConnected = false
          this.callbacks.onDisconnect.forEach(callback => callback())
        }
        
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
        reject(error)
      }
    })
  }

  // 断开连接
  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect()
      this.stompClient = null
    }
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.isConnected = false
    console.log('WebSocket已断开连接')
  }

  // 发送消息
  send(destination, message) {
    console.log('📤 WebSocket.send 被调用:', { destination, message, isConnected: this.isConnected });
    if (this.isConnected && this.stompClient) {
      this.stompClient.send(destination, {}, message)
      console.log('✅ STOMP消息已发送');
      return true
    }
    console.log('❌ WebSocket未连接或stompClient不存在');
    return false
  }

  // 注册回调
  on(event, callback) {
    if (this.callbacks[event]) {
      this.callbacks[event].push(callback)
    }
  }

  // 移除回调
  off(event, callback) {
    if (this.callbacks[event]) {
      this.callbacks[event] = this.callbacks[event].filter(cb => cb !== callback)
    }
  }

  // 检查连接状态
  getConnectionStatus() {
    return this.isConnected
  }
}

// 导出单例
const webSocketService = new WebSocketService()
export default webSocketService
