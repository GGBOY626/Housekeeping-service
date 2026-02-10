<template>
  <div class="detail-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">{{ detail?.serveItemName || '服务详情' }}</span>
      <span class="placeholder"></span>
    </header>
    <div v-if="detail" class="content">
      <img v-if="detail.serveItemImg" :src="detail.serveItemImg" class="cover" alt="" />
      <div class="head">
        <div class="info">
          <h1 class="serve-name">{{ detail.serveItemName }}</h1>
          <p class="price">
            ￥<span class="num">{{ detail.price }}</span
            ><span class="unit">/{{ unitLabel }}</span>
          </p>
        </div>
        <div class="num-box">
          <button class="num-btn" @click="num > 1 && num--">-</button>
          <span class="num-val">{{ num }}</span>
          <button class="num-btn" @click="num < 10 && num++">+</button>
        </div>
      </div>
      <div v-if="detail.detailImg" class="detail-img-wrap">
        <img :src="detail.detailImg" class="detail-img" alt="服务详情" />
      </div>
      <div class="guarantee">
        <h3>到家保障</h3>
        <div class="guarantee-list">
          <div class="item">
            <span class="icon">🕐</span>
            <span>时效保障</span>
          </div>
          <div class="item">
            <span class="icon">¥</span>
            <span>明码标价</span>
          </div>
          <div class="item">
            <span class="icon">✓</span>
            <span>清洁保障</span>
          </div>
        </div>
        <p class="disclaimer">*上门服务前,如有问题,请您提前与师傅沟通,避免服务产生分歧</p>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>
    <footer class="foot-bar">
      <button class="book-btn" @click="handleBook">立即预约</button>
    </footer>

    <!-- 预约信息弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showBookModal" class="modal-mask" @click.self="closeModal">
          <div class="modal-wrap">
            <div class="modal-content">
              <div class="modal-header">
                <h3>请填写您的预约信息</h3>
                <span class="modal-close" @click="closeModal">×</span>
              </div>
              <div class="modal-body">
                <div class="form-row address-row" @click="toAddressSelect">
                  <div class="form-left">
                    <img src="/static/dizhi@2x.png" class="form-icon" alt="" />
                    <div v-if="!addressData" class="form-placeholder">请选择服务地址</div>
                    <div v-else class="form-address">
                      <div class="address-text">{{ fullAddress }}</div>
                      <div class="address-contact">{{ addressData.name }} {{ addressData.phone }}</div>
                    </div>
                  </div>
                  <span class="form-arrow">›</span>
                </div>
                <div class="form-row" @click.stop="showTimePicker = true">
                  <div class="form-left">
                    <img src="/static/smsj@2x.png" class="form-icon" alt="" />
                    <span class="form-placeholder">请选择上门时间</span>
                  </div>
                  <span class="form-value" :class="{ placeholder: !toDoorTimeLabel }">{{ toDoorTimeLabel || '请选择' }}</span>
                  <span class="form-arrow">›</span>
                </div>
                <!-- 自定义上门时间选择弹层 -->
                <div v-if="showTimePicker" class="time-picker-mask" @click.self="showTimePicker = false">
                  <div class="time-picker-panel">
                    <div class="time-picker-header">
                      <span @click="showTimePicker = false">取消</span>
                      <span>选择上门时间</span>
                      <span class="confirm" @click="confirmTime">确定</span>
                    </div>
                    <div class="time-picker-body">
                      <div class="time-picker-date">
                        <div
                          v-for="(item, idx) in pickerDateList"
                          :key="idx"
                          :class="['date-item', { active: selectedDateIdx === idx }]"
                          @click="selectedDateIdx = idx"
                        >
                          {{ item.label }}
                        </div>
                      </div>
                      <div class="time-picker-slots">
                        <div
                          v-for="(slot, idx) in pickerTimeSlots"
                          :key="idx"
                          :class="['slot-item', { active: selectedTimeIdx === idx }]"
                          @click="selectedTimeIdx = idx"
                        >
                          {{ slot }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="form-title">优惠券信息</div>
                <div class="form-row">
                  <div class="form-left">
                    <img src="/static/yhqtb@2x.png" class="form-icon" alt="" />
                    <span>优惠券</span>
                  </div>
                  <span class="form-value gray">暂无可用</span>
                  <span class="form-arrow">›</span>
                </div>
              </div>
              <div class="modal-foot">
                <span class="foot-price">¥{{ totalPrice }}</span>
                <button
                  class="foot-btn"
                  :class="{ disabled: !canSubmit }"
                  :disabled="!canSubmit"
                  @click="handleSubmit"
                >
                  立即预约
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getServeById, placeOrder } from '@/api/service'
import { getDefaultAddress, getAddressBookDetail } from '@/api/address'
import { UNIT } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const num = ref(1)
const showBookModal = ref(false)
const addressData = ref(null)
const toDoorTimeLabel = ref('')
const serveStartTime = ref(null)
const showTimePicker = ref(false)
const selectedDateIdx = ref(0)
const selectedTimeIdx = ref(0)
// 近7天日期
const pickerDateList = computed(() => {
  const list = []
  for (let i = 0; i < 7; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    const month = d.getMonth() + 1
    const day = d.getDate()
    const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
    list.push({
      value: `${d.getFullYear()}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
      label: i === 0 ? '今天' : i === 1 ? '明天' : `${month}/${day} 周${week}`
    })
  }
  return list
})
// 时段：08:00 - 20:00 每半小时
const pickerTimeSlots = computed(() => {
  const slots = []
  for (let h = 8; h <= 20; h++) {
    slots.push(`${String(h).padStart(2, '0')}:00`)
    if (h < 20) slots.push(`${String(h).padStart(2, '0')}:30`)
  }
  return slots
})

const unitLabel = computed(() => {
  if (!detail.value?.unit) return '次'
  const u = UNIT.find((x) => x.value === detail.value.unit)
  return u ? u.label : '次'
})

const fullAddress = computed(() => {
  if (!addressData.value) return ''
  const { province = '', city = '', county = '', address = '' } = addressData.value
  return `${province}${city}${county}${address}`.replace(/\s/g, '')
})

const totalPrice = computed(() => {
  if (!detail.value?.price) return '0'
  const price = Number(detail.value.price) * num.value
  return price % 1 === 0 ? price : price.toFixed(2)
})

const canSubmit = computed(() => {
  return addressData.value?.id && toDoorTimeLabel.value
})

const confirmTime = () => {
  const dateStr = pickerDateList.value[selectedDateIdx.value]?.value
  const timeStr = pickerTimeSlots.value[selectedTimeIdx.value]
  if (!dateStr || !timeStr) return
  toDoorTimeLabel.value = `${dateStr} ${timeStr}`
  serveStartTime.value = `${dateStr} ${timeStr}:00`
  showTimePicker.value = false
}

onMounted(async () => {
  const id = route.params.id
  if (!id) return
  try {
    const res = await getServeById(id)
    detail.value = res?.data ?? res
  } catch (e) {
    console.error(e)
  }
})

const loadDefaultAddress = async () => {
  try {
    const selectedId = sessionStorage.getItem('activeAddressId')
    if (selectedId) {
      sessionStorage.removeItem('activeAddressId')
      const res = await getAddressBookDetail(selectedId)
      addressData.value = res?.data ?? res ?? null
      return
    }
    const res = await getDefaultAddress()
    addressData.value = res?.data ?? res ?? null
  } catch (e) {
    console.error(e)
    addressData.value = null
  }
}

const handleBook = async () => {
  showBookModal.value = true
  await loadDefaultAddress()
}

const closeModal = () => {
  showBookModal.value = false
}

const toAddressSelect = () => {
  router.push(`/address?fromDetail=true&detailId=${route.params.id}`)
}

const handleSubmit = async () => {
  if (!canSubmit.value) return
  try {
    // 服务 id 可能超过 JS 安全整数，用字符串传参避免精度丢失，后端 Long 可正常反序列化
    const res = await placeOrder({
      serveId: route.params.id,
      addressBookId: addressData.value.id,
      serveStartTime: serveStartTime.value,
      purNum: num.value
    })
    const orderId = res?.data?.id ?? res?.id
    if (orderId) {
      closeModal()
      router.push(`/order/pay?id=${orderId}&amount=${totalPrice.value}`)
    } else {
      alert(res?.msg || '下单失败')
    }
  } catch (e) {
    console.error(e)
    alert(e?.response?.data?.msg || e?.message || '下单失败')
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 70px;
  max-width: 375px;
  margin: 0 auto;
  overflow-x: hidden;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.placeholder {
  min-width: 50px;
}
.content {
  background: #fff;
}
.cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
  background: #eee;
  display: block;
}
.head {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  background: #fff;
}
.info .serve-name {
  font-size: 18px;
  font-weight: 500;
  color: #151515;
  margin: 0 0 8px 0;
}
.price {
  font-size: 14px;
  color: var(--essential-color-red);
  margin: 0;
}
.price .num {
  font-size: 22px;
  font-weight: 600;
}
.price .unit {
  font-size: 13px;
  margin-left: 2px;
}
.num-box {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}
.num-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}
.num-btn:hover {
  background: #eee;
}
.num-val {
  min-width: 36px;
  text-align: center;
  font-size: 15px;
}
.detail-img-wrap {
  background: #fff;
  margin-top: 8px;
  padding: 0 0 20px 0;
  overflow: hidden;
}
.detail-img {
  width: 100%;
  height: auto;
  display: block;
  vertical-align: top;
}
.guarantee {
  background: #fff;
  padding: 20px 16px 24px;
  margin-top: 8px;
}
.guarantee h3 {
  font-size: 16px;
  font-weight: 500;
  color: #151515;
  margin: 0 0 16px 0;
}
.guarantee-list {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.guarantee-list .item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #404040;
}
.guarantee-list .icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.disclaimer {
  font-size: 12px;
  color: var(--essential-color-red);
  margin: 0;
  line-height: 1.5;
  word-wrap: break-word;
  overflow-wrap: break-word;
}
.loading {
  text-align: center;
  padding: 40px;
  color: #888;
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
  box-sizing: border-box;
}
.book-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 22px;
  background: var(--essential-color-red);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}

/* 预约弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.modal-wrap {
  width: 100%;
  max-width: 375px;
  max-height: 85vh;
  overflow-y: auto;
}
.modal-content {
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding-bottom: env(safe-area-inset-bottom, 0);
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f4f4f4;
}
.modal-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: #151515;
  margin: 0;
}
.modal-close {
  font-size: 24px;
  color: #888;
  cursor: pointer;
  line-height: 1;
}
.modal-body {
  padding: 0 16px 20px;
}
.form-row {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f4f4f4;
  cursor: pointer;
}
.form-row.address-row {
  align-items: flex-start;
  min-height: 56px;
}
.form-left {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
}
.form-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}
.form-placeholder {
  color: #888;
  font-size: 14px;
}
.form-address {
  flex: 1;
  min-width: 0;
}
.address-text {
  font-size: 14px;
  color: #151515;
  line-height: 1.5;
  word-break: break-all;
}
.address-contact {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}
.form-value {
  font-size: 14px;
  color: #151515;
  margin-right: 4px;
}
.form-value.gray,
.form-value.placeholder {
  color: #888;
}
/* 上门时间选择弹层 */
.time-picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.time-picker-panel {
  width: 100%;
  max-width: 375px;
  max-height: 60vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
}
.time-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}
.time-picker-header .confirm {
  color: var(--essential-color-red);
  cursor: pointer;
}
.time-picker-body {
  display: flex;
  flex: 1;
  min-height: 0;
}
.time-picker-date {
  width: 110px;
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  padding: 8px 0;
}
.date-item {
  padding: 10px 12px;
  font-size: 14px;
  color: #333;
  text-align: center;
  cursor: pointer;
}
.date-item.active {
  color: var(--essential-color-red);
  font-weight: 500;
}
.time-picker-slots {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-content: flex-start;
}
.slot-item {
  width: calc(50% - 4px);
  padding: 10px;
  font-size: 14px;
  color: #333;
  text-align: center;
  border-radius: 8px;
  background: #f5f5f5;
  cursor: pointer;
}
.slot-item.active {
  background: rgba(255, 59, 48, 0.1);
  color: var(--essential-color-red);
  font-weight: 500;
}
.form-arrow {
  font-size: 18px;
  color: #ccc;
  margin-left: 8px;
}
.form-title {
  font-size: 14px;
  color: #151515;
  padding: 16px 0 8px;
}
.modal-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid #f4f4f4;
}
.foot-price {
  font-size: 22px;
  font-weight: 600;
  color: var(--essential-color-red);
}
.foot-btn {
  height: 40px;
  padding: 0 32px;
  border: none;
  border-radius: 20px;
  background: var(--essential-color-red);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}
.foot-btn.disabled {
  background: #ccc;
  cursor: not-allowed;
}
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s;
}
.modal-enter-active .modal-wrap,
.modal-leave-active .modal-wrap {
  transition: transform 0.3s;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-wrap,
.modal-leave-to .modal-wrap {
  transform: translateY(100%);
}
</style>
