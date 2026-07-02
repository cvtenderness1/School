<script setup lang="ts">
import { useRiderStore } from '@/stores'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getRiderProfileAPI } from '@/services/rider'
import { ref } from 'vue'

// 获取屏幕边界到安全区域距离
const { safeAreaInsets } = uni.getSystemInfoSync()

// 骑手订单选项
const orderTypes = [
  { type: '1', text: '待接单', icon: 'clock-o' },
  { type: '2', text: '待配送', icon: 'direction-o' },
  { type: '3', text: '配送中', icon: 'checked-o' },
  { type: '4', text: '已完成', icon: 'balance-o' },
]

// 骑手信息
const riderStore = useRiderStore()

const isTriggered = ref(false)

// 页面加载时刷新骑手信息
onLoad(async () => {
  if (riderStore.profile) {
    const res = await getRiderProfileAPI()
    riderStore.profile.name = res.data.name
    riderStore.profile.phone = res.data.phone
  }
})

onShow(() => {})

// 下拉刷新
const onRefresherrefresh = async () => {
  isTriggered.value = true

  if (riderStore.profile) {
    const res = await getRiderProfileAPI()
    riderStore.profile.name = res.data.name
    riderStore.profile.phone = res.data.phone
  }

  isTriggered.value = false
}
</script>

<template>
  <scroll-view
    enable-back-to-top
    class="viewport"
    scroll-y
    refresher-enabled
    :refresher-triggered="isTriggered"
    @refresherrefresh="onRefresherrefresh"
  >
    <!-- 骑手资料 -->
    <view class="profile" :style="{ paddingTop: safeAreaInsets!.top + 'px' }">
      <!-- 已登录 -->
      <view class="overview" v-if="riderStore.profile">
        <navigator url="/pages/pagesRider/profile" hover-class="none">
          <image
            class="avatar"
            src="https://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-06/db628d42-88a7-46e7-abb8-659448c33081.png"
            mode="aspectFill"
          ></image>
        </navigator>
        <view class="meta">
          <view class="nickname">{{ riderStore.profile.name || '骑手' }}</view>
          <navigator class="extra" url="/pages/pagesRider/profile" hover-class="none">
            <text class="update">骑手信息</text>
          </navigator>
        </view>
      </view>

      <!-- 未登录 → 跳骑手登录 -->
      <view class="overview" v-else>
        <navigator url="/pages/login/login" hover-class="none">
          <image
            class="avatar gray"
            src="https://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-06/db628d42-88a7-46e7-abb8-659448c33081.png"
          ></image>
        </navigator>
        <view class="meta">
          <navigator url="/pages/login/login" hover-class="none" class="nickname">
            骑手未登录
          </navigator>
          <view class="extra">
            <text class="tips">点击登录骑手账号</text>
          </view>
        </view>
      </view>

      <navigator class="settings" url="/pages/pagesRider/setting" hover-class="none">
        设置
      </navigator>
    </view>

    <!-- 订单  -->
    <view class="orders">
      <view class="title">
        骑手中心
        <navigator class="navigator" url="/pages/pagesRider/list" hover-class="none">
          全部订单<text class="icon-right"></text>
        </navigator>
      </view>
      <view class="section">
        <navigator
          v-for="item in orderTypes"
          :key="item.type"
          :url="`/pages/pagesRider/list?type=${item.type}`"
          class="order-item"
          hover-class="none"
        >
          <van-icon :name="item.icon" class="order-icon" />
          <text class="order-text">{{ item.text }}</text>
        </navigator>
      </view>
    </view>

    <!--收益中心入口 -->
    <view class="income-card" v-if="riderStore.profile">
      <navigator url="/pages/pagesRider/income" hover-class="none" class="income-box">
        <view class="left">
          <van-icon name="balance-o" class="icon" />
          <text class="text">收益中心</text>
        </view>
        <view class="right">
          <text>提现 / 收入排行榜</text>
        </view>
      </navigator>
    </view>
  </scroll-view>
</template>

<style lang="scss">
page {
  height: 100%;
  overflow: hidden;
  background-color: #fff9e6;
}

.viewport {
  height: 100%;
  background-color: #25c8ff;
  background-image: linear-gradient(135deg, #25c8ff 0%, #009cff 100%);
  background-size: 100% auto;
}

.profile {
  margin-top: 30rpx;
  position: relative;

  .overview {
    display: flex;
    height: 120rpx;
    padding: 0 36rpx;
    color: #fff;
  }

  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background-color: #fff;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
  }

  .gray {
    filter: grayscale(100%);
  }

  .meta {
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 16rpx 0;
    margin-left: 20rpx;
  }

  .nickname {
    font-size: 32rpx;
    font-weight: 500;
  }

  .update {
    padding: 6rpx 14rpx;
    color: #fff;
    border: 1rpx solid rgba(255, 255, 255, 0.5);
    border-radius: 30rpx;
    background: rgba(255, 255, 255, 0.1);
  }

  .settings {
    position: absolute;
    bottom: 0;
    right: 40rpx;
    font-size: 28rpx;
    color: #fff;
  }
}

.orders {
  padding: 30rpx;
  margin: 50rpx 20rpx 0;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 156, 255, 0.12);

  .title {
    font-size: 30rpx;
    font-weight: 500;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .navigator {
      font-size: 24rpx;
      color: #999;
    }
  }

  .section {
    display: flex;
    justify-content: space-between;
    padding: 30rpx 0 10rpx;
  }

  .order-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 24rpx;
    color: #333;
  }

  .order-icon {
    font-size: 40rpx;
    color: #009cff;
    margin-bottom: 10rpx;
  }
}

.guess {
  background-color: #fff9e6;
  margin-top: 20rpx;
}

.income-card {
  margin: 20rpx 20rpx 0;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 156, 255, 0.12);
  overflow: hidden;

  .income-box {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;

    .left {
      display: flex;
      align-items: center;

      .icon {
        font-size: 36rpx;
        color: #ff9f00;
        margin-right: 16rpx;
      }

      .text {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
      }
    }

    .right {
      display: flex;
      align-items: center;
      font-size: 26rpx;
      color: #999;

      .arrow {
        margin-left: 10rpx;
        color: #ccc;
      }
    }
  }
}
</style>
