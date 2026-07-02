<script setup lang="ts">
const query = defineProps<{
  id: string
}>()
</script>

<template>
  <view class="page">
    <!-- 成功图标 + 文字 -->
    <view class="success-card">
      <view class="icon-wrapper">
        <text class="icon-check">✔</text>
      </view>
      <view class="title">支付成功</view>
      <view class="subtitle">订单已提交，商家正在加急处理中</view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info">
      <view class="item">
        <text>订单状态</text>
        <text class="status-text">待商家接单</text>
      </view>
      <view class="item">
        <text>温馨提示</text>
        <text class="tip">骑手很快上门取餐，请您耐心等待</text>
      </view>
    </view>

    <!-- 按钮 -->
    <view class="btn-group">
      <navigator url="/pages/index/index" open-type="switchTab" class="btn home-btn">
        返回首页
      </navigator>
      <navigator :url="`/pagesOrder/detail?id=${query.id}`" open-type="redirect" class="btn detail-btn">
        查看订单
      </navigator>
    </view>
  </view>
</template>

<style lang="scss">
page {
  background-color: #fff9e6;
}

.page {
  padding: 60rpx 30rpx;
}

/* 成功卡片 */
.success-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  margin-bottom: 30rpx;
  box-shadow: 0 6rpx 22rpx rgba(255, 190, 0, 0.15);

  .icon-wrapper {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background-color: #ffbc00;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 30rpx;

    .icon-check {
      font-size: 50rpx;
      color: #fff;
      font-weight: bold;
    }
  }

  .title {
    font-size: 40rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 16rpx;
  }

  .subtitle {
    font-size: 28rpx;
    color: #999;
  }
}

/* 订单信息 */
.order-info {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx 40rpx;
  margin-bottom: 50rpx;

  .item {
    display: flex;
    justify-content: space-between;
    padding: 20rpx 0;
    font-size: 28rpx;

    &:first-child {
      border-bottom: 1rpx solid #f2f2f2;
    }

    text {
      color: #666;
    }

    .status-text {
      color: #ff9f00;
      font-weight: 500;
    }

    .tip {
      color: #ffbc00;
    }
  }
}

/* 按钮组 */
.btn-group {
  display: flex;
  gap: 20rpx;

  .btn {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
  }

  .home-btn {
    border: 2rpx solid #ffe49c;
    color: #cc8a00;
  }

  .detail-btn {
    background: #ffbc00;
    color: #fff;
  }
}
</style>
