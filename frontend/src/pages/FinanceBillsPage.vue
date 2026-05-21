<template>
  <div class="page-container">
    <div class="page-header">
      <h3>账单列表</h3>
    </div>

    <div class="page-card">
      <div class="search-bar">
        <el-select v-model="query.type" placeholder="账单类型" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="收入" value="INCOME" />
          <el-option label="支出" value="EXPENSE" />
        </el-select>
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <div class="summary-bar">
        <div class="summary-item">
          <span class="summary-label">总收入：</span>
          <span class="summary-value income">+¥{{ Number(summary.totalIncome || 0).toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">总支出：</span>
          <span class="summary-value expense">-¥{{ Number(summary.totalExpense || 0).toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">净收入：</span>
          <span class="summary-value" :class="netIncome >= 0 ? 'income' : 'expense'">
            {{ netIncome >= 0 ? '+' : '' }}¥{{ netIncome.toFixed(2) }}
          </span>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="日期" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <span :class="row.type === 'INCOME' ? 'text-income' : 'text-expense'">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="130">
          <template #default="{ row }">
            <span :class="row.type === 'INCOME' ? 'text-income' : 'text-expense'">
              {{ row.type === 'INCOME' ? '+' : '-' }}¥{{ Number(row.amount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="关联单号" width="120">
          <template #default="{ row }">
            <el-button v-if="row.relatedId" link type="primary" @click="$router.push('/purchase/detail/' + row.relatedId)">
              #{{ row.relatedId }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getBills } from '../api/finance'

const loading = ref(false)
const tableData = ref([])
const summary = ref({ totalIncome: 0, totalExpense: 0, netIncome: 0 })
const query = ref({ type: '', dateRange: null })

const netIncome = computed(() => {
  return Number(summary.value.totalIncome || 0) - Number(summary.value.totalExpense || 0)
})

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (query.value.type) params.type = query.value.type
    if (query.value.dateRange) {
      params.startDate = query.value.dateRange[0]
      params.endDate = query.value.dateRange[1]
    }
    const data = await getBills(params)
    tableData.value = data.bills || []
    summary.value = {
      totalIncome: data.totalIncome,
      totalExpense: data.totalExpense,
      netIncome: data.netIncome
    }
  } catch {} finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { type: '', dateRange: null }
  loadData()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
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
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.summary-bar {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #F7F8FA;
  border-radius: 6px;
}

.summary-label { color: #86909C; font-size: 14px; }
.summary-value { font-size: 16px; font-weight: 600; }
.income { color: #00B42A; }
.expense { color: #F53F3F; }
.text-income { color: #00B42A; font-weight: 500; }
.text-expense { color: #F53F3F; font-weight: 500; }
</style>
