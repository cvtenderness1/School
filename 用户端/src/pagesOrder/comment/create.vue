<script setup lang="ts">
import { ref } from 'vue'
import { postMemberCommentAPI } from '@/services/order'

const query = defineProps<{
  orderId: string
}>()

const score = ref(5)
const content = ref('')

const submit = async () => {
  if (!content.value.trim()) {
    uni.showToast({ icon: 'none', title: '请输入评价' })
    return
  }

  try {
    await postMemberCommentAPI({
      orderId: query.orderId,
      score: score.value,
      content: content.value
    })
    uni.showToast({ icon: 'success', title: '评价成功' })
    uni.navigateBack()
  } catch (err) {
    uni.showToast({ icon: 'none', title: '评价失败' })
  }
}
</script>

<template>
  <view style="background:#fff9e6;padding:30rpx;min-height:100vh">
    <view style="font-size:34rpx;font-weight:bold;margin-bottom:30rpx">订单评价</view>

    <view
      style="background:#fff;padding:20rpx;border-radius:12rpx;display:flex;justify-content:space-between;align-items:center">
      <text>服务评分</text>
      <view class="stars">
        <text v-for="i in 5" :key="i" @tap="score = i" :style="{
          fontSize: '44rpx',
          color: i <= score ? '#ff9800' : '#ddd',
          marginLeft: '10rpx'
        }">
          ★
        </text>
      </view>
    </view>

    <textarea v-model="content" placeholder="请输入评价内容"
      style="background:#fff;margin-top:20rpx;border-radius:12rpx;padding:20rpx;height:240rpx" />

    <button @tap="submit"
      style="background:#ff9800;color:#fff;border-radius:12rpx;margin-top:40rpx;height:80rpx;border:none">
      提交评价
    </button>
  </view>
</template>

<style scoped>
.stars {
  display: flex;
}
</style>
