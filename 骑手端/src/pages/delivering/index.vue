<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
const mode = ref('list')
const list = ref([])
const near = ref([])
const myLat = ref(29.85)
const myLng = ref(121.65)

const load = async () => {
  const { data } = await uni.request({ url: '/rider/delivering' })
  list.value = data.data
  const n = await uni.request({
    url: '/rider/nearbyOrders',
    data: { lat: myLat.value, lng: myLng.value },
  })
  near.value = n.data.data
}

const markers = computed(() => {
  let arr = []
  list.value.forEach((o) => {
    arr.push({ id: 1, latitude: o.shopLat, longitude: o.shopLng, iconPath: '/static/shop.png' })
    arr.push({ id: 2, latitude: o.userLat, longitude: o.userLng, iconPath: '/static/user.png' })
  })
  return arr
})

const polyline = computed(() => {
  return list.value.map((o) => ({
    points: [
      { latitude: o.shopLat, longitude: o.shopLng },
      { latitude: o.userLat, longitude: o.userLng },
    ],
    color: '#ff980088',
    width: 6,
  }))
})

const call = (item) => uni.makePhoneCall({ phoneNumber: item.userPhone })
const go = (item) => uni.navigateTo({ url: `/pages/rider/finish?id=${item.orderId}` })

onShow(load)
</script>

<template>
  <view class="page">
    <view class="bar">
      <text :class="mode === 'list' && 'on'" @click="mode = 'list'">配送列表</text>
      <text :class="mode === 'map' && 'on'" @click="mode = 'map'">地图模式</text>
    </view>

    <scroll-view v-if="mode === 'list'" scroll-y class="list">
      <view class="card" v-for="item in list" :key="item.orderId">
        <view class="t">{{ item.orderType === 1 ? '跑腿' : '跑腿' }}</view>
        <view class="text">商家：{{ item.startAddress }}</view>
        <view class="text">用户：{{ item.endAddress }}</view>
        <view class="gap mt-2">
          <button size="mini" @click="call(item)">电话</button>
          <button size="mini" type="primary" @click="go(item)">送达</button>
        </view>
      </view>

      <view class="p-2 font-bold">🚚 顺路推荐</view>
      <view class="card p-2" v-for="n in near" :key="n.orderId">
        <view>距离：{{ n.distance }}m</view>
        <button size="mini" type="primary">抢单</button>
      </view>
    </scroll-view>

    <map
      v-else
      class="map"
      :latitude="myLat"
      :longitude="myLng"
      :scale="16"
      :markers="markers"
      :polyline="polyline"
    />
  </view>
</template>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
}
.bar {
  display: flex;
  background: #fff;
  padding: 10px;
}
.bar text {
  flex: 1;
  text-align: center;
  padding: 10px;
}
.bar .on {
  color: #ff9800;
  border-bottom: 2px solid #ff9800;
}
.card {
  background: #fff;
  margin: 10px;
  padding: 15px;
  border-radius: 12px;
}
.gap {
  display: flex;
  gap: 10px;
}
.map {
  width: 100%;
  height: 700rpx;
}
</style>
