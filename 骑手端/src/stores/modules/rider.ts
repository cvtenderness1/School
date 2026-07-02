import type { RiderLoginResult } from '@/types/rider'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRiderStore = defineStore(
  'rider',
  () => {
    const profile = ref<RiderLoginResult>()

    // 保存骑手信息
    const setProfile = (val: RiderLoginResult) => {
      profile.value = val
    }

    // 清空骑手信息
    const clearProfile = () => {
      profile.value = undefined
    }

    return {
      profile,
      setProfile,
      clearProfile,
    }
  },
  {
    // 持久化
    persist: {
      storage: {
        getItem(key) {
          return uni.getStorageSync(key)
        },
        setItem(key, value) {
          uni.setStorageSync(key, value)
        },
      },
    },
  },
)
