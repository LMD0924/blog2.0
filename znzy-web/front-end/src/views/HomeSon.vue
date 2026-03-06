<template>
  <!-- 主要内容 -->
  <div class="min-h-screen">
    <!-- 页面加载遮罩 -->
    <div v-if="isLoading" class="fixed inset-0 z-50 flex items-center justify-center bg-white dark:bg-dark-900 transition-opacity duration-1000"
         :class="{'opacity-0 pointer-events-none': !isLoading}">
      <div class="text-center">
        <!-- 加载动画 -->
        <div class="relative mb-8">
          <div class="w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-2xl flex items-center justify-center mx-auto shadow-2xl animate-pulse">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
            </svg>
          </div>
          <!-- 旋转边框 -->
          <div class="absolute inset-0 border-4 border-primary/20 border-t-primary rounded-2xl animate-spin"></div>
        </div>
        <h3 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-2">
          TechBlog
        </h3>
        <p class="text-gray-600 dark:text-gray-400">加载精彩内容中...</p>
      </div>
    </div>

    <!-- 页面内容 -->
    <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="transition-opacity duration-1000">
      <!-- 英雄区域 -->
      <section class="min-h-screen flex flex-col items-center justify-center text-center relative overflow-hidden">
        <!-- 背景装饰 -->
        <div class="absolute inset-0 -z-10">
          <div class="absolute top-10 left-10 w-96 h-96 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-full blur-3xl animate-pulse-slow"></div>
          <div class="absolute bottom-10 right-10 w-80 h-80 bg-gradient-to-r from-secondary/15 to-primary/15 rounded-full blur-3xl animate-pulse-slow animation-delay-2000"></div>
          <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-gradient-to-r from-primary/10 to-secondary/10 rounded-full blur-2xl"></div>
        </div>

        <div class="relative container mx-auto px-4 py-12">
          <!-- 欢迎徽章 -->
          <div class="inline-flex items-center px-6 py-3 bg-white/80 dark:bg-dark-800/80 backdrop-blur-lg rounded-full border border-white/20 dark:border-dark-700/20 shadow-2xl mb-8 transition-all duration-1000"
               :style="{
                 opacity: contentLoaded ? 1 : 0,
                 transform: `translateY(${contentLoaded ? '0' : '20px'})`
               }">
            <div class="w-2 h-2 bg-primary rounded-full mr-3 animate-ping"></div>
            <span class="text-sm font-medium text-primary uppercase tracking-wider">欢迎回来, {{User.account || '访客'}}</span>
          </div>

          <!-- 主标题 -->
          <h3 class="text-white text-5xl md:text-7xl lg:text-8xl font-black mb-8 leading-tight transition-all duration-1000 delay-300">
        落叶，Blog
          </h3>

          <!-- 副标题 -->
          <p class="text-xl md:text-2xl lg:text-3xl text-gray-600 dark:text-gray-300 max-w-4xl mx-auto mb-12 leading-relaxed transition-all duration-1000 delay-500"
             :style="{
               opacity: contentLoaded ? 1 : 0,
               transform: `translateY(${contentLoaded ? '0' : '20px'})`
             }">
            <span class="typing-text text-white">{{ displayText }}</span>
            <span class="typing-cursor">|</span>
          </p>
        </div>
      </section>

      <!-- 热门文章 -->
      <section class="py-24">
        <div class="container mx-auto px-4">
          <div class="flex items-center justify-between mb-12 transition-all duration-1000 delay-300"
               :style="{
                 opacity: contentLoaded ? 1 : 0,
                 transform: `translateY(${contentLoaded ? '0' : '20px'})`
               }">
            <div>
              <h2 class="text-4xl md:text-5xl font-bold mb-4 bg-gradient-to-r from-white to-gray-200 bg-clip-text text-transparent">
                热门文章
              </h2>
              <p class="text-xl text-white/90">社区最受欢迎的技术内容</p>
            </div>
            <router-link
              to="/articles"
              class="group flex items-center text-primary hover:text-primary/80 font-semibold transition-colors"
            >
              查看全部
              <svg class="w-5 h-5 ml-2 transform group-hover:translate-x-1 transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
              </svg>
            </router-link>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <article
              v-for="(article, index) in featuredArticles"
              :key="index"
              class="group relative bg-white/80 dark:bg-dark-800/80 backdrop-blur-xl rounded-3xl p-8 border border-white/20 dark:border-dark-700/20 shadow-2xl hover:shadow-3xl transition-all duration-1000 hover:-translate-y-2 global-card"
              :style="{
                opacity: contentLoaded ? 1 : 0,
                transform: `translateY(${contentLoaded ? '0' : '30px'})`,
                transitionDelay: `${500 + index * 150}ms`
              }"
            >
              <div class="absolute -inset-4 bg-gradient-to-r from-primary/5 to-secondary/5 rounded-3xl blur-xl opacity-0 group-hover:opacity-100 transition-all duration-500"></div>
              <div class="relative">
                <div class="flex items-center justify-between mb-4">
                  <span class="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">{{ article.category || '技术' }}</span>
                  <span class="text-sm text-white/90">{{ article.date || '最近更新' }}</span>
                </div>
                <h3 class="text-2xl font-bold text-white mb-4 group-hover:text-primary transition-colors duration-300 line-clamp-2">
                  {{ article.title || '文章标题' }}
                </h3>
                <p class="text-white/90 mb-6 line-clamp-3 leading-relaxed">
                  {{ article.excerpt || '文章摘要内容，这里展示了文章的主要内容和亮点...' }}
                </p>
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-4">
                    <span class="text-sm text-white/90">👁️ 1.2k</span>
                    <span class="text-sm text-white/90">❤️ 86</span>
                  </div>
                  <button
                    @click="goArticleById(article.id)"
                    class="group flex items-center text-primary hover:text-primary/80 font-medium transition-colors"
                  >
                    阅读全文
                    <svg class="w-4 h-4 ml-2 transform group-hover:translate-x-1 transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </article>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
// 示例数据
import { onMounted, ref, nextTick, onUnmounted } from "vue";
import { get } from "@/net/index.js";
import { message } from "ant-design-vue";
import router from "@/router/index.js";

const [messageApi, contextHolder] = message.useMessage();
const User = ref({});
const featuredArticles = ref([]);
const latestArticles = ref([]);
const isLoading = ref(true);
const contentLoaded = ref(false);

// 打字机效果
const displayText = ref('');
const typingTexts = ref([
  '探索前沿技术，分享创新思想，构建智能未来',
  '连接全球开发者，共创技术生态，引领行业趋势',
  '突破技术边界，激发创新潜能，塑造数字未来'
]);
let currentTextIndex = 0;
let currentCharIndex = 0;
let typingInterval = null;
let isTyping = true;

// 统计数据
const stats = ref([
  { value: '500+', label: '技术文章', color: 'text-primary' },
  { value: '10K+', label: '开发者', color: 'text-secondary' },
  { value: '50+', label: '技术栈', color: 'text-green-500' },
  { value: '1M+', label: '阅读量', color: 'text-purple-500' }
]);

// 特性数据
const features = ref([
  {
    title: 'AI 智能助手',
    description: '集成先进 AI 技术，为您提供智能代码审查、技术问题解答和个性化学习建议。',
    icon: 'AIcon'
  },
  {
    title: '数据驱动洞察',
    description: '深度数据分析，可视化技术趋势，帮助您把握行业发展方向和技能提升路径。',
    icon: 'ChartIcon'
  },
  {
    title: '精英社区',
    description: '与全球顶尖开发者交流，参与技术讨论，共同推动技术创新和最佳实践。',
    icon: 'CommunityIcon'
  }
]);

const technologies = ref([
  { name: 'Vue 3', icon: '⚡' },
  { name: 'Spring Boot', icon: '🌱' },
  { name: 'Docker', icon: '🐳' },
  { name: 'Kubernetes', icon: '☸️' },
  { name: 'AI/ML', icon: '🧠' },
  { name: 'Microservices', icon: '🔗' },
  { name: 'React', icon: '⚛️' },
  { name: 'Node.js', icon: '🟢' },
  { name: 'Python', icon: '🐍' },
  { name: 'Go', icon: '🐹' },
  { name: 'AWS', icon: '☁️' },
  { name: 'Azure', icon: '🔷' }
]);

// 图标组件
const AIcon = {
  template: `
    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
    </svg>
  `
};

const ChartIcon = {
  template: `
    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
    </svg>
  `
};

const CommunityIcon = {
  template: `
    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
    </svg>
  `
};

const getUserInfo = () => {
  get('api/user/current', {},
    (message, data) => {
      User.value = data;
      messageApi.success(message);
    })
}

const getTopArticles = () => {
  get('api/article/getTopArticles', {},
    (message, data) => {
      featuredArticles.value = data;
      messageApi.success(message);

      // 数据加载完成后显示内容
      setTimeout(() => {
        isLoading.value = false;
        nextTick(() => {
          setTimeout(() => {
            contentLoaded.value = true;
          }, 100);
        });
      }, 1500);
    })
}

const goArticleById = (id) => {
  router.push("/view/" + id);
};

// 打字机效果函数
const startTypingAnimation = () => {
  if (typingInterval) clearInterval(typingInterval);

  typingInterval = setInterval(() => {
    const currentText = typingTexts.value[currentTextIndex];

    if (isTyping) {
      // 打字阶段
      if (currentCharIndex < currentText.length) {
        displayText.value += currentText[currentCharIndex];
        currentCharIndex++;
      } else {
        // 打字完成，切换到删除阶段
        isTyping = false;
        setTimeout(() => {
          isTyping = false;
        }, 2000); // 停留2秒
      }
    } else {
      // 删除阶段
      if (currentCharIndex > 0) {
        displayText.value = currentText.substring(0, currentCharIndex - 1);
        currentCharIndex--;
      } else {
        // 删除完成，切换到下一个文本
        isTyping = true;
        currentTextIndex = (currentTextIndex + 1) % typingTexts.value.length;
      }
    }
  }, 100); // 打字速度
};

// 清理函数
const cleanupTyping = () => {
  if (typingInterval) {
    clearInterval(typingInterval);
    typingInterval = null;
  }
};

onMounted(() => {
  getUserInfo();
  getTopArticles();

  // 如果数据加载很快，确保至少显示加载动画1.5秒
  setTimeout(() => {
    if (isLoading.value) {
      isLoading.value = false;
      nextTick(() => {
        setTimeout(() => {
          contentLoaded.value = true;
          // 内容加载完成后启动打字机效果
          setTimeout(startTypingAnimation, 500);
        }, 100);
      });
    }
  }, 1500);
});

// 组件卸载时清理
onUnmounted(() => {
  cleanupTyping();
});
</script>

<style scoped>
/* 添加自定义动画 */
@keyframes pulse-slow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.8; }
}

.animate-pulse-slow {
  animation: pulse-slow 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animation-delay-2000 {
  animation-delay: 2000ms;
}

/* 过渡延迟类 */
.delay-300 {
  transition-delay: 300ms;
}

.delay-500 {
  transition-delay: 500ms;
}

.delay-700 {
  transition-delay: 700ms;
}

.delay-900 {
  transition-delay: 900ms;
}

.delay-1000 {
  transition-delay: 1000ms;
}

/* 玻璃态效果增强 */
.backdrop-blur-xl {
  backdrop-filter: blur(24px);
}

/* 阴影效果增强 */
.shadow-3xl {
  box-shadow: 0 35px 60px -15px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
}

/* 文本截断 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .text-8xl {
    font-size: 3.5rem;
  }

  .text-7xl {
    font-size: 3rem;
  }

  .text-5xl {
    font-size: 2.5rem;
  }
}

/* 页面加载动画 */
.page-enter-active,
.page-leave-active {
  transition: all 0.8s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 元素入场动画 */
.fade-in-up {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 交错动画 */
.stagger-animation > * {
  animation: fadeInUp 0.8s ease-out both;
}

.stagger-animation > *:nth-child(1) { animation-delay: 0.1s; }
.stagger-animation > *:nth-child(2) { animation-delay: 0.2s; }
.stagger-animation > *:nth-child(3) { animation-delay: 0.3s; }
.stagger-animation > *:nth-child(4) { animation-delay: 0.4s; }
.stagger-animation > *:nth-child(5) { animation-delay: 0.5s; }

/* 打字机效果 */
.typing-cursor {
  animation: blink 1s infinite;
  font-weight: bold;
  color: currentColor;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.typing-text {
  display: inline-block;
}
</style>
