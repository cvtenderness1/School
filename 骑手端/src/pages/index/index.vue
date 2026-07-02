<script setup lang="ts">
import { getRiderGrabListAPI, postRiderGrabOrderAPI } from '@/services/rider'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { http } from '@/utils/http'
type GrabOrder = {
  orderId: string
  orderType: number
  orderStatus: number
  startAddress?: string
  endAddress?: string
  expectTime?: string
  deliveryFee?: number
  totalAmount?: number
  payMoney?: number
  merchantId?: number
  note?: string
  deliveryTimeType?: number
}

const orderList = ref<GrabOrder[]>([])
// 下拉刷新
const isRefreshing = ref(false)

// 加载可抢订单
const loadGrabOrders = async () => {
  const res = await getRiderGrabListAPI()
  orderList.value = res.data as GrabOrder[]
}

// 抢单
const grabOrder = async (orderId: string, e?: Event) => {
  if (e) e.stopPropagation()

  try {
    // 调用抢单接口
    await postRiderGrabOrderAPI(orderId)

    // 不报错才会走到这里
    uni.showToast({
      title: '抢单成功',
      icon: 'success',
    })

    // 刷新列表
    loadGrabOrders()
  } catch (err) {
    // 报错了
  }
}

// 去详情
const goOrderDetail = (orderId: string) => {
  uni.navigateTo({
    url: `/pages/pagesRider/detail?orderId=${orderId}`,
  })
}

// 下拉刷新
const onRefresh = async () => {
  isRefreshing.value = true
  await loadGrabOrders()
  isRefreshing.value = false
}

onLoad(() => {
  loadGrabOrders()
})
</script>

<template>
  <view class="page-container">
    <!-- 下拉刷新 -->
    <scroll-view
      scroll-y
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      class="refresh-scroll"
    >
      <!-- 顶部背景图 -->
      <view class="banner">
        <image
          class="banner-img"
          src="https://images.unsplash.com/photo-1562774053-701939374585?w=800&h=400&fit=crop"
          mode="aspectFill"
        ></image>
      </view>

      <!-- 公告栏 -->
      <view class="notice-section">
        <van-notice-bar
          left-icon="volume-o"
          text="欢迎来到骑手接单大厅"
          background="#FFF7E6"
          color="#FF8C00"
        />
      </view>

      <!-- 可抢订单列表 -->
      <view class="task-section">
        <view class="section-header">
          <text class="section-title">可抢订单</text>
        </view>

        <view class="task-list">
          <view
            class="task-card"
            v-for="item in orderList"
            :key="item.orderId"
            @click="goOrderDetail(item.orderId)"
          >
            <view class="task-card-gradient"></view>

            <view class="task-header">
              <text class="task-title">
                {{ item.note && item.note.trim() ? item.note : '订单：' + item.orderId }}
              </text>
              <view class="task-tag">{{ item.orderType === 1 ? '外卖' : '跑腿' }}</view>
            </view>

            <!-- 起点 + 终点 + 期望送达时间 -->
            <view class="task-info">
              <view class="task-location">
                <view class="location-item">
                  <van-icon name="location-o" size="28rpx" color="#07c160" />
                  <text class="location-text">取件：{{ item.startAddress || '未设置' }}</text>
                </view>
                <view class="location-item">
                  <van-icon name="aim" size="28rpx" color="#ee0a24" />
                  <text class="location-text">送达：{{ item.endAddress || '未设置' }}</text>
                </view>
              </view>
              <view class="time-item" style="margin-top: 8rpx">
                <van-icon name="clock-o" size="24rpx" color="#ff9f00" />
                <text class="time-text">
                  期望送达：
                  <text v-if="item.deliveryTimeType === 0">尽快送达</text>
                  <text v-else>{{ item.deliveryTimeType }}小时内送达</text>
                </text>
              </view>
            </view>

            <view class="task-footer">
              <button class="grab-btn" @click.stop="grabOrder(item.orderId)">立即抢单</button>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty" v-if="orderList.length === 0">暂无可抢订单</view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
/* 全局容器 */
.page-container {
  height: 100vh;
  background-color: #f5f5f5;
}
/* 下拉刷新滚动层 */
.refresh-scroll {
  height: 100%;
}

/* 顶部 Banner */
.banner {
  width: 100%;
  height: 360rpx;
}
.banner-img {
  width: 100%;
  height: 100%;
}

/* 公告 */
.notice-section {
  margin: 24rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

/* 订单区域 */
.task-section {
  padding: 0 24rpx;
}
.section-header {
  margin-bottom: 24rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

/* 卡片列表 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 订单卡片 */
.task-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}
.task-card-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 80rpx;
  background: linear-gradient(to bottom, rgba(255, 149, 0, 0.1), transparent);
}

/* 标题 */
.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 1;
}
.task-title {
  font-size: 30rpx;
  font-weight: bold;
}
.task-tag {
  background: #fff8dc;
  color: #daa520;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
}

/* 状态 */
.task-info {
  margin-bottom: 20rpx;
  font-size: 26rpx;
  color: #666;
}
/* 位置样式 */
.task-location {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.location-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.location-text {
  color: #666;
  font-size: 26rpx;
}
.time-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #999;
  font-size: 24rpx;
}

/* 底部按钮 */
.task-footer {
  display: flex;
  justify-content: flex-end;
}
.grab-btn {
  background: linear-gradient(135deg, #ff9500 0%, #ff7a00 100%);
  color: #fff;
  border-radius: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  border: none;
}

/* 空状态 */
.empty {
  text-align: center;
  color: #999;
  margin-top: 60rpx;
  font-size: 28rpx;
}
</style>
