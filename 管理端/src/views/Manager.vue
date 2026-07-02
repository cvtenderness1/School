<template>
  <div class="main-layout">
    <header class="admin-header">
      <div class="logo-area">
        <div class="logo-box">
          <el-icon><Bicycle /></el-icon>
        </div>
        <span class="logo-text">校园跑腿<span class="text-tag">管理端</span></span>
      </div>

      <div class="header-right">
        <div class="status-badge">
          <span class="dot"></span> 平台运行中
        </div>

        <el-dropdown>
          <div class="user-info">
            <el-avatar :size="32" :src="data.admin.avatarUrl" />
            <span class="nickname">{{ data.admin.nickname || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="openProfileDialog">个人中心</el-dropdown-item>
              <el-dropdown-item @click="openPwdDialog">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="main-container">
      <aside class="sidebar">
        <el-menu
            router
            :default-active="route.path"
            class="custom-menu"
        >
          <el-menu-item index="/admin/home">
            <el-icon><House /></el-icon>
            <span>主页</span>
          </el-menu-item>

          <el-menu-item index="/admin/list">
            <el-icon><User /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/order">
            <el-icon><DocumentChecked /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchant">
            <el-icon><Bicycle /></el-icon>
            <span>商家管理</span>
          </el-menu-item>

          <el-sub-menu index="1">
            <template #title>
              <el-icon><User /></el-icon>
              <span>人员管理</span>
            </template>
            <el-menu-item index="/admin/userdata">用户列表</el-menu-item>
            <el-menu-item index="/admin/rider">骑手信息</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/admin/Dashboard">
            <el-icon><Histogram /></el-icon>
            <span>订单看板</span>
          </el-menu-item>

          <el-sub-menu index="2">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>结算</span>
            </template>
            <el-menu-item index="/admin/FinanceSettlement">结算管理</el-menu-item>
            <el-menu-item index="/admin/FinancePayment">批量打款</el-menu-item>
            <el-menu-item index="/admin/FinanceInvoice">开票管理</el-menu-item>
          </el-sub-menu>

<!--          <el-menu-item index="/admin/setting">-->
<!--            <el-icon><Setting /></el-icon>-->
<!--            <span>系统设置</span>-->
<!--          </el-menu-item>-->
        </el-menu>
      </aside>

      <main class="content-wrapper">
        <div class="page-content">
          <RouterView @update = "update"/>
        </div>
      </main>

    </div>

    <!-- 个人中心弹窗 -->
    <el-dialog
        v-model="profileDialogVisible"
        title="个人中心"
        width="500px"
        @close="resetForm"
    >
      <el-form ref="profileFormRef" :model="profileForm" label-width="80px">
        <el-form-item label="头像" label-width="80px">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:1000/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog
        v-model="pwdDialogVisible"
        title="修改密码"
        width="460px"
        @close="resetPwdForm"
    >
      <el-form ref="pwdFormRef" :model="pwdForm" label-width="80px">
        <el-form-item label="原密码" prop="oldPwd">
          <el-input v-model="pwdForm.oldPwd" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPwd">
          <el-input v-model="pwdForm.newPwd" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input v-model="pwdForm.confirmPwd" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePassword">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import request from '@/utils/request.js';
import {
  House, DocumentChecked, User, Setting,
  Bicycle, ArrowDown, Plus
} from "@element-plus/icons-vue";

const router = useRouter();
const route = useRoute();

// 管理员数据
const data = reactive({
  admin: JSON.parse(localStorage.getItem("xm-pro-admin") || '{}'),
  form: {},
});
const update = () => {
  data.admin = JSON.parse(localStorage.getItem("xm-pro-admin"))
};

//个人信息
const profileDialogVisible = ref(false);
const profileFormRef = ref(null);
const profileForm = reactive({
  nickname: "",
  avatarUrl: ""
});

const openProfileDialog = () => {
  request.get('/admin/selectById/' + data.admin.id).then((res) => {
    profileForm.nickname = res.data.nickname;
    profileForm.avatarUrl = res.data.avatarUrl;
  });
  profileDialogVisible.value = true;
};

const saveProfile = () => {
  if (!profileForm.nickname.trim()) {
    ElMessage.warning("昵称不能为空");
    return;
  }
  const updateData = {
    id: data.admin.id,
    nickname: profileForm.nickname,
    avatarUrl: profileForm.avatarUrl,
  };

  request.put('/admin/update', updateData).then((res) => {
    if (res.code === '200') {
      data.admin.nickname = profileForm.nickname;
      data.admin.avatarUrl = profileForm.avatarUrl;
      localStorage.setItem("xm-pro-admin", JSON.stringify(data.admin));

      ElMessage.success('更新成功');
      profileDialogVisible.value = false;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const resetForm = () => {
  profileForm.nickname = data.admin.nickname || "";
  profileForm.avatarUrl = data.admin.avatarUrl || "";
};

const handleAvatarSuccess = (res) => {
  profileForm.avatarUrl = res.data;
};

// 修改密码
const pwdDialogVisible = ref(false);
const pwdFormRef = ref(null);
const pwdForm = reactive({
  oldPwd: "",
  newPwd: "",
  confirmPwd: ""
});

const openPwdDialog = () => {
  pwdDialogVisible.value = true;
};

const resetPwdForm = () => {
  pwdForm.oldPwd = "";
  pwdForm.newPwd = "";
  pwdForm.confirmPwd = "";
};

const savePassword = () => {
  const {oldPwd, newPwd, confirmPwd} = pwdForm;

  if (!oldPwd) return ElMessage.warning("请输入原密码");
  if (!newPwd) return ElMessage.warning("请输入新密码");
  if (newPwd.length < 6) return ElMessage.warning("密码长度至少6位");
  if (newPwd !== confirmPwd) return ElMessage.warning("两次密码不一致");

  request.post("/admin/updatePassword", {
    adminId: data.admin.id,
    oldPassword: oldPwd,
    newPassword: newPwd
  }).then(res => {
    if (String(res.code) === "200") {
      ElMessage.success("密码修改成功，请重新登录");
      setTimeout(() => {
        localStorage.removeItem("xm-pro-admin");
        router.push("/login");
      }, 1000);
      pwdDialogVisible.value = false;
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(err => {
    ElMessage.error("修改失败：" + (err.msg || "系统异常"));
  });
};

// 退出登录
const handleLogout = () => {
  localStorage.removeItem("xm-pro-admin");
  ElMessage.success('已安全退出');
  setTimeout(() => {
    router.push('/login');
  }, 500);
};
</script>
<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7f9;
}

.admin-header {
  height: 64px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 100;
}

.logo-area {
  display: flex;
  align-items: center;
}

.logo-box {
  width: 32px;
  height: 32px;
  background: #FF9900;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: #333;
}

.text-tag {
  font-size: 12px;
  background: #fff2e8;
  color: #FF9900;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
  font-weight: normal;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-badge {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  background: #f0f9eb;
  padding: 4px 12px;
  border-radius: 15px;
  color: #67c23a;
}

.dot {
  width: 6px;
  height: 6px;
  background: #67c23a;
  border-radius: 50%;
  margin-right: 6px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  border-right: 1px solid #eee;
  height: calc(100vh - 64px); /* 减去顶部header高度 */
  overflow-y: auto; /* 垂直滚动 */
}

.custom-menu {
  border-right: none !important;
  padding-top: 10px;
  height: 100%;
}

:deep(.el-menu-item.is-active) {
  background-color: #fff7e6 !important;
  color: #FF9900 !important;
  font-weight: bold;
  border-right: 3px solid #FF9900;
}

:deep(.el-menu-item:hover) {
  background-color: #fafafa !important;
}

.content-wrapper {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.page-content {
  background: #fff;
  min-height: 100%;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.avatar-preview {
  margin-right: 15px;
}

.avatar-upload {
  display: inline-block;
}

.dialog-footer {
  text-align: right;
}

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