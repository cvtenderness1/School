import { useRiderStore } from '@/stores'

const baseURL = 'http://localhost:1000'

// 添加拦截器
const httpInterceptor = {
  invoke(options: UniApp.RequestOptions) {
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
    }
    options.timeout = 10000

    const riderStore = useRiderStore()
    const token = riderStore.profile?.token

    options.header = {
      ...options.header,
      token: token || '',
    }
  },
}

uni.addInterceptor('request', httpInterceptor)
uni.addInterceptor('uploadFile', httpInterceptor)

interface Data<T> {
  code: string
  msg: string
  data: T
}

// 封装请求
export const http = <T>(options: UniApp.RequestOptions) => {
  return new Promise<Data<T>>((resolve, reject) => {
    uni.request({
      ...options,
      success: (res) => {
        const data = res.data as Data<T>
        const { code, msg } = data
        if (code === '401') {
          useRiderStore().clearProfile()
          uni.navigateTo({ url: '/pages/login/rider' })
          reject(res)
          return
        }
        if (code === '200') {
          resolve(data)
        } else {
          uni.showToast({ title: msg || '请求失败', icon: 'none' })
          reject(res)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      },
    })
  })
}
