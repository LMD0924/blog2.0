<script setup>
import {onMounted, ref, watch} from 'vue'
import {get, post} from '@/net/index.js'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'

const [messageApi, contextHolder] = message.useMessage()
const route = useRoute()

// 获取路由参数中的作者ID（支持路径参数和查询参数）
const authorId = ref(route.params.id || route.query.id || 1)

// 进入动画控制
const isMounted = ref(false)

// 作者信息状态
const authorInfo = ref({
  id: null,
  account: '',
  avatar: '',
  email: '',
  role: '',
  follow: 0,
  fans: 0,
  articles: 0
})
// 文章列表
const ArticleList = ref([])
// 加载状态
const isAuthorLoading = ref(false)
const isArticlesLoading = ref(false)
// 关注状态
const isFollowing = ref(false)

// 获取作者信息
const getAuthorInfo = () => {
  isAuthorLoading.value = true
  get('api/user/getUserById', { id: authorId.value }, (message, data) => {
    if (data) {
      authorInfo.value = data
      // 获取关注状态
      checkFollowStatus()
    }
    isAuthorLoading.value = false
  })
}
//获取作者文章
const getArticleByUserId = () => {
  isArticlesLoading.value = true
  get('api/article/getArticleByUserId', { id: authorId.value }, (message, data) => {
    if (data) {
      ArticleList.value = data
      authorInfo.value.articles = data.length
    }
    isArticlesLoading.value = false
  })
}
// 检查关注状态
const checkFollow = () => {
  get('api/user/checkFollow', { followId: authorId.value }, (message, data) => {
    isFollowing.value = !!data
  })
}

// 切换关注状态
const toggleFollow = async () => {
  if (followLoading.value) return
  followLoading.value = true
  try {
    if (isFollowing.value) {
      // 取消关注
      await post('api/user/unfollow', { followId: authorId.value }, (msg, data) => {
        messageApi.success('取消关注成功')
        isFollowing.value = false
        // 更新粉丝数
        authorInfo.value.fans--
      })
    } else {
      // 关注
      await post('api/user/follow', { followId: authorId.value }, (msg, data) => {
        messageApi.success('关注成功')
        isFollowing.value = true
        // 更新粉丝数
        authorInfo.value.fans++
      })
    }
  } catch (error) {
    messageApi.error('操作失败，请重试')
  } finally {
    followLoading.value = false
  }
}

// 检查关注状态
const checkFollowStatus = () => {
  checkFollow()
}

onMounted(() => {
  getAuthorInfo()
  getArticleByUserId()
  // 启动进入动画
  requestAnimationFrame(() => { isMounted.value = true })
})

// 监听路由变化，动态切换作者
watch(() => route.params.id || route.query.id, (newId) => {
  if (!newId) return
  authorId.value = newId
  getAuthorInfo()
  getArticleByUserId()
})

// 当前激活的菜单
const activeMenu = ref('info')

// 菜单项数据
const menuItems = [
  { id: 'info', label: '作者信息' },
  { id: 'articles', label: '作者文章' },
  { id: 'portfolio', label: '作品集' },
  { id: 'favorites', label: '收藏夹' }
]

// 文章数据
const articles = ArticleList

// 关注按钮加载状态
const followLoading = ref(false)

// 项目数据
const projects = [
  {
    id: 1,
    title: '电商数据分析平台',
    tech: 'Vue 3 + Tailwind CSS',
    description: '为电商企业提供实时数据分析和可视化展示',
    image: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&h=300&q=80'
  },
  {
    id: 2,
    title: '健康管理应用',
    tech: 'React + Node.js',
    description: '帮助用户记录健康数据和运动习惯',
    image: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&h=300&q=80'
  },
  {
    id: 3,
    title: '在线教育平台',
    tech: 'Next.js + MongoDB',
    description: '提供在线课程学习和互动功能',
    image: 'https://images.unsplash.com/photo-1588072432836-e10032774350?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&h=300&q=80'
  },
  {
    id: 4,
    title: '任务管理系统',
    tech: 'Vue + Firebase',
    description: '团队协作任务管理和进度跟踪',
    image: 'https://images.unsplash.com/photo-1579389083078-4e7018379f7e?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&h=300&q=80'
  }
]

// 收藏数据
const favorites = [
  {
    id: 1,
    type: '文章',
    title: 'CSS Grid布局完全指南',
    url: 'css-tricks.com/css-grid',
    date: '2023-05-10'
  },
  {
    id: 2,
    type: '工具',
    title: 'VueUse工具库',
    url: 'vueuse.org',
    date: '2023-04-22'
  },
  {
    id: 3,
    type: '视频',
    title: 'TypeScript入门教程',
    url: 'youtube.com/typescript',
    date: '2023-03-15'
  },
  {
    id: 4,
    type: '文章',
    title: 'Tailwind最佳实践',
    url: 'tailwindcss.com/best-practices',
    date: '2023-06-01'
  },
  {
    id: 5,
    type: '工具',
    title: 'Iconify图标库',
    url: 'iconify.design',
    date: '2023-05-18'
  },
  {
    id: 6,
    type: '视频',
    title: 'Vue 3性能优化',
    url: 'youtube.com/vue-performance',
    date: '2023-04-05'
  }
]
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 消息提示组件 -->
    <contextHolder />

    <div class="flex flex-col lg:flex-row gap-8 bg-white dark:bg-gray-900 mx-auto rounded-xl shadow-modern overflow-hidden animate-fade-in">
    <!-- 左侧信息栏 (宽度1/5) -->
    <div class="w-1/5 flex flex-col transition-all duration-500 ease-in-out transform"
         :class="{ 'opacity-100 translate-x-0': isMounted, 'opacity-0 -translate-x-4': !isMounted }">
      <!-- 上方：用户头像和基本信息 -->
      <div class="p-6 bg-gradient-to-b from-primary to-secondary rounded-t-xl shadow-modern border-b border-white/20 dark:border-gray-700/20">
        <div class="flex flex-col items-center">
          <div class="w-32 h-32 rounded-full overflow-hidden mb-4 border-4 border-white dark:border-gray-700 shadow-lg relative group">
            <img :src="authorInfo.avatar || 'https://ts4.tc.mm.bing.net/th/id/OIP-C.Dp7V2vixW3M9C7YZ79EIYQAAAA?rs=1&pid=ImgDetMain&o=7&rm=3'"
                 alt="头像"
                 class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110 rounded-full">
          </div>

          <button
            :class="{
              'w-40 font-medium py-2 px-6 rounded-full transition duration-300 ease-in-out transform hover:scale-105 shadow-modern focus:outline-none focus:ring-2 focus:ring-opacity-50': true,
              'bg-primary hover:bg-primary/90 text-white focus:ring-primary': !isFollowing,
              'bg-gray-300 dark:bg-gray-700 hover:bg-gray-400 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 focus:ring-gray-500': isFollowing
            }"
            @click="toggleFollow"
            class="font-display"
            :disabled="followLoading"
          >
            {{ followLoading ? '处理中...' : (isFollowing ? '已关注' : '关注') }}
          </button>

          <div class="flex justify-between w-full text-sm text-white font-medium mt-4">
            <div class="text-center hover:scale-105 transition-transform duration-200">
              <p class="text-lg font-bold">{{ authorInfo.follow }}</p>
              <p>关注</p>
            </div>
            <div class="text-center hover:scale-105 transition-transform duration-200">
              <p class="text-lg font-bold">{{ authorInfo.fans }}</p>
              <p>粉丝</p>
            </div>
            <div class="text-center hover:scale-105 transition-transform duration-200">
              <p class="text-lg font-bold">{{ authorInfo.articles }}</p>
              <p>文章</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 下方：导航菜单 -->
      <div class="p-6 bg-gradient-to-b from-gray-100 to-gray-50 dark:from-gray-800 dark:to-gray-900 rounded-b-xl shadow-modern flex-grow border-t border-gray-200 dark:border-gray-700">
        <div class="flex flex-col space-y-3 text-center">
          <div
            v-for="(item, index) in menuItems"
            :key="index"
            @click="activeMenu = item.id"
            :class="{
              'w-full py-3 px-4 rounded-lg transition duration-300 cursor-pointer font-medium transform hover:-translate-y-0.5': true,
              'bg-primary/90 text-white shadow-md': activeMenu === item.id,
              'bg-white/80 dark:bg-gray-700/50 hover:bg-white dark:hover:bg-gray-700': activeMenu !== item.id
            }"
          >
            {{ item.label }}
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧内容区 (宽度4/5) -->
    <div class="w-4/5 p-6 transition-all duration-500 ease-in-out transform"
         :class="{ 'opacity-100 translate-y-0': isMounted, 'opacity-0 translate-y-4': !isMounted }">
      <!-- 作者信息 -->
      <div v-if="activeMenu === 'info'" class="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-modern mb-6 transition-all duration-500 hover:shadow-xl border border-gray-100 dark:border-gray-700 animate-delay-100">
        <h2 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-4 flex items-center font-display">
          <span class="w-2 h-8 bg-primary mr-3 rounded-full"></span>
          作者详细信息
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="bg-gray-50 dark:bg-gray-900/50 p-4 rounded-lg border border-gray-100 dark:border-gray-700 hover:shadow-md transition-all duration-300">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-3 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              基本信息
            </h3>
            <ul class="space-y-3">
              <li class="flex items-center">
                <span class="text-gray-500 dark:text-gray-400 w-24">用户名：</span>
                <span class="text-primary font-medium">{{ authorInfo.account || '未知' }}</span>
              </li>
              <li class="flex items-center">
                <span class="text-gray-500 dark:text-gray-400 w-24">角色：</span>
                <span class="text-secondary font-medium">{{ authorInfo.role || '普通用户' }}</span>
              </li>
              <li class="flex items-center">
                <span class="text-gray-500 dark:text-gray-400 w-24">ID：</span>
                <span class="text-tertiary font-medium">{{ authorInfo.id || '未知' }}</span>
              </li>
            </ul>
          </div>
          <div class="bg-gray-50 dark:bg-gray-900/50 p-4 rounded-lg border border-gray-100 dark:border-gray-700 hover:shadow-md transition-all duration-300">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-3 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              联系方式
            </h3>
            <ul class="space-y-3">
              <li class="flex items-center">
                <span class="text-gray-500 dark:text-gray-400 w-24">邮箱：</span>
                <span class="text-primary font-medium">{{ authorInfo.email || '未设置' }}</span>
              </li>
              <li class="flex items-center">
                <span class="text-gray-500 dark:text-gray-400 w-24">关注数：</span>
                <span class="text-secondary font-medium">{{ authorInfo.follow || 0 }}</span>
              </li>
              <li class="flex items-center">
                <span class="text-gray-500 dark:text-gray-400 w-24">粉丝数：</span>
                <span class="text-tertiary font-medium">{{ authorInfo.fans || 0 }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 作者文章 -->
      <div v-if="activeMenu === 'articles'" class="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-modern mb-6 animate-delay-100 border border-gray-100 dark:border-gray-700">
        <h2 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-6 flex items-center font-display">
          <span class="w-2 h-8 bg-primary mr-3 rounded-full"></span>
          作者文章
        </h2>
        
        <div v-if="isArticlesLoading" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
        
        <div v-else-if="articles.length === 0" class="text-center py-12">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300 dark:text-gray-600 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <h3 class="mt-4 text-lg font-medium text-gray-700 dark:text-gray-300">暂无文章</h3>
          <p class="mt-2 text-gray-500 dark:text-gray-400">该用户还没有发布任何文章</p>
        </div>
        
        <div v-else class="space-y-4">
          <div v-for="article in articles" :key="article.id"
               class="border-b border-gray-100 dark:border-gray-700 pb-4 last:border-0 hover:bg-gray-50 dark:hover:bg-gray-900 p-3 rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-md">
            <h3 class="text-xl font-semibold text-gray-800 dark:text-white hover:text-primary cursor-pointer transition-colors duration-300">
              {{ article.title }}
            </h3>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {{ article.date }}
            </p>
            <p class="text-gray-600 dark:text-gray-300 mt-2 line-clamp-2">{{ article.summary }}</p>
            <div class="flex flex-wrap mt-3 gap-2">
              <span v-for="tag in article.tags" :key="tag"
                    class="text-xs px-3 py-1 bg-primary/10 dark:bg-primary/20 rounded-full text-primary font-medium hover:bg-primary/20 dark:hover:bg-primary/30 transition-colors">
                {{ tag }}
              </span>
            </div>
          </div>
        </div>
        <button class="mt-6 text-primary hover:text-secondary font-medium flex items-center transition-all duration-300 transform hover:scale-105 group">
          查看全部文章
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1 group-hover:translate-x-1 transition-transform duration-300" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>

      <!-- 作品集 -->
      <div v-if="activeMenu === 'portfolio'" class="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-modern mb-6 animate-delay-100 border border-gray-100 dark:border-gray-700">
        <h2 class="text-2xl font-bold bg-gradient-to-r from-secondary to-tertiary bg-clip-text text-transparent mb-6 flex items-center font-display">
          <span class="w-2 h-8 bg-secondary mr-3 rounded-full"></span>
          作品集
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div v-for="project in projects" :key="project.id"
               class="border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden hover:shadow-xl transition-all duration-500 transform hover:-translate-y-1 group">
            <div class="overflow-hidden">
              <img :src="project.image"
                   :alt="project.title"
                   class="w-full h-48 object-cover group-hover:scale-110 transition duration-700">
            </div>
            <div class="p-5 bg-gray-50 dark:bg-gray-900">
              <h3 class="font-semibold text-xl text-gray-800 dark:text-white group-hover:text-secondary transition-colors duration-300">{{ project.title }}</h3>
              <div class="flex items-center mt-1">
                <span class="text-xs px-2 py-1 bg-secondary/20 dark:bg-secondary/30 text-secondary rounded-full">{{ project.tech }}</span>
              </div>
              <p class="text-gray-600 dark:text-gray-400 mt-3 text-sm">{{ project.description }}</p>
              <a href="#" class="mt-4 inline-flex items-center text-sm font-medium text-secondary hover:text-primary transition-colors group">
                查看详情
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform group-hover:translate-x-1 transition-transform duration-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 收藏夹 -->
      <div v-if="activeMenu === 'favorites'" class="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-modern animate-delay-100 border border-gray-100 dark:border-gray-700">
        <h2 class="text-2xl font-bold bg-gradient-to-r from-tertiary to-primary bg-clip-text text-transparent mb-6 flex items-center font-display">
          <span class="w-2 h-8 bg-tertiary mr-3 rounded-full"></span>
          收藏夹
        </h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="fav in favorites" :key="fav.id"
               class="border border-gray-200 dark:border-gray-700 rounded-xl p-4 hover:shadow-md transition-all duration-300 cursor-pointer transform hover:-translate-y-1 hover:border-tertiary/30">
            <div class="flex items-center mb-3">
              <div :class="{
                'w-10 h-10 rounded-full flex items-center justify-center mr-3 text-xs font-medium text-white': true,
                'bg-primary': fav.type === '文章',
                'bg-secondary': fav.type === '工具',
                'bg-tertiary': fav.type === '视频'
              }">
                {{ fav.type }}
              </div>
              <h3 class="font-medium truncate text-gray-800 dark:text-white">{{ fav.title }}</h3>
            </div>
            <p class="text-gray-500 dark:text-gray-400 text-sm truncate hover:text-primary transition-colors duration-300">{{ fav.url }}</p>
            <p class="text-gray-400 dark:text-gray-500 text-xs mt-2 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {{ fav.date }}
            </p>
          </div>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<style scoped>
/* 自定义样式 */

/* 确保动画类可用 */
.animate-fade-in {
  animation: fadeIn 0.6s ease-out;
}

.animate-delay-100 {
  animation-delay: 100ms;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 阴影效果增强 */
.shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.dark .shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}
</style>
