<template>
  <div class="login-page">
    <div class="login-card" :class="{ 'card-enter': entered }">
      <div class="login-header">
        <el-icon :size="40" color="#165DFF"><Reading /></el-icon>
        <h1>图书管理系统</h1>
        <p class="subtitle">专业的图书进销存管理平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        @keyup.enter="handleLogin"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="showPwd ? 'text' : 'password'"
            placeholder="请输入密码"
            :prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon class="pwd-toggle" @click="showPwd = !showPwd">
                <View v-if="showPwd" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <transition name="fade">
        <div v-if="errorMsg" class="error-msg">
          <el-icon><WarningFilled /></el-icon>
          {{ errorMsg }}
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, View, Hide, WarningFilled, Reading } from '@element-plus/icons-vue'
import { login } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const showPwd = ref(false)
const errorMsg = ref('')
const entered = ref(false)

const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  setTimeout(() => { entered.value = true }, 100)
})

async function handleLogin() {
  errorMsg.value = ''
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = await login(form.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({
      userId: data.userId,
      username: data.username,
      role: data.role
    }))
    router.push('/index')
  } catch (e) {
    errorMsg.value = e.message || '用户名或密码错误'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E8F3FF 0%, #F2F3F5 50%, #E8F3FF 100%);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #165DFF15 0%, transparent 70%);
  top: -100px;
  right: -100px;
  border-radius: 50%;
}

.login-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #165DFF10 0%, transparent 70%);
  bottom: -50px;
  left: -50px;
  border-radius: 50%;
}

.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.5s ease-out;
  position: relative;
  z-index: 1;
}

.login-card.card-enter {
  opacity: 1;
  transform: translateY(0);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
  margin: 12px 0 4px;
}

.subtitle {
  font-size: 14px;
  color: #86909C;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.pwd-toggle {
  cursor: pointer;
  color: #86909C;
}

.error-msg {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #F53F3F;
  font-size: 14px;
  margin-top: 12px;
  animation: shake 0.3s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-2px); }
  75% { transform: translateX(2px); }
}

.fade-enter-active {
  transition: opacity 0.3s;
}
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
