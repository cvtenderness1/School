<template>
  <div style="padding: 20px; background: #fff9e6; min-height: calc(100vh - 80px)">
    <el-card shadow="hover" style="margin-bottom: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div><h3>骑手结算管理</h3></div>
        <div>
          <el-button style="background: #ff9f00; border-color: #ff9f00; color: #fff;" @click="generate">生成周期结算单</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover">
      <el-form :inline="true" :model="query" style="margin-bottom: 15px">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择">
            <el-option label="全部" value="-1"></el-option>
            <el-option label="待审核" value="0"></el-option>
            <el-option label="待打款" value="1"></el-option>
            <el-option label="已打款" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button style="background: #ff9f00; border-color: #ff9f00; color: #fff;" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border stripe>
        <el-table-column prop="settleNo" label="结算单号" />
        <el-table-column prop="riderId" label="骑手ID" />
        <el-table-column prop="totalOrderNum" label="完成订单数" />
        <el-table-column prop="totalOrderAmount" label="订单总金额" />
        <el-table-column prop="platformCommission" label="平台商家抽成" />
        <el-table-column prop="riderCommission" label="骑手佣金" />
        <el-table-column prop="createTime" label="生成时间" />
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 2 ? 'success' : scope.row.status === 1 ? 'warning' : 'info'">
              {{ scope.row.status === 0 ? '待审核' : scope.row.status === 1 ? '待打款' : '已打款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作"  align="center">
          <template #default="scope">
            <el-button type="primary" link @click="audit(scope.row.settleNo, 1)">
              审核通过
            </el-button>
            <el-button type="danger" link @click="audit(scope.row.settleNo, 3)">
              作废
            </el-button>

            <el-button type="danger" link @click="deleteSettle(scope.row.settleNo)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const query = ref({ status: -1 })

const getList = async () => {
  const res = await request.get('/finance/settlement/list', { params: query.value })
  list.value = res.data
}

const generate = async () => {
  await request.post('/finance/settlement/generate', null, {
    params: { startDate: '2026-01-01', endDate: '2026-12-31' }
  })
  ElMessage.success('生成成功')
  getList()
}

const audit = async (settleNo, status) => {
  await request.put('/finance/settlement/audit', null, { params: { settleNo, status } })
  ElMessage.success('操作成功')
  getList()
}

const deleteSettle = async (settleNo) => {
  await ElMessageBox.confirm('确定要删除该结算单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  // 调用删除接口
  await request.delete('/finance/settlement/delete', { params: { settleNo } })
  ElMessage.success('删除成功')
  getList()
}

onMounted(() => getList())
</script>