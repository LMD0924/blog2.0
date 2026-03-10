<template>
  <div class="min-h-screen pt-16">
    <!-- 页面加载遮罩 -->
    <div v-if="isLoading" class="fixed inset-0 z-5 flex items-center justify-center bg-white dark:bg-dark-900 transition-opacity duration-1000"
         :class="{'opacity-0 pointer-events-none': !isLoading}">
      <div class="text-center">
        <div class="relative mb-8">
          <div class="w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-2xl flex items-center justify-center mx-auto shadow-2xl animate-pulse">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
            </svg>
          </div>
          <div class="absolute inset-0 border-4 border-primary/20 border-t-primary rounded-2xl animate-spin"></div>
        </div>
        <h3 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-2">
          时光档案
        </h3>
        <p class="text-gray-600 dark:text-gray-400">回溯知识足迹...</p>
      </div>
    </div>

    <!-- 页面内容 -->
    <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="transition-opacity duration-1000">
      <!-- 英雄区域 - 归档专属 -->
      <section class="relative min-h-[40vh] flex items-center justify-center overflow-hidden pt-8">
        <!-- 背景装饰 - 时间纹理 -->
        <div class="absolute inset-0 -z-10">
          <div class="absolute inset-0 bg-[linear-gradient(45deg,#8881_1px,transparent_1px)] bg-[size:40px_40px]"></div>
          <div class="absolute top-0 left-0 w-full h-full bg-gradient-to-b from-transparent via-primary/5 to-transparent animate-slow-spin"></div>
        </div>

        <div class="container mx-auto px-4 py-16 text-center relative z-10">
          <nav class="inline-flex items-center px-6 py-3 bg-white/80 dark:bg-dark-800/80 backdrop-blur-lg rounded-full border border-white/20 dark:border-dark-700/20 shadow-2xl mb-8 transition-all duration-1000"
               :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <router-link to="/" class="text-primary hover:text-primary/80 transition-colors">
              <svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
              </svg>
            </router-link>
            <span class="mx-2 text-gray-400">/</span>
            <span class="text-gray-600 dark:text-gray-300">文章归档</span>
          </nav>

          <h1 class="text-5xl md:text-7xl font-black mb-6 transition-all duration-1000 delay-300"
              :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <span class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              时光档案馆
            </span>
          </h1>

          <!-- 时间统计 -->
          <div class="flex items-center justify-center space-x-8 text-gray-600 dark:text-gray-400 transition-all duration-1000 delay-500"
               :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <div class="text-center">
              <div class="text-4xl font-bold text-primary">{{ totalYears }}</div>
              <div>年</div>
            </div>
            <div class="text-4xl text-primary/30">·</div>
            <div class="text-center">
              <div class="text-4xl font-bold text-secondary">{{ totalMonths }}</div>
              <div>个月</div>
            </div>
            <div class="text-4xl text-primary/30">·</div>
            <div class="text-center">
              <div class="text-4xl font-bold text-purple-500">{{ totalArticles }}</div>
              <div>篇文章</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 时间轴视图切换 -->
      <section class="py-12">
        <div class="container mx-auto px-4">
          <div class="flex justify-center space-x-4 mb-16">
            <button v-for="view in timelineViews" :key="view.key"
                    @click="activeView = view.key"
                    class="px-8 py-3 rounded-2xl transition-all duration-500"
                    :class="activeView === view.key
                      ? 'bg-gradient-to-r from-primary to-secondary text-white shadow-2xl scale-110'
                      : 'bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl text-gray-600 dark:text-gray-400 hover:scale-105'">
              {{ view.label }}
            </button>
          </div>

          <!-- 年份视图 -->
          <div v-if="activeView === 'year'" class="space-y-16">
            <div v-for="(yearData, year) in archives" :key="year"
                 class="relative group"
                 :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">

              <!-- 年份卡片 -->
              <div class="sticky top-20 z-10 mb-8">
                <div class="inline-flex items-center px-8 py-4 bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-2xl border border-white/20 dark:border-dark-700/20 shadow-xl hover:shadow-2xl transition-all duration-500 global-card">
                  <span class="text-5xl font-bold text-primary mr-4">{{ year }}</span>
                  <div>
                    <div class="text-2xl font-semibold text-gray-800 dark:text-white">{{ yearData.total }} 篇文章</div>
                    <div class="text-sm text-gray-500">涵盖 {{ Object.keys(yearData.months).length }} 个月</div>
                  </div>
                </div>
              </div>

              <!-- 月份网格 -->
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 pl-8">
                <div v-for="(monthData, month) in yearData.months" :key="month"
                     class="relative bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-2xl p-6 border border-white/20 dark:border-dark-700/20 shadow-xl hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 cursor-pointer global-card"
                     @click="expandMonth(year, month)">

                  <!-- 月份装饰 -->
                  <div class="absolute -top-3 -left-3 w-10 h-10 bg-gradient-to-br from-primary to-secondary rounded-xl flex items-center justify-center text-white font-bold shadow-lg">
                    {{ month }}
                  </div>

                  <div class="ml-4">
                    <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-2">{{ month }}月</h3>
                    <div class="text-3xl font-bold text-primary mb-2">{{ monthData.count }}</div>
                    <div class="text-gray-500 mb-4">篇文章</div>

                    <!-- 文章预览 -->
                    <div class="space-y-2">
                      <div v-for="article in monthData.articles.slice(0, 2)" :key="article.id"
                           class="text-sm text-gray-600 dark:text-gray-400 truncate hover:text-primary transition-colors">
                        • {{ article.title }}
                      </div>
                      <div v-if="monthData.articles.length > 2" class="text-sm text-primary">
                        +{{ monthData.articles.length - 2 }} 篇更多
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 时间轴视图 -->
          <div v-if="activeView === 'timeline'" class="relative max-w-4xl mx-auto">
            <!-- 时间轴线 -->
            <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-primary via-secondary to-primary opacity-30"></div>

            <div v-for="(item, index) in timelineData" :key="index"
                 class="relative mb-12 pl-16 group"
                 :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})`, transitionDelay: `${index * 100}ms` }">

              <!-- 时间节点 -->
              <div class="absolute left-0 top-0 w-16 h-16 bg-gradient-to-br from-primary to-secondary rounded-2xl flex items-center justify-center text-white font-bold shadow-2xl group-hover:scale-110 group-hover:rotate-12 transition-all duration-500">
                {{ item.day }}
              </div>

              <!-- 时间标签 -->
              <div class="absolute left-20 top-4 text-sm text-gray-500">
                {{ item.date }}
              </div>

              <!-- 内容卡片 -->
              <div class="bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-2xl p-6 border border-white/20 dark:border-dark-700/20 shadow-xl hover:shadow-2xl transition-all duration-500 ml-8"
                   @click="goArticleById(item.article.id)">

                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-2 group-hover:text-primary transition-colors">
                      {{ item.article.title }}
                    </h3>
                    <p class="text-gray-600 dark:text-gray-400 mb-4">{{ item.article.excerpt }}</p>

                    <div class="flex items-center space-x-4 text-sm">
                      <span class="text-gray-500">👁️ {{ item.article.views }}</span>
                      <span class="text-gray-500">❤️ {{ item.article.likes }}</span>
                      <span class="text-gray-500">💬 {{ item.article.comments }}</span>
                    </div>
                  </div>

                  <!-- 月份标识 -->
                  <div class="ml-4 text-4xl opacity-10 group-hover:opacity-30 transition-opacity">
                    {{ item.month }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 日历热力图视图 -->
          <div v-if="activeView === 'heatmap'" class="bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-3xl p-8 border border-white/20 dark:border-dark-700/20 shadow-xl">
            <h3 class="text-2xl font-bold text-center mb-8 bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              2024 写作热力图
            </h3>

            <!-- 月份标签 -->
            <div class="flex justify-around mb-4 text-sm text-gray-500">
              <span>1月</span><span>2月</span><span>3月</span><span>4月</span><span>5月</span><span>6月</span>
              <span>7月</span><span>8月</span><span>9月</span><span>10月</span><span>11月</span><span>12月</span>
            </div>

            <!-- 热力图网格 -->
            <div class="grid grid-cols-52 gap-1">
              <div v-for="week in 52" :key="week" class="space-y-1">
                <div v-for="day in 7" :key="day"
                     class="w-4 h-4 rounded-sm transition-all duration-300 hover:scale-150"
                     :class="getHeatmapColor(Math.floor(Math.random() * 5))">
                </div>
              </div>
            </div>

            <!-- 图例 -->
            <div class="flex items-center justify-end mt-4 space-x-4 text-xs">
              <div class="flex items-center">
                <div class="w-3 h-3 bg-gray-100 dark:bg-dark-700 rounded-sm mr-1"></div>
                <span>较少</span>
              </div>
              <div class="flex items-center">
                <div class="w-3 h-3 bg-primary/30 rounded-sm mr-1"></div>
                <span>一般</span>
              </div>
              <div class="flex items-center">
                <div class="w-3 h-3 bg-primary/60 rounded-sm mr-1"></div>
                <span>较多</span>
              </div>
              <div class="flex items-center">
                <div class="w-3 h-3 bg-primary rounded-sm mr-1"></div>
                <span>活跃</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 月度详情模态框 -->
      <div v-if="selectedMonth" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm transition-all duration-500"
           @click.self="selectedMonth = null">
        <div class="relative w-full max-w-2xl max-h-[70vh] overflow-y-auto bg-white dark:bg-dark-800 rounded-3xl shadow-2xl">
          <div class="sticky top-0 bg-gradient-to-r from-primary to-secondary p-6 text-white rounded-t-3xl">
            <button @click="selectedMonth = null" class="absolute top-4 right-4 text-white/80 hover:text-white">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
            <h2 class="text-2xl font-bold">{{ selectedMonth.year }}年 {{ selectedMonth.month }}月</h2>
            <p class="text-white/80">共 {{ selectedMonth.articles.length }} 篇文章</p>
          </div>

          <div class="p-6 space-y-4">
            <div v-for="article in selectedMonth.articles" :key="article.id"
                 class="group p-4 bg-gray-50 dark:bg-dark-900 rounded-xl hover:shadow-lg transition-all duration-300 cursor-pointer"
                 @click="goArticleById(article.id)">
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="font-semibold text-gray-800 dark:text-white group-hover:text-primary transition-colors">
                    {{ article.title }}
                  </h3>
                  <div class="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                    <span>{{ article.date }}</span>
                    <span>👁️ {{ article.views }}</span>
                  </div>
                </div>
                <span class="text-primary opacity-0 group-hover:opacity-100 transition-opacity">阅读全文 →</span>
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
const activeView = ref('year');
const selectedMonth = ref(null);

const timelineViews = ref([
  { key: 'year', label: '年份视图' },
  { key: 'timeline', label: '时间轴' },
  { key: 'heatmap', label: '热力图' }
]);

// 归档数据
const archives = ref({});

// 时间轴数据
const timelineData = computed(() => {
  const data = [];
  Object.entries(archives.value).forEach(([year, yearData]) => {
    Object.entries(yearData.months).forEach(([month, monthData]) => {
      monthData.articles.forEach(article => {
        data.push({
          date: article.date,
          day: article.date.split('-')[2],
          month: month,
          year: year,
          article: article
        });
      });
    });
  });
  return data.sort((a, b) => new Date(b.date) - new Date(a.date)).slice(0, 20);
});

const totalYears = computed(() => Object.keys(archives.value).length);
const totalMonths = computed(() => {
  let count = 0;
  Object.values(archives.value).forEach(year => {
    count += Object.keys(year.months).length;
  });
  return count;
});
const totalArticles = computed(() => {
  let count = 0;
  Object.values(archives.value).forEach(year => {
    count += year.total;
  });
  return count;
});

const expandMonth = (year, month) => {
  selectedMonth.value = {
    year,
    month,
    articles: archives.value[year].months[month].articles
  };
};

const getHeatmapColor = (level) => {
  const colors = [
    'bg-gray-100 dark:bg-dark-700',
    'bg-primary/20',
    'bg-primary/40',
    'bg-primary/60',
    'bg-primary'
  ];
  return colors[level];
};

const goArticleById = (id) => {
  router.push(`/view/${id}`);
};

const loadArchives = () => {
  get(
    'api/article/getAllArticle',
    {},
    (message, data) => {
      console.log('API 响应数据:', data);
      if (data) {
        // 处理归档数据
        const archiveMap = new Map();
        data.forEach(item => {
          const article = item.article || item;
          console.log('处理文章:', article);
          if (article.time) {
            const date = new Date(article.time);
            const year = date.getFullYear().toString();
            const month = (date.getMonth() + 1).toString();
            
            if (!archiveMap.has(year)) {
              archiveMap.set(year, {
                total: 0,
                months: {}
              });
            }
            
            const yearData = archiveMap.get(year);
            yearData.total++;
            
            if (!yearData.months[month]) {
              yearData.months[month] = {
                count: 0,
                articles: []
              };
            }
            
            yearData.months[month].count++;
            yearData.months[month].articles.push({
              id: article.id,
              title: article.title,
              excerpt: article.content.substring(0, 100) + '...',
              date: article.time.substring(0, 10),
              views: article.views || 0,
              likes: article.likes || 0,
              comments: 0 // 暂时设为0，需要从评论接口获取
            });
          }
        });
        
        archives.value = Object.fromEntries(archiveMap);
        console.log('归档数据:', archives.value);
      }
    },
    (message, data) => {
      console.error('API 请求失败:', message);
    },
    (error) => {
      console.error('API 请求错误:', error);
    }
  );
};

onMounted(() => {
  loadArchives();
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
/* 归档页面特有样式 */
.grid-cols-52 {
  grid-template-columns: repeat(52, minmax(0, 1fr));
}

@keyframes slow-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-slow-spin {
  animation: slow-spin 20s linear infinite;
}
</style>
