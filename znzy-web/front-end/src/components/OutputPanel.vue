<template>
  <div class="h-full overflow-auto relative bg-gray-900">
    <!-- 输出内容 -->
    <div v-if="!isLoading && hasOutput" class="p-4">
      <!-- 状态信息 -->
      <div v-if="status" class="mb-3 p-2 rounded text-sm"
           :class="statusClass">
        {{ status }}
      </div>

      <!-- 执行时间 -->
      <div v-if="executionTime !== undefined" class="text-gray-400 text-xs mb-3">
        执行时间: {{ executionTime }}ms
      </div>

      <!-- 标准输出 -->
      <div v-if="output" class="mb-4">
        <div class="text-gray-400 text-xs mb-1">标准输出:</div>
        <pre class="font-mono text-sm whitespace-pre-wrap break-all text-green-300">{{ output }}</pre>
      </div>

      <!-- 错误输出 -->
      <div v-if="error" class="mb-4">
        <div class="text-gray-400 text-xs mb-1">错误输出:</div>
        <pre class="font-mono text-sm whitespace-pre-wrap break-all text-red-300">{{ error }}</pre>
      </div>
    </div>

    <!-- 加载动画 -->
    <div v-if="isLoading" class="absolute inset-0 bg-gray-900/90 flex items-center justify-center">
      <div class="flex flex-col items-center space-y-3">
        <div class="w-10 h-10 border-4 border-gray-600 border-t-blue-500 rounded-full animate-spin"></div>
        <span class="text-gray-400 text-sm">正在编译运行...</span>
        <span class="text-gray-500 text-xs">这可能需要几秒钟</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!hasOutput" class="h-full flex flex-col items-center justify-center text-gray-500 p-8">
      <svg class="w-16 h-16 mb-4 opacity-50" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
      </svg>
      <p class="text-center text-gray-400">等待执行程序</p>
      <p class="text-sm mt-2 text-center text-gray-500">点击"运行代码"按钮执行程序</p>
      <p class="text-xs mt-1 text-center text-gray-600">支持C、Java、Python、JavaScript等语言</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 完整的响应数据
  response: {
    type: Object,
    default: null
  },
  isLoading: Boolean
})

// 计算属性
const hasOutput = computed(() => {
  return props.response && (props.response.output || props.response.error)
})

const output = computed(() => {
  if (!props.response) return ''

  // 处理不同的数据结构
  if (props.response.output !== undefined) {
    return props.response.output
  }
  if (props.response.data?.output !== undefined) {
    return props.response.data.output
  }
  if (props.response.data?.data?.output !== undefined) {
    return props.response.data.data.output
  }
  return ''
})

const error = computed(() => {
  if (!props.response) return ''

  // 处理不同的数据结构
  if (props.response.error !== undefined) {
    return props.response.error
  }
  if (props.response.data?.error !== undefined) {
    return props.response.data.error
  }
  return ''
})

const status = computed(() => {
  if (!props.response) return ''

  if (props.response.status === 'success' || props.response.code === 200) {
    return '✅ 程序执行成功'
  } else if (props.response.status === 'compile_error') {
    return '❌ 编译错误'
  } else if (props.response.status === 'error') {
    return '❌ 执行错误'
  } else if (props.response.message) {
    return props.response.message
  }
  return ''
})

const statusClass = computed(() => {
  if (props.response?.status === 'success' || props.response?.code === 200) {
    return 'bg-green-900/30 text-green-300 border border-green-800/50'
  } else if (props.response?.status === 'error' || props.response?.code !== 200) {
    return 'bg-red-900/30 text-red-300 border border-red-800/50'
  }
  return 'bg-blue-900/30 text-blue-300 border border-blue-800/50'
})

const executionTime = computed(() => {
  if (!props.response) return undefined

  // 从不同层级获取执行时间
  return props.response.executionTime ||
    props.response.data?.executionTime ||
    props.response.data?.data?.executionTime
})
</script>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 10px;
}

::-webkit-scrollbar-track {
  background: #1f2937;
}

::-webkit-scrollbar-thumb {
  background: #4b5563;
  border-radius: 5px;
}

::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}

/* 代码块样式 */
pre {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  line-height: 1.5;
  tab-size: 2;
}
</style>
