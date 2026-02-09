<template>
  <div class="app-wrap">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  -webkit-text-size-adjust: 100%;
}
#app {
  min-height: 100vh;
  background-color: #f5f5f5;
}
/* 375x667 适配 iPhone 6/7/8 */
.app-wrap {
  max-width: 375px;
  min-height: 100vh;
  margin: 0 auto;
  background: #f8f8f8;
  overflow-x: hidden;
}
</style>
