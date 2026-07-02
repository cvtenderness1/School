<template>
  <div style="padding: 20px; background: #fff9e6; min-height: calc(100vh - 80px)">
    <el-card shadow="hover" style="margin-bottom: 20px;display:flex;justify-content:space-between;align-items:center">
      <h3>开票申请管理</h3>
      <el-button style="background:#ffb800; border-color:#ffb800; color:#fff;" @click="openDialog">新增开票申请</el-button>
    </el-card>

    <el-card shadow="hover">
      <el-table :data="invoiceList" border stripe>
        <el-table-column prop="invoiceNo" label="开票单号" />
        <el-table-column label="申请类型">
          <template #default="scope">
            <span>{{ scope.row.applyType === 1 ? '用户开票' : '骑手开票' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="riderName" label="姓名" />
        <el-table-column prop="orderId" label="关联订单号" />
        <el-table-column prop="settleNo" label="关联结算单号" />
        <el-table-column prop="invoiceAmount" label="开票金额" />
        <el-table-column prop="invoiceTitle" label="发票抬头" />
        <el-table-column prop="taxNo" label="税号" />
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已开票' : '待开票' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button v-if="scope.row.status===0" type="primary" link @click="done(scope.row.id)">
              标记已开票
            </el-button>
            <el-button type="danger" link @click="del(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="申请开票" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="开票类型">
          <el-select v-model="form.applyType" @change="changeType" style="width:100%">
            <el-option label="用户开票" :value="1" />
            <el-option label="骑手开票" :value="2" />
          </el-select>
        </el-form-item>

        <!-- 用户开票：选择订单 -->
        <el-form-item label="选择订单" v-if="form.applyType === 1">
          <el-select v-model="form.orderId" @change="selectOrder" style="width:100%">
            <el-option
                v-for="item in orderList"
                :key="item.orderId"
                :label="item.orderId + ' | 金额：' + item.payMoney"
                :value="item.orderId"
            />
          </el-select>
        </el-form-item>

        <!-- 骑手开票：选择结算单 -->
        <el-form-item label="选择结算单" v-if="form.applyType === 2">
          <el-select v-model="form.settleNo" @change="selectSettle" style="width:100%">
            <el-option
                v-for="item in settleList"
                :key="item.settleNo"
                :label="item.riderName + ' | ' + item.settleNo + ' | 佣金：' + item.riderCommission"
                :value="item.settleNo"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开票金额">
          <el-input v-model="form.invoiceAmount" disabled placeholder="自动带出" />
        </el-form-item>

        <el-form-item label="发票抬头">
          <el-input v-model="form.invoiceTitle" placeholder="校园跑腿服务中心" />
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="form.taxNo" placeholder="91330106MA27K58912" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import {ref, onMounted} from 'vue'
import request from '@/utils/request.js'
import {ElMessage, ElMessageBox} from 'element-plus'

const invoiceList = ref([])
const settleList = ref([])
const orderList = ref([])
const dialogVisible = ref(false)

const currentUserId = ref('')
const currentRiderId = ref('')
const currentRiderName = ref('') // 新增：存姓名

const form = ref({
  applyType: 1,
  orderId: '',
  settleNo: '',
  invoiceAmount: '',
  invoiceTitle: '校园跑腿服务中心',
  taxNo: '91330106MA27K58912'
})

const getList = async () => {
  const res = await request.get('/finance/invoice/list', {params: {status: -1}})
  invoiceList.value = res.data
}

const getSettleList = async () => {
  const res = await request.get('/finance/settlement/list', {params: {status: 1}})
  settleList.value = res.data
}

const getOrderList = async () => {
  const res = await request.get('/order/list')
  orderList.value = res.data
}

const changeType = () => {
  form.value.orderId = ''
  form.value.settleNo = ''
  form.value.invoiceAmount = ''
  currentRiderName.value = '' // 清空骑手名
}

const openDialog = async () => {
  await getOrderList()
  await getSettleList()
  form.value = {
    applyType: 1,
    orderId: '',
    settleNo: '',
    invoiceAmount: '',
    invoiceTitle: '校园跑腿服务中心',
    taxNo: '91330106MA27K58912'
  }
  currentRiderName.value = ''
  dialogVisible.value = true
}

// 选择订单
const selectOrder = (val) => {
  const item = orderList.value.find(i => i.orderId === val)
  if (item) {
    form.value.invoiceAmount = item.payMoney
    currentUserId.value = item.userId
  }
}

// 选择结算单 → 同时存姓名
const selectSettle = (val) => {
  const item = settleList.value.find(i => i.settleNo === val)
  if (item) {
    form.value.invoiceAmount = item.riderCommission
    currentRiderId.value = item.riderId
    currentRiderName.value = item.riderName // 关键：存姓名
  }
}

// 提交开票
const submit = async () => {
  if (form.value.applyType === 1 && !form.value.orderId) {
    return ElMessage.warning('请选择订单')
  }
  if (form.value.applyType === 2 && !form.value.settleNo) {
    return ElMessage.warning('请选择结算单')
  }

  const params = {
    applyType: form.value.applyType,
    targetId: form.value.applyType === 1 ? currentUserId.value : currentRiderId.value,
    riderName: currentRiderName.value, // 关键：传给后端！
    orderId: form.value.orderId,
    settleNo: form.value.settleNo,
    invoiceAmount: form.value.invoiceAmount,
    invoiceTitle: form.value.invoiceTitle,
    taxNo: form.value.taxNo
  }

  await request.post('/finance/invoice/apply', params)
  ElMessage.success('开票申请成功')
  dialogVisible.value = false
  getList()
}

const done = async (id) => {
  await request.put('/finance/invoice/done', null, {params: {id}})
  ElMessage.success('已开票')
  getList()
}

const del = async (id) => {
  await ElMessageBox.confirm('确定删除该开票记录？', '提示', {type: 'warning'})
  await request.delete('/finance/invoice/delete', {params: {id}})
  ElMessage.success('删除成功')
  getList()
}

onMounted(() => getList())
</script>