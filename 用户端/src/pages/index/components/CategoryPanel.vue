<script setup lang="ts">
import type { CategoryItem } from '@/types/home'
import { computed } from 'vue';

const props = defineProps<{
  list: CategoryItem[]
}>()

const showList = computed(() => {
  return props.list.slice(0, 10)
})
</script>

<template>
  <view class="category">
    <navigator class="category-item" hover-class="none" url="/pages/index/index" v-for="item in showList"
      :key="item.category_id">
      <image class="icon" src="https://www.0xiao.cn/static/img/web/errands_ico.png">
      </image>
      <text class="text">{{ item.category_name }}</text>
    </navigator>
  </view>
</template>

<style lang="scss">
@import '../styles/category.scss';
</style>
