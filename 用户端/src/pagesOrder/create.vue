<script setup lang="ts">
import {
  getMemberOrderPreAPI,
  getMemberOrderPreNowAPI,
  getMemberOrderRepurchaseByIdAPI,
  postMemberOrderAPI,
} from '@/services/order'
import { useAddressStore } from '@/stores/modules/address'
import type { OrderPreResult } from '@/types/order'
import { onLoad } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'

const { safeAreaInsets } = uni.getSystemInfoSync()
const buyerMessage = ref('')

// 优惠券
const couponList = ref<any>([])
const selectedCoupon = ref({ id: 0, name: '不使用优惠', value: 0, min: 0 })
const showCouponPopup = ref(false)

// 用户积分 & 使用积分
const userPoint = ref(100)
const usePoint = ref(0)
const pointToYuan = 100

//订单价格计算
const orderTotalPrice = computed(() => {
  return Number(orderPre.value?.summary.totalPrice || 0) // 改为 totalPrice
})
const postFee = computed(() => {
  return Number(orderPre.value?.summary.postFee || 0)
})
// 订单应付 = 商品原价 + 运费（正确！）
const orderAllPrice = computed(() => {
  return orderTotalPrice.value + postFee.value
})
// 优惠券抵扣（修复版）
const couponDiscount = computed(() => {
  const total = orderAllPrice.value
  const coupon = selectedCoupon.value
  // 订单金额 ≥ 满减门槛，才返回优惠金额；否则返回 0
  return total >= coupon.min ? coupon.value : 0
})

// 积分抵扣
const pointDiscount = computed(() => {
  return usePoint.value / pointToYuan
})

// 最终实付
const finalPrice = computed(() => {
  let real = orderAllPrice.value - couponDiscount.value - pointDiscount.value
  return real < 0 ? '0.00' : real.toFixed(2)
})

// 积分限制
const handlePointInput = () => {
  const maxPoint = Math.min(100, Math.floor((orderAllPrice.value - couponDiscount.value) * pointToYuan))
  if (usePoint.value > maxPoint) usePoint.value = maxPoint
  if (usePoint.value < 0) usePoint.value = 0
}

// 选择优惠券
const handleSelectCoupon = (c: any) => {
  if (orderAllPrice.value >= c.min || c.id === 0) {
    selectedCoupon.value = c
    usePoint.value = 0
    showCouponPopup.value = false
  }
}

// 基础配置
const isErrandOrder = ref(false)
const updateErrandOrder = (e: { detail: { value: boolean } }) => {
  isErrandOrder.value = e.detail.value
}
const generateDeliveryTime = () => {
  const list = [
    { type: 0, text: '时间不限，尽快完成' }
  ]
  const now = new Date()
  let currentHour = now.getHours() + 1

  for (let i = 1; i <= 5; i++) {
    const startHour = currentHour % 24
    const endHour = (startHour + 1) % 24
    const text = `${String(startHour).padStart(2, '0')}:00-${String(endHour).padStart(2, '0')}:00`
    list.push({
      type: i,
      text: text
    })
    currentHour++
  }
  return list
}
const deliveryList = ref(generateDeliveryTime())
const activeIndex = ref(0)
const activeDelivery = computed(() => deliveryList.value[activeIndex.value])

const onChangeDelivery = (ev: any) => {
  activeIndex.value = ev.detail.value
}

const query = defineProps<{
  skuId?: number
  count?: number
  goodsId?: number
  orderId?: string
}>()

const orderPre = ref<OrderPreResult>()

//获取订单信息
const getMemberOrderPreData = async () => {
  try {
    let res
    if (query.orderId) {
      res = await getMemberOrderRepurchaseByIdAPI(query.orderId)
    } else if (query.skuId && query.goodsId && query.count) {
      res = await getMemberOrderPreNowAPI({
        skuId: query.skuId ?? -1,
        goodsId: query.goodsId,
        count: query.count,
      })
    } else {
      res = await getMemberOrderPreAPI()
    }
    orderPre.value = res.data

    // 自动获取店铺优惠券
    await getMerchantCoupon()
  } catch (e) {
    console.error("获取订单数据失败", e)
  }
}
// 获取店铺优惠券
const getMerchantCoupon = async () => {
  try {
    const firstGoods = orderPre.value?.goods?.[0]
    if (!firstGoods) return

    const goodsId = parseInt(firstGoods.id)

    const res: any = await uni.request({
      url: `/member/order/coupon?goodsId=${goodsId}`,
    })

    // 👇 后端已经直接返回 min 和 value，不需要再映射了
    const rawList: any[] = (res?.data?.data) || []

    // 加上默认的“不使用优惠”选项
    couponList.value = [
      ...rawList,
      { id: 0, name: '不使用优惠', value: 0, min: 0 }
    ]
    selectedCoupon.value = couponList.value.at(-1)
  } catch (e) {
    console.log('获取优惠券失败，使用默认')
    couponList.value = [{ id: 0, name: '不使用优惠', value: 0, min: 0 }]
  }
}
onLoad(() => {
  getMemberOrderPreData()
})

const addressStore = useAddressStore()
const selecteAddress = computed(() => {
  return addressStore.selectedAddress || orderPre.value?.userAddresses.find((v) => v.isDefault)
})

// ==================== 取件码识别 ====================
const ocrTakeCode = async () => {
  const res = await uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera', 'album']
  })
  const imgPath = res.tempFilePaths[0]

  uni.uploadFile({
    url: '/ocr/takeCode',
    filePath: imgPath,
    name: 'file',
    timeout: 15000,
    success: (uploadRes) => {
      try {
        const data = JSON.parse(uploadRes.data)
        if ((data.code === 200 || data.code === "200") && data.data) {
          const codeStr = data.data.trim()
          const code = codeStr.slice(0, 6)
          if (code) {
            if (buyerMessage.value) {
              buyerMessage.value += `\n取件码：${code}`
            } else {
              buyerMessage.value = `取件码：${code}`
            }
            uni.showToast({ title: '取件码识别成功' })
          } else {
            uni.showToast({ icon: 'none', title: '未识别到取件码' })
          }
        } else {
          uni.showToast({ icon: 'none', title: '未识别到取件码' })
        }
      } catch (err) {
        uni.showToast({ icon: 'none', title: '数据解析失败' })
      }
    },
    fail: () => {
      uni.showToast({ icon: 'none', title: '图片上传失败' })
    }
  })
}

// ==================== 提交订单 ====================
const onOrderSubmit = async () => {
  if (!selecteAddress.value?.id) {
    return uni.showToast({ icon: 'none', title: '请选择收货地址' })
  }

  try {
    const isFromCart = !query.skuId && !query.orderId
    const res = await postMemberOrderAPI({
      addressId: selecteAddress.value.id,
      buyerMessage: buyerMessage.value,
      deliveryTimeType: activeDelivery.value?.type || 0,
      goods: orderPre.value!.goods.map(v => ({
        count: v.count,
        goodsId: v.id,
        skuId: v.skuId
      })),
      fromCart: isFromCart,
      orderType: isErrandOrder.value ? 2 : 1,
      discountPrice: couponDiscount.value,
      realPayPrice: Number(finalPrice.value),
      payChannel: 1,
      payType: 1,
      usePoint: 0,
      pointMoney: 0
    })
    console.log('提交订单参数', res)
    const orderId = res.data.id
    uni.redirectTo({
      url: `/pagesOrder/detail?id=${orderId}`
    })
  } catch (error) {
    console.error("提交订单失败", error)
    uni.showToast({ icon: "none", title: "提交订单失败" })
  }
}
</script>

<template>
  <scroll-view enable-back-to-top scroll-y class="viewport">
    <navigator v-if="selecteAddress" class="shipment" hover-class="none" url="/pagesMember/address?from=order">
      <view class="user"> {{ selecteAddress.receiver }} {{ selecteAddress.contact }} </view>
      <view class="address"> {{ selecteAddress.fullLocation }} {{ selecteAddress.address }} </view>
      <text class="icon icon-right"></text>
    </navigator>
    <navigator v-else class="shipment" hover-class="none" url="/pagesMember/address/address?from=order">
      <view class="address"> 请选择收货地址 </view>
      <text class="icon icon-right"></text>
    </navigator>

    <view class="goods">
      <navigator v-for="item in orderPre?.goods" :key="item.skuId" :url="`/pages/goods/goods?id=${item.id}`"
        class="item" hover-class="none">
        <image class="picture" :src="item.picture" />
        <view class="meta">
          <view class="name ellipsis"> {{ item.name }} </view>
          <view class="attrs">{{ item.attrsText }}</view>
          <view class="prices">
            <view class="pay-price symbol">{{ item.payPrice }}</view>
            <view class="price symbol">{{ item.price }}</view>
          </view>
          <view class="count">x{{ item.count }}</view>
        </view>
      </navigator>
    </view>

    <view class="related coupon-section">
      <view class="item" @tap="showCouponPopup = true">
        <text class="text">优惠券</text>
        <view class="right-arrow">
          <text class="value">{{ selectedCoupon.name }}</text>
          <text class="iconfont icon-right"></text>
        </view>
      </view>
    </view>

    <view class="related coupon-section">
      <view class="item">
        <text class="text">积分抵扣</text>
        <view class="point-row">
          <input class="point-input" v-model.number="usePoint" type="number" placeholder="输入抵扣积分"
            @input="handlePointInput" />
          <text class="point-desc">/100积分</text>
        </view>
      </view>
    </view>

    <view v-if="showCouponPopup" class="coupon-mask" @tap="showCouponPopup = false">
      <view class="coupon-panel" @tap.stop>
        <view class="panel-title">选择优惠券</view>
        <view class="coupon-list">
          <view v-for="c in couponList" :key="c.id" class="coupon-item" :class="{
            active: selectedCoupon.id === c.id,
            disabled: orderAllPrice < c.min && c.id !== 0
          }" @tap="handleSelectCoupon(c)">
            <view class="coupon-name">{{ c.name }}</view>
            <view v-if="c.min > 0" class="coupon-limit">满{{ c.min }}元可用</view>
          </view>
        </view>
      </view>
    </view>

    <view class="related">
      <view class="item">
        <text class="text">配送时间</text>
        <picker :range="deliveryList" range-key="text" @change="onChangeDelivery">
          <view class="icon-fonts picker">{{ activeDelivery.text }}</view>
        </picker>
      </view>

      <view class="item">
        <text class="text">是否跑腿服务</text>
        <switch :checked="isErrandOrder" active-color="#ff9f00" @change="updateErrandOrder" />
      </view>

      <view class="item">
        <text class="text">订单备注</text>
        <view style="display: flex;align-items: center;justify-content: flex-end;flex: 1;gap:12rpx;">
          <input class="input" placeholder="填写跑腿需求、物品信息，拍照自动识别取件码" v-model="buyerMessage" />
          <button v-if="isErrandOrder" size="mini"
            style="font-size:22rpx;padding:6rpx 12rpx;background:#ff9f00;color:#fff;border-radius:8rpx;border:none;"
            @tap="ocrTakeCode">
            拍照识码
          </button>
        </view>
      </view>
    </view>

    <view class="settlement">
      <view class="item">
        <text class="text">商品总价:</text>
        <text class="number symbol">{{ orderTotalPrice.toFixed(2) }}</text>
      </view>
      <view class="item">
        <text class="text">运费:</text>
        <text class="number symbol">{{ postFee.toFixed(2) }}</text>
      </view>
      <view class="item" style="color:#ff5722">
        <text class="text">优惠券抵扣:</text>
        <text class="number symbol">-{{ couponDiscount.toFixed(2) }}</text>
      </view>
      <view class="item" style="color:#ff9f00">
        <text class="text">积分抵扣:</text>
        <text class="number symbol">-{{ pointDiscount.toFixed(2) }}</text>
      </view>
    </view>
  </scroll-view>

  <view class="toolbar" :style="{ paddingBottom: safeAreaInsets?.bottom + 'px' }">
    <view class="total-pay symbol">
      <text class="number">{{ finalPrice }}</text>
    </view>
    <view class="button" :class="{ disabled: !selecteAddress?.id }" @tap="onOrderSubmit">
      提交订单
    </view>
  </view>
</template>

<style lang="scss">
page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background-color: #fff9e6;
}

.symbol::before {
  content: '¥';
  font-size: 80%;
  margin-right: 5rpx;
}

.shipment {
  margin: 20rpx;
  padding: 30rpx 30rpx 30rpx 84rpx;
  font-size: 26rpx;
  border-radius: 16rpx;
  background: url(https://pcapi-xiaotuxian-front-devtest.itheima.net/miniapp/images/locate.png) 20rpx center / 50rpx no-repeat #fffdf5;
  position: relative;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .icon {
    font-size: 36rpx;
    color: #ff9f00;
    position: absolute;
    top: 50%;
    right: 20rpx;
    transform: translateY(-50%);
  }

  .user {
    color: #333;
    margin-bottom: 5rpx;
  }

  .address {
    color: #666;
  }
}

.goods {
  margin: 20rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background-color: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .item {
    display: flex;
    padding: 30rpx 0;
    border-top: 1rpx solid #ffeccd;

    &:first-child {
      border-top: none;
    }

    .picture {
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
    }

    .attrs {
      line-height: 1.8;
      padding: 0 15rpx;
      margin-top: 6rpx;
      font-size: 24rpx;
      border-radius: 6rpx;
      color: #ff9f00;
      background-color: #fff1d9;
    }

    .prices {
      display: flex;
      align-items: baseline;
      margin-top: 6rpx;
      font-size: 28rpx;

      .pay-price {
        margin-right: 10rpx;
        color: #ff5000;
      }

      .price {
        font-size: 24rpx;
        color: #999;
        text-decoration: line-through;
      }
    }

    .count {
      position: absolute;
      bottom: 0;
      right: 0;
      font-size: 26rpx;
      color: #666;
    }
  }
}

.related {
  margin: 20rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background-color: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 80rpx;
    font-size: 26rpx;
    color: #333;
  }

  .input {
    flex: 1;
    text-align: right;
    padding: 20rpx 0;
    padding-right: 20rpx;
    font-size: 26rpx;
    color: #999;
  }

  .text {
    width: 125rpx;
  }

  .picker {
    color: #ff9f00;
  }
}

.settlement {
  margin: 20rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background-color: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);

  .item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 80rpx;
    font-size: 26rpx;
    color: #333;
  }
}

.toolbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  background-color: #fffdf5;
  height: 100rpx;
  padding: 0 20rpx;
  border-top: 1rpx solid #ffeccd;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .total-pay {
    font-size: 40rpx;
    color: #ff5000;
  }

  .button {
    width: 220rpx;
    text-align: center;
    line-height: 72rpx;
    font-size: 26rpx;
    color: #fff;
    border-radius: 72rpx;
    background-color: #ff9f00;
  }

  .disabled {
    opacity: 0.6;
    background-color: #ffc966;
  }
}

.coupon-section {
  margin: 20rpx;
  padding: 0 30rpx;
  border-radius: 16rpx;
  background: #fffdf5;
  box-shadow: 0 2rpx 12rpx rgba(255, 215, 0, 0.1);
}

.right-arrow {
  display: flex;
  align-items: center;
  color: #ff9f00;
  font-size: 26rpx;
}

.value {
  margin-right: 10rpx;
}

.point-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.point-input {
  width: 160rpx;
  text-align: right;
  font-size: 26rpx;
  color: #ff9f00;
}

.point-desc {
  font-size: 24rpx;
  color: #999;
  margin-left: 10rpx;
}

.coupon-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.coupon-panel {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
}

.panel-title {
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30rpx;
  color: #333;
}

.coupon-item {
  padding: 30rpx;
  border-radius: 16rpx;
  background: #fffdf5;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-item.active {
  background: #fff1d9;
  border: 2rpx solid #ff9f00;
}

.coupon-item.disabled {
  opacity: 0.45;
  pointer-events: none;
}

.coupon-name {
  font-size: 28rpx;
  color: #333;
}

.coupon-limit {
  font-size: 24rpx;
  color: #ff5722;
}
</style>
