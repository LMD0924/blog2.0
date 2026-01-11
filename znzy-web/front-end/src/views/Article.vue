<script setup>
import {ref, onMounted, reactive, computed, onUnmounted, watch} from 'vue';
import { get } from "@/net/index.js";
import {message} from "ant-design-vue";
import router from "@/router/index.js";

const isDark = ref(false);
const [messageApi, contextHolder] = message.useMessage();
// 添加布局类型状态
const layoutType = ref(localStorage.getItem('articleLayoutType') || 'grid'); // 从 localStorage 获取布局类型，默认为网格布局

// 监听布局类型变化并保存
watch(layoutType, (newValue) => {
  localStorage.setItem('articleLayoutType', newValue);
});

// 获取当前用户信息
const options = reactive({
  Article:[],
  user: {
    username: '',
    password: '',
    email: '',
    avatar: '',
    account: ''
  }
});
//获取个人信息
const GetCurrentUser = () => {
  get('api/user/current', {},
    (message, data) => {
      options.user = data
    })
}
//获取所有文章
const GetAllArticle=()=>{
  get('api/article/getAllArticle',{},
    (message,data)=>{
      options.Article=data;
      console.log(options.Article)
    })
}
// 跳转到文章详情页
const goArticleById = (id) => {
  router.push("/view/" + id);
};
onMounted(() => {
  GetCurrentUser();
  GetAllArticle();
  // 初始化主题
  const savedTheme = localStorage.getItem('darkMode');
  if (savedTheme !== null) {
    isDark.value = savedTheme === 'true';
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
});

// 分类筛选
const categories = ref(["全部", "技术", "生活", "阅读","算法", "其他"]);
const selectedCategory = ref("全部");

const filteredArticles = computed(() => {
  if (selectedCategory.value === "全部") {
    return options.Article;
  }
  return options.Article.filter(article => article.category === selectedCategory.value);
});

// 搜索功能
const searchQuery = ref("");
const searchedArticles = computed(() => {
  if (!searchQuery.value) {
    return filteredArticles.value;
  }
  const query = searchQuery.value.toLowerCase();
  return filteredArticles.value.filter(article =>
    article.title.toLowerCase().includes(query) ||
    (article.excerpt && article.excerpt.toLowerCase().includes(query)) ||
    (article.tags && article.tags.some(tag => tag.includes(query)))
  );
});

// 分页
const currentPage = ref(1);
const itemsPerPage = 12;

const paginatedArticles = computed(() => {
  // 如果是列表布局，返回所有文章
  if (layoutType.value === 'list') {
    return searchedArticles.value;
  }
  // 如果是网格布局，保持分页
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return searchedArticles.value.slice(start, end);
});

const totalPages = computed(() => {
  // 如果是列表布局，不显示分页
  if (layoutType.value === 'list') {
    return 0;
  }
  return Math.ceil(searchedArticles.value.length / itemsPerPage);
});

const goToPage = (page) => {
  currentPage.value = page;
};

</script>

<template>
  <contextHolder />
  <div :class="isDark" class="min-h-screen transition-colors duration-300">
    <div class="bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100 min-h-screen">
      <!-- 主要内容 -->
      <main class="container mx-auto px-4 py-8">
        <!-- 文章列表标题和搜索 -->
        <section class="mb-8">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 animate-fade-in">
            <h1 class="text-3xl md:text-4xl font-bold mb-4 md:mb-0 font-display bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">文章列表</h1>
            <div class="flex items-center gap-4">
              <!-- 布局切换按钮 -->
              <div class="flex items-center space-x-2">
                <button
                  @click="layoutType = 'grid'"
                  :class="{
                    'bg-primary text-white': layoutType === 'grid',
                'bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 hover:bg-gray-300 dark:hover:bg-gray-600': layoutType !== 'grid'
                  }"
                  class="p-2 rounded-md transition"
                  title="网格布局"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
                  </svg>
                </button>
                <button
                  @click="layoutType = 'list'"
                  :class="{
                    'bg-primary text-white': layoutType === 'list',
                'bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 hover:bg-gray-300 dark:hover:bg-gray-600': layoutType !== 'list'
                  }"
                  class="p-2 rounded-md transition"
                  title="列表布局"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
                  </svg>
                </button>
              </div>

              <div class="relative w-full md:w-64">
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索文章..."
                  class="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-primary transition-all duration-300"
                  @input="currentPage = 1"
                >
                <svg class="absolute left-3 top-2.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
              </div>
            </div>
          </div>

          <!-- 分类筛选 -->
          <div class="flex flex-wrap gap-2 mb-6">
            <button
              v-for="category in categories"
              :key="category"
              @click="selectedCategory = category; currentPage = 1"
              :class="{
                'bg-primary text-white': selectedCategory === category,
                'bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 hover:bg-gray-300 dark:hover:bg-gray-600 transition-all duration-300': selectedCategory !== category
              }"
              class="px-4 py-2 rounded-full text-sm font-medium transition"
            >
              {{ category }}
            </button>
          </div>
        </section>

        <!-- 文章列表 - 根据布局类型显示不同样式 -->
        <section class="mb-8">
          <!-- 网格布局 -->
          <div v-if="layoutType === 'grid'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-6">
            <article
              @click="goArticleById(article.id)"
              v-for="article in paginatedArticles"
              :key="article.id"
              class="bg-white dark:bg-gray-800 rounded-xl overflow-hidden shadow-lg hover:shadow-xl transition-all duration-500 article-card border border-gray-100 dark:border-gray-700 transform hover:-translate-y-1 animate-delay-100 cursor-pointer"
            >
              <!-- 原有网格布局内容 -->
              <div class="flex flex-nowrap">
                <span class="mt-5 ml-5">作者：</span>
                <img
                  :src="article.user.avatar + '?timestamp=' + Date.now()"
                  class="w-8 h-8 rounded-xl object-cover mt-5 ml-5"
                  alt="头像"
                />
                <span class="mt-5 ml-5">{{ article.user.account }}</span>
              </div>
              <div class="p-6">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-primary font-medium">{{ article.category }}</span>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ article.date }}</span>
                </div>
                <h3 class="text-xl font-bold mb-3 font-display text-gray-800 dark:text-white">{{ article.title }}</h3>
                <div v-if="article.tag" class="flex flex-wrap gap-2 mb-4">
                  <span
                    v-for="tag in article.tag.split(',')"
                    :key="tag"
                    class="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs hover:bg-primary/10 transition-colors"
                  >
                    {{ tag }}
                  </span>
                </div>

                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400">
                    <span class="flex items-center">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                      </svg>
                      {{ article.views || 0 }}
                    </span>
                    <span class="flex items-center">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                      </svg>
                      {{ article.likes || 0 }}
                    </span>
                    <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                    </svg>
                    {{ article.favorites || 0 }} 收藏
                      </span>
                  </div>
                  <a
                    class="inline-flex items-center text-primary hover:text-primary/80 font-medium transition-all transform hover:-translate-x-0.5"
                  >
                    阅读全文
                    <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                    </svg>
                  </a>
                </div>
              </div>
            </article>
          </div>

          <!-- 列表布局（类似图片中的样式） -->
          <div v-else class="space-y-6 max-h-[800px] overflow-y-auto pr-2 custom-scrollbar">
            <article
              @click="goArticleById(article.id)"
              v-for="article in paginatedArticles"
              :key="article.id"
              class="bg-white dark:bg-gray-800 rounded-xl overflow-hidden shadow-lg hover:shadow-xl transition-all duration-500 article-card border border-gray-100 dark:border-gray-700 transform hover:-translate-y-1 animate-delay-100 cursor-pointer"
            >
              <div class="flex flex-col md:flex-row">
                <!-- 左侧内容区域 -->
                <div class="flex-1 p-6">
                  <div class="flex items-center space-x-3 mb-3">
                    <img
                      :src="article.user.avatar + '?timestamp=' + Date.now()"
                      class="w-8 h-8 rounded-full object-cover"
                      alt="头像"
                    />
                    <span class="text-sm">{{ article.user.account }}</span>
                    <span class="text-sm text-gray-500 dark:text-gray-400">{{ article.date }}</span>
                  </div>

                  <h3 class="text-xl font-bold mb-3 font-display text-gray-800 dark:text-white">{{ article.title }}</h3>
                  <div class="flex items-center justify-between">
                    <div class="flex items-center space-x-4">
                      <span v-if="article.category" class="text-sm dark:text-gray-400">文章分类：{{ article.category }}</span>
                      <span v-else class="text-sm dark:text-gray-400">文章分类：暂无分类</span>

                      <div v-if="article.tag" class="flex flex-wrap gap-2">
                        <span class="text-sm dark:text-gray-400">文章标签：</span>
                        <span
                          v-for="tag in article.tag.split(',')"
                          :key="tag"
                          class="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs hover:bg-primary/10 transition-colors"
                        >
                          {{ tag.trim() }}
                        </span>
                      </div>
                    </div>

                    <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
                      <span class="flex items-center">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                        {{ article.views || 0 }}
                      </span>
                      <span class="flex items-center">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                        </svg>
                        {{ article.likes || 0 }}
                      </span>
                      <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                    </svg>
                    {{ article.favorites || 0 }} 收藏
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="px-6 pb-4">
                <a
                  class="inline-flex items-center text-primary hover:text-primary/80 font-medium transition-all transform hover:-translate-x-0.5"
                >
                  阅读全文
                  <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                  </svg>
                </a>
              </div>
            </article>
          </div>

          <!-- 无文章提示 -->
          <div v-if="searchedArticles.length === 0" class="text-center py-12">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <h3 class="mt-2 text-lg font-medium text-gray-900 dark:text-gray-100 font-display">没有找到文章</h3>
            <p class="mt-1 text-gray-500 dark:text-gray-400">尝试不同的搜索词或分类筛选</p>
          </div>
        </section>

        <!-- 分页 -->
        <div v-if="totalPages > 1 && layoutType === 'grid'" class="flex justify-center mt-8">
          <nav class="inline-flex rounded-md shadow">
            <button
              @click="goToPage(currentPage - 1)"
              :disabled="currentPage === 1"
              class="px-3 py-2 rounded-l-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
            >
              上一页
            </button>
            <button
              v-for="page in totalPages"
              :key="page"
              @click="goToPage(page)"
              :class="{
                'bg-primary text-white': currentPage === page,
                'bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 transition-all duration-300 transform hover:-translate-y-0.5': currentPage !== page
              }"
              class="px-4 py-2 border-t border-b border-gray-300 dark:border-gray-700"
            >
              {{ page }}
            </button>
            <button
              @click="goToPage(currentPage + 1)"
              :disabled="currentPage === totalPages"
              class="px-3 py-2 rounded-r-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
            >
              下一页
            </button>
          </nav>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 添加主题切换过渡效果 */
.min-h-screen {
  transition: background-color 0.3s ease, color 0.3s ease;
}

.bg-white,
.bg-gray-100,
.bg-gray-800,
.bg-gray-900,
.dark\:bg-gray-800,
.dark\:bg-gray-900 {
  transition: background-color 0.3s ease;
}

.text-gray-900,
.text-gray-100,
.text-gray-500,
.text-gray-400,
.text-gray-700,
.text-gray-300,
.dark\:text-gray-100,
.dark\:text-gray-400 {
  transition: color 0.3s ease;
}

.border-gray-200,
.border-gray-700,
.dark\:border-gray-700 {
  transition: border-color 0.3s ease;
}

/* 文章卡片过渡效果 */
.article-card {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, rgba(59, 130, 246, 0.1), rgba(99, 102, 241, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-card:hover::before {
  opacity: 1;
}

/* 文章卡片内容悬停效果 */
.article-card:hover h3 {
  color: #3b82f6;
}

.article-card:hover .text-gray-600 {
  color: #4b5563;
}

.dark .article-card:hover h3 {
  color: #60a5fa;
}

.dark .article-card:hover .text-gray-600 {
  color: #9ca3af;
}

/* 文章卡片按钮悬停效果 */
.article-card:hover .text-blue-500 {
  color: #2563eb;
}

.dark .article-card:hover .text-blue-500 {
  color: #3b82f6;
}

/* 文章卡片标签悬停效果 */
.article-card:hover .bg-gray-100 {
  background-color: #e5e7eb;
}

.dark .article-card:hover .bg-gray-700 {
  background-color: #4b5563;
}

/* 按钮和交互元素过渡效果 */
button,
a,
input,
textarea,
select {
  transition: background-color 0.3s ease,
              color 0.3s ease,
              border-color 0.3s ease,
              box-shadow 0.3s ease;
}

/* 卡片和阴影效果的过渡 */
.shadow,
.hover\:shadow-lg {
  transition: box-shadow 0.3s ease;
}

/* 确保暗色模式下的过渡效果 */
.dark * {
  transition: background-color 0.3s ease,
              color 0.3s ease,
              border-color 0.3s ease,
              box-shadow 0.3s ease;
}

/* 其他现有样式 */
.router-link-active {
  @apply text-blue-500 font-medium;
}

/* 隐藏滚动条 */
html {
  scrollbar-width: none;
  -ms-overflow-style: none;
  scroll-behavior: smooth;
}

html::-webkit-scrollbar {
  display: none;
}

/* 过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 自定义滚动条样式 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  display: block;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
  transition: background-color 0.3s ease;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #4b5563;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}
/* 使用边框模拟更自定义的下划线 */
.article-card h3 {
  display: inline-block;
  position: relative;
}

.article-card h3::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: -2px;
  left: 0;
  background-color: #3b82f6;
  transition: width 0.3s ease;
}

.article-card:hover h3::after {
  width: 100%;
}

.dark .article-card h3::after {
  background-color: #3b82f6;
}

/* 动画类 */
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

.animate-delay-100 {
  animation-delay: 0.1s;
}

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

/* 颜色变量 */
:root {
  --primary: #3b82f6;
  --primary-dark: #2563eb;
  --primary-light: #60a5fa;
  --secondary: #8b5cf6;
  --tertiary: #ec4899;
}
</style>
