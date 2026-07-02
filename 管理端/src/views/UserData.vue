<template>
  <div>
    <el-card>
      <el-input style="width: 200px" v-model="data.username" placeholder="输入账号查询" :prefix-icon="Search"></el-input>
      <el-button type="primary" @click="load" :icon="Search" style="margin-left: 10px;">查询</el-button>
      <el-button type="warning" @click="reset" :icon="Refresh">重置</el-button>
    </el-card>

    <el-card style="margin-top: 10px;">
      <el-button type="primary" @click="HandAdd" :icon="Plus">新增</el-button>
      <el-button type="danger" @click="HandDelete" :icon="Delete">批量删除</el-button>
    </el-card>

    <el-card style="margin-top: 10px;">
      <el-table :data="data.tabledata" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户头像" width="100">
          <template #default="scope">
            <el-image
                style="width: 50px; height: 50px; border-radius: 50%"
                :src="scope.row.avatarUrl"
                :preview-src-list="[scope.row.avatarUrl]"
                :preview-teleported="true"
                fit="cover"
            ></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="nickname" label="姓名" />
        <el-table-column prop="tel" label="电话" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="email" show-overflow-tooltip label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status === '启用'">启用</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '禁用'">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />

        <el-table-column fixed="right" label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" icon="Edit" @click="HandleEdit(scope.row)" size="small" plain>编辑</el-button>
            <el-button type="danger" icon="Delete" @click="deletesave(scope.row.id)" size="small" plain>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
            @size-change="load"
            @current-change="load"
            v-model:current-page="data.pagenum"
            v-model:page-size="data.pagesize"
            :page-sizes="[5, 10, 15, 20]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="data.total"
        />
      </div>
    </el-card>

    <el-dialog v-model="data.dialogFormVisible" title="用户信息" width="550px" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding-right: 30px;">

        <!-- 头像上传组件（替换了原来的输入框） -->
        <el-form-item label="头像">
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

        <el-form-item prop="username" label="用户名">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item label="密码" v-if="!data.form.id">
          <el-input v-model="data.form.password" autocomplete="off" placeholder="默认为123456" />
        </el-form-item>

        <el-form-item prop="nickname" label="姓名">
          <el-input v-model="data.form.nickname" autocomplete="off" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="电话">
          <el-input v-model="data.form.tel" autocomplete="off" placeholder="请输入电话" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="data.form.email" autocomplete="off" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="data.form.address" autocomplete="off" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="data.form.status">
            <el-radio label="启用">启用</el-radio>
            <el-radio label="禁用">禁用</el-radio>
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
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { Search, Refresh, Plus, Delete } from "@element-plus/icons-vue";
import request from "@/utils/request.js";

const data = reactive({
  username: '',
  tabledata: [],
  pagenum: 1,
  pagesize: 10,
  total: 0,
  dialogFormVisible: false,
  form: {},
  ids: [],
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    nickname: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ]
  }
});

const formRef = ref();

// 加载表格数据
const load = () => {
  request.get('/user/page', {
    params: {
      pageNum: data.pagenum,
      pageSize: data.pagesize,
      username: data.username,
    }
  }).then((res) => {
    if (res.code === '200') {
      data.tabledata = res.data.list;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};
load();

// 重置查询
const reset = () => {
  data.username = '';
  load();
};

// 头像上传成功
const handleAvatarSuccess = (res)=>{
  data.form.avatarUrl = res.data;
}

// 新增弹窗
const HandAdd = () => {
  data.dialogFormVisible = true;
  data.form = {
    avatarUrl: "",
    status: '启用'
  };
};

// 编辑弹窗
const HandleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.dialogFormVisible = true;
};

// 提交保存 (校验表单后判断是新增还是更新)
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      data.form.id ? update() : add();
    }
  });
};

const add = () => {
  request.post('/user/add', data.form).then((res) => {
    if (res.code === '200') {
      data.dialogFormVisible = false;
      ElMessage.success('新增成功');
      load();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const update = () => {
  request.put('/user/update', data.form).then((res) => {
    if (res.code === '200') {
      data.dialogFormVisible = false;
      ElMessage.success('修改成功');
      load();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 单个删除
const deletesave = (id) => {
  ElMessageBox.confirm(
      '此操作将永久删除该数据，删除后无法恢复！',
      '温馨提示',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    request.delete('/user/delBatch', { data: [id] }).then((res) => {
      if (res.code === '200') {
        ElMessage.success('删除成功');
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 处理多选
const handleSelectionChange = (rows) => {
  data.ids = rows.map(row => row.id);
};

// 批量删除
const HandDelete = () => {
  if (data.ids.length === 0) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }
  ElMessageBox.confirm(
      `确定要删除选中的 ${data.ids.length} 条数据吗？`,
      '批量删除提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    request.delete("/user/delBatch", { data: data.ids }).then((res) => {
      if (res.code === '200') {
        ElMessage.success('批量删除成功');
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};
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