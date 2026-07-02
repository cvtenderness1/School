import { Component } from '@uni-helper/uni-app-types'

/** SKU 弹出层 */
export type SkuPopup = Component<SkuPopupProps>

/** SKU 弹出层实例 */
export type SkuPopupInstance = InstanceType<SkuPopup>

/** SKU 弹出层属性 */
export type SkuPopupProps = {
  modelValue: boolean
  localdata: SkuPopupLocaldata
  mode?: 1 | 2 | 3
  noStockText?: string
  stockText?: string
  maskCloseAble?: boolean
  borderRadius?: string | number
  minBuyNum?: number
  maxBuyNum?: number
  stepBuyNum?: number
  stepStrictly?: boolean
  hideStock?: boolean
  theme?: 'default' | 'red-black' | 'black-white' | 'coffee' | 'green'
  amountType?: 0 | 1
  customAction?: () => void
  showClose?: boolean
  closeImage?: string
  priceColor?: string
  buyNowText?: string
  buyNowColor?: string
  buyNowBackgroundColor?: string
  addCartText?: string
  addCartColor?: string
  addCartBackgroundColor?: string
  goodsThumbBackgroundColor?: string
  disableStyle?: object
  activedStyle?: object
  btnStyle?: object
  goodsIdName?: string
  skuIdName?: string
  skuListName?: string
  specListName?: string
  stockName?: string
  skuArrName?: string
  goodsThumbName?: string
  selectArr?: string[]

  onOpen: () => void
  onClose: () => void
  onAddCart: (event: SkuPopupEvent) => void
  onBuyNow: (event: SkuPopupEvent) => void
}

/** 商品信息本地数据源 */
export type SkuPopupLocaldata = {
  _id: string | number // 替换 goodsId
  name: string // 替换 goodsName
  goods_thumb: string // 替换 img
  spec_list: SkuPopupSpecItem[]
  sku_list: SkuPopupSkuItem[]
}

/** SKU 项 */
export type SkuPopupSkuItem = {
  _id: string | number // 替换 skuId
  goods_id: string | number // 替换 goodsId
  goods_name: string
  image: string
  price: number
  discountPrice: number
  sku_name_arr: string[]
  stock: number
}

// /** SKU 项 —— 适配你的后端 */
// export type SkuPopupSkuItem = {
//   skuId: string | number
//   goodsId: number
//   goodsName: string
//   image: string
//   price: number
//   discountPrice: number
//   sku_name_arr: string[]
//   stock: number
// }

export type SkuPopupEvent = SkuPopupSkuItem & {
  buy_num: number
}

/** 全局组件类型声明 */
declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    'vk-data-goods-sku-popup': SkuPopup
  }
}
