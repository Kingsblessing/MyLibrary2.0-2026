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
        <el-table-column label="书名" min-width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/book/detail/' + row.id)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="140" />
        <el-table-column prop="isbn" label="ISBN" width="150" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            ¥{{ Number(row.retailPrice).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/book/detail/' + row.id)">详情</el-button>
            <el-button link type="primary" @click="$router.push('/book/edit/' + row.id)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookList, deleteBook } from '../api/book'

const loading = ref(false)
const tableData = ref([])
const query = ref({ title: '', author: '', isbn: '', publisher: '' })

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
</style>
