<template>
  <div class="page-container">
    <div class="page-header">
      <h3>新增进货</h3>
    </div>

    <div class="page-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择图书" prop="bookId">
          <el-select
            v-model="form.bookId"
            filterable
            remote
            :remote-method="searchBooks"
            placeholder="输入书名或ISBN搜索"
            style="width: 400px"
            @change="onBookChange"
          >
            <el-option
              v-for="b in bookOptions"
              :key="b.id"
              :label="`${b.title} (${b.isbn})`"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进货价格" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="进货数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :step="1" />
        </el-form-item>

        <el-form-item label="总金额">
          <span class="total-amount">¥{{ totalAmount }}</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookList } from '../api/book'
import { addPurchase } from '../api/purchase'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const bookOptions = ref([])

const form = ref({
  bookId: null,
  purchasePrice: 0,
  quantity: 1
})

const rules = {
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }],
  purchasePrice: [{ required: true, message: '请输入进货价格', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const totalAmount = computed(() => {
  return (Number(form.value.purchasePrice) * form.value.quantity).toFixed(2)
})

async function searchBooks(query) {
  if (!query) return
  try {
    bookOptions.value = await getBookList({ title: query })
  } catch {}
}

function onBookChange(bookId) {
  const book = bookOptions.value.find(b => b.id === bookId)
  if (book) {
    form.value.purchasePrice = Number(book.retailPrice) * 0.6
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await addPurchase(form.value)
    ElMessage.success('进货记录创建成功')
    router.push('/purchase')
  } catch {} finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    bookOptions.value = await getBookList({})
  } catch {}
})
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
.total-amount { font-size: 20px; font-weight: 600; color: #F53F3F; }
</style>
