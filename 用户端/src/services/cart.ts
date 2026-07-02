import type { CartItem } from '@/types/cart'
import { http } from '@/utils/http'

/**
 * 加入购物车
 */
export const postMemberCartAPI = (data: { skuId: number; count: number }) => {
  return http({
    method: 'POST',
    url: '/user/cart',
    data,
  })
}

/**
 * 获取购物车列表
 */
export const getMemberCartAPI = () => {
  return http<CartItem[]>({
    method: 'GET',
    url: '/user/cart',
  })
}

/**
 * 删除购物车
 */
export const deleteMemberCartAPI = (data: { ids: number[] }) => {
  return http({
    method: 'DELETE',
    url: '/user/cart',
    data,
  })
}

/**
 * 修改购物车单品
 */
export const putMemberCartBySkuIdAPI = (
  skuId: number,
  data: { selected?: boolean; count?: number; goodsId?: number },
) => {
  return http({
    method: 'PUT',
    url: `/user/cart/${skuId}`,
    data,
  })
}

/**
 * 全选/取消全选
 */
export const putMemberCartSelectedAPI = (data: { selected: boolean }) => {
  return http({
    method: 'PUT',
    url: '/user/cart/selected',
    data,
  })
}
