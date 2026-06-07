import axios from 'axios'

// 获取API基础URL（支持多环境配置）
function getBaseURL() {
  // 优先使用环境变量（仅在有实际值时使用）
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL
  if (envBaseUrl && envBaseUrl.trim() !== '') {
    return `${envBaseUrl.trim()}/api`
  }

  // 生产环境：使用相对路径（前后端一体化部署）
  // 空字符串会让 axios 自动使用当前域名，实现同源请求
  // 例如：https://c2cmarket.store/api/xxx
  const hostname = window.location.hostname

  // 开发环境：localhost 或 127.0.0.1
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return '/api'  // 使用相对路径，由 vite proxy 转发到后端
  }

  // 其他生产环境：使用 /api 前缀
  return '/api'
}

// 创建axios实例
const api = axios.create({
  baseURL: getBaseURL(),
  timeout: 20000, // 增加超时时间到20秒
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 解析图片URL为完整可访问地址
 * - 如果图片URL已是完整HTTP(S)地址，直接返回
 * - 如果设置了 VITE_IMAGE_BASE_URL 环境变量，拼接前缀（用于CDN/OSS等场景）
 * - 否则直接返回相对路径（默认情况下 /uploads/xxx.png 通过 nginx/vite-proxy 可访问）
 * @param {string|null|undefined} url - 图片URL（如 "/uploads/2026/06/07/uuid.png"）
 * @returns {string} 完整的图片访问地址
 */
export function resolveImageUrl(url) {
  if (!url) return ''
  // 已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // 通过环境变量配置图片资源基础URL（CDN/OSS场景）
  const imageBaseUrl = import.meta.env.VITE_IMAGE_BASE_URL
  if (imageBaseUrl && imageBaseUrl.trim() !== '') {
    const base = imageBaseUrl.trim().replace(/\/+$/, '')
    return base + url
  }
  return url
}

// 请求拦截器 - 添加JWT令牌
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误（增强版）
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      let message = '请求失败'
      let shouldLogout = false

      // 根据状态码生成友好的错误消息
      if (data && data.message) {
        message = data.message
      } else if (status === 400) {
        message = '请求参数错误，请检查输入'
      } else if (status === 401) {
        message = '登录已过期，请重新登录'
        shouldLogout = true
      } else if (status === 403) {
        message = '权限不足，无法执行此操作'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 429) {
        message = '操作太频繁，请稍后再试'
      } else if (status >= 500 && status < 504) {
        // 服务器错误（包括502 Bad Gateway, 503 Service Unavailable）
        const serverErrors = {
          500: '服务器内部错误',
          502: '网关错误，服务暂时不可用',
          503: '服务维护中，请稍后重试',
          504: '网关超时，请稍后重试'
        }
        message = serverErrors[status] || '服务器繁忙，请稍后重试'
      }

      // 如果需要登出（如token过期）
      if (shouldLogout) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 不在这里直接跳转，让组件自行决定如何处理
      }

      return Promise.reject({ message, status, code: status })
    } else if (error.request) {
      // 请求已发出但没有收到响应（网络问题）
      // 区分不同网络错误类型
      if (error.message.includes('timeout')) {
        return Promise.reject({
          message: '请求超时，请检查网络后重试',
          status: 0,
          code: 'TIMEOUT'
        })
      } else if (error.message.includes('Network Error')) {
        return Promise.reject({
          message: '网络连接失败，请检查网络设置',
          status: 0,
          code: 'NETWORK_ERROR'
        })
      }

      return Promise.reject({
        message: '网络连接失败，请检查网络设置',
        status: 0,
        code: 'NETWORK_ERROR'
      })
    } else {
      // 请求配置出错
      return Promise.reject({
        message: '请求配置错误',
        status: 0,
        code: 'CONFIG_ERROR'
      })
    }
  }
)

// ============================================
// 用户相关API
// ============================================
export const userApi = {
  register(data) {
    return api.post('/v2/users/register', data)
  },

  login(data) {
    return api.post('/v2/users/login', data)
  },

  getUserInfo() {
    return api.get('/v2/users/info')
  },

  getUserPublic(userId) {
    return api.get(`/v2/users/${userId}`)
  },

  updateProfile(data) {
    return api.put('/v2/users/profile', data)
  },

  changePassword(data) {
    return api.put('/v2/users/password', data)
  },

  sendResetCode(account) {
    return api.post('/v2/users/reset-password/send-code', { account })
  },

  verifyAndResetPassword(account, verifyCode, newPassword) {
    return api.post('/v2/users/reset-password/verify', { account, verifyCode, newPassword })
  }
}

// ============================================
// 商品相关API
// ============================================
export const productApi = {
  getProducts(params) {
    return api.get('/v2/products', { params })
  },

  getProductDetail(id) {
    return api.get(`/v2/products/${id}`)
  },

  createProduct(data) {
    return api.post('/v2/products', data)
  },

  updateProduct(id, data) {
    return api.put(`/v2/products/${id}`, data)
  },

  deleteProduct(id) {
    return api.delete(`/v2/products/${id}`)
  },

  toggleProductStatus(id, status) {
    return api.put(`/v2/products/${id}/status`, null, { params: { status } })
  },

  markAsSold(id) {
    return api.put(`/v2/products/${id}/sold`)
  },

  getMyProducts() {
    return api.get('/v2/products/my')
  },

  searchProducts(keyword) {
    return api.get('/v2/products/search', { params: { keyword } })
  }
}

// ============================================
// 消息/聊天相关API（增强版）
// ============================================
export const messageApi = {
  // 发送消息
  sendMessage(data) {
    return api.post('/v2/chat/messages', data)
  },

  // 获取对话记录（分页）
  getConversation(conversationId, params = {}) {
    return api.get(`/v2/chat/conversations/${conversationId}/messages`, { params })
  },

  // 获取联系人/会话列表
  getContacts() {
    return api.get('/v2/chat/conversations')
  },

  // 获取未读消息列表
  getUnreadMessages() {
    return api.get('/v2/chat/conversations')
  },

  // 标记单条消息为已读
  markAsRead(messageId) {
    return api.put(`/v2/chat/messages/${messageId}/read`)
  },

  // 标记整个对话为已读
  markConversationAsRead(conversationId) {
    return api.put(`/v2/chat/conversations/${conversationId}/read`)
  },

  // 获取未读消息数量
  getUnreadCount() {
    return api.get('/v2/chat/unread/count')
  },

  // 删除消息
  deleteMessage(messageId) {
    return api.delete(`/v2/chat/messages/${messageId}`)
  },

  // 撤回消息（发送后2分钟内）
  recallMessage(messageId) {
    return api.put(`/v2/chat/messages/${messageId}/recall`)
  },

  // 获取最近的消息预览（用于首页展示）
  getRecentMessages(limit = 10) {
    return api.get('/v2/chat/conversations', { params: { limit } })
  },

  // 创建会话（如果不存在则创建）
  createConversation(userId, productId) {
    return api.post('/v2/chat/messages', { userId, productId })
  },

  // 获取与某用户的会话ID
  getConversationId(otherUserId) {
    return api.get('/v2/chat/conversations')
  }
}

// ============================================
// 收藏相关API
// ============================================
export const favoriteApi = {
  addFavorite(productId) {
    return api.post(`/v2/favorites/${productId}`)
  },
  addPostFavorite(postId) {
    return api.post(`/v2/favorites/${postId}?targetType=POST`)
  },

  removeFavorite(productId) {
    return api.delete(`/v2/favorites/${productId}`)
  },
  removePostFavorite(postId) {
    return api.delete(`/v2/favorites/${postId}?targetType=POST`)
  },

  checkFavorited(productId) {
    return api.get(`/v2/favorites/${productId}/check`)
  },
  checkPostFavorited(postId) {
    return api.get(`/v2/favorites/${postId}/check?targetType=POST`)
  },

  getMyFavorites(params) {
    return api.get('/v2/favorites', { params })
  },

  getFavoriteCount() {
    return api.get('/v2/favorites/count')
  }
}

// ============================================
// 分类相关API
// ============================================
export const categoryApi = {
  getCategories(params) {
    return api.get('/v2/categories', { params })
  },

  getCategoryTree() {
    return api.get('/v2/categories/tree')
  },

  getCategoryDetail(id) {
    return api.get(`/v2/categories/${id}`)
  },

  createCategory(data) {
    return api.post('/v2/categories', data)
  },

  updateCategory(id, data) {
    return api.put(`/v2/categories/${id}`, data)
  },

  deleteCategory(id) {
    return api.delete(`/v2/categories/${id}`)
  },

  getCategoryProducts(categoryId, params) {
    return api.get(`/v2/categories/${categoryId}/products`, { params })
  }
}

// ============================================
// WebSocket连接管理（用于实时聊天）
// ============================================
class WebSocketManager {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
    this.listeners = new Map()
    this.isConnected = false
  }

  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return
    }

    const hostname = window.location.hostname
    const isDev = hostname === 'localhost' || hostname === '127.0.0.1'
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    
    // 开发环境：使用 localhost:8080
    // 生产环境：使用当前域名的 wss/ws 协议（前后端一体化部署）
    const wsUrl = import.meta.env.VITE_WS_URL || 
      (isDev 
        ? `ws://localhost:8080/ws/chat`
        : `${protocol}://${hostname}/ws/chat`
      )

    try {
      this.ws = new WebSocket(`${wsUrl}?token=${token}`)

      this.ws.onopen = () => {
        this.isConnected = true
        this.reconnectAttempts = 0
        this.emit('connected')
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.emit('message', data)

          // 根据消息类型分发事件
          if (data.type) {
            this.emit(data.type, data)
          }
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.emit('error', error)
      }

      this.ws.onclose = () => {
        this.isConnected = false
        this.emit('disconnected')

        // 自动重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          setTimeout(() => this.connect(token), this.reconnectDelay * this.reconnectAttempts)
        }
      }
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error)
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
      this.isConnected = false
    }
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
      return true
    }
    console.warn('WebSocket is not connected')
    return false
  }

  // 发送聊天消息
  sendMessage(content, receiverId, productId = null) {
    return this.send({
      type: 'chat',
      content,
      receiverId,
      productId,
      timestamp: Date.now()
    })
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => {
        try {
          callback(data)
        } catch (e) {
          console.error(`Error in event handler for ${event}:`, e)
        }
      })
    }
  }
}

// ==================== 全局搜索 API ====================
export const searchApi = {
  search(params) { return api.get('/v2/search', { params }) }
}

// ==================== 社区帖子 API ====================
export const postApi = {
  getPosts(params) { return api.get('/v2/posts', { params }) },
  getPostDetail(id) { return api.get(`/v2/posts/${id}`) },
  createPost(data) { return api.post('/v2/posts', data) },
  updatePost(id, data) { return api.put(`/v2/posts/${id}`, data) },
  deletePost(id) { return api.delete(`/v2/posts/${id}`) },
  getUserPosts(userId) { return api.get(`/v2/posts/user/${userId}`) },
  addComment(postId, data) { return api.post(`/v2/posts/${postId}/comments`, data) },
  getComments(postId, params) { return api.get(`/v2/posts/${postId}/comments`, { params }) },
  deleteComment(commentId) { return api.delete(`/v2/posts/comments/${commentId}`) },
  togglePin(id, isPinned) { return api.put(`/v2/posts/${id}/pin`, null, { params: { isPinned } }) },
  toggleEssence(id, isEssence) { return api.put(`/v2/posts/${id}/essence`, null, { params: { isEssence } }) }
}

// ==================== 商品评论 API ====================
export const productCommentApi = {
  addComment(productId, data) { return api.post(`/v2/products/${productId}/comments`, data) },
  getComments(productId, params) { return api.get(`/v2/products/${productId}/comments`, { params }) },
  deleteComment(commentId) { return api.delete(`/v2/products/comments/${commentId}`) }
}

// ==================== 点赞 API ====================
export const likeApi = {
  toggleLike(targetType, targetId) { return api.post('/v2/likes/toggle', null, { params: { targetType, targetId } }) },
  checkLiked(targetType, targetId) { return api.get('/v2/likes/check', { params: { targetType, targetId } }) },
  getLikeCount(targetType, targetId) { return api.get('/v2/likes/count', { params: { targetType, targetId } }) }
}

// ==================== 关注 API ====================
export const followApi = {
  toggleFollow(followeeId) { return api.post('/v2/follows/toggle', null, { params: { followeeId } }) },
  checkFollowing(followeeId) { return api.get('/v2/follows/check', { params: { followeeId } }) },
  getFollowStats(userId) { return api.get('/v2/follows/stats', { params: { userId } }) },
  getFollowingList(userId, page = 1, size = 20) { return api.get('/v2/follows/following', { params: { userId, page, size } }) },
  getFollowerList(userId, page = 1, size = 20) { return api.get('/v2/follows/followers', { params: { userId, page, size } }) }
}

// ==================== 信息流 API ====================
export const feedApi = {
  getFeed(params) { return api.get('/v2/feed', { params }) },
  getRecommendations(limit = 6) { return api.get('/v2/feed/recommend', { params: { limit } }) },
  recordBehavior(targetType, targetId) { return api.post('/v2/feed/behavior', { targetType, targetId }) }
}

// ==================== 活动 API ====================
export const activityApi = {
  getActivities(params) { return api.get('/v2/activities', { params }) },
  getActivityDetail(id) { return api.get(`/v2/activities/${id}`) },
  createActivity(data) { return api.post('/v2/activities', data) },
  getMyActivities(params) { return api.get('/v2/activities/my', { params }) },
  joinActivity(id) { return api.post(`/v2/activities/${id}/join`) },
  cancelJoin(id) { return api.delete(`/v2/activities/${id}/join`) }
}

// ==================== 商品故事 API ====================
export const storyApi = {
  getStoryFeed(params) { return api.get('/v2/stories/feed', { params }) },
  getUserStories(userId) { return api.get(`/v2/stories/user/${userId}`) }
}

// ==================== 通知 API ====================
export const notificationApi = {
  /** 获取通知列表，支持按类型筛选 */
  getNotifications(params = {}) {
    return api.get('/v2/notifications', { params })
  },
  /** 获取私信通知列表 */
  getChatNotifications(params = {}) {
    return api.get('/v2/notifications/chats', { params })
  },
  /** 获取未读通知数 */
  getUnreadCount() {
    return api.get('/v2/notifications/unread/count')
  },
  /** 标记单条通知为已读 */
  markAsRead(id) {
    return api.put(`/v2/notifications/${id}/read`)
  },
  /** 标记所有通知为已读 */
  markAllAsRead() {
    return api.put('/v2/notifications/read/all')
  },
  /** 清空所有通知 */
  clearAll() {
    return api.delete('/v2/notifications/clear')
  }
}

// ==================== 广告 API ====================
export const adApi = {
  createAd(data) { return api.post('/v2/ads', data) },
  getPackages() { return api.get('/v2/ads/packages') },
  simulatePayment(postId, packageId) { return api.post(`/v2/ads/${postId}/pay`, { packageId }) }
}

// ==================== 文件上传 API ====================
export const uploadApi = {
  // 上传单张图片
  uploadImage(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/v2/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress,
      timeout: 60000
    })
  },

  // 批量上传图片
  uploadImages(files, onProgress) {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return api.post('/v2/upload/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress,
      timeout: 120000
    })
  },

  // 上传头像
  uploadAvatar(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/v2/upload/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress,
      timeout: 60000
    })
  },

  // 上传分片
  uploadChunk(chunk, fileId, chunkIndex, totalChunks, fileName) {
    const formData = new FormData()
    formData.append('chunk', chunk)
    formData.append('fileId', fileId)
    formData.append('chunkIndex', chunkIndex)
    formData.append('totalChunks', totalChunks)
    formData.append('fileName', fileName)
    return api.post('/v2/upload/chunk', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
  },

  // 合并分片
  mergeChunks(fileId, fileName, totalChunks) {
    return api.post('/v2/upload/merge', null, {
      params: { fileId, fileName, totalChunks }
    })
  }
}

// ==================== 组织 API ====================
export const organizationApi = {
  create(data) { return api.post('/v2/organizations', data) },
  getDetail(id) { return api.get(`/v2/organizations/${id}`) },
  getMyOrgs() { return api.get('/v2/organizations/my') },
  getList(params) { return api.get('/v2/organizations', { params }) },
  invite(orgId, inviteeId) { return api.post(`/v2/organizations/${orgId}/invite`, { inviteeId }) },
  acceptInvite(code) { return api.post(`/v2/organizations/invitations/${code}/accept`) },
  getMyInvitations() { return api.get('/v2/organizations/invitations/my') },
  applyJoin(orgId, message) { return api.post(`/v2/organizations/${orgId}/apply`, { message }) },
  getPendingRequests(orgId) { return api.get(`/v2/organizations/${orgId}/requests`) },
  approveRequest(requestId) { return api.put(`/v2/organizations/requests/${requestId}/approve`) },
  rejectRequest(requestId) { return api.put(`/v2/organizations/requests/${requestId}/reject`) },
  getMembers(orgId, params) { return api.get(`/v2/organizations/${orgId}/members`, { params }) },
  removeMember(orgId, userId) { return api.delete(`/v2/organizations/${orgId}/members/${userId}`) },
  changeRole(orgId, userId, role) { return api.put(`/v2/organizations/${orgId}/members/${userId}/role`, { role }) },
  getMyRole(orgId) { return api.get(`/v2/organizations/${orgId}/my-role`) },
  getAuditLogs(orgId, limit) { return api.get(`/v2/organizations/${orgId}/audit-logs`, { params: { limit } }) },
  approveOrg(id) { return api.put(`/v2/organizations/${id}/approve`) },
  rejectOrg(id) { return api.put(`/v2/organizations/${id}/reject`) }
}

// 创建全局WebSocket实例
export const wsManager = new WebSocketManager()

export default api
