<template>
  <div class="page-container">
    <div class="page-header">
      <h3>{{ isEdit ? '编辑图书' : '添加图书' }}</h3>
    </div>

    <div class="page-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="零售价格" prop="retailPrice">
          <el-input-number v-model="form.retailPrice" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQuantity">
          <el-input-number v-model="form.stockQuantity" :min="0" :step="1" />
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBook, addBook, updateBook } from '../api/book'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = ref({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  retailPrice: 0,
  stockQuantity: 0
})

const rules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  retailPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const data = await getBook(route.params.id)
      Object.assign(form.value, data)
    } catch {} finally {
      loading.value = false
    }
  }
})

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateBook(route.params.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await addBook(form.value)
      ElMessage.success('添加成功')
    }
    router.push('/book')
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
</style>
