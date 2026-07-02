<template>
  <div class="login-container">
    <div class="login-box">
      <div style="padding: 50px 30px; background: #f7f6e7; margin-left: 100px; border-radius: 12px; box-shadow: 0 8px 24px rgba(98, 154, 219, 0.25); border: 1px solid rgba(255, 255, 255, 0.8);">
        <el-form ref="formRef" :rules="data.rules" :model="data.form" style="width: 340px">
          <div style="margin-bottom: 30px; font-size: 24px; text-align: center; color: #f5a623; font-weight: bold; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);">
            跑腿管理系统
          </div>
          <el-form-item prop="username" >
            <el-input size="large" v-model="data.form.username" placeholder="请输入账号" prefix-icon="User" style="--el-input-border-color: #f5a623; --el-input-focus-border-color: #f5a623;"></el-input>
          </el-form-item>
          <el-form-item prop="password" >
            <el-input  size="large" v-model="data.form.password" placeholder="请输入密码" prefix-icon="Lock" show-password style="--el-input-border-color: #f5a623; --el-input-focus-border-color: #f5a623;"></el-input>
          </el-form-item>
          <el-form-item label="" prop="type">
          </el-form-item>
          <div>
            <el-button size="large" style="width: 100%; background: #f5a623; border-color: #f5a623; --el-button-hover-background: #e09a1f; --el-button-hover-border-color: #e09a1f;" @click="handleLogin">登 录</el-button>
          </div>
          <div style="text-align: right; margin-top: 12px;">
            还没有账号？请 <a href="/Register" style="color: #629adb; text-decoration: none; font-weight: 500;">注册</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";


import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const data = reactive({
  form: {
    username: '',
    password: '',
  },
  rules: {
    username: [
      {required: true, message: "请输入账号", trigger: "blur"}
    ],
    password: [
      {required: true, message: "请输入密码", trigger: "blur"}
    ]
  }
});

const formRef = ref(null);

const handleLogin = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/login', data.form).then(async (res) => {

        if (res.code === '200') {
          const { admin, token } = res.data;
          localStorage.setItem('xm-pro-admin', JSON.stringify(admin));
          localStorage.setItem("token", token)
          ElMessage.success('登录成功')
          setTimeout(() => {
            location.href = '/admin/list';
          },500)

        }
        else {
            ElMessage.error(res.msg)
        }
      })
    }
  })

}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  /* 全屏铺满背景 */
  background-image: url("@/assets/img.png");
  background-size: 100% 100%;
  background-position: center;
  background-repeat: no-repeat;
}

.login-box {
  width: 50%;
  height: 100%;
  display: flex;
  align-items: center;
  right: 0;
  position: absolute;
}
</style>