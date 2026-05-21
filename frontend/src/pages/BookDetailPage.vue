<template>
  <div class="page-container">
    <div class="page-header">
      <h3>图书详情</h3>
    </div>

    <div class="page-card" v-loading="loading">
      <div class="detail-layout">
        <div class="cover-area">
          <div class="cover-placeholder">
            <el-icon :size="64" color="#C9CDD4"><Collection /></el-icon>
          </div>
        </div>

        <div class="info-area">
          <div class="info-row">
            <span class="info-label">书名</span>
            <span class="info-value">{{ book.title }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">作者</span>
            <span class="info-value">{{ book.author }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">ISBN</span>
            <span class="info-value">{{ book.isbn }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">出版社</span>
            <span class="info-value">{{ book.publisher }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">零售价格</span>
            <span class="info-value price">¥{{ Number(book.retailPrice || 0).toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">库存数量</span>
            <span class="info-value">{{ book.stockQuantity }}本</span>
          </div>

          <div class="action-bar">
            <el-button type="success" @click="openSellDialog">出售</el-button>
            <el-button type="primary" @click="$router.push('/book/edit/' + book.id)">编辑</el-button>
            <el-button @click="$router.push('/book')">返回列表</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="sellVisible" title="出售图书" width="400px" destroy-on-close>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="书名">{{ book.title }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ Number(book.retailPrice || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ book.stockQuantity }}本</el-descriptions-item>
      </el-descriptions>

      <el-form ref="sellFormRef" :model="sellForm" :rules="sellRules" label-width="80px" style="margin-top: 16px">
        <el-form-item label="出售数量" prop="quantity">
          <el-input-number v-model="sellForm.quantity" :min="1" :max="book.stockQuantity" :step="1" />
        </el-form-item>
        <el-form-item>
          <span>应收金额：<strong class="price">¥{{ sellAmount }}</strong></span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sellVisible = false">取消</el-button>
        <el-button type="primary" :loading="sellLoading" @click="handleSell">确认出售</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBook, sellBook } from '../api/book'

const route = useRoute()
const loading = ref(false)
const book = ref({})

const sellVisible = ref(false)
const sellLoading = ref(false)
const sellFormRef = ref(null)
const sellForm = ref({ quantity: 1 })

const sellRules = {
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value > book.value.stockQuantity) callback(new Error('超出库存数量'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const sellAmount = computed(() => {
  return (Number(book.value.retailPrice || 0) * sellForm.value.quantity).toFixed(2)
})

async function loadBook() {
  loading.value = true
  try {
    book.value = await getBook(route.params.id)
  } catch {} finally {
    loading.value = false
  }
}

function openSellDialog() {
  sellForm.value.quantity = 1
  sellVisible.value = true
}

async function handleSell() {
  await sellFormRef.value.validate()
  sellLoading.value = true
  try {
    await sellBook({ bookId: book.value.id, quantity: sellForm.value.quantity })
    ElMessage.success('出售成功')
    sellVisible.value = false
    loadBook()
  } catch {} finally {
    sellLoading.value = false
  }
}

onMounted(loadBook)
</script>

<style scoped>
.page-container { animation: pageIn 0.3s ease-out; }
@keyframes pageIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.page-header { margin-bottom: 16px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: #1D2129; }
.page-card { background: #fff; border-radius: 8px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

.detail-layout {
  display: flex;
  gap: 48px;
}

.cover-area {
  flex-shrink: 0;
}

.cover-placeholder {
  width: 200px;
  height: 280px;
  background: #F7F8FA;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #E5E6EB;
}

.info-area {
  flex: 1;
}

.info-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #F2F3F5;
}

.info-label {
  width: 100px;
  color: #86909C;
  font-size: 14px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #1D2129;
}

.info-value.price {
  color: #F53F3F;
  font-size: 18px;
  font-weight: 600;
}

.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.price { color: #F53F3F; font-size: 18px; }
</style>
