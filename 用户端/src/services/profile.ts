import type { ProfileDetail, ProfileParams } from '@/types/member'
import { http } from '@/utils/http'

/**
 * 获取个人信息（/user/list）
 */
export const getMemberProfileAPI = () => {
  return http<ProfileDetail>({
    method: 'GET',
    url: '/user/info',
  })
}

/**
 * 修改个人信息（ /user/update）
 * @param data 请求体参数
 */
export const putMemberProfileAPI = (data: ProfileParams) => {
  return http<ProfileDetail>({
    method: 'PUT',
    url: '/user/AppUpdate',
    data,
  })
}
