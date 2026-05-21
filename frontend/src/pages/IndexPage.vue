<template>
  <div class="index-page">
    <div class="welcome-section">
      <div>
        <h2>欢迎回来，{{ username }}！</h2>
        <p class="date-text">今天是 {{ todayText }}，祝您工作愉快！</p>
      </div>
      <div class="time-display">{{ currentTime }}</div>
    </div>

    <div class="stats-row">
      <div class="stat-card" v-for="(item, i) in statCards" :key="i">
        <div class="stat-icon" :style="{ background: item.bg }">
          <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <div class="quick-row">
      <div class="quick-card" @click="$router.push('/book')">
        <el-icon :size="48" color="#165DFF"><Collection /></el-icon>
        <h3>图书管理</h3>
        <p>管理库存图书信息</p>
      </div>
      <div class="quick-card" @click="$router.push('/purchase')">
        <el-icon :size="48" color="#00B42A"><ShoppingCart /></el-icon>
        <h3>进货管理</h3>
        <p>管理图书进货记录</p>
      </div>
      <div class="quick-card" @click="$router.push('/finance/bills')">
        <el-icon :size="48" color="#FF7D00"><Money /></el-icon>
        <h3>财务管理</h3>
        <p>查看财务账单报表</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getStats } from '../api/dashboard'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const username = computed(() => user.username || '用户')

const stats = ref({ totalBooks: 0, todayPurchases: 0, monthIncome: 0, pendingPurchases: 0 })
const currentTime = ref('')

let timer = null

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
}

const todayText = computed(() => {
  const now = new Date()
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${days[now.getDay()]}`
})

const statCards = computed(() => [
  { icon: 'Collection', label: '总图书数', value: stats.value.totalBooks + '本', color: '#165DFF', bg: '#E8F3FF' },
  { icon: 'ShoppingCart', label: '今日进货', value: stats.value.todayPurchases + '批', color: '#00B42A', bg: '#E8FFEA' },
  { icon: 'Money', label: '本月收入', value: '¥' + Number(stats.value.monthIncome || 0).toLocaleString(), color: '#FF7D00', bg: '#FFF7E8' },
  { icon: 'Clock', label: '待处理', value: stats.value.pendingPurchases + '笔', color: '#F53F3F', bg: '#FFECE8' }
])

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  try {
    stats.value = await getStats()
  } catch {}
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.index-page {
  animation: pageIn 0.3s ease-out;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.welcome-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1D2129;
}

.date-text {
  font-size: 14px;
  color: #86909C;
  margin-top: 4px;
}

.time-display {
  font-size: 28px;
  font-weight: 600;
  color: #165DFF;
  font-variant-numeric: tabular-nums;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease-in-out;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
}

.stat-label {
  font-size: 13px;
  color: #86909C;
  margin-top: 2px;
}

.quick-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.quick-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease-in-out;
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.quick-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 12px 0 4px;
  color: #1D2129;
}

.quick-card p {
  font-size: 13px;
  color: #86909C;
}
</style>
