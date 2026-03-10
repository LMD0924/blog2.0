<template>
  <!-- 页面加载遮罩 -->
  <div v-if="isLoading" class="fixed inset-0 z-50 flex items-center justify-center bg-white dark:bg-dark-900 transition-opacity duration-1000"
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
        备忘录
      </h3>
      <p class="text-gray-600 dark:text-gray-400">加载中...</p>
    </div>
  </div>
  <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="container mx-auto px-4 py-8 transition-opacity duration-1000">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center mb-8 animate-fade-in">
      <h1 class="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary dark:from-primary-light dark:to-secondary-light">我的备忘录</h1>
      <button
        @click="showCreateModal = true"
        class="flex items-center px-4 py-2 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5"
      >
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
        </svg>
        新建备忘录
      </button>
    </div>

    <!-- 备忘录列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- 单个备忘录卡片 -->
      <div
        v-for="(memo, index) in memos"
        :key="index"
        class="relative bg-white dark:bg-dark-800 rounded-xl shadow-modern hover:shadow-hover transition-all duration-300 overflow-hidden transform hover:-translate-y-2 animate-fade-in animate-delay-100 border border-gray-100 dark:border-dark-700 global-card"
        :class="{'border-l-4 border-primary': memo.priority === 'high', 'border-l-4 border-secondary': memo.priority === 'medium', 'border-l-4 border-tertiary': memo.priority === 'low'}"
      >
        <!-- 备忘录内容 -->
        <div class="p-6">
          <div class="flex justify-between items-start mb-2">
            <h3 class="text-xl font-semibold text-gray-800 dark:text-gray-100 group-hover:text-primary transition-colors duration-300">{{ memo.title }}</h3>
            <span
              class="px-2 py-1 text-xs rounded-full transition-transform duration-200 hover:scale-105"
              :class="{
                'bg-primary/10 text-primary dark:bg-primary/20 dark:text-primary-light': memo.priority === 'high',
                'bg-secondary/10 text-secondary dark:bg-secondary/20 dark:text-secondary-light': memo.priority === 'medium',
                'bg-tertiary/10 text-tertiary dark:bg-tertiary/20 dark:text-tertiary-light': memo.priority === 'low'
              }"
            >
              {{ priorityText[memo.priority] }}
            </span>
          </div>
          <p class="text-gray-600 dark:text-gray-300 mb-4 line-clamp-3">{{ memo.content }}</p>
          <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
            </svg>
            {{ formatDate(memo.time) }}
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="bg-gray-50 dark:bg-dark-700 px-4 py-3 flex justify-end space-x-2">
          <button
            @click="editMemo(index)"
            class="p-2 text-gray-500 hover:text-primary dark:hover:text-primary-light rounded-full hover:bg-gray-100 dark:hover:bg-dark-600 transition-all duration-200 hover:scale-110"
            title="编辑"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
            </svg>
          </button>
          <button
            @click="deletePlan(index)"
            class="p-2 text-gray-500 hover:text-red-500 dark:hover:text-red-400 rounded-full hover:bg-gray-100 dark:hover:bg-dark-600 transition-all duration-200 hover:scale-110"
            title="删除"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
            </svg>
          </button>
          <button
            @click="toggleComplete(index)"
            class="p-2 text-gray-500 hover:text-green-500 dark:hover:text-green-400 rounded-full hover:bg-gray-100 dark:hover:bg-dark-600 transition-all duration-200 hover:scale-110"
            :title="memo.finish === 'true' ? '标记为未完成' : '标记为已完成'"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
              :class="{'text-green-500 dark:text-green-400': memo.finish === 'true'}"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </button>
        </div>

        <!-- 完成状态标记 -->
        <div
          v-if="memo.finish === 'true'"
          class="absolute top-0 right-0 bg-green-500 text-white px-2 py-1 text-xs font-semibold rounded-bl-lg animate-fadeIn"
        >
          已完成
        </div>
      </div>

      <!-- 空状态 -->
      <div
        v-if="memos.length === 0"
        class="col-span-full flex flex-col items-center justify-center py-16 text-gray-500 dark:text-gray-400 animate-fade-in animate-delay-200"
      >
        <svg class="w-20 h-20 mb-4 text-gray-300 dark:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
        </svg>
        <p class="text-lg mb-2">暂无备忘录</p>
        <p class="text-sm">点击右上角按钮创建你的第一条备忘录</p>
      </div>
    </div>

    <!-- 创建/编辑备忘录的模态框 -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 transition-opacity duration-300 animate-fade-in"
    >
      <div class="bg-white dark:bg-dark-800 rounded-xl shadow-xl w-full max-w-md mx-4 transition-all duration-300 transform animate-slide-up global-card">
        <div class="p-6 ">
          <h2 class="text-xl font-semibold text-gray-800 dark:text-gray-100 mb-4">
            {{ editingIndex === null ? '新建备忘录' : '编辑备忘录' }}
          </h2>

          <form @submit.prevent="saveMemo">
            <div class="mb-4">
              <label for="title" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">标题</label>
              <input
                type="text"
                id="title"
                v-model="currentMemo.title"
                class="w-full px-3 py-2 border border-gray-300 dark:border-dark-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary dark:focus:ring-primary-light bg-white dark:bg-dark-700 text-gray-900 dark:text-gray-100 transition-all duration-200"
                required
              >
            </div>

            <div class="mb-4">
              <label for="content" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">内容</label>
              <textarea
                id="content"
                v-model="currentMemo.content"
                rows="4"
                class="w-full px-3 py-2 border border-gray-300 dark:border-dark-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary dark:focus:ring-primary-light bg-white dark:bg-dark-700 text-gray-900 dark:text-gray-100 transition-all duration-200"
                required
              ></textarea>
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">优先级</label>
              <div class="flex space-x-4">
                <label class="inline-flex items-center transition-all duration-200 hover:scale-105 cursor-pointer">
                  <input
                    type="radio"
                    v-model="currentMemo.priority"
                    value="high"
                    class="text-primary focus:ring-primary dark:text-primary-light dark:focus:ring-primary-light"
                  >
                  <span class="ml-2 text-gray-700 dark:text-gray-300">高</span>
                </label>
                <label class="inline-flex items-center transition-all duration-200 hover:scale-105 cursor-pointer">
                  <input
                    type="radio"
                    v-model="currentMemo.priority"
                    value="medium"
                    class="text-secondary focus:ring-secondary dark:text-secondary-light dark:focus:ring-secondary-light"
                  >
                  <span class="ml-2 text-gray-700 dark:text-gray-300">中</span>
                </label>
                <label class="inline-flex items-center transition-all duration-200 hover:scale-105 cursor-pointer">
                  <input
                    type="radio"
                    v-model="currentMemo.priority"
                    value="low"
                    class="text-tertiary focus:ring-tertiary dark:text-tertiary-light dark:focus:ring-tertiary-light"
                  >
                  <span class="ml-2 text-gray-700 dark:text-gray-300">低</span>
                </label>
              </div>
            </div>

            <div class="flex justify-end space-x-3">
              <button
                type="button"
                @click="showCreateModal = false"
                class="px-4 py-2 border border-gray-300 dark:border-dark-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-700 transition-all duration-300 transform hover:-translate-y-0.5"
              >
                取消
              </button>
              <button
                type="submit"
                @click="saveMemo"
                class="px-4 py-2 bg-primary hover:bg-primary/90 text-white rounded-lg transition-all duration-300 transform hover:-translate-y-0.5 shadow-modern"
              >
                保存
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue';
import { message } from 'ant-design-vue';
import {get, post, put} from "@/net/index.js";

const [messageApi] = message.useMessage();

// 页面加载状态
const isLoading = ref(true)
// 备忘录数据
const memos = ref([])
const User=ref([])
const getCurrentUser=()=>{
  get('api/user/current',{},(message,data)=>{
    User.value=data
    console.log(User.value.id)
  })
}
const getAllPlan=()=>{
  get('api/plan/AllPlan',{
      id:User.value.id,
    },
    (message,data)=>{
    console.log(data)
      memos.value=data
      console.log(memos.value)
      // 当数据加载完成后，设置页面加载状态为false
      setTimeout(() => {
        isLoading.value = false
      }, 500)
    })
}
const addPlan = () => {
  post('api/plan/AddPlan', {
    title: currentMemo.title,
    content: currentMemo.content,
    priority: currentMemo.priority,
    finish: currentMemo.finish
  }, (message, data) => {
    messageApi.success(message);
    // 重置当前备忘录
    Object.assign(currentMemo, {
      id: null,
      title: '',
      content: '',
      priority: 'medium',
      createdAt: new Date(),
      finish: false
    });
    getAllPlan();
  });
};
// 更新计划
const updatePlan = () => {
  post('api/plan/UpdatePlan', {
    id: currentMemo.id, // 使用当前编辑的备忘录ID
    title: currentMemo.title,
    content: currentMemo.content,
    priority: currentMemo.priority,
    finish: currentMemo.finish
  }, (message, data) => {
    messageApi.success(message);
    getAllPlan();
  });
};

// 删除计划
const deletePlan = (index) => {
  const memoId = memos.value[index].id;
  post('api/plan/delete', {
    id: memoId // 传递要删除的备忘录ID
  }, (message, data) => {
    messageApi.success(message);
    getAllPlan(); // 刷新列表
  });
};
onMounted(()=>{
  getCurrentUser()
  getAllPlan()
})
// 优先级文本映射
const priorityText = {
  high: '高优先级',
  medium: '中优先级',
  low: '低优先级'
};

// 模态框状态
const showCreateModal = ref(false);
const editingIndex = ref(null);

// 当前编辑的备忘录
const currentMemo = reactive({
  id: null, // 添加ID字段
  title: '',
  content: '',
  priority: 'medium',
  createdAt: new Date(),
  finish: false
});

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

// 编辑备忘录
const editMemo = (index) => {
  Object.assign(currentMemo, memos.value[index]);
  editingIndex.value = index;
  showCreateModal.value = true;
};

// 保存备忘录
const saveMemo = () => {
  if (editingIndex.value !== null) {
    updatePlan();
    messageApi.success('备忘录已更新');
  } else {
    addPlan();
    messageApi.success('备忘录已创建');
  }
  showCreateModal.value = false;
};

// 切换完成状态
const toggleComplete = (index) => {
  const memo = memos.value[index];
  const newFinishStatus = memo.finish === 'true' ? 'false' : 'true';  // 处理字符串类型

  post('api/plan/UpdatePlan', {
    id: memo.id,
    title: memo.title,
    content: memo.content,
    priority: memo.priority,
    finish: newFinishStatus
  }, (message, data) => {
    const status = newFinishStatus === 'true' ? '已完成' : '未完成';
    messageApi.success(`已标记为${status}`);
    memo.finish = newFinishStatus;
  });
};
</script>

<style scoped>
/* 动画效果会从全局样式中获取 */

/* 如果需要额外的样式，可以在这里添加 */
</style>
