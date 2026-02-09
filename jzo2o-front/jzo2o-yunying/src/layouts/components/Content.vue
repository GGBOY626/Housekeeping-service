<!-- 内置组件，避免重复渲染DOM -->
<template>
  <router-view v-if="!isRefreshing" v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <!-- 临时完全禁用 keep-alive，强制每次路由变化都重新挂载组件 -->
      <component :is="Component" :key="route.name" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useTabsRouterStore } from '@/store'

const route = useRoute()
const tabsRouterStore = useTabsRouterStore()

// isRefreshing是一个布尔值，用来判断是否正在刷新页面
const isRefreshing = computed(() => tabsRouterStore.refreshing)

// 【诊断日志】监控路由变化
watch(
  () => route.fullPath,
  (newPath) => {
    console.log('[Content-强制刷新] 路由变化:', newPath, '| 路由name:', route.name, '| 组件将重新挂载')
  },
  { immediate: true }
)
</script>
<style lang="less" scoped>
.fade-leave-active,
.fade-enter-active {
  transition: opacity @anim-duration-slow @anim-time-fn-easing;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
