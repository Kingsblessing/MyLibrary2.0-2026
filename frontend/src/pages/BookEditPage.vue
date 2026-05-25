<template>
  <div class="page-container">
    <div class="page-header">
      <h3>{{ isEdit ? '编辑图书' : '添加图书' }}</h3>
    </div>

    <div class="page-card">
      <!-- ADD mode: select from paid purchases -->
      <template v-if="!isEdit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
          <el-form-item label="选择进货单" prop="purchaseId">
            <el-select
              v-model="form.purchaseId"
              filterable
              placeholder="输入书名搜索已付款的进货单"
              style="width: 100%"
              @change="onPurchaseSelected"
              :loading="purchasesLoading"
            >
              <el-option
                v-for="p in paidPurchases"
                :key="p.id"
                :label="`#${p.id} 《${p.title}》 ${p.author || ''} - ${p.quantity}本 ¥${Number(p.purchasePrice).toFixed(2)}`"
                :value="p.id"
              />
            </el-select>
          </el-form-item>

          <template v-if="preview.title">
            <el-divider content-position="left">自动填充信息</el-divider>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="ISBN">{{ preview.isbn }}</el-descriptions-item>
              <el-descriptions-item label="书名">{{ preview.title }}</el-descriptions-item>
              <el-descriptions-item label="作者">{{ preview.author || '-' }}</el-descriptions-item>
              <el-descriptions-item label="出版社">{{ preview.publisher || '-' }}</el-descriptions-item>
              <el-descriptions-item label="进货数量">{{ preview.quantity }}本</el-descriptions-item>
              <el-descriptions-item label="进货价">¥{{ Number(preview.purchasePrice).toFixed(2) }}</el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">补充信息</el-divider>
            <el-form-item label="零售价格" prop="retailPrice">
              <el-input-number v-model="form.retailPrice" :min="0" :precision="2" :step="1" />
            </el-form-item>
          </template>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
            <el-button @click="$router.push('/book')">取消</el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- EDIT mode: existing form -->
      <template v-else>
        <el-form ref="formRef" :model="form" :rules="editRules" label-width="100px" style="max-width: 600px">
          <el-divider content-position="left">基本信息</el-divider>
          <el-form-item label="书名" prop="title">
            <el-input v-model="form.title" placeholder="请输入书名" />
          </el-form-item>
          <el-form-item label="作者" prop="author">
            <el-input v-model="form.author" placeholder="请输入作者" />
          </el-form-item>
          <el-form-item label="ISBN" prop="isbn">
            <el-input v-model="form.isbn" disabled />
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
            <el-button @click="$router.push('/book')">取消</el-button>
          </el-form-item>
        </el-form>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBook, addBook, updateBook } from '../api/book'
import { getPurchaseList } from '../api/purchase'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const purchasesLoading = ref(false)
const isEdit = computed(() => !!route.params.id)
const paidPurchases = ref([])
const preview = ref({})

const form = ref({
  purchaseId: null,
  retailPrice: 0,
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  stockQuantity: 0
})

const rules = {
  purchaseId: [{ required: true, message: '请选择进货单', trigger: 'change' }],
  retailPrice: [{ required: true, message: '请输入零售价格', trigger: 'blur' }]
}

const editRules = {
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
  } else {
    purchasesLoading.value = true
    try {
      paidPurchases.value = await getPurchaseList({ status: 'PAID' })
    } catch {} finally {
      purchasesLoading.value = false
    }
  }
})

function onPurchaseSelected(id) {
  const p = paidPurchases.value.find(x => x.id === id)
  if (p) {
    preview.value = p
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateBook(route.params.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await addBook({
        purchaseId: form.value.purchaseId,
        retailPrice: form.value.retailPrice
      })
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

@media (max-width: 767px) {
  .el-form {
    max-width: 100% !important;
  }
}
</style>
