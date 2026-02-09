<template>
  <div class="form-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">{{ id ? '编辑地址' : '新增地址' }}</span>
    </header>
    <form class="form" @submit.prevent="handleSubmit">
      <div class="form-item">
        <label>收货人</label>
        <input v-model="form.name" placeholder="请输入姓名" required />
      </div>
      <div class="form-item">
        <label>手机号</label>
        <input v-model="form.phone" type="tel" placeholder="请输入手机号" required />
      </div>
      <div class="form-item">
        <label>省份</label>
        <input v-model="form.province" placeholder="如：北京市" required />
      </div>
      <div class="form-item">
        <label>城市</label>
        <input v-model="form.city" placeholder="如：北京市" required />
      </div>
      <div class="form-item">
        <label>区/县</label>
        <input v-model="form.county" placeholder="如：朝阳区" required />
      </div>
      <div class="form-item">
        <label>详细地址</label>
        <input v-model="form.address" placeholder="街道、楼栋、门牌号等" required />
      </div>
      <div class="form-item default-row">
        <label>设为默认地址</label>
        <div class="switch-wrap" @click="form.isDefault = form.isDefault === 1 ? 0 : 1">
          <span class="switch" :class="{ on: form.isDefault === 1 }"></span>
        </div>
      </div>
      <button type="submit" class="submit-btn">保存</button>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addAddressBook, updateAddressBook, getAddressBookDetail } from '@/api/address'

const route = useRoute()
const router = useRouter()
const id = computed(() => route.query.id)

const form = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  county: '',
  address: '',
  isDefault: 0
})

const loadDetail = async () => {
  if (!id.value) return
  try {
    const res = await getAddressBookDetail(id.value)
    const d = res?.data ?? res
    if (d) {
      form.name = d.name || ''
      form.phone = d.phone || ''
      form.province = d.province || ''
      form.city = d.city || ''
      form.county = d.county || ''
      form.address = d.address || ''
      form.isDefault = d.isDefault ?? 0
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  const payload = {
    name: form.name.trim(),
    phone: form.phone.trim(),
    province: form.province.trim(),
    city: form.city.trim(),
    county: form.county.trim(),
    address: form.address.trim(),
    isDefault: form.isDefault
  }
  if (!payload.name || !payload.phone || !payload.province || !payload.city || !payload.county || !payload.address) {
    alert('请填写完整信息')
    return
  }
  try {
    if (id.value) {
      await updateAddressBook(id.value, payload)
    } else {
      await addAddressBook(payload)
    }
    router.replace('/address')
  } catch (e) {
    console.error(e)
    alert(e?.response?.data?.msg || '操作失败')
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.form-page {
  min-height: 100vh;
  background: #f8f8f8;
  max-width: 375px;
  margin: 0 auto;
}
.nav-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}
.back {
  cursor: pointer;
  margin-right: 16px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.form {
  padding: 12px;
}
.form-item {
  background: #fff;
  padding: 14px 16px;
  margin-bottom: 1px;
  display: flex;
  align-items: center;
  border-radius: 8px;
  margin-bottom: 8px;
}
.form-item label {
  width: 80px;
  font-size: 15px;
  color: #333;
  flex-shrink: 0;
}
.form-item input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
}
.default-row {
  justify-content: space-between;
}
.switch-wrap {
  cursor: pointer;
}
.switch {
  display: inline-block;
  width: 48px;
  height: 28px;
  border-radius: 14px;
  background: #ddd;
  position: relative;
  transition: 0.3s;
}
.switch::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  top: 2px;
  left: 2px;
  transition: 0.3s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}
.switch.on {
  background: var(--essential-color-red);
}
.switch.on::after {
  left: 22px;
}
.submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 24px;
  border: none;
  border-radius: 24px;
  background: var(--essential-color-red);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}
</style>
