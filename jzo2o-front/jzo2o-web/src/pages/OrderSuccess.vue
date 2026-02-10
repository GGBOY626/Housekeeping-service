<template>
  <div class="success-page">
    <div class="icon-wrap">✓</div>
    <p class="title">下单成功</p>
    <p class="amount">¥{{ amount }}</p>
    <p class="tip">服务人员正在疯狂抢单中，请耐心等待~</p>
    <div class="btns">
      <button class="btn secondary" @click="goOrder">查看订单</button>
      <button class="btn primary" @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const amount = computed(() => route.query.amount || '0')
const orderId = computed(() => route.query.id || '')

function goOrder() {
  if (orderId.value) {
    router.push(`/order/detail/${orderId.value}`)
  } else {
    router.push('/order/list')
  }
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-top: 80px;
  text-align: center;
  max-width: 375px;
  margin: 0 auto;
  box-sizing: border-box;
}
.icon-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto 24px;
  background: var(--essential-color-red);
  color: #fff;
  font-size: 40px;
  line-height: 72px;
  border-radius: 50%;
}
.title {
  font-size: 20px;
  font-weight: 600;
  color: #151515;
  margin: 0 0 8px 0;
}
.amount {
  font-size: 28px;
  font-weight: 600;
  color: var(--essential-color-red);
  margin: 0 0 8px 0;
}
.tip {
  font-size: 14px;
  color: #666;
  margin: 0 0 48px 0;
}
.btns {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding: 0 16px;
}
.btn {
  flex: 1;
  max-width: 160px;
  height: 44px;
  border-radius: 22px;
  font-size: 15px;
  cursor: pointer;
  border: none;
}
.btn.secondary {
  background: #fff;
  color: #333;
  border: 1px solid #ddd;
}
.btn.primary {
  background: var(--essential-color-red);
  color: #fff;
}
</style>
