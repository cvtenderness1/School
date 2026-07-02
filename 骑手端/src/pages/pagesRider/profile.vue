<script setup lang="ts">
import { getRiderProfileAPI, putRiderProfileAPI } from '@/services/profile'
import { useRiderStore } from '@/stores'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'

const { safeAreaInsets } = uni.getSystemInfoSync()
const profile = ref({} as any)
const memberStore = useRiderStore()

// 获取骑手资料
const getRiderProfileData = async () => {
  const res = await getRiderProfileAPI()
  profile.value = res.data
}

onLoad(() => {
  getRiderProfileData()
})

// 保存基础信息
const onSubmit = async () => {
  try {
    await putRiderProfileAPI({
      name: profile.value.name,
      phone: profile.value.phone,
      studentId: profile.value.studentId,
    })
    uni.showToast({ icon: 'success', title: '保存成功' })
    setTimeout(() => uni.navigateBack(), 400)
  } catch (err) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

const toggleWorkStatus = async () => {
  // 冻结拦截
  if (profile.value.frozen === 1) {
    uni.showToast({ title: '账号已冻结，无法切换状态', icon: 'none' })
    return
  }

  const oldStatus = profile.value.workStatus ?? 0
  const newStatus = oldStatus === 1 ? 0 : 1
  profile.value.workStatus = newStatus

  try {
    await putRiderProfileAPI({
      workStatus: newStatus,
    })
    uni.showToast({
      title: newStatus === 1 ? '已上线' : '已休息',
      icon: 'success',
    })
  } catch (err) {
    // 接口失败回滚状态
    profile.value.workStatus = oldStatus
    uni.showToast({ title: '状态切换失败', icon: 'none' })
  }
}

// 模拟违规记录
// const violationList = ref([
//   { title: '超时送达', score: -2, time: '2026-04-10' },
//   { title: '用户投诉', score: -5, time: '2026-04-18' },
// ])
</script>

<template>
  <view class="viewport">
    <view class="navbar" :style="{ paddingTop: safeAreaInsets?.top + 'px' }">
      <navigator open-type="navigateBack" class="back" hover-class="none"></navigator>
      <view class="title">骑手详细页面</view>
    </view>

    <!-- 工作状态卡片 -->
    <view class="card work-card">
      <view class="card-title">工作状态</view>
      <view class="status-row">
        <view class="status-label">当前模式</view>
        <view
          class="status-btn"
          :class="[profile.workStatus === 1 ? 'online' : 'rest']"
          @click="toggleWorkStatus"
          :disabled="profile.frozen === 1"
        >
          {{ profile.workStatus === 1 ? '在线接单' : '休息中' }}
        </view>
      </view>
      <view class="tip" v-if="profile.frozen === 1"> 账号已冻结，无法接单与切换状态 </view>
    </view>

    <!-- 今日接单限制 -->
    <view class="card quota-card">
      <view class="card-title">今日接单限制</view>
      <view class="quota-row">
        <view class="quota-label">今日已接</view>
        <view class="quota-num done">{{ profile.todayOrderCount || 0 }}</view>
      </view>
      <view class="quota-row">
        <view class="quota-label">接单上限</view>
        <view class="quota-num max">{{ profile.dailyOrderLimit || 15 }}</view>
      </view>
      <view class="quota-progress">
        <view
          class="progress-bar"
          :style="{
            width: ((profile.todayOrderCount || 0) / (profile.dailyOrderLimit || 15)) * 100 + '%',
          }"
        ></view>
      </view>
    </view>

    <!-- 风控信用中心 -->
    <view class="card risk-card">
      <view class="card-title">风控与信用</view>
      <view class="score-row">
        <view class="score-label">当前信用分</view>
        <view class="score-num" :class="{ low: (profile.creditScore || 100) <= 60 }">
          {{ profile.creditScore || 100 }}
        </view>
      </view>

      <view class="divider"></view>
      <!--
      <view class="violation-title">近期违规记录</view>
      <view class="violation-item" v-for="(item, idx) in violationList" :key="idx">
        <view class="vio-text">{{ item.title }}</view>
        <view class="vio-score">{{ item.score }}</view>
        <view class="vio-time">{{ item.time }}</view>
      </view> -->
    </view>

    <!-- 骑手基础信息 -->
    <view class="form">
      <view class="form-content">
        <view class="form-item">
          <text class="label">骑手账号</text>
          <text class="account">{{ profile?.account }}</text>
        </view>
        <view class="form-item">
          <text class="label">姓名</text>
          <input class="input" v-model="profile.name" placeholder="请输入姓名" />
        </view>
        <view class="form-item">
          <text class="label">联系电话</text>
          <input class="input" v-model="profile.phone" placeholder="请输入电话" />
        </view>
        <view class="form-item">
          <text class="label">身份证</text>
          <input class="input" v-model="profile.studentId" placeholder="请输入身份证" />
        </view>
      </view>

      <button @tap="onSubmit" class="form-button">保 存</button>
    </view>
  </view>
</template>

<style lang="scss">
page {
  background: #fff9e6;
}
.viewport {
  min-height: 100vh;
  background: #fff9e6;
  padding-bottom: 40rpx;
}

/* 导航栏 */
.navbar {
  position: relative;
  background: #f9c950;
  .title {
    height: 44px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32rpx;
    color: #fff;
    font-weight: bold;
  }
  .back {
    position: absolute;
    height: 44px;
    width: 44px;
    left: 0;
    top: 0;
    font-size: 40rpx;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

/* 通用卡片 */
.card {
  margin: 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

/* 工作状态 */
.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-label {
  font-size: 28rpx;
  color: #666;
}
.status-btn {
  padding: 16rpx 32rpx;
  border-radius: 50rpx;
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
  &.online {
    background: #07c160;
  }
  &.rest {
    background: #999;
  }
  &[disabled] {
    opacity: 0.5;
  }
}
.tip {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #ff5722;
}

/* 接单上限进度 */
.quota-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.quota-label {
  font-size: 28rpx;
  color: #666;
}
.quota-num {
  font-size: 30rpx;
  font-weight: bold;
  &.done {
    color: #ff9800;
  }
  &.max {
    color: #333;
  }
}
.quota-progress {
  width: 100%;
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 10rpx;
  margin-top: 16rpx;
  overflow: hidden;
  .progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #ffb347, #ff9800);
    border-radius: 10rpx;
  }
}

/* 信用风控 */
.score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.score-label {
  font-size: 28rpx;
  color: #666;
}
.score-num {
  font-size: 40rpx;
  font-weight: bold;
  color: #07c160;
  &.low {
    color: #ff5722;
  }
}
.divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 24rpx 0;
}
.violation-title {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
}
.violation-item {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  font-size: 26rpx;
  .vio-text {
    color: #333;
  }
  .vio-score {
    color: #ff5722;
    font-weight: bold;
  }
  .vio-time {
    color: #999;
  }
}

/* 表单区域 */
.form {
  margin: 24rpx;
  &-content {
    background: #fff;
    border-radius: 24rpx;
    padding: 0 24rpx;
  }
  &-item {
    display: flex;
    height: 96rpx;
    align-items: center;
    border-bottom: 1rpx solid #f5e8c8;
    &:last-child {
      border: none;
    }
    .label {
      width: 180rpx;
      font-size: 28rpx;
      color: #8c6c35;
    }
    .input,
    .account {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }
  }
  &-button {
    height: 88rpx;
    line-height: 88rpx;
    margin-top: 30rpx;
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    border-radius: 88rpx;
    background: #f9c950;
  }
}
</style>
