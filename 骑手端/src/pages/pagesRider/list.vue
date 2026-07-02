<script setup lang="ts">
import { ref } from 'vue'
import { getRiderOrderListAPI } from '@/services/rider'
import { onLoad } from '@dcloudio/uni-app'

// 接收类型参数 type=1 待接单 2配送中 3已完成 4收入
const type = ref('1')
const orderList = ref<any>([])
const loading = ref(false)

// 下拉刷新控制
const isRefreshing = ref(false)

// 标签页
const tabs = [
  { name: '1', text: '待接单' },
  { name: '2', text: '待配送' },
  { name: '3', text: '配送中' },
  { name: '4', text: '已完成' },
]

// 页面加载
onLoad(async (option: any) => {
  type.value = option.type || '1'
  await getOrderList()
})

// 获取订单列表
const getOrderList = async () => {
  loading.value = true
  try {
    const res = await getRiderOrderListAPI(type.value)
    orderList.value = res.data || []
  } catch (e) {
    console.log('获取订单失败')
  }
  loading.value = false
}

// 切换标签
const onTabChange = async () => {
  orderList.value = []
  await getOrderList()
}

// 去订单详情
const goDetail = (orderId: string) => {
  uni.navigateTo({
    url: `/pages/pagesRider/detail?orderId=${orderId}`,
  })
}

const onRefresh = async () => {
  isRefreshing.value = true
  await getOrderList()
  isRefreshing.value = false
}
</script>

<template>
  <view class="viewport">
    <!-- 标签栏 -->
    <view class="tabs">
      <text
        class="item"
        v-for="item in tabs"
        :key="item.name"
        @tap="
          () => {
            type = item.name
            onTabChange()
          }
        "
      >
        {{ item.text }}
      </text>
      <!-- 游标 -->
      <view
        class="cursor"
        :style="{ left: tabs.findIndex((t) => t.name === type) * 25 + '%' }"
      ></view>
    </view>

    <!-- 下拉刷新区域 -->
    <scroll-view
      scroll-y
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      class="order-scroll"
    >
      <!-- 加载中 -->
      <view class="loading" v-if="loading"> <van-loading type="spinner" /> 加载中... </view>

      <!-- 订单列表 -->
      <view class="order-container" v-else>
        <!-- 空状态 -->
        <view class="empty" v-if="orderList.length === 0"> 暂无订单 </view>

        <!-- 订单卡片 -->
        <view
          class="order-card"
          v-for="item in orderList"
          :key="item.orderId"
          @click="goDetail(item.orderId)"
        >
          <view class="card-header">
            <text class="order-id">订单号：{{ item.orderId }}</text>
            <view class="tag">
              {{
                item.orderStatus === 1
                  ? '待接单'
                  : item.orderStatus === 2
                  ? '待配送'
                  : item.orderStatus === 3
                  ? '配送中'
                  : '已完成'
              }}
            </view>
          </view>

          <view class="card-info">
            <view>类型：{{ item.orderType === 1 ? '外卖' : '跑腿' }}</view>
            <view>备注：{{ item.note || '无' }}</view>
            <view v-if="item.startAddress">取件：{{ item.startAddress }}</view>
            <view v-if="item.endAddress">送达：{{ item.endAddress }}</view>
          </view>

          <view class="card-footer">
            <text>创建时间：{{ item.createTime }}</text>
            <text class="price">¥{{ item.totalAmount || '0.00' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
page {
  height: 100%;
  overflow: hidden;
}

.viewport {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fffdf5; /* 统一暖黄色 */
}

/* 完全和用户端一样的 tabs 样式 */
.tabs {
  display: flex;
  justify-content: space-around;
  line-height: 60rpx;
  margin: 0 10rpx;
  background-color: #fffdf5;
  box-shadow: 0 4rpx 8rpx rgba(255, 215, 0, 0.12);
  position: relative;
  z-index: 9;
  border-radius: 12rpx;
  margin: 16rpx 20rpx;

  .item {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    font-size: 28rpx;
    color: #555;
    font-weight: 500;
  }

  .cursor {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 25%;
    height: 6rpx;
    border-radius: 3rpx;
    background-color: #fffdf5;
    transition: all 0.4s ease;
  }
}

/* 滚动区域（支持下拉刷新） */
.order-scroll {
  flex: 1;
}

.loading {
  padding: 40rpx;
  text-align: center;
  color: #666;
}

.order-container {
  padding: 20rpx;
}

.empty {
  text-align: center;
  padding: 60rpx;
  color: #999;
}

/* 卡片也统一成柔和圆角暖白风格 */
.order-card {
  background: #fffdf5;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;

  .order-id {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
  }

  .tag {
    font-size: 24rpx;
    padding: 4rpx 10rpx;
    background: #fff3d7;
    color: #ff9f00;
    border-radius: 6rpx;
  }
}

.card-info {
  font-size: 26rpx;
  color: #666;
  line-height: 1.7;
  margin-bottom: 16rpx;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #999;

  .price {
    color: #ff3333;
    font-weight: bold;
  }
}
</style>
