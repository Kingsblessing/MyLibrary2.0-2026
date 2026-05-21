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
            <el-button type="primary" @click="$router.push('/book/edit/' + book.id)">编辑</el-button>
            <el-button @click="$router.push('/book')">返回列表</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBook } from '../api/book'

const route = useRoute()
const loading = ref(false)
const book = ref({})

onMounted(async () => {
  loading.value = true
  try {
    book.value = await getBook(route.params.id)
  } catch {} finally {
    loading.value = false
  }
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
}
</style>
