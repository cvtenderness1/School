/** 订单状态枚举 */
export enum OrderState {
  /** 待付款 */
  DaiFuKuan = 0,
  /** 待接单 */
  DaiJieDan = 1,
  /** 待配送 */
  DaiPeiCang = 2,
  /** 配送中 */
  PeiSongZhong = 3,
  /** 已完成 */
  YiWanCheng = 4,
  /** 已取消 */
  YiQuXiao = 5,
}
/** 订单状态列表 */
export const orderStateList = [
  { id: 0, text: '待付款' },
  { id: 1, text: '待接单' },
  { id: 2, text: '待配送' },
  { id: 3, text: '配送中' },
  { id: 4, text: '已完成' },
  { id: 5, text: '已取消' },
]
