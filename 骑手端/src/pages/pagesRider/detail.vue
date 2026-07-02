<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  getRiderOrderDetailAPI,
  updateOrderStatusAPI,
  postRiderGrabOrderAPI,
} from '@/services/rider'
import { onLoad } from '@dcloudio/uni-app'

type OrderDetail = {
  orderId?: string
  orderType?: number
  orderStatus?: number
  note?: string | null
  createTime?: string | Date
  totalAmount?: number
  deliveryId?: number | null
  items?: Array<{
    itemId: number
    goodsName: string
    quantity: number
    skuId?: number
    skuAttrs?: string
    picture?: string
    payPrice?: number
  }>
  startAddress?: string
  endAddress?: string
  deliveryFee?: number
}

const detail = ref<OrderDetail>({})

onLoad(async (option: any) => {
  const orderId = option.orderId || ''
  const res = await getRiderOrderDetailAPI(orderId)
  detail.value = res.data as OrderDetail
})

const statusText = computed(() => {
  const s = detail.value.orderStatus
  if (s === 1) return '待接单'
  if (s === 2) return '待配送'
  if (s === 3) return '配送中'
  if (s === 4) return '已完成'
  return '已取消'
})

const statusDesc = computed(() => {
  const s = detail.value.orderStatus
  if (s === 1) return '等待骑手接单'
  if (s === 2) return '等待取货'
  if (s === 3) return '正在配送'
  if (s === 4) return '已完成'
  return '已取消'
})

const grabOrder = async () => {
  if (!detail.value.orderId) return
  const res = await postRiderGrabOrderAPI(detail.value.orderId)
  if (res.code === '200') {
    uni.showToast({ title: '抢单成功', icon: 'success' })
    detail.value.orderStatus = 2
  } else {
    uni.showToast({ title: res.msg || '抢单失败', icon: 'none' })
  }
}

const startDelivery = async () => {
  if (!detail.value.orderId) return
  const res = await updateOrderStatusAPI({
    orderId: detail.value.orderId,
    orderStatus: 3,
  })
  if (res.code === '200') {
    uni.showToast({ title: '开始配送', icon: 'success' })
    detail.value.orderStatus = 3
  } else {
    uni.showToast({ title: res.msg || '失败', icon: 'none' })
  }
}

const finishDelivery = async () => {
  uni.navigateTo({
    url: `/pages/pagesRider/finish?id=${detail.value.orderId}`,
  })
}
</script>

<template>
  <view class="page-container">
    <!-- 顶部状态 -->
    <view class="status-header">
      <view class="status-info">
        <text class="status-text">{{ statusText }}</text>
        <text class="status-desc">{{ statusDesc }}</text>
      </view>
      <view class="price-info">
        <text class="price-label">订单金额</text>
        <text class="price-value">¥{{ detail.deliveryFee || '0.00' }}</text>
      </view>
    </view>

    <!-- 订单卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="task-title">订单号：{{ detail.orderId }}</text>
        <van-tag plain type="primary" color="#FF9500">
          {{ detail.orderType === 1 ? '外卖' : '跑腿' }}
        </van-tag>
      </view>

      <view class="task-desc">备注：{{ detail.note || '无' }}</view>
      <van-divider margin="24rpx 0" />

      <!-- 时间 -->
      <view class="time-item">
        <van-icon name="clock-o" size="32rpx" color="#999" />
        <text class="time-label">创建时间</text>
        <text class="time-value">{{ detail.createTime }}</text>
      </view>
      <van-divider margin="24rpx 0" />

      <!-- 地址（外卖+跑腿通用） -->
      <view class="address-section">
        <view class="address-item">
          <view class="address-icon start">
            <van-icon name="location-o" size="28rpx" color="#fff" />
          </view>
          <view class="address-content">
            <text class="address-label">
              {{ detail.orderType === 1 ? '商家地址' : '取件地址' }}
            </text>
            <text class="address-text">{{ detail.startAddress || '未获取到地址' }}</text>
          </view>
        </view>
        <view class="address-line"></view>
        <view class="address-item">
          <view class="address-icon end">
            <van-icon name="aim" size="28rpx" color="#fff" />
          </view>
          <view class="address-content">
            <text class="address-label">
              {{ detail.orderType === 1 ? '收货地址' : '送达地址' }}
            </text>
            <text class="address-text">{{ detail.endAddress || '未获取到地址' }}</text>
          </view>
        </view>
      </view>

      <van-divider margin="24rpx 0" v-if="detail.items" />

      <view v-if="detail.items" class="goods-section">
        <view class="card-title">商品明细</view>
        <view class="goods-list">
          <view class="goods-item" v-for="i in detail.items" :key="i.itemId">
            <image class="goods-img" :src="i.picture || '/static/logo.png'" mode="aspectFill" />
            <view class="goods-info">
              <view class="goods-name ellipsis">{{ i.goodsName }}</view>
              <view class="goods-sku">
                {{ i.skuId === -1 ? '默认规格' : i.skuAttrs || '默认规格' }}
              </view>
              <view class="goods-price"> ¥{{ i.payPrice || 0 }} × {{ i.quantity }} </view>
            </view>
            <view class="goods-total"> ¥{{ ((i.payPrice || 0) * i.quantity).toFixed(2) }} </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="action-bar">
      <view class="accept-btn grab" v-if="detail.orderStatus === 1" @click="grabOrder">
        立即抢单
      </view>
      <view class="accept-btn doing" v-else-if="detail.orderStatus === 2" @click="startDelivery">
        开始配送
      </view>
      <view class="accept-btn done" v-else-if="detail.orderStatus === 3" @click="finishDelivery">
        确认完成
      </view>
      <view class="accept-btn finish" v-else>已完成</view>
    </view>
  </view>
</template>

<style scoped>
page {
  background: #fff9e6;
}

.page-container {
  min-height: 100vh;
  padding-bottom: 140rpx;
}

.status-header {
  background: linear-gradient(135deg, #ffb347, #ff9800);
  padding: 50rpx 30rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}
.status-text {
  font-size: 38rpx;
  font-weight: bold;
}
.status-desc {
  font-size: 26rpx;
  opacity: 0.9;
  margin-top: 8rpx;
}
.price-label {
  font-size: 24rpx;
  opacity: 0.9;
}
.price-value {
  font-size: 44rpx;
  font-weight: bold;
}

.info-card {
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.task-title {
  font-size: 32rpx;
  font-weight: bold;
}
.task-desc {
  font-size: 26rpx;
  color: #666;
  margin-top: 12rpx;
}

.time-item {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #666;
  margin: 8rpx 0;
}
.time-label {
  margin: 0 16rpx;
}

/* 地址 */
.address-section {
  position: relative;
}
.address-item {
  display: flex;
  align-items: flex-start;
  margin: 16rpx 0;
}
.address-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}
.address-icon.start {
  background: #07c160;
}
.address-icon.end {
  background: #ee0a24;
}
.address-label {
  font-size: 24rpx;
  color: #999;
}
.address-text {
  font-size: 28rpx;
  color: #333;
  margin-top: 4rpx;
}
.address-line {
  position: absolute;
  left: 24rpx;
  top: 52rpx;
  width: 2rpx;
  height: 50rpx;
  background: #eee;
}

/* ====================== */
/* 🔥 商品样式（和你用户端完全一样） */
/* ====================== */
.goods-section {
  margin-top: 20rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}
.goods-list {
  gap: 20rpx;
  display: flex;
  flex-direction: column;
}
.goods-item {
  display: flex;
  align-items: center;
}
.goods-img {
  width: 90rpx;
  height: 90rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  background: #f6f6f6;
}
.goods-info {
  flex: 1;
}
.goods-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}
.goods-sku {
  font-size: 24rpx;
  color: #ff9800;
  background: #fff1d9;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  display: inline-block;
  margin: 6rpx 0;
}
.goods-price {
  font-size: 24rpx;
  color: #999;
}
.goods-total {
  font-size: 26rpx;
  color: #ff5722;
  font-weight: bold;
}
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 底部按钮 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx;
}
.accept-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  text-align: center;
}
.accept-btn.grab {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
}
.accept-btn.doing {
  background: #07c160;
}
.accept-btn.done {
  background: #007aff;
}
.accept-btn.finish {
  background: #999;
}
</style>
