<template>
  <div class="product-card" @click="$emit('click')">
    <div class="card-image">
      <img
        v-if="product.coverImage"
        :src="product.coverImage"
        :alt="product.title"
        class="cover-image"
        loading="lazy"
        @error="onImageError"
      />
      <div v-else class="cover-placeholder">
        <p class="placeholder-text">{{ descriptionSnippet }}</p>
      </div>

      <span v-if="condition" class="condition-badge">{{ condition }}</span>

      <div class="chat-overlay">
        <button class="chat-btn" @click.stop="onChatClick">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          聊一聊
        </button>
      </div>
    </div>

    <div class="card-body">
      <div class="price-row">
        <span class="price-current">¥{{ formatPrice(product.price) }}</span>
        <span v-if="hasOriginalPrice" class="price-original">¥{{ formatPrice(product.originalPrice) }}</span>
        <span v-if="discountPercent" class="discount-badge">-{{ discountPercent }}%</span>
      </div>

      <h3 class="card-title">{{ product.title || product.name || '闲置好物' }}</h3>

      <div v-if="productTags.length" class="card-tags">
        <span v-for="tag in productTags" :key="tag" class="tag-hashtag">{{ tag }}</span>
      </div>

      <div v-if="product.createdAt" class="card-time">{{ timeAgo }}</div>

      <div class="card-user-bar">
        <div class="user-left" @click.stop="goToUser">
          <img :src="product.userAvatar || product.sellerAvatar || defaultAvatar" class="user-avatar" loading="lazy" @error="onAvatarError" />
          <span class="user-name">{{ product.userName || product.sellerName || '校园卖家' }}</span>
        </div>
        <div class="like-wrapper" @click.stop>
          <LikeButton
            :is-liked="product.isLiked"
            :count="product.likeCount"
            target-type="PRODUCT"
            :target-id="product.id"
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
import LikeButton from './LikeButton.vue'

const props = defineProps({
  product: { type: Object, required: true }
})

const emit = defineEmits(['click', 'like-toggled'])
const router = useRouter()

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const productTags = computed(() => {
  if (!props.product.tags) return []
  return props.product.tags.split(',').filter(t => t.trim())
})

const hasOriginalPrice = computed(() => {
  const op = Number(props.product.originalPrice)
  const p = Number(props.product.price)
  return op > 0 && op > p
})

const discountPercent = computed(() => {
  if (!hasOriginalPrice.value) return null
  const op = Number(props.product.originalPrice)
  const p = Number(props.product.price)
  const percent = Math.round((1 - p / op) * 100)
  return percent > 0 ? percent : null
})

const condition = computed(() => {
  return props.product.condition || ''
})

const timeAgo = computed(() => {
  if (!props.product.createdAt) return ''
  const diffMs = Date.now() - new Date(props.product.createdAt).getTime()
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMs / 3600000)
  const diffDay = Math.floor(diffMs / 86400000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  if (diffHour < 24) return `${diffHour}小时前`
  if (diffDay < 30) return `${diffDay}天前`
  return new Date(props.product.createdAt).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
})

const descriptionSnippet = computed(() => {
  const text = (props.product.description || '').replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  if (!text) return '暂无描述'
  return text.length > 100 ? text.slice(0, 100) + '...' : text
})

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

/* 统一的图片加载失败占位图 */
const BROKEN_IMAGE = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect width="200" height="200" fill="#f3f4f6"/><path d="M60 80h80v60H60z" fill="#e5e7eb"/><circle cx="80" cy="95" r="8" fill="#d1d5db"/><path d="M60 140l30-25 20 15 30-30v40H60z" fill="#d1d5db"/></svg>')

function onAvatarError(e) {
  e.target.src = defaultAvatar
}

function onImageError(e) {
  e.target.src = BROKEN_IMAGE
}

function onLikeToggled(isLiked, count) {
  props.product.isLiked = isLiked
  props.product.likeCount = count
  emit('like-toggled', { isLiked, count })
}

function onChatClick() {
  const sellerId = props.product.sellerId
  if (sellerId) {
    router.push(`/chat/${sellerId}`)
  }
}

function goToUser() {
  const sellerId = props.product.sellerId
  if (sellerId) router.push(`/users/${sellerId}`)
}
</script>

<style scoped>
.product-card {
  position: relative;
  background: var(--color-bg-primary);
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform var(--duration-normal) var(--ease-out),
              box-shadow var(--duration-normal) var(--ease-out);
  break-inside: avoid;
  margin-bottom: 8px;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.product-card:active {
  transform: translateY(-2px) scale(0.99);
}

.card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, var(--color-gray-100), var(--color-gray-200));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--duration-slow) var(--ease-out);
}

.product-card:hover .cover-image {
  transform: scale(1.05);
}

.cover-placeholder {
  color: var(--color-gray-300);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 120px;
  padding: 16px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 50%, #fed7aa 100%);
}

.placeholder-text {
  margin: 0;
  font-size: 11px;
  line-height: 1.6;
  color: #9a3412;
  text-align: center;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.condition-badge {
  position: absolute;
  top: var(--space-2_5);
  left: var(--space-2_5);
  padding: var(--space-0_5) var(--space-2);
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: var(--color-text-inverse);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-sm);
  line-height: 1.4;
  z-index: 1;
}

.chat-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  padding-bottom: var(--space-3);
  opacity: 0;
  transform: translateY(8px);
  transition: opacity var(--duration-normal) var(--ease-out),
              transform var(--duration-normal) var(--ease-out);
  z-index: 1;
}

.product-card:hover .chat-overlay {
  opacity: 1;
  transform: translateY(0);
}

.chat-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1_5);
  padding: var(--space-1_5) var(--space-4);
  background: var(--gradient-primary);
  color: var(--color-text-inverse);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  box-shadow: var(--shadow-green);
  transition: transform var(--duration-fast) var(--ease-spring),
              box-shadow var(--duration-fast) var(--ease-out);
}

.chat-btn:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-green-lg);
}

.chat-btn:active {
  transform: scale(0.97);
}

.card-body {
  padding: 6px 8px 8px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 2px;
}

.price-current {
  font-size: 15px;
  font-weight: 800;
  color: #ef4444;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.price-original {
  font-size: var(--text-xs);
  color: var(--color-gray-400);
  text-decoration: line-through;
  line-height: 1;
}

.discount-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  border-radius: 6px;
  line-height: 1.2;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.4);
  letter-spacing: -0.5px;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
  margin: 0 0 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
  color: var(--color-gray-400, #9ca3af);
  padding-top: 1px;
}

.card-user-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  padding-top: 4px;
  min-height: 24px;
}

.user-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--color-gray-100);
  flex-shrink: 0;
  border: 1px solid var(--color-gray-100);
}

.user-name {
  font-size: 11px;
  color: var(--color-text-tertiary, #9ca3af);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--duration-fast) var(--ease-out);
}

.user-left:hover .user-name {
  color: var(--color-primary-500);
}

.like-wrapper {
  flex-shrink: 0;
}
</style>
