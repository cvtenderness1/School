<template>
  <div style="padding: 20px; background: #fff9e6; min-height: calc(100vh - 80px)">
    <el-card shadow="hover" style="margin-bottom: 20px">
      <h3>骑手佣金批量打款</h3>
    </el-card>

    <el-card shadow="hover">
      <el-table :data="waitList" border stripe ref="tableRef">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="settleNo" label="结算单号" />
        <el-table-column prop="riderCommission" label="打款金额" />
        <el-table-column prop="riderId" label="骑手ID" />
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag type="warning">待打款</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 15px; text-align: right">
        <el-button style="background-color: #ff9f00; border-color: #ff9f00; color: #fff;" @click="batchPay">批量确认打款</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import request from '@/utils/request.js'
import {ElMessage} from 'element-plus'

const waitList = ref([])
const tableRef = ref(null)

const getList = async () => {
  const res = await request.get('/finance/settlement/list', {params: {status: 1}})
  waitList.value = res.data
}

const batchPay = async () => {
  if (!tableRef.value) return ElMessage.warning('表格加载中')
  const rows = tableRef.value.getSelectionRows()
  if (!rows.length) return ElMessage.warning('请选择要打款的结算单')

  const nos = rows.map(i => i.settleNo)
  await request.post('/finance/pay/batch', nos)
  ElMessage.success('打款成功！')
  getList()
}

onMounted(() => getList())
</script>