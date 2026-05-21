<template>
  <div class="page-container">
    <div class="page-header">
      <h3>图书管理</h3>
      <el-button type="primary" @click="$router.push('/book/add')">
        <el-icon><Plus /></el-icon>添加图书
      </el-button>
    </div>

    <div class="page-card">
      <div class="search-bar">
        <el-input v-model="query.title" placeholder="书名" clearable style="width: 160px" />
        <el-input v-model="query.author" placeholder="作者" clearable style="width: 140px" />
        <el-input v-model="query.isbn" placeholder="ISBN" clearable style="width: 160px" />
        <el-input v-model="query.publisher" placeholder="出版社" clearable style="width: 160px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="书名" min-width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/book/detail/' + row.id)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="publisher" label="出版社" width="120" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column label="价格" width="90">
          <template #default="{ row }">
            ¥{{ Number(row.retailPrice).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="70" />
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/book/detail/' + row.id)">详情</el-button>
            <el-button link type="primary" @click="$router.push('/book/edit/' + row.id)">编辑</el-button>
            <el-button link type="success" @click="openSellDialog(row)">出售</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="sellDialog.visible" title="出售图书" width="400px" destroy-on-close>
      <template v-if="sellDialog.book">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="书名">{{ sellDialog.book.title }}</el-descriptions-item>
          <el-descriptions-item label="ISBN">{{ sellDialog.book.isbn }}</el-descriptions-item>
          <el-descriptions-item label="零售价">¥{{ Number(sellDialog.book.retailPrice).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="当前库存">{{ sellDialog.book.stockQuantity }}本</el-descriptions-item>
        </el-descriptions>

        <el-form ref="sellFormRef" :model="sellDialog" :rules="sellRules" label-width="80px" style="margin-top: 16px">
          <el-form-item label="出售数量" prop="quantity">
            <el-input-number v-model="sellDialog.quantity" :min="1" :max="sellDialog.book.stockQuantity" :step="1" />
          </el-form-item>
          <el-form-item>
            <span>应收金额：<strong class="price">¥{{ sellAmount }}</strong></span>
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="sellDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="sellDialog.loading" @click="handleSell">确认出售</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookList, deleteBook, sellBook } from '../api/book'

const loading = ref(false)
const tableData = ref([])
const query = ref({ title: '', author: '', isbn: '', publisher: '' })
const sellFormRef = ref(null)
const sellDialog = ref({
  visible: false,
  loading: false,
  book: null,
  quantity: 1
})

const sellRules = {
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (sellDialog.value.book && value > sellDialog.value.book.stockQuantity) {
          callback(new Error('超出库存数量'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const sellAmount = computed(() => {
  const b = sellDialog.value.book
  if (!b) return '0.00'
  return (Number(b.retailPrice) * sellDialog.value.quantity).toFixed(2)
})

async function loadData() {
  loading.value = true
  try {
    const params = {}
    Object.entries(query.value).forEach(([k, v]) => { if (v) params[k] = v })
    tableData.value = await getBookList(params)
  } catch {} finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { title: '', author: '', isbn: '', publisher: '' }
  loadData()
}

function openSellDialog(row) {
  sellDialog.value = { visible: true, loading: false, book: row, quantity: 1 }
}

async function handleSell() {
  await sellFormRef.value.validate()
  sellDialog.value.loading = true
  try {
    await sellBook({
      bookId: sellDialog.value.book.id,
      quantity: sellDialog.value.quantity
    })
    ElMessage.success('出售成功')
    sellDialog.value.visible = false
    loadData()
  } catch {} finally {
    sellDialog.value.loading = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除图书《${row.title}》吗？`, '删除确认', { type: 'warning' })
  await deleteBook(row.id)
  ElMessage.success('删除成功')
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
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: #1D2129; }
.page-card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.price { color: #F53F3F; font-size: 18px; }
</style>
