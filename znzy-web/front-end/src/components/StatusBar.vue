<template>
  <div class="mt-4 px-6">
    <div class="max-w-7xl mx-auto">
      <div class="flex flex-wrap items-center justify-between gap-2 text-sm">
        <!-- 状态指示器 -->
        <div class="flex items-center space-x-4">
          <div class="flex items-center space-x-2">
            <div
              class="w-2 h-2 rounded-full"
              :class="{
                'bg-green-500': status === 'ready',
                'bg-yellow-500': status === 'running',
                'bg-red-500': status === 'error',
                'bg-blue-500': status === 'success'
              }"
            ></div>
            <span class="text-gray-300">
              {{ getStatusText(status) }}
            </span>
          </div>

          <!-- 执行时间 -->
          <div v-if="executionTime > 0" class="flex items-center space-x-2">
            <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-400">执行时间: {{ executionTime }}ms</span>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="flex items-center space-x-3">
          <button class="text-xs text-gray-400 hover:text-gray-300 transition">
            <span class="flex items-center space-x-1">
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path d="M5 4a1 1 0 00-2 0v7.268a2 2 0 000 3.464V16a1 1 0 102 0v-1.268a2 2 0 000-3.464V4zM11 4a1 1 0 10-2 0v1.268a2 2 0 000 3.464V16a1 1 0 102 0V8.732a2 2 0 000-3.464V4zM16 3a1 1 0 011 1v7.268a2 2 0 010 3.464V16a1 1 0 11-2 0v-1.268a2 2 0 010-3.464V4a1 1 0 011-1z"/>
              </svg>
              <span>快捷键</span>
            </span>
          </button>
          <button class="text-xs text-gray-400 hover:text-gray-300 transition">
            <span class="flex items-center space-x-1">
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
              </svg>
              <span>帮助</span>
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  status: String,
  executionTime: Number
})

const getStatusText = (status) => {
  const texts = {
    ready: '就绪',
    running: '运行中',
    error: '错误',
    success: '成功'
  }
  return texts[status] || '未知状态'
}
</script>
