<template>
  <div class="home-page">
    <header class="nav-bar">
      <span class="title">云岚到家</span>
      <span @click="toMy" class="user-btn">我的</span>
    </header>
    <div class="home-box">
      <div class="input-view">
        <div @click="toCity" class="city">
          <img src="/static/dw.png" alt="" />
          <span>{{ city?.name || '请选择城市' }}</span>
        </div>
        <img src="/static/ss.png" alt="" class="search-icon" />
        <input v-model="searchVal" placeholder="日常保洁" @click="handleSearch" readonly />
      </div>
      <img src="/static/banner.png" alt="" class="banner" />
      <div class="tips">
        <span><img src="/static/smile.png" /> 云岚到家平台，给你贴心的 专业的上门服务</span>
        <span><img src="/static/yuan.png" /> 标准定价 售后无忧</span>
      </div>
      <div v-if="!city?.name || menuData.length === 0" class="empty-box">
        <img src="/static/zwnr@2x.png" alt="" />
        <p v-if="!city?.name">请先选择城市进行下单</p>
        <p v-else>当前城市暂未开通服务，请切换其他城市</p>
        <button class="agree-btn" @click="toCity">选择城市</button>
      </div>
      <div v-else class="menu">
        <div class="left-box">
          <div v-for="(item, i) in menuData" :key="i" class="left" @click="toService(item.serveTypeId, 1)">
            <img :src="item.serveTypeIcon" />
            <span>{{ item.serveTypeName }}</span>
          </div>
        </div>
        <div class="right-box">
          <div v-for="(item, i) in menuData" :key="i" class="rights">
            <div class="left-line"></div>
            <div class="right">
              <div v-for="(c, k) in item.serveResDTOList" :key="k" class="card" @click="toService(c.id, 2)">
                <img :src="c.serveItemIcon" />
                <span>{{ c.serveItemName }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="hotData.length > 0" class="section">
        <h3><img src="/static/biao.png" /> 精选推荐</h3>
        <div class="recommend">
          <div v-for="(item, i) in hotData" :key="i" class="card">
            <img :src="item.serveItemImg" class="card-img" @click="toService(item.id, 2)" />
            <div class="card-name">{{ item.serveItemName }}</div>
            <div class="reservation">
              <span>￥{{ item.price }}</span>
              <button @click="toService(item.id, 2)">立即预约</button>
            </div>
          </div>
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
import { getHomeService, getHotServe } from '@/api'

const router = useRouter()
const city = ref(null)
const menuData = ref([])
const hotData = ref([])
const searchVal = ref('')

onMounted(() => {
  city.value = JSON.parse(localStorage.getItem('city') || 'null')
  if (city.value?.id) {
    loadData()
  }
})

const loadData = async () => {
  try {
    const [r1, r2] = await Promise.all([
      getHomeService({ regionId: city.value.id }),
      getHotServe({ regionId: city.value.id })
    ])
    menuData.value = r1?.data || []
    hotData.value = r2?.data || []
  } catch (e) {
    console.error(e)
  }
}

const toCity = () => router.push('/city')
const toService = (id, type) => {
  if (type === 1) {
    localStorage.setItem('activeId', id)
    router.push('/service')
  } else {
    router.push(`/service/detail/${id}`)
  }
}
const handleSearch = () => router.push('/search')
const toMy = () => router.push('/my')
</script>

<style scoped>
.home-page {
  padding-bottom: 60px;
  background: #f8f8f8;
  width: 100%;
  max-width: 375px;
  margin: 0 auto;
  overflow-x: hidden;
  box-sizing: border-box;
}
.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.user-btn {
  font-size: 14px;
  color: var(--essential-color-red);
  cursor: pointer;
}
.home-box {
  padding: 12px;
  box-sizing: border-box;
}
.input-view {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}
.city {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  margin-right: 8px;
}
.city img {
  width: 20px;
  height: 20px;
}
.search-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
}
.input-view input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
}
.banner {
  width: 100%;
  height: auto;
  border-radius: 8px;
  margin-bottom: 12px;
}
.tips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  padding: 10px 0;
  font-size: 12px;
  color: #888;
}
.tips img {
  width: 16px;
  height: 16px;
  vertical-align: middle;
  margin-right: 4px;
}
.empty-box {
  text-align: center;
  padding: 40px 20px;
  background: #fff;
  border-radius: 8px;
}
.empty-box img {
  width: 120px;
  margin-bottom: 16px;
}
.empty-box p {
  margin-bottom: 16px;
  color: #888;
}
.menu {
  display: flex;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  min-width: 0;
}
.left-box {
  width: 72px;
  flex-shrink: 0;
}
.left {
  padding: 10px 0;
  text-align: center;
  cursor: pointer;
  font-size: 12px;
  line-height: 1.3;
  word-break: keep-all;
  white-space: nowrap;
}
.left img {
  width: 32px;
  height: 32px;
  display: block;
  margin: 0 auto 4px;
}
.right-box {
  flex: 1;
  min-width: 0;
}
.rights {
  margin-bottom: 10px;
}
.rights:last-child { margin-bottom: 0; }
.right {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 8px;
}
.right .card {
  width: calc(33.333% - 6px);
  min-width: 0;
  text-align: center;
  cursor: pointer;
  box-sizing: border-box;
}
.right .card img {
  width: 36px;
  height: 36px;
  margin: 0 auto;
  display: block;
}
.right .card span {
  font-size: 12px;
  line-height: 1.4;
  display: block;
  word-break: keep-all;
  overflow-wrap: break-word;
}
.section {
  width: 100%;
  box-sizing: border-box;
}
.section h3 {
  font-size: 15px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section h3 img {
  width: 24px;
  height: 24px;
}
.recommend {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
  box-sizing: border-box;
}
.recommend .card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  min-width: 0;
  box-sizing: border-box;
}
.card-img {
  width: 100%;
  height: 110px;
  object-fit: cover;
  cursor: pointer;
  display: block;
}
.card-name {
  padding: 8px;
  font-size: 14px;
}
.reservation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
}
.reservation span {
  color: var(--essential-color-red);
  font-weight: bold;
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
