<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { http } from '@/utils/http'
import type { GoodsItem, GoodsSkuData } from '@/types/global'
import { getMemberOrderPreNowAPI } from '@/services/order' // 加这行
import type {
  SkuPopupEvent,
  SkuPopupInstance,
  SkuPopupLocaldata,
} from '@/components/vk-data-goods-sku-popup/vk-data-goods-sku-popup'

import AddressPanel from './components/AddressPanel.vue'
import ServicePanel from './components/ServicePanel.vue'

const query = defineProps<{
  id: string
}>()

const { safeAreaInsets } = uni.getSystemInfoSync()
const isLoading = ref(true)
const goods = ref<any>(null)
const isGoodsOff = ref(false) // 商品是否下架 status=0
const isShowSku = ref(false)
const localdata = ref<SkuPopupLocaldata>({
  _id: 0,
  name: '',
  goods_thumb: '',
  spec_list: [],
  sku_list: [],
})

enum SkuMode {
  Both = 1,
  Cart = 2,
  Buy = 3,
}
const mode = ref(SkuMode.Cart)

const popup = ref<any>()
const popupName = ref<'address' | 'service'>()

const openSkuPopup = (val: SkuMode) => {
  if (isGoodsOff.value) {
    uni.showToast({ title: '该商品已下架', icon: 'none' })
    return
  }
  mode.value = val
  isShowSku.value = true
}

const openPopup = (name: typeof popupName.value) => {
  popupName.value = name
  popup.value?.open()
}

// 新增：当前选中价格和规格文字
const currentPrice = ref(0)
const selectArrText = ref('请选择规格')

// 新增：SKU 选中变化事件
const onSkuChange = (sku: any) => {
  if (sku) {
    // 选全了
    currentPrice.value = sku.price / 100
    selectArrText.value = '已选：' + sku.sku_name_arr.join(' ')
  } else {
    // 还没选全
    selectArrText.value = '请选择规格'
  }
}

// 【核心】获取商品数据

const getGoodsData = async () => {
  isLoading.value = true
  try {
    // 1. 获取商品基础信息
    const goodsRes = await http<GoodsItem[]>({
      method: 'GET',
      url: '/goods/list',
      data: { goodsId: query.id }
    })

    if (!goodsRes.data?.length) return
    const raw = goodsRes.data[0]
    goods.value = raw
    currentPrice.value = raw.price
    isGoodsOff.value = raw.status === 0

    // 2. 获取 SKU 数据
    const skuRes = await http<GoodsSkuData>({
      method: 'GET',
      url: `/goods/sku/${query.id}`
    })

    // 解构数据，并提供空数组默认值
    const specList = skuRes.data?.specList || []
    const skuList = skuRes.data?.skuList || []

    // 3. 构建本地数据
    const localSkuList = skuList.map(sku => ({
      _id: sku.id,
      goods_id: sku.goodsId,
      goods_name: raw.goodsName,
      image: sku.image || raw.img,
      price: sku.price * 100, // 组件要求单位为分
      discountPrice: (sku.discountPrice || 0) * 100,
      sku_name_arr: sku.skuNameArr ? sku.skuNameArr.split(',') : [],
      stock: sku.stock,
    }))
    let finalSpecList = specList.map(spec => ({
      name: spec.name,
      list: spec.list.map(item => ({ name: item.name }))
    }))
    if (localSkuList.length === 0 || finalSpecList.length === 0) {
      // 1. 确保 SKU 列表里有一个默认项
      if (localSkuList.length === 0) {
        localSkuList.push({
          _id: -1,
          goods_id: raw.goodsId,
          goods_name: raw.goodsName,
          image: raw.img,
          price: raw.price * 100,
          discountPrice: raw.discountPrice * 100,
          sku_name_arr: ['默认'], // 这里要和下方的 spec 对应
          stock: raw.stock || 0
        })
      }

      // 2. 重点：如果 specList 为空，手动造一个“规格”展示出来
      if (finalSpecList.length === 0) {
        finalSpecList = [{
          name: '规格',
          list: [{ name: '默认' }]
        }]
      }
    }

    localdata.value = {
      _id: raw.goodsId,
      name: raw.goodsName,
      goods_thumb: raw.img,
      spec_list: finalSpecList, // 使用处理后的 specList
      sku_list: localSkuList
    }

    // 如果没有规格，直接更新显示文字
    if (specList.length === 0) {
      selectArrText.value = '默认规格'
    }

  } catch (err) {
    console.error('获取商品详情失败', err)
  } finally {
    isLoading.value = false
  }
}
onLoad(() => {
  getGoodsData()
})

const onAddCart = async (ev: SkuPopupEvent) => {
  try {
    // 组装完整的购物车数据（包含所有商品信息）
    const cartData = {
      skuId: ev._id,
      count: ev.buy_num || 1,
      goodsId: goods.value.goodsId,
      name: goods.value.goodsName,
      picture: ev.image || goods.value.img,
      price: ev.price / 100,
      nowPrice: ev.discountPrice / 100,
      stock: ev.stock,
      attrsText: ev.sku_name_arr.join(' ')
    }

    // 直接传完整对象给后端
    await http({
      method: 'POST',
      url: '/user/cart',
      data: cartData
    })

    uni.showToast({ title: '加入购物车成功' })
    isShowSku.value = false
  } catch (err) {
    uni.showToast({ title: '加入失败', icon: 'none' })
    console.error(err)
  }
}
const onBuyNow = async (ev: SkuPopupEvent) => {
  try {
    uni.navigateTo({
      url: `/pagesOrder/create?skuId=${ev._id}&goodsId=${goods.value.goodsId}&count=${ev.buy_num}`,
    })
  } catch (err) {
    uni.showToast({ title: '下单失败', icon: 'none' })
  }
}

const previewImage = (current: string) => {
  uni.previewImage({
    urls: [goods.value.img],
    current
  })
}
</script>

<template>
  <view class="page-container">
    <vk-data-goods-sku-popup v-model="isShowSku" :localdata="localdata" :mode="mode" add-cart-background-color="#FFA868"
      buy-now-background-color="#FF5722" ref="skuPopupRef" price-key="price" stock-key="stock" :actived-style="{
        color: '#FF5722',
        borderColor: '#FF5722',
        backgroundColor: '#FFF1ED',
      }" @add-cart="onAddCart" @buy-now="onBuyNow" @change="onSkuChange" />

    <scroll-view scroll-y class="viewport" v-if="!isLoading">
      <view class="preview">
        <image class="image" :src="goods.img" mode="aspectFill" @tap="previewImage(goods.img)" />
      </view>

      <view class="goods-info">
        <view class="price">
          <text class="symbol">¥</text>
          <text class="number">{{ currentPrice }}</text>
        </view>
        <view class="name">{{ goods.goodsName }}</view>
        <view class="desc">{{ goods.desc }}</view>
      </view>

      <view class="action-panel">
        <view class="item">
          <text class="label">商家</text>
          <text class="text">{{ goods.merchantId }} 号店</text>
        </view>

        <view class="item arrow" @tap="openSkuPopup(SkuMode.Both)">
          <text class="label">选择</text>
          <text class="text">{{ selectArrText }}</text>
        </view>
        <!-- <view class="item arrow" @tap="openPopup('address')">
          <text class="label">送至</text>
          <text class="text">请选择收货地址</text>
        </view> -->
        <view class="item arrow" @tap="openPopup('service')">
          <text class="label">服务</text>
          <text class="text">快速跑腿 准时送达 无忧退款</text>
        </view>
      </view>

      <view class="detail-panel">
        <view class="title">商品详情</view>
        <view class="content">
          <image class="detail-img" :src="goods.img" mode="widthFix" />
          <view class="text">{{ goods.desc || '暂无商品介绍' }}</view>
        </view>
      </view>
    </scroll-view>

    <!-- 用户操作 -->
    <view v-if="goods" class="toolbar" :style="{ paddingBottom: safeAreaInsets?.bottom + 'px' }">
      <view class="icons">
        <!-- <button class="icons-button"><text class="icon-heart"></text>收藏</button> -->
        <!-- #ifdef MP-WEIXIN -->
        <button class="icons-button" open-type="contact">
          <text class="icon-handset"></text>客服
        </button>
        <!-- #endif -->
        <navigator class="icons-button" url="/pages/cart/cart2" open-type="navigate">
          <text class="icon-cart"></text>购物车
        </navigator>
      </view>
      <view class="buttons">
        <view @tap="openSkuPopup(SkuMode.Cart)" class="addcart" :class="{ disabled: isGoodsOff }">
          加入购物车
        </view>
        <view @tap="openSkuPopup(SkuMode.Buy)" class="payment" :class="{ disabled: isGoodsOff }">
          立即购买
        </view>
      </view>
    </view>

    <view class="loading" v-else>加载中...</view>

    <uni-popup ref="popup" type="bottom" background-color="#fff">
      <AddressPanel v-if="popupName === 'address'" @close="popup?.close()" />
      <ServicePanel v-if="popupName === 'service'" @close="popup?.close()" />
    </uni-popup>
  </view>
</template>

<style lang="scss" scoped>
/* 核心布局 */
.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
}

.viewport {
  flex: 1;
  overflow: hidden;
}

/* 底部工具栏 */
.toolbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background-color: #fff;
  height: 100rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20rpx;
  border-top: 1rpx solid #eaeaea;
  box-sizing: content-box;

  .icons {
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: space-around;
    padding-right: 10rpx;

    .icons-button,
    .navigator-wrap {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      line-height: 1.4;
      padding: 0;
      margin: 0;
      font-size: 20rpx;
      color: #333;
      background-color: #fff;

      &::after {
        border: none;
      }

      text {
        display: block;
        font-size: 38rpx;
        margin-bottom: 2rpx;
      }
    }
  }

  .buttons {
    display: flex;
    align-items: center;

    view {
      width: 200rpx;
      height: 72rpx;
      line-height: 72rpx;
      text-align: center;
      font-size: 26rpx;
      color: #fff;
      border-radius: 72rpx;
    }

    .addcart {
      background-color: #ffa868;
    }

    .payment {
      background-color: #ff5722;
      margin-left: 15rpx;
    }

    .disabled {
      background-color: #ccc !important;
      pointer-events: none;
    }
  }
}

/* 操作面板样式 */
.action-panel {
  margin-top: 20rpx;
  background: #fff;
  padding: 0 30rpx;

  .item {
    height: 90rpx;
    display: flex;
    align-items: center;
    font-size: 26rpx;
    border-bottom: 1rpx solid #f5f5f5;
    position: relative;

    &.arrow::after {
      content: '>';
      position: absolute;
      right: 0;
      color: #ccc;
    }

    .label {
      width: 80rpx;
      color: #999;
    }

    .text {
      flex: 1;
      color: #333;
    }
  }
}

/* 基础样式 */
.preview {
  width: 100%;
  height: 500rpx;

  .image {
    width: 100%;
    height: 100%;
  }
}

.goods-info {
  background: #fff;
  padding: 30rpx;

  .price {
    color: #ff5722;
    font-size: 42rpx;
    font-weight: bold;
  }

  .name {
    font-size: 34rpx;
    font-weight: bold;
    margin-top: 10rpx;
  }

  .desc {
    font-size: 26rpx;
    color: #666;
    margin-top: 10rpx;
  }
}

.detail-panel {
  margin-top: 20rpx;
  background: #fff;
  padding: 30rpx;

  .title {
    font-weight: bold;
    margin-bottom: 20rpx;
    border-left: 8rpx solid #ff5722;
    padding-left: 20rpx;
  }

  .detail-img {
    width: 100%;
  }
}
</style>
