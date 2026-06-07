<template>
  <div class="login-page">
    <div class="login-brand">
      <div class="brand-circles">
        <span class="circle circle-1"></span>
        <span class="circle circle-2"></span>
        <span class="circle circle-3"></span>
        <span class="circle circle-4"></span>
        <span class="circle circle-5"></span>
      </div>
      <div class="brand-content">
        <h1 class="brand-title">校园集市</h1>
        <p class="brand-subtitle">Campus Market</p>
        <p class="brand-slogan">让闲置流动起来，让校园更精彩</p>
      </div>
    </div>

    <main class="login-form-side">
      <form @submit.prevent="handleLogin" class="login-form">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-desc">登录你的账号，继续探索校园好物</p>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.username, filled: form.username }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              type="text"
              v-model="form.username"
              placeholder="学号/手机号/邮箱"
              @focus="focusState.username = true"
              @blur="focusState.username = false"
              autocomplete="username"
            />
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.password, filled: form.password }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2"/>
              <path d="M7 11V7a5 5 0 1110 0v4"/>
            </svg>
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              placeholder="请输入密码"
              @focus="focusState.password = true"
              @blur="focusState.password = false"
              autocomplete="current-password"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="toggle-password"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="rememberMe" />
            <span>记住我</span>
          </label>
          <router-link to="/forgot-password" class="forgot-link">忘记密码?</router-link>
        </div>

        <button
          type="submit"
          :disabled="loading || !isFormValid"
          class="submit-btn"
          :class="{ active: isFormValid }"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <button
          type="button"
          @click="goRegister"
          class="register-btn"
          :disabled="loading"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
            <circle cx="8.5" cy="7" r="4"/>
            <line x1="20" y1="8" x2="20" y2="14"/>
            <line x1="23" y1="11" x2="17" y2="11"/>
          </svg>
          注册账号
        </button>

        <div class="divider">
          <span>其他登录方式</span>
        </div>

        <div class="social-login">
          <button type="button" class="social-btn wechat" title="微信登录" @click="handleWechatLogin">
            <svg viewBox="0 0 24 24" fill="#07C160" width="24" height="24">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.045c.134 0 .24-.111.24-.247 0-.06-.023-.118-.038-.177l-.327-1.233a.582.582 0 01.176-.554C23.438 18.142 24 16.494 24 14.694c0-3.442-3.155-6.282-7.062-6.836zm-2.821 2.571c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
            </svg>
          </button>
          <button type="button" class="social-btn student" title="校园认证" @click="handleCampusAuth">
            <span>📚</span>
          </button>
        </div>
      </form>

      <p class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { userApi } from '../services/api'
import { useToast } from '../use/useToast'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)

const focusState = ref({
  username: false,
  password: false
})

const isFormValid = computed(() => {
  return form.value.username.trim().length > 0 && form.value.password.length >= 6
})

function goRegister() {
  if (loading.value) return
  router.push('/register')
}

function handleWechatLogin() {
  toast.showToast('微信登录即将上线，敬请期待', 'info')
}

function handleCampusAuth() {
  toast.showToast('校园认证功能开发中', 'info')
}

async function handleLogin() {
  if (!isFormValid.value || loading.value) return

  try {
    loading.value = true
    errorMessage.value = ''

    const result = await authStore.login(
      form.value.username,
      form.value.password
    )

    if (result.success) {
      const redirectPath = route.query.redirect || '/'
      router.push(redirectPath)
    } else {
      errorMessage.value = result.message || '登录失败，请检查账号密码'
    }
  } catch (error) {
    console.error('登录失败:', error)

    if (error.code === 'NETWORK_ERROR' || error.code === 'TIMEOUT') {
      errorMessage.value = error.message || '网络错误，请稍后重试'
    } else if (error.status === 401) {
      errorMessage.value = '账号或密码错误'
    } else {
      errorMessage.value = error.message || '登录失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  animation: pageFadeIn var(--duration-slower) var(--ease-out) both;
}

@keyframes pageFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.login-brand {
  position: relative;
  flex: 1;
  background: var(--gradient-hero);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 100vh;
}

.brand-circles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.circle-1 {
  width: 320px;
  height: 320px;
  top: -80px;
  left: -60px;
  animation: floatCircle 8s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 10%;
  right: -40px;
  animation: floatCircle 6s ease-in-out infinite 1s;
}

.circle-3 {
  width: 140px;
  height: 140px;
  top: 30%;
  right: 15%;
  background: rgba(255, 255, 255, 0.05);
  animation: floatCircle 10s ease-in-out infinite 2s;
}

.circle-4 {
  width: 80px;
  height: 80px;
  bottom: 25%;
  left: 20%;
  background: rgba(255, 255, 255, 0.06);
  animation: floatCircle 7s ease-in-out infinite 0.5s;
}

.circle-5 {
  width: 60px;
  height: 60px;
  top: 15%;
  left: 40%;
  background: rgba(255, 255, 255, 0.04);
  animation: floatCircle 9s ease-in-out infinite 3s;
}

@keyframes floatCircle {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(1.05);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: var(--color-text-inverse);
  padding: var(--space-8);
}

.brand-title {
  font-size: 3.5rem;
  font-weight: var(--font-extrabold);
  letter-spacing: 6px;
  margin: 0 0 var(--space-2) 0;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.brand-subtitle {
  font-size: var(--text-lg);
  font-weight: var(--font-medium);
  letter-spacing: 3px;
  opacity: 0.85;
  margin: 0 0 var(--space-6) 0;
}

.brand-slogan {
  font-size: var(--text-base);
  opacity: 0.7;
  letter-spacing: 1px;
  margin: 0;
}

.login-form-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-primary);
  padding: var(--space-12) var(--space-8);
  min-height: 100vh;
}

.login-form {
  width: 100%;
  max-width: 420px;
  animation: formSlideIn var(--duration-slower) var(--ease-out) both;
  animation-delay: 0.15s;
}

@keyframes formSlideIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-extrabold);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-1) 0;
}

.form-desc {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
  margin: 0 0 var(--space-8) 0;
}

.input-group {
  margin-bottom: var(--space-5);
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background-color: var(--color-bg-secondary);
  border: 2px solid transparent;
  border-radius: var(--radius-xl);
  transition: all var(--duration-normal) var(--ease-out);
}

.input-wrapper.focused,
.input-wrapper:focus-within {
  background-color: var(--color-bg-primary);
  border-color: var(--color-primary-400);
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
}

.input-icon {
  width: 22px;
  height: 22px;
  color: var(--color-text-tertiary);
  flex-shrink: 0;
  transition: color var(--duration-normal) var(--ease-out);
}

.input-wrapper.focused .input-icon {
  color: var(--color-primary-500);
}

.input-wrapper input {
  flex: 1;
  border: none;
  background: none;
  font-size: var(--text-base);
  font-family: var(--font-sans);
  color: var(--color-text-primary);
  outline: none;
}

.input-wrapper input::placeholder {
  color: var(--color-text-tertiary);
}

.toggle-password {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: color var(--duration-normal) var(--ease-out);
  background: none;
  border: none;
}

.toggle-password svg {
  width: 20px;
  height: 20px;
}

.toggle-password:hover {
  color: var(--color-primary-500);
}

.toggle-password:active {
  color: var(--color-primary-600);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-1_5);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary-500);
}

.forgot-link {
  font-size: var(--text-sm);
  color: var(--color-primary-500);
  font-weight: var(--font-medium);
  text-decoration: none;
  transition: color var(--duration-fast) var(--ease-out);
}

.forgot-link:hover {
  color: var(--color-primary-600);
}

.submit-btn {
  width: 100%;
  padding: var(--space-4);
  background-color: var(--color-gray-200);
  color: var(--color-text-tertiary);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  font-family: var(--font-sans);
  cursor: not-allowed;
  transition: all var(--duration-normal) var(--ease-out);
  letter-spacing: 4px;
}

.submit-btn.active {
  background: var(--gradient-primary);
  color: white;
  cursor: pointer;
  box-shadow: var(--shadow-green);
}

.submit-btn.active:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-green-lg);
}

.submit-btn.active:active {
  transform: translateY(0);
  box-shadow: var(--shadow-green);
}

.submit-btn:disabled {
  opacity: 0.6;
}

.register-btn {
  width: 100%;
  padding: var(--space-4);
  margin-top: var(--space-3);
  background-color: var(--color-bg-primary);
  color: var(--color-primary-600);
  border: 2px solid var(--color-primary-200);
  border-radius: var(--radius-xl);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-out);
  letter-spacing: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.register-btn:hover:not(:disabled) {
  border-color: var(--color-primary-400);
  background-color: var(--color-primary-50);
  transform: translateY(-1px);
}

.register-btn:active {
  transform: translateY(0);
}

.register-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-message {
  margin-top: var(--space-4);
  padding: var(--space-3);
  background-color: var(--color-rose-50);
  border: 1px solid var(--color-rose-100);
  border-radius: var(--radius-lg);
  color: var(--color-rose-500);
  font-size: var(--text-sm);
  text-align: center;
}

.divider {
  position: relative;
  margin: var(--space-8) 0 var(--space-5);
  text-align: center;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background-color: var(--color-border-light);
}

.divider::before { left: 0; }
.divider::after { right: 0; }

.divider span {
  position: relative;
  padding: 0 var(--space-3);
  background-color: var(--color-bg-primary);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.social-login {
  display: flex;
  justify-content: center;
  gap: var(--space-6);
}

.social-btn {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-out);
  background-color: var(--color-bg-primary);
}

.social-btn:hover {
  border-color: var(--color-primary-300);
  box-shadow: var(--shadow-sm);
  transform: translateY(-2px);
}

.social-btn:active {
  transform: scale(0.95);
}

.social-btn span {
  font-size: 24px;
}

.register-link {
  text-align: center;
  margin-top: var(--space-8);
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.register-link a {
  color: var(--color-primary-500);
  font-weight: var(--font-semibold);
  text-decoration: none;
  transition: color var(--duration-fast) var(--ease-out);
}

.register-link a:hover {
  color: var(--color-primary-600);
}

@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
  }

  .login-brand {
    min-height: auto;
    padding: var(--space-16) var(--space-6) var(--space-10);
  }

  .brand-content {
    padding: var(--space-4);
  }

  .brand-title {
    font-size: 2.25rem;
    letter-spacing: 4px;
  }

  .brand-subtitle {
    font-size: var(--text-base);
  }

  .brand-slogan {
    font-size: var(--text-sm);
  }

  .circle-1 {
    width: 180px;
    height: 180px;
    top: -40px;
    left: -30px;
  }

  .circle-2 {
    width: 120px;
    height: 120px;
    bottom: -20px;
    right: -20px;
  }

  .circle-3 {
    width: 80px;
    height: 80px;
  }

  .circle-4,
  .circle-5 {
    display: none;
  }

  .login-form-side {
    min-height: auto;
    flex: 1;
    padding: var(--space-8) var(--space-5) var(--space-10);
    border-radius: var(--radius-2xl) var(--radius-2xl) 0 0;
    margin-top: calc(-1 * var(--space-6));
    position: relative;
    z-index: 1;
  }

  .login-form {
    animation-delay: 0.05s;
  }

  .form-title {
    font-size: var(--text-2xl);
  }

  .submit-btn {
    padding: var(--space-3) var(--space-4);
    font-size: var(--text-base);
  }

  .register-btn {
    padding: var(--space-3) var(--space-4);
    font-size: var(--text-base);
  }
}
</style>
