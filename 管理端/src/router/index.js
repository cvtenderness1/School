import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', redirect:"/Login" },
    {path: '/admin',name:'admin',component:()=>import('../views/Manager.vue'),children:[
        {path: 'home', name: 'home',meta:{title:'主页'}, component: () => import('../views/home.vue')},
        {path:'test',name:'test',meta:{title:'测试'},component:()=>import('../views/test.vue')},
        {path:'list',name:'list',meta:{title:'管理员数据'},component:()=>import('../views/Data.vue')},
        {path:'userdata',name:'userdata',meta:{title:'用户数据'},component:()=>import('../views/UserData.vue')},
        {path:'rider',name:'rider',meta:{title:'骑手数据'},component:()=>import('../views/Rider.vue')},
        {path:'merchant',name:'merchant',meta:{title:'商家数据'},component:()=>import('../views/Merchant.vue')},
        {path:'order',name:'order',meta:{title:'订单'},component:()=>import('../views/order.vue')},
        {path:'Dashboard',name:'Dashboard',meta:{title:'订单看板'},component:()=>import('../views/Dashboard.vue')},
        {path:'FinanceSettlement',name:'FinanceSettlement',meta:{title:'结算管理'},component:()=>import('../views/FinanceSettlement.vue')},
        {path:'FinancePayment',name:'FinancePayment',meta:{title:'批量打款'},component:()=>import('../views/FinancePayment.vue')},
        {path:'FinanceInvoice',name:'FinanceInvoice',meta:{title:'开票管理'},component:()=>import('../views/FinanceInvoice.vue')},

    ]},
    {path:'/404',name:'404',meta:{title:'404'},component:()=>import('../views/404/404.vue')},
    {path:'/Login',name:'Login',meta:{title:'登录页面'},component:()=>import('../views/Login.vue')},
    {path:'/Register',name:'Register',meta:{title:'注册页面'},component:()=>import('../views/Register.vue')},
    { path: '/:pathMatch(.*)*', redirect: '/404' }
  ]

})
router.beforeEach((to, from, next) => {
    if (to.meta.title) {
        document.title = to.meta.title
    }

    const adminStr = localStorage.getItem("xm-pro-admin")
    let admin = null

    try {
        if (adminStr) admin = JSON.parse(adminStr)
    } catch (e) {}

    // 需要登录的页面
    if (to.path.startsWith("/admin")) {
        // 1. 没有登录信息
        if (!admin || !admin.nickname) {
            ElMessage.warning("请先登录！")
            return next("/Login")
        }

    }

    next()
})

export default router
