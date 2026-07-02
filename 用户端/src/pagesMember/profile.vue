<script setup lang="ts">
import { getMemberProfileAPI, putMemberProfileAPI } from '@/services/profile'
import { useMemberStore } from '@/stores'
import type { Gender, ProfileDetail } from '@/types/member'
import { formatDate } from '@/utils'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'

// 获取屏幕边界到安全区域距离
const { safeAreaInsets } = uni.getSystemInfoSync()

// 获取个人信息，修改个人信息需提供初始值
const profile = ref({} as ProfileDetail)
// 城市编码
let fullLocationCode: [string, string, string] = ['', '', '']

const getMemberProfileData = async () => {
  const res = await getMemberProfileAPI()
  profile.value = res.data

  if (res.data.provinceCode && res.data.cityCode && res.data.countyCode) {
    fullLocationCode = [res.data.provinceCode, res.data.cityCode, res.data.countyCode]
  }

  // 同步 Store 的头像和昵称
  memberStore.profile!.avatarUrl = res.data.avatarUrl
  memberStore.profile!.nickname = res.data.nickname
}

onLoad(() => {
  getMemberProfileData()
})

const memberStore = useMemberStore()

// 修改头像
const onAvatarChange = () => {
  // #ifdef H5 || APP-PLUS
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempFilePaths = res.tempFilePaths
      uploadFile(tempFilePaths[0])
    },
  })
  // #endif

  // #ifdef MP-WEIXIN
  uni.chooseMedia({
    count: 1,
    mediaType: ['image'],
    success: (res) => {
      const { tempFilePath } = res.tempFiles[0]
      uploadFile(tempFilePath)
    },
  })
  // #endif
}

// 文件上传
const uploadFile = (file: string) => {
  uni.uploadFile({
    url: 'http://localhost:1000/files/upload',
    name: 'file',
    filePath: file,
    success: (res) => {
      if (res.statusCode === 200) {
        const avatarUrl = JSON.parse(res.data).data
        profile.value.avatarUrl = avatarUrl
        memberStore.profile!.avatarUrl = avatarUrl
        uni.showToast({ icon: 'success', title: '头像选择成功' })
      }
    },
    fail() {
      uni.showToast({ icon: 'error', title: '上传失败' })
    }
  })
}

// 修改性别
const onGenderChange: UniHelper.RadioGroupOnChange = (ev) => {
  profile.value.gender = ev.detail.value as Gender
}

// 修改生日
const onBirthdayChange: UniHelper.DatePickerOnChange = (ev) => {
  profile.value.birthday = ev.detail.value
}

// 修改城市
const onFullLocationChange: UniHelper.RegionPickerOnChange = (ev) => {
  profile.value.fullLocation = ev.detail.value.join(' ')
  fullLocationCode = ev.detail.code!
}

// 点击保存提交所有信息
const onSubmit = async () => {
  const { avatarUrl, nickname, gender, birthday } = profile.value

  const res = await putMemberProfileAPI({
    avatarUrl,
    nickname,
    gender,
    birthday,
    provinceCode: fullLocationCode[0],
    cityCode: fullLocationCode[1],
    countyCode: fullLocationCode[2],
  })

  memberStore.profile!.nickname = res.data.nickname
  memberStore.profile!.avatarUrl = res.data.avatarUrl

  uni.showToast({ icon: 'success', title: '保存成功' })
  setTimeout(() => {
    uni.navigateBack()
  }, 400)
}
</script>

<template>
  <view class="viewport">
    <view class="navbar" :style="{ paddingTop: safeAreaInsets?.top + 'px' }">
      <navigator open-type="navigateBack" class="back icon-left" hover-class="none"></navigator>
      <view class="title">个人信息</view>
    </view>

    <view class="avatar">
      <view @tap="onAvatarChange" class="avatar-content">
        <image class="image" :src="profile?.avatarUrl" mode="aspectFill" />
        <text class="text">点击修改头像</text>
      </view>
    </view>

    <view class="form">
      <view class="form-content">
        <view class="form-item">
          <text class="label">账号</text>
          <text class="account placeholder">{{ profile?.username }}</text>
        </view>
        <view class="form-item">
          <text class="label">昵称</text>
          <input class="input" type="text" placeholder="请填写昵称" v-model="profile!.nickname" />
        </view>
        <view class="form-item">
          <text class="label">性别</text>
          <radio-group @change="onGenderChange">
            <label class="radio">
              <radio value="男" color="#27ba9b" :checked="profile?.gender === '男'" />男
            </label>
            <label class="radio">
              <radio value="女" color="#27ba9b" :checked="profile?.gender === '女'" />女
            </label>
          </radio-group>
        </view>
        <view class="form-item">
          <text class="label">生日</text>
          <picker @change="onBirthdayChange" mode="date" class="picker" :value="profile?.birthday" start="1900-01-01"
            :end="formatDate(new Date())">
            <view v-if="profile?.birthday">{{ profile?.birthday }}</view>
            <view class="placeholder" v-else>请选择日期</view>
          </picker>
        </view>

        <!-- #ifdef MP-WEIXIN -->
        <view class="form-item">
          <text class="label">城市</text>

          <picker @change="onFullLocationChange" mode="region" class="picker" :value="fullLocationCode">
            <view v-if="profile?.fullLocation">{{ profile.fullLocation }}</view>
            <view class="placeholder" v-else>请选择城市</view>
          </picker>
        </view>
        <!-- #endif -->
      </view>

      <button @tap="onSubmit" class="form-button">保 存</button>
    </view>
  </view>
</template>

<style lang="scss">
page {
  background-color: #fff9e6;
  /* 暖黄背景 */
}

.viewport {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f9c950;
  /* 纯暖黄色背景 */
  background-image: none;
}

.navbar {
  position: relative;

  .title {
    height: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
    color: #fff;
  }

  .back {
    position: absolute;
    height: 40px;
    width: 40px;
    left: 0;
    font-size: 20px;
    color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.avatar {
  text-align: center;
  width: 100%;
  height: 260rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .image {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background-color: #fff;
    border: 3rpx solid #f9c950;
    /* 头像边框暖黄 */
  }

  .text {
    display: block;
    padding-top: 20rpx;
    line-height: 1;
    font-size: 26rpx;
    color: #fff;
  }
}

.form {
  background-color: #fff9e6;
  /* 暖黄背景 */

  &-content {
    margin: 20rpx 20rpx 0;
    padding: 0 20rpx;
    border-radius: 10rpx;
    background-color: #fff;
  }

  &-item {
    display: flex;
    height: 96rpx;
    line-height: 46rpx;
    padding: 25rpx 10rpx;
    background-color: #fff;
    font-size: 28rpx;
    border-bottom: 1rpx solid #f5e8c8;
    /* 浅黄分割线 */

    &:last-child {
      border: none;
    }

    .label {
      width: 180rpx;
      color: #8c6c35;
      /* 标签暖黄色 */
    }

    .account {
      color: #b98f52;
      /* 账号浅黄 */
    }

    .input {
      flex: 1;
      display: block;
      height: 46rpx;
      color: #6b4e24;
    }

    .radio {
      margin-right: 20rpx;
      color: #8c6c35;
    }

    .picker {
      flex: 1;
      color: #6b4e24;
    }

    .placeholder {
      color: #c8a870;
      /* 占位符浅黄 */
    }
  }

  &-button {
    height: 80rpx;
    text-align: center;
    line-height: 80rpx;
    margin: 30rpx 20rpx;
    color: #fff;
    border-radius: 80rpx;
    font-size: 30rpx;
    background-color: #f9c950;
    /* 按钮暖黄色 */
  }
}
</style>
