<template>
  <div class="order-list-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">我的订单</span>
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
    <div v-else-if="list.length === 0" class="empty">暂无订单</div>
    <div v-else class="list">
      <div
        v-for="item in list"
        :key="item.id"
        class="order-card"
        @click="$router.push(`/order/detail/${item.id}`)"
      >
        <div class="card-head">
          <span class="order-no">订单 {{ item.id }}</span>
          <span class="status-tag" :class="statusClass(item)">{{ orderStatusText(item) }}</span>
        </div>
        <div class="card-body">
          <img v-if="item.serveItemImg" :src="item.serveItemImg" class="serve-img" alt="" />
          <div class="serve-info">
            <div class="serve-name">{{ item.serveItemName }}</div>
            <div class="serve-meta">¥{{ item.realPayAmount ?? item.totalAmount }} · {{ item.purNum || 1 }}份</div>
            <div class="serve-time" v-if="item.serveStartTime">预约: {{ formatTime(item.serveStartTime) }}</div>
          </div>
        </div>
        <div class="card-foot">
          <template v-if="item.ordersStatus === 0 && item.payStatus !== 4">
            <button class="btn primary" @click.stop="$router.push(`/order/pay?id=${item.id}&amount=${item.realPayAmount || item.totalAmount}`)">去支付</button>
          </template>
        </div>
      </div>
    </div>
    <div v-if="hasMore && list.length > 0" class="load-more" @click="loadMore">加载更多</div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderList } from '@/api/order'

const route = useRoute()
const tabs = [
  { status: '', label: '全部' },
  { status: 0, label: '待支付' },
  { status: 200, label: '待服务' },
  { status: 300, label: '服务中' },
  { status: 400, label: '待评价' },
  { status: 500, label: '已完成' }
]
const statusFilter = ref(route.query.status !== undefined ? Number(route.query.status) : '')
const list = ref([])
const loading = ref(false)
const lastSortBy = ref(null)
const hasMore = ref(true)
const pageSize = 20

function orderStatusText(item) {
  const s = item.ordersStatus
  const pay = item.payStatus
  if (s === 0 && pay !== 4) return '待支付'
  if (s === 100) return '派单中'
  if (s === 200) return '待服务'
  if (s === 300) return '服务中'
  if (s === 400) return '待评价'
  if (s === 500) return '已完成'
  if (s === 600) return '已取消'
  if (s === 700) return '已关闭'
  return '未知'
}

function statusClass(item) {
  const s = item.ordersStatus
  if (s === 0) return 'status-pay'
  if (s === 500) return 'status-done'
  if (s === 600 || s === 700) return 'status-cancel'
  return ''
}

function formatTime(t) {
  if (!t) return ''
  const d = typeof t === 'string' ? new Date(t) : t
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadList(reset) {
  if (reset) {
    list.value = []
    lastSortBy.value = null
    hasMore.value = true
  }
  if (loading.value) return
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value !== '' && statusFilter.value !== null) params.ordersStatus = statusFilter.value
    if (lastSortBy.value != null) params.sortBy = lastSortBy.value
    const res = await getOrderList(params)
    const data = Array.isArray(res) ? res : (res?.data ?? res?.records ?? [])
    if (reset) list.value = data
    else list.value = [...list.value, ...data]
    if (data.length > 0) {
      const last = data[data.length - 1]
      lastSortBy.value = last.sortBy ?? null
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
.order-list-page {
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
.order-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  cursor: pointer;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f4f4f4;
}
.order-no { font-size: 13px; color: #666; }
.status-tag { font-size: 12px; }
.status-pay { color: var(--essential-color-red); }
.status-done { color: #52c41a; }
.status-cancel { color: #999; }
.card-body {
  display: flex;
  padding: 12px 16px;
  gap: 12px;
}
.serve-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
}
.serve-info { flex: 1; min-width: 0; }
.serve-name { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.serve-meta { font-size: 13px; color: #666; margin-bottom: 2px; }
.serve-time { font-size: 12px; color: #999; }
.card-foot {
  padding: 12px 16px;
  border-top: 1px solid #f4f4f4;
  text-align: right;
}
.btn {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  border: none;
  cursor: pointer;
}
.btn.primary {
  background: var(--essential-color-red);
  color: #fff;
}
.load-more {
  text-align: center;
  padding: 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
</style>
