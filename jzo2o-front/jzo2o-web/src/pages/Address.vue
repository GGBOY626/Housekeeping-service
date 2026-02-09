<template>
  <div class="address-page">
    <header class="nav-bar">
      <span class="back" @click="$router.back()">← 返回</span>
      <span class="title">我的地址</span>
    </header>
    <div class="list-wrap" v-if="list.length > 0">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="address-card"
        @click="handleSelect(item)"
      >
        <div class="card-main">
          <div class="name">{{ item.name }}</div>
          <div class="phone">{{ item.phone }}</div>
          <div class="addr">{{ item.province }}{{ item.city }}{{ item.county }}{{ item.address }}</div>
          <div class="card-footer">
            <div
              class="default-btn"
              :class="{ active: item.isDefault === 1 }"
              @click.stop="handleSetDefault(item)"
            >
              <span class="checkbox"></span>
              <span>默认地址</span>
            </div>
            <div class="actions">
              <span class="edit" @click.stop="handleEdit(item)">编辑</span>
              <span class="del" @click.stop="handleDeleteOne(item)">删除</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty">
      <img src="/static/zwnr@2x.png" alt="" />
      <p>暂无服务地址哦～</p>
    </div>
    <div class="footer-bar">
      <button class="add-btn" @click="handleAdd">新增地址</button>
    </div>
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showConfirm" class="confirm-mask" @click.self="showConfirm = false">
          <div class="confirm-box">
            <p>确定删除{{ deleteIds.length > 1 ? '所选地址' : '此条地址' }}？</p>
            <div class="confirm-btns">
              <button class="cancel" @click="showConfirm = false">取消</button>
              <button class="ok" @click="confirmDelete">确定</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  getAddressBookPage,
  setDefaultAddress,
  deleteAddressBookBatch
} from '@/api/address'

const router = useRouter()
const route = useRoute()
const list = ref([])
const showConfirm = ref(false)
const deleteIds = ref([])

const loadList = async () => {
  try {
    const res = await getAddressBookPage({
      pageNo: 1,
      pageSize: 100,
      orderBy1: 'isDefault',
      orderBy2: 'updateTime',
      isAsc1: false,
      isAsc2: false
    })
    const data = res?.data ?? res
    list.value = data?.list ?? data?.records ?? []
  } catch (e) {
    console.error(e)
  }
}

const handleSelect = (item) => {
  if (route.query.fromDetail === 'true') {
    sessionStorage.setItem('activeAddressId', String(item.id))
    router.back()
  }
}

const handleSetDefault = async (item) => {
  try {
    const flag = item.isDefault === 1 ? 0 : 1
    await setDefaultAddress(item.id, flag)
    loadList()
  } catch (e) {
    console.error(e)
  }
}

const handleEdit = (item) => {
  router.push(`/address/form?id=${item.id}`)
}

const handleDeleteOne = (item) => {
  deleteIds.value = [item.id]
  showConfirm.value = true
}

const handleAdd = () => {
  router.push('/address/form')
}

const confirmDelete = async () => {
  try {
    await deleteAddressBookBatch(deleteIds.value)
    showConfirm.value = false
    deleteIds.value = []
    loadList()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 80px;
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
.list-wrap {
  padding: 12px;
}
.address-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.name, .phone {
  font-size: 15px;
  color: #151515;
  margin-bottom: 4px;
}
.addr {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f4f4f4;
}
.default-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
  cursor: pointer;
}
.default-btn .checkbox {
  width: 16px;
  height: 16px;
  border: 1px solid #ddd;
  border-radius: 50%;
}
.default-btn.active .checkbox {
  background: var(--essential-color-red);
  border-color: var(--essential-color-red);
}
.actions {
  display: flex;
  gap: 16px;
}
.actions span {
  font-size: 14px;
  cursor: pointer;
}
.edit { color: #333; }
.del { color: var(--essential-color-red); }
.empty {
  text-align: center;
  padding: 60px 20px;
}
.empty img {
  width: 120px;
  margin-bottom: 16px;
}
.empty p {
  color: #888;
  font-size: 14px;
}
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 375px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #eee;
}
.add-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 22px;
  background: var(--essential-color-red);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}
.confirm-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.confirm-box {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin: 20px;
  min-width: 260px;
}
.confirm-box p {
  margin: 0 0 20px;
  font-size: 16px;
  text-align: center;
}
.confirm-btns {
  display: flex;
  gap: 12px;
}
.confirm-btns button {
  flex: 1;
  height: 40px;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
}
.cancel {
  border: 1px solid #ddd;
  background: #fff;
}
.ok {
  border: none;
  background: var(--essential-color-red);
  color: #fff;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
