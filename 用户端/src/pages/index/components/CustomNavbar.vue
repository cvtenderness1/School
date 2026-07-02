<script setup lang="ts">
// 搜索点击事件
const handleSearchClick = () => {
  uni.navigateTo({
    url: '/pages/search/search',
  })
}
//获取安全距离
const { safeAreaInsets } = uni.getSystemInfoSync()

// 扫码点击事件
const handleScanClick = () => {
  uni.scanCode({
    success: (res) => {
      console.log('扫码结果：', res.result)
    }
  })
}
</script>

<template>
  <view class="navbar" :style="{ paddingTop: safeAreaInsets?.top + 'px' }">
    <!-- logo区域：恢复稳定布局 -->
    <view class="logo">
      <image class="logo-image" src="https://www.0xiao.cn/static/img/web/errands_ico.png" mode="widthFix"></image>
      <text class="logo-text">快捷·便利·方便</text>
    </view>

    <!-- 搜索条区域 -->
    <view class="search" @click="handleSearchClick">
      <text class="icon-search">搜索商品</text>
      <text class="icon-scan" @click.stop="handleScanClick"></text>
    </view>
  </view>
</template>

<style lang="scss">
.navbar {
  background-color: #ffde26;
  background-size: cover;
  position: relative;
  display: flex;
  flex-direction: column;
  padding-top: 60rpx;
  padding-bottom: 20rpx;
}

.logo {
  display: flex;
  align-items: center;
  height: 80rpx;
  padding-left: 30rpx;
  padding-right: 20rpx;

  .logo-image {
    width: 200rpx;
    flex-shrink: 0;
  }

  .logo-text {
    flex: 1;
    line-height: 28rpx;
    color: #333;
    margin: 0 0 0 15rpx;
    padding-left: 15rpx;
    border-left: 1rpx solid #333;
    font-size: 26rpx;
  }
}

.search {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10rpx 0 26rpx;
  height: 64rpx;
  margin: 24rpx 20rpx 10rpx;
  color: #333;
  font-size: 28rpx;
  border-radius: 32rpx;
  background-color: rgba(255, 255, 255, 0.8);

  .icon-search {
    &::before {
      padding-left: 8rpx !important;
      margin-right: 10rpx;
    }
  }

  .icon-scan {
    font-size: 30rpx;
    padding: 10rpx;
    color: #333;
  }
}
</style>
