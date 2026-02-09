<template>
  <div class="city-page">
    <header class="nav-bar">
      <span @click="$router.back()" class="back">← 返回</span>
      <span class="title">选择城市</span>
    </header>
    <div class="city-list">
      <div v-for="item in cityList" :key="item.id" class="city-item" @click="selectCity(item)">
        {{ item.name || item.cityName }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCityList } from '@/api/address'

const router = useRouter()
const cityList = ref([])

onMounted(async () => {
  try {
    const res = await getCityList()
    cityList.value = res?.data || []
  } catch (e) {
    console.error(e)
  }
})

const selectCity = (item) => {
  const city = { id: item.id, name: item.name || item.cityName, cityCode: item.cityCode }
  localStorage.setItem('city', JSON.stringify(city))
  router.replace('/')
}
</script>

<style scoped>
.city-page {
  min-height: 100vh;
  background: #f8f8f8;
}
.nav-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}
.back {
  cursor: pointer;
  margin-right: 16px;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.city-list {
  padding: 12px;
}
.city-item {
  padding: 14px 16px;
  background: #fff;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
}
</style>
