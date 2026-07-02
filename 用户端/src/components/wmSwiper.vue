<script setup lang="ts">
import { ref } from 'vue'

const activeIndex = ref(0)

// 直接可用的网络轮播图
const list = ref([
  { id: 1, imgUrl: 'https://pic1.zhimg.com/80/v2-b97278f1fcf0c7887a52fa3379e15c36_720w.webp?source=1def8aca' },
  { id: 2, imgUrl: 'https://pic1.zhimg.com/80/v2-b97278f1fcf0c7887a52fa3379e15c36_720w.webp?source=1def8aca' },
])

const onChange: UniHelper.SwiperOnChange = (ev) => {
  activeIndex.value = ev.detail.current
}
</script>

<template>
  <view class="carousel">
    <swiper :circular="true" autoplay :interval="3000" @change="onChange">
      <swiper-item v-for="item in list" :key="item.id">
        <navigator url="/pages/index/index" hover-class="none" class="navigator">
          <image mode="aspectFill" class="image" :src="item.imgUrl"></image>
        </navigator>
      </swiper-item>
    </swiper>
    <!-- 指示点 -->
    <view class="indicator">
      <text v-for="(item, index) in list" :key="item.id" class="dot" :class="{ active: index === activeIndex }"></text>
    </view>
  </view>
</template>

<style lang="scss">
@import './styles/wmSwiper.scss';
</style>
