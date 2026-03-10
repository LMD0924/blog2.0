<template>
  <div class="min-h-screen pt-16">
    <!-- 页面加载遮罩 -->
    <div v-if="isLoading" class="fixed inset-0 z-5 flex items-center justify-center bg-white dark:bg-dark-900 transition-opacity duration-1000"
         :class="{'opacity-0 pointer-events-none': !isLoading}">
      <div class="text-center">
        <div class="relative mb-8">
          <div class="w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-2xl flex items-center justify-center mx-auto shadow-2xl animate-pulse">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
          </div>
          <div class="absolute inset-0 border-4 border-primary/20 border-t-primary rounded-2xl animate-spin"></div>
        </div>
        <h3 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-2">
          分类目录
        </h3>
        <p class="text-gray-600 dark:text-gray-400">探索知识领域...</p>
      </div>
    </div>

    <!-- 页面内容 -->
    <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="transition-opacity duration-1000">
      <!-- 英雄区域 - 分类专属 -->
      <section class="relative min-h-[40vh] flex items-center justify-center overflow-hidden pt-8">
        <!-- 背景装饰 - 网格图案 -->
        <div class="absolute inset-0 -z-10">
          <div class="absolute inset-0 bg-[linear-gradient(to_right,#8882_1px,transparent_1px),linear-gradient(to_bottom,#8882_1px,transparent_1px)] bg-[size:4rem_4rem]"></div>
          <div class="absolute top-0 left-0 right-0 h-96 bg-gradient-to-b from-primary/10 to-transparent"></div>
          <div class="absolute bottom-0 left-0 right-0 h-96 bg-gradient-to-t from-secondary/10 to-transparent"></div>
        </div>

        <div class="container mx-auto px-4 py-16 text-center relative z-10">
          <!-- 面包屑 -->
          <nav class="inline-flex items-center px-6 py-3 bg-white/80 dark:bg-dark-800/80 backdrop-blur-lg rounded-full border border-white/20 dark:border-dark-700/20 shadow-2xl mb-8 transition-all duration-1000"
               :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <router-link to="/" class="text-primary hover:text-primary/80 transition-colors">
              <svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
              </svg>
            </router-link>
            <span class="mx-2 text-gray-400">/</span>
            <span class="text-gray-600 dark:text-gray-300">文章分类</span>
          </nav>

          <!-- 主标题 -->
          <h1 class="text-5xl md:text-7xl font-black mb-6 transition-all duration-1000 delay-300"
              :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <span class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              知识分类
            </span>
          </h1>

          <p class="text-xl text-gray-600 dark:text-gray-400 max-w-2xl mx-auto transition-all duration-1000 delay-500"
             :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            探索不同的技术领域，找到你感兴趣的方向
          </p>
        </div>
      </section>

      <!-- 分类网格 -->
      <section class="py-20">
        <div class="container mx-auto px-4">
          <!-- 分类统计 -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-16">
            <div class="bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-2xl p-6 border border-white/20 dark:border-dark-700/20 shadow-xl text-center global-card"
                 v-for="(stat, index) in categoryStats" :key="index"
                 :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})`, transitionDelay: `${index * 100}ms` }">
              <div class="text-3xl mb-2">{{ stat.icon }}</div>
              <div class="text-2xl font-bold text-primary">{{ stat.value }}</div>
              <div class="text-gray-600 dark:text-gray-400">{{ stat.label }}</div>
            </div>
          </div>

          <!-- 分类卡片网格 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div v-for="(category, index) in categories" :key="index"
                 class="group relative"
                 :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '30px'})`, transitionDelay: `${index * 100}ms` }">

              <!-- 卡片容器 -->
              <div class="relative bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-3xl p-8 border border-white/20 dark:border-dark-700/20 shadow-2xl hover:shadow-3xl transition-all duration-700 hover:-translate-y-2 overflow-hidden cursor-pointer global-card"
                   @click="selectCategory(category)">

                <!-- 背景光效 -->
                <div class="absolute -inset-2 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-3xl blur-xl opacity-0 group-hover:opacity-100 transition-all duration-700"></div>

                <!-- 装饰图案 -->
                <div class="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-primary/5 to-secondary/5 rounded-full blur-2xl transform translate-x-16 -translate-y-16 group-hover:translate-x-8 group-hover:-translate-y-8 transition-transform duration-700"></div>

                <!-- 内容区域 -->
                <div class="relative z-10">
                  <!-- 分类图标和计数 -->
                  <div class="flex items-start justify-between mb-6">
                    <div class="w-16 h-16 bg-gradient-to-br from-primary to-secondary rounded-2xl flex items-center justify-center text-3xl text-white shadow-lg group-hover:scale-110 group-hover:rotate-3 transition-all duration-500">
                      {{ category.icon }}
                    </div>
                    <div class="text-right">
                      <div class="text-3xl font-bold text-primary">{{ category.count }}</div>
                      <div class="text-sm text-gray-500">篇文章</div>
                    </div>
                  </div>

                  <!-- 分类信息 -->
                  <h3 class="text-2xl font-bold text-gray-800 dark:text-white mb-3 group-hover:text-primary transition-colors">
                    {{ category.name }}
                  </h3>
                  <p class="text-gray-600 dark:text-gray-400 mb-6 line-clamp-2">
                    {{ category.description }}
                  </p>

                  <!-- 标签预览 -->
                  <div class="flex flex-wrap gap-2 mb-6">
                    <span v-for="(tag, tagIndex) in category.tags.slice(0, 3)" :key="tagIndex"
                          class="px-3 py-1 bg-primary/10 text-primary rounded-full text-xs">
                      {{ tag }}
                    </span>
                    <span v-if="category.tags.length > 3" class="px-3 py-1 bg-gray-100 dark:bg-dark-700 text-gray-500 rounded-full text-xs">
                      +{{ category.tags.length - 3 }}
                    </span>
                  </div>

                  <!-- 进度条 -->
                  <div class="relative h-2 bg-gray-100 dark:bg-dark-700 rounded-full overflow-hidden">
                    <div class="absolute top-0 left-0 h-full bg-gradient-to-r from-primary to-secondary rounded-full transition-all duration-1000"
                         :style="{ width: `${(category.count / maxCategoryCount) * 100}%` }">
                    </div>
                  </div>

                  <!-- 查看详情 -->
                  <div class="mt-4 flex items-center justify-between text-sm">
                    <span class="text-primary group-hover:translate-x-2 transition-transform duration-300 flex items-center">
                      查看分类文章
                      <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                      </svg>
                    </span>
                    <span class="text-gray-400">最新更新 {{ category.lastUpdate }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 选中的分类文章详情模态框 -->
      <div v-if="selectedCategory" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm transition-all duration-500"
           @click.self="selectedCategory = null">
        <div class="relative w-full max-w-4xl max-h-[80vh] overflow-y-auto bg-white dark:bg-dark-800 rounded-3xl shadow-2xl transform transition-all duration-500 scale-100">

          <!-- 模态框头部 -->
          <div class="sticky top-0 z-10 bg-gradient-to-r from-primary to-secondary p-8 text-white rounded-t-3xl">
            <button @click="selectedCategory = null" class="absolute top-4 right-4 text-white/80 hover:text-white transition-colors">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
            <div class="flex items-center">
              <div class="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center text-4xl mr-4">
                {{ selectedCategory.icon }}
              </div>
              <div>
                <h2 class="text-3xl font-bold mb-2">{{ selectedCategory.name }}</h2>
                <p class="text-white/80">{{ selectedCategory.description }}</p>
              </div>
            </div>
          </div>

          <!-- 文章列表 -->
          <div class="p-8">
            <div class="mb-6 flex items-center justify-between">
              <h3 class="text-xl font-semibold text-gray-800 dark:text-white">
                共 {{ selectedCategory.articles.length }} 篇文章
              </h3>
              <div class="flex space-x-2">
                <button class="px-4 py-2 bg-primary/10 text-primary rounded-xl hover:bg-primary hover:text-white transition-colors">
                  最新
                </button>
                <button class="px-4 py-2 bg-gray-100 dark:bg-dark-700 text-gray-600 dark:text-gray-400 rounded-xl hover:bg-primary/10 hover:text-primary transition-colors">
                  最热
                </button>
              </div>
            </div>

            <div class="space-y-4">
              <div v-for="(article, index) in selectedCategory.articles" :key="index"
                   class="group relative bg-gray-50 dark:bg-dark-900 rounded-2xl p-6 hover:shadow-xl transition-all duration-500 hover:-translate-y-1 cursor-pointer"
                   @click="goArticleById(article.id)">
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <h4 class="text-lg font-semibold text-gray-800 dark:text-white mb-2 group-hover:text-primary transition-colors">
                      {{ article.title }}
                    </h4>
                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-4">{{ article.excerpt }}</p>
                    <div class="flex items-center space-x-4 text-xs text-gray-500">
                      <span>{{ article.date }}</span>
                      <span>👁️ {{ article.views }}</span>
                      <span>❤️ {{ article.likes }}</span>
                      <span>💬 {{ article.comments }}</span>
                    </div>
                  </div>
                  <div class="ml-4 p-2 bg-primary/10 rounded-xl text-primary group-hover:bg-primary group-hover:text-white transition-all duration-300">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { get } from '@/net/index.js';

const router = useRouter();
const isLoading = ref(true);
const contentLoaded = ref(false);
const selectedCategory = ref(null);

// 分类统计数据
const categoryStats = ref([
  { icon: '📚', value: '0', label: '总分类' },
  { icon: '✍️', value: '0', label: '总文章' },
  { icon: '🏷️', value: '0', label: '子分类' },
  { icon: '👥', value: '0', label: '关注者' }
]);

// 分类数据
const categories = ref([]);

const maxCategoryCount = computed(() => {
  if (categories.value.length === 0) return 1;
  return Math.max(...categories.value.map(c => c.count));
});

const selectCategory = (category) => {
  selectedCategory.value = category;
};

const goArticleById = (id) => {
  router.push(`/view/${id}`);
};

const loadCategories = () => {
  // 首先获取所有文章信息，包含分类数据
  get(
    'api/articleInfo/getAllArticleInfo',
    {},
    (message, articleInfos) => {
      console.log('文章信息数据:', articleInfos);
      if (articleInfos && articleInfos.length > 0) {
        // 获取所有文章数据
        get(
          'api/article/getAllArticle',
          {},
          (message, articlesData) => {
            console.log('文章数据:', articlesData);
            if (articlesData) {
              // 创建文章ID到文章对象的映射
              const articleMap = new Map();
              articlesData.forEach(item => {
                const article = item.article || item;
                if (article) {
                  articleMap.set(article.id, article);
                }
              });
              
              // 处理分类数据
              const categoryMap = new Map();
              let totalArticles = 0;
              
              articleInfos.forEach(info => {
                const article = articleMap.get(info.articleId);
                if (article) {
                  totalArticles++;
                  const categoryName = info.classification || '未分类';
                  
                  if (!categoryMap.has(categoryName)) {
                    categoryMap.set(categoryName, {
                      id: categoryMap.size + 1,
                      name: categoryName,
                      icon: getCategoryIcon(categoryName),
                      count: 0,
                      description: getCategoryDescription(categoryName),
                      tags: info.tag ? info.tag.split(',').map(tag => tag.trim()) : [],
                      lastUpdate: '刚刚',
                      articles: []
                    });
                  }
                  
                  const category = categoryMap.get(categoryName);
                  category.count++;
                  category.articles.push({
                    id: article.id,
                    title: article.title,
                    excerpt: article.content.substring(0, 100) + '...',
                    date: article.time ? article.time.substring(0, 10) : '未知',
                    views: article.views || 0,
                    likes: article.likes || 0,
                    comments: 0
                  });
                }
              });
              
              categories.value = Array.from(categoryMap.values());
              
              // 更新统计数据
              categoryStats.value[0].value = categories.value.length.toString();
              categoryStats.value[1].value = totalArticles.toString();
              
              console.log('分类数据:', categories.value);
            }
          },
          (message, data) => {
            console.error('获取文章数据失败:', message);
          },
          (error) => {
            console.error('获取文章数据错误:', error);
          }
        );
      }
    },
    (message, data) => {
      console.error('获取文章信息失败:', message);
    },
    (error) => {
      console.error('获取文章信息错误:', error);
    }
  );
};

// 获取分类图标
const getCategoryIcon = (categoryName) => {
  const iconMap = {
    '前端开发': '⚛️',
    '后端架构': '⚙️',
    '人工智能': '🧠',
    '移动开发': '📱',
    'DevOps': '🚀',
    '数据库技术': '🗄️',
    '未分类': '📝'
  };
  return iconMap[categoryName] || '📁';
};

// 获取分类描述
const getCategoryDescription = (categoryName) => {
  const descMap = {
    '前端开发': '探索现代前端技术，包括 Vue、React、Angular 等框架的最佳实践',
    '后端架构': '深入后端开发，涵盖微服务、分布式系统、数据库设计等',
    '人工智能': 'AI/ML技术前沿，包括深度学习、自然语言处理等',
    '移动开发': 'iOS、Android、跨平台移动应用开发技术',
    'DevOps': '持续集成、持续部署、自动化运维实践',
    '数据库技术': '关系型与非关系型数据库的原理与应用',
    '未分类': '未分类的文章内容'
  };
  return descMap[categoryName] || '该分类下的文章集合';
};

onMounted(() => {
  loadCategories();
  setTimeout(() => {
    isLoading.value = false;
    nextTick(() => {
      setTimeout(() => {
        contentLoaded.value = true;
      }, 100);
    });
  }, 1500);
});
</script>

<style scoped>
/* 分类页面特有样式 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.shadow-3xl {
  box-shadow: 0 35px 60px -15px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
}

@keyframes pulse-slow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.8; }
}

.animate-pulse-slow {
  animation: pulse-slow 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* 网格背景动画 */
.bg-grid {
  background-size: 50px 50px;
  background-image:
    linear-gradient(to right, rgba(128, 128, 128, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(128, 128, 128, 0.05) 1px, transparent 1px);
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { background-position: 0 0; }
  100% { background-position: 50px 50px; }
}
</style>
