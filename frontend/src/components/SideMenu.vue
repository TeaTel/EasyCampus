<template>
  <teleport to="body">
    <transition name="drawer-fade">
      <div v-if="visible" class="side-overlay" @click="$emit('close')"></div>
    </transition>
    <transition name="drawer-slide">
      <aside v-if="visible" class="side-drawer">
        <div class="drawer-header">
          <div v-if="isAuthenticated" class="user-section" @click="goProfile">
            <img :src="currentUser?.avatar || defaultAvatar" class="user-avatar" @error="onAvatarError" />
            <div class="user-info">
              <span class="user-name">{{ currentUser?.nickname || currentUser?.username || '用户' }}</span>
              <span class="user-sub">{{ currentUser?.school || '校园集市' }}</span>
            </div>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div v-else class="user-section-guest">
            <div class="guest-avatar">
              <svg viewBox="0 0 40 40" width="40" height="40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>
            </div>
            <div class="user-info">
              <span class="user-name">未登录</span>
              <span class="user-sub">登录享受更多功能</span>
            </div>
            <button class="guest-login-btn" @click="goLogin">登录</button>
          </div>
        </div>

        <div v-if="isAuthenticated" class="stats-row">
          <div class="stat-item" @click="goRoute('/my-products')">
            <span class="stat-num">{{ stats.published }}</span>
            <span class="stat-lbl">发布</span>
          </div>
          <div class="stat-item" @click="goRoute('/favorites')">
            <span class="stat-num">{{ stats.favorites }}</span>
            <span class="stat-lbl">收藏</span>
          </div>
        </div>

        <nav v-if="isAuthenticated" class="menu-list">
          <div class="menu-item highlight" @click="handlePublishClick">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#fff" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </span>
            <span class="menu-text">发布</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/my-products')">
            <span class="menu-icon">📦</span>
            <span class="menu-text">商品与帖子</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/favorites')">
            <span class="menu-icon">❤️</span>
            <span class="menu-text">我的收藏</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/notifications')">
            <span class="menu-icon">🔔</span>
            <span class="menu-text">通知</span>
            <span v-if="notificationUnreadCount > 0" class="menu-badge">{{ notificationUnreadCount }}</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/my-activities')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </span>
            <span class="menu-text">我的活动</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>

          <div class="menu-divider"><span>更多功能</span></div>

          <div class="menu-item" @click="goRoute('/orgs/my')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
            </span>
            <span class="menu-text">我的组织</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item new-feature" @click="goRoute('/ads/create')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="9" x2="15" y2="9"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="12" y2="17"/></svg>
            </span>
            <span class="menu-text">我的广告</span>
            <span class="menu-tag hot">HOT</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>

          <div class="menu-divider"></div>

          <div class="menu-item" @click="goRoute('/settings')">
            <span class="menu-icon">⚙️</span>
            <span class="menu-text">账号设置</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="handleAbout">
            <span class="menu-icon">ℹ️</span>
            <span class="menu-text">关于我们</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
        </nav>

        <nav v-else class="menu-list guest-menu">
          <div class="guest-info-card">
            <p class="guest-info-text">注册后即可发布商品、加入组织、收藏好物等</p>
            <button class="guest-register-btn" @click="goLogin">登录</button>
          </div>
          <div class="menu-item" @click="handleAbout">
            <span class="menu-icon">ℹ️</span>
            <span class="menu-text">关于我们</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
        </nav>

        <div v-if="isAuthenticated" class="drawer-footer">
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </aside>
    </transition>

    <PublishActionSheet :visible="showPublishSheet" @close="showPublishSheet = false" />
  </teleport>
</template>

<script setup>
import { reactive, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useNotificationStore } from '../store/notification'
import { productApi, favoriteApi, postApi } from '../services/api'
import { useToast } from '../use/useToast'
import PublishActionSheet from './PublishActionSheet.vue'

const props = defineProps({ visible: { type: Boolean, default: false } })
const emit = defineEmits(['close'])

const router = useRouter()
const store = useAuthStore()
const toast = useToast()
const notificationStore = useNotificationStore()

const isAuthenticated = computed(() => store.isAuthenticated.value)
const currentUser = computed(() => store.currentUser.value)
const notificationUnreadCount = computed(() => notificationStore.notificationUnreadCount.value)

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const stats = reactive({ published: 0, favorites: 0 })
const showPublishSheet = ref(false)

watch(() => props.visible, async (newVal) => {
  if (newVal && isAuthenticated.value) {
    await fetchStats()
  }
})

async function fetchStats() {
  try {
    const [myProductsRes, favCountRes, myPostsRes] = await Promise.allSettled([
      productApi.getMyProducts(),
      favoriteApi.getFavoriteCount(),
      postApi.getUserPosts(currentUser.value?.id)
    ])
    if (myProductsRes.status === 'fulfilled' && myProductsRes.value?.code === 200) {
      stats.published = (myProductsRes.value.data || []).length
    }
    // 统计用户发布的帖子数量，累加到发布数
    if (myPostsRes.status === 'fulfilled' && myPostsRes.value?.code === 200) {
      stats.published += (myPostsRes.value.data || []).length
    }
    if (favCountRes.status === 'fulfilled' && favCountRes.value?.code === 200) {
      stats.favorites = favCountRes.value.data || 0
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

function handlePublishClick() { emit('close'); showPublishSheet.value = true }
function goProfile() { emit('close'); router.push('/profile') }
function goLogin() { emit('close'); router.push('/login') }
function goRoute(path) { emit('close'); router.push(path) }
function handleAbout() { emit('close'); toast.showToast('校园二手交易平台 v2.0 —— 让每一件闲置都有归宿') }
function handleLogout() { emit('close'); store.logout() }
function onAvatarError(e) { e.target.src = defaultAvatar }
</script>

<style scoped>
.side-overlay {
  position: fixed; inset: 0; z-index: 2000;
  background: rgba(0,0,0,0.45);
}
.side-drawer {
  position: fixed; top: 0; left: 0; bottom: 0; z-index: 2001;
  width: 300px; max-width: 85vw;
  background: #FFFFFF;
  display: flex; flex-direction: column;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.drawer-header {
  padding: 20px 16px 12px;
  background: linear-gradient(135deg, #FFF7E6, #FFE7BA);
}
.user-section {
  display: flex; align-items: center; gap: 12px;
  cursor: pointer; border-radius: 8px; padding: 4px;
  transition: background 0.15s;
}
.user-section:active { background: rgba(16,185,129,0.08); }
.user-avatar {
  width: 52px; height: 52px; border-radius: 50%;
  object-fit: cover; background: #eee; flex-shrink: 0;
}
.user-info { flex: 1; min-width: 0; }
.user-name { display: block; font-size: 17px; font-weight: 700; color: #333; }
.user-sub { display: block; font-size: 12px; color: #999; margin-top: 2px; }
.user-section-guest {
  display: flex; align-items: center; gap: 12px;
}
.guest-avatar {
  width: 52px; height: 52px; border-radius: 50%; background: #eee; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.guest-login-btn {
  padding: 8px 20px; border-radius: 16px; border: none;
  background: linear-gradient(135deg, var(--color-primary-500, #10b981), var(--color-primary-400, #34d399));
  color: #fff; font-size: 13px; font-weight: 600; cursor: pointer;
}
.guest-login-btn:active { transform: scale(0.96); }
.stats-row {
  display: flex; align-items: center; padding: 12px 16px;
  background: #fff; border-bottom: 1px solid #F5F7FA;
}
.stat-item {
  flex: 1; text-align: center; cursor: pointer;
  padding: 4px 0; border-radius: 8px; transition: background 0.15s;
}
.stat-item:active { background: #f5f5f5; }
.stat-num { display: block; font-size: 18px; font-weight: 700; color: #333; }
.stat-lbl { display: block; font-size: 11px; color: #999; margin-top: 2px; }
.menu-list { flex: 1; padding: 8px 0; }
.menu-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 20px; cursor: pointer;
  transition: background 0.15s; border: none; background: none; width: 100%; text-align: left;
  font-size: 15px; color: #333; text-decoration: none;
}
.menu-item:active { background: #f5f5f5; }
.menu-item.highlight { background: linear-gradient(135deg, #FFF7E6, #FFF0D9); margin: 0 12px 4px; border-radius: 10px; padding: 12px 16px; }
.menu-item.highlight .menu-text { color: var(--color-primary-500, #10b981); font-weight: 600; }
.menu-item.highlight .menu-icon {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-500, #10b981), var(--color-primary-400, #34d399)); border-radius: 50%;
}
.menu-icon { font-size: 20px; flex-shrink: 0; width: 24px; text-align: center; }
.menu-text { flex: 1; font-size: 15px; color: #333; }
.menu-badge {
  background: #FF4D4F; color: #fff; font-size: 11px; font-weight: 600;
  padding: 2px 7px; border-radius: 8px; margin-right: -4px;
}
.menu-tag {
  font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; margin-right: -4px;
}
.menu-tag.new { background: #E8F4FD; color: #1890FF; }
.menu-tag.hot { background: #FFF1F0; color: #FF4D4F; }
.menu-divider {
  display: flex; align-items: center; padding: 8px 20px;
  color: #ccc; font-size: 12px;
}
.menu-divider::before, .menu-divider::after { content: ''; flex: 1; height: 1px; background: #E8ECF0; margin: 0 10px; }
.new-feature .menu-text { color: #555; }

.guest-menu { padding-top: 16px; }
.guest-info-card {
  margin: 0 16px 12px; padding: 20px 16px;
  background: linear-gradient(135deg, #FFF7E6, #FFE7BA);
  border-radius: 12px; text-align: center;
}
.guest-info-text { font-size: 13px; color: #999; margin: 0 0 14px; line-height: 1.5; }
.guest-register-btn {
  padding: 10px 32px; border-radius: 20px; border: none;
  background: linear-gradient(135deg, var(--color-primary-500, #10b981), var(--color-primary-400, #34d399));
  color: #fff; font-size: 14px; font-weight: 600; cursor: pointer;
}
.guest-register-btn:active { transform: scale(0.96); }

.drawer-footer {
  padding: 16px 20px; border-top: 1px solid #F5F7FA;
}
.logout-btn {
  width: 100%; padding: 12px; border: 1px solid #E8ECF0; border-radius: 8px;
  background: #fff; color: #999; font-size: 14px; font-weight: 500; cursor: pointer;
  transition: all 0.15s;
}
.logout-btn:active { background: #FFF1F0; color: #FF4D4F; border-color: #FFCCC7; }

.drawer-fade-enter-active, .drawer-fade-leave-active { transition: opacity 0.25s ease; }
.drawer-fade-enter-from, .drawer-fade-leave-to { opacity: 0; }
.drawer-slide-enter-active, .drawer-slide-leave-active { transition: transform 0.25s cubic-bezier(0.4,0,0.2,1); }
.drawer-slide-enter-from, .drawer-slide-leave-to { transform: translateX(-100%); }

@media (min-width: 768px) {
  .side-drawer { width: 360px; }
}
</style>
