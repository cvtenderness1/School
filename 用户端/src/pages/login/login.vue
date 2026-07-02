<script setup lang="ts">
import {
  postLoginAPI,
  postLoginWxMinAPI,
  postLoginWxMinSimpleAPI,
  postRegisterAPI,
} from '@/services/login'
import { useMemberStore } from '@/stores'
import type { LoginResult } from '@/types/member'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'

// #ifdef MP-WEIXIN
let code = ''
onLoad(async () => {
  const res = await wx.login()
  code = res.code
})

const onGetphonenumber: UniHelper.ButtonOnGetphonenumber = async (ev) => {
  const { encryptedData, iv } = ev.detail
  const res = await postLoginWxMinAPI({ code, encryptedData, iv })
  loginSuccess(res.data)
}
// #endif

const onGetphonenumberSimple = async () => {
  const res = await postLoginWxMinSimpleAPI('13123456789')
  loginSuccess(res.data)
}

const loginSuccess = (profile: LoginResult) => {
  const memberStore = useMemberStore()
  memberStore.setProfile(profile)
  uni.showToast({ icon: 'success', title: '登录成功' })
  setTimeout(() => {
    uni.navigateBack()
  }, 500)
}

// 登录表单
const form = ref({
  account: '',
  password: '',
})

// 注册表单
const registerForm = ref({
  account: '',
  password: '',
  confirmPwd: ''
})

// 控制显示登录/注册
const isRegister = ref(false)

// 登录提交
const onSubmit = async () => {
  if (!form.value.account) {
    uni.showToast({ icon: 'none', title: '请输入手机号/账号' })
    return
  }
  if (!form.value.password) {
    uni.showToast({ icon: 'none', title: '请输入密码' })
    return
  }
  const res = await postLoginAPI(form.value)
  loginSuccess(res.data)
}
// 注册提交
const onRegister = async () => {
  const { account, password, confirmPwd } = registerForm.value

  if (!account) {
    uni.showToast({ icon: 'none', title: '请输入手机号' })
    return
  }
  if (!password) {
    uni.showToast({ icon: 'none', title: '请输入密码' })
    return
  }
  if (password !== confirmPwd) {
    uni.showToast({ icon: 'none', title: '两次密码不一致' })
    return
  }

  try {
    // 调用真实注册接口
    const res = await postRegisterAPI({ account, password })
    uni.showToast({ icon: 'success', title: '注册成功' })
    // 自动登录
    loginSuccess(res.data)
  } catch (err) {
    uni.showToast({ icon: 'none', title: '注册失败，用户名已存在' })
  }
}
</script>

<template>
  <view class="viewport">
    <view class="logo">
      <image
        src="https://s1.aigei.com/src/img/png/d6/d6836172c2454d3a98f103f53bc5dda3.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:CM9rFWw6svkvN6J9IMiSrOn9XWQ=">
      </image>
    </view>

    <view class="login">
      <!-- 登录面板 -->
      <view v-if="!isRegister">
        <input v-model="form.account" class="input" type="tel" placeholder="请输入用户名/手机号码" />
        <input v-model="form.password" class="input" type="text" password placeholder="请输入密码" />
        <button @tap="onSubmit" class="button phone">账号密码登录</button>
      </view>

      <!-- 注册面板 -->
      <view v-else>
        <input v-model="registerForm.account" class="input" type="tel" placeholder="请输入手机号" />
        <input v-model="registerForm.password" class="input" type="text" password placeholder="请设置密码" />
        <input v-model="registerForm.confirmPwd" class="input" type="text" password placeholder="请确认密码" />
        <button @tap="onRegister" class="button phone">立即注册</button>
      </view>

      <!-- 切换登录/注册 -->
      <view class="toggle" @tap="isRegister = !isRegister">
        <text v-if="!isRegister">没有账号？立即注册</text>
        <text v-else>已有账号？返回登录</text>
      </view>

      <!-- #ifdef MP-WEIXIN -->
      <button class="button phone" open-type="getPhoneNumber" @getphonenumber="onGetphonenumber"
        style="margin-top:20rpx">
        手机号快捷登录
      </button>
      <!-- #endif -->

      <view class="extra">
        <view class="caption"><text>其他登录方式</text></view>
        <view class="options">
          <button @tap="onGetphonenumberSimple">
            <text class="icon icon-phone">模拟快捷登录</text>
          </button>
        </view>
      </view>

      <view class="tips">登录/注册即视为你同意《服务条款》和《校园跑腿外卖隐私协议》</view>
    </view>
  </view>
</template>

<style lang="scss">
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
  }

  .phone {
    background-color: #ff9f00;
  }

  .toggle {
    text-align: center;
    margin: 20rpx 0;
    color: #ff9f00;
    font-size: 26rpx;
  }

  .extra {
    flex: 1;
    padding: 40rpx 0 0;

    .caption {
      width: 440rpx;
      margin: 0 auto;
      border-top: 1rpx solid #ffeccd;
      font-size: 26rpx;
      color: #999;
      position: relative;

      text {
        transform: translateX(-50%);
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
      margin-top: 40rpx;

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
  font-size: 22rpx;
  color: #999;
  text-align: center;
  margin-top: 20rpx;
}
</style>
