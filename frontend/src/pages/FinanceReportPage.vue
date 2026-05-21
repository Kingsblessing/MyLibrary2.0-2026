<template>
  <div class="page-container">
    <div class="page-header">
      <h3>财务报表</h3>
      <div class="year-picker">
        <el-select v-model="year" @change="loadData" style="width: 120px">
          <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
        </el-select>
      </div>
    </div>

    <div class="page-card">
      <div class="summary-row">
        <div class="summary-card income-card">
          <div class="summary-label">总收入</div>
          <div class="summary-value income">¥{{ Number(report.totalIncome || 0).toLocaleString() }}</div>
        </div>
        <div class="summary-card expense-card">
          <div class="summary-label">总支出</div>
          <div class="summary-value expense">¥{{ Number(report.totalExpense || 0).toLocaleString() }}</div>
        </div>
        <div class="summary-card net-card">
          <div class="summary-label">净收入</div>
          <div class="summary-value" :class="netIncome >= 0 ? 'income' : 'expense'">
            {{ netIncome >= 0 ? '+' : '' }}¥{{ netIncome.toLocaleString() }}
          </div>
        </div>
      </div>

      <div class="chart-section">
        <h4>收支趋势图</h4>
        <div ref="chartRef" class="chart-container"></div>
      </div>

      <div class="table-section">
        <h4>收支统计</h4>
        <el-table :data="report.monthly || []" stripe border show-summary :summary-method="getSummary">
          <el-table-column prop="month" label="月份" width="100">
            <template #default="{ row }">{{ row.month }}月</template>
          </el-table-column>
          <el-table-column label="收入" width="140">
            <template #default="{ row }">
              <span class="text-income">¥{{ Number(row.income).toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column label="支出" width="140">
            <template #default="{ row }">
              <span class="text-expense">¥{{ Number(row.expense).toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column label="净收入" width="140">
            <template #default="{ row }">
              <span :class="Number(row.netIncome) >= 0 ? 'text-income' : 'text-expense'">
                {{ Number(row.netIncome) >= 0 ? '+' : '' }}¥{{ Number(row.netIncome).toLocaleString() }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="利润率" width="120">
            <template #default="{ row }">
              {{ row.profitRate }}%
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getReport } from '../api/finance'

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 5 }, (_, i) => currentYear - i)
const year = ref(currentYear)
const report = ref({})
const chartRef = ref(null)
let chart = null

const netIncome = computed(() => {
  return Number(report.value.totalIncome || 0) - Number(report.value.totalExpense || 0)
})

async function loadData() {
  try {
    report.value = await getReport({ year: year.value })
    await nextTick()
    renderChart()
  } catch {}
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  const monthlyData = report.value.monthly || []
  const incomeData = new Array(12).fill(0)
  const expenseData = new Array(12).fill(0)

  monthlyData.forEach(item => {
    incomeData[item.month - 1] = Number(item.income)
    expenseData[item.month - 1] = Number(item.expense)
  })

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['收入', '支出'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: incomeData,
        itemStyle: { color: '#00B42A' },
        areaStyle: { color: 'rgba(0, 180, 42, 0.1)' }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: expenseData,
        itemStyle: { color: '#F53F3F' },
        areaStyle: { color: 'rgba(245, 63, 63, 0.1)' }
      }
    ],
    animationDuration: 1000
  })
}

function getSummary({ columns, data }) {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (index === 4) {
      const totalIncome = data.reduce((sum, row) => sum + Number(row.income), 0)
      const totalExpense = data.reduce((sum, row) => sum + Number(row.expense), 0)
      sums[index] = totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome * 100).toFixed(1) + '%' : '0%'
      return
    }
    const values = data.map(row => {
      const key = ['month', 'income', 'expense', 'netIncome'][index]
      return Number(row[key] || 0)
    })
    const sum = values.reduce((a, b) => a + b, 0)
    if (index === 1 || index === 2 || index === 3) {
      sums[index] = (index === 3 && sum >= 0 ? '+' : '') + '¥' + sum.toLocaleString()
    } else {
      sums[index] = ''
    }
  })
  return sums
}

function handleResize() {
  chart && chart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart && chart.dispose()
})
</script>

<style scoped>
.page-container { animation: pageIn 0.3s ease-out; }
@keyframes pageIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: #1D2129; }
.page-card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.income-card { background: #E8FFEA; }
.expense-card { background: #FFECE8; }
.net-card { background: #E8F3FF; }

.summary-label { font-size: 14px; color: #86909C; margin-bottom: 8px; }
.summary-value { font-size: 24px; font-weight: 600; }
.income { color: #00B42A; }
.expense { color: #F53F3F; }
.text-income { color: #00B42A; font-weight: 500; }
.text-expense { color: #F53F3F; font-weight: 500; }

.chart-section h4, .table-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.table-section {
  margin-top: 24px;
}
</style>
