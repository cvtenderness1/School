import { http } from '@/utils/http'

// 获取骑手信息
export const getRiderProfileAPI = () => {
  return http({
    method: 'GET',
    url: '/rider/info',
  })
}

// 修改骑手信息
export const putRiderProfileAPI = (data: any) => {
  return http({
    method: 'PUT',
    url: '/rider/updates',
    data,
  })
}
