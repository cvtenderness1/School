<script setup lang="ts">
import { getHomeCategoryAPI, getHomeHotAPI } from '@/services/home'
import CustomNavbar from './components/CustomNavbar.vue'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import CategoryPanel from './components/CategoryPanel.vue'
import type { CategoryItem, HotItem } from '@/types/home'
import HotPanel from './components/HotPanel.vue'
import type { wmGuessInstance } from '@/types/component'

const categoryList = ref<CategoryItem[]>([])
const getHomeCategoryData = async () => {
  const res = await getHomeCategoryAPI()
  const uniqueList = res.data.reduce((acc: CategoryItem[], current: CategoryItem) => {
    const isExist = acc.some(item => item.category_name === current.category_name)
    if (!isExist) {
      acc.push(current)
    }
    return acc
  }, [])
  categoryList.value = uniqueList
}

const hotList = ref<HotItem[]>([])
const getHomeHotData = async () => {
  const res = await getHomeHotAPI()
  hotList.value = res.data
}

onLoad(() => {
  getHomeCategoryData()
  getHomeHotData()
})
const guessRef = ref<wmGuessInstance>()
const onScrolltolower = () => {
  guessRef.value?.getMore()
}
const isTriggered = ref(false)
const onRefresherrefresh = async () => {
  isTriggered.value = true
  // await getHomeCategoryData()
  // await getHomeHotData()
  guessRef.value?.resetData()
  await Promise.all([getHomeCategoryData(), getHomeHotData(), guessRef.value?.getMore()])
  isTriggered.value = false
}
</script>
<template>
  <CustomNavbar />
  <scroll-view refresher-enabled @refresherrefresh="onRefresherrefresh" @scrolltolower="onScrolltolower"
    :refresher-triggered="isTriggered" class="scroll-view" scroll-y>
    <wmSwiper />
    <CategoryPanel :list="categoryList" />
    <!--推荐-->
    <HotPanel :list="hotList" />
    <wmGuess ref="guessRef" />
  </scroll-view>
</template>
<style lang="scss">
page {
  background-color: #f7f7f7;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scroll-view {
  flex: 1;
}
</style>
