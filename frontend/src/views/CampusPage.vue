<template>
  <div class="campus-page">
    <header class="page-header">
      <button class="back-btn" @click="router.back()">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#333" stroke-width="2"><polyline points="15,18 9,12 15,6"/></svg>
      </button>
      <h1 class="page-title">我的校区</h1>
      <span class="header-spacer"></span>
    </header>

    <main class="campus-content">
      <!-- 当前校区信息 -->
      <section class="current-campus" v-if="currentSchool || currentCampus">
        <div class="info-card">
          <div class="info-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#FF6A00" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9,22 9,12 15,12 15,22"/></svg>
          </div>
          <div class="info-detail">
            <span class="info-school">{{ currentSchool || '未设置' }}</span>
            <span class="info-campus">{{ currentCampus || '未选择校区' }}</span>
          </div>
        </div>
      </section>

      <!-- 学校选择 -->
      <section class="section">
        <h2 class="section-title">选择学校</h2>
        <div class="school-list">
          <div
            class="school-item"
            :class="{ active: selectedSchool === '韩山师范学院' }"
            @click="selectSchool('韩山师范学院')"
          >
            <div class="school-icon">🏫</div>
            <span class="school-name">韩山师范学院</span>
            <svg v-if="selectedSchool === '韩山师范学院'" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#FF6A00" stroke-width="2.5"><polyline points="20,6 9,17 4,12"/></svg>
          </div>
        </div>
      </section>

      <!-- 校区选择 -->
      <section class="section" v-if="selectedSchool">
        <h2 class="section-title">选择校区</h2>
        <div class="campus-grid">
          <div
            v-for="campus in campusOptions"
            :key="campus"
            class="campus-item"
            :class="{ active: selectedCampus === campus }"
            @click="selectCampus(campus)"
          >
            <div class="campus-dot" :class="{ filled: selectedCampus === campus }"></div>
            <span class="campus-name">{{ campus }}</span>
          </div>
        </div>
      </section>

      <!-- 保存按钮 -->
      <button
        class="save-btn"
        :disabled="!selectedSchool || !selectedCampus || saving"
        @click="saveCampus"
      >
        {{ saving ? '保存中...' : '保存设置' }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { userApi } from '../services/api'
import { useToast } from '../use/useToast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const campusOptions = ['南三区', '南二区', '南一区', '中区', '东区', '西区']

const currentSchool = ref('')
const currentCampus = ref('')
const selectedSchool = ref('')
const selectedCampus = ref('')
const saving = ref(false)

onMounted(async () => {
  const user = authStore.currentUser.value
  if (user) {
    currentSchool.value = user.school || ''
    currentCampus.value = user.campus || ''
    selectedSchool.value = currentSchool.value || '韩山师范学院'
    selectedCampus.value = currentCampus.value
  }
})

function selectSchool(school) {
  selectedSchool.value = school
}

function selectCampus(campus) {
  selectedCampus.value = campus
}

async function saveCampus() {
  if (!selectedSchool.value || !selectedCampus.value) return
  saving.value = true
  try {
    const res = await userApi.updateProfile({
      school: selectedSchool.value,
      campus: selectedCampus.value
    })
    if (res.code === 200) {
      currentSchool.value = selectedSchool.value
      currentCampus.value = selectedCampus.value
      await authStore.fetchUserInfo()
      toast.showToast('校区设置成功')
    } else {
      toast.showToast(res.message || '保存失败')
    }
  } catch (e) {
    toast.showToast('保存失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.campus-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 50%;
}

.back-btn:active {
  background: #f5f5f5;
}

.page-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-spacer {
  width: 36px;
}

.campus-content {
  padding: 16px;
}

/* 当前校区信息 */
.current-campus {
  margin-bottom: 20px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  background: linear-gradient(135deg, #FFF7E6, #FFE7BA);
  border-radius: 14px;
}

.info-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 12px;
  flex-shrink: 0;
}

.info-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-school {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.info-campus {
  font-size: 13px;
  color: #999;
}

/* 区块 */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

/* 学校列表 */
.school-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.school-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.15s;
}

.school-item.active {
  border-color: var(--color-primary-500, #10b981);
  background: #FFFBF5;
}

.school-item:active {
  transform: scale(0.98);
}

.school-icon {
  font-size: 28px;
}

.school-name {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

/* 校区网格 */
.campus-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.campus-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.15s;
}

.campus-item.active {
  border-color: var(--color-primary-500, #10b981);
  background: #FFFBF5;
}

.campus-item:active {
  transform: scale(0.96);
}

.campus-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #ddd;
  flex-shrink: 0;
  transition: all 0.15s;
}

.campus-dot.filled {
  border-color: var(--color-primary-500, #10b981);
  background: var(--color-primary-500, #10b981);
  box-shadow: inset 0 0 0 3px #fff;
}

.campus-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 保存按钮 */
.save-btn {
  display: block;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-500, #10b981), var(--color-primary-400, #34d399));
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: all 0.15s;
}

.save-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (min-width: 769px) {
  .campus-page {
    max-width: 750px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
    background: #fff;
  }
}
</style>
