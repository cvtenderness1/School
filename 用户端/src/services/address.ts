import type { AddressItem, AddressParams } from '@/types/address'
import { http } from '@/utils/http'

/**
 * 添加收货地址
 * @param data 请求参数
 */
export const postMemberAddressAPI = (data: AddressParams) => {
  return http({
    method: 'POST',
    url: '/user/address',
    data,
  })
}

/**
 * 获取收货地址列表
 */
export const getMemberAddressAPI = () => {
  return http<AddressItem[]>({
    method: 'GET',
    url: '/user/address',
  })
}

/**
 * 获取收货地址详情
 * @param id 地址id(路径参数)
 */
export const getMemberAddressByIdAPI = (id: string) => {
  return http<AddressItem>({
    method: 'GET',
    url: `/user/address/${id}`,
  })
}

/**
 * 修改收货地址
 * @param id 地址id(路径参数)
 * @param data 表单数据(请求体参数)
 */
export const putMemberAddressByIdAPI = (id: string, data: AddressParams) => {
  return http({
    method: 'PUT',
    url: `/user/address/${id}`,
    data,
  })
}

/**
 * 删除收货地址
 * @param id 地址id(路径参数)
 */
export const deleteMemberAddressByIdAPI = (id: string) => {
  return http({
    method: 'DELETE',
    url: `/user/address/${id}`,
  })
}
