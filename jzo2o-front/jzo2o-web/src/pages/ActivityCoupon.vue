<template>
  <div class="activity-coupon-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">抢券</span>
      <span class="placeholder"></span>
    </header>
    <div class="tabs">
      <div
        v-for="t in tabs"
        :key="t.value"
        class="tab"
        :class="{ active: tabType === t.value }"
        @click="tabType = t.value; loadList()"
      >
        {{ t.label }}
      </div>
    </div>
    <div v-if="loading && list.length === 0" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无优惠券活动</div>
    <div v-else class="list">
      <div
        v-for="item in list"
        :key="item.id"
        class="coupon-card"
        :class="{ upcoming: tabType === 2 }"
      >
        <div class="card-left">
          <div class="value" :class="{ grey: tabType === 2 }">{{ displayValue(item) }}</div>
          <div
            v-if="tabType === 1"
            class="btn"
            :class="{ disabled: !item.remainNum }"
            @click.stop="tabType === 1 && item.remainNum ? claimCoupon(item.id) : null"
          >
            {{ item.remainNum > 0 ? '立即领取' : '已抢光' }}
          </div>
        </div>
        <div class="card-right">
          <div class="name">{{ item.name }}</div>
          <div class="condition">{{ conditionText(item) }}</div>
          <div class="validity">{{ formatRange(item.distributeStartTime, item.distributeEndTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getActivityList } from '@/api/activity'

const route = useRoute()
const tabs = [
  { value: 1, label: '疯抢中' },
  { value: 2, label: '即将开始' }
]
const tabType = ref(route.query.tabType !== undefined ? Number(route.query.tabType) : 1)
const list = ref([])
const loading = ref(false)

function displayValue(item) {
  if (item.type === 1 && item.discountAmount != null) {
    return `¥${Number(item.discountAmount)}`
  }
  if (item.type === 2 && item.discountRate != null) {
    const rate = Number(item.discountRate)
    return rate >= 10 ? `${Math.floor(rate / 10)}折` : `${rate}折`
  }
  return '优惠'
}

function conditionText(item) {
  const cond = item.amountCondition != null && Number(item.amountCondition) > 0
    ? `满${Number(item.amountCondition)}元`
    : '无门槛'
  if (item.type === 1 && item.discountAmount != null) {
    return `${cond}立减${Number(item.discountAmount)}元`
  }
  if (item.type === 2 && item.discountRate != null) {
    const r = Number(item.discountRate)
    const zhe = r >= 10 ? Math.floor(r / 10) : r
    return `${cond}立享${zhe}折`
  }
  return cond
}

function formatRange(start, end) {
  const toStr = (v) => {
    if (!v) return ''
    if (typeof v === 'string') return v.slice(0, 10)
    if (v.year) return `${v.year}-${String(v.monthValue).padStart(2, '0')}-${String(v.dayOfMonth).padStart(2, '0')}`
    return ''
  }
  const s = toStr(start)
  const e = toStr(end)
  if (!s && !e) return ''
  return s && e ? `${s}~${e}` : s || e
}

async function loadList() {
  loading.value = true
  try {
    const res = await getActivityList({ tabType: tabType.value })
    const data = Array.isArray(res) ? res : (res?.data ?? res?.records ?? [])
    list.value = data
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function claimCoupon(activityId) {
  // 抢券接口若存在可在此调用；当前仅跳转展示，后续可对接 POST /market/consumer/coupon/seize
  alert('抢券功能需对接抢券接口')
}

onMounted(() => {
  loadList()
})

watch(() => route.query.tabType, (v) => {
  if (v !== undefined) tabType.value = Number(v)
})
</script>

<style scoped>
.activity-coupon-page {
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
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
  gap: 24px;
}
.tab {
  padding: 12px 0;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
}
.tab.active {
  color: var(--essential-color-red);
  font-weight: 500;
}
.tab.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: var(--essential-color-red);
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
  border: 1px solid #eee;
}
.card-left {
  width: 110px;
  min-height: 100px;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b 0%, #f74346 100%);
  border-right: 2px dashed rgba(255,255,255,0.5);
}
.coupon-card.upcoming .card-left {
  background: linear-gradient(135deg, #ccc 0%, #999 100%);
}
.value {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}
.value.grey { color: #e8e8e8; }
.btn {
  margin-top: 8px;
  padding: 6px 20px;
  font-size: 13px;
  color: var(--essential-color-red);
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
}
.btn.disabled {
  background: #cda3a3;
  color: #fff;
  cursor: not-allowed;
  pointer-events: none;
}
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
</style>
