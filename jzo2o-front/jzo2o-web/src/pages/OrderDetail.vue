<template>
  <div class="detail-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">订单详情</span>
      <span class="placeholder"></span>
    </header>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="detail" class="content">
      <div class="status-bar" :class="statusClass">
        <span class="status-text">{{ orderStatusText }}</span>
      </div>
      <div class="block">
        <div class="block-title">服务信息</div>
        <div class="serve-row">
          <img v-if="detail.serveItemImg" :src="detail.serveItemImg" class="serve-img" alt="" />
          <div class="serve-info">
            <div class="serve-name">{{ detail.serveItemName }}</div>
            <div class="serve-meta">¥{{ detail.realPayAmount }} · {{ detail.purNum || 1 }}份</div>
          </div>
        </div>
        <div class="info-row" v-if="detail.serveStartTime">
          <span class="label">预约时间</span>
          <span class="value">{{ formatTime(detail.serveStartTime) }}</span>
        </div>
      </div>
      <div class="block">
        <div class="block-title">服务地址</div>
        <div class="address-text">{{ detail.serveAddress }}</div>
        <div class="address-contact">{{ detail.contactsName }} {{ detail.contactsPhone }}</div>
      </div>
      <div class="block" v-if="detail.createTime">
        <div class="info-row">
          <span class="label">下单时间</span>
          <span class="value">{{ formatTime(detail.createTime) }}</span>
        </div>
        <div class="info-row" v-if="detail.payTime">
          <span class="label">支付时间</span>
          <span class="value">{{ formatTime(detail.payTime) }}</span>
        </div>
      </div>
      <div class="foot-actions" v-if="canPay || canCancel">
        <button v-if="canCancel" class="btn secondary" @click="handleCancel">取消订单</button>
        <button v-if="canPay" class="btn primary" @click="$router.push(`/order/pay?id=${detail.id}&amount=${detail.realPayAmount}`)">去支付</button>
      </div>
    </div>
    <div v-else class="empty">订单不存在</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(true)
const canceling = ref(false)

const orderStatusText = computed(() => {
  if (!detail.value) return ''
  const s = detail.value.ordersStatus
  const pay = detail.value.payStatus
  if (s === 0 && pay !== 4) return '待支付'
  if (s === 100) return '派单中'
  if (s === 200) return '待服务'
  if (s === 300) return '服务中'
  if (s === 400) return '待评价'
  if (s === 500) return '已完成'
  if (s === 600) return '已取消'
  if (s === 700) return '已关闭'
  return '未知'
})

const statusClass = computed(() => {
  if (!detail.value) return ''
  const s = detail.value.ordersStatus
  if (s === 0) return 'status-pay'
  if (s === 500) return 'status-done'
  if (s === 600 || s === 700) return 'status-cancel'
  return ''
})

const canPay = computed(() => {
  return detail.value && detail.value.ordersStatus === 0 && detail.value.payStatus !== 4
})

const canCancel = computed(() => {
  if (!detail.value) return false
  const s = detail.value.ordersStatus
  return s === 0 || s === 100
})

async function handleCancel() {
  if (!detail.value || canceling.value) return
  if (!confirm('确定要取消该订单吗？')) return
  canceling.value = true
  try {
    await cancelOrder({
      orderId: String(detail.value.id),
      cancelReason: '用户取消'
    })
    alert('取消成功')
    const res = await getOrderDetail(route.params.id)
    detail.value = res?.data ?? res
  } catch (e) {
    alert(e.response?.data?.msg || e.message || '取消失败')
  } finally {
    canceling.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = typeof t === 'string' ? new Date(t) : t
  return d.toLocaleString('zh-CN')
}

onMounted(async () => {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }
  try {
    const res = await getOrderDetail(id)
    detail.value = res?.data ?? res
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
  max-width: 375px;
  margin: 0 auto;
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
.back { font-size: 15px; cursor: pointer; min-width: 50px; }
.title { font-size: 17px; font-weight: 600; flex: 1; text-align: center; }
.placeholder { min-width: 50px; }
.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
.content { padding: 12px 16px; }
.status-bar {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 12px;
  text-align: center;
}
.status-bar.status-pay { background: linear-gradient(135deg, #fff5f5, #fff); }
.status-bar.status-done { background: linear-gradient(135deg, #f6ffed, #fff); }
.status-bar.status-cancel { background: #f5f5f5; }
.status-text { font-size: 18px; font-weight: 600; }
.block {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}
.block-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}
.serve-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.serve-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
}
.serve-info { flex: 1; min-width: 0; }
.serve-name { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.serve-meta { font-size: 13px; color: #666; }
.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 8px 0;
  border-bottom: 1px solid #f4f4f4;
}
.info-row:last-child { border-bottom: none; }
.label { color: #666; }
.value { color: #333; }
.address-text { font-size: 14px; color: #333; margin-bottom: 4px; }
.address-contact { font-size: 13px; color: #666; }
.foot-actions {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 375px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #eee;
  box-sizing: border-box;
}
.foot-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.btn {
  flex: 1;
  max-width: 160px;
  height: 44px;
  border: none;
  border-radius: 22px;
  font-size: 16px;
  cursor: pointer;
}
.btn.primary {
  background: var(--essential-color-red);
  color: #fff;
}
.btn.secondary {
  background: #fff;
  color: #333;
  border: 1px solid #ddd;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
