<script setup lang="ts">
import type { InputNumberBoxEvent } from '@/components/vk-data-input-number-box/vk-data-input-number-box'

import {
  deleteMemberCartAPI,
  getMemberCartAPI,
  putMemberCartBySkuIdAPI,
  putMemberCartSelectedAPI,
} from '@/services/cart'
import { useMemberStore } from '@/stores'
import type { CartItem } from '@/types/cart'
import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'

// 是否适配底部安全区域
defineProps<{
  safeAreaInsetBottom?: boolean
}>()

// 获取屏幕边界到安全区域距离
const { safeAreaInsets } = uni.getSystemInfoSync()

// 获取会员Store
const memberStore = useMemberStore()

// 获取购物车数据
const cartList = ref<CartItem[]>([])
// 优化购物车空列表状态，默认展示列表
const showCartList = ref(true)
const getMemberCartData = async () => {
  const res = await getMemberCartAPI()
  cartList.value = res.data || []
  showCartList.value = cartList.value.length > 0
}

// 初始化调用: 页面显示触发
onShow(() => {
  if (memberStore.profile) {
    getMemberCartData()
  }
})

// 点击删除按钮
// 点击删除按钮
const onDeleteCart = (id: number) => {
  uni.showModal({
    content: '是否删除',
    confirmColor: '#27BA9B',
    success: async (res) => {
      if (res.confirm) {
        // 传购物车主键 id，不是 skuId！
        await deleteMemberCartAPI({ ids: [id] })
        getMemberCartData()
      }
    },
  })
}

// 修改商品数量
const onChangeCount = (ev: InputNumberBoxEvent, goodsId: number) => {
  const skuId = ev.index as unknown as number
  // 直接使用传入的 goodsId，不再从列表查找，避免重复 skuId 错乱
  putMemberCartBySkuIdAPI(skuId, {
    count: ev.value,
    goodsId: goodsId,
  })
}

// 修改选中状态-单品修改
const onChangeSelected = (item: CartItem) => {
  // 前端数据更新-是否选中取反
  item.selected = !item.selected
  // 后端数据更新
  putMemberCartBySkuIdAPI(item.skuId, { selected: item.selected, goodsId: item.goodsId })
}

// 计算全选状态
const isSelectedAll = computed(() => {
  return cartList.value.length && cartList.value.every((v) => v.selected)
})

// 修改选中状态-全选修改
const onChangeSelectedAll = () => {
  // 全选状态取反
  const _isSelectedAll = !isSelectedAll.value
  // 前端数据更新
  cartList.value.forEach((item) => {
    item.selected = _isSelectedAll
  })
  // 后端数据更新
  putMemberCartSelectedAPI({ selected: _isSelectedAll })
}

// 计算选中单品列表
const selectedCartList = computed(() => {
  return cartList.value.filter((v) => v.selected)
})

// 计算选中总件数
const selectedCartListCount = computed(() => {
  return selectedCartList.value.reduce((sum, item) => sum + item.count, 0)
})

// 计算选中总金额
const selectedCartListMoney = computed(() => {
  return selectedCartList.value
    .reduce((sum, item) => sum + item.count * item.nowPrice, 0)
    .toFixed(2)
})

// 结算按钮
const gotoPayment = () => {
  if (selectedCartListCount.value === 0) {
    return uni.showToast({
      icon: 'none',
      title: '请选择商品',
    })
  }
  // 跳转到结算页
  uni.navigateTo({ url: '/pagesOrder/create' })
}
const gotoIndex = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<template>
  <scroll-view enable-back-to-top scroll-y class="scroll-view">
    <!-- 已登录: 显示购物车 -->
    <template v-if="memberStore.profile">
      <!-- 购物车列表 -->
      <view class="cart-list" v-if="showCartList">
        <!-- 优惠提示 -->
        <view class="tips">
          <text class="label">满减</text>
          <text class="desc">满1件, 即可享受9折优惠</text>
        </view>
        <!-- 滑动操作分区 -->
        <uni-swipe-action>
          <!-- 滑动操作项 -->
          <uni-swipe-action-item v-for="item in cartList" :key="item.skuId" class="cart-swipe">
            <!-- 商品信息 -->
            <view class="goods">
              <!-- 选中状态 -->
              <text @tap="onChangeSelected(item)" class="checkbox" :class="{ checked: item.selected }"></text>
              <navigator :url="`/pages/goods/goods?id=${item.goodsId}`" hover-class="none" class="navigator">
                <image mode="aspectFill" class="picture" :src="item.picture"></image>
                <view class="meta">
                  <view class="name ellipsis">{{ item.name }}</view>
                  <view class="attrsText ellipsis">{{ item.attrsText }}</view>
                  <view class="price">{{ item.nowPrice }}</view>
                </view>
              </navigator>
              <!-- 商品数量 -->
              <view class="count">
                <vk-data-input-number-box v-model="item.count" :min="1" :max="item.stock" :index="item.skuId"
                  @change="(ev: InputNumberBoxEvent) => onChangeCount(ev, item.goodsId)" />
              </view>
            </view>
            <!-- 右侧删除按钮 -->
            <template #right>
              <view class="cart-swipe-right">
                <!-- 这里改成 item.id -->
                <button @tap="onDeleteCart(Number(item.id))" class="button delete-button">删除</button>
              </view>
            </template>
          </uni-swipe-action-item>
        </uni-swipe-action>
      </view>
      <!-- 购物车空状态 -->
      <view class="cart-blank" v-else>
        <image src="/static/images/blank_cart.png" class="image" />
        <text class="text">购物车还是空的，快来挑选好货吧</text>
        <!-- 修复：保留样式 + 可点击跳转 -->
        <button class="button" @tap="gotoIndex">去首页看看</button>
      </view>
      <!-- 吸底工具栏 -->
      <view v-if="showCartList" class="toolbar"
        :style="{ paddingBottom: safeAreaInsetBottom ? safeAreaInsets?.bottom + 'px' : 0 }">
        <text @tap="onChangeSelectedAll" class="all" :class="{ checked: isSelectedAll }">全选</text>
        <text class="text">合计:</text>
        <text class="amount">{{ selectedCartListMoney }}</text>
        <view class="button-grounp">
          <view @tap="gotoPayment" class="button payment-button" :class="{ disabled: selectedCartListCount === 0 }">
            去结算({{ selectedCartListCount }})
          </view>
        </view>
      </view>
    </template>
    <!-- 未登录: 提示登录 -->
    <view class="login-blank" v-else>
      <text class="text">登录后可查看购物车中的商品</text>
      <navigator url="/pages/login/login" hover-class="none">
        <button class="button">去登录</button>
      </navigator>
    </view>
    <!-- 底部占位空盒子 -->
    <view class="toolbar-height"></view>
  </scroll-view>
</template>
<style lang="scss">
// 根元素
:host {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff9e6;
}

// 滚动容器
.scroll-view {
  flex: 1;
  background-color: #fff9e6;
}

// 购物车列表
.cart-list {
  padding: 0 20rpx;

  // 优惠提示
  .tips {
    display: flex;
    align-items: center;
    line-height: 1;
    margin: 30rpx 10rpx;
    font-size: 26rpx;
    color: #666;

    .label {
      color: #fff;
      padding: 7rpx 15rpx 5rpx;
      border-radius: 4rpx;
      font-size: 24rpx;
      background-color: #ffbc00;
      /* 黄色主题 */
      margin-right: 10rpx;
    }
  }

  // 购物车商品
  .goods {
    display: flex;
    padding: 20rpx 20rpx 20rpx 80rpx;
    border-radius: 16rpx;
    /* 更圆润 */
    background: #ffffff;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
    position: relative;

    .navigator {
      display: flex;
    }

    .checkbox {
      position: absolute;
      top: 0;
      left: 0;

      display: flex;
      align-items: center;
      justify-content: center;
      width: 80rpx;
      height: 100%;

      &::before {
        content: '\e6cd';
        font-family: 'erabbit' !important;
        font-size: 40rpx;
        color: #ccc;
      }

      &.checked::before {
        content: '\e6cc';
        color: #ffbc00;
        /* 选中黄色 */
      }
    }

    .picture {
      width: 170rpx;
      height: 170rpx;
      border-radius: 12rpx;
    }

    .meta {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      margin-left: 20rpx;
    }

    .name {
      height: 72rpx;
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }

    .attrsText {
      line-height: 1.8;
      padding: 0 15rpx;
      font-size: 24rpx;
      align-self: flex-start;
      border-radius: 8rpx;
      color: #888;
      background-color: #f7f7f8;
    }

    .price {
      line-height: 1;
      font-size: 28rpx;
      color: #ff4444;
      margin-bottom: 2rpx;
      font-weight: bold;

      &::before {
        content: '￥';
        font-size: 80%;
      }
    }

    // 商品数量
    .count {
      position: absolute;
      bottom: 20rpx;
      right: 5rpx;

      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 220rpx;
      height: 48rpx;

      .text {
        height: 100%;
        padding: 0 20rpx;
        font-size: 32rpx;
        color: #444;
      }

      .input {
        height: 100%;
        text-align: center;
        border-radius: 4rpx;
        font-size: 24rpx;
        color: #444;
        background-color: #f6f6f6;
      }
    }
  }

  .cart-swipe {
    display: block;
    margin: 20rpx 0;
  }

  .cart-swipe-right {
    display: flex;
    height: 100%;

    .button {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 50px;
      padding: 6px;
      line-height: 1.5;
      color: #fff;
      font-size: 26rpx;
      border-radius: 0;
    }

    .delete-button {
      background-color: #ff6b6b;
    }
  }
}

// 空状态
.cart-blank,
.login-blank {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  height: 60vh;

  .image {
    width: 400rpx;
    height: 281rpx;
  }

  .text {
    color: #666;
    font-size: 28rpx;
    margin: 20rpx 0;
  }

  .button {
    width: 240rpx !important;
    height: 70rpx !important;
    line-height: 70rpx;
    margin-top: 20rpx;
    font-size: 28rpx;
    border-radius: 70rpx;
    color: #fff;
    background-color: #ffbc00;
    border: none;
  }
}

// 吸底工具栏
.toolbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;

  height: 100rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  border-top: 1rpx solid #f5e5c8;
  background-color: #ffffff;
  box-sizing: content-box;

  .all {
    margin-left: 25rpx;
    font-size: 14px;
    color: #333;
    display: flex;
    align-items: center;
  }

  .all::before {
    font-family: 'erabbit' !important;
    content: '\e6cd';
    font-size: 40rpx;
    margin-right: 8rpx;
    color: #ccc;
  }

  .checked::before {
    content: '\e6cc';
    color: #ffbc00;
  }

  .text {
    margin-right: 8rpx;
    margin-left: 32rpx;
    color: #333;
    font-size: 14px;
  }

  .amount {
    font-size: 22px;
    color: #ff4444;
    font-weight: bold;

    .decimal {
      font-size: 12px;
    }

    &::before {
      content: '￥';
      font-size: 12px;
    }
  }

  .button-grounp {
    margin-left: auto;
    display: flex;
    justify-content: space-between;
    text-align: center;
    line-height: 72rpx;
    font-size: 13px;
    color: #fff;

    .button {
      width: 260rpx;
      height: 72rpx;
      line-height: 72rpx;
      margin: 0 10rpx;
      border-radius: 72rpx;
    }

    .payment-button {
      background-color: #ffbc00;
      /* 黄色结算按钮 */
      color: #fff;
      font-size: 28rpx;
      font-weight: 500;

      &.disabled {
        opacity: 0.5;
        background-color: #ddd;
      }
    }
  }
}

// 底部占位空盒子
.toolbar-height {
  height: 100rpx;
}
</style>
