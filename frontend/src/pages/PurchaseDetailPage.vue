<template>
  <div class="page-container">
    <div class="page-header">
      <h3>进货详情</h3>
    </div>

    <div class="page-card" v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="进货ID">{{ purchase.id }}</el-descriptions-item>
        <el-descriptions-item label="图书">{{ purchase.bookTitle }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ purchase.bookIsbn }}</el-descriptions-item>
        <el-descriptions-item label="进货价格">¥{{ Number(purchase.purchasePrice || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="进货数量">{{ purchase.quantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="price">¥{{ (Number(purchase.purchasePrice || 0) * (purchase.quantity || 0)).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(purchase.status)" size="small">{{ statusText(purchase.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ purchase.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(purchase.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(purchase.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <div class="action-bar">
        <template v-if="purchase.status === 'UNPAID'">
          <el-button type="success" @click="handlePay">付款</el-button>
          <el-button type="warning" @click="handleReturn">退货</el-button>
        </template>
        <el-button @click="$router.push('/purchase')">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchase, payPurchase, returnPurchase } from '../api/purchase'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const purchase = ref({})

async function loadData() {
  loading.value = true
  try {
    purchase.value = await getPurchase(route.params.id)
  } catch {} finally {
    loading.value = false
  }
}

function statusText(s) {
  return { UNPAID: '未付款', PAID: '已付款', RETURNED: '已退货' }[s] || s
}

function statusType(s) {
  return { UNPAID: 'warning', PAID: 'success', RETURNED: 'info' }[s] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

async function handlePay() {
  await ElMessageBox.confirm('确定付款？', '付款确认', { type: 'warning' })
  await payPurchase(route.params.id)
  ElMessage.success('付款成功')
  loadData()
}

async function handleReturn() {
  await ElMessageBox.confirm('确定退货？', '退货确认', { type: 'warning' })
  await returnPurchase(route.params.id)
  ElMessage.success('退货成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { animation: pageIn 0.3s ease-out; }
@keyframes pageIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.page-header { margin-bottom: 16px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: #1D2129; }
.page-card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.price { color: #F53F3F; font-weight: 600; font-size: 16px; }
.action-bar { margin-top: 24px; display: flex; gap: 12px; }
</style>
