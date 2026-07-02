<script setup lang="ts">
import type { HotItem } from '@/types/home'
defineProps<{
  list: HotItem[]
}>()
</script>

<template>
  <view class="hot-panel">
    <view class="panel-head">
      <text class="title">热门推荐</text>
      <text class="subtitle">人气商家</text>
    </view>

    <view v-if="list.length === 0" class="empty-tip">暂无推荐商家</view>

    <!-- 点击商家卡片 → 进入商家页面 -->
    <view class="merchant-item" v-for="item in list" :key="item.merchantId">
      <navigator hover-class="none" :url="`/pages/hot/hot?id=${item.merchantId}`" class="card">
        <image class="merchant-img" mode="aspectFill" :src="item.coverImg"></image>

        <view class="merchant-info">
          <view class="name-line">
            <text class="merchant-name">{{ item.merchantName }}</text>
            <text class="status" :class="item.status === 1 ? 'open' : 'close'">
              {{ item.status === 1 ? '营业中' : '休息中' }}
            </text>
          </view>

          <text class="desc">{{ item.alt || '这家店很懒，什么都没写~' }}</text>

          <view class="footer">
            <text class="score">⭐ {{ item.score }}分</text>
            <text class="tag">配送费 ¥{{ item.postFee }}</text>
            <text class="tag">{{ item.categoryName }}</text>
          </view>
        </view>
      </navigator>
    </view>
  </view>
</template>

<style lang="scss">
.hot-panel {
  background-color: #fff9e6;
  padding: 20rpx;
  box-sizing: border-box;
}

.panel-head {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 16rpx;

  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .subtitle {
    font-size: 24rpx;
    color: #999;
  }
}

.empty-tip {
  text-align: center;
  padding: 60rpx 0;
  font-size: 26rpx;
  color: #ccc;
}

.merchant-item {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 188, 0, 0.08);
  overflow: hidden;
}

.card {
  display: flex;
  padding: 20rpx;
  align-items: center;
}

.merchant-img {
  width: 150rpx;
  height: 150rpx;
  border-radius: 12rpx;
  background: #f6f6f6;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  margin-left: 20rpx;
}

.name-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.merchant-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.status {
  font-size: 20rpx;
  padding: 4rpx 8rpx;
  border-radius: 6rpx;
  background: #f5f5f5;

  &.open {
    color: #fff;
    background: #ff9f00;
  }

  &.close {
    color: #999;
    background: #f0f0f0;
  }
}

.desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.footer {
  display: flex;
  align-items: center;
  gap: 10rpx;

  .score {
    font-size: 22rpx;
    color: #ff7d00;
  }

  .tag {
    font-size: 20rpx;
    color: #999;
    background: #f5f5f5;
    padding: 4rpx 8rpx;
    border-radius: 6rpx;
  }
}
</style>
