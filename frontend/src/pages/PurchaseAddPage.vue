<template>
  <div class="page-container">
    <div class="page-header">
      <h3>新增进货</h3>
    </div>

    <div class="page-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-divider content-position="left">图书信息</el-divider>

        <el-form-item label="选择模式">
          <el-radio-group v-model="mode" @change="onModeChange">
            <el-radio label="existing">从库存中选择</el-radio>
            <el-radio label="new">录入新书信息</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="mode === 'existing'">
          <el-form-item label="选择图书" prop="bookId">
            <el-select
              v-model="form.bookId"
              filterable
              remote
              :remote-method="searchBooks"
              placeholder="输入书名或ISBN搜索"
              style="width: 400px"
              @change="onBookSelected"
            >
              <el-option
                v-for="b in bookOptions"
                :key="b.id"
                :label="`${b.title} - ${b.author} (ISBN: ${b.isbn})`"
                :value="b.id"
              />
            </el-select>
          </el-form-item>
          <div v-if="selectedBook.title" class="book-preview">
            <el-tag>ISBN: {{ selectedBook.isbn }}</el-tag>
            <el-tag>作者: {{ selectedBook.author }}</el-tag>
            <el-tag>出版社: {{ selectedBook.publisher || '-' }}</el-tag>
          </div>
        </template>

        <template v-else>
          <el-form-item label="ISBN" prop="isbn">
            <el-input v-model="form.isbn" placeholder="请输入ISBN号" />
          </el-form-item>
          <el-form-item label="书名" prop="title">
            <el-input v-model="form.title" placeholder="请输入书名" />
          </el-form-item>
          <el-form-item label="作者" prop="author">
            <el-input v-model="form.author" placeholder="请输入作者" />
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="form.publisher" placeholder="请输入出版社" />
          </el-form-item>
        </template>

        <el-divider content-position="left">进货详情</el-divider>

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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookList } from '../api/book'
import { addPurchase } from '../api/purchase'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const mode = ref('existing')
const bookOptions = ref([])
const selectedBook = ref({})

const form = ref({
  bookId: null,
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  purchasePrice: 0,
  quantity: 1
})

const rules = computed(() => {
  const base = {
    purchasePrice: [{ required: true, message: '请输入进货价格', trigger: 'blur' }],
    quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
  }
  if (mode.value === 'existing') {
    return {
      ...base,
      bookId: [{ required: true, message: '请选择图书', trigger: 'change' }]
    }
  }
  return {
    ...base,
    isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
    title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
    author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
  }
})

const totalAmount = computed(() => {
  return (Number(form.value.purchasePrice) * form.value.quantity).toFixed(2)
})

function onModeChange() {
  form.value.bookId = null
  form.value.isbn = ''
  form.value.title = ''
  form.value.author = ''
  form.value.publisher = ''
  selectedBook.value = {}
  formRef.value?.clearValidate()
}

async function searchBooks(query) {
  if (!query) return
  try {
    bookOptions.value = await getBookList({ title: query })
  } catch {}
}

async function onBookSelected(bookId) {
  const book = bookOptions.value.find(b => b.id === bookId)
  if (book) {
    selectedBook.value = book
    form.value.purchasePrice = Number(book.retailPrice || 0) * 0.6
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = { ...form.value }
    if (mode.value === 'existing') {
      data.isbn = selectedBook.value.isbn
      data.title = selectedBook.value.title
      data.author = selectedBook.value.author || ''
      data.publisher = selectedBook.value.publisher || ''
    } else {
      data.bookId = null
    }
    await addPurchase(data)
    ElMessage.success('进货记录创建成功')
    router.push('/purchase')
  } catch {} finally {
    loading.value = false
  }
}
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
.book-preview { display: flex; gap: 8px; flex-wrap: wrap; padding: 12px; background: #F7F8FA; border-radius: 6px; margin-bottom: 16px; }
</style>
