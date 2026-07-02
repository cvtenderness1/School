<script setup lang="ts">
import { OrderState } from '@/services/constants'
import { orderStateList } from '@/services/constants'
import { putMemberOrderReceiptByIdAPI } from '@/services/order'
import { deleteMemberOrderAPI } from '@/services/order'
import { getMemberOrderAPI } from '@/services/order'
import { getPayMockAPI, getPayWxPayMiniPayAPI } from '@/services/pay'
import type { OrderItem, OrderListParams } from '@/types/order'
import { onMounted, ref } from 'vue'

const { safeAreaInsets } = uni.getSystemInfoSync()

const props = defineProps<{
  orderState: number
}>()

const queryParams: Required<OrderListParams> = {
  pageNum: 1,
  pageSize: 5,
  orderState: props.orderState,
}

const orderList = ref<OrderItem[]>([])
const isLoading = ref(false)
const isFinish = ref(false)
const isTriggered = ref(false)

const getMemberOrderData = async () => {
  if (isLoading.value) return
  if (isFinish.value) {
    return uni.showToast({ icon: 'none', title: '没有更多数据~' })
  }
  isLoading.value = true
  const res = await getMemberOrderAPI(queryParams)
  isLoading.value = false

  orderList.value.push(...res.data.list)

  if (res.data.hasNextPage) {
    queryParams.pageNum++
  } else {
    isFinish.value = true
  }
}

onMounted(() => {
  getMemberOrderData()
})

// 订单支付
const onOrderPay = async (id: string) => {
  try {
    if (import.meta.env.DEV) {
      await getPayMockAPI({ orderId: id })
    } else {
      // #ifdef MP-WEIXIN
      const res = await getPayWxPayMiniPayAPI({ orderId: id })
      await wx.requestPayment(res.data)
      // #endif

      // #ifdef H5 || APP-PLUS
      await getPayMockAPI({ orderId: id })
      // #endif
    }

    uni.showToast({
      title: '支付成功',
      icon: 'success',
      mask: true
    })

    const order = orderList.value.find((v) => String(v.id) === String(id))
    if (order) {
      order.orderState = OrderState.DaiJieDan
    }

    setTimeout(() => {
      onRefresherrefresh()
    }, 1500)

  } catch (e) {
    console.error('支付失败详情：', e)
    uni.showToast({
      icon: 'none',
      title: '支付已取消'
    })
  }
}

// 确认收货
const onOrderConfirm = (id: string) => {
  uni.showModal({
    content: '为保障您的权益，请收到货并确认无误后，再确认收货',
    confirmColor: '#27BA9B',
    success: async (res) => {
      if (res.confirm) {
        await putMemberOrderReceiptByIdAPI(id)
        uni.showToast({ icon: 'success', title: '确认收货成功' })
        const order = orderList.value.find((v) => v.id === id)
        if (order) order.orderState = OrderState.YiWanCheng
      }
    },
  })
}

// 删除订单
const onOrderDelete = (id: string) => {
  uni.showModal({
    content: '你确定要删除该订单？',
    confirmColor: '#27BA9B',
    success: async (res) => {
      if (res.confirm) {
        await deleteMemberOrderAPI({ ids: [id] })
        const index = orderList.value.findIndex((v) => v.id === id)
        if (index !== -1) orderList.value.splice(index, 1)
      }
    },
  })
}

const onRefresherrefresh = async () => {
  isTriggered.value = true
  queryParams.pageNum = 1
  orderList.value = []
  isFinish.value = false
  await getMemberOrderData()
  isTriggered.value = false
}


// 退款申请
const onOrderRefund = (id: string) => {
  uni.showModal({
    content: '确定要对该订单申请退款吗？',
    confirmColor: '#27BA9B',
    success: async (res) => {
      if (res.confirm) {
        // 这里调用你的退款接口
        // await refundOrderAPI(id)

        uni.showToast({
          icon: 'success',
          title: '退款申请已提交',
          mask: true
        })

        // 可选：刷新订单列表
        setTimeout(() => {
          onRefresherrefresh()
        }, 1000)
      }
    }
  })
}
</script>

<template>
  <scroll-view enable-back-to-top scroll-y class="orders" refresher-enabled :refresher-triggered="isTriggered"
    @refresherrefresh="onRefresherrefresh" @scrolltolower="getMemberOrderData">
    <view class="card" v-for="order in orderList" :key="order.id">
      <view class="status">
        <text class="date">{{ order.createTime }}</text>
        <text>{{ orderStateList[order.orderState].text }}</text>
        <text v-if="order.orderState === OrderState.YiWanCheng || order.orderState === OrderState.YiQuXiao"
          class="icon-delete" @tap="onOrderDelete(order.id)"></text>
      </view>

      <navigator v-for="item in order.skus" :key="item.id" class="goods" :url="`/pagesOrder/detail?id=${order.id}`"
        hover-class="none">
        <view class="cover">
          <image class="image" mode="aspectFit" :src="item.picture"></image>
        </view>
        <view class="meta">
          <view class="name ellipsis">{{ item.goodsName }}</view>
          <view class="type">{{ item.skuAttrs }}</view>
        </view>
      </navigator>

      <view class="payment">
        <text class="quantity">共{{ order.totalNum }}件商品</text>
        <text>实付</text>
        <text class="amount"> <text class="symbol">¥</text>{{ order.payMoney }}</text>
      </view>

      <view class="action">
        <template v-if="order.orderState === OrderState.DaiFuKuan">
          <view class="button primary" @tap="onOrderPay(order.id)">去支付</view>
        </template>

        <template v-else>
          <navigator class="button secondary" :url="`/pagesOrder/create?orderId=${order.id}`" hover-class="none">
            再次购买
          </navigator>

          <!-- 配送中 = 显示确认收货 -->
          <view v-if="order.orderState === OrderState.PeiSongZhong" class="button primary"
            @tap="onOrderConfirm(order.id)">
            确认收货
          </view>

          <!-- 已完成 = 显示退款按钮 -->
          <view v-else-if="order.orderState === OrderState.YiWanCheng" class="button primary"
            @tap="onOrderRefund(order.id)">
            退款
          </view>
        </template>
      </view>
    </view>

    <view class="loading-text" :style="{ paddingBottom: safeAreaInsets?.bottom + 'px' }">
      {{ isFinish ? '没有更多数据~' : '正在加载...' }}
    </view>
  </scroll-view>
</template>

<style lang="scss">
.orders {
  height: 100%;
  background-color: #fff9e6;
  /* 暖黄背景 */

  .card {
    min-height: 100rpx;
    padding: 20rpx;
    margin: 20rpx 20rpx 0;
    border-radius: 16rpx;
    /* 更大圆角 */
    background-color: #fffdf5;
    /* 暖白卡片 */
    box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);
    /* 淡黄色阴影 */

    &:last-child {
      padding-bottom: 40rpx;
    }
  }

  .status {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 28rpx;
    color: #888;
    margin-bottom: 15rpx;

    .date {
      color: #666;
      flex: 1;
    }

    .icon-delete {
      line-height: 1;
      margin-left: 10rpx;
      padding-left: 10rpx;
      border-left: 1rpx solid #ffeccd;
      /* 浅黄分割线 */
      color: #ff5722;
    }
  }

  .goods {
    display: flex;
    margin-bottom: 20rpx;

    .cover {
      width: 170rpx;
      height: 170rpx;
      margin-right: 20rpx;
      border-radius: 12rpx;
      overflow: hidden;
      position: relative;

      .image {
        width: 170rpx;
        height: 170rpx;
      }
    }

    .meta {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .name {
      height: 80rpx;
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
      line-height: 1.4;
    }

    .type {
      line-height: 1.8;
      padding: 0 15rpx;
      margin-top: 10rpx;
      font-size: 24rpx;
      align-self: flex-start;
      border-radius: 6rpx;
      color: #ff9f00;
      /* 黄色文字 */
      background-color: #fff1d9;
      /* 浅黄标签 */
    }
  }

  .payment {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    line-height: 1;
    padding: 20rpx 0;
    text-align: right;
    color: #888;
    font-size: 28rpx;
    border-bottom: 1rpx solid #ffeccd;
    /* 浅黄分割线 */

    .quantity {
      font-size: 24rpx;
      margin-right: 16rpx;
    }

    .amount {
      color: #ff5722;
      /* 橙黄色价格 */
      margin-left: 6rpx;
      font-weight: bold;
    }

    .symbol {
      font-size: 20rpx;
    }
  }

  .action {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding-top: 20rpx;

    .button {
      width: 180rpx;
      height: 60rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 20rpx;
      border-radius: 60rpx;
      border: 1rpx solid #ccc;
      font-size: 26rpx;
      color: #444;
    }

    .secondary {
      color: #ff9f00;
      border-color: #ff9f00;
    }

    .primary {
      color: #fff;
      background-color: #ff9f00;
      /* 暖黄色主按钮 */
      border-color: #ff9f00;
    }
  }

  .loading-text {
    text-align: center;
    font-size: 28rpx;
    color: #999;
    padding: 20rpx 0;
  }
}
</style>
