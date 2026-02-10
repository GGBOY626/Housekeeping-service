<template>
  <div class="pay-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">支付订单</span>
      <span class="placeholder"></span>
    </header>
    <div class="pay-remain" v-if="payRemainText">支付剩余时间 {{ payRemainText }}</div>
    <div class="order-info">
      <div class="row">
        <span>订单总价</span>
        <span>{{ amount }}元</span>
      </div>
      <div class="row highlight">
        <span>待付金额</span>
        <span class="amount">{{ amount }}元</span>
      </div>
    </div>
    <div class="pay-methods">
      <div class="method-item" :class="{ active: payMethod === 'stripe' }" @click="payMethod = 'stripe'">
        <span class="method-name">Stripe 支付</span>
        <span class="method-radio"><span v-if="payMethod === 'stripe'" class="dot"></span></span>
      </div>
      <div class="method-item method-item-disabled" :class="{ active: payMethod === 'paypal' }" @click="onPayPalClick">
        <span class="method-name">PayPal 支付</span>
        <span class="method-tag">敬请期待</span>
      </div>
    </div>
    <div v-if="payMethod === 'stripe'" class="card-section">
      <div class="card-label">银行卡信息</div>
      <div id="card-element" class="card-element-wrap"></div>
      <p class="card-hint">测试卡号：4242 4242 4242 4242</p>
    </div>
    <footer class="foot-bar">
      <span class="foot-label">还需支付: {{ amount }}元</span>
      <button class="pay-btn" :disabled="paying" @click="handleConfirmPay">{{ paying ? '支付中...' : '确认支付' }}</button>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createPaymentIntent, confirmPaid } from '@/api/order'

const STRIPE_PK = 'pk_test_51Sz5gM4IbIaoE0j4xXWnoLUQQVGwrjKLYWJwnGtoQA6pX6nNT1nPIelqAUT5LSjRppewLHw98Nl88IQrDEZv833s00s5NqE70L'

const route = useRoute()
const router = useRouter()
const orderId = ref(route.query.id || '')
const amount = ref(route.query.amount || '0')
const payMethod = ref('stripe')
const remainSeconds = ref(15 * 60)
const paying = ref(false)
let timer = null
let stripe = null
let cardElement = null
let elements = null

const payRemainText = computed(() => {
  const m = Math.floor(remainSeconds.value / 60)
  const s = remainSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

function initStripe() {
  if (stripe) return
  if (typeof window.Stripe === 'undefined') {
    console.error('Stripe.js 未加载')
    return
  }
  stripe = window.Stripe(STRIPE_PK)
  elements = stripe.elements()
  const style = {
    base: { fontSize: '16px', color: '#32325d' },
    invalid: { color: '#fa755a' }
  }
  cardElement = elements.create('card', { style })
  const el = document.getElementById('card-element')
  if (el && !el.querySelector('.StripeElement')) {
    cardElement.mount('#card-element')
  }
}

function onPayPalClick() {
  alert('PayPal 暂未接入，请使用 Stripe 支付')
}

onMounted(() => {
  timer = setInterval(() => {
    if (remainSeconds.value > 0) remainSeconds.value--
  }, 1000)
  if (payMethod.value === 'stripe') {
    setTimeout(initStripe, 100)
  }
})

watch(payMethod, (v) => {
  if (v === 'stripe') setTimeout(initStripe, 100)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (cardElement) cardElement.destroy()
})

const handleConfirmPay = async () => {
  if (payMethod.value === 'paypal') {
    alert('请使用 Stripe 支付')
    return
  }
  if (!orderId.value || !amount.value) {
    alert('订单信息缺失')
    return
  }
  if (!stripe || !cardElement) {
    alert('支付未就绪，请稍候')
    return
  }
  paying.value = true
  try {
    // 订单 id 为 19 位，不能用 Number() 否则精度丢失导致“订单不存在”
    const res = await createPaymentIntent({
      orderId: orderId.value,
      amount: Number(amount.value)
    })
    // 接口返回 { code: 200, data: { clientSecret: "pi_xxx_secret_xxx" } }，clientSecret 在 data 里
    const clientSecret = res?.data?.clientSecret
    if (!clientSecret) {
      throw new Error('获取支付参数失败')
    }
    const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
      payment_method: { card: cardElement }
    })
    if (error) {
      alert(error.message || '支付失败')
      paying.value = false
      return
    }
    await confirmPaid({
      orderId: orderId.value,
      paymentIntentId: paymentIntent?.id || ''
    })
    if (timer) clearInterval(timer)
    router.replace(`/order/success?id=${orderId.value}&amount=${amount.value}`)
  } catch (e) {
    alert(e.response?.data?.msg || e.message || '支付失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.pay-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 80px;
  max-width: 375px;
  margin: 0 auto;
  box-sizing: border-box;
}
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}
.back {
  font-size: 15px;
  cursor: pointer;
  min-width: 50px;
}
.title {
  font-size: 17px;
  font-weight: 600;
  flex: 1;
  text-align: center;
}
.placeholder {
  min-width: 50px;
}
.pay-remain {
  background: linear-gradient(90deg, #ff9500 0%, #ff6b00 100%);
  color: #fff;
  text-align: center;
  padding: 10px;
  font-size: 14px;
}
.order-info {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}
.order-info .row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}
.order-info .row:last-child {
  margin-bottom: 0;
}
.order-info .row.highlight .amount {
  color: var(--essential-color-red);
  font-weight: 600;
  font-size: 18px;
}
.pay-methods {
  background: #fff;
  margin: 0 16px 12px;
  border-radius: 12px;
  overflow: hidden;
}
.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f4f4f4;
  cursor: pointer;
}
.method-item:last-child {
  border-bottom: none;
}
.method-item-disabled {
  opacity: 0.8;
}
.method-name {
  font-size: 15px;
  color: #151515;
}
.method-tag {
  font-size: 12px;
  color: #999;
}
.method-radio {
  width: 20px;
  height: 20px;
  border: 1px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.method-item.active .method-radio {
  border-color: var(--essential-color-red);
  background: var(--essential-color-red);
}
.method-radio .dot {
  width: 6px;
  height: 6px;
  background: #fff;
  border-radius: 50%;
}
.card-section {
  background: #fff;
  margin: 0 16px 12px;
  border-radius: 12px;
  padding: 16px;
}
.card-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
}
.card-element-wrap {
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  min-height: 44px;
}
.card-hint {
  font-size: 12px;
  color: #888;
  margin: 8px 0 0 0;
}
.foot-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 375px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}
.foot-label {
  font-size: 14px;
  color: #333;
}
.pay-btn {
  height: 44px;
  padding: 0 32px;
  border: none;
  border-radius: 22px;
  background: var(--essential-color-red);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}
.pay-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
