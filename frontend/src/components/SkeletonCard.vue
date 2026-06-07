<template>
  <div class="skeleton-card">
    <div class="skeleton-image"></div>
    <div class="skeleton-content">
      <div class="skeleton-line" :style="{ width: '80%' }"></div>
      <div class="skeleton-line" :style="{ width: '60%' }"></div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-card { background: #fff; border-radius: 12px; overflow: hidden; break-inside: avoid; margin-bottom: 8px; }
.skeleton-image { width: 100%; aspect-ratio: 1; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.skeleton-content { padding: 8px 10px; }
.skeleton-line { height: 14px; border-radius: 4px; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; margin-bottom: 6px; }
.skeleton-line:last-child { margin-bottom: 0; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
</style>
