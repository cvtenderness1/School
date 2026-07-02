<script setup lang="ts">
import { useRiderStore } from '@/stores'

const riderStore = useRiderStore()

// 退出登录
const onLogout = () => {
  uni.showModal({
    content: '是否退出登录？',
    confirmColor: '#f9c950',
    success: (res) => {
      if (res.confirm) {
        riderStore.clearProfile()
        uni.navigateBack()
      }
    },
  })
}
</script>

<template>
  <view class="viewport">
    <view class="list" v-if="riderStore.profile">
      <navigator url="/pages/pagesRider/profile" hover-class="none" class="item arrow">
        个人信息
      </navigator>
    </view>

    <view class="list">
      <button hover-class="none" class="item arrow" open-type="feedback">问题反馈</button>
      <button hover-class="none" class="item arrow" open-type="contact">联系我们</button>
    </view>

    <view class="list">
      <button hover-class="none" class="item arrow">关于校园跑腿</button>
    </view>

    <view class="action" v-if="riderStore.profile">
      <view @tap="onLogout" class="button">退出登录</view>
    </view>
  </view>
</template>

<style lang="scss">
page {
  background-color: #fff9e6;
}
.viewport {
  padding: 20rpx;
}
.list {
  padding: 0 20rpx;
  background-color: #fff;
  margin-bottom: 20rpx;
  border-radius: 10rpx;
  .item {
    line-height: 90rpx;
    padding-left: 10rpx;
    font-size: 30rpx;
    color: #8c6c35;
    border-top: 1rpx solid #f5e8c8;
    position: relative;
    text-align: left;
    border-radius: 0;
    background-color: #fff;
    &::after {
      width: auto;
      height: auto;
      left: auto;
      border: none;
    }
    &:first-child {
      border: none;
    }
  }
  .arrow::after {
    content: '\e6c2';
    position: absolute;
    top: 50%;
    color: #f9c950;
    font-family: 'erabbit' !important;
    font-size: 32rpx;
    transform: translateY(-50%);
  }
}
.action {
  text-align: center;
  line-height: 90rpx;
  margin-top: 40rpx;
  font-size: 32rpx;
  color: #8c6c35;
  .button {
    background-color: #f9c950;
    color: #fff;
    margin-bottom: 20rpx;
    border-radius: 10rpx;
  }
}
</style>
