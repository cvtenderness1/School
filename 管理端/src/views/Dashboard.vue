<template>
  <div class="dashboard-page" style="padding: 20px">
    <h2 style="margin-bottom: 20px">平台数据看板</h2>

    <!-- 顶部数据卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="card">
          <div class="box">
            <div class="label">今日订单</div>
            <div class="value text-orange">{{ dataStat.todayOrder }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="card">
          <div class="box">
            <div class="label">总流水</div>
            <div class="value text-green">¥{{ dataStat.totalIncome }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 待处理客诉 → 点击可打开处理弹窗 -->
      <el-col :span="6">
        <el-card class="card" @click="openComplaintDialog" style="cursor:pointer">
          <div class="box">
            <div class="label">待处理客诉</div>
            <div class="value text-red">{{ dataStat.complaint }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="card">
          <div class="box">
            <div class="label">在线骑手</div>
            <div class="value text-blue">{{ dataStat.onlineRider }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近7天订单 -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <span>近7天订单趋势</span>
      </template>
      <el-table :data="orderTrend" border size="small">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="orderCount" label="订单数量" />
      </el-table>
    </el-card>

    <!-- 骑手排行 + 统计 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>骑手绩效排行</span>
          </template>
          <el-table :data="riderPerformance" border size="small">
            <el-table-column prop="name" label="骑手" />
            <el-table-column prop="total_order_count" label="完成订单" />
            <el-table-column prop="avg_score" label="平均评分" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>客诉统计</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="总售后单">{{ complaintStats.total_after_sale }}</el-descriptions-item>
            <el-descriptions-item label="待处理">{{ complaintStats.pending }}</el-descriptions-item>
            <el-descriptions-item label="差评数">{{ complaintStats.bad_comment_count }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理客诉处理弹窗 -->
    <el-dialog v-model="complaintDialogVisible" title="待处理客诉列表" width="70%">
      <el-table :data="pendingComplaintList" border size="small">
        <el-table-column label="订单号" prop="orderId" />
        <el-table-column label="类型">
          <template #default="scope">
            {{ scope.row.type === 2 ? '退款' : scope.row.type === 3 ? '投诉差评' : '其他' }}
          </template>
        </el-table-column>
        <el-table-column label="原因" prop="reason" />
        <el-table-column label="申请时间" prop="createTime" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleComplaint(scope.row.id, 2)">
              同意处理
            </el-button>
            <el-button type="danger" size="small" @click="handleComplaint(scope.row.id, 3)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

// 数据面板
const dataStat = ref({
  todayOrder: 0,
  totalIncome: 0,
  complaint: 0,
  onlineRider: 0
})

// 订单趋势
const orderTrend = ref([])
// 骑手绩效
const riderPerformance = ref([])
// 售后统计
const complaintStats = ref({})
// 今日收入
const todayIncome = ref(0)

// 客诉弹窗 + 列表
const complaintDialogVisible = ref(false)
const pendingComplaintList = ref([])

onMounted(() => {
  loadData()
})

// 加载所有看板数据
const loadData = () => {
  // 核心指标
  request.get('/dashboard/core/metrics').then(res => {
    dataStat.value = {
      todayOrder: res.data.total_order || 0,
      totalIncome: res.data.total_income || 0,
      complaint: res.data.pending_complaint || 0,
      onlineRider: res.data.online_rider_count || 0
    }
  })

  // 订单趋势
  request.get('/dashboard/order/heatmap', { params: { days: 7 } }).then(res => {
    orderTrend.value = res.data.map(i => ({
      date: i.date,
      orderCount: i.order_count
    }))
  })

  // 骑手绩效
  request.get('/dashboard/rider/performance').then(res => {
    riderPerformance.value = res.data
  })

  // 客诉统计
  request.get('/dashboard/complaint/stats').then(res => {
    complaintStats.value = res.data
  })
}

// 打开客诉处理弹窗
const openComplaintDialog = () => {
  complaintDialogVisible.value = true
  // 获取待处理客诉列表
  request.get('/dashboard/complaint/pending').then(res => {
    pendingComplaintList.value = res.data
  })
}

// 处理客诉：status 2=已完成 3=已驳回
const handleComplaint = async (id, status) => {
  await request.post('/dashboard/complaint/handle', { id, status })
  ElMessage.success('处理成功')
  // 刷新列表 & 刷新看板数据
  openComplaintDialog()
  loadData()
}
</script>

<style scoped>
.card {
  border-radius: 8px;
}
.box {
  text-align: center;
  padding: 10px 0;
}
.label {
  font-size: 14px;
  color: #666;
}
.value {
  font-size: 22px;
  font-weight: bold;
  margin-top: 6px;
}
.text-orange { color: #ff9900; }
.text-green { color: #00b42a; }
.text-red { color: #f53f3f; }
.text-blue { color: #1677ff; }
</style>