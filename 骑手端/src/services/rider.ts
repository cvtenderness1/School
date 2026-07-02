import type { RiderLoginResult, RiderDetail } from '@/types/rider'
import { http } from '@/utils/http'

// 骑手账号密码登录参数
type RiderLoginParams = {
  account: string
  password: string
}
export interface RiderRegisterDTO {
  account: string
  password: string
  name: string
  phone: string
  studentId?: string
}
/** 骑手注册 */
export const postRiderRegisterAPI = (data: RiderRegisterDTO) => {
  return http<RiderLoginResult>({
    url: '/rider/register',
    method: 'POST',
    data,
  })
}
/**
 * 骑手账号密码登录
 */
export const postRiderLoginAPI = (data: RiderLoginParams) => {
  return http<RiderLoginResult>({
    method: 'POST',
    url: '/rider/login',
    data,
  })
}

/**
 * 骑手模拟快捷登录
 */
export const postRiderLoginSimpleAPI = (phoneNumber: string) => {
  return http<RiderLoginResult>({
    method: 'POST',
    url: '/rider/login/simple',
    data: { phoneNumber },
  })
}

/**
 * 获取骑手个人信息
 */
export const getRiderProfileAPI = () => {
  return http<RiderDetail>({
    method: 'GET',
    url: '/rider/info',
  })
}

/**
 * 修改骑手信息
 */
export const putRiderProfileAPI = (data: any) => {
  return http({
    method: 'PUT',
    url: '/rider/updates',
    data,
  })
}
// 获取骑手可抢订单列表
export const getRiderGrabListAPI = () => {
  return http({
    method: 'GET',
    url: '/rider/grabList',
  })
}

// 抢单接口也一起对齐
export const postRiderGrabOrderAPI = (orderId: string) => {
  return http({
    method: 'POST',
    url: '/rider/grab',
    data: { orderId },
  })
}
export const getRiderOrderDetailAPI = (orderId: string) => {
  return http({
    method: 'GET',
    url: `/rider/detail/${orderId}`,
  })
}
/**
 * 获取骑手订单列表
 * @param type 1待接单 2配送中 3已完成 4收入
 */
export const getRiderOrderListAPI = (type: string) => {
  return http({
    method: 'GET',
    url: `/rider/list?type=${type}`,
  })
}
export const updateOrderStatusAPI = (data: { orderId: string; orderStatus: number }) => {
  return http({
    method: 'PUT',
    url: '/rider/updateStatus',
    data,
  })
}
