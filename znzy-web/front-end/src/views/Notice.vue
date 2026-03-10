<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { get, post } from "@/net/index.js"

const [messageApi, contextHolder] = message.useMessage()
const userId = ref([])

// 表单数据
const formData = reactive({
  User:{},
  title: '',
  content: '',
  type: '普通通知',
  priority: '低',
  receive: '所有用户',
  time:new Date().toISOString(),
  isImportant: false
})

// 通知类型选项
const noticeTypes = [
  { value: '普通通知', label: '普通通知', color: 'blue' },
  { value: '重要提醒', label: '重要提醒', color: 'orange' },
  { value: '紧急通知', label: '紧急通知', color: 'red' },
  { value: '成功消息', label: '成功消息', color: 'green' }
]

// 优先级选项
const priorityOptions = [
  { value: '低', label: '低', color: 'gray' },
  { value: '中', label: '中', color: 'blue' },
  { value: '高', label: '高', color: 'orange' },
  { value: '紧急', label: '紧急', color: 'red' }
]
// 目标用户选项
const targetOptions = [
  { value: '所有用户', label: '所有用户' },
  { value: '普通用户', label: '普通用户' },
]
//获取个人信息
const getUserInfo = () => {
 return new Promise((resolve,reject)=>{
   get('api/user/current', {}, (message,data) => {
     formData.User = data
     resolve(data)
   },(error)=>{
     reject(error)
   })
 })
}
// 页面加载状态
const isLoading = ref(true)
// 历史通知列表
const noticeHistory = ref([])

// 加载历史通知
const loadNoticeHistory = () => {
  get('api/notice/SelectAllNotice', {}, (message,data) => {
    noticeHistory.value = data.filter(item=>item.userId===formData.User.id)
    // 当数据加载完成后，设置页面加载状态为false
    setTimeout(() => {
      isLoading.value = false
    }, 500)
  })
}

// 发布通知
const publishNotice =async () => {
  if (!formData.title.trim()) {
    messageApi.warning('请输入通知标题')
    return
  }

  if (!formData.content.trim()) {
    messageApi.warning('请输入通知内容')
    return
  }

  await post('api/notice/InsertNotice', {
    ...formData,
    userId: formData.User.id
  }, (msg) => {
    messageApi.success(msg)
    // 重置表单
    Object.assign(formData, {
      title: '',
      content: '',
      type: '普通通知',
      priority: '低',
      receive: '所有用户',
      time: null,
      isImportant: false
    })
    // 重新加载历史记录
    loadNoticeHistory()
  })
}

// 立即发布
const publishNow = () => {
  formData.time = null
  publishNotice()
}

// 获取类型颜色
const getTypeColor = (type) => {
  const typeObj = noticeTypes.find(t => t.value === type)
  return typeObj ? typeObj.color : 'blue'
}

// 获取优先级颜色
const getPriorityColor = (priority) => {
  const priorityObj = priorityOptions.find(p => p.value === priority)
  return priorityObj ? priorityObj.color : 'blue'
}

onMounted(() => {
  getUserInfo()
  loadNoticeHistory()
})
</script>

<template>
  <!-- 页面加载遮罩 -->
  <div v-if="isLoading" class="fixed inset-0 z-50 flex items-center justify-center bg-white dark:bg-dark-900 transition-opacity duration-1000"
       :class="{'opacity-0 pointer-events-none': !isLoading}">
    <div class="text-center">
      <div class="relative mb-8">
        <div class="w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-2xl flex items-center justify-center mx-auto shadow-2xl animate-pulse">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
          </svg>
        </div>
        <div class="absolute inset-0 border-4 border-primary/20 border-t-primary rounded-2xl animate-spin"></div>
      </div>
      <h3 class="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent mb-2">
        发布通知
      </h3>
      <p class="text-gray-600 dark:text-gray-400">加载中...</p>
    </div>
  </div>
  <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="container mx-auto px-4 py-8 transition-opacity duration-1000">
    <context-holder />

    <!-- 页面标题 -->
    <section class="mb-8 text-center">
      <h1 class="text-3xl md:text-4xl font-bold mb-4 text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary dark:from-primary-light dark:to-secondary-light">
        发布通知
      </h1>
      <p class="text-gray-600 dark:text-gray-300 max-w-2xl mx-auto">
        创建和管理系统通知，确保用户及时了解重要信息
      </p>
    </section>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 左侧：发布表单 -->
      <div class="lg:col-span-2">
        <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 md:p-8 global-card">
          <h2 class="text-xl font-bold mb-6 relative">
            创建新通知
            <div class="absolute -bottom-2 left-0 w-8 h-1 bg-gradient-to-r from-primary to-secondary rounded-full"></div>
          </h2>

          <!-- 通知表单 -->
          <div class="space-y-6">
            <!-- 标题 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                通知标题 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="formData.title"
                type="text"
                placeholder="请输入通知标题"
                class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
              >
            </div>

            <!-- 内容 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                通知内容 <span class="text-red-500">*</span>
              </label>
              <textarea
                v-model="formData.content"
                rows="6"
                placeholder="请输入通知详细内容..."
                class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100 resize-vertical"
              ></textarea>
            </div>

            <!-- 设置选项 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- 通知类型 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  通知类型
                </label>
                <select
                  v-model="formData.type"
                  class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                  <option v-for="type in noticeTypes" :key="type.value" :value="type.value">
                    {{ type.label }}
                  </option>
                </select>
              </div>

              <!-- 优先级 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  优先级
                </label>
                <select
                  v-model="formData.priority"
                  class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                  <option v-for="priority in priorityOptions" :key="priority.value" :value="priority.value">
                    {{ priority.label }}
                  </option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- 目标用户 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  目标用户
                </label>
                <select
                  v-model="formData.receive"
                  class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                  <option v-for="target in targetOptions" :key="target.value" :value="target.value">
                    {{ target.label }}
                  </option>
                </select>
              </div>

              <!-- 发布时间 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  发布时间
                </label>
                <input
                  v-model="formData.time"
                  type="datetime-local"
                  class="w-full px-4 py-3 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">留空表示立即发布</p>
              </div>
            </div>

            <!-- 重要通知开关 -->
            <div class="flex items-center">
              <input
                v-model="formData.isImportant"
                type="checkbox"
                id="important"
                class="w-4 h-4 text-primary rounded focus:ring-primary/20 border-gray-300 dark:border-dark-600"
              >
              <label for="important" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                标记为重要通知
              </label>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-4 pt-4">
              <button
                @click="publishNow"
                class="flex-1 px-6 py-3 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center justify-center"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                </svg>
                立即发布
              </button>
              <button
                @click="publishNotice"
                class="flex-1 px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center justify-center"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
                {{ formData.time ? '定时发布' : '发布通知' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：历史通知和预览 -->
      <div class="space-y-6">
        <!-- 通知预览 -->
        <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 global-card">
          <h3 class="text-lg font-bold mb-4 relative">
            通知预览
            <div class="absolute -bottom-1 left-0 w-6 h-0.5 bg-gradient-to-r from-primary to-secondary rounded-full"></div>
          </h3>

          <div
            class="p-4 rounded-xl border-l-4 transition-all duration-300"
            :class="{
              'border-l-blue-500 bg-blue-50 dark:bg-blue-900/20': formData.type === '普通通知',
              'border-l-orange-500 bg-orange-50 dark:bg-orange-900/20': formData.type === '重要提醒',
              'border-l-red-500 bg-red-50 dark:bg-red-900/20': formData.type === '紧急通知',
              'border-l-green-500 bg-green-50 dark:bg-green-900/20': formData.type === '成功消息'
            }"
          >
            <div class="flex items-start justify-between mb-2">
              <h4 class="font-semibold text-gray-900 dark:text-gray-100">{{ formData.title || '通知标题' }}</h4>
              <span
                class="text-xs px-2 py-1 rounded-full text-white"
                :class="{
                  'bg-blue-500': formData.type === '普通通知',
                  'bg-orange-500': formData.type === '重要提醒',
                  'bg-red-500': formData.type === '紧急通知',
                  'bg-green-500': formData.type === '成功消息'
                }"
              >
                {{ noticeTypes.find(t => t.value === formData.type)?.label || '普通通知' }}
              </span>
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-300 mb-3">
              {{ formData.content || '通知内容将在这里显示...' }}
            </p>
            <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
              <span>优先级: {{ priorityOptions.find(p => p.value === formData.priority)?.label || '中' }}</span>
              <span>目标: {{ targetOptions.find(t => t.value === formData.receive)?.label || '所有用户' }}</span>
            </div>
          </div>
        </div>

        <!-- 最近通知 -->
        <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 global-card">
          <h3 class="text-lg font-bold mb-4 relative">
            最近通知
            <div class="absolute -bottom-1 left-0 w-6 h-0.5 bg-gradient-to-r from-primary to-secondary rounded-full"></div>
          </h3>

          <div class="space-y-3 max-h-96 overflow-y-auto">
            <div
              v-for="notice in noticeHistory.slice(0, 5)"
              :key="notice.id"
              class="p-3 rounded-lg border border-gray-100 dark:border-dark-700 hover:bg-gray-50 dark:hover:bg-dark-700 transition-all duration-300 cursor-pointer"
            >
              <div class="flex items-start justify-between mb-1">
                <h4 class="font-medium text-sm text-gray-900 dark:text-gray-100 line-clamp-1">{{ notice.title }}</h4>
                <span
                  class="text-xs px-1.5 py-0.5 rounded-full text-white shrink-0 ml-2"
                  :class="`bg-${getTypeColor(notice.type)}-500`"
                >
                  {{ noticeTypes.find(t => t.value === notice.type)?.label || '通知' }}
                </span>
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-2">{{ notice.content }}</p>
              <div class="flex items-center justify-between mt-2 text-xs text-gray-400">
                <span>{{ notice.time }}</span>
                <span class="flex items-center">
                  <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-6h2v6zm4 0h-2V9h2v8z"/>
                  </svg>
                  {{ priorityOptions.find(p => p.value === notice.priority)?.label || '中' }}
                </span>
              </div>
            </div>

            <div v-if="noticeHistory.length === 0" class="text-center py-4 text-gray-500 dark:text-gray-400">
              <svg class="w-8 h-8 mx-auto mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              <p class="text-sm">暂无通知记录</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-1 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.resize-vertical {
  resize: vertical;
}
</style>
