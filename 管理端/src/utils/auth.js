// 获取当前登录的管理员信息
export function getCurrentAdmin() {
    const adminStr = localStorage.getItem('xm-pro-admin')
    return adminStr ? JSON.parse(adminStr) : {}
}

// 判断是否是当前登录的管理员
export function isCurrentLoginAdmin(userId) {
    const currentAdmin = getCurrentAdmin()
    return currentAdmin.id && String(currentAdmin.id) === String(userId)
}