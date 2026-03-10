<template>
  <div class="min-h-screen pt-16">
    <!-- 页面加载遮罩 -->
    <div v-if="isLoading" class="fixed inset-0 z-5 flex items-center justify-center bg-white dark:bg-dark-900 transition-opacity duration-1000"
         :class="{'opacity-0 pointer-events-none': !isLoading}">
      <div class="text-center">
        <div class="relative mb-8">
          <div class="w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-2xl flex items-center justify-center mx-auto shadow-2xl animate-pulse">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l5 5a2 2 0 01.586 1.414V19a2 2 0 01-2 2H7a2 2 0 01-2-2V5a2 2 0 012-2z"></path>
            </svg>
          </div>
          <div class="absolute inset-0 border-4 border-primary/20 border-t-primary rounded-2xl animate-spin"></div>
        </div>
        <h3 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-2">
          标签云
        </h3>
        <p class="text-gray-600 dark:text-gray-400">发现热门话题...</p>
      </div>
    </div>

    <!-- 页面内容 -->
    <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="transition-opacity duration-1000">
      <!-- 英雄区域 - 标签专属 -->
      <section class="relative min-h-[40vh] flex items-center justify-center overflow-hidden pt-8">
        <!-- 背景装饰 - 气泡效果 -->
        <div class="absolute inset-0 -z-10">
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_30%,#8881_1px,transparent_1px)] bg-[size:30px_30px]"></div>
          <div v-for="i in 20" :key="i" class="absolute rounded-full bg-gradient-to-r from-primary/10 to-secondary/10 animate-float"
               :style="{
                 width: `${Math.random() * 100 + 50}px`,
                 height: `${Math.random() * 100 + 50}px`,
                 left: `${Math.random() * 100}%`,
                 top: `${Math.random() * 100}%`,
                 animationDelay: `${Math.random() * 5}s`,
                 animationDuration: `${Math.random() * 10 + 10}s`
               }">
          </div>
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
            <span class="text-gray-600 dark:text-gray-300">标签云</span>
          </nav>

          <h1 class="text-5xl md:text-7xl font-black mb-6 transition-all duration-1000 delay-300"
              :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <span class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              标签云集
            </span>
          </h1>

          <p class="text-xl text-gray-600 dark:text-gray-400 max-w-2xl mx-auto transition-all duration-1000 delay-500"
             :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            {{ totalTags }} 个标签，汇聚技术精华
          </p>
        </div>
      </section>

      <!-- 标签云区域 -->
      <section class="py-20">
        <div class="container mx-auto px-4">
          <!-- 搜索和过滤 -->
          <div class="max-w-md mx-auto mb-16 transition-all duration-1000"
               :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <div class="relative global-card">
              <input type="text" v-model="searchQuery" placeholder="搜索标签..."
                     class="w-full px-6 py-4 bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-2xl border border-white/20 dark:border-dark-700/20 shadow-xl focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all global-card">
              <svg class="absolute right-4 top-4 w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>
          </div>

          <!-- 标签分类导航 -->
          <div class="flex flex-wrap justify-center gap-3 mb-12 transition-all duration-1000"
               :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})` }">
            <button v-for="category in tagCategories" :key="category"
                    @click="selectedCategory = category"
                    class="px-6 py-2 rounded-full transition-all duration-300"
                    :class="selectedCategory === category
                      ? 'bg-gradient-to-r from-primary to-secondary text-white shadow-lg scale-105'
                      : 'bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl text-gray-600 dark:text-gray-400 hover:scale-105 global-card'">
              {{ category }}
            </button>
          </div>

          <!-- 3D标签云效果 -->
          <div class="relative min-h-[500px] flex items-center justify-center mb-20 transition-all duration-1000"
               :style="{ opacity: contentLoaded ? 1 : 0 }">
            <div class="relative w-full max-w-4xl">
              <!-- 中心球体 -->
              <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-32 h-32 bg-gradient-to-r from-primary to-secondary rounded-full blur-xl opacity-20 animate-pulse-slow"></div>

              <!-- 标签球 -->
              <div class="relative" ref="tagCloud" id="tagCloud" style="transform-style: preserve-3d; animation: rotate 30s linear infinite;">
                <span v-for="(tag, index) in filteredTags" :key="index"
                      @click="selectTag(tag)"
                      class="absolute cursor-pointer transition-all duration-300 hover:scale-150 hover:z-50"
                      :style="getTagStyle(index, filteredTags.length)">
                  <span class="relative group">
                    <span class="absolute inset-0 bg-gradient-to-r from-primary to-secondary rounded-full blur-lg opacity-0 group-hover:opacity-50 transition-opacity"></span>
                    <span class="relative px-4 py-2 bg-white dark:bg-dark-800 rounded-full shadow-lg hover:shadow-2xl border border-white/20 dark:border-dark-700/20 whitespace-nowrap global-card"
                          :class="{
                            'text-primary': tag.count > 50,
                            'text-secondary': tag.count > 30 && tag.count <= 50,
                            'text-gray-600': tag.count <= 30
                          }">
                      #{{ tag.name }}
                      <span class="ml-2 text-xs opacity-60">{{ tag.count }}</span>
                    </span>
                  </span>
                </span>
              </div>
            </div>
          </div>

          <!-- 标签卡片网格 -->
          <div class="mt-20">
            <h2 class="text-3xl font-bold text-center mb-12 bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              热门标签
            </h2>

            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              <div v-for="(tag, index) in topTags" :key="index"
                   class="group relative bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-2xl p-6 border border-white/20 dark:border-dark-700/20 shadow-xl hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 cursor-pointer global-card"
                   @click="selectTag(tag)"
                   :style="{ opacity: contentLoaded ? 1 : 0, transform: `translateY(${contentLoaded ? '0' : '20px'})`, transitionDelay: `${index * 50}ms` }">

                <!-- 装饰光效 -->
                <div class="absolute -inset-2 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-3xl blur-xl opacity-0 group-hover:opacity-100 transition-all duration-500"></div>

                <div class="relative z-10">
                  <div class="flex items-center justify-between mb-4">
                    <div class="text-4xl">🏷️</div>
                    <div class="text-3xl font-bold text-primary">{{ tag.count }}</div>
                  </div>

                  <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-3 group-hover:text-primary transition-colors">
                    #{{ tag.name }}
                  </h3>

                  <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
                    {{ tag.description }}
                  </p>

                  <!-- 相关分类 -->
                  <div class="flex flex-wrap gap-2 mb-4">
                    <span v-for="cat in tag.categories.slice(0, 3)" :key="cat"
                          class="px-2 py-1 bg-primary/5 text-xs rounded-full text-primary">
                      {{ cat }}
                    </span>
                  </div>

                  <!-- 最新文章预览 -->
                  <div class="space-y-2">
                    <div v-for="article in tag.recentArticles.slice(0, 2)" :key="article.id"
                         class="text-sm text-gray-500 truncate hover:text-primary transition-colors">
                      • {{ article }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 相关文章推荐 -->
          <div v-if="selectedTag" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm transition-all duration-500"
               @click.self="selectedTag = null">
            <div class="relative w-full max-w-4xl max-h-[80vh] overflow-y-auto bg-white dark:bg-dark-800 rounded-3xl shadow-2xl">

              <!-- 头部 -->
              <div class="sticky top-0 bg-gradient-to-r from-primary to-secondary p-8 text-white rounded-t-3xl">
                <button @click="selectedTag = null" class="absolute top-4 right-4 text-white/80 hover:text-white">
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </button>

                <div class="flex items-center">
                  <div class="text-6xl mr-4">🏷️</div>
                  <div>
                    <h2 class="text-3xl font-bold mb-2">#{{ selectedTag.name }}</h2>
                    <p class="text-white/80">{{ selectedTag.description }}</p>
                  </div>
                </div>

                <div class="flex mt-4 space-x-4 text-sm">
                  <span>{{ selectedTag.count }} 篇文章</span>
                  <span>·</span>
                  <span>{{ selectedTag.followers }} 人关注</span>
                </div>
              </div>

              <!-- 文章列表 -->
              <div class="p-8">
                <div class="flex items-center justify-between mb-6">
                  <h3 class="text-xl font-semibold">相关文章</h3>
                  <button class="text-primary hover:text-primary/80">关注标签</button>
                </div>

                <div class="space-y-4">
                  <div v-for="article in selectedTag.articles" :key="article.id"
                       class="group p-6 bg-gray-50 dark:bg-dark-900 rounded-xl hover:shadow-lg transition-all duration-300 cursor-pointer"
                       @click="goArticleById(article.id)">
                    <div class="flex items-start justify-between">
                      <div class="flex-1">
                        <h4 class="text-lg font-semibold text-gray-800 dark:text-white mb-2 group-hover:text-primary transition-colors">
                          {{ article.title }}
                        </h4>
                        <p class="text-gray-600 dark:text-gray-400 text-sm mb-3">{{ article.excerpt }}</p>
                        <div class="flex items-center space-x-4 text-xs text-gray-500">
                          <span>{{ article.date }}</span>
                          <span>👁️ {{ article.views }}</span>
                          <span>❤️ {{ article.likes }}</span>
                          <span>💬 {{ article.comments }}</span>
                        </div>
                      </div>
                      <div class="ml-4 text-primary opacity-0 group-hover:opacity-100 transition-opacity">
                        阅读全文 →
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
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
const searchQuery = ref('');
const selectedCategory = ref('全部');
const selectedTag = ref(null);
const tagCloud = ref(null);

const tagCategories = ref(['全部', '前端', '后端', '人工智能', '移动开发', 'DevOps', '数据库', '工具']);

// 标签数据
const tags = ref([]);

const filteredTags = computed(() => {
  let filtered = tags.value;

  if (searchQuery.value) {
    filtered = filtered.filter(tag =>
      tag.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }

  if (selectedCategory.value !== '全部') {
    filtered = filtered.filter(tag =>
      tag.categories.includes(selectedCategory.value)
    );
  }

  return filtered;
});

const topTags = computed(() => {
  return [...filteredTags.value]
    .sort((a, b) => b.count - a.count)
    .slice(0, 8);
});

const totalTags = computed(() => tags.value.length);
const maxTagCount = computed(() => tags.value.length > 0 ? Math.max(...tags.value.map(t => t.count)) : 1);

// 3D标签云样式计算
const getTagStyle = (index, total) => {
  const radius = 300;
  const angle = (index / total) * Math.PI * 2;
  const verticalAngle = Math.acos(2 * Math.random() - 1);

  const x = radius * Math.sin(verticalAngle) * Math.cos(angle);
  const y = radius * Math.sin(verticalAngle) * Math.sin(angle);
  const z = radius * Math.cos(verticalAngle);

  const fontSize = 0.8 + (tags.value[index].count / maxTagCount.value) * 0.7;

  return {
    transform: `translate3d(${x}px, ${y}px, ${z}px)`,
    fontSize: `${fontSize}rem`,
    left: '50%',
    top: '50%',
    marginLeft: '-60px',
    marginTop: '-20px'
  };
};

const selectTag = (tag) => {
  // 从后端获取与标签相关的文章
  getArticlesByTag(tag.name);
};

const getArticlesByTag = (tagName) => {
  get(
    'api/article/getArticlesByTag',
    { tag: tagName },
    (message, data) => {
      if (data) {
        // 找到对应的标签
        const tag = tags.value.find(t => t.name === tagName);
        if (tag) {
          selectedTag.value = {
            ...tag,
            articles: data.map(article => ({
              id: article.id,
              title: article.title,
              excerpt: article.content.substring(0, 100) + '...',
              date: article.time ? article.time.substring(0, 10) : '',
              views: article.views || 0,
              likes: article.likes || 0,
              comments: 0
            }))
          };
        }
      }
    }
  );
};

const goArticleById = (id) => {
  router.push(`/view/${id}`);
};

const loadTags = () => {
  get(
    'api/articleInfo/getAllArticleInfo',
    {},
    (message, data) => {
      if (data) {
        // 处理标签数据
        const tagMap = new Map();
        data.forEach(item => {
          if (item.tag) {
            const tagList = item.tag.split(',');
            tagList.forEach(tag => {
              if (tag.trim()) {
                if (!tagMap.has(tag)) {
                  tagMap.set(tag, {
                    name: tag,
                    count: 0,
                    followers: Math.floor(Math.random() * 200) + 50,
                    description: `关于${tag}的文章集合`,
                    categories: getTagCategories(tag),
                    recentArticles: [`${tag} 入门指南`, `${tag} 高级技巧`]
                  });
                }
                tagMap.get(tag).count++;
              }
            });
          }
        });
        tags.value = Array.from(tagMap.values());
      }
    }
  );
};

const getTagCategories = (tagName) => {
  const categoryMap = {
    'Vue 3': ['前端'],
    'React': ['前端'],
    'TypeScript': ['前端'],
    'Spring Boot': ['后端'],
    'Docker': ['DevOps'],
    'Kubernetes': ['DevOps'],
    'Python': ['人工智能', '后端'],
    '机器学习': ['人工智能'],
    'Flutter': ['移动开发'],
    'MySQL': ['数据库'],
    'Redis': ['数据库'],
    'Webpack': ['前端'],
    'Node.js': ['后端'],
    'GraphQL': ['后端'],
    'TensorFlow': ['人工智能'],
    'MongoDB': ['数据库']
  };
  return categoryMap[tagName] || ['其他'];
};

onMounted(() => {
  loadTags();
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
/* 标签页面特有样式 */
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

.animate-float {
  animation: float ease-in-out infinite;
}

@keyframes rotate {
  from { transform: rotateX(0deg) rotateY(0deg); }
  to { transform: rotateX(360deg) rotateY(360deg); }
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@keyframes pulse-slow {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 0.4; }
}

.animate-pulse-slow {
  animation: pulse-slow 4s ease-in-out infinite;
}

/* 确保标签云不挡住导航栏 */
@media (max-width: 768px) {
  .relative.min-h-\[500px\] {
    min-height: 400px;
  }
  
  #tagCloud {
    font-size: 0.8rem;
  }
}

/* 调整标签云容器的位置，确保不与导航栏重叠 */
.relative.w-full.max-w-4xl {
  margin-top: 2rem;
  margin-bottom: 2rem;
}
</style>
