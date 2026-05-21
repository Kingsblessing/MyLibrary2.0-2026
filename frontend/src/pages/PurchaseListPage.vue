<template>
  <div class="page-container">
    <div class="page-header">
      <h3>进货记录</h3>
      <el-button type="primary" @click="$router.push('/purchase/add')">
        <el-icon><Plus /></el-icon>新增进货
      </el-button>
    </div>

    <div class="page-card">
      <div class="search-bar">
        <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="未付款" value="UNPAID" />
          <el-option label="已付款" value="PAID" />
          <el-option label="已退货" value="RETURNED" />
          <el-option label="已入库" value="STOCKED" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="query.status = ''; loadData()">重置</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="进货日期" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="图书" min-width="180" />
        <el-table-column prop="isbn" label="ISBN" width="150" />
        <el-table-column label="进货价" width="100">
          <template #default="{ row }">¥{{ Number(row.purchasePrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="总金额" width="110">
          <template #default="{ row }">
            ¥{{ (Number(row.purchasePrice) * row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/purchase/detail/' + row.id)">详情</el-button>
            <template v-if="row.status === 'UNPAID'">
              <el-button link type="success" @click="handlePay(row)">付款</el-button>
              <el-button link type="warning" @click="handleReturn(row)">退货</el-button>
            </template>
            <template v-if="row.status === 'PAID'">
              <el-button link type="primary" @click="handleStockIn(row)">入库</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="stockInDialog.visible" title="新书入库" width="440px" destroy-on-close>
      <p>确定将进货单 <strong>#{{ stockInDialog.purchaseId }}</strong>《{{ stockInDialog.title }}》入库 <strong>{{ stockInDialog.quantity }}</strong> 本？</p>
      <el-form v-if="!stockInDialog.isExisting" :model="stockInDialog" label-width="100px" style="margin-top: 16px">
        <el-form-item label="零售价格" prop="retailPrice" :rules="[{ required: true, message: '请输入零售价格', trigger: 'blur' }]">
          <el-input-number v-model="stockInDialog.retailPrice" :min="0" :precision="2" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockInDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="stockInDialog.loading" @click="handleStockInConfirm">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseList, payPurchase, returnPurchase } from '../api/purchase'
import { stockIn } from '../api/book'

const loading = ref(false)
const tableData = ref([])
const query = ref({ status: '' })
const stockInDialog = ref({ visible: false, loading: false, purchaseId: null, quantity: 0, title: '', isExisting: true, retailPrice: 0 })

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (query.value.status) params.status = query.value.status
    tableData.value = await getPurchaseList(params)
  } catch {} finally {
    loading.value = false
  }
}

function statusText(s) {
  return { UNPAID: '未付款', PAID: '已付款', RETURNED: '已退货', STOCKED: '已入库' }[s] || s
}

function statusType(s) {
  return { UNPAID: 'warning', PAID: 'success', RETURNED: 'info', STOCKED: '' }[s] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

async function handlePay(row) {
  await ElMessageBox.confirm(`确定付款进货单 #${row.id}？总金额 ¥${(Number(row.purchasePrice) * row.quantity).toFixed(2)}`, '付款确认', { type: 'warning' })
  await payPurchase(row.id)
  ElMessage.success('付款成功')
  loadData()
}

async function handleReturn(row) {
  await ElMessageBox.confirm(`确定退货进货单 #${row.id}？`, '退货确认', { type: 'warning' })
  await returnPurchase(row.id)
  ElMessage.success('退货成功')
  loadData()
}

function handleStockIn(row) {
  stockInDialog.value = {
    visible: true,
    loading: false,
    purchaseId: row.id,
    quantity: row.quantity,
    title: row.title,
    isExisting: row.bookId != null,
    retailPrice: 0
  }
}

async function handleStockInConfirm() {
  stockInDialog.value.loading = true
  try {
    const payload = {
      purchaseId: stockInDialog.value.purchaseId,
      quantity: stockInDialog.value.quantity
    }
    if (!stockInDialog.value.isExisting) {
      payload.retailPrice = stockInDialog.value.retailPrice
    }
    await stockIn(payload)
    ElMessage.success('入库成功')
    stockInDialog.value.visible = false
    loadData()
  } catch {} finally {
    stockInDialog.value.loading = false
  }
}

onMounted(loadData)
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
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
