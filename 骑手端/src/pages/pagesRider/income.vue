<script setup lang="ts">
import { ref } from 'vue'

import { useRiderStore } from '@/stores'
import { http } from '@/utils/http'
import { onLoad } from '@dcloudio/uni-app'

// 骑手仓库
const riderStore = useRiderStore()
const riderId = ref<number>(0)
const balance = ref<number>(0)

// 排行榜
const rankList = ref<any>([])

// 页面加载
onLoad(() => {
  if (!riderStore.profile) return
  riderId.value = riderStore.profile.riderId
  getRiderInfo()
  getRankList()
})

// 1. 获取当前骑手信息（余额）
const getRiderInfo = async () => {
  const res = await http<any>({
    url: `/rider/${riderId.value}`,
    method: 'GET',
  })
  balance.value = res.data.balance || 0
}

// 2. 一键提现（直接把 balance 设为 0）
const withdraw = async () => {
  if (balance.value <= 0) {
    uni.showToast({ title: '暂无余额可提现', icon: 'none' })
    return
  }

  const res = await http<any>({
    url: `/rider/withdraw/${riderId.value}`,
    method: 'POST',
  })
  uni.showToast({ title: '提现成功', icon: 'success' })

  await getRiderInfo()
}

// 3. 获取骑手排行榜（按余额降序）
const getRankList = async () => {
  const res = await http<any>({
    url: '/rider/rank',
    method: 'GET',
  })
  rankList.value = res.data
}
</script>

<template>
  <view class="wrap">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="label">可提现余额</view>
      <view class="num">¥{{ balance }}</view>
      <button class="btn" @click="withdraw">一键提现到微信</button>
    </view>

    <!-- 排行榜 -->
    <view class="rank-title">骑手收入排行榜</view>
    <view class="rank-item" v-for="(item, index) in rankList" :key="item.riderId">
      <view class="rank">#{{ Number(index) + 1 }}</view>
      <view class="name">{{ item.name }}</view>
      <view class="money">¥{{ item.balance }}</view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.wrap {
  padding: 20rpx;
  background: #f7f7f7;
  min-height: 100vh;
}

/* 余额卡片 */
.balance-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  text-align: center;
  margin-bottom: 30rpx;

  .label {
    font-size: 28rpx;
    color: #666;
  }

  .num {
    font-size: 60rpx;
    font-weight: bold;
    color: #ff3333;
    margin: 20rpx 0;
  }

  .btn {
    background: #ff9f00;
    color: #fff;
    border-radius: 50rpx;
    border: none;
    padding: 10rpx 30rpx;
  }
}

/* 排行榜 */
.rank-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.rank-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;

  .rank {
    width: 70rpx;
    color: #ff9f00;
    font-weight: bold;
  }

  .name {
    flex: 1;
  }

  .money {
    color: #ff3333;
    font-weight: bold;
  }
}
</style>
