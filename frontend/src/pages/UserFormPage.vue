<template>
  <div class="page-container">
    <div class="page-header">
      <h3>{{ isEdit ? '编辑用户' : '添加用户' }}</h3>
    </div>

    <div class="page-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 500px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请确认密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="超级管理员" value="SUPER" />
            <el-option label="普通用户" value="NORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeId">
          <el-input v-model="form.employeeId" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="1" :max="120" />
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
import { addUser, updateUser, getUserById } from '../api/user'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'NORMAL',
  realName: '',
  employeeId: '',
  gender: '',
  age: null
})

const rules = computed(() => {
  const base = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '长度3-20个字符', trigger: 'blur' }
    ],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }
  if (!isEdit.value) {
    base.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
    ]
    base.confirmPassword = [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== form.value.password) callback(new Error('两次密码不一致'))
          else callback()
        },
        trigger: 'blur'
      }
    ]
  }
  return base
})

onMounted(async () => {
  if (isEdit.value) {
    try {
      const data = await getUserById(route.params.id)
      form.value = {
        username: data.username,
        password: '',
        confirmPassword: '',
        role: data.role,
        realName: data.realName || '',
        employeeId: data.employeeId || '',
        gender: data.gender || '',
        age: data.age
      }
    } catch {}
  }
})

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateUser(route.params.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await addUser(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/user')
  } catch {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-container {
  animation: pageIn 0.3s ease-out;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 16px;
}

.page-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1D2129;
}

.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

@media (max-width: 767px) {
  .el-form {
    max-width: 100% !important;
  }
}
</style>
