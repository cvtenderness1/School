import { http } from '@/utils/http'
import type { CategoryItem, GuessItem, HotItem } from '@/types/home'
import type { GoodsItem, PageParams, PageResult } from '@/types/global'
/**
 * 首页-前台分类-小程序
 */
export const getHomeCategoryAPI = () => {
  return http<CategoryItem[]>({
    method: 'GET',
    url: '/category/listAll',
  })
}
//热门推荐
export const getHomeHotAPI = () => {
  return http<HotItem[]>({
    method: 'GET',
    url: '/merchant/selectall',
  })
}

//猜你
export const getHomeGoodsGuessLikeAPI = (pageNum = 1, pageSize = 10) => {
  return http<PageResult<GuessItem>>({
    method: 'GET',
    url: '/goods/page',
    data: {
      pageNum,
      pageSize,
    },
  })
}
// 获取商家详情
export const getMerchantDetailAPI = (id: number) => {
  return http<HotItem>({
    method: 'GET',
    url: `/merchant/detail?id=${id}`,
  })
}

// 获取某个商家下的所有商品
export const getMerchantGoodsAPI = (merchantId: number) => {
  return http<GuessItem[]>({
    method: 'GET',
    url: `/merchant/goods?merchantId=${merchantId}`,
  })
}
