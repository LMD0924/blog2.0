<script setup>
import { ref, onMounted } from 'vue';
import { get } from '@/net/index.js';
import { message } from 'ant-design-vue';

const [messageApi, contextHolder] = message.useMessage();

// 归档数据
const archiveData = ref([]);
// 当前选中的月份
const selectedMonth = ref('');
// 月份下的文章
const articles = ref([]);
// 加载状态
const loading = ref(false);

// 获取归档数据
const fetchArchiveData = async () => {
  try {
    loading.value = true;
    get('api/article/archive', {}, (msg, data) => {
      if (data) {
        archiveData.value = data;
        // 默认选择第一个月份
        if (data.length > 0 && !selectedMonth.value) {
          selectedMonth.value = data[0].month;
          fetchArticlesByMonth(data[0].month);
        }
      }
    });
  } catch (error) {
    messageApi.error('获取归档数据失败');
  } finally {
    loading.value = false;
  }
};

// 根据月份获取文章
const fetchArticlesByMonth = async (month) => {
  try {
    loading.value = true;
    get(`api/article/archive/${month}`, {}, (msg, data) => {
      if (data) {
        articles.value = data;
      }
    });
  } catch (error) {
    messageApi.error('获取文章失败');
  } finally {
    loading.value = false;
  }
};

// 切换月份
const handleMonthChange = (month) => {
  selectedMonth.value = month;
  fetchArticlesByMonth(month);
};

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 格式化月份显示
const formatMonthDisplay = (month) => {
  const [year, monthNum] = month.split('-');
  return `${year}年${parseInt(monthNum)}月`;
};

onMounted(() => {
  fetchArchiveData();
});
</script>

<template>
  <contextHolder />
  <div class="archive-container container mx-auto px-4 py-12">
    <!-- 标题区域 -->
    <div class="header mb-10 animate-fade-in">
      <h1 class="text-3xl md:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary dark:from-primary-light dark:to-secondary-light mb-2">文章归档</h1>
      <p class="text-gray-500 dark:text-gray-400">按时间浏览文章</p>
    </div>

    <!-- 归档和文章区域 -->
    <div class="flex flex-col lg:flex-row gap-8">
      <!-- 归档列表 -->
      <div class="lg:w-1/4">
        <div class="global-card bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 animate-slide-up animate-delay-100">
          <h2 class="text-xl font-semibold mb-4">时间归档</h2>
          <div class="space-y-2">
            <button
              v-for="item in archiveData"
              :key="item.month"
              @click="handleMonthChange(item.month)"
              class="w-full text-left px-4 py-3 rounded-lg transition-all duration-300 flex justify-between items-center"
              :class="{
                'bg-primary/10 text-primary dark:bg-primary/20': selectedMonth === item.month,
                'hover:bg-gray-100 dark:hover:bg-dark-700': selectedMonth !== item.month
              }"
            >
              <span>{{ formatMonthDisplay(item.month) }}</span>
              <span class="text-sm bg-gray-200 dark:bg-dark-600 px-2 py-0.5 rounded-full">{{ item.count }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 文章列表 -->
      <div class="lg:flex-1">
        <div class="global-card bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 animate-slide-up animate-delay-200">
          <h2 class="text-xl font-semibold mb-6">{{ selectedMonth ? formatMonthDisplay(selectedMonth) : '选择月份' }}</h2>
          
          <div v-if="loading" class="flex justify-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
          </div>
          
          <div v-else-if="articles.length === 0" class="text-center py-12">
            <p class="text-gray-500 dark:text-gray-400">该月份下暂无文章</p>
          </div>
          
          <div v-else class="space-y-6">
            <div 
              v-for="article in articles" 
              :key="article.id"
              class="article-card p-6 rounded-xl border border-gray-200 dark:border-dark-700 hover:shadow-md transition-all duration-300"
            >
              <h3 class="text-xl font-semibold mb-2">{{ article.title }}</h3>
              <p class="text-gray-600 dark:text-gray-400 mb-4 line-clamp-2">{{ article.content }}</p>
              <div class="flex justify-between items-center text-sm text-gray-500 dark:text-gray-400">
                <span>{{ formatDate(article.time) }}</span>
                <div class="flex items-center space-x-4">
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                    </svg>
                    {{ article.views }}
                  </span>
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                    </svg>
                    {{ article.likes }}
                  </span>
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                    </svg>
                    {{ article.favorites }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.archive-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 1.5rem;
  margin-bottom: 2rem;
}

.article-card {
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-2px);
}

/* 暗黑模式适配 */
.dark .article-card {
  border-color: rgba(255, 255, 255, 0.1);
}

.dark .header {
  border-color: rgba(255, 255, 255, 0.1);
}
</style>