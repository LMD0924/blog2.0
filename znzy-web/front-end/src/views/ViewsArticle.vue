<script setup>
import { message, Modal } from "ant-design-vue";
import { useRoute } from "vue-router";
import { onMounted, reactive, ref, watch, nextTick, onUnmounted, computed } from "vue";
import { get, post, del } from "@/net/index.js";
import { formatDate } from "@/time/Data.js";
import { MdPreview } from 'md-editor-v3';
import { MdEditor } from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import router from "@/router/index.js";

const isDark = ref(false);
const [messageApi, contextHolder] = message.useMessage();
const route = useRoute();
const ArticleId = route.params.id;

// State management
const state = reactive({
  article: {
    title: '',
    content: '',
    category: '',
    date: '',
    authorId: null,
    views: 0,
    likes: 0,
    favorites: 0,
    isLike: 0,
    isFavorite: 0,
    tags: [],
    ispublic:true,
    time:new Date().toISOString(),
    user: {
      id: null,
      account: '',
      avatar: ''
    }
  },
  user: {},
  comments: [],
  newComment: "",
  headings: [],
  activeHeading: '',
  currentUser: null,
  isAuthor: computed(() => {
    return state.currentUser && state.article && state.currentUser.id === state.article.authorId;
  })
});

// Modal control
const showEditModal = ref(false);
const editForm = reactive({
  title: '',
  content: '',
  tag:[],
  category:'',
  ispublic:true
});

// Computed properties
const formattedDate = computed(() => {
  return state.article.date ? formatDate(state.article.date) : '';
});

// 添加一个计算属性来处理当前用户ID
const currentUserId = computed(() => {
  try {
    const current = localStorage.getItem('current');
    return current ? JSON.parse(current)?.id : null;
  } catch (e) {
    return null;
  }
});

// Methods
const extractHeadings = () => {
  nextTick(() => {
    const articleElement = document.querySelector('.md-editor-preview-wrapper');
    if (articleElement) {
      const headingElements = articleElement.querySelectorAll('h1, h2, h3, h4, h5, h6');
      state.headings = Array.from(headingElements).map(el => ({
        id: el.id || generateId(el.textContent),
        text: el.textContent,
        level: parseInt(el.tagName.substring(1))
      }));

      headingElements.forEach((el, index) => {
        if (!el.id) {
          el.id = state.headings[index].id;
        }
      });
    }
  });
};

const generateId = (text) => {
  return text.toLowerCase().replace(/[^\w\u4e00-\u9fa5]+/g, '-');
};

const scrollToHeading = (id) => {
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
    state.activeHeading = id;
  }
};

const handleScroll = () => {
  const articleElement = document.querySelector('.md-editor-preview-wrapper');
  if (!articleElement) return;

  const headingsElements = articleElement.querySelectorAll('h1, h2, h3, h4, h5, h6');
  let currentActive = '';

  headingsElements.forEach((el) => {
    const rect = el.getBoundingClientRect();
    if (rect.top <= 100 && rect.bottom >= 0) {
      currentActive = el.id;
    }
  });

  if (currentActive && currentActive !== state.activeHeading) {
    state.activeHeading = currentActive;
  }
};

const getArticleById = () => {
  get(
    "api/article/getArticleById",
    { id: ArticleId },
    (message, data) => {
      Object.assign(state.article, data);
      console.log(data.time)
      if (data.authorId) {
        getUserById(data.authorId);
      }
      setTimeout(extractHeadings, 100);
    }
  );
};

const getUserById = (userId) => {
  get(
    "api/user/getUserById",
    { id: userId },
    (message, data) => {
      state.user = data;
    }
  );
};

const checkIsLike = () => {
  post('api/article/checkIsLike', {
    id: ArticleId,
  }, (message, data) => {
    if (data === 0) like();
    else unlike();
  });
};

const like = () => {
  post('api/article/like', {
    id: ArticleId,
  }, (message, data) => {
    state.article.likes++;
    state.article.isLike = 1;
    messageApi.success(message);
  });
};

const unlike = () => {
  post('api/article/unlike', {
    id: ArticleId,
  }, (message, data) => {
    state.article.likes--;
    state.article.isLike = 0;
    messageApi.success(message);
  });
};

const favorite = () => {
  post('api/article/favorite', {
    id: ArticleId,
  }, (message, data) => {
    state.article.favorites++;
    state.article.isFavorite = 1;
    messageApi.success(message);
  });
};

const unfavorite = () => {
  post('api/article/unfavorite', {
    id: ArticleId,
  }, (message, data) => {
    state.article.favorites--;
    state.article.isFavorite = 0;
    messageApi.success(message);
  });
};

const checkIsFavorite = () => {
  post('api/article/checkIsFavorite', {
    id: ArticleId,
  }, (message, data) => {
    if (data === 0) favorite();
    else unfavorite();
  });
};

const openEditModal = () => {
  editForm.title = state.article.title;
  editForm.content = state.article.content;
  editForm.tag=[...state.article.tags];
  editForm.category=state.article.category;
  editForm.ispublic=state.article.ispublic;
  showEditModal.value = true;
};

const updateArticle = () => {
  post(
    "api/article/updateArticle",
    {
      id: ArticleId,
      title: editForm.title,
      content: editForm.content,
      tag: Array.isArray(editForm.tag) ? editForm.tag.join(',') : editForm.tag, // 这里做了修改
      category: editForm.category,
      ispublic: editForm.ispublic
    },
    (message, data) => {
      if (data) {
        messageApi.success("文章更新成功！");
        showEditModal.value = false;
        Object.assign(state.article, data);
      } else {
        messageApi.error("文章更新失败");
      }
    }
  );
};

const deleteArticle = () => {
  // 检查用户是否已登录
  if (!state.currentUser) {
    messageApi.error("请先登录！");
    return;
  }

  // 显示删除确认对话框
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这篇文章吗？此操作不可撤销。',
    okText: '确定',
    cancelText: '取消',
    onOk() {
      // 用户确认后发送删除请求
      post('api/article/deleteArticle', {
        id: ArticleId,
      }, (message, data) => {
        messageApi.success(message);
        // 确保删除成功后重定向到首页
        router.push('/Article');
      }, (error) => {
        // 错误处理，避免未捕获的异常导致返回登录页面
        console.error('删除文章失败:', error);
        messageApi.error('删除失败，请稍后重试');
      });
    },
    onCancel() {
      // 用户取消删除操作
    },
  });
};

const getCurrentUserId = () => {
  get(
    "api/user/current",
    {},
    (message, data) => {
      if (data) {
        state.currentUser = data;
        localStorage.setItem("current", JSON.stringify(data));
      }
    }
  );
};

const loadComments = () => {
  get(
    "api/comment/article",
    { articleId: ArticleId },
    (message, data) => {
      if (data) {
        state.comments = data;
        console.log('Loaded comments:', data);
      } else {
        state.comments = [];
      }
    }
  );
};

const addComment = () => {
  if (state.newComment.trim() === "") return;

  if (!state.currentUser) {
    messageApi.error("请先登录！");
    return;
  }

  post(
    "api/comment/addComment",
    {
      content: state.newComment,
      articleId: ArticleId,
      userId: state.currentUser.id,
      username: state.currentUser.account,
      avatar: state.currentUser.avatar,
    },
    (message, data) => {
      messageApi.success(message);
      state.newComment = "";
      // 重新加载评论列表和文章信息
      loadComments();
      getArticleById();
    }
  );
};

const deleteComment = (commentId) => {
  del(
    "api/comment/delete",
    { id: commentId },
    (message, data) => {
      messageApi.success(message);
      loadComments();
      getArticleById();
      console.log('Comment deleted, reloading data...');
    }
  );
};
//跳转用户信息页面
const goToUserPage = (userId) => {
  router.push({ path: '/User', query: { id: userId } });
  console.log("跳转用户信息页面,用户ID:",userId)
};
// 分类选项
const categories = ref([
  { value: '技术', label: '技术' },
  { value: '生活', label: '生活' },
  { value: '阅读', label: '阅读' },
  { value: '算法', label: '算法' },
  { value: '其他', label: '其他' }
]);

// 标签选项
const tagOptions = ref([
  { value: 'vue', label: 'Vue' },
  { value: 'react', label: 'React' },
  { value: 'javascript', label: 'JavaScript' },
  { value: 'css', label: 'CSS' },
  { value: 'nodejs', label: 'Node.js' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'c++', label: 'C++' },
  { value: 'C', label: 'C' },
  { value: '其他', label: '其他' }
]);

// Lifecycle hooks
onMounted(() => {
  getArticleById();
  getCurrentUserId();
  loadComments();
  const articleElement = document.querySelector('.md-editor-preview-wrapper');
  if (articleElement) {
    articleElement.addEventListener('scroll', handleScroll);
  }
});

onUnmounted(() => {
  const articleElement = document.querySelector('.md-editor-preview-wrapper');
  if (articleElement) {
    articleElement.removeEventListener('scroll', handleScroll);
  }
});

// Watchers
watch(() => state.article.content, (newVal) => {
  if (newVal) {
    nextTick(extractHeadings);
  }
});
</script>

<template>
  <contextHolder />
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100 transition-colors duration-300">
    <!-- Main content -->
    <main class="container mx-auto px-4 py-12 flex flex-col lg:flex-row gap-8">
      <!-- Article navigation (left) -->
      <aside class="lg:w-64 flex-shrink-0 hidden lg:block">
        <div class="sticky top-20">
          <div class="bg-white dark:bg-dark-800 rounded-xl shadow-modern p-6 animate-fade-in animate-delay-100">
            <h3 class="font-bold text-lg mb-4 flex items-center">
              <svg class="w-5 h-5 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
              </svg>
              文章目录
            </h3>
            <nav class="space-y-1">
              <a
                v-for="heading in state.headings"
                :key="heading.id"
                @click.prevent="scrollToHeading(heading.id)"
                :class="{
                  'text-primary font-medium bg-primary/5 dark:bg-primary/10': state.activeHeading === heading.id,
                  'text-gray-700 dark:text-gray-300 hover:text-primary dark:hover:text-primary hover:bg-gray-50 dark:hover:bg-dark-700': state.activeHeading !== heading.id,
                  'ml-0': heading.level === 1,
                  'ml-4': heading.level === 2,
                  'ml-8': heading.level === 3
                }"
                class="block transition-all duration-300 cursor-pointer text-sm truncate max-w-full px-3 py-2 rounded-lg"
                :title="heading.text"
              >
                {{ heading.text }}
              </a>
              <p v-if="state.headings.length === 0" class="text-gray-500 text-sm px-3 py-2">
                本文没有标题结构
              </p>
            </nav>
          </div>
        </div>
      </aside>

      <!-- Article content (middle) -->
      <article class="flex-1 bg-white dark:bg-dark-800 rounded-xl shadow-modern overflow-hidden animate-fade-in">
        <!-- Article header -->
        <header class="p-8 border-b border-gray-100 dark:border-dark-700">
          <div class="flex items-center justify-between mb-6 flex-wrap gap-3">
            <span class="text-sm font-medium px-3 py-1 rounded-full bg-gray-100 dark:bg-dark-700 text-gray-600 dark:text-gray-300">
              {{ formattedDate }}
            </span>
            <span v-if="state.article.category" class="text-sm font-medium px-3 py-1 rounded-full bg-primary/10 dark:bg-primary/20 text-primary">
              {{ state.article.category }}
            </span>
          </div>

          <h1 class="text-3xl md:text-4xl font-bold mb-6 text-balance">{{ state.article.title }}</h1>

          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <div class="flex items-center space-x-4">
              <img
                :src="state.user.avatar + '?timestamp=' + Date.now()"
                class="w-12 h-12 rounded-full object-cover cursor-pointer border-2 border-white dark:border-dark-800 shadow-modern transition-transform hover:scale-105"
                alt="作者头像"
                @click="goToUserPage(state.user.id)"
              />
              <div>
                <span class="font-medium text-lg cursor-pointer hover:text-primary transition-colors" @click="goToUserPage(state.user.id)">{{ state.user.account }}</span>
              </div>
            </div>

            <div class="flex flex-wrap gap-3">
              <!-- 编辑删除按钮 -->
              <div v-if="state.isAuthor" class="flex space-x-3">
                <button
                  @click="openEditModal"
                  class="px-4 py-2 text-sm bg-primary hover:bg-primary/90 text-white rounded-lg transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center"
                >
                  <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                  </svg>
                  编辑
                </button>
                <button
                  @click="deleteArticle"
                  class="px-4 py-2 text-sm bg-red-500 hover:bg-red-600 text-white rounded-lg transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center"
                >
                  <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                  删除
                </button>
              </div>

              <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
                <span class="flex items-center gap-1 px-3 py-2 bg-gray-100 dark:bg-dark-700 rounded-lg">
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                  </svg>
                  {{ state.article.views || 0 }} 阅读
                </span>
              </div>
            </div>
          </div>
        </header>

        <!-- Article content -->
        <section class="p-8">
          <div class="rounded-lg overflow-hidden">
            <MdPreview :modelValue="state.article.content" class="prose dark:prose-invert prose-lg max-w-none" />
          </div>
        </section>

        <!-- Article footer -->
        <footer class="p-8 border-t border-gray-100 dark:border-dark-700">
          <div v-if="state.article.tags && state.article.tags.length" class="mb-8">
            <h4 class="text-sm font-medium mb-3 text-gray-500 dark:text-gray-400">文章标签</h4>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="tag in state.article.tags"
                :key="tag"
                class="px-3 py-1.5 bg-gray-100 dark:bg-dark-700 hover:bg-primary/10 dark:hover:bg-primary/20 hover:text-primary rounded-full text-sm transition-colors duration-300 cursor-pointer"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <div class="flex space-x-4">
              <button
                @click="checkIsLike"
                class="flex items-center gap-2 px-5 py-2.5 rounded-xl transition-all duration-300 transform hover:-translate-y-0.5"
                :class="state.article.isLike
                  ? 'bg-primary/10 dark:bg-primary/20 text-primary'
                  : 'bg-gray-100 dark:bg-dark-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-dark-600'"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                </svg>
                <span>{{ state.article.likes || 0 }}</span>
                <span>点赞</span>
              </button>
              <button
                @click="checkIsFavorite"
                class="flex items-center gap-2 px-5 py-2.5 rounded-xl transition-all duration-300 transform hover:-translate-y-0.5"
                :class="state.article.isFavorite
                  ? 'bg-primary/10 dark:bg-primary/20 text-primary'
                  : 'bg-gray-100 dark:bg-dark-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-dark-600'"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                </svg>
                <span>{{ state.article.favorites || 0 }}</span>
                <span>收藏</span>
              </button>
            </div>

            <div class="text-sm text-gray-500 dark:text-gray-400">
              最后更新于 {{ state.article.time }}
            </div>
          </div>
        </footer>
      </article>

      <!-- Comments section (right) -->
      <aside class="lg:w-80 flex-shrink-0">
        <div class="sticky top-20 space-y-6">
          <!-- Comment form -->
          <div class="bg-white dark:bg-dark-800 rounded-xl shadow-modern p-6 animate-fade-in animate-delay-200">
            <h3 class="font-bold text-lg mb-4 flex items-center">
              <svg class="w-5 h-5 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
              发表评论
            </h3>
            <textarea
              v-model="state.newComment"
              rows="4"
              class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary bg-white dark:bg-dark-800 transition-all duration-300 resize-none"
              placeholder="写下你的评论..."
            ></textarea>
            <button
              @click="addComment"
              class="mt-4 w-full py-3 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 font-medium"
            >
              提交评论
            </button>
          </div>

          <!-- Comments list -->
          <div class="bg-white dark:bg-dark-800 rounded-xl shadow-modern p-6 animate-fade-in animate-delay-300">
            <h3 class="font-bold text-lg mb-6 flex items-center justify-between">
              <div class="flex items-center">
                <svg class="w-5 h-5 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"></path>
                </svg>
                评论
              </div>
              <span class="text-sm bg-gray-100 dark:bg-dark-700 px-2 py-1 rounded-full text-gray-600 dark:text-gray-300">{{ state.comments.length }}</span>
            </h3>

            <div v-if="state.comments.length === 0" class="text-center py-10 text-gray-500">
              <svg class="mx-auto h-10 w-10 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
              <p class="mt-3">暂无评论，快来抢沙发吧~</p>
            </div>

            <div v-else class="space-y-6">
              <div
                v-for="comment in state.comments"
                :key="comment.id"
                class="group relative border-b border-gray-100 dark:border-dark-700 pb-6 last:border-0 last:pb-0"
              >
                <div class="flex items-start space-x-4">
                  <img
                    :src="comment.avatar + '?timestamp=' + Date.now()"
                    class="w-12 h-12 rounded-full object-cover border-2 border-white dark:border-dark-800 shadow-sm transition-transform hover:scale-105"
                    alt="评论者头像"
                  />
                  <div class="flex-1">
                    <div class="flex items-center justify-between mb-1">
                      <span class="font-medium text-gray-900 dark:text-white group-hover:text-primary transition-colors duration-300">{{ comment.username }}</span>
                      <span class="text-xs text-gray-500 dark:text-gray-400">{{ formatDate(comment.time) }}</span>
                    </div>
                    <p class="text-gray-700 dark:text-gray-300 mb-3 line-clamp-3 group-hover:text-gray-900 dark:group-hover:text-white transition-colors duration-300">{{ comment.content }}</p>
                    <div class="flex items-center space-x-4 text-xs text-gray-500">
                      <button class="hover:text-primary transition-colors duration-300 px-2 py-1 rounded-full hover:bg-primary/5">回复</button>
                      <button
                        v-if="comment.userId === currentUserId"
                        @click="deleteComment(comment.id)"
                        class="hover:text-red-500 transition-colors duration-300 px-2 py-1 rounded-full hover:bg-red-50 dark:hover:bg-red-900/20"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                  </div>
                  <div class="absolute bottom-0 left-0 w-0 h-[2px] bg-blue-500 dark:bg-blue-400 transition-all duration-300 group-hover:w-full"></div>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </main>
    </div>
    <!-- 修改编辑模态框 -->
    <a-modal
      v-model:open="showEditModal"
      title="编辑文章"
      @ok="updateArticle"
      :width="800"
    >
      <a-form layout="vertical">
        <a-form-item label="标题">
          <a-input v-model:value="editForm.title" />
        </a-form-item>

        <!-- 添加分类选择 -->
        <a-form-item label="分类">
          <a-select
            v-model:value="editForm.category"
            placeholder="选择分类"
            :options="categories"
          />
        </a-form-item>

        <!-- 添加标签选择 -->
        <a-form-item label="标签">
          <a-select
            v-model:value="editForm.tag"
            mode="multiple"
            placeholder="选择标签"
            :options="tagOptions"
          />
        </a-form-item>

        <!-- 添加是否公开设置 -->
        <a-form-item label="发布设置">
          <a-space>
            <a-switch
              v-model:checked="editForm.isPublic"
              checked-children="公开"
              un-checked-children="私密"
            />
            <span>设为私密后仅自己可见</span>
          </a-space>
        </a-form-item>

        <a-form-item label="内容">
          <MdEditor v-model="editForm.content" />
        </a-form-item>
      </a-form>

      <template #footer>
        <a-button key="back" @click="showEditModal = false">取消</a-button>
        <a-button key="submit" type="primary" @click="updateArticle">保存</a-button>
      </template>
    </a-modal>
</template>

<style scoped>

.prose {
  line-height: 1.75;
}

.prose :where(h1, h2, h3, h4, h5, h6) {
  margin-top: 1.5em;
  margin-bottom: 0.75em;
  font-weight: 600;
  line-height: 1.3;
}

.prose :where(h1) {
  font-size: 2em;
  margin-top: 0;
}

.prose :where(h2) {
  font-size: 1.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #e5e7eb;
}

.prose :where(h3) {
  font-size: 1.25em;
}

.prose :where(p) {
  margin-top: 1em;
  margin-bottom: 1em;
}

.prose :where(a) {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.prose :where(a:hover) {
  text-decoration: underline;
}

.prose :where(ul, ol) {
  margin-top: 1em;
  margin-bottom: 1em;
  padding-left: 1.5em;
}

.prose :where(blockquote) {
  margin-top: 1em;
  margin-bottom: 1em;
  padding-left: 1em;
  border-left: 4px solid #e5e7eb;
  color: #6b7280;
}

.prose :where(pre) {
  background-color: #f3f4f6;
  border-radius: 0.375rem;
  padding: 1em;
  overflow-x: auto;
  margin-top: 1em;
  margin-bottom: 1em;
}

.prose :where(code) {
  background-color: #f3f4f6;
  border-radius: 0.25rem;
  padding: 0.2em 0.4em;
  font-size: 0.9em;
}

.prose :where(img) {
  max-width: 100%;
  height: auto;
  margin-top: 1em;
  margin-bottom: 1em;
}

/* Dark mode styles */
.dark .prose :where(pre) {
  background-color: #1f2937;
}

.dark .prose :where(code) {
  background-color: #1f2937;
}

.dark .prose :where(blockquote) {
  border-left-color: #374151;
  color: #9ca3af;
}

.dark .prose :where(h2) {
  border-bottom-color: #374151;
}

/* Smooth scrolling */
html {
  scroll-behavior: smooth;
}

/* Custom styles for markdown preview */
:deep(.md-editor-preview-wrapper) {
  padding: 0 !important;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* 确保文章内容区域在暗色模式下也能正常滚动 */
.dark :deep(.md-editor-preview-wrapper) {
  scrollbar-color: #4B5563 #1F2937;
}

/* 自定义滚动条样式 */
:deep(.md-editor-preview-wrapper::-webkit-scrollbar) {
  width: 6px;
}

:deep(.md-editor-preview-wrapper::-webkit-scrollbar-track) {
  background: #f1f1f1;
}

:deep(.md-editor-preview-wrapper::-webkit-scrollbar-thumb) {
  background: #888;
  border-radius: 3px;
}

:deep(.md-editor-preview-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #555;
}

/* Dark mode scrollbar styles */
.dark :deep(.md-editor-preview-wrapper::-webkit-scrollbar-track) {
  background: #1F2937;
}

.dark :deep(.md-editor-preview-wrapper::-webkit-scrollbar-thumb) {
  background: #4B5563;
}

.dark :deep(.md-editor-preview-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #6B7280;
}

/* 添加新的动画样式 */
.group {
  position: relative;
  transition: all 0.3s ease;
}

.group:hover {
  transform: translateY(-2px);
}

/* 确保暗色模式下的过渡效果 */
.dark .group {
  transition: all 0.3s ease;
}

/* 按钮和交互元素过渡效果 */
button {
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-1px);
}

/* 确保评论内容区域有足够的空间显示下划线 */
.pb-4 {
  padding-bottom: 1.5rem;
}

/* 添加平滑的悬停效果 */
.group:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.dark .group:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.2), 0 2px 4px -1px rgba(0, 0, 0, 0.1);
}
</style>
