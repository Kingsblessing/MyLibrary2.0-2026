import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/LoginPage.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../components/MainLayout.vue'),
    redirect: '/index',
    children: [
      {
        path: 'index',
        name: 'Index',
        component: () => import('../pages/IndexPage.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('../pages/UserManagerPage.vue'),
        meta: { title: '用户管理', role: 'SUPER' }
      },
      {
        path: 'user/add',
        name: 'UserAdd',
        component: () => import('../pages/UserFormPage.vue'),
        meta: { title: '添加用户', role: 'SUPER' }
      },
      {
        path: 'user/edit/:id',
        name: 'UserEdit',
        component: () => import('../pages/UserFormPage.vue'),
        meta: { title: '编辑用户', role: 'SUPER' }
      },
      {
        path: 'book',
        name: 'BookList',
        component: () => import('../pages/BookListPage.vue'),
        meta: { title: '图书列表' }
      },
      {
        path: 'book/detail/:id',
        name: 'BookDetail',
        component: () => import('../pages/BookDetailPage.vue'),
        meta: { title: '图书详情' }
      },
      {
        path: 'book/edit/:id',
        name: 'BookEdit',
        component: () => import('../pages/BookEditPage.vue'),
        meta: { title: '编辑图书' }
      },
      {
        path: 'book/add',
        name: 'BookAdd',
        component: () => import('../pages/BookEditPage.vue'),
        meta: { title: '添加图书' }
      },
      {
        path: 'purchase',
        name: 'PurchaseList',
        component: () => import('../pages/PurchaseListPage.vue'),
        meta: { title: '进货记录' }
      },
      {
        path: 'purchase/add',
        name: 'PurchaseAdd',
        component: () => import('../pages/PurchaseAddPage.vue'),
        meta: { title: '新增进货' }
      },
      {
        path: 'purchase/detail/:id',
        name: 'PurchaseDetail',
        component: () => import('../pages/PurchaseDetailPage.vue'),
        meta: { title: '进货详情' }
      },
      {
        path: 'finance/bills',
        name: 'FinanceBills',
        component: () => import('../pages/FinanceBillsPage.vue'),
        meta: { title: '账单列表' }
      },
      {
        path: 'finance/report',
        name: 'FinanceReport',
        component: () => import('../pages/FinanceReportPage.vue'),
        meta: { title: '财务报表' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
