<template>
  <div class="page-container">
    <div class="top-bar">
      <el-button type="primary" class="btn-orange" @click="openAddDialog">新增订单</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleDelBatch">批量删除</el-button>
    </div>

    <div class="search-section">
      <el-form :inline="true" :model="queryParams" class="custom-search-form">
        <el-form-item label="订单类型">
          <el-radio-group v-model="queryParams.orderType" @change="loadData" id="orderType">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button :label="1">外卖单</el-radio-button>
            <el-radio-button :label="2">跑腿单</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="queryParams.orderStatus"
              placeholder="全部"
              clearable
              style="width:130px"
              @change="loadData"
          >
            <el-option label="待付款" :value="0" />
            <el-option label="待接单" :value="1" />
            <el-option label="待配送" :value="2" />
            <el-option label="配送中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="queryParams.orderId"
              placeholder="订单号"
              prefix-icon="Search"
              clearable
              @keyup.enter="loadData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-orange" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" style="width:100%" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="orderId" label="订单编号" width="180" />
      <el-table-column label="类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.orderType === 1 ? 'warning' : 'success'">
            {{ scope.row.orderType === 1 ? '外卖' : '跑腿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="160">
        <template #default="scope">
          <div style="line-height:1.4">
            <div style="color:#f56c6c;font-weight:bold">￥{{ scope.row.totalAmount }}</div>
            <div style="font-size:12px;color:#999">配送 ￥{{ scope.row.deliveryFee }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.orderStatus)">{{ getStatusText(scope.row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.orderStatus === 1" size="small" class="btn-orange" @click="openAssign(scope.row)">指派骑手</el-button>
          <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.orderStatus < 3" size="small" type="danger" link @click="handleCancel(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 指派骑手弹窗 -->
    <el-dialog v-model="assignDialog" title="指派骑手" width="400px">
      <el-select v-model="current.deliveryId" placeholder="请选择骑手" style="width:100%">
        <!-- 循环渲染真实骑手列表 -->
        <el-option
            v-for="rider in riderList"
            :key="rider.riderId"
            :label="`${rider.name}（${rider.phone}）`"
            :value="rider.riderId"
        />
      </el-select>
      <template #footer>
        <el-button @click="assignDialog=false">取消</el-button>
        <el-button class="btn-orange" @click="submitAssign">确认派单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addDialog" title="创建新订单" width="680px" append-to-body>
      <el-form label-width="80px" :model="addForm">
        <el-form-item label="订单类型">
          <el-radio-group v-model="addForm.orderType" id="addOrderType">
            <el-radio :label="1">外卖订单</el-radio>
            <el-radio :label="2">跑腿订单</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 用户ID 下拉选择框 -->
        <el-form-item label="选择用户">
          <el-select
              v-model="addForm.userId"
              placeholder="请选择用户"
              style="width: 100%"
              filterable
              @change="loadUserAddress"
          >
            <el-option
                v-for="user in userList"
                :key="user.id"
                :label="`${user.nickname || user.username} (ID:${user.id})`"
                :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择商家" v-if="addForm.orderType === 1">
          <el-select
              v-model="addForm.merchantId"
              placeholder="请选择商家"
              style="width:100%"
              @change="loadMerchantGoods"
          >
            <el-option v-for="m in merchantList" :key="m.merchantId" :label="m.merchantName" :value="m.merchantId"></el-option>
          </el-select>
        </el-form-item>

        <!-- 选择菜品（自动加载当前商家） -->
        <el-form-item label="选择菜品" v-if="addForm.orderType === 1 && addForm.merchantId">
          <el-select
              v-model="addForm.goodsId"
              placeholder="请点选菜品"
              style="width:100%"
              @change="selectGood"
          >
            <el-option
                v-for="good in goodsList"
                :key="good.goodsId"
                :label="`${good.goodsName} - ￥${good.discountPrice || good.price}`"
                :value="good.goodsId"
            />
          </el-select>
        </el-form-item>

        <!-- 数量选择 -->
        <el-form-item label="购买数量" v-if="addForm.orderType === 1 && addForm.goodsId">
          <el-input-number
              v-model="addForm.buyCount"
              :min="1"
              :max="maxStock"
              style="width: 100%"
              @change="recalculateTotal"
          />
          <div style="font-size:12px;color:#666;margin-top:5px">
            库存剩余：{{ maxStock }} 件
          </div>
        </el-form-item>

        <!-- 自动加载地址（只读） -->
        <el-form-item label="收货地址">
          <el-input
              v-model="addForm.addressText"
              type="textarea"
              rows="2"
              placeholder="请先选择用户，自动加载地址"
              readonly
          />
        </el-form-item>

        <el-form-item label="总金额">
          <el-input-number v-model="addForm.totalAmount" :precision="2" :min="0" :step="0.1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="配送费">
          <el-input-number v-model="addForm.deliveryFee" :precision="2" :min="0" :step="0.1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="跑腿类型" v-if="addForm.orderType === 2">
          <el-select v-model="addForm.errandType" placeholder="请选择跑腿类型" style="width:100%">
            <el-option label="代取快递" :value="1" />
            <el-option label="代买" :value="2" />
            <el-option label="代送" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <!-- 跑腿任务专用：取件地址 + 送达地址 -->
        <el-form-item label="取件地址" v-if="addForm.orderType === 2">
          <el-input v-model="addForm.startAddress" type="textarea" rows="2" placeholder="请输入取件地址" />
        </el-form-item>

        <el-form-item label="送达地址" v-if="addForm.orderType === 2">
          <el-input v-model="addForm.endAddress" type="textarea" rows="2" placeholder="请输入送达地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.note" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog=false">取消</el-button>
        <el-button class="btn-orange" @click="submitAdd">创建订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, watch} from 'vue';
import request from '@/utils/request.js';
import {ElMessage, ElMessageBox} from 'element-plus';

const queryParams = reactive({orderType: '', orderStatus: '', orderId: ''});
const tableData = ref([]);
const merchantList = ref([]);
const userList = ref([]);
const riderList = ref([]);
const selectedIds = ref([]);
const goodsList = ref([]);
const maxStock = ref(999);

const assignDialog = ref(false);
const current = reactive({orderId: '', deliveryId: null});

const addDialog = ref(false);
const addForm = reactive({
  orderType: 1,
  userId: null,
  goodsId: null,
  merchantId: null,
  buyCount: 1,
  addressId: '',
  addressText: '',
  totalAmount: 0,
  deliveryFee: 0,
  note: '',
  startAddress: '',
  endAddress: '',
  errandType: 4
});

watch(() => addForm.orderType, (newVal) => {
  addForm.merchantId = null;
  addForm.goodsId = null;
  addForm.buyCount = 1;
  goodsList.value = [];
});

// 加载商家菜品
const loadMerchantGoods = () => {
  if (!addForm.merchantId) {
    goodsList.value = [];
    return;
  }
  request.get('/goods/list', { params: { merchantId: addForm.merchantId } }).then(res => {
    goodsList.value = res.data;
  });
};

// 选择菜品自动填充价格 & 库存
const selectGood = () => {
  const good = goodsList.value.find(g => g.goodsId === addForm.goodsId);
  if (good) {
    addForm.buyCount = 1;
    maxStock.value = good.stock || 999;
    recalculateTotal();
  }
};

// 重新计算总价
const recalculateTotal = () => {
  const good = goodsList.value.find(g => g.goodsId === addForm.goodsId);
  if (good) {
    const price = good.discountPrice || good.price;
    addForm.totalAmount = (price * addForm.buyCount).toFixed(2);
  }
};

// 加载所有骑手
const loadAllRider = () => {
  request.get('/rider/selectAll')
      .then(res => {
        riderList.value = Array.isArray(res.data) ? res.data : [];
      })
      .catch(() => {
        ElMessage.error('骑手列表加载失败');
        riderList.value = [];
      });
};

// 加载订单列表
const loadData = () => {
  request.get('/order/list', {params: queryParams}).then(res => {
    tableData.value = res.data;
  });
};

// 加载商家列表
const loadMerchant = () => {
  request.get('/merchant/selectall').then(res => {
    merchantList.value = res.data;
  });
};

// 加载所有用户
const loadAllUser = () => {
  request.get('/user/list').then(res => {
    let list = Array.isArray(res.data) ? res.data : [];
    // 过滤掉 status 为 "禁用" 的用户
    userList.value = list.filter(item => item.status !== '禁用');
  }).catch(() => {
    ElMessage.error('用户列表加载失败');
    userList.value = [];
  });
};

// 自动加载用户地址
const loadUserAddress = () => {
  const userId = addForm.userId;

  if (!userId) {
    addForm.addressText = '';
    addForm.addressId = '';
    return;
  }

  request.get(`/user/selectById/${userId}`).then(res => {
    const user = res.data;
    if (user) {
      addForm.addressText = user.address || '未填写地址';
      addForm.addressId = user.address || '';
    } else {
      addForm.addressText = '用户不存在';
      addForm.addressId = '';
    }
  }).catch(() => {
    ElMessage.error('获取用户信息失败');
    addForm.addressId = '';
  });
};

const getStatusText = s => ({0: '待付款', 1: '待接单', 2: '待配送', 3: '配送中', 4: '已完成', 5: '已取消', 6: '已完成'}[s]);
const getStatusType = s => ({0: 'info', 1: 'danger', 2: 'warning', 3: '', 4: 'success', 5: 'info', 6: 'success'}[s]);

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.orderId);
};

const handleDelBatch = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个订单吗？`, '警告', {type: 'warning'})
      .then(() => {
        request.delete('/order/delBatch', {data: selectedIds.value})
            .then(res => {
              ElMessage.success('批量删除成功');
              loadData();
            })
            .catch(err => {
              ElMessage.error('删除失败');
            });
      });
};

const openAssign = row => {
  current.orderId = row.orderId;
  current.deliveryId = null;
  assignDialog.value = true;
};

const submitAssign = () => {
  if(!current.deliveryId){
    ElMessage.warning('请选择骑手');
    return;
  }
  request.put('/order/assignRider', current)
      .then(() => {
        ElMessage.success('指派成功');
        assignDialog.value = false;
        loadData();
      })
      .catch(() => ElMessage.error('派单失败'));
};

const handleCancel = row => {
  ElMessageBox.confirm('确定取消该订单？')
      .then(() => {
        request.put('/order/cancel', {orderId: row.orderId})
            .then(() => {
              ElMessage.success('已取消');
              loadData();
            })
            .catch(() => ElMessage.error('取消失败'));
      });
};

const openAddDialog = () => {
  addDialog.value = true;
};

const submitAdd = () => {
  if (!addForm.userId) {
    ElMessage.warning('请选择用户');
    return;
  }
  if (addForm.orderType === 1 && !addForm.merchantId) {
    ElMessage.warning('外卖订单必须选择商家');
    return;
  }
  if (addForm.orderType === 1 && !addForm.goodsId) {
    ElMessage.warning('请选择菜品');
    return;
  }
  if (addForm.totalAmount <= 0) {
    ElMessage.warning('总金额必须大于0');
    return;
  }

  // 创建订单后扣减库存
  request.post('/order/add', addForm)
      .then(res => {
        if (addForm.orderType === 1) {
          request.post('/goods/stock/deduct', {
            goodsId: addForm.goodsId,
            count: addForm.buyCount
          }).then(() => {
            ElMessage.success('订单创建成功，库存已更新');
          });
        } else {
          ElMessage.success('订单创建成功');
        }
        addDialog.value = false;
        loadData();
        Object.assign(addForm, {
          orderType: 1,
          userId: null,
          merchantId: null,
          goodsId: null,
          buyCount: 1,
          addressId: '',
          addressText: '',
          totalAmount: 0,
          deliveryFee: 0,
          note: '',
          startAddress: '',
          endAddress: '',
          errandType: 4
        });
      })
      .catch(() => ElMessage.error('创建失败'));
};

const viewDetail = (row) => {
  request.get('/order/selectById/' + row.orderId).then(res => {
    const items = res.data || []

    let content = `<h4>订单号：${row.orderId}</h4>`

    // 跑腿单：显示跑腿信息
    if (row.orderType === 2) {
      request.get('/order/errand/' + row.orderId).then(eres => {
        const err = eres.data
        content += `
          <div style="padding: 10px; background: #f9f9f9; border-radius: 8px;">
            <div><b>类型：</b> ${err.errandType == 1 ? '代取快递' : err.errandType == 2 ? '代买' : err.errandType == 3 ? '代送' : '其他'}</div>
            <div><b>取件：</b>${err.startAddress}</div>
            <div><b>送达：</b>${err.endAddress}</div>
            <div><b>备注：</b>${err.remark || '无'}</div>
          </div>
        `

        ElMessageBox.alert(content, '订单详情', {
          dangerouslyUseHTMLString: true
        })
      })
      return
    }

    // ==============================================
    // 外卖单：显示商品
    // ==============================================
    content += '<div style="margin:10px 0;">'
    if (items.length === 0) {
      content += '暂无商品明细'
    } else {
      items.forEach(it => {
        content += `
          <div style="border-bottom:1px solid #eee;padding:8px 0;">
            <div>商品：${it.goodsName}</div>
            <div>总价：￥${it.price}</div>
            <div>数量：${it.quantity}</div>
          </div>
        `
      })
    }
    content += '</div>'

    ElMessageBox.alert(content, '订单详情', {
      dangerouslyUseHTMLString: true
    })
  })
}
onMounted(() => {
  loadData();
  loadMerchant();
  loadAllUser();
  loadAllRider();
});
</script>

<style scoped>
.top-bar {
  margin-bottom: 14px;
  display: flex;
  gap: 10px;
}

.search-section {
  background: #fcfcfc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.btn-orange {
  background: #ff9900 !important;
  border-color: #ff9900 !important;
  color: #fff !important;
}

:deep(.el-table__row) {
  height: 80px;
}
</style>