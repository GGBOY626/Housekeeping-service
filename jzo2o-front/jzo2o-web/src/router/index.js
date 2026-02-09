import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/pages/Login.vue'), meta: { noAuth: true } },
  { path: '/register', name: 'Register', component: () => import('@/pages/Register.vue'), meta: { noAuth: true } },
  { path: '/', name: 'Home', component: () => import('@/pages/Home.vue') },
  { path: '/city', name: 'City', component: () => import('@/pages/City.vue') },
  { path: '/service', name: 'Service', component: () => import('@/pages/Service.vue') },
  { path: '/service/detail/:id', name: 'ServiceDetail', component: () => import('@/pages/ServiceDetail.vue') },
  { path: '/search', name: 'Search', component: () => import('@/pages/Search.vue') },
  { path: '/my', name: 'My', component: () => import('@/pages/My.vue') },
  { path: '/address', name: 'Address', component: () => import('@/pages/Address.vue') },
  { path: '/address/form', name: 'AddressForm', component: () => import('@/pages/AddressForm.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const noAuthPaths = ['/login', '/register']
  if (!noAuthPaths.includes(to.path) && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
