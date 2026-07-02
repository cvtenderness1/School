import type { GoodsItem } from './global'
/** 首页-前台类目数据类型 */
export type CategoryItem = {
  /** id */
  category_id: int
  /** 分类名称 */
  category_name: string
}
/** 首页-热门推荐数据类型 */
export type HotItem = {
  merchantId: number
  merchantName: string
  coverImg: string
  alt: string
  score: number
  postFee: number
  categoryName: string
  status: number // 1营业 0休息
}

/** 猜你喜欢-商品类型 */
export type GuessItem = GoodsItem
