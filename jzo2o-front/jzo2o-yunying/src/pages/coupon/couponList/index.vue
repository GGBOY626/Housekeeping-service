<!-- 优惠券管理：仅做路由出口；key 用 fullPath 保证 list/add/edit 切换时子页面重新渲染 -->
<template>
  <router-view :key="$route.fullPath" />
</template>

<script setup lang="ts">
</script>
