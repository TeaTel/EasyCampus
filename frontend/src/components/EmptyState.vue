<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <h3 v-if="title" class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <button v-if="actionText" class="empty-action" @click="$emit('action')">{{ actionText }}</button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '📭' },
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' }
})

defineEmits(['action'])
</script>

<style scoped>
.empty-state { text-align: center; padding: 64px 20px; }
.empty-icon { font-size: 56px; margin-bottom: 16px; opacity: 0.7; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--color-text-secondary, #666); margin: 0 0 8px; }
.empty-desc { font-size: 13px; color: var(--color-text-tertiary, #999); margin: 0 0 16px; }
.empty-action { padding: 8px 24px; border-radius: 20px; border: none; background: var(--color-primary-500, #10b981); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.empty-action:active { opacity: 0.85; }
</style>
