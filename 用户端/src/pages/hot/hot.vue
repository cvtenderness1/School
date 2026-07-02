<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantDetailAPI, getMerchantGoodsAPI } from '@/services/home'
import { getMerchantCommentsAPI } from '@/services/order'
import type { HotItem, GuessItem } from '@/types/home'
import type { CommentItem } from '@/types/order'
import { computed, ref } from 'vue'

// 商家信息
const merchant = ref<HotItem>()
// 商家全部商品
const goodsList = ref<GuessItem[]>([])
const loading = ref(true)

// 搜索关键词
const searchKey = ref('')

// 左侧分类
const categoryList = computed(() => {
  const map: Record<string, boolean> = {}
  goodsList.value.forEach(item => {
    const key = item.desc || '其他商品'
    map[key] = true
  })
  return Object.keys(map)
})

// 当前选中分类
const activeIndex = ref(0)

// 右侧商品列表
const goodsGroupList = computed(() => {
  const key = categoryList.value[activeIndex.value]
  if (!key) return []

  let list = goodsList.value.filter(item => (item.desc || '其他商品') === key)
  if (searchKey.value.trim()) {
    const kw = searchKey.value.trim()
    list = list.filter(item => item.goodsName.includes(kw))
  }
  return [{ desc: key, list }]
})

// 切换分类
const switchCategory = (index: number) => {
  activeIndex.value = index
  searchKey.value = ''
}

// ========== 评价相关 ==========
const commentList = ref<CommentItem[]>([])
const showCommentPopup = ref(false)

// 加载评价（修复了类型报错）
const loadComments = async (merchantId: number) => {
  const { data } = await getMerchantCommentsAPI(merchantId)
  commentList.value = data || []
}
const openCommentPopup = () => {
  showCommentPopup.value = true
}
const closeCommentPopup = () => {
  showCommentPopup.value = false
}

onLoad(async (options: any) => {
  const merchantId = Number(options.id)
  const { data: shopData } = await getMerchantDetailAPI(merchantId)
  merchant.value = shopData

  const { data: goodsData } = await getMerchantGoodsAPI(merchantId)
  goodsList.value = goodsData

  await loadComments(merchantId)

  if (categoryList.value.length) activeIndex.value = 0
  loading.value = false
})
</script>

<template>
  <view class="viewport" v-if="!loading">
    <!-- 搜索栏 -->
    <view class="search">
      <view class="input">
        <text class="icon-search"></text>
        <input v-model="searchKey" class="search-input" placeholder="搜索商品" placeholder-class="placeholder" />
      </view>
    </view>

    <!-- 商家头部 -->
    <view class="shop-header">
      <image class="shop-logo" :src="merchant?.coverImg" mode="aspectFill" />
      <view class="shop-info">
        <view class="name">{{ merchant?.merchantName }}</view>
        <view class="desc">{{ merchant?.alt || '暂无店铺介绍' }}</view>
        <view class="tags">
          <text class="tag score">⭐ {{ merchant?.score }} 分</text>
          <text class="tag fee">配送费 ¥{{ merchant?.postFee }}</text>
          <text class="tag status" :class="merchant?.status === 1 ? 'open' : 'close'">
            {{ merchant?.status === 1 ? '营业中' : '休息中' }}
          </text>
          <text class="tag comment-btn" @tap="openCommentPopup">查看评价</text>
        </view>
      </view>
    </view>

    <!-- 商品分类 -->
    <view class="categories">
      <scroll-view class="primary" scroll-y>
        <view v-for="(cat, index) in categoryList" :key="index" class="item" :class="{ active: index === activeIndex }"
          @tap="switchCategory(index)">
          {{ cat }}
        </view>
      </scroll-view>

      <scroll-view class="secondary" scroll-y>
        <view class="panel">
          <view v-for="(group, idx) in goodsGroupList" :key="idx" class="goods-group">
            <view class="group-title">
              <text class="name">{{ group.desc }}</text>
              <view class="more">全部</view>
            </view>
            <view class="section">
              <navigator v-for="item in group.list" :key="item.goodsId" class="goods"
                :url="`/pages/goods/goods?id=${item.goodsId}`">
                <image class="image" :src="item.img" mode="aspectFill" />
                <view class="name ellipsis">{{ item.goodsName }}</view>
                <view class="price">
                  <text class="symbol">¥</text>
                  <text class="number">{{ item.price }}</text>
                </view>
              </navigator>
            </view>
          </view>
          <view class="empty" v-if="goodsGroupList.length === 0 || goodsGroupList[0].list.length === 0">
            {{ searchKey ? '没有找到相关商品' : '该分类下暂无商品' }}
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="comment-popup" v-if="showCommentPopup" @tap="closeCommentPopup">
      <view class="popup-content" @tap.stop>
        <view class="popup-header">
          <text class="title">店铺评价</text>
          <text class="close-btn" @tap="closeCommentPopup">×</text>
        </view>
        <scroll-view class="comment-list" scroll-y>
          <view v-if="commentList.length === 0" class="empty-tip">暂无评价</view>

          <!-- 评价列表：显示 评分 + 时间 + 用户ID -->
          <view v-for="item in commentList" :key="item.commentId" class="comment-item">
            <view class="comment-header">
              <view class="left">
                <text class="uid">用户 {{ item.userId }}</text>
                <text class="stars">⭐{{ item.score }}星</text>
              </view>
              <text class="time">{{ item.createTime }}</text>
            </view>
            <view class="comment-content">{{ item.content || '用户未填写评价内容' }}</view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<style lang="scss">
page {
  height: 100%;
  overflow: hidden;
  background-color: #fff9e6;
}

.viewport {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff9e6;
}

/* 搜索栏 */
.search {
  padding: 20rpx 30rpx;
  background: linear-gradient(90deg, #ffbc00 0%, #ff9f00 100%);

  .input {
    display: flex;
    align-items: center;
    height: 64rpx;
    padding-left: 26rpx;
    color: #999;
    font-size: 28rpx;
    border-radius: 32rpx;
    background-color: #ffffff;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  }

  .search-input {
    flex: 1;
    height: 100%;
    margin-left: 10rpx;
    font-size: 28rpx;
    color: #333;
  }

  .placeholder {
    color: #999;
  }
}

.icon-search {
  &::before {
    margin-right: 10rpx;
    color: #ffbc00;
  }
}

/* 商家头部 */
.shop-header {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 188, 0, 0.08);
}

.shop-logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #f6f6f6;
}

.shop-info {
  flex: 1;
}

.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.desc {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.tag {
  font-size: 22rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
  color: #666;
}

.tag.score {
  color: #ff7d00;
  background: #fff2e5;
}

.tag.fee {
  color: #ff4444;
  background: #fff0f0;
}

.tag.status.open {
  color: #fff;
  background: #ff9f00;
}

.tag.status.close {
  color: #999;
  background: #f0f0f0;
}

.tag.comment-btn {
  color: #fff;
  background: #ff7d00;
}

/* 分类布局 */
.categories {
  flex: 1;
  min-height: 400rpx;
  display: flex;
}

/* 左侧分类 */
.primary {
  width: 180rpx;
  flex: none;
  background-color: #fff;

  .item {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 96rpx;
    font-size: 26rpx;
    color: #666;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      left: 30rpx;
      right: 30rpx;
      bottom: 0;
      border-top: 1rpx solid #f5f5f5;
    }
  }

  .active {
    background-color: #fffcf5;
    color: #ffbc00;
    font-weight: 500;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 8rpx;
      height: 100%;
      background-color: #ffbc00;
    }
  }
}

.primary .item:last-child::after,
.primary .active::after {
  display: none;
}

/* 右侧内容 */
.secondary {
  flex: 1;
  background-color: #fff9e6;

  .panel {
    margin: 0 30rpx;
  }

  .group-title {
    height: 70rpx;
    line-height: 70rpx;
    color: #333;
    font-size: 28rpx;
    font-weight: 500;
    border-bottom: 1rpx solid #f2e5c8;
    margin-top: 20rpx;

    .more {
      float: right;
      padding-left: 20rpx;
      font-size: 24rpx;
      color: #ffbc00;
    }
  }

  .section {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx 0;

    .goods {
      width: 150rpx;
      margin: 0 20rpx 30rpx 0;
      background: #fff;
      border-radius: 16rpx;
      overflow: hidden;
      box-shadow: 0 4rpx 12rpx rgba(255, 188, 0, 0.08);

      &:nth-child(3n) {
        margin-right: 0;
      }

      .image {
        width: 150rpx;
        height: 150rpx;
      }

      .name {
        padding: 8rpx 10rpx 2rpx;
        font-size: 22rpx;
        color: #333;
      }

      .price {
        padding: 2rpx 10rpx 10rpx;
        font-size: 18rpx;
        color: #ff4444;
        font-weight: bold;
      }

      .number {
        font-size: 24rpx;
        margin-left: 2rpx;
      }
    }
  }
}

.empty {
  width: 100%;
  text-align: center;
  padding: 60rpx 0;
  font-size: 26rpx;
  color: #999;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 评价弹窗 */
.comment-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;

  .popup-content {
    width: 80%;
    max-height: 70%;
    background: #fff;
    border-radius: 20rpx;
    display: flex;
    flex-direction: column;

    .popup-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx;
      border-bottom: 1rpx solid #eee;

      .title {
        font-size: 32rpx;
        font-weight: bold;
      }

      .close-btn {
        font-size: 40rpx;
        color: #999;
      }
    }

    .comment-list {
      flex: 1;
      padding: 20rpx 30rpx;
      box-sizing: border-box;

      .empty-tip {
        text-align: center;
        padding: 60rpx 0;
        color: #999;
        font-size: 26rpx;
      }

      .comment-item {
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f5f5f5;

        &:last-child {
          border-bottom: none;
        }

        .comment-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 10rpx;
          align-items: center;

          .left {
            display: flex;
            align-items: center;
            gap: 20rpx;
          }

          .stars {
            font-size: 26rpx;
            color: #ff7d00;
          }

          .uid {
            font-size: 22rpx;
            color: #fff;
            background: #ffa733;
            padding: 4rpx 10rpx;
            border-radius: 6rpx;
          }

          .time {
            font-size: 22rpx;
            color: #999;
          }
        }

        .comment-content {
          font-size: 26rpx;
          color: #333;
          line-height: 1.5;
        }
      }
    }
  }
}
</style>
