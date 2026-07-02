<script setup lang="ts">
import {
  deleteMemberOrderAPI,
  getMemberOrderByIdAPI,
  getMemberOrderCancelByIdAPI,
  getMemberOrderLogisticsByIdAPI,
  getMemberOrderConsignmentByIdAPI,
  putMemberOrderReceiptByIdAPI,
  postMemberAfterSaleAPI,
  postMemberCommentAPI,
} from '@/services/order'
import type { LogisticItem } from '@/types/order'
import { onLoad, onReady, onShow } from '@dcloudio/uni-app'
import { ref, computed } from 'vue'
import { getPayMockAPI, getPayWxPayMiniPayAPI } from '@/services/pay'

const { safeAreaInsets } = uni.getSystemInfoSync()

const popup = ref<UniHelper.UniPopupInstance>()
// 支付方式弹窗
const payPopup = ref<UniHelper.UniPopupInstance>()

const reasonList = ref([
  '商品无货',
  '不想要了',
  '商品信息填错了',
  '地址信息填写错误',
  '商品降价',
  '其它',
])
const reason = ref('')

const onCopy = (id: string) => {
  uni.setClipboardData({ data: id })
}

const query = defineProps<{
  id: string
}>()

const pages = getCurrentPages()

type PageInstance = Page.PageInstance & WechatMiniprogram.Page.InstanceMethods<any>

// #ifdef MP-WEIXIN
const pageInstance = pages.at(-1) as PageInstance

onReady(() => {
  pageInstance.animate(
    '.navbar',
    [{ backgroundColor: 'transparent' }, { backgroundColor: '#f8f8f8' }],
    1000,
    {
      scrollSource: '#scroller',
      timeRange: 1000,
      startScrollOffset: 0,
      endScrollOffset: 50,
    },
  )
  pageInstance.animate('.navbar .title', [{ color: 'transparent' }, { color: '#000' }], 1000, {
    scrollSource: '#scroller',
    timeRange: 1000,
    startScrollOffset: 0,
    endScrollOffset: 50,
  })
  pageInstance.animate('.navbar .back', [{ color: '#fff' }, { color: '#000' }], 1000, {
    scrollSource: '#scroller',
    timeRange: 1000,
    startScrollOffset: 0,
    endScrollOffset: 50,
  })
})
// #endif

const order = ref<any>(null)

const getMemberOrderByIdData = async () => {
  const res = await getMemberOrderByIdAPI(query.id)
  order.value = res.data
  console.log('订单详情', order.value)

  if ([3, 4, 5].includes(order.value.orderState)) {
    getMemberOrderLogisticsByIdData()
  }
}

const logisticList = ref<LogisticItem[]>([])
const getMemberOrderLogisticsByIdData = async () => {
  try {
    const res = await getMemberOrderLogisticsByIdAPI(query.id)
    logisticList.value = res.data.list
  } catch (e) {
    logisticList.value = []
  }
}

onLoad(() => {
  getMemberOrderByIdData()
})
onShow(() => {
  getMemberOrderByIdData()
})

const onTimeup = () => {
  order.value!.orderState = 5
}

// 打开支付方式选择
const openPayType = () => {
  payPopup.value!.open?.()
}

// 真正执行支付
const onOrderPayReal = async () => {
  try {
    // 先关闭支付弹窗
    payPopup.value!.close?.()

    if (import.meta.env.DEV) {
      await getPayMockAPI({ orderId: query.id })
    } else {
      // #ifdef MP-WEIXIN
      const res = await getPayWxPayMiniPayAPI({ orderId: query.id })
      await wx.requestPayment(res.data)
      // #endif
      // #ifdef H5 || APP-PLUS
      await getPayMockAPI({ orderId: query.id })
      // #endif
    }
    uni.redirectTo({ url: `/pagesOrder/payment?id=${query.id}` })
  } catch (e) {
    uni.showToast({ icon: 'none', title: '支付失败' })
  }
}

const isDev = import.meta.env.DEV
const onOrderSend = async () => {
  if (isDev && [1, 2].includes(order.value!.orderState)) {
    await getMemberOrderConsignmentByIdAPI(query.id)
    uni.showToast({ icon: 'success', title: '模拟发货完成' })
    order.value!.orderState = 3
  }
}

const onOrderConfirm = () => {
  uni.showModal({
    content: '为保障您的权益，请确认无误后再确认',
    confirmColor: '#27BA9B',
    success: async (success) => {
      if (success.confirm && order.value!.orderState === 3) {
        const res = await putMemberOrderReceiptByIdAPI(query.id)
        order.value = res.data
      }
    },
  })
}

const onOrderDelete = () => {
  uni.showModal({
    content: '是否删除订单',
    confirmColor: '#27BA9B',
    success: async (success) => {
      if (success.confirm && [4, 5].includes(order.value!.orderState)) {
        await deleteMemberOrderAPI({ ids: [query.id] })
        uni.navigateBack()
      }
    },
  })
}

const onOrderCancel = async () => {
  if (!reason.value) {
    uni.showToast({ icon: 'none', title: '请选择取消原因' })
    return
  }
  try {
    const res = await getMemberOrderCancelByIdAPI(query.id, { cancelReason: reason.value })
    order.value = res.data
    popup.value?.close?.()
    uni.showToast({ icon: 'none', title: '订单取消成功' })
  } catch (e) {
    popup.value?.close?.()
    uni.showToast({ icon: 'none', title: '取消失败' })
  }
}
const markers = computed(() => {
  if (!order.value?.merchantLat || !order.value?.merchantLng) return [] as any[]
  const mLat = Number(order.value.merchantLat)
  const mLng = Number(order.value.merchantLng)

  let uLat = Number(order.value.receiverLat) || mLat + 0.0008
  let uLng = Number(order.value.receiverLng) || mLng + 0.0008

  // 骑手位置（从订单里取 riderLat / riderLng）
  const rLat = Number(order.value.riderLat) || mLat + 0.0004
  const rLng = Number(order.value.riderLng) || mLng + 0.0004

  return [
    {
      id: 1,
      latitude: mLat,
      longitude: mLng,
      iconPath: "https://tse2-mm.cn.bing.net/th/id/OIP-C.X5PGKr5SDQVTlFSoCfaXlgHaHa?w=174&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3",
      width: 32,
      height: 32,
      callout: {
        content: order.value.merchantName || '商家',
        fontSize: 12,
        color: '#ff9800'
      }
    } as any,
    {
      id: 2,
      latitude: uLat,
      longitude: uLng,
      iconPath: order.value?.userAvatar || "https://tse1-mm.cn.bing.net/th/id/OIP-C.l0XRaYzWUHrgtLufh0wdbQAAAA?w=180&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3",
      width: 36,
      height: 36,
      anchor: { x: 0.5, y: 0.5 },
      callout: { content: '您的位置', fontSize: 12 }
    } as any,
    //骑手位置
    {
      id: 3,
      latitude: rLat,
      longitude: rLng,
      iconPath: "https://tse2-mm.cn.bing.net/th/id/OIP-C.qUuEHRId1AAjwrRzaSsJnQAAAA?w=164&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3",
      width: 36,
      height: 36,
      callout: { content: '骑手正在配送', fontSize: 12, color: '#ff5722' }
    } as any
  ] as any[]
})

//道路规划路线
const polyline = computed(() => {
  if (!order.value?.merchantLat || !order.value?.merchantLng) return [] as any[]

  const mLat = Number(order.value.merchantLat)
  const mLng = Number(order.value.merchantLng)

  let uLat = Number(order.value.receiverLat) || mLat + 0.0008
  let uLng = Number(order.value.receiverLng) || mLng + 0.0008

  // 计算中间偏移点，模拟真实道路转弯
  const midLat1 = mLat + (uLat - mLat) * 0.25
  const midLng1 = mLng
  const midLat2 = mLat + (uLat - mLat) * 0.25
  const midLng2 = mLng + (uLng - mLng) * 0.5
  const midLat3 = mLat + (uLat - mLat) * 0.75
  const midLng3 = mLng + (uLng - mLng) * 0.5
  const midLat4 = mLat + (uLat - mLat) * 0.75
  const midLng4 = uLng

  const points = [
    { latitude: mLat, longitude: mLng },
    { latitude: midLat1, longitude: midLng1 },
    { latitude: midLat2, longitude: midLng2 },
    { latitude: midLat3, longitude: midLng3 },
    { latitude: midLat4, longitude: midLng4 },
    { latitude: uLat, longitude: uLng }
  ]

  return [{
    points,
    color: '#ff9800dd',
    width: 5,
    arrowLine: true
  }] as any[]
})

//多商家送达凭证
// 提取所有子订单的送达凭证
const allFinishImages = computed(() => {
  if (!order.value || order.value.orderState !== 4) return []
  // 直接读取后端返回的图片数组
  return order.value.finishImgList || []
})
// 图片预览
const previewImage = (url: string) => {
  uni.previewImage({
    urls: allFinishImages.value,
    current: url
  })
}
//售后
const afterSalePopup = ref<UniHelper.UniPopupInstance>()
const afterSaleTypeList = ref([
  { label: '取消/改单', value: 1 },
  { label: '申诉退款', value: 2 },
  { label: '投诉差评', value: 3 },
  { label: '发票申请', value: 4 }
])
const afterSaleType = ref(1)
const afterSaleReason = ref('')

const openAfterSale = () => {
  afterSalePopup.value?.open?.()
}

const onSubmitAfterSale = async () => {
  if (!afterSaleReason.value) {
    uni.showToast({ icon: 'none', title: '请填写原因' })
    return
  }

  try {
    await postMemberAfterSaleAPI({
      orderId: query.id,
      type: afterSaleType.value,
      reason: afterSaleReason.value
    })
    afterSalePopup.value?.close?.()
    uni.showToast({ icon: 'success', title: '提交成功' })
  } catch (e) {
    uni.showToast({ icon: 'none', title: '提交失败' })
  }
}

// 评价
const goToComment = () => {
  uni.navigateTo({
    url: `/pagesOrder/comment/create?orderId=${query.id}`
  })
}
</script>

<template>
  <view class="navbar" :style="{ paddingTop: safeAreaInsets?.top + 'px' }">
    <view class="wrap">
      <navigator v-if="pages.length > 1" open-type="navigateBack" class="back icon-left"></navigator>
      <navigator v-else url="/pages/index/index" open-type="switchTab" class="back icon-home"></navigator>
      <view class="title">订单详情</view>
    </view>
  </view>

  <scroll-view enable-back-to-top scroll-y class="viewport" id="scroller">
    <template v-if="order">
      <view class="overview" :style="{ paddingTop: safeAreaInsets!.top + 20 + 'px' }">
        <template v-if="order.orderState === 0">
          <view class="status icon-clock">等待付款</view>
          <view class="tips">
            <text class="money">应付金额: ¥ {{ order.payMoney }}</text>
            <text class="time">支付剩余</text>
            <uni-countdown :second="order.countdown" color="#fff" splitor-color="#fff" :show-day="false"
              :show-colon="false" @timeup="onTimeup" />
          </view>
          <view class="button" @tap="openPayType">去支付</view>
        </template>

        <template v-else-if="order.orderState >= 1 && order.orderState <= 3">
          <view class="status">
            {{
              order.orderState === 1 ? '待接单' :
                order.orderState === 2 ? '待配送' : '配送中'
            }}
          </view>

          <view v-if="order.orderState === 3 && order.merchantLat && order.merchantLng" class="map-wrapper">
            <view class="map-title">骑手正在配送</view>
            <map class="map" :latitude="order.merchantLat" :longitude="order.merchantLng" :scale="17" :markers="markers"
              :polyline="polyline" />
          </view>

          <view class="button-group">
            <view v-if="isDev && order.orderState <= 2" class="button" @tap="onOrderSend">模拟发货</view>
            <view v-if="order.orderState === 3" class="button" @tap="onOrderConfirm">确认收货</view>
          </view>
        </template>

        <template v-else>
          <view class="status">
            {{ order.orderState === 4 ? '已完成' : '已取消' }}
          </view>
        </template>
      </view>

      <view class="shipment">
        <view v-for="item in logisticList" :key="item.id" class="item">
          <view class="message">{{ item.text }}</view>
          <view class="date">{{ item.time }}</view>
        </view>
        <view class="locate">
          <view class="user">{{ order.receiverContact }} {{ order.receiverMobile }}</view>
          <view class="address">{{ order.receiverAddress }}</view>
        </view>
      </view>

      <view class="goods">
        <view class="item">
          <navigator class="navigator" v-for="item in order.skus" :key="item.id"
            :url="`/pages/goods/goods?id=${item.goodsId}`" hover-class="none">
            <image class="cover" :src="item.picture"></image>
            <view class="meta">
              <view class="name ellipsis">{{ item.goodsName }}</view>
              <view class="type">{{ item.skuAttrs }}</view>
              <view class="price">
                <view class="actual">
                  <text class="symbol">¥</text>
                  <text>{{ item.payPrice }}</text>
                </view>
              </view>
              <view class="quantity">x{{ item.quantity }}</view>
            </view>
          </navigator>
          <view class="action" v-if="order.orderState === 4">
            <view class="button primary" @tap="openAfterSale">申请售后</view>
            <view class="button" @tap="goToComment">去评价</view>
          </view>
        </view>
        <view class="total">
          <view class="row">
            <view class="text">商品总价:</view>
            <view class="symbol">{{ order.totalMoney }}</view>
          </view>
          <view class="row">
            <view class="text">运费:</view>
            <view class="symbol">{{ order.postFee }}</view>
          </view>
          <view class="row">
            <view class="text">应付金额:</view>
            <view class="symbol primary">{{ order.payMoney }}</view>
          </view>
        </view>
        <!-- 多商家 送达凭证（多图版） -->
        <view class="evidence" v-if="allFinishImages.length > 0">
          <view class="title">骑手送达凭证（共{{ allFinishImages.length }}张）</view>
          <view class="evidence-list">
            <image class="evidence-img" v-for="(img, idx) in allFinishImages" :key="idx" :src="img" mode="widthFix"
              @tap="previewImage(img)" />
          </view>
        </view>
      </view>

      <view class="detail">
        <view class="title">订单信息</view>
        <view class="row">
          <view class="item">
            订单编号: {{ query.id }} <text class="copy" @tap="onCopy(query.id)">复制</text>
          </view>
          <view class="item">下单时间: {{ order.createTime }}</view>
        </view>
      </view>

      <view class="toolbar-height" :style="{ paddingBottom: safeAreaInsets?.bottom + 'px' }"></view>
      <view class="toolbar" :style="{ paddingBottom: safeAreaInsets?.bottom + 'px' }">
        <template v-if="order.orderState === 0">
          <view class="button primary" @tap="openPayType">去支付</view>
          <view class="button" @tap="popup?.open?.()">取消订单</view>
        </template>
        <template v-else>
          <view class="button secondary" :url="`/pagesOrder/create?orderId=${query.id}`">
            再次购买
          </view>
          <view class="button primary" v-if="order.orderState === 3" @tap="onOrderConfirm">确认收货</view>
          <view class="button" v-if="order.orderState === 4" @tap="goToComment">去评价</view>
          <view class="button delete" v-if="[4, 5].includes(order.orderState)" @tap="onOrderDelete">删除订单</view>
        </template>
      </view>
    </template>
  </scroll-view>

  <!-- 取消订单弹窗 -->
  <uni-popup ref="popup" type="bottom" background-color="#fff">
    <view class="popup-root">
      <view class="title">订单取消</view>
      <view class="description">
        <view class="tips">请选择取消订单的原因：</view>
        <view class="cell" v-for="item in reasonList" :key="item" @tap="reason = item">
          <text class="text">{{ item }}</text>
          <text class="icon" :class="{ checked: item === reason }"></text>
        </view>
      </view>
      <view class="footer">
        <view class="button" @tap="popup?.close?.()">取消</view>
        <view class="button primary" @tap="onOrderCancel">确认</view>
      </view>
    </view>
  </uni-popup>

  <!-- 支付方式选择弹窗 -->
  <uni-popup ref="payPopup" type="bottom" background-color="#fff">
    <view class="pay-popup-root">
      <view class="title">选择支付方式</view>
      <view class="pay-list">
        <view class="pay-item" @tap="onOrderPayReal">
          <image class="icon"
            src="https://s1.aigei.com/src/img/png/a8/a815e25f2e0748d6bcee79b957582d4d.png?e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:2FtjpyWCGslvFxZsBFgawBBnZXs=">
          </image>
          <text class="name">微信支付</text>

        </view>
        <view class="pay-item" @tap="onOrderPayReal">
          <image class="icon"
            src="https://s1.aigei.com/src/img/png/84/84a0b942cadd4f8983954e5386bf6d44.png?e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:oUi35_XBhAudCO6aaoE3OIBDIu4="
            mode="widthFix"></image>
          <text class="name">支付宝支付</text>

        </view>
      </view>
      <view class="close" @tap="payPopup?.close?.()">取消</view>
    </view>
  </uni-popup>


  <!-- 售后弹窗 -->
  <uni-popup ref="afterSalePopup" type="bottom" background-color="#fff">
    <view class="popup-root">
      <view class="title">申请售后</view>
      <view class="description">
        <view class="tips">选择售后类型：</view>
        <view class="cell" v-for="item in afterSaleTypeList" :key="item.value" @tap="afterSaleType = item.value">
          <text class="text">{{ item.label }}</text>
          <text class="icon" :class="{ checked: afterSaleType === item.value }"></text>
        </view>

        <view class="tips" style="margin-top:20rpx">问题描述：</view>
        <textarea v-model="afterSaleReason" placeholder="请输入详细问题"
          style="border:1rpx solid #eee;border-radius:12rpx;padding:20rpx;margin-top:10rpx"></textarea>
      </view>

      <view class="footer">
        <view class="button" @tap="afterSalePopup?.close?.()">取消</view>
        <view class="button primary" @tap="onSubmitAfterSale">提交</view>
      </view>
    </view>
  </uni-popup>
</template>

<style lang="scss">
page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background-color: #fff9e6;
}

.navbar {
  width: 750rpx;
  color: #000;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9;
  background-color: transparent;

  .wrap {
    position: relative;

    .title {
      height: 44px;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 32rpx;
      font-weight: bold;
      color: transparent;
    }

    .back {
      position: absolute;
      left: 0;
      height: 44px;
      width: 44px;
      font-size: 44rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }
  }
}

.viewport {
  background-color: #fff9e6;
}

/* 🔥 地图样式 */
.map-wrapper {
  width: 100%;
  padding: 20rpx 30rpx;
  margin-top: 10rpx;
}

.map-title {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
  margin-bottom: 12rpx;
}

.map {
  width: 100%;
  height: 320rpx;
  border-radius: 16rpx;
}

.overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1;
  padding-bottom: 30rpx;
  color: #fff;
  background: linear-gradient(135deg, #ffc107, #ff9800);
  background-size: cover;

  .status {
    font-size: 36rpx;
    font-weight: bold;
  }

  .tips {
    margin: 30rpx 0;
    display: flex;
    font-size: 14px;
    align-items: center;

    .money {
      margin-right: 30rpx;
      font-weight: bold;
    }
  }

  .button-group {
    margin-top: 30rpx;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .button {
    width: 260rpx;
    height: 64rpx;
    margin: 0 10rpx;
    text-align: center;
    line-height: 64rpx;
    font-size: 28rpx;
    color: #ff9800;
    border-radius: 68rpx;
    background-color: #fff;
    font-weight: 500;
  }
}

.shipment {
  line-height: 1.4;
  padding: 0 20rpx;
  margin: 20rpx 20rpx 0;
  border-radius: 16rpx;
  background-color: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .locate,
  .item {
    min-height: 120rpx;
    padding: 30rpx 30rpx 25rpx 75rpx;
    background-size: 50rpx;
    background-repeat: no-repeat;
    background-position: 6rpx center;
  }

  .locate {
    background-image: url(https://pcapi-xiaotuxian-front-devtest.itheima.net/miniapp/images/locate.png);

    .user {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }

    .address {
      font-size: 24rpx;
      color: #666;
    }
  }

  .item {
    background-image: url(https://pcapi-xiaotuxian-front-devtest.itheima.net/miniapp/images/car.png);
    border-bottom: 1rpx solid #ffeccd;
    position: relative;

    .message {
      font-size: 26rpx;
      color: #333;
    }

    .date {
      font-size: 24rpx;
      color: #888;
    }
  }
}

.goods {
  margin: 20rpx 20rpx 0;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background-color: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .item {
    padding: 30rpx 0;
    border-bottom: 1rpx solid #ffeccd;

    .navigator {
      display: flex;
      margin: 20rpx 0;
    }

    .cover {
      width: 170rpx;
      height: 170rpx;
      border-radius: 12rpx;
      margin-right: 20rpx;
    }

    .meta {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      position: relative;
    }

    .name {
      height: 80rpx;
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }

    .type {
      line-height: 1.8;
      padding: 0 15rpx;
      margin-top: 6rpx;
      font-size: 24rpx;
      align-self: flex-start;
      border-radius: 6rpx;
      color: #ff9800;
      background-color: #fff1d9;
    }

    .price {
      display: flex;
      margin-top: 6rpx;
      font-size: 24rpx;
    }

    .symbol {
      font-size: 20rpx;
    }

    .actual {
      margin-left: 10rpx;
      color: #ff5722;
      font-weight: bold;
    }

    .quantity {
      position: absolute;
      bottom: 0;
      right: 0;
      font-size: 24rpx;
      color: #666;
    }

    .action {
      display: flex;
      flex-direction: row-reverse;
      justify-content: flex-start;
      padding: 30rpx 0 0;

      .button {
        width: 200rpx;
        height: 60rpx;
        text-align: center;
        justify-content: center;
        line-height: 60rpx;
        margin-left: 20rpx;
        border-radius: 60rpx;
        border: 1rpx solid #ccc;
        font-size: 26rpx;
        color: #444;
      }

      .primary {
        color: #ff9800;
        border-color: #ff9800;
      }
    }
  }

  .total {
    line-height: 1;
    font-size: 26rpx;
    padding: 20rpx 0;
    color: #666;

    .row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10rpx 0;
    }

    .symbol::before {
      content: '¥';
      font-size: 80%;
      margin-right: 3rpx;
    }

    .primary {
      color: #ff5722;
      font-size: 36rpx;
      font-weight: bold;
    }
  }
}

.detail {
  line-height: 1;
  padding: 30rpx 20rpx 0;
  margin: 20rpx 20rpx 0;
  font-size: 26rpx;
  color: #666;
  border-radius: 16rpx;
  background-color: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .title {
    font-size: 30rpx;
    color: #333;
    font-weight: bold;
  }

  .row {
    padding: 20rpx 0;

    .item {
      padding: 10rpx 0;
      display: flex;
      align-items: center;
    }

    .copy {
      border-radius: 20rpx;
      font-size: 20rpx;
      border: 1px solid #ffc107;
      color: #ff9800;
      padding: 5rpx 10rpx;
      margin-left: 10rpx;
    }
  }
}

.toolbar-height {
  height: 100rpx;
  box-sizing: content-box;
}

.toolbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(var(--window-bottom));
  z-index: 1;
  height: 100rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  flex-direction: row-reverse;
  border-top: 1rpx solid #ffeccd;
  background-color: #fffdf5;
  box-sizing: content-box;

  .button {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 200rpx;
    height: 72rpx;
    margin-left: 15rpx;
    font-size: 26rpx;
    border-radius: 72rpx;
    border: 1rpx solid #ccc;
    color: #444;
  }

  .delete {
    order: 4;
    color: #ff5722;
    border-color: #ff5722;
  }

  .button {
    order: 3;
  }

  .secondary {
    order: 2;
    color: #ff9800;
    border-color: #ff9800;
  }

  .primary {
    order: 1;
    color: #fff;
    background-color: #ff9800;
    border: none;
  }
}

.popup-root {
  padding: 30rpx 30rpx 0;
  border-radius: 10rpx 10rpx 0 0;
  overflow: hidden;
  background-color: #fff;

  .title {
    font-size: 30rpx;
    text-align: center;
    margin-bottom: 30rpx;
    font-weight: bold;
    color: #333;
  }

  .description {
    font-size: 28rpx;
    padding: 0 20rpx;

    .tips {
      color: #444;
      margin-bottom: 12rpx;
    }

    .cell {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15rpx 0;
      color: #666;
    }

    .icon::before {
      content: '\e6cd';
      font-family: 'erabbit' !important;
      font-size: 38rpx;
      color: #999;
    }

    .icon.checked::before {
      content: '\e6cc';
      font-size: 38rpx;
      color: #ff9800;
    }
  }

  .footer {
    display: flex;
    justify-content: space-between;
    padding: 30rpx 0 40rpx;
    font-size: 28rpx;
    color: #444;

    .button {
      flex: 1;
      height: 72rpx;
      text-align: center;
      line-height: 72rpx;
      margin: 0 20rpx;
      color: #444;
      border-radius: 72rpx;
      border: 1rpx solid #ccc;
    }

    .primary {
      color: #fff;
      background-color: #ff9800;
      border: none;
    }
  }
}

// 支付方式弹窗样式
.pay-popup-root {
  padding: 30rpx;
  border-radius: 20rpx 20rpx 0 0;
  background: #fff;

  .title {
    font-size: 32rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }

  .pay-list {
    margin-bottom: 40rpx;
  }

  .pay-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    .icon {
      width: 48rpx;
      height: 48rpx;
      margin-right: 20rpx;
    }

    .name {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }

    .arrow {
      font-size: 28rpx;
      color: #999;
    }
  }

  .close {
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    border-radius: 12rpx;
    background: #f5f5f5;
  }
}

// 骑手送达凭证样式
// 多商家送达凭证 - 多图网格样式
.evidence {
  margin: 30rpx 0 10rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #f0f0f0;

  .title {
    font-size: 28rpx;
    font-weight: bold;
    color: #16a34a;
    margin-bottom: 16rpx;
  }

  .evidence-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    /* 图片间距 */
  }

  .evidence-img {
    width: 220rpx !important;
    height: 220rpx !important;
    border-radius: 12rpx;
    background: #f9fafb;
    object-fit: cover;
  }
}
</style>
