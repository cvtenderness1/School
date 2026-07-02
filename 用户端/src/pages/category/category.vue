<script setup lang="ts">
import { getHomeCategoryAPI } from '@/services/home'
import { getHomeGoodsGuessLikeAPI } from '@/services/home'
import type { CategoryItem } from '@/types/home'
import type { GuessItem } from '@/types/home'
import { onLoad } from '@dcloudio/uni-app'
import { ref, computed } from 'vue'

// 分类
const categoryList = ref<CategoryItem[]>([])
const activeIndex = ref(0)

// 全部商品
const allGoods = ref<GuessItem[]>([])
const isFinish = ref(false)

// 搜索关键词
const searchKey = ref('')

// 获取分类
const getCategoryData = async () => {
  const res = await getHomeCategoryAPI()
  categoryList.value = res.data || []
}

// 获取全部商品
const getAllGoodsData = async () => {
  const res = await getHomeGoodsGuessLikeAPI(1, 999)
  allGoods.value = res.data.list || []
}

// 根据一级分类ID筛选 + 搜索 + 按desc二级分类分组
const goodsGroupList = computed(() => {
  const cid = categoryList.value[activeIndex.value]?.category_id
  if (!cid) return []

  // 1. 先筛选当前一级分类
  let filterGoods = allGoods.value.filter(item => item.categoryId === cid)

  // 2. 搜索过滤（关键词不为空时生效）
  if (searchKey.value.trim()) {
    const key = searchKey.value.trim()
    filterGoods = filterGoods.filter(item =>
      item.goodsName.includes(key)
    )
  }

  if (filterGoods.length === 0) return []

  // 3. 按 desc 分组
  const groupMap: Record<string, GuessItem[]> = {}
  filterGoods.forEach(item => {
    const desc = item.desc || '其他商品'
    if (!groupMap[desc]) {
      groupMap[desc] = []
    }
    groupMap[desc].push(item)
  })

  return Object.keys(groupMap).map(desc => ({
    desc,
    list: groupMap[desc]
  }))
})

// 切换分类
const switchCategory = (index: number) => {
  activeIndex.value = index
  // 切换分类时清空搜索
  searchKey.value = ''
}

onLoad(async () => {
  await getCategoryData()
  await getAllGoodsData()
  isFinish.value = true
})
</script>

<template>
  <view class="viewport" v-if="isFinish">
    <!-- 搜索 -->
    <view class="search">
      <view class="input">
        <text class="icon-search"></text>
        <input v-model="searchKey" class="search-input" placeholder="搜索商品" placeholder-class="placeholder" />
      </view>
    </view>

    <!-- 分类左右布局 -->
    <view class="categories">
      <!-- 左侧一级分类 -->
      <scroll-view class="primary" scroll-y>
        <view v-for="(item, index) in categoryList" :key="item.category_id" class="item"
          :class="{ active: index === activeIndex }" @tap="switchCategory(index)">
          {{ item.category_name }}
        </view>
      </scroll-view>

      <!-- 右侧：新增二级分类desc分组 -->
      <scroll-view class="secondary" scroll-y>
        <view class="panel">
          <!-- 分组循环：根据desc展示二级分类 -->
          <view v-for="(group, idx) in goodsGroupList" :key="idx" class="goods-group">
            <!-- 二级分类标题 -->
            <view class="group-title">
              <text class="name">{{ group.desc }}</text>
              <view class="more">全部</view>
            </view>

            <!-- 二级分类下商品 -->
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

          <!-- 空数据 -->
          <view class="empty" v-if="goodsGroupList.length === 0">
            {{ searchKey ? '没有找到相关商品' : '该分类下暂无商品' }}
          </view>
        </view>
      </scroll-view>
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

/* 分类 */
.categories {
  flex: 1;
  min-height: 400rpx;
  display: flex;
}

/* 一级分类 */
.primary {
  overflow: hidden;
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

  // 二级分组标题
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
</style>
