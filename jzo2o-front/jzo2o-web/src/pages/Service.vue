<template>
  <div class="service-page">
    <header class="nav-bar">
      <span @click="$router.back()">← 返回</span>
      <span class="title">全部服务</span>
    </header>
    <div class="content">
      <div class="left-tabs">
        <div
          v-for="(item, i) in serviceTypeData"
          :key="i"
          :class="['tab', { active: activeId === item.serveTypeId }]"
          @click="tabChange(item.serveTypeId)"
        >
          {{ item.serveTypeName }}
        </div>
      </div>
      <div class="right-list">
        <div v-for="(item, i) in serviceItemData" :key="i" class="item" @click="toDetail(item.id)">
          {{ item.serveItemName }}
        </div>
      </div>
    </div>
    <footer class="foot-bar">
      <span @click="$router.push('/')">首页</span>
      <span @click="$router.push('/service')">全部服务</span>
      <span @click="$router.push('/my')">我的</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getServeTypeList, getServeList } from '@/api/service'

const router = useRouter()
const serviceTypeData = ref([])
const serviceItemData = ref([])
const activeId = ref(null)

const tabChange = async (id) => {
  activeId.value = id
  await loadList()
}

const loadList = async () => {
  const city = JSON.parse(localStorage.getItem('city') || '{}')
  if (!city.id || !activeId.value) return
  try {
    const res = await getServeList({ regionId: city.id, serveTypeId: activeId.value })
    serviceItemData.value = res?.data ?? res ?? []
  } catch (e) {
    console.error(e)
    serviceItemData.value = []
  }
}

const toDetail = (id) => router.push(`/service/detail/${id}`)

onMounted(async () => {
  const city = JSON.parse(localStorage.getItem('city') || '{}')
  const savedActiveId = localStorage.getItem('activeId')
  if (!city.id) {
    router.replace('/')
    return
  }
  try {
    const res = await getServeTypeList({ regionId: city.id })
    serviceTypeData.value = res?.data || []
    if (serviceTypeData.value.length) {
      activeId.value = savedActiveId ? Number(savedActiveId) : serviceTypeData.value[0]?.serveTypeId
      localStorage.removeItem('activeId')
      await loadList()
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.service-page {
  min-height: 100vh;
  background: #f8f8f8;
  max-width: 375px;
  margin: 0 auto;
  overflow-x: hidden;
  padding-bottom: 60px;
}
.nav-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}
.nav-bar span:first-child {
  cursor: pointer;
  margin-right: 16px;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.content {
  display: flex;
}
.left-tabs {
  width: 90px;
  background: #fff;
}
.tab {
  padding: 14px;
  font-size: 14px;
  cursor: pointer;
}
.tab.active {
  color: var(--essential-color-red);
  font-weight: bold;
}
.right-list {
  flex: 1;
  padding: 12px;
}
.item {
  padding: 12px;
  background: #fff;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
}
.foot-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 375px;
  display: flex;
  justify-content: space-around;
  padding: 12px;
  background: #fff;
  border-top: 1px solid #eee;
  box-sizing: border-box;
}
.foot-bar span {
  cursor: pointer;
}
</style>
