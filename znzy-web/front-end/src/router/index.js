import { createRouter, createWebHistory } from 'vue-router'
import {userUserStore} from "@/stores/userStore.js";
import { get } from "@/net/index.js";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '登录界面',
      component: () => import('@/components/Login.vue'),
    },
    {
      path:'/excel',
      name:'Excel',
      component: () => import('@/views/Excel.vue')
    },
    {
      path:'/ai',
      name:'AI',
      component: () => import('@/views/AI.vue')
    },
    {
      path:'/OnlineCompiler',
      name:'OnlineCompiler',
      component: () => import('@/components/OnlineCompiler.vue')
    },
    {
      path: '/Home',
      name: '主界面',
      component: () => import('@/components/Home.vue'),
      children: [
        {
          path: '/Home',
          name:'主页',
          component:()=>import('@/views/HomeSon.vue')
        },
        {
          path:'/CreateArticle',
          name:'发布文章',
          component:()=>import("@/views/CreateArticle.vue")
        },
        {
          path:'/My',
          name:'个人信息',
          component:()=>import("@/views/My.vue")
        },
        {
          path:'/view/:id',
          name:'查看文章',
          component:()=>import('@/views/ViewsArticle.vue')
        },
        {
          path: '/Article',
          name: '文章界面',
          component: () => import('@/views/Article.vue'),
        },
        {
          path:'/Plan',
          name:'计划界面',
          component:()=>import('@/views/Plan.vue')
        },
        {
          path:'/FileUpload',
          name:'文件上传',
          component:()=>import('@/views/FileUpload.vue')
        },
        {
          path:'/User',
          name:'用户页面',
          component:()=>import('@/views/User.vue')
        },
        {
          path:'/Manage',
          name:'用户管理',
          component:()=>import('@/components/Manage.vue')
        },
        {
          path:'/Notice',
          name:'通知页面',
          component:()=>import('@/views/Notice.vue')
        },
        {
          path:'/NoticeList',
          name:'通知列表',
          component:()=>import('@/views/NoticeList.vue')
        },
        {
          path:'/OpenAI',
          name:'AI智能助手',
          component:()=>import('@/components/OpenAI/Chat.vue')
        },
        {
          path:'/Chart',
          name:'可视化',
          component:()=>import('@/views/chart.vue')
        },
        {
          path:'/calendar',
          name:'日历',
          component:()=>import('@/views/calendar.vue')
        }
      ]
    },
  ],
})
router.beforeEach((to, from, next)=>
{
  const userStore = userUserStore()
  const publicMap = new Map()
  publicMap.set('/', 1)
//  publicMap.set('/share', 2)
  publicMap.set('/error/401', 3)
  publicMap.set('/error/404', 4)
  // 检查要访问的路径是否是根路径

  if (to.matched.length === 0) next('error/404')
  // 检查是否是分享页面
  // if (to.path.startsWith('/share/')) {
  //   next();
  //   return;
  // }
  if (!publicMap.has(to.path)) {
    // 不是访问根路径，检查用户状态
    const user = userStore.user; // 假设你的用户状态保存在Vuex的`user`状态中
    if (user === null) {
      get('api/user/information', {},
        (message, data) => {
          userStore.login(data);
          next();
        }, (message, data) => {
          next('/')
        }, (message, data) => {
          next('/')
        }
      )
    } else {
      // 用户已登录，允许路由继续
      next();
    }
  }
  else {
    // 访问的是根路径或其他公开路径，直接放行
    next();
  }
})
export default router
