<script setup lang="ts">
import { useMemberStore } from '@/stores'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getMemberProfileAPI } from '@/services/profile'
import type { wmGuessInstance } from '@/types/component'
import { ref } from 'vue'

// 获取屏幕边界到安全区域距离
const { safeAreaInsets } = uni.getSystemInfoSync()

// 订单选项
const orderTypes = [
  { type: '1', text: '待接单', icon: 'icon-currency' },
  { type: '2', text: '待配送', icon: 'icon-gift' },
  { type: '3', text: '待收货', icon: 'icon-check' },
  { type: '4', text: '待评价', icon: 'icon-comment' },
]

// 获取会员信息
const memberStore = useMemberStore()

// 猜你喜欢 ref
const guessRef = ref<wmGuessInstance>()

// 下拉刷新状态
const isTriggered = ref(false)

// 页面加载时刷新用户信息
onLoad(async () => {
  if (memberStore.profile) {
    const res = await getMemberProfileAPI()
    memberStore.profile.avatarUrl = res.data.avatarUrl
    memberStore.profile.nickname = res.data.nickname
  }
})

// 页面显示时刷新（保证信息最新）
onShow(() => { })

// 下拉刷新（和首页一模一样！）
const onRefresherrefresh = async () => {
  isTriggered.value = true
  // 重置猜你喜欢
  guessRef.value?.resetData()
  // 刷新用户信息
  if (memberStore.profile) {
    const res = await getMemberProfileAPI()
    memberStore.profile.avatarUrl = res.data.avatarUrl
    memberStore.profile.nickname = res.data.nickname
  }
  // 刷新猜你喜欢
  await guessRef.value?.getMore()
  isTriggered.value = false
}

// 滚动到底部加载更多
const onScrolltolower = () => {
  guessRef.value?.getMore()
}
</script>

<template>
  <!-- 关键：把外层改成 可下拉刷新的 scroll-view -->
  <scroll-view enable-back-to-top class="viewport" scroll-y refresher-enabled :refresher-triggered="isTriggered"
    @refresherrefresh="onRefresherrefresh" @scrolltolower="onScrolltolower">
    <!-- 个人资料 -->
    <view class="profile" :style="{ paddingTop: safeAreaInsets!.top + 'px' }">
      <!-- 情况1：已登录 -->
      <view class="overview" v-if="memberStore.profile">
        <navigator url="/pagesMember/profile" hover-class="none">
          <image class="avatar" :src="memberStore.profile.avatarUrl" mode="aspectFill"></image>
        </navigator>
        <view class="meta">
          <view class="nickname">
            {{ memberStore.profile.nickname || memberStore.profile.username }}
          </view>
          <navigator class="extra" url="/pagesMember/profile" hover-class="none">
            <text class="update">更新头像昵称</text>
          </navigator>
        </view>
      </view>
      <!-- 情况2：未登录 -->
      <view class="overview" v-else>
        <navigator url="/pages/login/login" hover-class="none">
          <image class="avatar gray" mode="aspectFill"
            src="https://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-06/db628d42-88a7-46e7-abb8-659448c33081.png">
          </image>
        </navigator>
        <view class="meta">
          <navigator url="/pages/login/login" hover-class="none" class="nickname">
            未登录
          </navigator>
          <view class="extra">
            <text class="tips">点击登录账号</text>
          </view>
        </view>
      </view>
      <navigator class="settings" url="/pagesMember/settings" hover-class="none">
        设置
      </navigator>
    </view>

    <!-- 我的订单 -->
    <view class="orders">
      <view class="title">
        我的订单
        <navigator class="navigator" url="/pagesOrder/list?type=0" hover-class="none">
          查看全部订单<text class="icon-right"></text>
        </navigator>
      </view>
      <view class="section">
        <navigator v-for="item in orderTypes" :key="item.type" :class="item.icon"
          :url="`/pagesOrder/list?type=${item.type}`" class="navigator" hover-class="none">
          {{ item.text }}
        </navigator>
        <!-- #ifdef MP-WEIXIN -->
        <button class="contact icon-handset" open-type="contact">售后</button>
        <!-- #endif -->
      </view>
    </view>
    <view class="guess">
      <wmGuess ref="guessRef" />
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
  background-repeat: no-repeat;
  background-color: #ffbc00;
  background-image: linear-gradient(135deg, #ffbc00 0%, #ff9f00 100%);
  background-size: 100% auto;
}

/* 用户信息 */
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
    align-items: flex-start;
    line-height: 30rpx;
    padding: 16rpx 0;
    margin-left: 20rpx;
  }

  .nickname {
    max-width: 180rpx;
    margin-bottom: 16rpx;
    font-size: 32rpx;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .extra {
    display: flex;
    font-size: 20rpx;
  }

  .tips {
    font-size: 24rpx;
    opacity: 0.9;
  }

  .update {
    padding: 6rpx 14rpx 4rpx;
    color: #fff;
    border: 1rpx solid rgba(255, 255, 255, 0.5);
    margin-right: 10rpx;
    border-radius: 30rpx;
    background: rgba(255, 255, 255, 0.1);
  }

  .settings {
    position: absolute;
    bottom: 0;
    right: 40rpx;
    font-size: 28rpx;
    color: #fff;
    opacity: 0.9;
  }
}

/* 我的订单 */
.orders {
  position: relative;
  z-index: 99;
  padding: 30rpx;
  margin: 50rpx 20rpx 0;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 188, 0, 0.12);

  .title {
    height: 40rpx;
    line-height: 40rpx;
    font-size: 30rpx;
    font-weight: 500;
    color: #333;

    .navigator {
      font-size: 24rpx;
      color: #999;
      float: right;
    }
  }

  .section {
    width: 100%;
    display: flex;
    justify-content: space-between;
    padding: 40rpx 20rpx 10rpx;

    .navigator,
    .contact {
      text-align: center;
      font-size: 24rpx;
      color: #333;

      &::before {
        display: block;
        font-size: 56rpx;
        color: #ffbc00;
        margin-bottom: 8rpx;
      }

      &::after {
        border: none;
      }
    }

    .contact {
      padding: 0;
      margin: 0;
      border: 0;
      background-color: transparent;
      line-height: inherit;
    }
  }
}

/* 猜你喜欢 */
.guess {
  background-color: #fff9e6;
  margin-top: 20rpx;
}
</style>
