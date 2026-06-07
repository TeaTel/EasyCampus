<template>
  <div id="app" class="app-root">
    <ToastProvider />

    <AppHeader v-if="showAppHeader" @toggle-sidebar="showSideMenu = !showSideMenu" />

    <SideMenu :visible="showSideMenu" @close="showSideMenu = false" />

    <main class="main-content" :class="{ 'no-header': !showAppHeader }">
      <div class="content-wrapper">
        <router-view v-slot="{ Component, route: currentRoute }">
          <transition :name="currentRoute.meta.transition || 'page'" mode="out-in">
            <component :is="Component" :key="currentRoute.path" />
          </transition>
        </router-view>
      </div>
    </main>

    <NavBar class="mobile-navbar" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, watch, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from './store/auth'
import { useNotificationStore } from './store/notification'
import { wsManager } from './services/api'
import NavBar from './components/NavBar.vue'
import AppHeader from './components/AppHeader.vue'
import SideMenu from './components/SideMenu.vue'
import ToastProvider from './components/ToastProvider.vue'

const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const showSideMenu = ref(false)

const showTabBar = computed(() => {
  return route.meta.showTabBar === true
})

const showAppHeader = computed(() => {
  return route.meta.showTabBar === true
})

function initWebSocket() {
  if (authStore.isAuthenticated) {
    const token = localStorage.getItem('token')
    if (token && !wsManager.isConnected) {
      wsManager.connect(token)

      wsManager.on('chat_message', (data) => {
        console.log('收到新消息:', data)
      })
    }
  }
}

watch(
  () => authStore.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      initWebSocket()
      notificationStore.startPolling()
    } else {
      wsManager.disconnect()
      notificationStore.stopPolling()
    }
  },
  { immediate: true }
)

onMounted(() => {
  const viewport = document.querySelector('meta[name=viewport]')
  if (!viewport) {
    const meta = document.createElement('meta')
    meta.name = 'viewport'
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover'
    document.head.appendChild(meta)
  }

  initWebSocket()
})

onUnmounted(() => {
  notificationStore.stopPolling()
})
</script>

<style>
@import './assets/css/design-system.css';

.app-root {
  min-height: 100vh;
  background-color: var(--color-bg-page, #f0fdf4);
  position: relative;
}

.main-content {
  min-height: 100vh;
  padding-top: 64px;
  padding-bottom: 0;
  transition: padding 0.3s ease;
}

.main-content.no-header {
  padding-top: 0;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.page-enter-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 详情页左右滑入动画 */
.slide-enter-active {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.3s ease;
}

.slide-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.slide-enter-from {
  transform: translateX(30%);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-20%);
  opacity: 0;
}

.mobile-navbar {
  display: none;
}

@media (max-width: 768px) {
  .main-content {
    padding-top: 52px;
    padding-bottom: calc(56px + env(safe-area-inset-bottom, 0px));
  }

  .main-content.no-header {
    padding-top: 0;
  }

  .mobile-navbar {
    display: block;
  }
}

* {
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
}

body {
  -webkit-user-select: none;
  user-select: none;
  overscroll-behavior-y: contain;
}

input,
textarea {
  -webkit-user-select: auto;
  user-select: auto;
}

::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.15);
  border-radius: 20px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.25);
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

a {
  color: inherit;
  text-decoration: none;
}

button {
  font-family: inherit;
  cursor: pointer;
  border: none;
  background: none;
  padding: 0;
  margin: 0;
}

input,
textarea,
select {
  font-family: inherit;
  font-size: inherit;
}

@media (max-width: 768px) {
  .hide-on-mobile {
    display: none !important;
  }
}

@media (min-width: 769px) {
  .hide-on-desktop {
    display: none !important;
  }
}
</style>
