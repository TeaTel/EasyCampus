<template>
  <div class="post-card" @click="$emit('click')">
    <div class="card-image">
      <img
        v-if="post.coverImage"
        :src="post.coverImage"
        :alt="post.title"
        class="cover-image"
        loading="lazy"
        @error="onImageError"
      />
      <div v-else class="cover-placeholder">
        <p class="placeholder-text">{{ contentSnippet }}</p>
      </div>
      <span v-if="postTypeLabel" class="type-badge" :class="`type-badge--${post.postType?.toLowerCase()}`">
        {{ postTypeLabel }}
      </span>
      <span v-if="post.isAd" class="ad-badge">广告</span>
      <div class="card-shine"></div>
    </div>

    <div class="card-content">
      <h3 class="card-title">{{ post.title }}</h3>

      <div v-if="postTags.length" class="card-tags">
        <span v-for="tag in postTags" :key="tag" class="tag-hashtag">{{ tag }}</span>
      </div>

      <div v-if="post.createdAt" class="card-time">{{ timeAgo }}</div>

      <div class="card-user-bar">
        <div class="user-left" @click.stop="goToUser">
          <img :src="post.userAvatar || defaultAvatar" class="user-avatar" loading="lazy" @error="onAvatarError" />
          <span class="user-name">{{ post.userName || '匿名用户' }}</span>
        </div>
        <div class="like-wrapper" @click.stop>
          <LikeButton
            :is-liked="post.isLiked"
            :count="post.likeCount"
            target-type="POST"
            :target-id="post.id"
            @toggled="onLikeToggled"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import LikeButton from './LikeButton.vue'

const props = defineProps({
  post: { type: Object, required: true }
})

const emit = defineEmits(['click', 'like-toggled'])
const router = useRouter()
const auth = useAuthStore()

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#f0fdf4"/><circle cx="20" cy="15" r="8" fill="#6ee7b7"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#6ee7b7"/></svg>')

const POST_TYPE_MAP = {
  DISCUSSION: '讨论',
  SHOWCASE: '展示',
  HELP: '求助',
  ACTIVITY: '活动'
}

const postTypeLabel = computed(() => {
  return POST_TYPE_MAP[props.post.postType] || ''
})

const postTags = computed(() => {
  if (!props.post.tags) return []
  return props.post.tags.split(',').filter(t => t.trim())
})

const timeAgo = computed(() => {
  if (!props.post.createdAt) return ''
  const diffMs = Date.now() - new Date(props.post.createdAt).getTime()
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMs / 3600000)
  const diffDay = Math.floor(diffMs / 86400000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  if (diffHour < 24) return `${diffHour}小时前`
  if (diffDay < 30) return `${diffDay}天前`
  return new Date(props.post.createdAt).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
})

const contentSnippet = computed(() => {
  const text = (props.post.content || '').replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  return text.length > 120 ? text.slice(0, 120) + '...' : text
})

/* 统一的图片加载失败占位图 */
const BROKEN_IMAGE = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect width="200" height="200" fill="#f3f4f6"/><path d="M60 80h80v60H60z" fill="#e5e7eb"/><circle cx="80" cy="95" r="8" fill="#d1d5db"/><path d="M60 140l30-25 20 15 30-30v40H60z" fill="#d1d5db"/></svg>')

function onAvatarError(e) {
  e.target.src = defaultAvatar
}

function onImageError(e) {
  e.target.src = BROKEN_IMAGE
}

function onLikeToggled(isLiked, count) {
  props.post.isLiked = isLiked
  props.post.likeCount = count
  emit('like-toggled', { isLiked, count })
}

function goToUser() {
  if (props.post.userId) router.push(`/users/${props.post.userId}`)
}
</script>

<style scoped>
.post-card {
  background: var(--color-bg-primary, #ffffff);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  cursor: pointer;
  transition:
    transform var(--duration-normal, 200ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)),
    box-shadow var(--duration-normal, 200ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
  position: relative;
  break-inside: avoid;
  margin-bottom: 8px;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.post-card:active {
  transform: translateY(-1px) scale(0.99);
}

.card-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, var(--color-primary-50, #ecfdf5), var(--color-primary-100, #d1fae5));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform var(--duration-slow, 350ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
}

.post-card:hover .cover-image {
  transform: scale(1.03);
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 160px;
  max-height: 180px;
  padding: 20px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #f0fdf4 0%, #d1fae5 50%, #a7f3d0 100%);
}

.placeholder-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #065f46;
  text-align: center;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: var(--font-semibold, 600);
  border-radius: var(--radius-full, 9999px);
  letter-spacing: 0.03em;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 2;
}

.type-badge--discussion {
  background: rgba(16, 185, 129, 0.85);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.35);
}

.type-badge--showcase {
  background: rgba(139, 92, 246, 0.85);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.35);
}

.type-badge--help {
  background: rgba(249, 115, 22, 0.85);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.35);
}

.type-badge--activity {
  background: rgba(244, 63, 94, 0.85);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(244, 63, 94, 0.35);
}

.ad-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 8px;
  background: rgba(0, 0, 0, 0.5);
  color: rgba(255, 255, 255, 0.9);
  font-size: 10px;
  font-weight: var(--font-medium, 500);
  border-radius: var(--radius-xs, 4px);
  letter-spacing: 0.05em;
  z-index: 2;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.card-shine {
  position: absolute;
  inset: 0;
  background: var(--gradient-card-shine, linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.3) 45%, rgba(255,255,255,0.1) 50%, transparent 55%));
  transform: translateX(-100%);
  transition: none;
  pointer-events: none;
  z-index: 3;
}

.post-card:hover .card-shine {
  animation: cardShine var(--duration-slower, 500ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)) forwards;
}

@keyframes cardShine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.card-content {
  padding: 6px 8px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary, #111827);
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-hashtag {
  font-size: 11px;
  color: var(--color-primary-600, #059669);
  white-space: nowrap;
  cursor: pointer;
}

.card-time {
  font-size: 10px;
  color: var(--color-text-tertiary, #9ca3af);
  padding-top: 1px;
}

.card-user-bar {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  font-size: 11px;
  font-weight: var(--font-medium, 500);
  border-radius: var(--radius-full, 9999px);
  white-space: nowrap;
  line-height: 1.5;
}



.card-user-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
}

.user-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.user-left:hover .user-name {
  color: var(--color-primary-600, #059669);
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--color-gray-100, #f3f4f6);
  flex-shrink: 0;
  border: 1px solid var(--color-primary-100, #d1fae5);
  transition: border-color var(--duration-fast, 120ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
}

.user-left:hover .user-avatar {
  border-color: var(--color-primary-400, #34d399);
}

.user-name {
  font-size: 11px;
  color: var(--color-text-tertiary, #9ca3af);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--duration-fast, 120ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
}

.like-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
}

.like-wrapper :deep(.like-button.liked) {
  animation: heartBeat 1.3s ease-in-out;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  14% { transform: scale(1.3); }
  28% { transform: scale(1); }
  42% { transform: scale(1.3); }
  70% { transform: scale(1); }
}
</style>
