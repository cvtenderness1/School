<script setup>
import { ref } from 'vue'
const id = ref('')
const code = ref('')
const img = ref('')
const imgUrl = ref('')

import { onLoad } from '@dcloudio/uni-app'

onLoad(async (options) => {
  id.value = options.id
  code.value = id.value.slice(-4)
})

const photo = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      img.value = res.tempFilePaths[0]
      uploadImg(img.value)
    },
  })
}

const uploadImg = (path) => {
  uni.uploadFile({
    url: '/files/upload',
    filePath: path,
    name: 'file',
    success: (res) => {
      const data = JSON.parse(res.data)
      imgUrl.value = data.data
      uni.showToast({ title: '上传成功', icon: 'success' })
    },
  })
}

const finish = async () => {
  if (!imgUrl.value) {
    uni.showToast({ title: '请上传送达凭证', icon: 'none' })
    return
  }

  try {
    await uni.request({
      url: '/rider/finishDelivery',
      method: 'POST',
      data: {
        orderId: id.value,
        finishImg: imgUrl.value,
      },
    })

    uni.showToast({ title: '配送完成', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="page">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">确认送达</text>
      <text class="subtitle">请验证取件码并上传凭证</text>
    </view>

    <!-- 主体卡片 -->
    <view class="card">
      <!-- 取件码 -->
      <view class="code-box">
        <text class="label">取件验证码</text>
        <text class="code">{{ code }}</text>
      </view>

      <!-- 上传凭证 -->
      <view class="upload-section">
        <text class="section-title">送达凭证</text>

        <view class="upload-btn" @click="photo">
          <text class="upload-text">点击拍照上传现场照片</text>
        </view>

        <image v-if="img" :src="img" class="preview-img" mode="aspectFill" />
      </view>

      <!-- 确认按钮 -->
      <view class="btn-box">
        <button class="submit-btn" @click="finish">确认完成配送</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
/* 页面背景 */
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #eef2ff 100%);
  padding: 20rpx;
}

/* 顶部标题 */
.header {
  padding: 40rpx 20rpx 50rpx;
  text-align: center;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #6b7280;
}

/* 主卡片 */
.card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 30, 80, 0.08);
}

/* 取件码 */
.code-box {
  border: 2rpx dashed #3b82f6;
  border-radius: 24rpx;
  padding: 50rpx 30rpx;
  text-align: center;
  background: #f8fafc;
  margin-bottom: 50rpx;
}

.code-box .label {
  font-size: 26rpx;
  color: #64748b;
  display: block;
  margin-bottom: 16rpx;
}

.code-box .code {
  font-size: 52rpx;
  font-weight: bold;
  color: #2563eb;
  letter-spacing: 8rpx;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 60rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24rpx;
  display: block;
}

.upload-btn {
  height: 100rpx;
  border-radius: 20rpx;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx dashed #60a5fa;
}

.upload-text {
  font-size: 28rpx;
  color: #2563eb;
}

/* 照片预览 */
.preview-img {
  width: 100%;
  height: 360rpx;
  border-radius: 20rpx;
  margin-top: 24rpx;
  background: #f9fafb;
}

/* 提交按钮 */
.btn-box {
  margin-top: 20rpx;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(24, 144, 255, 0.2);
}

button::after {
  border: none;
}
</style>
