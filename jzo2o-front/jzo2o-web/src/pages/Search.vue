<template>
  <div class="search-page">
    <header class="nav-bar">
      <input v-model="keyword" placeholder="搜索服务" @keyup.enter="doSearch" />
      <button @click="doSearch">搜索</button>
    </header>
    <div class="result" v-if="result.length">
      <div v-for="(item, i) in result" :key="i" class="item" @click="$router.push(`/service/detail/${item.id}`)">
        {{ item.serveItemName }} - ￥{{ item.price }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getServeSearch } from '@/api'

const keyword = ref('')
const result = ref([])

const doSearch = async () => {
  const city = JSON.parse(localStorage.getItem('city') || '{}')
  if (!city.id || !keyword.value) return
  try {
    const res = await getServeSearch({ regionId: city.id, keyword: keyword.value })
    result.value = res?.data || []
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #f8f8f8;
}
.nav-bar {
  display: flex;
  padding: 12px;
  background: #fff;
}
.nav-bar input {
  flex: 1;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-right: 8px;
}
.result .item {
  padding: 14px 16px;
  background: #fff;
  margin: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}
</style>
