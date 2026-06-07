import { inject } from 'vue'

export function useToast() {
  const toast = inject('toast', {
    showToast: (msg) => {},
    showConfirm: () => Promise.resolve(false)
  })
  return toast
}
