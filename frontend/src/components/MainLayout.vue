<template>
  <div class="layout">
    <!-- Desktop sidebar -->
    <aside class="sidebar hide-on-mobile" :class="{ collapsed: isCollapsed }">
      <div class="logo" @click="$router.push('/index')">
        <el-icon :size="24"><Reading /></el-icon>
        <span v-show="!isCollapsed" class="logo-text">图书管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#165DFF"
        router
        unique-opened
      >
        <el-menu-item index="/index">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/book" v-if="true">
          <el-icon><Collection /></el-icon>
          <span>图书管理</span>
        </el-menu-item>

        <el-menu-item index="/purchase">
          <el-icon><ShoppingCart /></el-icon>
          <span>进货管理</span>
        </el-menu-item>

        <el-sub-menu index="finance" v-if="true">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/bills">账单列表</el-menu-item>
          <el-menu-item index="/finance/report">财务报表</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/user" v-if="userRole === 'SUPER'">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-area">
      <header class="navbar">
        <div class="navbar-left">
          <el-icon class="collapse-btn hide-on-mobile" @click="isCollapsed = !isCollapsed" :size="20">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-icon class="hamburger-btn show-on-mobile" @click="drawerVisible = true" :size="22">
            <Menu />
          </el-icon>
          <el-breadcrumb class="hide-on-mobile" separator="/">
            <el-breadcrumb-item :to="{ path: '/index' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="navbar-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ username }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="400px" destroy-on-close>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">确认</el-button>
      </template>
    </el-dialog>

    <!-- Mobile drawer -->
    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="240px"
      :with-header="false"
      class="mobile-drawer"
    >
      <el-menu
        :default-active="activeMenu"
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#165DFF"
        router
        unique-opened
        @select="drawerVisible = false"
      >
        <el-menu-item index="/index">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/book">
          <el-icon><Collection /></el-icon>
          <span>图书管理</span>
        </el-menu-item>
        <el-menu-item index="/purchase">
          <el-icon><ShoppingCart /></el-icon>
          <span>进货管理</span>
        </el-menu-item>
        <el-sub-menu index="finance">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/bills" @click="drawerVisible = false">账单列表</el-menu-item>
          <el-menu-item index="/finance/report" @click="drawerVisible = false">财务报表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/user" v-if="userRole === 'SUPER'">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Menu } from '@element-plus/icons-vue'
import { useResponsive } from '../composables/useResponsive'
import { logout, changePassword } from '../api/user'

const { isMobile } = useResponsive()
const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)
const drawerVisible = ref(false)
const pwdDialogVisible = ref(false)
const pwdFormRef = ref(null)

const user = JSON.parse(localStorage.getItem('user') || '{}')
const username = computed(() => user.username || '用户')
const userRole = computed(() => user.role || 'NORMAL')
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/finance')) return path
  if (path.startsWith('/purchase')) return '/purchase'
  if (path.startsWith('/book')) return '/book'
  if (path.startsWith('/user')) return '/user'
  return '/index'
})

const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
      logout().catch(() => {})
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }).catch(() => {})
  } else if (cmd === 'password') {
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    pwdDialogVisible.value = true
  }
}

async function submitPassword() {
  await pwdFormRef.value.validate()
  await changePassword({
    oldPassword: pwdForm.value.oldPassword,
    newPassword: pwdForm.value.newPassword
  })
  ElMessage.success('密码修改成功')
  pwdDialogVisible.value = false
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 240px;
  background: #001529;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 1px solid #ffffff1a;
}

.logo-text {
  white-space: nowrap;
}

.sidebar :deep(.el-menu) {
  border-right: none;
}

.sidebar :deep(.el-menu-item.is-active) {
  background: #165DFF20 !important;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.navbar {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #4E5969;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #165DFF;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #4E5969;
  font-size: 14px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.page-enter-active {
  transition: all 0.3s ease-out;
}

.page-leave-active {
  transition: all 0.2s ease-in;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
}

.hamburger-btn {
  cursor: pointer;
  color: #4E5969;
  transition: color 0.2s;
  display: none;
}

.hamburger-btn:hover {
  color: #165DFF;
}

.mobile-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.mobile-drawer :deep(.el-menu) {
  border-right: none;
}

.mobile-drawer :deep(.el-menu-item.is-active) {
  background: #165DFF20 !important;
}

@media (max-width: 767px) {
  .hamburger-btn {
    display: initial;
  }

  .navbar {
    padding: 0 16px;
  }

  .content {
    padding: 16px;
  }
}
</style>
