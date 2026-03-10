<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { get, post } from "@/net/index.js"
import router from "@/router/index.js"

const [messageApi, contextHolder] = message.useMessage()

// 页面加载状态
const isLoading = ref(true)
// 消息列表数据
const notices = ref([])
const loading = ref(false)

// 搜索条件
const searchQuery = ref('')

// 计算搜索过滤后的通知列表
const searchedNotices = computed(() => {
  if (!searchQuery.value) {
    return notices.value
  }
  const query = searchQuery.value.toLowerCase()
  return notices.value.filter(notice =>
    notice.title.toLowerCase().includes(query) ||
    (notice.content && notice.content.toLowerCase().includes(query))
  )
})

// 弹框相关状态
const detailVisible = ref(false)
const editVisible = ref(false)
const deleteVisible = ref(false)
const currentNotice = ref(null)
const editForm = reactive({
  title: '',
  content: '',
  type: '普通通知',
  priority: '中',
  receive: '所有用户',
  isImportant: false,
  remarks: ''
})

// 通知类型选项
const noticeTypes = [
  { value: '普通通知', label: '普通通知' },
  { value: '重要提醒', label: '重要提醒' },
  { value: '紧急通知', label: '紧急通知' },
  { value: '成功消息', label: '成功消息' }
]

// 优先级选项
const priorityOptions = [
  { value: '低', label: '低' },
  { value: '中', label: '中' },
  { value: '高', label: '高' },
  { value: '紧急', label: '紧急' }
]

// 目标用户选项
const targetUserOptions = [
  { value: '所有用户', label: '所有用户' },
  { value: '管理员', label: '管理员' },
  { value: '普通用户', label: '普通用户' },
  { value: 'VIP用户', label: 'VIP用户' },
  { value: '新注册用户', label: '新注册用户' }
]
//获取自己的信息
const User=ref({})
const Id=ref(null)
const getUserInfo=()=>{
  get('api/user/current',{},
    (message,data)=>{
    User.value=data
      Id.value=data.id
    })
}

// 加载消息列表
const loadNotices = () => {
  loading.value = true

  get('api/notice/SelectAllNotice', {}, (message,data) => {
    notices.value = data
    console.log(notices.value)
    loading.value = false
    // 当数据加载完成后，设置页面加载状态为false
    setTimeout(() => {
      isLoading.value = false
    }, 500)
  }, (error) => {
    messageApi.error('加载消息列表失败')
    loading.value = false
    // 即使加载失败，也要设置页面加载状态为false
    setTimeout(() => {
      isLoading.value = false
    }, 500)
  })
}

// 搜索消息
const searchNotices = () => {
  // 搜索时不需要分页，直接显示所有结果
  console.log('搜索关键词:', searchQuery.value)
}

// 查看详情
const viewNotice = (noticeId) => {
  const notice = notices.value.find(n => n.id === noticeId)
  if (notice) {
    currentNotice.value = notice
    detailVisible.value = true
  }
}

// 关闭详情弹框
const closeDetail = () => {
  detailVisible.value = false
  currentNotice.value = null
}

// 打开编辑弹框
const openEditNotice = (noticeId) => {
  const notice = notices.value.find(n => n.id === noticeId)
  if (notice) {
    currentNotice.value = notice
    // 填充编辑表单
    Object.assign(editForm, {
      title: notice.title,
      content: notice.content,
      type: notice.type || '普通通知',
      priority: notice.priority || '中',
      receive: notice.receive || '所有用户',
      isImportant: notice.isImportant || false,
      remarks: notice.remarks || ''
    })
    editVisible.value = true
  }
}

// 关闭编辑弹框
const closeEdit = () => {
  editVisible.value = false
  currentNotice.value = null
  // 重置表单
  Object.assign(editForm, {
    title: '',
    content: '',
    type: '普通通知',
    priority: '中',
    receive: '所有用户',
    isImportant: false,
    remarks: ''
  })
}

// 保存编辑
const saveEdit = () => {
  if (!editForm.title.trim()) {
    messageApi.warning('请输入通知标题')
    return
  }
  if (!editForm.content.trim()) {
    messageApi.warning('请输入通知内容')
    return
  }

  const submitData = {
    id: currentNotice.value.id,
    time:new Date().toISOString(),
    ...editForm
  }

  post('api/notice/UpdateNotice', submitData, (msg) => {
    messageApi.success('更新成功')
    closeEdit()
    loadNotices()
  }, (error) => {
    messageApi.error('更新失败')
  })
}

// 打开删除确认弹框
const openDeleteNotice = (noticeId) => {
  const notice = notices.value.find(n => n.id === noticeId)
  if (notice) {
    currentNotice.value = notice
    deleteVisible.value = true
  }
}

// 关闭删除弹框
const closeDelete = () => {
  deleteVisible.value = false
  currentNotice.value = null
}

// 确认删除
const confirmDelete = () => {
  if (!currentNotice.value) return

  post('api/notice/delete', {
    id: currentNotice.value.id
  }, (msg) => {
    messageApi.success('删除成功')
    closeDelete()
    loadNotices()
  }, (error) => {
    messageApi.error('删除失败')
  })
}

// 切换发布状态
const togglePublishStatus = (notice) => {
  const newStatus = notice.status === 'published' ? 'draft' : 'published'
  post('api/notice/updateStatus', {
    id: notice.id,
    status: newStatus
  }, (msg) => {
    messageApi.success(msg)
    notice.status = newStatus
    if (newStatus === 'published') {
      notice.publishTime = new Date().toISOString()
    }
  }, (error) => {
    messageApi.error('操作失败')
  })
}

// 获取类型颜色
const getTypeColor = (type) => {
  switch (type) {
    case '普通通知': return 'blue'
    case '重要提醒': return 'orange'
    case '紧急通知': return 'red'
    case '成功消息': return 'green'
    default: return 'blue'
  }
}

// 获取类型样式
const getTypeStyle = (type) => {
  switch (type) {
    case '普通通知':
      return 'border-l-blue-500 bg-blue-50 dark:bg-blue-900/20'
    case '重要提醒':
      return 'border-l-orange-500 bg-orange-50 dark:bg-orange-900/20'
    case '紧急通知':
      return 'border-l-red-500 bg-red-50 dark:bg-red-900/20'
    case '成功消息':
      return 'border-l-green-500 bg-green-50 dark:bg-green-900/20'
    default:
      return 'border-l-blue-500 bg-blue-50 dark:bg-blue-900/20'
  }
}

// 获取类型标签颜色
const getTypeLabelColor = (type) => {
  switch (type) {
    case '普通通知':
      return 'bg-blue-500'
    case '重要提醒':
      return 'bg-orange-500'
    case '紧急通知':
      return 'bg-red-500'
    case '成功消息':
      return 'bg-green-500'
    default:
      return 'bg-blue-500'
  }
}

// 获取优先级颜色
const getPriorityColor = (priority) => {
  switch (priority) {
    case '低': return 'gray'
    case '中': return 'blue'
    case '高': return 'orange'
    case '紧急': return 'red'
    default: return 'blue'
  }
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return '-'
  return new Date(timeString).toLocaleString('zh-CN')
}

// 计算筛选后的消息数量
const filteredNoticeCount = computed(() => {
  return searchedNotices.value.length
})

onMounted(() => {
  getUserInfo()
  loadNotices()
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
        消息管理
      </h3>
      <p class="text-gray-600 dark:text-gray-400">加载中...</p>
    </div>
  </div>
  <div :class="{'opacity-0': isLoading, 'opacity-100': !isLoading}" class="container mx-auto px-4 py-8 transition-opacity duration-1000">
    <context-holder />

    <!-- 页面标题 -->
    <section class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl md:text-4xl font-bold mb-2 text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary dark:from-primary-light dark:to-secondary-light">
            消息管理
          </h1>
          <p class="text-gray-600 dark:text-gray-300">
            管理系统通知消息，共 {{ notices.length }} 条记录
          </p>
        </div>
        <router-link
          to="/Notice"
          class="px-6 py-3 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center"
        >
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          发布新通知
        </router-link>
      </div>
    </section>

    <!-- 搜索栏 -->
    <section class="mb-6 bg-white dark:bg-dark-800 rounded-2xl shadow-modern p-6 global-card">
      <div class="flex flex-col md:flex-row gap-4 items-center">
        <!-- 关键词搜索 -->
        <div class="w-full md:w-auto md:flex-grow relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索通知标题或内容..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
            @input="searchNotices"
            @keyup.enter="searchNotices"
          >
          <svg class="absolute left-3 top-2.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <!-- 清除按钮 -->
          <button
            v-if="searchQuery"
            @click="searchQuery = ''; searchNotices()"
            class="absolute right-3 top-2.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- 搜索按钮 -->
        <div class="w-full md:w-auto">
          <button
            @click="searchNotices"
            class="w-full px-6 py-2 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center justify-center"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            搜索
          </button>
        </div>
      </div>
    </section>

    <!-- 消息列表 -->
    <section class="bg-white dark:bg-dark-800 rounded-2xl shadow-modern overflow-hidden global-card">
      <!-- 列表头部 -->
      <div class="border-b border-gray-200 dark:border-dark-700 px-6 py-4 bg-gray-50 dark:bg-dark-700/50">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            通知列表
          </h3>
          <div class="text-sm text-gray-500 dark:text-gray-400">
            共 {{ searchedNotices.length }} 条通知
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="p-8 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
        <p class="mt-2 text-gray-500 dark:text-gray-400">加载中...</p>
      </div>

      <!-- 消息列表内容 -->
      <div v-else-if="searchedNotices.length > 0" class="p-6 space-y-4">
        <div
          v-for="notice in searchedNotices"
          :key="notice.id"
          class="p-4 rounded-xl border-l-4 transition-all duration-300 hover:shadow-modern group"
          :class="getTypeStyle(notice.type)"
        >
          <div class="flex items-start justify-between mb-2">
            <h4 class="font-semibold text-gray-900 dark:text-gray-100 text-lg">{{ notice.title }}</h4>
            <div class="flex items-center gap-2">
              <span
                class="text-xs px-2 py-1 rounded-full text-white shrink-0"
                :class="getTypeLabelColor(notice.type)"
              >
                {{ notice.type || '通知' }}
              </span>
              <span
                v-if="notice.isImportant"
                class="text-xs px-2 py-1 bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 rounded-full shrink-0 flex items-center"
              >
                <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
                重要
              </span>
            </div>
          </div>

          <p class="text-sm text-gray-600 dark:text-gray-300 mb-3 line-clamp-2">
            {{ notice.content }}
          </p>

          <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
            <div class="flex items-center gap-4">
              <span class="flex items-center">
                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                创建: {{ formatTime(notice.time) }}
              </span>
              <span class="flex items-center">
                优先级:
                <span class="ml-1 font-medium">
                  {{ notice.priority || '中' }}
                </span>
              </span>
            </div>
            <div class="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
              <button
                @click="viewNotice(notice.id)"
                class="px-3 py-1 text-xs bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 rounded-lg hover:bg-blue-200 dark:hover:bg-blue-900/50 transition-colors duration-200"
              >
                查看详情
              </button>
              <button
                v-if="Id===notice.userId"
                @click="openEditNotice(notice.id)"
                class="px-3 py-1 text-xs bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 rounded-lg hover:bg-green-200 dark:hover:bg-green-900/50 transition-colors duration-200"
              >
                编辑
              </button>
              <button
                v-if="notice.status !== 'published'"
                @click="togglePublishStatus(notice)"
                class="px-3 py-1 text-xs bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300 rounded-lg hover:bg-orange-200 dark:hover:bg-orange-900/50 transition-colors duration-200"
              >
                发布
              </button>
              <button
                v-else
                @click="togglePublishStatus(notice)"
                class="px-3 py-1 text-xs bg-gray-100 dark:bg-gray-900/30 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-900/50 transition-colors duration-200"
              >
                取消发布
              </button>
              <button
                v-if="Id===notice.userId"
                @click="openDeleteNotice(notice.id)"
                class="px-3 py-1 text-xs bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300 rounded-lg hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors duration-200"
              >
                删除
              </button>
            </div>
          </div>

          <!-- 额外信息 -->
          <div class="flex items-center justify-between mt-2 pt-2 border-t border-gray-200 dark:border-gray-600 border-opacity-30">
            <div class="text-xs text-gray-500 dark:text-gray-400">
              目标用户: {{ notice.receive }}
              <span v-if="notice.publishTime" class="ml-4">
                发布时间: {{ formatTime(notice.time) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="searchQuery && searchedNotices.length === 0" class="p-12 text-center">
        <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
        </svg>
        <h4 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">没有找到匹配的通知</h4>
        <p class="text-gray-500 dark:text-gray-400 mb-6">尝试使用不同的搜索词</p>
        <button
          @click="searchQuery = ''; searchNotices()"
          class="px-6 py-2 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 inline-flex items-center"
        >
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          清除搜索
        </button>
      </div>
      <div v-else class="p-12 text-center">
        <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <h4 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">暂无通知消息</h4>
        <p class="text-gray-500 dark:text-gray-400 mb-6">还没有发布任何通知消息</p>
        <router-link
          to="/PublishNotice"
          class="px-6 py-2 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 inline-flex items-center"
        >
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          发布第一条通知
        </router-link>
      </div>
    </section>

    <!-- 详情弹框 -->
    <div v-if="detailVisible" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50 backdrop-blur-sm">
      <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-hidden">
        <!-- 弹框头部 -->
        <div class="border-b border-gray-200 dark:border-dark-700 px-6 py-4 bg-gray-50 dark:bg-dark-700/50">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              通知详情
            </h3>
            <button
              @click="closeDetail"
              class="p-2 hover:bg-gray-200 dark:hover:bg-dark-600 rounded-lg transition-colors duration-200"
            >
              <svg class="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- 弹框内容 -->
        <div class="p-6 overflow-y-auto max-h-[calc(90vh-120px)]">
          <div v-if="currentNotice" class="space-y-6">
            <!-- 标题和状态 -->
            <div class="flex items-start justify-between">
              <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100 pr-4">
                {{ currentNotice.title }}
              </h2>
              <div class="flex items-center gap-2 shrink-0">
                <span
                  class="text-sm px-3 py-1 rounded-full text-white"
                  :class="getTypeLabelColor(currentNotice.type)"
                >
                  {{ currentNotice.type || '通知' }}
                </span>
                <span
                  v-if="currentNotice.isImportant"
                  class="text-sm px-3 py-1 bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 rounded-full flex items-center"
                >
                  <svg class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                  </svg>
                  重要
                </span>
              </div>
            </div>

            <!-- 基本信息 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 bg-gray-50 dark:bg-dark-700/50 rounded-xl">
              <div class="space-y-2">
                <div class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                  <span class="font-medium w-20">优先级:</span>
                  <span class="font-semibold" :class="{
                    'text-green-600 dark:text-green-400': currentNotice.priority === '低',
                    'text-blue-600 dark:text-blue-400': currentNotice.priority === '中',
                    'text-orange-600 dark:text-orange-400': currentNotice.priority === '高',
                    'text-red-600 dark:text-red-400': currentNotice.priority === '紧急'
                  }">
                    {{ currentNotice.priority || '中' }}
                  </span>
                </div>
                <div class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                  <span class="font-medium w-20">目标用户:</span>
                  <span>{{ currentNotice.receive }}</span>
                </div>
              </div>
              <div class="space-y-2">
                <div class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                  <span class="font-medium w-20">创建时间:</span>
                  <span>{{ formatTime(currentNotice.time) }}</span>
                </div>
                <div v-if="currentNotice.publishTime" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                  <span class="font-medium w-20">发布时间:</span>
                  <span>{{ formatTime(currentNotice.time) }}</span>
                </div>
              </div>
            </div>

            <!-- 内容 -->
            <div>
              <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-3">通知内容</h4>
              <div class="prose dark:prose-invert max-w-none p-4 bg-white dark:bg-dark-900 border border-gray-200 dark:border-dark-700 rounded-xl">
                <p class="text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-wrap">
                  {{ currentNotice.content }}
                </p>
              </div>
            </div>

            <!-- 额外信息 -->
            <div v-if="currentNotice.remarks" class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-xl p-4">
              <h4 class="text-sm font-semibold text-yellow-800 dark:text-yellow-300 mb-2 flex items-center">
                <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
                备注信息
              </h4>
              <p class="text-sm text-yellow-700 dark:text-yellow-400">
                {{ currentNotice.remarks }}
              </p>
            </div>
          </div>
        </div>

        <!-- 弹框底部 -->
        <div class="border-t border-gray-200 dark:border-dark-700 px-6 py-4 bg-gray-50 dark:bg-dark-700/50">
          <div class="flex justify-end gap-3">
            <button
              @click="closeDetail"
              class="px-4 py-2 border border-gray-300 dark:border-dark-600 text-gray-700 dark:text-gray-300 rounded-xl hover:bg-gray-50 dark:hover:bg-dark-700 transition-all duration-300"
            >
              关闭
            </button>
            <button
              v-if="currentNotice"
              @click="openEditNotice(currentNotice.id)"
              class="px-4 py-2 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
              </svg>
              编辑通知
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹框 -->
    <div v-if="editVisible" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50 backdrop-blur-sm">
      <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
        <!-- 弹框头部 -->
        <div class="border-b border-gray-200 dark:border-dark-700 px-6 py-4 bg-gray-50 dark:bg-dark-700/50">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              编辑通知
            </h3>
            <button
              @click="closeEdit"
              class="p-2 hover:bg-gray-200 dark:hover:bg-dark-600 rounded-lg transition-colors duration-200"
            >
              <svg class="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- 编辑表单 -->
        <div class="p-6 overflow-y-auto max-h-[calc(90vh-120px)]">
          <div class="space-y-6">
            <!-- 标题 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                通知标题 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="editForm.title"
                type="text"
                placeholder="请输入通知标题"
                class="w-full px-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
              >
            </div>

            <!-- 类型和优先级 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  通知类型
                </label>
                <select
                  v-model="editForm.type"
                  class="w-full px-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                  <option v-for="type in noticeTypes" :key="type.value" :value="type.value">
                    {{ type.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  优先级
                </label>
                <select
                  v-model="editForm.priority"
                  class="w-full px-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                  <option v-for="priority in priorityOptions" :key="priority.value" :value="priority.value">
                    {{ priority.label }}
                  </option>
                </select>
              </div>
            </div>

            <!-- 目标用户和重要标记 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  目标用户
                </label>
                <select
                  v-model="editForm.receive"
                  class="w-full px-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100"
                >
                  <option v-for="user in targetUserOptions" :key="user.value" :value="user.value">
                    {{ user.label }}
                  </option>
                </select>
              </div>
              <div class="flex items-center">
                <label class="flex items-center cursor-pointer">
                  <input
                    v-model="editForm.isImportant"
                    type="checkbox"
                    class="hidden"
                  >
                  <div class="w-10 h-6 flex items-center bg-gray-300 rounded-full p-1 duration-300 ease-in-out"
                       :class="editForm.isImportant ? 'bg-red-500' : ''">
                    <div class="bg-white w-4 h-4 rounded-full shadow-md transform duration-300 ease-in-out"
                         :class="editForm.isImportant ? 'translate-x-4' : ''">
                    </div>
                  </div>
                  <span class="ml-3 text-sm font-medium text-gray-700 dark:text-gray-300">标记为重要通知</span>
                </label>
              </div>
            </div>

            <!-- 内容 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                通知内容 <span class="text-red-500">*</span>
              </label>
              <textarea
                v-model="editForm.content"
                rows="6"
                placeholder="请输入通知内容"
                class="w-full px-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100 resize-none"
              ></textarea>
            </div>

            <!-- 备注 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                备注信息
              </label>
              <textarea
                v-model="editForm.remarks"
                rows="3"
                placeholder="请输入备注信息（可选）"
                class="w-full px-4 py-2 border border-gray-200 dark:border-dark-700 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all duration-300 bg-white dark:bg-dark-900 text-gray-900 dark:text-gray-100 resize-none"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 弹框底部 -->
        <div class="border-t border-gray-200 dark:border-dark-700 px-6 py-4 bg-gray-50 dark:bg-dark-700/50">
          <div class="flex justify-end gap-3">
            <button
              @click="closeEdit"
              class="px-4 py-2 border border-gray-300 dark:border-dark-600 text-gray-700 dark:text-gray-300 rounded-xl hover:bg-gray-50 dark:hover:bg-dark-700 transition-all duration-300"
            >
              取消
            </button>
            <button
              @click="saveEdit"
              class="px-4 py-2 bg-primary hover:bg-primary/90 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
              </svg>
              保存修改
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认弹框 -->
    <div v-if="deleteVisible" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50 backdrop-blur-sm">
      <div class="bg-white dark:bg-dark-800 rounded-2xl shadow-2xl max-w-md w-full">
        <!-- 弹框头部 -->
        <div class="border-b border-gray-200 dark:border-dark-700 px-6 py-4">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              确认删除
            </h3>
            <button
              @click="closeDelete"
              class="p-2 hover:bg-gray-200 dark:hover:bg-dark-600 rounded-lg transition-colors duration-200"
            >
              <svg class="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- 弹框内容 -->
        <div class="p-6">
          <div class="text-center">
            <div class="w-16 h-16 mx-auto mb-4 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center">
              <svg class="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </div>
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
              确认删除通知？
            </h4>
            <p class="text-gray-600 dark:text-gray-400 mb-4">
              您即将删除通知 "<span class="font-medium text-red-600 dark:text-red-400">{{ currentNotice?.title }}</span>"。此操作不可撤销，请谨慎操作。
            </p>
          </div>
        </div>

        <!-- 弹框底部 -->
        <div class="border-t border-gray-200 dark:border-dark-700 px-6 py-4 bg-gray-50 dark:bg-dark-700/50">
          <div class="flex justify-end gap-3">
            <button
              @click="closeDelete"
              class="px-4 py-2 border border-gray-300 dark:border-dark-600 text-gray-700 dark:text-gray-300 rounded-xl hover:bg-gray-50 dark:hover:bg-dark-700 transition-all duration-300"
            >
              取消
            </button>
            <button
              @click="confirmDelete"
              class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl transition-all duration-300 shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 flex items-center"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
              确认删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

/* 弹框动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.scale-enter-active, .scale-leave-active {
  transition: all 0.3s ease;
}

.scale-enter-from, .scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
