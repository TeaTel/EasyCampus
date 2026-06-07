import { ref, computed } from 'vue'
import { userApi } from '../services/api'

// 创建响应式状态
const token = ref('')
const user = ref(null)

// 初始化时验证存储的token（拒绝无效token如demo_token）
function initializeAuth() {
  const storedToken = localStorage.getItem('token')
  const storedUser = localStorage.getItem('user')
  
  if (storedToken && storedUser) {
    // 验证token格式，拒绝演示模式的假token
    if (storedToken.startsWith('demo_token_') || storedToken.includes('demo')) {
      console.warn('检测到无效的演示模式token，已自动清除')
      clearAuthData()
      return
    }
    
    try {
      const parsedUser = JSON.parse(storedUser)
      
      // 验证用户数据完整性
      if (!parsedUser.id || !parsedUser.username) {
        console.warn('用户数据不完整，已自动清除')
        clearAuthData()
        return
      }
      
      token.value = storedToken
      user.value = parsedUser
      
      // 检查token是否过期
      if (parsedUser.tokenExpiry && new Date() > new Date(parsedUser.tokenExpiry)) {
        console.warn('Token已过期，请重新登录')
        clearAuthData()
        return
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
      clearAuthData()
    }
  }
}

// 执行初始化
initializeAuth()

// 计算属性
const isAuthenticated = computed(() => !!token.value)
const currentUser = computed(() => user.value)

// 登录函数
async function login(username, password) {
  try {
    const response = await userApi.login({ username, password })
    
    if (response.code === 200 && response.data) {
      const { token: newToken, user: userData } = response.data
      
      // 保存到本地存储
      localStorage.setItem('token', newToken)
      localStorage.setItem('user', JSON.stringify(userData))
      
      // 更新响应式状态
      token.value = newToken
      user.value = userData
      
      return { success: true, data: response.data }
    } else {
      return { success: false, message: response.message || '登录失败' }
    }
  } catch (error) {
    console.error('登录错误:', error)
    return { success: false, message: error.message || '登录失败' }
  }
}

// 注册函数
async function register(userData) {
  try {
    const response = await userApi.register(userData)
    
    if (response.code === 200 && response.data) {
      const { token: newToken, user: registeredUser } = response.data
      
      // 保存到本地存储
      localStorage.setItem('token', newToken)
      localStorage.setItem('user', JSON.stringify(registeredUser))
      
      // 更新响应式状态
      token.value = newToken
      user.value = registeredUser
      
      return { success: true, data: response.data }
    } else {
      return { success: false, message: response.message || '注册失败' }
    }
  } catch (error) {
    console.error('注册错误:', error)
    return { success: false, message: error.message || '注册失败' }
  }
}

// 登出函数
function logout() {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  
  // 清除响应式状态
  token.value = ''
  user.value = null
  
  // 跳转到登录页
  window.location.href = '/login'
}

// 获取用户信息函数
async function fetchUserInfo() {
  try {
    const response = await userApi.getUserInfo()
    
    if (response.code === 200 && response.data) {
      // 更新用户信息
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
      return { success: true, data: response.data }
    } else {
      return { success: false, message: response.message || '获取用户信息失败' }
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    return { success: false, message: error.message || '获取用户信息失败' }
  }
}

// 检查登录状态
function checkAuth() {
  const storedToken = localStorage.getItem('token')
  const storedUser = localStorage.getItem('user')
  
  if (!storedToken || !storedUser) {
    return false
  }
  
  // 基本格式验证
  try {
    token.value = storedToken
    user.value = JSON.parse(storedUser)
    
    // 如果有expiry字段，检查是否过期
    if (user.value?.tokenExpiry && new Date() > new Date(user.value.tokenExpiry)) {
      clearAuthData()
      return false
    }
    
    return true
  } catch (e) {
    console.error('解析用户信息失败:', e)
    clearAuthData()
    return false
  }
}

// 内部方法：清除认证数据
function clearAuthData() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  
  token.value = ''
  user.value = null
}

// 导出store
export function useAuthStore() {
  return {
    // 状态
    token,
    user,
    
    // 计算属性
    isAuthenticated,
    currentUser,
    
    // 方法
    login,
    register,
    logout,
    fetchUserInfo,
    checkAuth,
    clearAuthData
  }
}