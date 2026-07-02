<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const mode = ref('map')
const list = ref([])
const near = ref([])
const loading = ref(false)

// 默认坐标
const myLat = ref(39.92)
const myLng = ref(116.416334)
const mapKey = ref(0)

// 更新骑手位置接口
const updateRiderLocation = async (lat, lng) => {
  try {
    const riderId = uni.getStorageSync('riderId')
    if (!riderId) return
    await uni.request({
      url: '/rider/updateLocation',
      method: 'POST',
      data: { riderId, lat, lng },
    })
  } catch (e) {
    /* empty */
  }
}

// 获取定位 + 再加载列表
const getMyLocation = () => {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        myLat.value = res.latitude
        myLng.value = res.longitude
        updateRiderLocation(res.latitude, res.longitude)
        resolve(true)
      },
      fail: () => {
        resolve(false)
      },
    })
  })
}

// 加载配送中订单 + 顺路单
const load = async () => {
  loading.value = true
  // 1. 先拿到最新定位
  await getMyLocation()

  // 2. 当前正在配送订单
  const deliverRes = await uni.request({ url: '/rider/delivering' })
  list.value = deliverRes.data.data || []

  // 3. 顺路单：传最新经纬度
  const nearRes = await uni.request({
    url: '/rider/nearbyOrders',
    method: 'GET',
    data: {
      lat: myLat.value,
      lng: myLng.value,
    },
  })
  near.value = nearRes.data.data || []

  loading.value = false
  mapKey.value++
}

// 地图标记
const markers = computed(() => {
  let arr = []
  // 自己位置
  arr.push({
    id: 'rider_self',
    latitude: myLat.value,
    longitude: myLng.value,
    iconPath: '',
    width: 45,
    height: 45,
    anchor: { x: 0.5, y: 0.5 },
  })
  // 配送中订单起点终点
  list.value.forEach((o) => {
    arr.push({
      id: o.orderId + '_start',
      latitude: o.shopLat || o.startLat || 39.8,
      longitude: o.shopLng || o.startLng || 116.3,
      iconPath:
        'https://s1.aigei.com/src/img/png/85/8588385370494fc4b9027d727bd01590.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:Wqm2B-kTCCyAe3MBUv7foKcX9ic=',
      width: 40,
      height: 40,
      anchor: { x: 0.5, y: 1 },
    })
    arr.push({
      id: o.orderId + '_end',
      latitude: o.userLat || o.endLat || 39.8,
      longitude: o.userLng || o.endLng || 116.3,
      iconPath:
        'https://s1.chu0.com/src/img/png/37/376f37cbfccf44d9b426db85a5878c26.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=1srnZGLKZ0Aqlz6dk7yF4SkiYf4eP-YrEOdM1sob:6Zv5SNQX4MYJwRZ4_ule6EVkqOo=',
      width: 40,
      height: 40,
    })
  })
  return arr
})

// 路线
const polyline = computed(() => {
  return list.value.map((o) => {
    // 起点（取货点）
    const startLat = o.shopLat || o.startLat || 0
    const startLng = o.shopLng || o.startLng || 0

    // 终点（送达点）
    const endLat = o.userLat || o.endLat || 0
    const endLng = o.userLng || o.endLng || 0

    const midPoint = {
      latitude: startLat,
      longitude: endLng,
    }

    return {
      points: [
        { latitude: startLat, longitude: startLng }, // 起点
        midPoint,
        { latitude: endLat, longitude: endLng }, // 终点
      ],
      color: '#FF6A0088',
      width: 8,
      arrowLine: true,
      borderColor: '#FF4500',
      borderWidth: 2,
    }
  })
})

// 打电话
const call = (item) => {
  uni.makePhoneCall({
    phoneNumber: item.userPhone || '13800138000',
  })
}

// 去送达页
const go = (item) => {
  uni.navigateTo({
    url: `/pages/pagesRider/finish?id=${item.orderId}`,
  })
}

// 导航
const openNavigation = (item) => {
  uni.openLocation({
    latitude: item.userLat || item.endLat,
    longitude: item.userLng || item.endLng,
    name: '送达位置',
    address: item.endAddress || '用户地址',
  })
}

// 顺路单抢单事件
const grabOrder = async (item) => {
  uni.showLoading({ title: '抢单中...' })
  try {
    await uni.request({
      url: '/rider/grab',
      method: 'POST',
      data: {
        orderId: item.orderId,
        riderId: uni.getStorageSync('riderId'),
      },
    })
    uni.showToast({ title: '抢单成功' })
    load() // 抢单后刷新列表
  } catch (e) {
    uni.showToast({ icon: 'none', title: '抢单失败' })
  } finally {
    uni.hideLoading()
  }
}

onShow(async () => {
  await load()
})
</script>

<template>
  <view class="container">
    <!-- 顶部模式切换 -->
    <view class="tab-box">
      <text :class="mode === 'map' ? 'tab active' : 'tab'" @click="mode = 'map'"> 地图导航 </text>
      <text :class="mode === 'list' ? 'tab active' : 'tab'" @click="mode = 'list'"> 配送列表 </text>
    </view>
    <!-- 地图模式 -->

    <view v-show="mode === 'map'">
      <map
        :key="mapKey"
        class="map"
        :latitude="myLat"
        :longitude="myLng"
        :scale="16"
        :markers="markers"
        :polyline="polyline"
        :show-location="true"
        :enable-zoom="true"
        :enable-scroll="true"
        enable-satellite="false"
        enable-traffic="true"
      />

      <!-- 悬浮订单卡片 -->

      <view class="order-card" v-for="item in list" :key="item.orderId">
        <view class="card-top">
          <view class="tag" :class="item.orderType === 1 ? 'tag-waimai' : 'tag-paotui'">
            {{ item.orderType === 1 ? '跑腿' : '跑腿' }}
          </view>

          <text class="order-no">{{ item.orderId }}</text>
        </view>

        <view class="line">
          <text>取：</text>

          <text class="text-sm truncate">{{ item.startAddress }}</text>
        </view>

        <view class="line">
          <text>送：</text>

          <text class="text-sm truncate">{{ item.endAddress }}</text>
        </view>

        <view class="btns">
          <button size="mini" type="primary" plain @click="openNavigation(item)">导航</button>

          <button size="mini" type="primary" @click="call(item)">电话</button>

          <button size="mini" type="primary" @click="go(item)">送达</button>
        </view>
      </view>
    </view>

    <!-- 列表模式 -->

    <scroll-view v-show="mode === 'list'" scroll-y class="list-wrap">
      <view class="item" v-for="item in list" :key="item.orderId">
        <view class="left">
          <view class="tag" :class="item.orderType === 1 ? 'tag-waimai' : 'tag-paotui'">
            {{ item.orderType === 1 ? '跑腿' : '跑腿' }}
          </view>

          <view class="addrs">
            <text>取货: {{ item.startAddress }}</text>

            <text>送货: {{ item.endAddress }}</text>
          </view>
        </view>

        <view class="right">
          <button size="mini" type="primary" @click="go(item)">送达</button>
        </view>
      </view>

      <view style="margin-top: 30rpx"></view>

      <view class="list-title">顺路推荐</view>

      <view
        class="item"
        v-for="item in near"
        :key="item.orderId"
        style="border-left: 4px solid #ff6a00"
      >
        <view class="left">
          <view class="tag" style="background: #fff0e6; color: #ff6a00">顺路单</view>

          <view class="addrs">
            <text>取货: {{ item.startAddress }}</text>

            <text>送货: {{ item.endAddress }}</text>

            <text style="color: #ff6a00">距离：{{ item.distance }}m</text>

            <button size="mini" type="primary" @click="grabOrder(item)">抢单</button>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="loading" v-if="loading">加载中...</view>
  </view>
</template>
<style scoped>
.container {
  background: #f6f7f9;
  min-height: 100vh;
  padding: 20rpx;
  box-sizing: border-box;
}

/* 顶部tab */
.tab-box {
  display: flex;
  margin: 0 0 20rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 8rpx;
  gap: 8rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.tab {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #666;
  transition: all 0.2s ease;
}

.tab.active {
  background: #ff6a00;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 2rpx 8rpx rgba(255, 106, 0, 0.3);
}

/* 地图 */
.map {
  width: 100%;
  height: calc(100vh - 180rpx);
  border-radius: 16rpx;
  overflow: hidden;
}

/* 悬浮订单卡片 */
.order-card {
  background: #ffffff;
  margin: -150rpx 20rpx 30rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  position: relative;
  z-index: 10;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.tag {
  padding: 8rpx 18rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.tag-waimai {
  background: #fff5e6;
  color: #ff6a00;
}

.tag-paotui {
  background: #e6f7f0;
  color: #00c48c;
}

.order-no {
  font-size: 24rpx;
  color: #999;
  letter-spacing: 1rpx;
}

.line {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #333;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.truncate {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-left: 8rpx;
}

.btns {
  display: flex;
  gap: 20rpx;
  margin-top: 24rpx;
}

.btns button {
  flex: 1;
  border-radius: 12rpx;
  font-size: 26rpx;
  padding: 16rpx;
  font-weight: 500;
}

/* 列表样式 */
.list-wrap {
  padding: 0;
  height: calc(100vh - 120rpx);
}

.item {
  background: #ffffff;
  padding: 32rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.addrs {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.addrs text {
  display: block;
  margin-bottom: 8rpx;
}

.addrs button {
  margin-top: 8rpx;
  border-radius: 12rpx;
}

.list-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-left: 8rpx;
}

.loading {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
