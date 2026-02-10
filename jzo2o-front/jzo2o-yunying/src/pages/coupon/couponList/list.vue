<!-- 优惠券列表页：搜索、表格、撤销弹窗、领取记录弹窗。单根节点供 Content 的 <Transition> 正确动画 -->
<template>
  <div class="coupon-list-root">
    <div class="base-up-wapper bgTable min-h">
      <searchFormBox
        :initSearch="initSearch"
        :typeSelect="typeSelect"
        :cityList="cityList"
        @handleSearch="handleSearch"
        @handleReset="handleReset"
      />
      <tableList
        :list-data="listData"
        :pagination="pagination"
        :activeStatus="0"
        @handleClickAssign="handleClickAssign"
        @handleClickCancel="handleClickCancel"
        @onPageChange="onPageChange"
      />
    </div>
    <Delete
      :title="'确认撤销'"
      :dialog-delete-visible="visible"
      :delete-text="'此操作将结束发放优惠券，未使用的优惠券将作废，用户将无法使用该优惠券'"
      @handle-delete="handleSubmit"
      @handle-close="handleClose"
    />
    <assignDialog
      :visible="assignDialogVisible"
      :title="title"
      :pagination="pagination2"
      :receiveData="receiveData"
      :data="DialogFormData"
      @onPageChange="onAssignPageChange"
      @handleClose="handleClose"
      @fetchData="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import {
  getCouponList,
  getCouponRecordList,
  deleteCoupon,
  getCouponDetail
} from '@/api/coupon'
import { useRoute } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'

import tableList from './components/TableList.vue'
import Delete from '@/components/Delete/index.vue'
import searchFormBox from './components/SearchForm.vue'
import assignDialog from './components/assignDialog.vue'

const route = useRoute()
const visible = ref(false)
const assignDialogVisible = ref(false)
const listData = ref([])
const label = ref('')
const dataLoading = ref(false)
const DialogFormData = ref({})
const title = ref('新建')
const initSearch = ref()
const typeSelect = ref([])
const refundId = ref('')
const cityList = ref([])

const pagination = ref({
  defaultPageSize: 10,
  total: 0,
  defaultCurrent: 1
})
const pagination2 = ref({
  defaultPageSize: 10,
  total: 0,
  defaultCurrent: 1
})
const requestData = ref({
  id: null,
  name: null,
  status: null,
  type: null,
  pageNo: 1,
  pageSize: 10
})
const resetData = ref({
  id: null,
  name: null,
  status: null,
  type: null,
  pageNo: 1,
  pageSize: 10
})
const requestData2 = ref({
  activityId: null,
  pageNo: 1,
  pageSize: 10
})
const receiveData = ref()

watch(
  () => route.query,
  () => {
    pagination.value.defaultCurrent = 1
    pagination.value.defaultPageSize = 10
    fetchData(requestData.value)
  }
)

const handleSearch = (val) => {
  requestData.value.id = val.id
  requestData.value.name = val.name
  requestData.value.status = val.status
  requestData.value.type = val.type
  requestData.value.pageNo = 1
  requestData.value.pageSize = 10
  pagination.value.defaultCurrent = 1
  pagination.value.defaultPageSize = 10
  fetchData(requestData.value)
}

const handleReset = () => {
  pagination.value.defaultCurrent = 1
  fetchData(requestData.value)
}

const fetchData = async (val) => {
  dataLoading.value = true
  await getCouponList(val)
    .then((res) => {
      if (res.code === 200) {
        listData.value = res.data.list
        pagination.value.total = Number(res.data.total)
        dataLoading.value = false
      }
    })
    .catch((err) => {
      console.log(err)
    })
}

const fetchData2 = async (val) => {
  dataLoading.value = true
  await getCouponRecordList(val)
    .then((res) => {
      if (res.code === 200) {
        DialogFormData.value = res.data.list
        pagination2.value.total = Number(res.data.total)
        dataLoading.value = false
      }
    })
    .catch((err) => {
      console.log(err)
    })
}

const getCouponNumDetail = async (val) => {
  await getCouponDetail(val)
    .then((res) => {
      if (res.code === 200) {
        receiveData.value = res.data
      } else {
        MessagePlugin.error(res.msg)
      }
    })
    .catch((err) => {
      console.log(err)
    })
}

const handleClose = () => {
  visible.value = false
  assignDialogVisible.value = false
}

const handleClickCancel = (row) => {
  visible.value = true
  refundId.value = row.id
  title.value = '请填写取消原因'
  label.value = '取消原因：'
}

const handleSubmit = async () => {
  await deleteCoupon(refundId.value)
    .then((res) => {
      if (res.code === 200) {
        MessagePlugin.success('撤销成功')
        visible.value = false
        fetchData(requestData.value)
      } else {
        MessagePlugin.error(res.data.msg)
      }
    })
    .catch((err) => {
      console.log(err)
    })
}

const onPageChange = (val) => {
  requestData.value.pageNo = val.defaultCurrent
  requestData.value.pageSize = val.defaultPageSize
  fetchData(requestData.value)
}

const onAssignPageChange = (val) => {
  requestData2.value.pageNo = val.defaultCurrent
  requestData2.value.pageSize = val.defaultPageSize
  fetchData2(requestData.value)
}

const handleClickAssign = (row) => {
  assignDialogVisible.value = true
  title.value = '领取记录'
  requestData2.value.activityId = row.id
  getCouponNumDetail(row.id)
  fetchData2(requestData2.value)
}

onMounted(() => {
  fetchData(resetData.value)
})
</script>

<style lang="less" scoped>
.coupon-list-root {
  display: block;
}
.min-h {
  min-height: 720px !important;
}
</style>
