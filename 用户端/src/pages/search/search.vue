<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { getHomeHotAPI } from '@/services/home'
import type { HotItem } from '@/types/home'
import { ref, watch } from 'vue'

const keyword = ref('')
const merchantList = ref<HotItem[]>([])
const loading = ref(false)

// 历史搜索列表
const historyList = ref<string[]>([])

// 从本地缓存加载历史记录
const loadHistory = () => {
  const history = uni.getStorageSync('searchHistory') as unknown[] || []
  const safeHistory = history
    .filter((item): item is string => typeof item === 'string')
  historyList.value = [...new Set(safeHistory)]
}

// 保存搜索历史
const saveHistory = (value: string) => {
  if (!value.trim()) return
  const history = (uni.getStorageSync('searchHistory') as unknown[] || [])
    .filter((item): item is string => typeof item === 'string')
  const newHistory = history.filter(item => item !== value)
  newHistory.unshift(value)
  if (newHistory.length > 10) newHistory.length = 10
  uni.setStorageSync('searchHistory', newHistory)
  loadHistory()
}

// 点击历史项搜索
const clickHistory = (item: string) => {
  keyword.value = item
}

// 清空所有历史
const clearHistory = () => {
  uni.removeStorageSync('searchHistory')
  loadHistory()
}

// 搜索商家
const searchMerchant = async (value: string) => {
  if (!value.trim()) {
    merchantList.value = []
    return
  }

  loading.value = true
  const res = await getHomeHotAPI()
  const list = res.data || []
  merchantList.value = list.filter((item: HotItem) => {
    return item.merchantName.includes(value)
  })
  loading.value = false

  // 搜索成功后保存历史
  saveHistory(value)
}

// 监听搜索
watch(keyword, (newVal) => {
  searchMerchant(newVal)
})

onLoad(() => {
  loadHistory()
})
</script>

<template>
  <view class="search-page">
    <!-- 美团风格搜索框 -->
    <view class="search-box">
      <text class="icon-search"></text>
      <input v-model="keyword" placeholder="搜索商家名称" class="input" />
      <text v-if="keyword" class="clear-btn" @click="keyword = ''">✕</text>
    </view>

    <!-- ==================== 新增：历史搜索区域 ==================== -->
    <view class="history-box" v-if="!keyword && historyList.length">
      <view class="history-header">
        <text class="title">搜索历史</text>
        <text class="clear" @click="clearHistory">清空</text>
      </view>
      <view class="history-list">
        <text v-for="(item, index) in historyList" :key="index" class="history-item" @click="clickHistory(item)">
          {{ item }}
        </text>
      </view>
    </view>
    <!-- ========================================================== -->

    <!-- 搜索结果 -->
    <view class="result-box" v-if="keyword">
      <view class="title">搜索结果</view>

      <!-- 商家卡片 -->
      <navigator v-for="item in merchantList" :key="item.merchantId" :url="`/pages/hot/hot?id=${item.merchantId}`"
        class="merchant-card">
        <image class="logo" :src="item.coverImg" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ item.merchantName }}</view>
          <view class="desc">{{ item.alt || '这家店很懒，什么都没写' }}</view>
          <view class="tag">
            <text>⭐ {{ item.score }}</text>
            <text>¥{{ item.postFee }} 配送</text>
          </view>
        </view>
      </navigator>

      <!-- 空状态 -->
      <view class="empty" v-if="merchantList.length === 0 && !loading">
        未找到相关商家
      </view>
    </view>
  </view>
</template>

<style lang="scss">
/* 整体页面 */
.search-page {
  padding: 20rpx;
  background-color: #f7f7f7;
  min-height: 100vh;
}

/* 美团搜索框 */
.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50rpx;
  padding: 0 30rpx;
  height: 80rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

  .icon-search {
    font-size: 32rpx;
    color: #999;
    margin-right: 16rpx;
  }

  .input {
    flex: 1;
    font-size: 28rpx;
    color: #333;
  }

  .clear-btn {
    font-size: 24rpx;
    color: #bbb;
    padding: 10rpx;
  }
}

.history-box {
  margin-top: 30rpx;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-left: 10rpx;
    margin-bottom: 20rpx;

    .title {
      font-size: 26rpx;
      color: #666;
    }

    .clear {
      font-size: 24rpx;
      color: #999;
    }
  }

  .history-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .history-item {
      background: #fff;
      padding: 12rpx 24rpx;
      border-radius: 30rpx;
      font-size: 26rpx;
      color: #666;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
    }
  }
}

/* 结果标题 */
.result-box {
  margin-top: 30rpx;

  .title {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 20rpx;
    padding-left: 10rpx;
  }
}

/* 商家卡片 - 美团样式 */
.merchant-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);

  .logo {
    width: 120rpx;
    height: 120rpx;
    border-radius: 16rpx;
    background: #f5f5f5;
    flex-shrink: 0;
  }

  .info {
    flex: 1;
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .name {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 8rpx;
    }

    .desc {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 12rpx;
      line-height: 1.4;
    }

    .tag {
      display: flex;
      gap: 20rpx;
      font-size: 22rpx;
      color: #ff9f00;
    }
  }
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #ccc;
  font-size: 26rpx;
}
</style>
