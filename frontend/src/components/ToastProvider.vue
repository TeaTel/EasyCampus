<template>
  <div class="toast-provider">
    <teleport to="body">
      <transition-group name="toast-slide" tag="div" class="toast-stack">
        <div v-for="t in toasts" :key="t.id" class="toast-item" :class="t.type">
          <span>{{ t.message }}</span>
        </div>
      </transition-group>

      <transition name="confirm-fade">
        <div v-if="confirmState.visible" class="confirm-overlay" @click.self="cancelConfirm">
          <div class="confirm-box">
            <p class="confirm-msg">{{ confirmState.message }}</p>
            <div class="confirm-actions">
              <button class="confirm-btn cancel" @click="cancelConfirm">取消</button>
              <button class="confirm-btn ok" @click="okConfirm">确定</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, provide } from 'vue'

let toastId = 0
const toasts = ref([])

const confirmState = reactive({ visible: false, message: '', resolve: null })

function showToast(message, type = 'info', duration = 2000) {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx > -1) toasts.value.splice(idx, 1)
  }, duration)
}

function showConfirm(message) {
  return new Promise((resolve) => {
    Object.assign(confirmState, { visible: true, message, resolve })
  })
}

function okConfirm() {
  confirmState.visible = false
  if (confirmState.resolve) confirmState.resolve(true)
}

function cancelConfirm() {
  confirmState.visible = false
  if (confirmState.resolve) confirmState.resolve(false)
}

provide('toast', { showToast, showConfirm })
</script>

<style scoped>
.toast-stack {
  position: fixed; top: 80px; left: 50%; transform: translateX(-50%); z-index: 9999;
  display: flex; flex-direction: column; align-items: center; gap: 8px; pointer-events: none;
}
.toast-item {
  padding: 10px 24px; border-radius: 8px; font-size: 14px; font-weight: 500; color: #fff;
  background: rgba(0,0,0,0.78); backdrop-filter: blur(8px);
  pointer-events: auto; max-width: 80vw; text-align: center;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
.toast-item.error { background: rgba(255,77,79,0.9); }
.toast-item.success { background: rgba(82,196,26,0.9); }

.toast-slide-enter-active { transition: all 0.3s ease; }
.toast-slide-leave-active { transition: all 0.25s ease; }
.toast-slide-enter-from { opacity: 0; transform: translateY(-16px); }
.toast-slide-leave-to { opacity: 0; transform: translateY(-8px); }

.confirm-overlay {
  position: fixed; inset: 0; z-index: 10000;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.45); padding: 16px;
}
.confirm-box {
  background: #fff; border-radius: 16px; width: 100%; max-width: 300px;
  padding: 28px 24px 20px; box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  text-align: center;
}
.confirm-msg { font-size: 16px; color: #333; margin: 0 0 24px; line-height: 1.5; }
.confirm-actions { display: flex; gap: 12px; }
.confirm-btn { flex: 1; padding: 12px 0; border-radius: 10px; border: none; font-size: 15px; font-weight: 600; cursor: pointer; transition: transform 0.15s; }
.confirm-btn:active { transform: scale(0.96); }
.confirm-btn.cancel { background: #f5f5f5; color: #666; }
.confirm-btn.ok { background: linear-gradient(135deg,var(--color-primary-500, #10b981),var(--color-primary-400, #34d399)); color: #fff; }

.confirm-fade-enter-active { transition: opacity 0.2s ease; }
.confirm-fade-leave-active { transition: opacity 0.15s ease; }
.confirm-fade-enter-from, .confirm-fade-leave-to { opacity: 0; }
</style>
