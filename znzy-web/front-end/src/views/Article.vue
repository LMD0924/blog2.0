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

// 分类数据
const categories = ref([
  { name: "全部", icon: "📚", count: 0 },
  { name: "技术", icon: "💻", count: 0 },
  { name: "生活", icon: "🌱", count: 0 },
  { name: "阅读", icon: "📖", count: 0 },
  { name: "算法", icon: "🧮", count: 0 },
  { name: "其他", icon: "✨", count: 0 }
]);

const selectedCategory = ref("全部");

// 计算分类文章数量
const updateCategoryCounts = () => {
  categories.value.forEach(category => {
    if (category.name === "全部") {
      category.count = options.Article.length;
    } else {
      category.count = options.Article.filter(article => article.category === category.name).length;
    }
  });
};

// 分类筛选
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
    (article.tag && article.tag.toLowerCase().includes(query))
  );
});

// 分页
const currentPage = ref(1);
const itemsPerPage = 12;

const paginatedArticles = computed(() => {
  // 如果是列表布局，返回所有文章（滚动加载）
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
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 特色文章（置顶或推荐）
const featuredArticles = computed(() => {
  return options.Article.slice(0, 2).map(article => ({
    ...article,
    featured: true
  }));
});

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

  // 等待文章加载完成后更新分类计数
  setTimeout(() => {
    updateCategoryCounts();
  }, 500);
});

// 监听文章变化更新分类计数
watch(() => options.Article, () => {
  updateCategoryCounts();
}, { deep: true });
</script>

<template>
  <contextHolder />
  <div :class="isDark" class="min-h-screen transition-colors duration-300">
    <div class="text-gray-900 dark:text-gray-100 min-h-screen">
      <!-- 主要内容 -->
      <main class="container mx-auto px-4 py-8">
        <!-- 英雄区域 -->
        <section class="relative mb-12 overflow-hidden rounded-3xl bg-gradient-to-r from-primary/10 to-secondary/10 p-8 md:p-12">
          <div class="absolute inset-0 bg-grid-pattern opacity-5"></div>
          <div class="relative z-10">
            <div class="flex items-center space-x-2 mb-4">
              <span class="px-3 py-1 bg-primary/20 text-primary rounded-full text-sm font-medium">📝 文章列表</span>
              <span class="px-3 py-1 bg-secondary/20 text-secondary rounded-full text-sm font-medium">共 {{ options.Article.length }} 篇</span>
            </div>
            <h1 class="text-4xl md:text-5xl font-bold mb-4 font-display">
              <span class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                知识宝库
              </span>
            </h1>
            <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl">
              探索技术深度，分享生活感悟，让每一次阅读都成为成长的阶梯
            </p>
          </div>

          <!-- 装饰元素 -->
          <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
          <div class="absolute bottom-0 left-0 w-48 h-48 bg-gradient-to-r from-secondary/20 to-primary/20 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2"></div>
        </section>

        <!-- 搜索和布局控制 -->
        <section class="mb-8">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <!-- 搜索框 -->
            <div class="relative w-full md:w-96">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索文章、标签或内容..."
                class="w-full pl-12 pr-4 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all duration-300 shadow-lg"
                @input="currentPage = 1"
              >
              <svg class="absolute left-4 top-3.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>

            <!-- 布局切换和新建文章 -->
            <div class="flex items-center space-x-3">
              <!-- 布局切换按钮组 -->
              <div class="flex items-center bg-white dark:bg-gray-800 rounded-2xl p-1 shadow-lg border border-gray-200 dark:border-gray-700">
                <button
                  @click="layoutType = 'grid'"
                  :class="{
                    'bg-gradient-to-r from-primary to-secondary text-white shadow-lg': layoutType === 'grid',
                    'text-gray-500 hover:text-primary': layoutType !== 'grid'
                  }"
                  class="p-2 rounded-xl transition-all duration-300"
                  title="网格布局"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
                  </svg>
                </button>
                <button
                  @click="layoutType = 'list'"
                  :class="{
                    'bg-gradient-to-r from-primary to-secondary text-white shadow-lg': layoutType === 'list',
                    'text-gray-500 hover:text-primary': layoutType !== 'list'
                  }"
                  class="p-2 rounded-xl transition-all duration-300"
                  title="列表布局"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
                  </svg>
                </button>
              </div>

              <!-- 新建文章按钮 -->
              <router-link to="/editor" class="group relative">
                <div class="absolute inset-0 bg-gradient-to-r from-primary to-secondary rounded-2xl blur-xl opacity-50 group-hover:opacity-75 transition-opacity"></div>
                <button class="relative px-6 py-2 bg-gradient-to-r from-primary to-secondary text-white rounded-2xl font-medium flex items-center space-x-2 hover:shadow-2xl transition-all duration-300">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                  </svg>
                  <span>写文章</span>
                </button>
              </router-link>
            </div>
          </div>
        </section>

        <!-- 分类筛选 -->
        <section class="mb-10">
          <div class="flex flex-wrap gap-3">
            <button
              v-for="category in categories"
              :key="category.name"
              @click="selectedCategory = category.name; currentPage = 1"
              :class="{
                'bg-gradient-to-r from-primary to-secondary text-white shadow-xl scale-105': selectedCategory === category.name,
                'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:scale-105 hover:shadow-lg border border-gray-200 dark:border-gray-700': selectedCategory !== category.name
              }"
              class="relative px-5 py-2.5 rounded-xl font-medium transition-all duration-300 flex items-center space-x-2 group"
            >
              <span class="text-lg">{{ category.icon }}</span>
              <span>{{ category.name }}</span>
              <span class="ml-2 px-2 py-0.5 bg-white/20 rounded-full text-xs">{{ category.count }}</span>

              <!-- 悬停光效 -->
              <div v-if="selectedCategory !== category.name" class="absolute inset-0 rounded-xl bg-gradient-to-r from-primary/10 to-secondary/10 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </button>
          </div>
        </section>

        <!-- 文章统计信息 -->
        <section class="mb-8 flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
          <div class="flex items-center space-x-4">
            <span class="flex items-center">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
              </svg>
              {{ searchedArticles.length }} 篇文章
            </span>
            <span class="flex items-center">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l5 5a2 2 0 01.586 1.414V19a2 2 0 01-2 2H7a2 2 0 01-2-2V5a2 2 0 012-2z"></path>
              </svg>
              {{ categories.reduce((acc, cat) => acc + cat.count, 0) - categories[0].count }} 个分类
            </span>
          </div>

          <!-- 排序选项（可选） -->
          <div class="flex items-center space-x-2">
            <span>排序：</span>
            <select class="bg-transparent border-none focus:ring-0 text-sm cursor-pointer">
              <option>最新发布</option>
              <option>最多阅读</option>
              <option>最多点赞</option>
            </select>
          </div>
        </section>

        <!-- 文章列表 -->
        <section class="mb-8">
          <!-- 网格布局 -->
          <div v-if="layoutType === 'grid'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-6">
            <article
              @click="goArticleById(article.id)"
              v-for="(article, index) in paginatedArticles"
              :key="article.id"
              class="group relative bg-white dark:bg-gray-800 rounded-2xl overflow-hidden shadow-xl hover:shadow-2xl transition-all duration-700 hover:-translate-y-2 cursor-pointer"
              :style="{ animationDelay: `${index * 100}ms` }"
            >
              <!-- 卡片装饰 -->
              <div class="absolute inset-0 bg-gradient-to-r from-primary/5 to-secondary/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>

              <!-- 顶部渐变条 -->
              <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-primary to-secondary transform scale-x-0 group-hover:scale-x-100 transition-transform duration-500 origin-left"></div>

              <!-- 作者信息区域 -->
              <div class="relative p-6 pb-4 border-b border-gray-100 dark:border-gray-700">
                <div class="flex items-center space-x-3">
                  <div class="relative">
                    <img
                      :src="article.user.avatar + '?timestamp=' + Date.now()"
                      class="w-10 h-10 rounded-xl object-cover ring-2 ring-white dark:ring-gray-700 group-hover:ring-primary transition-all duration-300"
                      alt="头像"
                    />
                    <div class="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-white dark:border-gray-800"></div>
                  </div>
                  <div>
                    <p class="font-medium text-gray-900 dark:text-white">{{ article.user.account }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">{{ article.date || '最近更新' }}</p>
                  </div>
                </div>
              </div>

              <div class="p-6">
                <!-- 分类和标签 -->
                <div class="flex items-center justify-between mb-3">
                  <span class="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium flex items-center">
                    {{ categories.find(c => c.name === article.category)?.icon || '📄' }}
                    {{ article.category || '未分类' }}
                  </span>
                  <div class="flex space-x-2">
                    <span v-if="article.tag" v-for="tag in article.tag.split(',').slice(0, 2)" :key="tag"
                          class="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs text-gray-600 dark:text-gray-300">
                      {{ tag.trim() }}
                    </span>
                    <span v-if="article.tag && article.tag.split(',').length > 2" class="text-xs text-gray-400">
                      +{{ article.tag.split(',').length - 2 }}
                    </span>
                  </div>
                </div>

                <!-- 标题和摘要 -->
                <h3 class="text-xl font-bold mb-2 font-display text-gray-800 dark:text-white group-hover:text-primary transition-colors line-clamp-2">
                  {{ article.title }}
                </h3>
                <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
                  {{ article.excerpt || '暂无摘要...' }}
                </p>

                <!-- 统计信息 -->
                <div class="flex items-center justify-between text-sm">
                  <div class="flex items-center space-x-3">
                    <span class="flex items-center text-gray-500 dark:text-gray-400">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                      </svg>
                      {{ article.views || 0 }}
                    </span>
                    <span class="flex items-center text-gray-500 dark:text-gray-400">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                      </svg>
                      {{ article.likes || 0 }}
                    </span>
                    <span class="flex items-center text-gray-500 dark:text-gray-400">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                      </svg>
                      {{ article.favorites || 0 }}
                    </span>
                  </div>

                  <!-- 阅读全文按钮 -->
                  <div class="flex items-center text-primary opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <span class="text-sm font-medium">阅读全文</span>
                    <svg class="w-4 h-4 ml-1 transform group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                    </svg>
                  </div>
                </div>
              </div>

              <!-- 阅读进度条 -->
              <div class="absolute bottom-0 left-0 right-0 h-1 bg-gray-100 dark:bg-gray-700">
                <div class="h-full bg-gradient-to-r from-primary to-secondary w-0 group-hover:w-full transition-all duration-1000"></div>
              </div>
            </article>
          </div>

          <!-- 列表布局（瀑布流效果） -->
          <div v-else class="space-y-4">
            <article
              @click="goArticleById(article.id)"
              v-for="(article, index) in paginatedArticles"
              :key="article.id"
              class="group relative bg-white dark:bg-gray-800 rounded-2xl overflow-hidden shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-1 cursor-pointer"
              :style="{ animationDelay: `${index * 50}ms` }"
            >
              <div class="absolute inset-0 bg-gradient-to-r from-primary/5 to-secondary/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>

              <div class="p-6">
                <!-- 第一行：作者信息和分类 -->
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center space-x-3">
                    <img
                      :src="article.user.avatar + '?timestamp=' + Date.now()"
                      class="w-8 h-8 rounded-lg object-cover ring-2 ring-white dark:ring-gray-700 group-hover:ring-primary transition-all"
                      alt="头像"
                    />
                    <span class="font-medium text-gray-900 dark:text-white">{{ article.user.account }}</span>
                    <span class="text-gray-400">·</span>
                    <span class="text-sm text-gray-500 dark:text-gray-400">{{ article.date || '最近更新' }}</span>
                  </div>
                  <span class="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium flex items-center">
                    {{ categories.find(c => c.name === article.category)?.icon || '📄' }}
                    {{ article.category || '未分类' }}
                  </span>
                </div>

                <!-- 第二行：标题 -->
                <h3 class="text-xl font-bold mb-2 font-display text-gray-800 dark:text-white group-hover:text-primary transition-colors">
                  {{ article.title }}
                </h3>

                <!-- 第三行：标签 -->
                <div v-if="article.tag" class="flex flex-wrap gap-2 mb-3">
                  <span
                    v-for="tag in article.tag.split(',')"
                    :key="tag"
                    class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs text-gray-600 dark:text-gray-300 hover:bg-primary/10 transition-colors"
                  >
                    #{{ tag.trim() }}
                  </span>
                </div>

                <!-- 第四行：统计和操作 -->
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-4 text-sm">
                    <span class="flex items-center text-gray-500 dark:text-gray-400">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                      </svg>
                      {{ article.views || 0 }}
                    </span>
                    <span class="flex items-center text-gray-500 dark:text-gray-400">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                      </svg>
                      {{ article.likes || 0 }}
                    </span>
                    <span class="flex items-center text-gray-500 dark:text-gray-400">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                      </svg>
                      {{ article.favorites || 0 }}
                    </span>
                  </div>
                  <div class="flex items-center text-primary opacity-0 group-hover:opacity-100 transition-opacity">
                    <span class="text-sm font-medium">阅读全文</span>
                    <svg class="w-4 h-4 ml-1 transform group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                    </svg>
                  </div>
                </div>
              </div>

              <!-- 左侧装饰条 -->
              <div class="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-primary to-secondary transform scale-y-0 group-hover:scale-y-100 transition-transform duration-500 origin-top"></div>
            </article>
          </div>

          <!-- 无文章提示 -->
          <div v-if="searchedArticles.length === 0" class="text-center py-20">
            <div class="relative inline-block">
              <div class="w-24 h-24 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-3xl flex items-center justify-center mx-auto mb-6">
                <svg class="w-12 h-12 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
              <div class="absolute -inset-4 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-3xl blur-xl opacity-50"></div>
            </div>
            <h3 class="mt-6 text-2xl font-bold text-gray-900 dark:text-white font-display">没有找到文章</h3>
            <p class="mt-2 text-gray-500 dark:text-gray-400">尝试不同的搜索词或分类筛选</p>
            <button @click="searchQuery = ''; selectedCategory = '全部'" class="mt-6 px-6 py-3 bg-gradient-to-r from-primary to-secondary text-white rounded-xl font-medium hover:shadow-2xl transition-all">
              清除筛选
            </button>
          </div>
        </section>

        <!-- 分页（仅网格布局） -->
        <div v-if="totalPages > 1 && layoutType === 'grid'" class="flex justify-center mt-12">
          <nav class="inline-flex items-center space-x-2">
            <button
              @click="goToPage(currentPage - 1)"
              :disabled="currentPage === 1"
              class="w-10 h-10 flex items-center justify-center rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-primary hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>

            <button
              v-for="page in totalPages"
              :key="page"
              @click="goToPage(page)"
              :class="{
                'bg-gradient-to-r from-primary to-secondary text-white shadow-lg scale-110': currentPage === page,
                'bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-primary/10': currentPage !== page
              }"
              class="w-10 h-10 rounded-xl border border-gray-200 dark:border-gray-700 transition-all duration-300"
            >
              {{ page }}
            </button>

            <button
              @click="goToPage(currentPage + 1)"
              :disabled="currentPage === totalPages"
              class="w-10 h-10 flex items-center justify-center rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-primary hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </button>
          </nav>
        </div>

        <!-- 回到顶部按钮 -->
        <button
          @click="window.scrollTo({ top: 0, behavior: 'smooth' })"
          class="fixed bottom-8 right-8 w-12 h-12 bg-gradient-to-r from-primary to-secondary text-white rounded-xl shadow-2xl hover:shadow-3xl transition-all duration-300 hover:-translate-y-1"
        >
          <svg class="w-6 h-6 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path>
          </svg>
        </button>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 主题切换过渡效果 */
.min-h-screen {
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 网格背景图案 */
.bg-grid-pattern {
  background-image:
    linear-gradient(to right, rgba(128, 128, 128, 0.1) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(128, 128, 128, 0.1) 1px, transparent 1px);
  background-size: 40px 40px;
}

/* 文章卡片动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.article-card {
  animation: fadeInUp 0.5s ease-out forwards;
  opacity: 0;
}

/* 延迟动画 */
.article-card:nth-child(1) { animation-delay: 0.1s; }
.article-card:nth-child(2) { animation-delay: 0.2s; }
.article-card:nth-child(3) { animation-delay: 0.3s; }
.article-card:nth-child(4) { animation-delay: 0.4s; }
.article-card:nth-child(5) { animation-delay: 0.5s; }
.article-card:nth-child(6) { animation-delay: 0.6s; }
.article-card:nth-child(7) { animation-delay: 0.7s; }
.article-card:nth-child(8) { animation-delay: 0.8s; }

/* 文本截断 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 阴影增强 */
.shadow-3xl {
  box-shadow: 0 35px 60px -15px rgba(0, 0, 0, 0.3);
}

/* 暗色模式适配 */
.dark .bg-grid-pattern {
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.dark ::-webkit-scrollbar-thumb {
  background: #4b5563;
}

.dark ::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}

/* 颜色变量 */
:root {
  --primary: #3b82f6;
  --primary-dark: #2563eb;
  --primary-light: #60a5fa;
  --secondary: #8b5cf6;
}
</style>
