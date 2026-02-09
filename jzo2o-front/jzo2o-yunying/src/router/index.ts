import {
  useRoute,
  createRouter,
  createWebHashHistory,
  RouteRecordRaw
} from 'vue-router'
// 去重
import uniq from 'lodash/uniq'

// 自动导入modules文件夹下所有ts文件
const modules = import.meta.globEager('./modules/**/*.ts')
// 路由暂存
const routeModuleList: Array<RouteRecordRaw> = []

Object.keys(modules).forEach((key) => {
  const mod = modules[key].default || {}
  const modList = Array.isArray(mod) ? [...mod] : [mod]
  routeModuleList.push(...modList)
})

// 关于单层路由，meta 中设置 { single: true } 即可为单层路由，{ hidden: true } 即可在侧边栏隐藏该路由

// 存放动态路由
export const asyncRouterList: Array<RouteRecordRaw> = [...routeModuleList]

// 存放固定的路由
const defaultRouterList: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/login/index.vue')
  },
  {
    path: '/',
    redirect: '/dashboard/base'
  },
  {
    path: '/:w+',
    name: '404Page',
    redirect: '/result/404'
  }
]
// 所有路由合并
export const allRoutes = [...defaultRouterList, ...asyncRouterList]

// 过滤一个铺平的路由数组
export const getRoutesExpanded = () => {
  const expandedRoutes = []

  allRoutes.forEach((item) => {
    if (item.meta && item.meta.expanded) {
      expandedRoutes.push(item.path)
    }
    if (item.children && item.children.length > 0) {
      item.children
        .filter((child) => child.meta && child.meta.expanded)
        .forEach((child: RouteRecordRaw) => {
          expandedRoutes.push(item.path)
          expandedRoutes.push(`${item.path}/${child.path}`)
        })
    }
  })
  return uniq(expandedRoutes)
}

export const getActive = (maxLevel = 3): string => {
  try {
    const route = useRoute()
    if (!route || !route.path) {
      return ''
    }
    return route.path
      .split('/')
      .filter((_item: string, index: number) => index <= maxLevel && index > 0)
      .map((item: string) => `/${item}`)
      .join('')
  } catch (error) {
    console.warn('[getActive] 无法获取路由，返回空字符串', error)
    return ''
  }
}

// 新增：基于传入的 route 对象计算 active 路径（推荐用法）
export const getActivePath = (route: any, maxLevel = 3): string => {
  if (!route || !route.path) {
    return ''
  }
  return route.path
    .split('/')
    .filter((_item: string, index: number) => index <= maxLevel && index > 0)
    .map((item: string) => `/${item}`)
    .join('')
}

const router = createRouter({
  history: createWebHashHistory(),
  routes: allRoutes,
  scrollBehavior() {
    return {
      el: '#app',
      top: 0,
      behavior: 'smooth'
    }
  }
})

export default router
