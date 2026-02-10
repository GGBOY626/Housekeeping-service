<!-- 内置组件，避免重复渲染DOM -->
<template>
  <router-view v-if="!isRefreshing" v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <!-- 使用 name 避免全局 fullPath key 导致整站重挂载的副作用 -->
      <component :is="Component" :key="route.name" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTabsRouterStore } from '@/store'

const route = useRoute()
const tabsRouterStore = useTabsRouterStore()

// isRefreshing是一个布尔值，用来判断是否正在刷新页面
const isRefreshing = computed(() => tabsRouterStore.refreshing)
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
