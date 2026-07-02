<template>
  <div>
    <el-card>
      <el-input style="width: 200px" v-model="data.username" placeholder="输入查询" :prefix-icon="Search"></el-input>
      <el-button type="primary" @click="load" :icon="Search">查询</el-button>
      <el-button type="warning" @click="reset" :icon="Refresh">重置</el-button>
    </el-card>
    <el-card>
      <el-button type="primary" @click="HandAdd" :icon="Plus">新增</el-button>
      <el-button type="danger" @click="HandDelete" :icon="Delete">批量删除</el-button>
    </el-card>
    <el-card>
      <el-table :data="data.tabledata" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="id"/>
        <el-table-column label="用户头像">
          <template #default="scope">
            <el-image
                style="width: 50px; height: 50px;border-radius: 50%"
                :src="scope.row.avatarUrl"
                :preview-src-list="[scope.row.avatarUrl]"
                :preview-teleported="true"
                fit="cover"
            ></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="身份" />
        <el-table-column prop="age" label="年龄" />
        <el-table-column prop="nickname" label="姓名" />
        <el-table-column prop="tel" label="电话" />
        <el-table-column prop="email" show-overflow-tooltip label="邮箱" />
        <el-table-column prop="address" show-overflow-tooltip label="地址" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status==='启用'">启用</el-tag>
            <el-tag type="danger" v-if="scope.row.status==='禁用'">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" align="center"  width="160" label="创建时间" />
        <el-table-column fixed="right" label="操作" width="200" align="center" >
          <template #default="scope">
            <el-button type="primary" icon="Edit" @click="HandleEdit(scope.row)" size="small">编辑</el-button>
            <!-- 判断：如果是当前登录用户，删除按钮禁用 -->
            <el-button
                type="danger"
                icon="Delete"
                @click="deletesave(scope.row.id)"
                size="small"
                :disabled="scope.row.id === loginUserId.value"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px">
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
    </el-card>

    <el-dialog v-model="data.dialogFormVisible" title="人员信息" width="550" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form">
        <el-form-item label="头像" label-width="80px">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:1000/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="data.form.avatarUrl" :src="data.form.avatarUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="username" label="用户名" label-width="80px">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="年龄" label-width="80px">
          <el-input-number
              v-model="data.form.age"
              :min="1"
              :max="120"
              :step="1"
              placeholder="请输入年龄"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="密码" label-width="80px" >
          <el-input v-model="data.form.password" autocomplete="off" placeholder="默认为123456"/>
        </el-form-item>
        <el-form-item label="电话" label-width="80px">
          <el-input v-model="data.form.tel" autocomplete="off" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item prop="nickname" label="姓名" label-width="80px">
          <el-input v-model="data.form.nickname" autocomplete="off" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" label-width="80px">
          <el-input v-model="data.form.email" autocomplete="off" placeholder="请输入邮箱"/>
        </el-form-item>
        <el-form-item prop="address" label="地址" label-width="80px" >
          <el-input v-model="data.form.address" autocomplete="off" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="身份" label-width="80px">
          <el-select
              v-model="data.form.roles"
              placeholder="请选择身份"
              style="width: 100%;"
          >
            <el-option label="管理员" value="管理员"></el-option>
            <el-option label="用户" value="用户"></el-option>
            <el-option label="外卖员" value="外卖员"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" label-width="80px">
          <!-- 自己的账号 → 状态禁用不能改 -->
          <el-radio-group v-model="data.form.status" :disabled="data.form.id === loginUserId.value">
            <el-radio label="启用"></el-radio>
            <el-radio label="禁用"></el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessageBox, ElMessage } from "element-plus";
import { Search, Refresh, Plus, Delete } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {reactive, ref, computed} from "vue";

// 获取当前登录用户ID
const loginUserId = computed(() => {
  const admin = JSON.parse(localStorage.getItem("xm-pro-admin") || "{}");
  return admin.id || "";
});

const data = reactive({
  username: null,
  tabledata: [],
  pagenum: 1,
  pagesize: 10,
  total: 40,
  dialogFormVisible: false,
  DeleteFormVisible: false,
  form: {},
  deleteId: "",
  ids: [],
  rules:{
    username:[
      {required:true,message:'请输入账号',trigger:'blur'}
    ],
    address:[
      {required:true,message:'请输入地址',trigger:'blur'}
    ],
    nickname:[
      {required:true,message:'请输入姓名',trigger:'blur'}
    ],
  }
});
const emit = defineEmits(["update"]);

const formRef = ref()
const load = () => {
  request.get('/admin/page', {
    params: {
      pageNum: data.pagenum,
      pageSize: data.pagesize,
      username: data.username,
    }
  }).then((res) => {
    data.tabledata = res.data.list
    data.total = res.data.total
  })
}
load()

const reset = () => {
  data.username = null
  load()
}
const handleAvatarSuccess = (res)=>{
  data.form.avatarUrl = res.data
}
const HandAdd = () => {
  data.dialogFormVisible = true
}

// 批量删除：过滤掉当前登录用户ID
const HandDelete = () => {
  if (data.ids.length === 0) {
    ElMessage.error("请选择要删除的数据")
    return
  }

  const canDeleteIds = data.ids.filter(id => id !== loginUserId.value);

  if(canDeleteIds.length === 0){
    ElMessage.error("不能删除当前登录账号！");
    return;
  }

  request.delete("/admin/delBatch", { data: canDeleteIds }).then((res) => {
    if (res.code == '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      // 禁止修改自己的状态
      if (data.form.id === loginUserId.value) {
        const oldStatus = data.tabledata.find(item => item.id === data.form.id)?.status;
        if(data.form.status !== oldStatus) {
          data.form.status = oldStatus; // 强制恢复原来的状态
          ElMessage.error("禁止修改状态！");
          return
        }else{
          data.form.id ? update() : add()
          return
        }

      }
      data.form.id ? update() : add()
    }
  })
}

const HandleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.dialogFormVisible = true
}

const add = () => {
  request.post('/admin/add', data.form).then((res) => {
    if (res.code === '200') {
      data.dialogFormVisible = false
      ElMessage.success('新增成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/admin/update', data.form).then((res) => {
    if (res.code === '200') {
      data.dialogFormVisible = false;

      if (data.form.id === loginUserId.value) {
        localStorage.setItem('xm-pro-admin', JSON.stringify(data.form));
      }
      emit('update'); // 通知父页面刷新
      ElMessage.success('修改成功');
      load();
    } else {
      ElMessage.error(res.msg);
    }
  })
}

// 单个删除：判断是否是自己
const deletesave = (rowId) => {
  // 禁止删除自己
  if(rowId === loginUserId.value){
    ElMessage.error("不能删除当前登录账号！");
    return;
  }

  ElMessageBox.confirm(
      '此操作将永久删除该数据，删除后无法恢复！',
      '温馨提示',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    request.delete('/admin/delBatch', { data: [Number(rowId)] }).then((res) => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(row => row.id)
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>