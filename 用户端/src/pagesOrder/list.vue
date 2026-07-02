<script setup lang="ts">
import { ref } from 'vue'
import OrderList from './OrderList.vue'

// 获取页面参数
const query = defineProps<{
  type: string
}>()

// tabs 数据
const orderTabs = ref([
  { orderState: 0, title: '待付款', isRender: false },
  { orderState: 1, title: '待接单', isRender: false },
  { orderState: 2, title: '待配送', isRender: false },
  { orderState: 3, title: '配送中', isRender: false },
  { orderState: 4, title: '待评价', isRender: false },
])

// 高亮下标
const activeIndex = ref(orderTabs.value.findIndex((v) => v.orderState === Number(query.type)))
// 默认渲染容器
orderTabs.value[activeIndex.value].isRender = true
</script>

<template>
  <view class="viewport">
    <!-- tabs -->
    <view class="tabs">
      <text class="item" v-for="(item, index) in orderTabs" :key="item.title" @tap="
        () => {
          activeIndex = index
          item.isRender = true
        }
      ">
        {{ item.title }}
      </text>
      <!-- 游标 -->
      <view class="cursor" :style="{ left: activeIndex * 20 + '%' }"></view>
    </view>
    <!-- 滑动容器 -->
    <swiper class="swiper" :current="activeIndex" @change="activeIndex = $event.detail.current">
      <!-- 滑动项 -->
      <swiper-item v-for="item in orderTabs" :key="item.title">
        <!-- 订单列表 -->
        <OrderList v-if="item.isRender" :order-state="item.orderState" />
      </swiper-item>
    </swiper>
  </view>
</template>

<style lang="scss">
page {
  height: 100%;
  overflow: hidden;
}

.viewport {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff9e6;
}

// tabs
.tabs {
  display: flex;
  justify-content: space-around;
  line-height: 60rpx;
  margin: 0 10rpx;
  /* 标签栏背景改为暖白色 */
  background-color: #fffdf5;
  /* 淡黄色柔和阴影 */
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
    /* 未选中文字深灰 */
    color: #555;
    font-weight: 500;
  }

  .cursor {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 20%;
    height: 6rpx;
    border-radius: 3rpx;
    background-color: #ff9f00;
    /* 过渡效果 */
    transition: all 0.4s ease;
  }
}

// swiper
.swiper {
  /* 内容区域背景浅暖黄 */
  background-color: #fff9e6;
  flex: 1;
}
</style>
