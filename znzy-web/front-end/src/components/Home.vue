<script setup>
import {ref, reactive, onMounted, onUnmounted, computed, watch} from 'vue';
import { get } from "@/net/index.js";
import router from "@/router/index.js";
import { useRoute } from 'vue-router';
import {message} from "ant-design-vue";
import { useNotification } from '@/services/notificationService'
import TopNotificationBar from '@/components/TopNotificationBar.vue'
import webSocketService from '@/net/websocket'

const [messageApi,contextHolder]=message.useMessage();
// 状态管理
const isDark = ref(false);
const sidebarHovered = ref(false);
const showUserMenu = ref(false);
const showTopMenu = ref(false);
const topMenuTimeout = ref(null);


// 通知相关 - 正确使用 useNotification
const notificationService = useNotification()
const {
  activeNotifications,
  removeNotification,
  addNotification,  // 直接解构出 addNotification
  initNotificationService
} = notificationService

// 处理通知点击事件
const handleNotificationClick = (notification) => {
  console.log('通知被点击:', notification)
  // 根据通知类型进行不同的跳转或操作
  if (notification.type === 'assignment') {
    router.push('/assignments')
  } else if (notification.type === 'exam') {
    router.push('/exams')
  } else if (notification.type === 'system') {
    router.push('/notifications')
  }
  // 可以在这里添加更多的类型处理逻辑
}

// WebSocket 处理器 ID
const wsHandlerId = 'sidebar-notifications'

// 初始化通知服务
const initSidebarNotifications = () => {
  // 先注销可能存在的旧处理器，避免重复注册
  webSocketService.unregisterHandler(wsHandlerId)

  const handlers = {
    // 连接建立
    onConnected: () => {
      console.log('侧边栏WebSocket连接成功')
      // 连接成功后立即尝试获取最新消息
      setTimeout(() => {
        getLatestNotice()
      }, 500)
    },

    // 连接断开
    onDisconnected: () => {
      console.log('侧边栏WebSocket连接断开')
    },

    // 新消息通知
    new_notice: (noticeData) => {
      console.log('收到新通知:', noticeData)

      // 显示新发布的消息
      showLatestNotice(noticeData, '新消息发布')
    }
  }

  webSocketService.registerHandler(wsHandlerId, handlers)

  // 检查连接状态，如果未连接则尝试连接
  if (!webSocketService.isConnected) {
    console.log('尝试建立WebSocket连接...')
    webSocketService.connect()
  }
}

// 显示最新消息 - 修复引用问题
const showLatestNotice = (noticeData, type = '最新消息') => {
  console.log('🔔 准备显示最新消息:', noticeData)

  const notification = {
    id: noticeData.id || Date.now(),
    title: `${type}: ${noticeData.title}`,
    content: noticeData.content,
    type: noticeData.type,
    priority: noticeData.priority,
    time: noticeData.time || new Date().toISOString(),
    userId: noticeData.userId,
    publisher: noticeData.publisher || '系统'
  }

  console.log('📨 创建的通知对象:', notification)

  // 使用正确的引用 ✅
  addNotification(notification)
  console.log('✅ 通知已添加到队列，当前活跃通知数量:', activeNotifications.value.length)

  // 播放提示音
  playNotificationSound()

  // 显示桌面通知
  showDesktopNotification(notification)
}

// 播放通知提示音
const playNotificationSound = () => {
  try {
    const audio = new Audio('data:audio/wav;base64,UklGRigAAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQQAAAAAAA==')
    audio.volume = 0.2
    audio.play().catch(() => {
      // 忽略播放错误
    })
  } catch (error) {
    console.log('播放提示音失败:', error)
  }
}

// 显示桌面通知
const showDesktopNotification = (notification) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(notification.title, {
      body: notification.content,
      icon: '/favicon.ico',
      tag: 'latest-notice'
    })
  }
}

// 请求通知权限
const requestNotificationPermission = () => {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        console.log('通知权限已授予')
      }
    })
  }
}

// 获取最新发布的一条消息 - 添加详细日志
const getLatestNotice = async () => {
  try {
    console.log('🔍 开始获取最新消息...')

    const allNotices = await new Promise((resolve, reject) => {
      get('api/notice/SelectAllNotice', {}, (message, data) => {
        console.log('📡 API响应:', message)
        console.log('📦 响应数据长度:', data?.length || 0)
        resolve(data || [])
      }, (error) => {
        console.error('❌ API错误:', error)
        reject(error)
      })
    })

    console.log('📋 获取到的消息数组长度:', allNotices.length)

    if (Array.isArray(allNotices) && allNotices.length > 0) {
      const latestNotice = allNotices[allNotices.length - 1]
      console.log('⭐ 最新消息:', latestNotice.title)

      // 显示最新消息提示
      showLatestNotice(latestNotice, '最新公告')

      console.log('✅ 已触发显示最新消息')
    } else {
      console.log('ℹ️ 暂无消息')
    }

  } catch (error) {
    console.log('❌ 获取消息列表失败:', error)
  }
}


// 数据
const options = reactive({
  user: {
    id:'',
    email: '',
    avatar: '',
    account: '',
    role:'游客',
  }
});
const popularPosts = ref([]);
const tags = ref([
  'Vue', 'JavaScript', 'CSS', 'Tailwind',
  'Web Development', 'Design', 'UX', 'Frontend'
]);

// 方法
const GetCurrentUser = () => {
  get('api/user/current', {},
    (message, data) => {
      console.log("✅ 用户登录成功:", data.account)
      options.user = data

      // 用户信息获取成功后，初始化通知服务
      initNotificationService()
      initSidebarNotifications()
      requestNotificationPermission()

      // 保留直接获取最新消息的逻辑，作为WebSocket的补充
      setTimeout(() => {
        console.log('⏰ 延迟执行获取最新消息')
        getLatestNotice()
      }, 1000)
    },
    (error) => {
      console.error("获取用户信息失败:", error)
      options.user = { name: '未知用户', role: 'guest' }
    }
  )
};

const getTopArticles = () => {
  get('api/article/getTopArticles', {}, (message, data) => {
    popularPosts.value = data;
  });
};

const handleTopMenuEnter = () => {
  clearTimeout(topMenuTimeout.value);
  showTopMenu.value = true;
};

const handleTopMenuLeave = () => {
  topMenuTimeout.value = setTimeout(() => {
    showTopMenu.value = false;
  }, 200);
};

const toggleTheme = () => {
  isDark.value = !isDark.value;
  localStorage.setItem('darkMode', isDark.value);
};

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  router.push('/');
};

const switchAccount = () => {
  router.push('/');
};

const goToProfile = () => {
  router.push('/My');
  showUserMenu.value = false;
};
//禁止游客访问

router.beforeEach((to, from, next) => {
  if ((to.path === '/My' || to.path === '/CreateArticle') && options.user.role === '游客') {
    messageApi.warning('您当前身份为游客，无法访问');
    next(false);          // 阻止跳转
  } else {
    next();
  }
});

//多级导航栏
const activeNavItem = ref('Home');
const currentParent = ref(null);//当前选中的父级菜单
const navSections=ref([
  {
    label:'主页',
    items:[
      {
        id:'Home',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"></path>
                </svg>`,
        text:'主页',
        roles:['管理员','普通用户','游客'],
        path:'/Home'
      }
    ]
  },
  {
   label:'有关文章',
    items:[
      {
        id:'CreateArticle',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                </svg>`,
        text:'发布文章',
        roles:['管理员','普通用户'],
        path:'/CreateArticle'
      },
      {
        id:'Article',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"></path>
                </svg>`,
        text:'文章列表',
        roles:['管理员','普通用户','游客'],
        path:'/Article'
      }
    ]
  },
  {
    label:'通知',
    items:[
      {
        id:'Notice',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                </svg>`,
        text:'发布通知',
        roles:['管理员'],
        path:'/Notice'
      },
      {
        id:'NoticeList',
        icon:` <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
                </svg>`,
        text:'通知列表',
        roles:['管理员','普通用户','游客'],
        path:'/NoticeList'
      }
    ]
  },
  {
    label:'个人中心',
    items:[
      {
        id:'My',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>`,
        text:'个人中心',
        roles:['管理员','普通用户'],
        path:'/My'
      }
    ]
  },
  {
    label:'实用工具',
    items:[
      {
        id:'Plan',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>`,
        text:'备忘录',
        roles:['管理员','普通用户'],
        path:'/Plan'
      },
      {
        id:'OpenAI',
        icon:` <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
                </svg>`,
        text:'AI助手',
        roles:['管理员','普通用户'],
        path:'/OpenAI'
      },
      {
        id:'Chart',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                </svg>`,
        text:'数据统计',
        roles:['管理员','普通用户'],
        path:'/Chart'
      },
      {
        id:'calendar',
        icon:`<svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2
                  2v12a2 2 0 002 2z"></path>
                  </svg>`,
        text:'日程管理',
        roles:['管理员','普通用户'],
        path:'/calendar'
      }
    ]
  }
])
//计算当前显示的菜单列表
const activeMenuList=computed(()=>{
  if(currentParent.value){
    // 如果进入了某个section，显示该section的items（根据角色过滤）
    return currentParent.value.items.filter(item => 
      item.roles.includes(options.user.role)
    )
  }
  // 否则显示所有section的列表
  return navSections.value
})

// 点击section（父菜单）时进入该section
const setActiveSection = (section) => {
  currentParent.value = section
}

//设置激活的导航项（点击item时）
const setActiveMenu = (item) => {
  activeNavItem.value = item.id
  if(item.path){
    // 如果有路径，则跳转
    router.push(item.path)
  }
}

//返回上一级菜单
const goBack=()=>{
  currentParent.value=null
}

//检查是否为当前激活项
const isActiveItem=(item)=>{
  return activeNavItem.value === item.id
}

// 检查用户是否有权限访问该菜单
const hasPermission = (item) => {
  return item.roles && item.roles.includes(options.user.role)
}

// 监听路由变化，更新激活的菜单项
const route = useRoute()
watch(() => route.path, (newPath) => {
  // 遍历所有section的items，找到匹配的路径
  for (const section of navSections.value) {
    for (const item of section.items) {
      if (item.path === newPath) {
        activeNavItem.value = item.id
        break
      }
    }
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  GetCurrentUser();
  getTopArticles();

  const savedTheme = localStorage.getItem('darkMode');
  if (savedTheme !== null) {
    isDark.value = savedTheme === 'true';
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  document.addEventListener('click', (event) => {
    const target = event.target;
    if (!target.closest('.user-menu-container')) {
      showUserMenu.value = false;
    }
  });
});

onUnmounted(() => {
  // 清理WebSocket资源，避免内存泄漏
  webSocketService.unregisterHandler(wsHandlerId)
  // 注意：不要在这里调用disconnect()，因为可能有其他组件也在使用WebSocket
})
</script>

<template>
  <contextHolder />
  <div :class="{'dark': isDark}" class="min-h-screen transition-colors duration-500">
    <!-- dummy div 强制 Tailwind 生成动画类 -->
    <div class="hidden opacity-0 opacity-100 -translate-y-4 translate-y-0 transition-all transition-opacity duration-150 duration-200 duration-300 ease-in ease-out transform"></div>

    <!-- 顶部悬停区域 -->
    <div
      class="fixed top-0 left-0 right-0 h-4 z-50"
      @mouseenter="handleTopMenuEnter"
      @mouseleave="handleTopMenuLeave"
    ></div>
    <!-- 顶部通知栏容器 - 类似移动端QQ和微信的顶部消息提示 -->
    <div class="top-notifications-container">
      <TopNotificationBar
        v-for="notification in activeNotifications"
        :key="notification.id"
        :notification="notification"
        :duration="5000"
        @close="removeNotification"
        @click="handleNotificationClick"
      />
    </div>
    <!-- 高级下拉菜单 -->
    <div
      v-show="showTopMenu"
      class="fixed top-0 left-0 right-0 z-40 pt-4 transition-all duration-500 ease-out animate-fade-in"
      :class="showTopMenu ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-4'"
      @mouseenter="handleTopMenuEnter"
      @mouseleave="handleTopMenuLeave"
    >
      <div class="bg-white/95 dark:bg-gray-800/95 backdrop-blur-xl shadow-modern rounded-b-2xl mx-4 overflow-hidden border border-white/30 dark:border-gray-700/50">
        <div class="max-w-7xl mx-auto px-6 py-8">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <!-- 快捷导航 -->
            <div class="animate-fadeIn" style="animation-delay: 0.1s">
              <h3 class="text-sm font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-4">快捷导航</h3>
              <ul class="space-y-2">
                <li>
                  <router-link
                    to="/Home"
                    class="flex items-center px-4 py-2.5 rounded-lg transition-all duration-300 text-gray-700 dark:text-gray-300 hover:bg-gray-100/80 dark:hover:bg-dark-700/80 hover:text-primary dark:hover:text-primary"
                  >
                    <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
                    </svg>
                    主页
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/Article"
                    class="flex items-center px-4 py-2.5 rounded-lg transition-all duration-300 text-gray-700 dark:text-gray-300 hover:bg-gray-100/80 dark:hover:bg-dark-700/80 hover:text-primary dark:hover:text-primary"
                  >
                    <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"></path>
                    </svg>
                    查看文章
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/CreateArticle"
                    class="flex items-center px-4 py-2.5 rounded-lg transition-all duration-300 text-gray-700 dark:text-gray-300 hover:bg-gray-100/80 dark:hover:bg-dark-700/80 hover:text-primary dark:hover:text-primary"
                  >
                    <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                    发布文章
                  </router-link>
                </li>
              </ul>
            </div>

            <!-- 热门文章 -->
            <div class="animate-fadeIn" style="animation-delay: 0.2s">
              <h3 class="text-sm font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-4">热门文章</h3>
              <ul class="space-y-2">
                <li v-for="(post, index) in popularPosts.slice(0, 3)" :key="index">
                  <router-link
                    :to="`/article/${post.id}`"
                    class="group flex items-start px-4 py-2.5 rounded-lg transition-all duration-300 hover:bg-gray-100/80 dark:hover:bg-dark-700/80"
                  >
                    <span class="flex-shrink-0 mt-1.5 w-2 h-2 bg-primary rounded-full opacity-0 group-hover:opacity-100 transition-all duration-300 transform group-hover:scale-125"></span>
                    <span class="ml-3 text-sm font-medium text-gray-700 dark:text-gray-300 group-hover:text-primary dark:group-hover:text-primary transition-colors duration-300">
                      {{ post.title }}
                      <span class="block text-xs text-gray-500 dark:text-gray-400 mt-1">{{ post.date }}</span>
                    </span>
                  </router-link>
                </li>
              </ul>
            </div>

            <!-- 标签云 -->
            <div class="animate-fadeIn" style="animation-delay: 0.3s">
              <h3 class="text-sm font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-4">热门标签</h3>
              <div class="flex flex-wrap gap-2">
                <router-link
                  v-for="(tag, index) in tags.slice(0, 8)"
                  :key="index"
                  to="#"
                  class="px-3 py-1.5 text-xs font-medium rounded-full transition-all duration-300 bg-gray-100 dark:bg-dark-700 hover:bg-primary hover:text-white dark:hover:bg-primary transform hover:scale-105"
                >
                  {{ tag }}
                </router-link>
              </div>
            </div>

            <!-- 用户快捷操作 -->
            <div class="animate-fadeIn" style="animation-delay: 0.4s">
              <h3 class="text-sm font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-4">快捷操作</h3>
              <ul class="space-y-2">
                <li>
                  <button
                    @click="goToProfile"
                    class="w-full text-left group flex items-center px-4 py-2.5 rounded-lg transition-all duration-300 text-gray-700 dark:text-gray-300 hover:bg-gray-100/80 dark:hover:bg-gray-700/80 hover:text-primary dark:hover:text-primary"
                  >
                    <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                    个人资料
                  </button>
                </li>
                <li>
                  <button
                    @click="toggleTheme"
                    class="w-full text-left flex items-center px-4 py-2.5 rounded-lg transition-all duration-300 text-gray-700 dark:text-gray-300 hover:bg-gray-100/80 dark:hover:bg-gray-700/80 hover:text-primary dark:hover:text-primary"
                  >
                    <svg v-if="isDark" class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
                    </svg>
                    <svg v-else class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
                    </svg>
                    {{ isDark ? '亮色模式' : '暗色模式' }}
                  </button>
                </li>
                <li>
                  <button
                    @click="logout"
                    class="w-full text-left flex items-center px-4 py-2.5 rounded-lg transition-all duration-300 text-gray-700 dark:text-gray-300 hover:bg-gray-100/80 dark:hover:bg-gray-700/80 hover:text-red-500 dark:hover:text-red-400"
                  >
                    <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                    </svg>
                    退出登录
                  </button>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 管理员功能 -->
        <div v-if="options.user.id===1" class="px-6 pb-8 animate-fadeIn" style="animation-delay: 0.3s">
          <h3 class="text-sm font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-4 flex items-center">
            <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
            </svg>
            管理员功能
          </h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <!-- 内容管理 -->
            <button class="group relative p-3 rounded-xl transition-all duration-300 bg-gradient-to-br from-primary/10 to-tertiary/10 dark:from-primary/20 dark:to-tertiary/20 hover:from-primary/20 hover:to-tertiary/20 dark:hover:from-primary/30 dark:hover:to-tertiary/30 border border-primary/50 dark:border-primary/50 shadow-sm hover:shadow-modern transform hover:-translate-y-1 animate-fadeIn" style="animation-delay: 0.1s">
              <div class="flex flex-col items-center text-center">
                <div class="w-10 h-10 mb-2 flex items-center justify-center rounded-lg bg-primary text-white group-hover:bg-primary/90 transition-colors">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path>
                  </svg>
                </div>
                <span class="text-xs font-medium text-primary dark:text-primary">内容管理</span>
              </div>
            </button>

            <!-- 用户管理 -->
            <button @click="router.push('/Manage')" class="group relative p-3 rounded-xl transition-all duration-300 bg-gradient-to-br from-secondary/10 to-emerald-100 dark:from-secondary/20 dark:to-emerald-900/20 hover:from-secondary/20 hover:to-emerald-200 dark:hover:from-secondary/30 dark:hover:to-emerald-800/30 border border-secondary/50 dark:border-secondary/50 shadow-sm hover:shadow-modern transform hover:-translate-y-1 animate-fadeIn" style="animation-delay: 0.2s">
              <div class="flex flex-col items-center text-center">
                <div class="w-10 h-10 mb-2 flex items-center justify-center rounded-lg bg-secondary text-white group-hover:bg-secondary/90 transition-colors">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
                  </svg>
                </div>
                <span class="text-xs font-medium text-secondary dark:text-secondary">用户管理</span>
              </div>
            </button>

            <!-- 系统设置 -->
            <button class="group relative p-3 rounded-xl transition-all duration-300 bg-gradient-to-br from-tertiary/10 to-violet-100 dark:from-tertiary/20 dark:to-violet-900/20 hover:from-tertiary/20 hover:to-violet-200 dark:hover:from-tertiary/30 dark:hover:to-violet-800/30 border border-tertiary/50 dark:border-tertiary/50 shadow-sm hover:shadow-modern transform hover:-translate-y-1 animate-fadeIn" style="animation-delay: 0.3s">
              <div class="flex flex-col items-center text-center">
                <div class="w-10 h-10 mb-2 flex items-center justify-center rounded-lg bg-tertiary text-white group-hover:bg-tertiary/90 transition-colors">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  </svg>
                </div>
                <span class="text-xs font-medium text-tertiary dark:text-tertiary">系统设置</span>
              </div>
            </button>

            <!-- 数据统计 -->
            <button class="group relative p-3 rounded-xl transition-all duration-300 bg-gradient-to-br from-accent/10 to-amber-100 dark:from-accent/20 dark:to-amber-900/20 hover:from-accent/20 hover:to-amber-200 dark:hover:from-accent/30 dark:hover:to-amber-800/30 border border-accent/50 dark:border-accent/50 shadow-sm hover:shadow-modern transform hover:-translate-y-1 animate-fadeIn" style="animation-delay: 0.4s">
              <div class="flex flex-col items-center text-center">
                <div class="w-10 h-10 mb-2 flex items-center justify-center rounded-lg bg-accent text-white group-hover:bg-accent/90 transition-colors">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                  </svg>
                </div>
                <span class="text-xs font-medium text-accent dark:text-accent">数据统计</span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100 min-h-screen">
      <!-- 导航栏 -->
      <nav class="bg-white dark:bg-gray-800 shadow-lg sticky top-0 z-10">
        <div class="container mx-auto px-4 py-3 flex justify-between items-center">
          <div class="flex items-center space-x-2">
            <!-- 头像下拉菜单 -->
            <div class="relative user-menu-container">
              <img
                :src="options.user.avatar"
                alt="头像"
                class="w-8 h-8 rounded-full cursor-pointer hover:ring-2 hover:ring-blue-500 transition-all duration-200"
                @click="toggleUserMenu"
              >
              <!-- 下拉菜单 -->
              <div
                v-show="showUserMenu"
                class="absolute left-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-[100] transition-all duration-200 animate-scale-in"
                :class="showUserMenu ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-2'"
                @click.stop
              >
                <!-- 用户信息 -->
                <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700">
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ options.user.account }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ options.user.email }}</p>
                </div>
                <!-- 菜单项 -->
                <div class="py-1">
                  <button
                    @click="goToProfile"
                    class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center"
                  >
                    <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                    个人信息
                  </button>
                  <button
                    @click="switchAccount"
                    class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center"
                  >
                    <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    切换账号
                  </button>
                  <hr class="my-1 border-gray-200 dark:border-gray-700">
                  <button
                    @click="logout"
                    class="w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center"
                  >
                    <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                    </svg>
                    退出登录
                  </button>
                </div>
              </div>
            </div>
            <span class="text-xl font-bold">{{ options.user.account }}</span>
          </div>
          <div class="hidden md:flex space-x-8">
            <router-link to="/Home" class="hover:text-primary transition">主页</router-link>
            <router-link to="/Article" class="hover:text-primary transition">查看文章</router-link>
            <router-link to="/My" class="hover:text-primary transition">个人信息</router-link>
            <router-link to="/CreateArticle" class="hover:text-primary transition">发布文章</router-link>
          </div>
          <div class="flex items-center space-x-4">
            <button @click="toggleTheme" class="p-2 rounded-full focus:outline-none">
              <svg v-if="isDark" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
              </svg>
              <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
              </svg>
            </button>
          </div>
        </div>
      </nav>
      <main>
        <RouterView />
      </main>
      <footer class="dark:bg-gray-800 dark:text-gray-300 py-12">
        <div class="container mx-auto px-4">
          <div class="border-t border-gray-700 mt-8 pt-8 text-center">
            <p>&copy; {{ new Date().getFullYear() }} 个人网站. 总会落叶.</p>
          </div>
        </div>
      </footer>
      <!-- 左边栏 -->
      <div
        class="fixed left-0 top-0 h-full bg-white dark:bg-gray-800 shadow-lg z-10 transition-all duration-300 ease-in-out"
        :style="{ width: sidebarHovered ? '320px' : '64px' }"
        @mouseenter="sidebarHovered = true"
        @mouseleave="sidebarHovered = false"
      >
        <div class="h-full overflow-y-auto scrollbar-hide">
          <div v-if="!sidebarHovered"
               class="absolute right-0 top-1/2 transform -translate-y-1/2 translate-x-8 bg-blue-500 text-white p-2 rounded-r-lg cursor-pointer hover:bg-blue-600 transition-colors"
               @mouseenter="sidebarHovered = true">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </div>
          <div class="p-6 space-y-8 transition-opacity duration-300" :class="sidebarHovered ? 'opacity-100' : 'opacity-0'">
            <!-- 用户信息卡片 -->
            <div class="backdrop-blur-md bg-gradient-to-r from-primary/80 to-tertiary/80 rounded-lg p-4 text-white shadow-modern animate-fadeIn">
              <div class="flex items-center space-x-4">
                <img :src="options.user.avatar" alt="用户头像" @click="toggleUserMenu" class="w-12 h-12 rounded-full border-2 border-white cursor-pointer hover:ring-2 hover:ring-blue-500 transition-all duration-200">
                <div>
                  <h3 class="font-semibold">用户名：{{ options.user.account }}</h3>
                  <p class="text-sm opacity-80">邮箱：{{ options.user.email }}</p>
                </div>
              </div>
            </div>

            <nav class="space-y-2">
              <!-- 返回按钮 - 当进入子菜单时显示 -->
              <div v-if="currentParent" class="mb-4">
                <button 
                  @click="goBack" 
                  class="w-full flex items-center px-4 py-3 rounded-lg transition-colors duration-200 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 hover:text-primary dark:hover:text-primary"
                >
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                  </svg>
                  返回
                </button>
              </div>

              <!-- 显示section列表（父菜单） -->
              <template v-if="!currentParent">
                <button
                  v-for="(section, index) in activeMenuList"
                  :key="index"
                  @click="setActiveSection(section)"
                  class="w-full flex items-center px-4 py-3 rounded-lg transition-colors duration-200 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 hover:text-primary dark:hover:text-primary"
                >
                  <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                  </svg>
                  {{ section.label }}
                  <svg class="w-4 h-4 ml-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                  </svg>
                </button>
              </template>

              <!-- 显示item列表（子菜单） -->
              <template v-else>
                <template v-for="item in activeMenuList" :key="item.id">
                  <button
                    v-if="hasPermission(item)"
                    @click="setActiveMenu(item)"
                    class="w-full flex items-center px-4 py-3 rounded-lg transition-colors duration-200"
                    :class="{'bg-primary/10 dark:bg-primary/20 text-primary dark:text-primary': $route.path === item.path, 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 hover:text-primary dark:hover:text-primary': $route.path !== item.path}"
                  >
                    <span v-html="item.icon"></span>
                    {{ item.text }}
                  </button>
                </template>
              </template>
            </nav>
            <!-- 热门文章 -->
            <div>
              <h3 class="text-lg font-semibold mb-4 text-gray-700 dark:text-gray-300">热门文章</h3>
              <div class="space-y-3">
                <div v-for="(post, index) in popularPosts" :key="index" class="group relative p-4 rounded-lg transition-all duration-200 cursor-pointer bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700/50">
                  <div class="flex items-start space-x-4">
                    <div class="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-primary to-tertiary rounded-lg flex items-center justify-center text-white shadow-sm">
                      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"></path>
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <h4 class="font-medium text-sm group-hover:text-primary dark:group-hover:text-primary transition-colors duration-200 truncate inline-block relative">
                        <span class="absolute -top-1 left-0 w-0 h-[2px] bg-primary dark:bg-primary transition-all duration-300 group-hover:w-full"></span>
                        {{ post.title }}
                      </h4>
                      <div class="flex items-center mt-2 text-xs text-gray-500 dark:text-gray-400">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                        {{ post.date }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- 标签云 -->
            <div>
              <h3 class="text-lg font-semibold mb-4 text-gray-700 dark:text-gray-300">标签</h3>
              <div class="flex flex-wrap gap-2">
                <span v-for="(tag, index) in tags" :key="index" class="group relative px-3 py-1.5 rounded-lg text-sm transition-all duration-200 cursor-pointer inline-block bg-gray-100 dark:bg-gray-700 hover:text-primary dark:hover:text-primary animate-fadeIn" style="animation-delay: 0.1s * index">
                  <span class="absolute -top-1 left-0 w-0 h-[2px] bg-primary dark:bg-primary transition-all duration-300 group-hover:w-full"></span>
                  {{ tag }}
                </span>
              </div>
            </div>
            <!-- 主题切换 -->
            <button @click="toggleTheme" class="w-full flex items-center justify-center px-4 py-3 rounded-lg transition-colors duration-200 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600">
              <svg v-if="isDark" class="w-5 h-5 mr-3 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
              </svg>
              <svg v-else class="w-5 h-5 mr-3 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
              </svg>
              {{ isDark ? '切换到亮色模式' : '切换到暗色模式' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 滚动条隐藏 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 导航链接动画 */
nav a,
nav button {
  position: relative;
  overflow: hidden;
}
nav a::after,
nav button::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: currentColor;
  transform: scaleX(0);
  transition: transform 0.8s ease;
}
nav a:hover::after,
nav button:hover::after {
  transform: scaleX(1);
}

/* 顶部悬停区域 */
.fixed.h-4 {
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), transparent);
  cursor: pointer;
}

/* 背景模糊效果 */
.backdrop-blur-xl {
  backdrop-filter: blur(12px);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out forwards;
}

.animate-scale-in {
  animation: scaleIn 0.2s ease-out forwards;
}

/* 现代化阴影 */
.shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 暗色模式下的现代化阴影 */
.dark .shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}
 :root {
   --primary: #3b82f6;
   --secondary: #10b981;
   --tertiary: #8b5cf6;
   --accent: #f59e0b;
 }

.dark {
  --primary: #60a5fa;
  --secondary: #34d399;
  --tertiary: #a78bfa;
  --accent: #fbbf24;
}
</style>
