<template>
  <div class="page-container">
    <!-- 查询区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="data" class="custom-search-form">
        <el-form-item label="骑手信息">
          <el-input
              v-model="data.account"
              placeholder="账号查询"
              :prefix-icon="Search"
              clearable
          />
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="data.status" placeholder="全部状态" style="width: 120px">
            <el-option label="已认证" :value="1" />
            <el-option label="未认证" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-orange" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="operation-section">
      <el-button type="primary" class="btn-orange-gradient" @click="HandAdd">
        <el-icon><Plus /></el-icon> 新增骑手
      </el-button>
      <el-button type="danger" @click="HandDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>

      <!-- 一键通过认证 -->
      <el-button type="success" @click="batchPassAuth">
        一键通过认证
      </el-button>

      <el-button type="primary" @click="openAllOrder">
        全部订单
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table
        :data="data.tabledata"
        style="width: 100%"
        class="custom-table"
        stripe
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="riderId" label="ID" width="60" />
      <el-table-column prop="account" label="登录账号" />
      <el-table-column prop="name" label="骑手姓名" width="100" />
      <el-table-column prop="studentId" label="身份证" width="120" />
      <el-table-column prop="phone" label="联系电话" width="100" />

      <!-- 认证状态 -->
      <el-table-column label="认证状态" width="80">
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.status === 1">已认证</el-tag>
          <el-tag type="warning" v-if="scope.row.status === 0">未认证</el-tag>
        </template>
      </el-table-column>

      <!-- 冻结状态 -->
      <el-table-column label="冻结状态" width="80">
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.frozen === 0">正常</el-tag>
          <el-tag type="danger" v-if="scope.row.frozen === 1">已冻结</el-tag>
        </template>
      </el-table-column>

      <!-- 信用分 -->
      <el-table-column prop="creditScore" label="信用分" width="70" />

      <el-table-column prop="createTime" label="入驻时间" width="155" />

      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="scope">
          <el-button link type="primary" class="text-orange" @click="HandleEdit(scope.row)">
            <el-icon><EditPen /></el-icon> 编辑
          </el-button>

          <el-button link type="primary" @click="openRiderOrder(scope.row)">
            接单管理
          </el-button>

          <el-button link type="danger" @click="deletesave(scope.row.riderId)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 15px">
      <el-pagination
          @size-change="load"
          @current-change="load"
          v-model:current-page="data.pagenum"
          v-model:page-size="data.pagesize"
          :page-sizes="[5, 10, 15, 20]"
          background
          layout="total,sizes, prev, pager, next, jumper"
          :total="data.total"
      />
    </div>

    <!-- 骑手编辑弹窗 -->
    <el-dialog v-model="data.dialogFormVisible" title="骑手信息" width="600" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form">
        <el-form-item prop="account" label="账号" label-width="80px">
          <el-input v-model="data.form.account" placeholder="请输入账号" autocomplete="off"/>
        </el-form-item>
        <el-form-item prop="password" label="密码" label-width="80px">
          <el-input v-model="data.form.password" placeholder="默认123456" autocomplete="off"/>
        </el-form-item>
        <el-form-item prop="name" label="姓名" label-width="80px">
          <el-input v-model="data.form.name" placeholder="请输入姓名" autocomplete="off"/>
        </el-form-item>
        <el-form-item prop="phone" label="手机号" label-width="80px">
          <el-input v-model="data.form.phone" placeholder="请输入手机号" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="身份证" label-width="80px">
          <el-input v-model="data.form.studentId" placeholder="请输入身份证" autocomplete="off"/>
        </el-form-item>

        <!-- 认证状态 -->
        <el-form-item label="认证状态" label-width="80px">
          <el-radio-group v-model="data.form.status">
            <el-radio :label="1">已认证</el-radio>
            <el-radio :label="0">未认证</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 冻结状态 -->
        <el-form-item label="冻结状态" label-width="80px">
          <el-radio-group v-model="data.form.frozen">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">冻结</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 信用分 -->
        <el-form-item label="信用分" label-width="80px">
          <el-input-number v-model="data.form.creditScore" :min="0" :max="100" style="width: 100%"/>
        </el-form-item>

      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ====================== 接单管理弹窗 ====================== -->
    <el-dialog v-model="orderDialog" title="骑手接单管理" width="90%" top="20px">
      <el-form :inline="true" :model="orderQuery" class="mb-15">
        <el-form-item label="当前订单状态">
          <el-select
              v-model="orderQuery.orderStatus"
              placeholder="查看全部"
              clearable
              @change="handleStatusChange"
              style="width: 150px"
          >
            <el-option label="待配送" :value="2" />
            <el-option label="配送中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
            <el-option label="已评价" :value="6" />
          </el-select>
        </el-form-item>
        <el-button @click="resetOrder" type="primary">重置</el-button>
      </el-form>

      <el-table :data="orderList" border stripe>
        <el-table-column prop="orderId" label="订单号" />
        <el-table-column prop="orderType" label="类型">
          <template #default="scope">{{ scope.row.orderType ==1 ? '外卖' : '跑腿' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" />
        <el-table-column prop="deliveryFee" label="配送费" />
        <el-table-column prop="note" label="备注" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="
        scope.row.orderStatus === 2 ? '' :
        scope.row.orderStatus === 3 ? 'primary' :
        scope.row.orderStatus === 4 ? 'success' :
        scope.row.orderStatus === 5 ? 'danger' :
        scope.row.orderStatus === 6 ? 'success' : ''
      ">
              {{
                scope.row.orderStatus === 2 ? '待配送' :
                    scope.row.orderStatus === 3 ? '配送中' :
                        scope.row.orderStatus === 4 ? '已完成' :
                            scope.row.orderStatus === 5 ? '已取消' :
                                scope.row.orderStatus === 6 ? '已评价' : '未知'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button v-if="scope.row.orderStatus === 2" type="primary" link @click="startDelivery(scope.row)">开始配送</el-button>
            <el-button v-if="scope.row.orderStatus === 3" type="success" link @click="finishDelivery(scope.row)">完成配送</el-button>
            <el-button v-if="scope.row.orderStatus === 3" type="danger" link @click="cancelDelivery(scope.row)">取消配送</el-button>
            <el-button type="primary" link @click="editOrder(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="delOrder(scope.row.orderId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="t-right mt-15">
        <el-pagination
            @size-change="loadOrder"
            @current-change="loadOrder"
            v-model:current-page="orderPageNum"
            v-model:page-size="orderPageSize"
            :total="orderTotal"
            layout="total, sizes, prev, pager, next, jumper"
            background
        />
      </div>
    </el-dialog>

    <!-- 订单编辑弹窗 -->
    <el-dialog v-model="orderEditDialog" title="编辑订单" width="500px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="订单金额">
          <el-input-number v-model="orderForm.totalAmount" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="配送费">
          <el-input-number v-model="orderForm.deliveryFee" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.note" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveOrderEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessageBox, ElMessage } from "element-plus";
import { Search, Refresh, Plus, Delete, EditPen } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { reactive, ref } from "vue";

const data = reactive({
  account: null,
  status: null,
  tabledata: [],
  pagenum: 1,
  pagesize: 10,
  total: 0,
  dialogFormVisible: false,
  form: {
    frozen: 0,
    creditScore: 100
  },
  ids: [],
  rules: {
    account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
});

const formRef = ref();

// 加载骑手
const load = () => {
  request.get("/rider/page", {
    params: {
      pageNum: data.pagenum,
      pageSize: data.pagesize,
      account: data.account,
      status: data.status
    }
  }).then(res => {
    data.tabledata = res.data.list;
    data.total = res.data.total;
  }).catch(() => {
    ElMessage.error("骑手列表加载失败");
  });
};
load();

// 重置
const reset = () => {
  data.account = null;
  data.status = null;
  load();
};

const handleStatusChange = (val) => {
  orderQuery.orderStatus = val
  orderPageNum.value = 1
  loadOrder()
}

// 新增
const HandAdd = () => {
  data.dialogFormVisible = true;
  data.form = {
    password: "123456",
    status: 0,
    frozen: 0,
    creditScore: 100
  };
};

// 保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.riderId ? update() : add();
    } else {
      ElMessage.warning("请完善必填信息");
    }
  });
};

const add = () => {
  request.post("/rider/add", data.form).then(res => {
    if (res.code === "200") {
      ElMessage.success("新增成功");
      data.dialogFormVisible = false;
      load();
    } else {
      ElMessage.error(res.msg || "新增失败");
    }
  }).catch(() => {
    ElMessage.error("新增请求失败");
  });
};

const update = () => {
  request.put("/rider/update", data.form).then(res => {
    if (res.code === "200") {
      ElMessage.success("修改成功");
      data.dialogFormVisible = false;
      load();
    } else {
      ElMessage.error(res.msg || "修改失败");
    }
  }).catch(() => {
    ElMessage.error("修改请求失败");
  });
};

// 编辑
const HandleEdit = row => {
  data.form = { ...row };
  data.dialogFormVisible = true;
};

// 删除
const deletesave = id => {
  ElMessageBox.confirm("确定要删除该骑手吗？", "提示", { type: "warning" })
      .then(() => {
        request.delete("/rider/delBatch", { data: [id] })
            .then(() => {
              ElMessage.success("删除成功");
              load();
            }).catch(() => {
          ElMessage.error("删除失败");
        });
      }).catch(() => {
    ElMessage.info("已取消删除");
  });
};

// 批量删除
const HandDelete = () => {
  if (data.ids.length === 0) return ElMessage.warning("请选择数据");

  ElMessageBox.confirm("确定删除选中骑手？", "提示", { type: "warning" })
      .then(() => {
        request.delete("/rider/delBatch", { data: data.ids })
            .then(() => {
              ElMessage.success("删除成功");
              load();
            }).catch(() => {
          ElMessage.error("批量删除失败");
        });
      }).catch(() => {
    ElMessage.info("已取消操作");
  });
};

const handleSelectionChange = val => {
  data.ids = val.map(i => i.riderId);
};

// 一键通过认证
const batchPassAuth = () => {
  if(data.ids.length === 0) return ElMessage.warning("请选择要认证的骑手")

  ElMessageBox.confirm("确认将选中骑手【一键通过认证】？")
      .then(() => {
        request.put("/rider/batchPassAuth", { ids: data.ids })
            .then(res => {
              if(res.code === '200'){
                ElMessage.success("认证成功")
                load()
              } else {
                ElMessage.error(res.msg || "认证失败")
              }
            }).catch(() => {
          ElMessage.error("认证请求失败")
        })
      }).catch(() => {
    ElMessage.info("已取消认证")
  })
}

// ====================== 接单管理 ======================
const orderDialog = ref(false)
const currentRider = ref({})
const orderList = ref([])
const orderPageNum = ref(1)
const orderPageSize = ref(10)
const orderTotal = ref(0)
const orderQuery = reactive({ orderStatus: null })
const orderEditDialog = ref(false)
const orderForm = ref({})

// 打开当前骑手订单
const openRiderOrder = (row) => {
  currentRider.value = row
  orderDialog.value = true
  orderPageNum.value = 1
  loadOrder()
}

// 打开全部订单
const openAllOrder = () => {
  currentRider.value = null
  orderDialog.value = true
  orderPageNum.value = 1
  loadOrder()
}

// 加载订单
const loadOrder = () => {
  let params = {
    pageNum: orderPageNum.value,
    pageSize: orderPageSize.value,
    orderStatus: orderQuery.orderStatus
  }

  if (currentRider.value) {
    params.deliveryId = currentRider.value.riderId
  }

  request.get("/order/page", { params })
      .then(res => {
        if(res.code === '200') {
          orderList.value = res.data.list
          orderTotal.value = res.data.total
        } else {
          ElMessage.error(res.msg || "订单加载失败")
        }
      }).catch(() => {
    ElMessage.error("订单请求失败")
  })
}

const resetOrder = () => {
  orderQuery.orderStatus = null
  orderPageNum.value = 1
  loadOrder()
}

// 开始配送
const startDelivery = (row) => {
  ElMessageBox.confirm("确认开始配送？")
      .then(() => {
        request.put("/order/updateStatus", {
          orderId: row.orderId,
          orderStatus: 3
        }).then(() => {
          ElMessage.success("开始配送")
          loadOrder()
        }).catch(() => {
          ElMessage.error("操作失败")
        })
      }).catch(() => {
    ElMessage.info("已取消")
  })
}

// 完成配送
const finishDelivery = (row) => {
  ElMessageBox.confirm("确认完成配送？")
      .then(() => {
        request.put("/order/updateStatus", {
          orderId: row.orderId,
          orderStatus: 4
        }).then(() => {
          ElMessage.success("配送完成")
          loadOrder()
        }).catch(() => {
          ElMessage.error("操作失败")
        })
      }).catch(() => {
    ElMessage.info("已取消")
  })
}

// 取消配送
const cancelDelivery = (row) => {
  ElMessageBox.confirm("确认取消配送？")
      .then(() => {
        request.put("/order/updateStatus", {
          orderId: row.orderId,
          orderStatus: 5
        }).then(() => {
          ElMessage.success("已取消配送")
          loadOrder()
        }).catch(() => {
          ElMessage.error("操作失败")
        })
      }).catch(() => {
    ElMessage.info("已取消")
  })
}

// 编辑订单
const editOrder = (row) => {
  orderForm.value = { ...row }
  orderEditDialog.value = true
}

const saveOrderEdit = () => {
  request.put("/order/update", orderForm.value)
      .then(() => {
        ElMessage.success("修改成功")
        orderEditDialog.value = false
        loadOrder()
      }).catch(() => {
    ElMessage.error("修改失败")
  })
}

// 删除订单
const delOrder = (orderId) => {
  ElMessageBox.confirm("确定删除？")
      .then(() => {
        request.delete("/order/del", { params: { orderId } })
            .then(() => {
              ElMessage.success("删除成功")
              loadOrder()
            }).catch(() => {
          ElMessage.error("删除失败")
        })
      }).catch(() => {
    ElMessage.info("已取消删除")
  })
}
</script>

<style scoped>
.search-section {
  background: #fcfcfc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  margin-bottom: 20px;
}
.btn-orange {
  background-color: #ff9900 !important;
  border-color: #ff9900 !important;
  color: #fff !important;
}
.btn-orange-gradient {
  background: linear-gradient(to right, #ffb347, #ff9900) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(255, 153, 0, 0.3);
}
.operation-section {
  margin-bottom: 15px;
}
.text-orange {
  color: #ff9900 !important;
}
:deep(.el-table__row) {
  height: 60px;
}
.mb-15 { margin-bottom: 15px; }
.t-right { text-align: right; }
.mt-15 { margin-top: 15px; }
</style>