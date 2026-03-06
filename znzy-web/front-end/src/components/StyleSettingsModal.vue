<!-- components/StyleSettingsModal.vue -->
<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="closeModal">
    <!-- 背景遮罩 -->
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="closeModal"></div>

    <!-- 模态框内容 -->
    <div class="relative w-full max-w-2xl bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-6 animate-fade-in max-h-[90vh] overflow-y-auto global-card">
      <!-- 头部 -->
      <div class="flex items-center justify-between mb-6 sticky top-0 bg-white dark:bg-gray-800 z-10 pb-2">
        <h3 class="text-xl font-semibold text-gray-900 dark:text-white flex items-center">
          <svg class="w-6 h-6 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
          </svg>
          美化设置
        </h3>
        <button @click="closeModal" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 恢复默认设置按钮 -->
      <div class="mb-4 text-right">
        <button
          @click="resetToDefault"
          class="px-3 py-1 text-sm text-gray-600 dark:text-gray-400 hover:text-primary dark:hover:text-primary transition-colors"
        >
          恢复默认设置
        </button>
      </div>

      <!-- ========== 背景图片选择 ========== -->
      <div class="mb-8">
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4 flex items-center">
          <svg class="w-5 h-5 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          背景图片
        </h4>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <!-- 默认背景（无图片） -->
          <div
            @click="selectBackground('default')"
            class="relative group cursor-pointer"
          >
            <div
              class="h-24 rounded-lg overflow-hidden border-2 transition-all duration-300"
              :class="localBgType === 'default' ? 'border-primary ring-4 ring-primary/20' : 'border-gray-200 dark:border-gray-700 hover:border-primary/50'"
            >
              <div class="w-full h-full bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-900 flex items-center justify-center">
                <span class="text-sm text-gray-500 dark:text-gray-400">默认</span>
              </div>
            </div>
            <div v-if="localBgType === 'default'" class="absolute top-2 right-2">
              <svg class="w-5 h-5 text-primary" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
          </div>

          <!-- 渐变背景 -->
          <div
            @click="selectBackground('gradient')"
            class="relative group cursor-pointer"
          >
            <div
              class="h-24 rounded-lg overflow-hidden border-2 transition-all duration-300"
              :class="localBgType === 'gradient' ? 'border-primary ring-4 ring-primary/20' : 'border-gray-200 dark:border-gray-700 hover:border-primary/50'"
            >
              <div class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600"></div>
            </div>
            <div v-if="localBgType === 'gradient'" class="absolute top-2 right-2">
              <svg class="w-5 h-5 text-primary" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
          </div>

          <!-- 图片1 - 海滩日落 -->
          <div
            @click="selectBackground('image1')"
            class="relative group cursor-pointer"
          >
            <div
              class="h-24 rounded-lg overflow-hidden border-2 transition-all duration-300"
              :class="localBgType === 'image1' ? 'border-primary ring-4 ring-primary/20' : 'border-gray-200 dark:border-gray-700 hover:border-primary/50'"
            >
              <img
                src="/src/assets/image/image01.jpg"
                alt="海滩日落"
                class="w-full h-full object-cover"
                @error="handleImageError"
              >
            </div>
            <div v-if="localBgType === 'image1'" class="absolute top-2 right-2">
              <svg class="w-5 h-5 text-primary" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-all duration-300 rounded-lg"></div>
          </div>

          <!-- 图片2 - 山景 -->
          <div
            @click="selectBackground('image2')"
            class="relative group cursor-pointer"
          >
            <div
              class="h-24 rounded-lg overflow-hidden border-2 transition-all duration-300"
              :class="localBgType === 'image2' ? 'border-primary ring-4 ring-primary/20' : 'border-gray-200 dark:border-gray-700 hover:border-primary/50'"
            >
              <img
                src="/src/assets/image/image02.jpg"
                alt="山景"
                class="w-full h-full object-cover"
                @error="handleImageError"
              >
            </div>
            <div v-if="localBgType === 'image2'" class="absolute top-2 right-2">
              <svg class="w-5 h-5 text-primary" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-all duration-300 rounded-lg"></div>
          </div>

          <!-- 自定义颜色 -->
          <div
            @click="selectBackground('color')"
            class="relative group cursor-pointer"
          >
            <div
              class="h-24 rounded-lg overflow-hidden border-2 transition-all duration-300"
              :class="localBgType === 'color' ? 'border-primary ring-4 ring-primary/20' : 'border-gray-200 dark:border-gray-700 hover:border-primary/50'"
              :style="{ backgroundColor: localCustomColor }"
            >
              <div class="w-full h-full flex items-center justify-center">
                <span class="text-sm text-gray-700 dark:text-gray-300">自定义颜色</span>
              </div>
            </div>
            <div v-if="localBgType === 'color'" class="absolute top-2 right-2">
              <svg class="w-5 h-5 text-primary" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
          </div>

          <!-- 自定义上传 -->
          <div
            class="relative group cursor-pointer"
            @click="triggerFileUpload"
          >
            <div
              class="h-24 rounded-lg overflow-hidden border-2 border-dashed transition-all duration-300 flex flex-col items-center justify-center"
              :class="localBgType === 'custom' ? 'border-primary bg-primary/5' : 'border-gray-300 dark:border-gray-600 hover:border-primary/50 hover:bg-primary/5'"
            >
              <svg class="w-6 h-6 mb-1 text-gray-400 group-hover:text-primary transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              <span class="text-xs text-gray-500 dark:text-gray-400">上传图片</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileUpload"
            >
            <div v-if="localBgType === 'custom'" class="absolute top-2 right-2">
              <svg class="w-5 h-5 text-primary" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
          </div>
        </div>

        <!-- 自定义图片预览 -->
        <div v-if="localBgType === 'custom' && localCustomImage" class="mt-4 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">当前自定义图片：</p>
          <img :src="localCustomImage" class="w-full h-32 object-cover rounded-lg">
        </div>

        <!-- 自定义颜色选择器 -->
        <div v-if="localBgType === 'color'" class="mt-4 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">选择背景颜色：</p>
          <div class="flex items-center space-x-4">
            <input
              type="color"
              v-model="localCustomColor"
              class="w-16 h-16 cursor-pointer"
              @change="previewBackground"
            >
            <div class="flex-1">
              <input
                type="text"
                v-model="localCustomColor"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
                @input="previewBackground"
              >
            </div>
          </div>
        </div>
      </div>

      <!-- ========== 卡片样式设置 ========== -->
      <div class="mb-8">
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4 flex items-center">
          <svg class="w-5 h-5 mr-2 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z" />
          </svg>
          卡片样式
        </h4>

        <!-- 透明度滑块 -->
        <div class="mb-4">
          <div class="flex justify-between mb-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">透明度（0%-100%）</span>
            <span class="text-sm font-medium text-primary">{{ Math.round(localOpacity * 100) }}%</span>
          </div>
          <input
            type="range"
            v-model.number="localOpacity"
            min="0"
            max="1"
            step="0.01"
            class="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-lg appearance-none cursor-pointer"
          >
        </div>

        <!-- 模糊半径滑块 -->
        <div class="mb-4">
          <div class="flex justify-between mb-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">模糊半径（0px-100px）</span>
            <span class="text-sm font-medium text-primary">{{ localBlur }}px</span>
          </div>
          <input
            type="range"
            v-model.number="localBlur"
            min="0"
            max="100"
            step="1"
            class="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-lg appearance-none cursor-pointer"
          >
        </div>
      </div>

      <!-- 一、显示偏好（保留你原来的特效开关） -->
      <div class="mb-6">
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">一、显示偏好</h4>

        <!-- 特效开关 -->
        <div class="grid grid-cols-2 gap-3">
          <div class="flex items-center">
            <input type="checkbox" id="starEffect" class="mr-2" checked>
            <label for="starEffect" class="text-sm text-gray-600 dark:text-gray-400">星空特效（夜间模式）</label>
          </div>
          <div class="flex items-center">
            <input type="checkbox" id="neonEffect" class="mr-2" checked>
            <label for="neonEffect" class="text-sm text-gray-600 dark:text-gray-400">霓虹灯（夜间模式）</label>
          </div>
          <div class="flex items-center">
            <input type="checkbox" id="blurEffect" class="mr-2">
            <label for="blurEffect" class="text-sm text-gray-600 dark:text-gray-400">模糊效果（消耗性能）</label>
          </div>
          <div class="flex items-center">
            <input type="checkbox" id="sidebar" class="mr-2" checked>
            <label for="sidebar" class="text-sm text-gray-600 dark:text-gray-400">侧边栏（默认开）</label>
          </div>
          <div class="flex items-center">
            <input type="checkbox" id="fpsMonitor" class="mr-2" checked>
            <label for="fpsMonitor" class="text-sm text-gray-600 dark:text-gray-400">帧率监测（不要关闭）</label>
          </div>
          <div class="flex items-center">
            <input type="checkbox" id="snowEffect" class="mr-2">
            <label for="snowEffect" class="text-sm text-gray-600 dark:text-gray-400">雪花特效（白天模式）</label>
          </div>
        </div>
      </div>

      <!-- 二、字体设置（保留你的版权信息） -->
      <div class="mb-6">
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">二、字体设置</h4>
        <p class="text-xs text-gray-500 dark:text-gray-400 leading-relaxed">
          非商免字体未经授权只能个人使用。本站为完全非商业、非盈利性质的网站，平时用于个人学习交流，如有侵权请联系站长删除，谢谢！—致版权方
        </p>
      </div>

      <!-- 操作按钮 -->
      <div class="flex space-x-3 sticky bottom-0 bg-white dark:bg-gray-800 pt-4 border-t border-gray-200 dark:border-gray-700">
        <button
          @click="closeModal"
          class="flex-1 py-3 px-4 border border-gray-300 dark:border-gray-700 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
        >
          取消
        </button>
        <button
          @click="saveSettings"
          class="flex-1 py-3 px-4 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
        >
          保存设置
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { styleStore } from '@/stores/styleStore'
import { message } from 'ant-design-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

// 本地状态
const localBgType = ref(styleStore.state.backgroundType)
const localCustomImage = ref(styleStore.state.customBgImage)
const localCustomColor = ref(styleStore.state.customBgColor)
const localOpacity = ref(styleStore.state.cardOpacity)
const localBlur = ref(styleStore.state.cardBlur)
const fileInput = ref(null)

// 选择背景
const selectBackground = (type) => {
  localBgType.value = type
  if (!type.startsWith('image') && type !== 'custom') {
    localCustomImage.value = ''
  }
  previewBackground()
}

// 触发文件上传
const triggerFileUpload = () => {
  fileInput.value?.click()
}

// 处理文件上传
const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 检查文件类型
    if (!file.type.startsWith('image/')) {
      message.error('请选择图片文件')
      return
    }

    // 检查文件大小（限制 5MB）
    if (file.size > 5 * 1024 * 1024) {
      message.error('图片大小不能超过 5MB')
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      localCustomImage.value = e.target.result
      localBgType.value = 'custom'
      previewBackground()
    }
    reader.readAsDataURL(file)
  }
}

// 预览背景效果
const previewBackground = () => {
  const root = document.documentElement

  // 移除所有背景类
  document.body.classList.remove(
    'bg-default',
    'bg-gradient',
    'bg-image-1',
    'bg-image-2',
    'bg-image-3',
    'bg-image-4',
    'bg-custom'
  )

  // 默认背景图片路径
  const defaultBgImages = {
    image1: '/src/assets/image/image01.jpg',
    image2: '/src/assets/image/image02.jpg'
  }

  // 添加对应的背景类
  if (localBgType.value === 'default') {
    document.body.classList.add('bg-default')
  } else if (localBgType.value === 'gradient') {
    document.body.classList.add('bg-gradient')
  } else if (localBgType.value.startsWith('image')) {
    const index = localBgType.value.replace('image', '')
    document.body.classList.add(`bg-image-${index}`)
    // 设置背景图片 URL
    if (defaultBgImages[localBgType.value]) {
      root.style.setProperty('--bg-image-url', `url('${defaultBgImages[localBgType.value]}')`)
    }
  } else if (localBgType.value === 'color') {
    document.body.classList.add('bg-color')
    root.style.setProperty('--bg-color-value', localCustomColor.value)
  } else if (localBgType.value === 'custom' && localCustomImage.value) {
    document.body.classList.add('bg-custom')
    root.style.setProperty('--bg-image-url', `url('${localCustomImage.value}')`)
  }
}

// 处理图片加载错误
const handleImageError = (e) => {
  console.warn('图片加载失败:', e.target.src)
  e.target.style.display = 'none'
  e.target.parentNode.classList.add('bg-gray-200', 'dark:bg-gray-700', 'flex', 'items-center', 'justify-center')
  e.target.parentNode.innerHTML = '<span class="text-gray-400">图片缺失</span>'
}

// 预览卡片效果
watch([localOpacity, localBlur], ([newOpacity, newBlur]) => {
  document.documentElement.style.setProperty('--card-opacity', newOpacity)
  document.documentElement.style.setProperty('--card-blur', `${newBlur}px`)
})

// 保存设置
const saveSettings = () => {
  styleStore.state.backgroundType = localBgType.value
  styleStore.state.customBgImage = localCustomImage.value
  styleStore.state.customBgColor = localCustomColor.value
  styleStore.state.cardOpacity = localOpacity.value
  styleStore.state.cardBlur = localBlur.value

  styleStore.apply() // 确保样式应用
  emit('update:visible', false)
  message.success('样式设置已保存')
}

// 关闭模态框
const closeModal = () => {
  // 恢复预览前的值
  localBgType.value = styleStore.state.backgroundType
  localCustomImage.value = styleStore.state.customBgImage
  localCustomColor.value = styleStore.state.customBgColor
  localOpacity.value = styleStore.state.cardOpacity
  localBlur.value = styleStore.state.cardBlur

  // 恢复原来的样式
  styleStore.apply()
  emit('update:visible', false)
}

// 重置为默认
const resetToDefault = () => {
  localBgType.value = 'default'
  localCustomImage.value = ''
  localCustomColor.value = '#f5f7fa'
  localOpacity.value = 0.87
  localBlur.value = 0
  previewBackground()
  message.success('已恢复默认设置')
}

// 组件卸载时恢复保存的值
onUnmounted(() => {
  styleStore.apply()
})
</script>

<style scoped>
/* 滑块样式 */
input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #3b82f6;
  cursor: pointer;
  transition: all 0.2s ease;
}

input[type="range"]::-webkit-slider-thumb:hover {
  transform: scale(1.2);
  box-shadow: 0 0 0 8px rgba(59, 130, 246, 0.1);
}

input[type="range"]::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #3b82f6;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
}

input[type="range"]::-moz-range-thumb:hover {
  transform: scale(1.2);
  box-shadow: 0 0 0 8px rgba(59, 130, 246, 0.1);
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 4px;
}

.dark ::-webkit-scrollbar-thumb {
  background: #4a5568;
}

::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

.dark ::-webkit-scrollbar-thumb:hover {
  background: #718096;
}
</style>
