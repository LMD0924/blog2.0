<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {get, post} from "@/net/index.js";
import {message} from "ant-design-vue";

const [messageApi, contextHolder] = message.useMessage();
const router = useRouter();
const user=ref({});
// 主题切换逻辑
const isDark = ref(false);
const toggleTheme = () => {
  isDark.value = !isDark.value;
  localStorage.setItem('darkMode', isDark.value);
};
// 全局加载状态
const isLoading = ref(false);
// 卡片动画
const animateCard = ref(true);

// 表单数据
const form = ref({
  username: '',
  password: '',
  remember: false
});

// 表单错误
const errors = ref({
  username: '',
  password: ''
});

// 显示密码
const showPassword = ref(false);

// 加载状态
const loading = ref(false);

// 清除错误
const clearError = (field) => {
  if (errors.value[field]) {
    errors.value[field] = '';
  }
};

// 社交登录
const socialLogin = (provider) => {
  // 这里实现社交登录逻辑
  console.log(`Logging in with ${provider}`);
  // 实际项目中这里会重定向到相应的OAuth端点
};
const Login = () => {
  post('api/auth/login', {
    username: form.value.username,
    password: form.value.password
  }, (message, data) => {
    // 登录成功，先存token
     localStorage.setItem("authToken", data);
    // 获取当前用户信息
    get('api/user/current', {}, (msg, userData) => {
      if (userData.status !== '审核通过') {
        // 清除token
        localStorage.removeItem("authToken");
        messageApi.error('您的账号尚未审核通过，无法登录！');
        return;
      }
      // 审核通过才允许进入主页
      messageApi.success(message);
      isLoading.value = true;
      setTimeout(() => {
        router.push('/Home');
      }, 1000);
    });
  });
};

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('darkMode');
  if (savedTheme !== null) {
    isDark.value = savedTheme === 'true';
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  // 5秒后停止卡片动画
  setTimeout(() => {
    animateCard.value = false;
  }, 5000);
});
</script>

<template>
  <contextHolder />
  <div :class="{'dark': isDark}" class="min-h-screen transition-colors duration-300">
    <!-- CSS变量定义 -->
    <div
      v-if="isLoading"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="loader">
        <div class="loader-inner">
          <div class="loader-line-wrap">
            <div class="loader-line"></div>
          </div>
          <div class="loader-line-wrap">
            <div class="loader-line"></div>
          </div>
          <div class="loader-line-wrap">
            <div class="loader-line"></div>
          </div>
          <div class="loader-line-wrap">
            <div class="loader-line"></div>
          </div>
          <div class="loader-line-wrap">
            <div class="loader-line"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100 min-h-screen flex flex-col">
      <!-- 导航栏 - 与博客一致 -->
      <nav class="bg-white dark:bg-gray-800 shadow-lg">
        <div class="container mx-auto px-4 py-3 flex justify-between items-center">
          <div class="flex items-center space-x-2">
            <svg class="w-8 h-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"></path>
            </svg>
            <span class="text-xl font-bold bg-gradient-to-r from-primary to-tertiary text-transparent bg-clip-text">My Blog</span>
          </div>

          <div class="flex items-center space-x-4">
            <button @click="toggleTheme" class="p-2 rounded-full focus:outline-none">
              <svg v-if="isDark" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
              </svg>
              <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
              </svg>
            </button>

            <router-link
              to="/register"
              class="hidden md:inline-block px-4 py-2 text-primary hover:text-primary/80 dark:hover:text-primary transition animate-fadeIn"
            >
              Sign Up
            </router-link>
          </div>
        </div>
      </nav>

      <!-- 主要内容 -->
      <main class="flex-grow flex items-center justify-center p-4">
        <div class="w-full max-w-md">
            <!-- 登录卡片 - 带有微妙的浮动动画 -->
            <div
              class="global-card bg-white dark:bg-gray-800 rounded-2xl shadow-modern overflow-hidden transform transition-all duration-500 hover:shadow-xl animate-fadeIn"
              :class="{'animate-float': animateCard}"
            >
              <!-- 渐变顶部装饰条 -->
              <div class="h-2 bg-gradient-to-r from-primary to-tertiary"></div>

            <div class="p-8">
              <!-- 标题和描述 -->
              <div class="text-center mb-8 animate-fadeIn" style="animation-delay: 0.2s">
                <h1 class="text-3xl font-bold mb-2 bg-gradient-to-r from-primary to-tertiary text-transparent bg-clip-text">欢迎登录</h1>
                <p class="text-gray-500 dark:text-gray-400">Sign in to access your account</p>
              </div>

              <!-- 社交登录按钮 -->
              <div class="flex justify-center space-x-4 mb-6 animate-fadeIn" style="animation-delay: 0.3s">
                <button
                  @click="socialLogin('google')"
                  class="p-2 rounded-full border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition hover:shadow-modern transform hover:-translate-y-1 duration-300"
                  aria-label="Login with Google"
                >
                  <svg class="w-5 h-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12.545 10.239v3.821h5.445c-0.712 2.315-2.647 3.972-5.445 3.972-3.332 0-6.033-2.701-6.033-6.032s2.701-6.032 6.033-6.032c1.498 0 2.866 0.549 3.921 1.453l2.814-2.814c-1.786-1.667-4.166-2.698-6.735-2.698-5.522 0-10 4.477-10 10s4.478 10 10 10c8.396 0 10-7.524 10-10 0-0.67-0.069-1.325-0.189-1.955h-9.811z" fill="currentColor"/>
                  </svg>
                </button>
                <button
                  @click="socialLogin('github')"
                  class="p-2 rounded-full border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition hover:shadow-modern transform hover:-translate-y-1 duration-300"
                  aria-label="Login with GitHub"
                >
                  <svg class="w-5 h-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" fill="currentColor"/>
                  </svg>
                </button>
                <button
                  @click="socialLogin('twitter')"
                  class="p-2 rounded-full border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition hover:shadow-modern transform hover:-translate-y-1 duration-300"
                  aria-label="Login with Twitter"
                >
                  <svg class="w-5 h-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z" fill="currentColor"/>
                  </svg>
                </button>
              </div>

              <!-- 分隔线 -->
              <div class="relative mb-6">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-gray-300 dark:border-gray-700"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                  <span class="px-2 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400">
                    Or continue with
                  </span>
                </div>
              </div>

              <!-- 表单 -->
              <form @submit.prevent="Login" class="space-y-6 animate-fadeIn" style="animation-delay: 0.4s">
                <!-- 账号输入 -->
                <div>
                  <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">账号</label>
                  <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                      </svg>
                    </div>
                    <input
                      id="username"
                      v-model="form.username"
                      autocomplete="username"
                      required
                      class="block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white transition-all duration-300"
                      :class="{'border-red-500': errors.username}"
                      @input="clearError('username')"
                    >
                  </div>
                  <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
                </div>

                <!-- 密码输入 -->
                <div>
                  <div class="flex justify-between items-center mb-1">
                    <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300">密码</label>
                    <router-link
                      to="/forgot-password"
                      class="text-sm text-blue-500 hover:text-blue-700 dark:hover:text-blue-400 transition"
                    >
                      Forgot password?
                    </router-link>
                  </div>
                  <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                      </svg>
                    </div>
                    <input
                      id="password"
                      v-model="form.password"
                      :type="showPassword ? 'text' : 'password'"
                      autocomplete="current-password"
                      required
                      class="block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white transition-all duration-300"
                      :class="{'border-red-500': errors.password}"
                      @input="clearError('password')"
                    >
                    <button
                      type="button"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center"
                      @click="showPassword = !showPassword"
                    >
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path v-if="showPassword" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"></path>
                        <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                      </svg>
                    </button>
                  </div>
                  <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
                </div>

                <!-- 记住我 -->
                <div class="flex items-center">
                  <input
                      id="remember-me"
                      v-model="form.remember"
                      type="checkbox"
                      class="h-4 w-4 text-primary focus:ring-primary border-gray-300 dark:border-gray-700 rounded transition-all duration-300"
                    >
                  <label for="remember-me" class="ml-2 block text-sm text-gray-700 dark:text-gray-300">记住我</label>
                </div>

                <!-- 提交按钮 -->
                <div>
                  <button
                    type="submit"
                    class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-modern text-sm font-medium text-white bg-gradient-to-r from-primary to-tertiary hover:from-primary/90 hover:to-tertiary/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-all duration-300 transform hover:-translate-y-0.5"
                    :disabled="loading"
                    :class="{'opacity-75 cursor-not-allowed': loading}"
                  >
                    <span v-if="!loading">立即登录</span>
                    <span v-else class="flex items-center">
                      <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Signing in...
                    </span>
                  </button>
                </div>
              </form>

              <!-- 注册链接 -->
              <div class="mt-6 text-center text-sm animate-fadeIn" style="animation-delay: 0.5s">
                <span class="text-gray-500 dark:text-gray-400">Don't have an account? </span>
                <router-link
                  to="/register"
                  class="font-medium text-primary hover:text-primary/80 dark:hover:text-primary transition relative group inline-block"
                >
                  Sign up
                  <span class="absolute -bottom-0.5 left-0 w-0 h-[2px] bg-primary dark:bg-primary transition-all duration-300 group-hover:w-full"></span>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 页脚 - 简化版 -->
      <footer class=" text-gray-300 py-6">
        <div class="container mx-auto px-4 text-center text-sm">
          <p>&copy; {{ new Date().getFullYear() }} My Blog. All rights reserved.</p>
        </div>
      </footer>
    </div>
  </div>
</template>

<style>
/* 动画效果 */
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

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out forwards;
}

.animate-scale-in {
  animation: scaleIn 0.2s ease-out forwards;
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 输入框过渡效果 */
input {
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

/* 按钮加载动画 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 现代化阴影 */
.shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 暗色模式下的现代化阴影 */
.dark .shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

/** just copy and paste this entire code in your stylesheet **/
/* 加载动画样式 */
.loader {
  position: relative;
  width: 150px;
  height: 150px;
  background: transparent;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
}

.loader:after {
  content: '';
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  bottom: 10px;
  background: transparent;
  border-radius: 50%;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.2);
}

.loader-inner {
  position: absolute;
  width: 100%;
  height: 100%;
}

.loader-line-wrap {
  position: absolute;
  width: 50%;
  height: 100%;
  overflow: hidden;
  left: 50%;
}

.loader-line-wrap:nth-child(1) { transform: rotate(0deg); }
.loader-line-wrap:nth-child(2) { transform: rotate(72deg); }
.loader-line-wrap:nth-child(3) { transform: rotate(144deg); }
.loader-line-wrap:nth-child(4) { transform: rotate(216deg); }
.loader-line-wrap:nth-child(5) { transform: rotate(288deg); }

.loader-line {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(59, 130, 246, 0.8), rgba(139, 92, 246, 0.8));
  animation: spin 1.5s infinite ease-in-out;
  transform-origin: 0 50%;
}

@keyframes spin {
  0%, 10% { transform: translateX(0) scaleX(0); }
  40% { transform: translateX(0) scaleX(1); }
  60%, 100% { transform: translateX(100%) scaleX(1); }
}

    :root {
      --primary: #3b82f6;
      --secondary: #10b981;
      --tertiary: #8b5cf6;
      --accent: #f59e0b;
    }

    .dark {
      --primary: #60a5fa;
      --secondary: #34d399;
      --tertiary: #a78bfa;
      --accent: #fbbf24;
    }
    </style>
