import type { LoginResult } from '@/types/member'
import { http } from '@/utils/http'

type LoginWxMinParams = {
  code: string
  encryptedData?: string
  iv?: string
}
/**
 * 小程序登录
 * @param data 请求参数
 */
export const postLoginWxMinAPI = (data: LoginWxMinParams) => {
  return http<LoginResult>({
    method: 'POST',
    url: '/wx/login/wxMin',
    data,
  })
}

/**
 * 小程序登录_内测版
 * @param phoneNumber 模拟手机号码
 */
export const postLoginWxMinSimpleAPI = (phoneNumber: string) => {
  return http<LoginResult>({
    method: 'POST',
    url: '/wx/login/wxMin/simple',
    data: {
      phoneNumber,
    },
  })
}

type LoginParams = {
  account: string
  password: string
}
/**
 * 传统登录-用户名+密码
 * @param data 请求参数
 */
export const postLoginAPI = (data: LoginParams) => {
  return http<LoginResult>({
    method: 'POST',
    url: '/wx/login',
    data,
  })
}
export const postRegisterAPI = (data: { account: string; password: string }) => {
  return http<LoginResult>({
    method: 'POST',
    url: '/wx/register', // 这里改成你后端实际的注册接口地址
    data,
  })
}
