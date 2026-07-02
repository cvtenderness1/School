/** 骑手基础信息 */
type BaseRiderProfile = {
  riderId: number
  account: string
  name: string
  phone: string
  studentId: string
  status: number
}

/** 骑手登录返回 */
export type RiderLoginResult = BaseRiderProfile & {
  token: string
}

/** 骑手详情 */
export type RiderDetail = BaseRiderProfile & {}

/** 骑手修改参数 */
export type RiderParams = Pick<RiderDetail, 'name' | 'phone' | 'studentId'>

