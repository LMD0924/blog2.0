<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { get, post } from "@/net/index.js";
import { message } from "ant-design-vue";
import router from "@/router/index.js";
import { formatDate } from "@/time/Data.js";
import { EditOutlined, DeleteOutlined, LinkOutlined, GithubOutlined, TwitterOutlined, EnvironmentOutlined } from '@ant-design/icons-vue';

const [messageApi, contextHolder] = message.useMessage();
const isDark = ref(false);

// 用户数据
const userInfo = reactive({
  id: '',
  account: '',
  name: '',
  email: '',
  avatar: '',
 password: '',
});

// 编辑状态
const isEditing = ref(false);
const editForm = reactive({
  account: '',
  name: '',
  email: '',
});

// 头像上传相关
const avatarFile = ref(null);
const avatarPreview = ref('');
const isUploading = ref(false);

// 密码修改相关
const showPasswordModal = ref(false);
const passwordForm = reactive({});

// 选项卡相关
const tabs = [
  { key: 'profile', label: '个人信息' },
  { key: 'articles', label: '我的文章' },
  { key: 'liked', label: '点赞文章' },
  { key: 'collected', label: '收藏文章' },
];
const activeTab = ref('profile');

// 文章相关数据
const articles = ref([]);
const articlesLoading = ref(false);
const articlesPage = ref(1);
const articlesPageSize = ref(10);
const articlesTotal = ref(0);

const likedArticles = ref([]);
const likedLoading = ref(false);
const likedPage = ref(1);
const likedPageSize = ref(10);
const likedTotal = ref(0);

const collectedArticles = ref([]);
const collectedLoading = ref(false);
const collectedPage = ref(1);
const collectedPageSize = ref(10);
const collectedTotal = ref(0);

// 跳转到文章详情页
const goArticleById = (id) => {
  router.push("/view/" + id);
};

// 获取用户信息
const fetchUserInfo = () => {
  get('api/user/current', {}, (msg, data) => {
    Object.assign(userInfo, data);
    console.log(userInfo);
    Object.assign(editForm, {
      account:data.account,
      name: data.name,
      email: data.email,
      bio: data.bio || '',
      location: data.location || '',
      website: data.website || '',
      github: data.github || '',
      twitter: data.twitter || '',
    });
  });
};

// 保存编辑信息
const saveEdit = () => {
  post('api/user/updateUser', {
    ...editForm,
    id:userInfo.id,
  }, (msg) => {
    messageApi.success(msg);
    Object.assign(userInfo, editForm);
    isEditing.value = false;
  });
};

// 获取用户文章
const getUserArticles = async () => {
  get("api/article/getArticlesByUser", {}, (message, data) => {
    if (data) {
      articles.value = data
    }
  })
}

// 获取用户点赞和收藏的文章
const getLikesArticles = () => {
  get('api/article/getLikedArticles', {}, (message, data) => {
    if (data) {
      likedArticles.value = data
    }
  })
}

const getFavoriteArticles = () => {
  get('api/article/getFavoriteArticles', {}, (message, data) => {
    if (data) {
      collectedArticles.value = data
    }
  })
}

// 进入编辑模式
const enterEditMode = () => {
  isEditing.value = true;
};

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false;
  Object.assign(editForm, {
    account:userInfo.account,
    name: userInfo.name,
    email: userInfo.email,
    bio: userInfo.bio || '',
    location: userInfo.location || '',
    website: userInfo.website || '',
    github: userInfo.github || '',
    twitter: userInfo.twitter || '',
  });
};

// 处理头像选择
const handleAvatarChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    if (!file.type.startsWith('image/')) {
      messageApi.error('请选择图片文件');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      messageApi.error('图片大小不能超过2MB');
      return;
    }
    avatarFile.value = file;
    avatarPreview.value = URL.createObjectURL(file);
  }
};

// 上传头像
const uploadAvatar = () => {
  if (!avatarFile.value) {
    messageApi.warning('请先选择头像');
    return;
  }

  isUploading.value = true;
  const formData = new FormData();
  formData.append('file', avatarFile.value);

  post('api/user/upload-avatar', formData, (msg, data) => {
    messageApi.success(msg);
    userInfo.avatar = data.avatarUrl;
    avatarPreview.value = '';
    avatarFile.value = null;
    isUploading.value = false;
  }, () => {
    isUploading.value = false;
  });
};

// 打开修改密码模态框
const openPasswordModal = () => {
  showPasswordModal.value = true;
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
};

// 提交密码修改
const submitPasswordChange = () => {
  post('api/user/updateUser', {
    password:editForm.password,
  }, (msg) => {
    messageApi.success(msg);
    showPasswordModal.value = false;
  });
};

// 初始化
onMounted(() => {
  fetchUserInfo();
  getUserArticles();
  getLikesArticles();
  getFavoriteArticles();
  // 初始化主题
  const savedTheme = localStorage.getItem('darkMode');
  if (savedTheme !== null) {
    isDark.value = savedTheme === 'true';
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
});
</script>

<template>
  <contextHolder />
  <div :class="{ 'dark': isDark }" class="min-h-screen transition-all duration-300 ease-in-out">
    <div class="bg-gray-50 dark:bg-dark-900 text-gray-800 dark:text-gray-100 min-h-screen py-8">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- 头部标题 -->
          <div class="flex justify-between items-center mb-8 animate-fade-in">
            <h1 class="text-[clamp(1.75rem,4vw,2.5rem)] font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">个人信息</h1>
            <button
              @click="router.back()"
              class="flex items-center text-gray-600 dark:text-gray-300 hover:text-primary dark:hover:text-primary-light group transition-all duration-300"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 group-hover:-translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
              </svg>
              返回
            </button>
          </div>

          <!-- 选项卡 -->
          <div class="mb-8 border-b border-gray-200 dark:border-dark-700 animate-fade-in animate-delay-100">
            <ul class="flex flex-wrap -mb-px">
              <li class="mr-2" v-for="tab in tabs" :key="tab.key">
                <button
                  @click="activeTab = tab.key"
                  :class="{
                    'text-primary border-primary dark:text-primary-light dark:border-primary-light': activeTab === tab.key,
                    'hover:text-gray-600 hover:border-gray-300 dark:hover:text-gray-300': activeTab !== tab.key
                  }"
                  class="inline-block p-4 border-b-2 border-transparent rounded-t-lg transition-all duration-300 font-medium tab-button"
                >
                  {{ tab.label }}
                </button>
              </li>
            </ul>
          </div>

          <!-- 选项卡内容 -->
          <div>
            <!-- 个人信息 -->
            <div v-show="activeTab === 'profile'" class="animate-fade-in animate-delay-200">
              <div class="flex gap-8">
                <div class="flex-1">
                  <!-- 个人信息卡片 -->
                  <div class="bg-white dark:bg-dark-800 rounded-xl shadow-modern overflow-hidden mb-8 transition-all duration-300 hover:shadow-hover">
                    <!-- 头像部分 -->
                    <div class="relative bg-gradient-to-r from-primary-light to-secondary-dark h-48 flex items-center justify-center">
                      <img src="https://s.panlai.com/zb_users/upload/2025/05/20250504011229174629234975517.jpg-arthumbs" alt="背景" class="h-48 w-full object-cover opacity-70">
                      <div class="absolute -bottom-16 left-8">
                        <div class="relative group">
                          <img
                            :src="avatarPreview || (userInfo.avatar ? userInfo.avatar + '?timestamp=' + Date.now() : '/default-avatar.jpg')"
                            class="w-32 h-32 rounded-full border-4 border-white dark:border-dark-800 object-cover shadow-lg transition-transform duration-300 group-hover:scale-105"
                            alt="用户头像"
                          >
                          <div class="absolute inset-0 bg-black bg-opacity-50 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all duration-300">
                            <label class="cursor-pointer text-white p-2 rounded-full hover:bg-white hover:bg-opacity-20 transition-colors">
                              <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                              </svg>
                              <input
                                type="file"
                                accept="image/*"
                                class="hidden"
                                @change="handleAvatarChange"
                              >
                            </label>
                          </div>
                        </div>
                      </div>
                      <div v-if="avatarPreview" class="absolute bottom-4 right-4">
                        <button
                          @click="uploadAvatar"
                          :disabled="isUploading"
                          class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark disabled:opacity-50 transition-all duration-300 shadow-md hover:shadow-lg"
                        >
                          <span v-if="isUploading">上传中...</span>
                          <span v-else>保存头像</span>
                        </button>
                        <button
                          @click="avatarPreview = ''; avatarFile = null"
                          class="ml-2 px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-all duration-300 shadow-md hover:shadow-lg"
                        >
                          取消
                        </button>
                      </div>
                    </div>

                    <!-- 用户信息部分 -->
                    <div class="pt-20 px-6 pb-6">
                      <!-- 基本信息 -->
                      <div class="mb-8">
                        <div class="flex justify-between items-center mb-4">
                          <h2 class="text-xl font-semibold text-gray-800 dark:text-gray-100">基本信息</h2>
                          <button
                            v-if="!isEditing"
                            @click="enterEditMode"
                            class="flex items-center text-primary hover:text-primary-dark dark:hover:text-primary-light transition-colors duration-300"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                            </svg>
                            编辑
                          </button>
                          <div v-else>
                            <button
                              @click="saveEdit"
                              class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark mr-2 transition-all duration-300 shadow-md hover:shadow-lg"
                            >
                              保存
                            </button>
                            <button
                              @click="cancelEdit"
                              class="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-all duration-300 shadow-md hover:shadow-lg"
                            >
                              取消
                            </button>
                          </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <!-- 用户名 -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">用户名</label>
                            <input
                              v-if="isEditing"
                              v-model="editForm.account"
                              type="text"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                            >
                            <div v-else class="text-lg group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">
                              {{ userInfo.account }}
                            </div>
                          </div>

                          <!-- 账号 -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">账号</label>
                            <div class="text-lg group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">
                              {{ userInfo.name }}
                            </div>
                          </div>

                          <!-- 邮箱 -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">邮箱</label>
                            <input
                              v-if="isEditing"
                              v-model="editForm.email"
                              type="email"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                            >
                            <div v-else class="text-lg group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">
                              {{ userInfo.email || '未设置' }}
                            </div>
                          </div>

                          <!-- 注册日期 -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">注册日期</label>
                            <div class="text-lg group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">
                              {{ userInfo.registrationDate ? formatDate(userInfo.time) : '未知' }}
                            </div>
                          </div>

                          <!-- 个人简介 -->
                          <div class="md:col-span-2 group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">个人简介</label>
                            <textarea
                              v-if="isEditing"
                              v-model="editForm.bio"
                              rows="3"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                              placeholder="介绍一下你自己..."
                            ></textarea>
                            <div v-else class="text-lg whitespace-pre-line group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">
                              {{ userInfo.bio || '这个人很懒，什么都没留下~' }}
                            </div>
                          </div>
                        </div>
                      </div>

                      <!-- 作者信息 -->
                      <div class="mb-8 border-t border-gray-200 dark:border-dark-700 pt-6">
                        <h2 class="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-100">作者信息</h2>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <!-- 所在地 -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">所在地</label>
                            <div v-if="!isEditing" class="flex items-center text-lg group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">
                              <EnvironmentOutlined class="mr-2" />
                              {{ userInfo.location || '未设置' }}
                            </div>
                            <input
                              v-else
                              v-model="editForm.location"
                              type="text"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                              placeholder="请输入所在地"
                            >
                          </div>

                          <!-- 个人网站 -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">个人网站</label>
                            <div v-if="!isEditing" class="flex items-center text-lg">
                              <LinkOutlined class="mr-2" />
                              <a v-if="userInfo.website" :href="userInfo.website" target="_blank" class="text-primary hover:text-primary-dark hover:underline transition-all duration-300 transform hover:-translate-y-1">
                                {{ userInfo.website }}
                              </a>
                              <span v-else class="group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">未设置</span>
                            </div>
                            <input
                              v-else
                              v-model="editForm.website"
                              type="url"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                              placeholder="请输入个人网站URL"
                            >
                          </div>

                          <!-- GitHub -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">GitHub</label>
                            <div v-if="!isEditing" class="flex items-center text-lg">
                              <GithubOutlined class="mr-2" />
                              <a v-if="userInfo.github" :href="'https://github.com/' + userInfo.github" target="_blank" class="text-primary hover:text-primary-dark hover:underline transition-all duration-300 transform hover:-translate-y-1">
                                {{ userInfo.github }}
                              </a>
                              <span v-else class="group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">未设置</span>
                            </div>
                            <input
                              v-else
                              v-model="editForm.github"
                              type="text"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                              placeholder="请输入GitHub用户名"
                            >
                          </div>

                          <!-- Twitter -->
                          <div class="group">
                            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Twitter</label>
                            <div v-if="!isEditing" class="flex items-center text-lg">
                              <TwitterOutlined class="mr-2" />
                              <a v-if="userInfo.twitter" :href="'https://twitter.com/' + userInfo.twitter" target="_blank" class="text-primary hover:text-primary-dark hover:underline transition-all duration-300 transform hover:-translate-y-1">
                                @{{ userInfo.twitter }}
                              </a>
                              <span v-else class="group-hover:text-primary dark:group-hover:text-primary-light transition-colors duration-300">未设置</span>
                            </div>
                            <input
                              v-else
                              v-model="editForm.twitter"
                              type="text"
                              class="w-full px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg bg-white dark:bg-dark-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all duration-300"
                              placeholder="请输入Twitter用户名"
                            >
                          </div>
                        </div>
                      </div>

                      <!-- 其他操作 -->
                      <div class="border-t border-gray-200 dark:border-dark-700 pt-6">
                        <h2 class="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-100">账户安全</h2>
                        <div class="space-y-4">
                          <button
                            @click="openPasswordModal"
                            class="flex items-center text-left w-full px-4 py-3 bg-gray-100 dark:bg-dark-700 hover:bg-gray-200 dark:hover:bg-dark-600 rounded-lg transition-all duration-300 transform hover:-translate-x-1 hover:shadow-md"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                            </svg>
                            <div>
                              <div class="font-medium">修改密码</div>
                              <div class="text-sm text-gray-500 dark:text-gray-400">定期修改密码可以提高账户安全性</div>
                            </div>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 我的文章 -->
            <div v-show="activeTab === 'articles'" class="bg-white dark:bg-dark-800 rounded-xl shadow-modern overflow-hidden p-6 animate-fade-in animate-delay-200">
              <h2 class="text-xl font-semibold mb-6 text-gray-800 dark:text-gray-100">我的文章</h2>
              <div v-if="articlesLoading" class="flex justify-center py-8">
                <a-spin size="large" />
              </div>
              <div v-else-if="articles.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto mb-4 text-gray-300 dark:text-gray-600 empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p class="text-lg">暂无文章</p>
                <a href="/create" class="inline-block mt-4 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors duration-300">
                  写第一篇文章
                </a>
              </div>
              <div v-else class="space-y-4">
                <div
                  @click="goArticleById(article.id)"
                  v-for="article in articles" :key="article.id" class="border-b border-gray-200 dark:border-dark-700 pb-4 article-item transition-all duration-300 hover:pl-2 cursor-pointer">
                  <div class="flex justify-between items-start article-card">
                    <div>
                      <h3 class="text-lg font-medium hover:text-primary dark:hover:text-primary-light cursor-pointer">
                        {{ article.title }}
                      </h3>
                      <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400 mt-2">
                      <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                        {{ article.views || 0 }}
                      </span>
                        <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                        </svg>
                        {{ article.likes || 0 }}
                      </span>
                        <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                    </svg>
                    {{ article.favorites || 0 }} 收藏
                      </span>
                      </div>
                    </div>
                    <div class="flex space-x-2">
                      <a-button type="text" size="small" @click.stop="editArticle(article.id)" class="hover:text-primary dark:hover:text-primary-light transition-colors">
                        <template #icon><EditOutlined /></template>
                      </a-button>
                      <a-button type="text" danger size="small" @click.stop="deleteArticle(article.id)" class="hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
                        <template #icon><DeleteOutlined /></template>
                      </a-button>
                    </div>
                  </div>
                </div>
                <div class="flex justify-center mt-6">
                  <a-pagination
                    v-model:current="articlesPage"
                    :total="articlesTotal"
                    :pageSize="articlesPageSize"
                    show-less-items
                    class="pagination"
                  />
                </div>
              </div>
            </div>

            <!-- 点赞文章 -->
            <div v-show="activeTab === 'liked'" class="bg-white dark:bg-dark-800 rounded-xl shadow-modern overflow-hidden p-6 animate-fade-in animate-delay-200">
              <h2 class="text-xl font-semibold mb-6 text-gray-800 dark:text-gray-100">点赞的文章</h2>
              <div v-if="likedLoading" class="flex justify-center py-8">
                <a-spin size="large" />
              </div>
              <div v-else-if="likedArticles.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto mb-4 text-gray-300 dark:text-gray-600 empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                </svg>
                <p class="text-lg">暂无点赞文章</p>
              </div>
              <div v-else class="space-y-4">
                <div
                  @click="goArticleById(article.id)"
                  v-for="article in likedArticles" :key="article.id" class="article-card article-item border-b border-gray-200 dark:border-dark-700 pb-4 transition-all duration-300 hover:pl-2 cursor-pointer">
                  <h3 class="text-lg font-medium hover:text-primary dark:hover:text-primary-light cursor-pointer">
                    {{ article.title }}
                  </h3>
                  <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400 mt-2">
                      <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                        {{ article.views || 0 }}
                      </span>
                    <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                        </svg>
                        {{ article.likes || 0 }}
                      </span>
                    <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path>
                    </svg>
                    {{ article.favorites || 0 }} 收藏
                      </span>
                  </div>
                </div>
                <div class="flex justify-center mt-6">
                  <a-pagination
                    v-model:current="likedPage"
                    :total="likedTotal"
                    :pageSize="likedPageSize"
                    show-less-items
                    class="pagination"
                  />
                </div>
              </div>
            </div>

            <!-- 收藏文章 -->
            <div v-show="activeTab === 'collected'" class="bg-white dark:bg-dark-800 rounded-xl shadow-modern overflow-hidden p-6 animate-fade-in animate-delay-200">
              <h2 class="text-xl font-semibold mb-6 text-gray-800 dark:text-gray-100">收藏的文章</h2>
              <div v-if="collectedLoading" class="flex justify-center py-8">
                <a-spin size="large" />
              </div>
              <div v-else-if="collectedArticles.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto mb-4 text-gray-300 dark:text-gray-600 empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                </svg>
                <p class="text-lg">暂无收藏文章</p>
              </div>
              <div v-else class="space-y-4">
                <div
                  @click="goArticleById(article.id)"
                  v-for="article in collectedArticles" :key="article.id" class="article-card article-item border-b border-gray-200 dark:border-dark-700 pb-4 transition-all duration-300 hover:pl-2 cursor-pointer">
                  <h3 class="text-lg font-medium hover:text-primary dark:hover:text-primary-light cursor-pointer">
                    {{ article.title }}
                  </h3>
                  <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400 mt-2">
                    <h3>
                      <span class="flex items-center transition-colors hover:text-primary dark:hover:text-primary-light"></span>
                    {{ article.title }}
                  </h3>
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
                <div class="flex justify-center mt-6">
                  <a-pagination
                    v-model:current="collectedPage"
                    :total="collectedTotal"
                    :pageSize="collectedPageSize"
                    show-less-items
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码模态框 -->
    <a-modal
      v-model:visible="showPasswordModal"
      title="修改密码"
      :footer="null"
      centered
    >
      <a-form layout="vertical">
        <a-form-item label="当前密码">
          <a-input-password
            v-model:value="passwordForm.oldPassword"
            placeholder="请输入当前密码"
          />
        </a-form-item>
        <a-form-item label="新密码">
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入新密码"
          />
        </a-form-item>
        <a-form-item label="确认新密码">
          <a-input-password
            v-model:value="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
        <div class="flex justify-end">
          <a-button @click="showPasswordModal = false" class="mr-2">取消</a-button>
          <a-button type="primary" @click="submitPasswordChange">确认修改</a-button>
        </div>
      </a-form>
    </a-modal>
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

/* 选项卡样式 */
.tab-button {
  transition: all 0.3s ease;
}

.tab-button:hover {
  color: #3b82f6;
  border-color: #3b82f6;
}

/* 文章列表样式 */
.article-item {
  transition: all 0.3s ease;
}

.article-item:hover {
  transform: translateX(4px);
  background-color: rgba(59, 130, 246, 0.05);
}

.dark .article-item:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

/* 空状态图标 */
.empty-icon {
  opacity: 0.5;
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
  background-color: #60a5fa;
}

/* 作者信息链接样式 */
.author-link {
  transition: color 0.2s ease;
}

.author-link:hover {
  color: #2563eb;
  text-decoration: underline;
}
</style>
