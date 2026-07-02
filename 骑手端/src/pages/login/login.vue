<script setup lang="ts">
import { postRiderLoginAPI, postRiderLoginSimpleAPI, postRiderRegisterAPI } from '@/services/rider'
import { useRiderStore } from '@/stores'
import type { RiderLoginResult } from '@/types/rider'
import { ref } from 'vue'

// 模式切换：login / register
const mode = ref<'login' | 'register'>('login')

// 骑手表单登录
const form = ref({
  account: '',
  password: '',
})

// 注册表单
const registerForm = ref({
  account: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  studentId: '',
})

// 表单提交登录
const onSubmit = async () => {
  const res = await postRiderLoginAPI(form.value)
  loginSuccess(res.data)
}

// 模拟快捷登录（开发用）
const onSimpleLogin = async () => {
  const res = await postRiderLoginSimpleAPI('13800138000')
  loginSuccess(res.data)
}

// 注册逻辑
const onRegister = async () => {
  const { account, password, confirmPassword, name, phone } = registerForm.value
  if (!account || !password || !confirmPassword || !name || !phone) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (password !== confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  // 对密码做简单校验
  const res = await postRiderRegisterAPI({
    account,
    password,
    name,
    phone,
    studentId: registerForm.value.studentId || undefined,
  })
  // 注册成功直接返回了 token 和骑手信息，复用登录成功处理
  loginSuccess(res.data)
}

// 登录成功统一处理
const loginSuccess = (profile: RiderLoginResult) => {
  const riderStore = useRiderStore()
  riderStore.setProfile(profile)

  uni.showToast({ icon: 'success', title: mode.value === 'register' ? '注册成功' : '骑手登录成功' })

  setTimeout(() => {
    uni.navigateBack()
  }, 500)
}

// 切换模式时清空表单
const switchMode = (m: 'login' | 'register') => {
  mode.value = m
  if (m === 'login') {
    registerForm.value = {
      account: '',
      password: '',
      confirmPassword: '',
      name: '',
      phone: '',
      studentId: '',
    }
  } else {
    form.value = { account: '', password: '' }
  }
}
</script>

<template>
  <view class="viewport">
    <view class="logo">
      <image src="https://s1.aigei.com/src/img/png/d6/d6836172c2454d3a98f103f53bc5dda3.png"></image>
    </view>

    <!-- 登录表单 -->
    <view v-if="mode === 'login'" class="login">
      <input v-model="form.account" class="input" placeholder="请输入骑手账号" />
      <input v-model="form.password" class="input" password placeholder="请输入密码" />

      <button @tap="onSubmit" class="button phone">骑手登录</button>
      <view class="switch-register">
        <text @tap="switchMode('register')">没有账号？立即注册</text>
      </view>
      <view class="extra">
        <view class="caption">
          <text>开发测试</text>
        </view>
        <view class="options">
          <button @tap="onSimpleLogin">
            <text class="icon icon-phone">模拟骑手登录</text>
          </button>
        </view>
      </view>

      <view class="tips">骑手登录即视为同意《骑手服务条款》</view>
    </view>

    <!-- 注册表单 -->
    <view v-else class="login">
      <input v-model="registerForm.account" class="input" placeholder="设置账号" />
      <input v-model="registerForm.password" class="input" password placeholder="设置密码" />
      <input v-model="registerForm.confirmPassword" class="input" password placeholder="确认密码" />
      <input v-model="registerForm.name" class="input" placeholder="真实姓名" />
      <input v-model="registerForm.phone" class="input" type="number" placeholder="手机号" />
      <input v-model="registerForm.studentId" class="input" placeholder="学号 (选填)" />

      <button @tap="onRegister" class="button phone">立即注册</button>

      <view class="switch-register">
        <text @tap="switchMode('login')">已有账号？去登录</text>
      </view>
      <view class="tips">注册即视为同意《骑手服务条款》</view>
    </view>
  </view>
</template>

<style lang="scss">
.switch-register {
  text-align: center;
  margin-top: 20rpx;
  font-size: 26rpx;
  color: #ff9f00;
  text-decoration: underline;
}
page {
  height: 100%;
  background-color: #fff9e6;
}
.viewport {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20rpx 40rpx;
}
.logo {
  flex: 1;
  text-align: center;
  image {
    width: 220rpx;
    height: 220rpx;
    margin-top: 15vh;
  }
}
.login {
  display: flex;
  flex-direction: column;
  height: 60vh;
  padding: 40rpx 20rpx 20rpx;
  .input {
    width: 100%;
    height: 80rpx;
    font-size: 28rpx;
    border-radius: 72rpx;
    border: 1px solid #ffeccd;
    background-color: #fffdf5;
    padding-left: 30rpx;
    margin-bottom: 20rpx;
  }
  .button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 80rpx;
    font-size: 28rpx;
    border-radius: 72rpx;
    color: #fff;
    background-color: #ff9f00;
  }
  .extra {
    flex: 1;
    padding: 70rpx 70rpx 0;
    .caption {
      width: 440rpx;
      line-height: 1;
      border-top: 1rpx solid #ffeccd;
      font-size: 26rpx;
      color: #999;
      position: relative;
      text {
        transform: translate(-40%);
        background-color: #fff9e6;
        position: absolute;
        top: -12rpx;
        left: 50%;
        padding: 0 15rpx;
      }
    }
    .options {
      display: flex;
      justify-content: center;
      margin-top: 70rpx;
      button {
        padding: 0;
        background: transparent;
        &::after {
          border: none;
        }
      }
    }
  }
}
.tips {
  position: absolute;
  bottom: 80rpx;
  left: 0;
  right: 0;
  font-size: 22rpx;
  color: #999;
  text-align: center;
}
</style>
