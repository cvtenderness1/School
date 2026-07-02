import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
    baseURL: 'http://localhost:1000',
    timeout: 5000
})

// 请求拦截器
// 请求拦截器
request.interceptors.request.use(
    config => {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8'
        const token = localStorage.getItem('token')
        if (token && token !== 'undefined') { // 额外兜底，防止 undefined 写入
            config.headers['token'] = token
        }

        return config
    },
    error => {
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录失效，请重新登录');
            localStorage.removeItem('token');
            localStorage.removeItem('xm-pro-admin');
            router.push('/Login'); // 需要引入 router
        } else if (error.response.status === 404) {
            ElMessage.error('未找到请求接口');
        }
        return Promise.reject(error);
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        let res = response.data;
        if(typeof res === 'string') {
            res = res?JSON.parse(res):res
        }
        return res;
    },
    error => {
        if(error.response.status === 404) {
            ElMessage.error('未找到请求接口')

        }else if(error.response.status === 500) {
            ElMessage.error('系统异常')
        }else{
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request