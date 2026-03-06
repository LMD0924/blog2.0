<script setup>
import { ref, onMounted } from 'vue';
import { get } from '@/net/index.js';
import { message } from 'ant-design-vue';

const [messageApi, contextHolder] = message.useMessage();

// 标签列表
const tags = ref([]);
// 当前选中的标签
const selectedTag = ref('');
// 标签下的文章
const articles = ref([]);
// 加载状态
const loading = ref(false);

// 获取所有标签
const fetchTags = async () => {
  try {
    loading.value = true;
    get('api/article/tags', {}, (msg, data) => {
      if (data) {
        tags.value = data;
        // 默认选择第一个标签
        if (data.length > 0 && !selectedTag.value) {
          selectedTag.value = data[0];
          fetchArticlesByTag(data[0]);
        }
      }
    });
  } catch (error) {
    messageApi.error('获取标签失败');
  } finally {
    loading.value = false;
  }
};

// 根据标签获取文章
const fetchArticlesByTag = async (tag) => {
  try {
    loading.value = true;
    get(`api/article/tag/${encodeURIComponent(tag)}`, {}, (msg, data) => {
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

// 切换标签
const handleTagChange = (tag) => {
  selectedTag.value = tag;
  fetchArticlesByTag(tag);
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

onMounted(() => {
  fetchTags();
});
</script>

<template>
  <contextHolder />
  <div class="tag-container container mx-auto px-4 py-12">
    <!-- 标题区域 -->
    <div class="header mb-10 animate-fade-in">
      <h1 class="text-3xl md:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary dark:from-primary-light dark:to-secondary-light mb-2">文章标签</h1>
      <p class="text-gray-500 dark:text-gray-400">按标签浏览文章</p>
    </div>

    <!-- 标签和文章区域 -->
    <div class="flex flex-col lg:flex-row gap-8">
      <!-- 标签列表 -->
      <div class="lg:w-1/4">
        <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 animate-slide-up animate-delay-100 global-card">
          <h2 class="text-xl font-semibold mb-4">标签列表</h2>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in tags"
              :key="tag"
              @click="handleTagChange(tag)"
              class="px-4 py-2 rounded-full text-sm transition-all duration-300"
              :class="{
                'bg-primary text-white': selectedTag === tag,
                'bg-gray-100 dark:bg-dark-700 hover:bg-gray-200 dark:hover:bg-dark-600': selectedTag !== tag
              }"
            >
              {{ tag }}
            </button>
          </div>
        </div>
      </div>

      <!-- 文章列表 -->
      <div class="lg:flex-1">
        <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 animate-slide-up animate-delay-200 global-card">
          <h2 class="text-xl font-semibold mb-6">{{ selectedTag || '选择标签' }}</h2>
          
          <div v-if="loading" class="flex justify-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
          </div>
          
          <div v-else-if="articles.length === 0" class="text-center py-12">
            <p class="text-gray-500 dark:text-gray-400">该标签下暂无文章</p>
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
.tag-container {
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