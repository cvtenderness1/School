/** 通用分页结果类型 */
export type PageResult<T> = {
  /** 列表数据 */
  list: T[]
  /** 总条数 */
  total: number
  /** 当前页数 */
  pageNum: number
  /** 总页数 */
  pages: number
  /** 每页条数 */
  pageSize: number
  /** 是否有下一页 */
  hasNextPage: boolean
  /** 是否有上一页 */
  hasPreviousPage: boolean
}

/** 通用分页参数类型 */
export type PageParams = {
  /** 页码：默认值为 1 */
  pageNum?: number
  /** 页大小：默认值为 10 */
  pageSize?: number
}

/** 通用商品类型 */
export type GoodsItem = {
  /** 商品描述 */
  desc: string
  /** 商品折扣 */
  discountPrice: number
  merchantId: number
  /** id */
  goodsId: number
  categoryId: number
  /** 商品名称 */
  goodsName: string
  /** 商品库存数量 */
  stock: number
  /** 商品图片 */
  img: string
  /** 商品价格 */
  price: number
  status: number
}

/** 商品规格项（如：颜色） */
export type GoodsSpec = {
  id: number
  goodsId: number
  name: string
  list: GoodsSpecItem[]
}

/** 规格值（如：红色、XL） */
export type GoodsSpecItem = {
  id: number
  specId: number
  name: string
}

/** 商品 SKU 信息 */
export type GoodsSku = {
  id: number
  goodsId: number
  skuNameArr: string
  image: string
  price: number
  discountPrice: number
  stock: number
}

/** 后端返回：商品完整 SKU 数据（规格 + SKU 列表） */
export type GoodsSkuData = {
  specList: GoodsSpec[]
  skuList: GoodsSku[]
}
