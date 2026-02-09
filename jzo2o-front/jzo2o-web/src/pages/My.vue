<template>
  <div class="my-page">
    <div class="header">
      <div class="user-info" @click="handleLogin">
        <img src="https://yjy-oss-videos.oss-accelerate.aliyuncs.com/tx.png" class="avatar" alt="" />
        <span class="nickname">{{ nickName || '微信用户' }}</span>
      </div>
    </div>
    <div class="order-section">
      <div class="order-header">
        <span class="order-title">我的订单</span>
        <span class="view-all" @click="toOrders('')">
          查看全部 <span class="arrow">›</span>
        </span>
      </div>
      <div class="order-tabs">
        <div class="tab" @click="toOrders(0)">
          <img src="/static/icon01.png" class="tab-icon" alt="" />
          <span>待支付</span>
        </div>
        <div class="tab" @click="toOrders(200)">
          <img src="/static/icon02.png" class="tab-icon" alt="" />
          <span>待服务</span>
        </div>
        <div class="tab" @click="toOrders(300)">
          <img src="/static/icon03.png" class="tab-icon" alt="" />
          <span>服务中</span>
        </div>
        <div class="tab" @click="toOrders(400)">
          <img src="/static/icon04.png" class="tab-icon" alt="" />
          <span>待评价</span>
        </div>
      </div>
    </div>
    <div class="menu-section">
      <div class="menu-item" @click="toAddress">
        <span>我的地址</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="toReviews">
        <span>我的评价</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="toCoupons">
        <span>我的优惠券</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>
    <div class="service-section">
      <div class="menu-item">
        <span>客服电话</span>
        <a href="tel:400-000-4000" class="phone">400-000-4000</a>
      </div>
    </div>
    <footer class="foot-bar">
      <span @click="$router.push('/')">首页</span>
      <span @click="$router.push('/service')">全部服务</span>
      <span class="active">我的</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const nickName = ref('')

onMounted(() => {
  nickName.value = localStorage.getItem('nickName') || ''
})

const handleLogin = () => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
  }
}

const toOrders = (status) => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  // 订单列表页（待实现）
  alert('订单列表功能开发中')
}

const toAddress = () => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  router.push('/address')
}

const toReviews = () => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  alert('我的评价功能开发中')
}

const toCoupons = () => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  alert('我的优惠券功能开发中')
}
</script>

<style scoped>
.my-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 60px;
  max-width: 375px;
  margin: 0 auto;
  overflow-x: hidden;
}
.header {
  background: linear-gradient(135deg, #ff6b6b 0%, var(--essential-color-red) 100%);
  padding: 40px 20px 30px;
  position: relative;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin-right: 16px;
  background: #fff;
}
.nickname {
  font-size: 18px;
  font-weight: 500;
  color: #fff;
}
.order-section {
  background: #fff;
  margin: -16px 12px 12px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.order-title {
  font-size: 16px;
  font-weight: 600;
  color: #151515;
}
.view-all {
  font-size: 14px;
  color: #888;
  cursor: pointer;
}
.arrow {
  font-size: 16px;
}
.order-tabs {
  display: flex;
  justify-content: space-around;
}
.tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 12px;
  color: #404040;
}
.tab-icon {
  width: 32px;
  height: 32px;
}
.menu-section {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  overflow: hidden;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px;
  border-bottom: 1px solid #f4f4f4;
  cursor: pointer;
  font-size: 15px;
  color: #151515;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-arrow {
  color: #ccc;
  font-size: 18px;
}
.service-section {
  background: #fff;
  margin: 0 12px;
  border-radius: 12px;
}
.service-section .menu-item {
  cursor: default;
}
.phone {
  color: var(--essential-color-red);
  text-decoration: none;
  font-weight: 500;
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
  font-size: 14px;
}
.foot-bar span.active {
  color: var(--essential-color-red);
}
</style>
