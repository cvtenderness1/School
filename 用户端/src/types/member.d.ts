/** 通用的用户信息 */
type BaseProfile = {
  /** 用户ID */
  id: number
  /** 头像  */
  avatarUrl: string
  /** 账户名  */
  username: string
  /** 昵称 */
  nickname?: string
}

/** 小程序登录 登录用户信息 */
export type LoginResult = BaseProfile & {
  /** 手机号 */
  mobile: string
  /** 登录凭证 */
  token: string
}

/** 个人信息 用户详情信息 */
export type ProfileDetail = BaseProfile & {
  /** 性别 */
  gender?: Gender
  birthday?: string
  fullLocation?: string
  provinceCode?: string
  cityCode?: string
  countyCode?: string
}
/** 性别 */
export type Gender = '女' | '男'

/** 个人信息 修改请求体参数 */
export type ProfileParams = Pick<ProfileDetail, 'nickname' | 'gender' | 'birthday'> & {
  avatarUrl?: string
  /** 省份编码 */
  provinceCode?: string
  /** 城市编码 */
  cityCode?: string
  /** 区/县编码 */
  countyCode?: string
}
