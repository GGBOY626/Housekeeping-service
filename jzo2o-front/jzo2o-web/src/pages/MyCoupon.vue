<template>
  <div class="my-coupon-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">我的优惠券</span>
      <span class="placeholder"></span>
    </header>
    <div class="tabs">
      <div
        v-for="t in tabs"
        :key="t.status"
        class="tab"
        :class="{ active: statusFilter === t.status }"
        @click="statusFilter = t.status; loadList(true)"
      >
        {{ t.label }}
      </div>
    </div>
    <div v-if="loading && list.length === 0" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无优惠券</div>
    <div v-else class="list">
      <div
        v-for="item in list"
        :key="item.id"
        class="coupon-card"
        :class="{ disabled: item.status === 2 || item.status === 3 }"
      >
        <div class="card-left">
          <div class="value">{{ couponValue(item) }}</div>
          <div v-if="item.status === 2" class="tag used">已使用</div>
          <div v-else-if="item.status === 3" class="tag expired">已过期</div>
        </div>
        <div class="card-right">
          <div class="name">{{ item.name }}</div>
          <div class="condition">{{ conditionText(item) }}</div>
          <div class="validity">有效期至 {{ formatDate(item.validityTime) }}</div>
        </div>
      </div>
    </div>
    <div v-if="hasMore && list.length > 0" class="load-more" @click="loadMore">加载更多</div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMyCouponList } from '@/api/coupon'

const route = useRoute()
const tabs = [
  { status: null, label: '全部' },
  { status: 1, label: '未使用' },
  { status: 2, label: '已使用' },
  { status: 3, label: '已过期' }
]
const statusFilter = ref(route.query.status !== undefined ? Number(route.query.status) : null)
const list = ref([])
const loading = ref(false)
const lastId = ref(null)
const hasMore = ref(true)
const pageSize = 10

function couponValue(item) {
  if (item.type === 1 && item.discountAmount != null) {
    return `¥${Number(item.discountAmount)}`
  }
  if (item.type === 2 && item.discountRate != null) {
    return `${item.discountRate}折`
  }
  return '优惠券'
}

function conditionText(item) {
  const cond = item.amountCondition != null && Number(item.amountCondition) > 0
    ? `满${Number(item.amountCondition)}元`
    : '无门槛'
  if (item.type === 1 && item.discountAmount != null) {
    return `${cond}立减${Number(item.discountAmount)}元`
  }
  if (item.type === 2 && item.discountRate != null) {
    return `${cond}立享${item.discountRate}折`
  }
  return cond
}

function formatDate(t) {
  if (!t) return '-'
  const d = typeof t === 'string' ? new Date(t) : t
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function loadList(reset) {
  if (reset) {
    list.value = []
    lastId.value = null
    hasMore.value = true
  }
  if (loading.value) return
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value != null) params.status = statusFilter.value
    if (lastId.value != null) params.lastId = lastId.value
    const res = await getMyCouponList(params)
    const data = Array.isArray(res) ? res : (res?.data ?? res?.records ?? [])
    if (reset) list.value = data
    else list.value = [...list.value, ...data]
    if (data.length > 0) {
      const last = data[data.length - 1]
      lastId.value = last.id ?? null
    }
    if (data.length < pageSize) hasMore.value = false
  } catch (e) {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

function loadMore() {
  loadList(false)
}

onMounted(() => {
  loadList(true)
})

watch(() => route.query.status, (v) => {
  if (v !== undefined) statusFilter.value = Number(v)
})
</script>

<style scoped>
.my-coupon-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 24px;
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
.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.tab {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  background: #f0f0f0;
  color: #666;
  cursor: pointer;
}
.tab.active {
  background: var(--essential-color-red);
  color: #fff;
}
.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
.list { padding: 12px 16px; }
.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.coupon-card.disabled {
  opacity: 0.75;
  background: #f9f9f9;
}
.card-left {
  width: 100px;
  min-height: 80px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border-right: 2px dashed rgba(0,0,0,0.06);
}
.coupon-card.disabled .card-left {
  background: #eee;
  color: #999;
}
.value {
  font-size: 22px;
  font-weight: 700;
  color: var(--essential-color-red);
  line-height: 1.2;
}
.coupon-card.disabled .value { color: #999; }
.tag {
  margin-top: 6px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}
.tag.used { background: #e0e0e0; color: #666; }
.tag.expired { background: #f0f0f0; color: #999; }
.card-right {
  flex: 1;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}
.name {
  font-size: 15px;
  font-weight: 500;
  color: #151515;
  margin-bottom: 4px;
}
.condition {
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}
.validity {
  font-size: 12px;
  color: #999;
}
.load-more {
  text-align: center;
  padding: 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
</style>
