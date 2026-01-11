// @/net/websocket.js
/**
 * WebSocket服务类
 * 提供WebSocket连接管理、消息处理、自动重连、心跳检测等功能
 * 采用单例模式，确保全局只有一个WebSocket连接实例
 */
class WebSocketService {
  constructor() {
    // WebSocket连接实例
    this.socket = null
    // 当前重连尝试次数
    this.reconnectAttempts = 0
    // 最大重连尝试次数
    this.maxReconnectAttempts = 5
    // 重连间隔时间（毫秒）
    this.reconnectInterval = 3000
    // 消息处理器映射表：key=处理器ID, value=处理器对象
    this.messageHandlers = new Map()
    // 连接状态标志
    this.isConnected = false
  }

  /**
   * 建立WebSocket连接
   * 根据当前协议自动选择ws://或wss://
   */
  connect() {
    try {
      // 根据当前页面协议决定使用ws还是wss
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      // 构建WebSocket连接URL
      const wsUrl = `${protocol}//${window.location.host}/ws/notifications`

      // 创建WebSocket实例
      this.socket = new WebSocket(wsUrl)

      // 连接成功回调
      this.socket.onopen = () => {
        console.log('WebSocket连接已建立')
        this.isConnected = true
        this.reconnectAttempts = 0 // 重置重连计数器
        this.onConnected() // 执行连接成功后的逻辑
      }

      // 接收到消息回调
      this.socket.onmessage = (event) => {
        try {
          // 解析JSON格式的消息
          const message = JSON.parse(event.data)
          this.handleMessage(message) // 分发处理消息
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      // 连接关闭回调
      this.socket.onclose = (event) => {
        console.log('WebSocket连接已关闭:', event.code, event.reason)
        this.isConnected = false
        this.onDisconnected() // 执行连接断开后的逻辑
        this.handleReconnect() // 尝试重新连接
      }

      // 连接错误回调
      this.socket.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.isConnected = false
      }

    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.handleReconnect() // 连接失败时尝试重连
    }
  }

  /**
   * 处理WebSocket重连逻辑
   * 采用指数退避策略，重连间隔逐渐增加
   */
  handleReconnect() {
    // 检查是否达到最大重连次数
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      // 计算重连延迟时间（逐渐增加）
      const delay = this.reconnectInterval * this.reconnectAttempts
      console.log(`尝试重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})，延迟${delay}ms`)

      // 设置重连定时器
      this.reconnectTimer = setTimeout(() => {
        // 确保在重新连接前，前一个socket已经关闭
        if (this.socket) {
          try {
            this.socket.close()
          } catch (e) {
            console.warn('关闭旧socket时出错:', e)
          }
        }
        this.connect() // 重新连接
      }, delay)
    } else {
      console.error('WebSocket重连次数已达上限，请刷新页面重试')
    }
  }

  /**
   * 连接成功后的处理逻辑
   */
  onConnected() {
    console.log('WebSocket连接已完全建立，当前处理器数量:', this.messageHandlers.size)

    // 清除可能存在的重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    // 启动心跳检测
    this.startHeartbeat()

    // 通知所有已注册的处理器连接已建立
    this.messageHandlers.forEach((handler, handlerId) => {
      if (handler.onConnected) {
        try {
          console.log(`通知处理器 ${handlerId} 连接已建立`)
          handler.onConnected()
        } catch (error) {
          console.error(`处理器 ${handlerId} 的onConnected回调执行失败:`, error)
        }
      }
    })
  }

  /**
   * 连接断开后的处理逻辑
   */
  onDisconnected() {
    // 停止心跳检测
    this.stopHeartbeat()

    // 通知所有已注册的处理器连接已断开
    this.messageHandlers.forEach(handler => {
      if (handler.onDisconnected) {
        handler.onDisconnected()
      }
    })
  }

  /**
   * 启动心跳检测机制
   * 定期向服务器发送心跳包，保持连接活跃
   */
  startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      if (this.isConnected) {
        this.send('ping') // 发送心跳包
      }
    }, 30000) // 30秒发送一次心跳
  }

  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
    }
  }

  /**
   * 发送消息到服务器
   * @param {string|object} message 要发送的消息，可以是字符串或对象
   */
  send(message) {
    if (this.isConnected && this.socket) {
      if (typeof message === 'object') {
        // 对象类型消息转换为JSON字符串
        this.socket.send(JSON.stringify(message))
      } else {
        // 字符串类型消息直接发送
        this.socket.send(message)
      }
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }

  /**
   * 处理接收到的消息
   * 根据消息类型分发给对应的处理器
   * @param {object} message 接收到的消息对象
   */
  handleMessage(message) {
    try {
      const { type, data } = message
      console.log(`收到WebSocket消息: 类型=${type}`)

      // 标志位，记录消息是否被处理
      let messageHandled = false

      // 遍历所有注册的消息处理器
      this.messageHandlers.forEach((handler, handlerId) => {
        // 如果处理器有对应消息类型的处理方法
        if (handler[type]) {
          try {
            handler[type](data) // 调用对应的处理方法
            messageHandled = true
          } catch (error) {
            console.error(`处理器 ${handlerId} 处理消息类型 ${type} 失败:`, error)
          }
        }
        // 如果处理器有默认的消息处理方法
        else if (handler.default) {
          try {
            handler.default(message) // 调用默认处理方法
            messageHandled = true
          } catch (error) {
            console.error(`处理器 ${handlerId} 的default处理器执行失败:`, error)
          }
        }
      })

      // 如果消息未被任何处理器处理，记录日志（心跳和连接消息除外）
      if (!messageHandled && type !== 'pong' && type !== 'connected') {
        console.log(`未处理的WebSocket消息类型: ${type}`)
      }
    } catch (error) {
      console.error('处理WebSocket消息时发生错误:', error)
    }
  }

  /**
   * 注册消息处理器
   * @param {string} handlerId 处理器唯一标识
   * @param {object} handlers 处理器对象，包含各种消息类型的处理函数
   * 示例：
   * {
   *   onConnected: () => {},    // 连接建立回调
   *   onDisconnected: () => {}, // 连接断开回调
   *   new_notice: (data) => {}, // 新通知处理函数
   *   default: (message) => {}  // 默认消息处理函数
   * }
   */
  registerHandler(handlerId, handlers) {
    this.messageHandlers.set(handlerId, handlers)
  }

  /**
   * 注销消息处理器
   * @param {string} handlerId 要注销的处理器ID
   */
  unregisterHandler(handlerId) {
    this.messageHandlers.delete(handlerId)
  }

  /**
   * 主动断开WebSocket连接
   * 清理所有定时器和资源
   */
  disconnect() {
    console.log('主动断开WebSocket连接')
    // 停止心跳检测
    this.stopHeartbeat()

    // 清除重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    // 关闭WebSocket连接
    if (this.socket) {
      try {
        // 1000表示正常关闭
        this.socket.close(1000, '正常关闭')
      } catch (error) {
        console.warn('关闭socket时出错:', error)
      }
      this.socket = null
    }

    // 重置连接状态
    this.isConnected = false
    this.reconnectAttempts = 0
  }
}

// 创建单例实例 - 确保整个应用只有一个WebSocket连接
const webSocketService = new WebSocketService()

export default webSocketService
