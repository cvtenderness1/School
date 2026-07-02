import { useMemberStore } from '@/stores'

const baseURL = 'http://localhost:1000'

// 添加拦截器
const httpInterceptor = {
  // 拦截前触发
  invoke(options: UniApp.RequestOptions) {
    // 1. 非 http 开头需拼接地址
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
    }
    // 2. 请求超时，默认 60s（单位：毫秒）
    options.timeout = 10000
    const memberStore = useMemberStore()
    const token = memberStore.profile?.token
    options.header = {
      ...options.header,
      token: token || '',
    }

    console.log(options)
  },
}

uni.addInterceptor('request', httpInterceptor)
uni.addInterceptor('uploadFile', httpInterceptor)

interface Data<T> {
  code: string
  msg: string
  data: T
}

// 定义封装 uni.request 的 HTTP 请求工具函数
export const http = <T>(options: UniApp.RequestOptions) => {
  // 1. 返回 Promise 对象，以支持 async/await 语法
  return new Promise<Data<T>>((resolve, reject) => {
    uni.request({
      ...options,
      success: (res) => {
        const data = res.data as Data<T>
        const { code, msg } = data

        if (code === '500') {
          const memberstore = useMemberStore()
          memberstore.clearProfile()
          uni.showToast({
            title: msg || '登录已过期，请重新登录',
            icon: 'none',
            duration: 1500,
          })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 1500)
          reject(res)
          return
        }
        // 业务成功
        if (code === '200') {
          resolve(data)
        } else {
          // 其他业务错误
          uni.showToast({
            title: msg || '请求失败',
            icon: 'none',
          })
          reject(res)
        }
      },
      fail(err) {
        uni.showToast({
          title: '网络错误,换个网络试试',
          icon: 'none',
        })
        reject(err)
      },
    })
  })
}
