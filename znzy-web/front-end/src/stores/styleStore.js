// stores/styleStore.js
import { reactive, watch } from 'vue'

// 默认背景图片路径（请根据你的实际路径修改）
const defaultBgImages = {
  image1: '/src/assets/image/image01.jpg',
  image2: '/src/assets/image/image02.jpg'
}

// 创建全局样式状态
const styleState = reactive({
  // 背景设置
  backgroundType: 'default', // default, gradient, image1, image2, color, custom
  customBgImage: '',
  customBgColor: '#f5f7fa', // 默认自定义颜色

  // 卡片样式
  cardOpacity: 0.87, // 0-1
  cardBlur: 0, // px

  // 主题模式
  isDark: false,

  // 初始化标志
  initialized: false
})

// 从 localStorage 加载保存的设置
const loadSettings = () => {
  try {
    const saved = localStorage.getItem('globalStyleSettings')
    if (saved) {
      const parsed = JSON.parse(saved)
      Object.assign(styleState, parsed)
    }
  } catch (error) {
    console.error('加载样式设置失败:', error)
  }
}

// 保存设置到 localStorage
const saveSettings = () => {
  try {
    localStorage.setItem('globalStyleSettings', JSON.stringify(styleState))
  } catch (error) {
    console.error('保存样式设置失败:', error)
  }
}

// 应用样式到文档
const applyStyles = () => {
  const root = document.documentElement

  // 设置 CSS 变量
  root.style.setProperty('--card-opacity', styleState.cardOpacity)
  root.style.setProperty('--card-blur', `${styleState.cardBlur}px`)

  // 移除所有背景类
  document.body.classList.remove(
    'bg-default',
    'bg-gradient',
    'bg-image-1',
    'bg-image-2',
    'bg-image-3',
    'bg-image-4',
    'bg-custom',
    'bg-color'
  )

  // 添加对应的背景类
  if (styleState.backgroundType === 'default') {
    document.body.classList.add('bg-default')
  } else if (styleState.backgroundType === 'gradient') {
    document.body.classList.add('bg-gradient')
  } else if (styleState.backgroundType.startsWith('image')) {
    const index = styleState.backgroundType.replace('image', '')
    document.body.classList.add(`bg-image-${index}`)
    // 设置背景图片 URL
    if (defaultBgImages[styleState.backgroundType]) {
      root.style.setProperty('--bg-image-url', `url('${defaultBgImages[styleState.backgroundType]}')`)
    }
  } else if (styleState.backgroundType === 'color') {
    document.body.classList.add('bg-color')
    root.style.setProperty('--bg-color-value', styleState.customBgColor)
  } else if (styleState.backgroundType === 'custom' && styleState.customBgImage) {
    document.body.classList.add('bg-custom')
    root.style.setProperty('--bg-image-url', `url('${styleState.customBgImage}')`)
  }

  // 设置主题
  if (styleState.isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// 初始化样式
const initStyleStore = () => {
  if (!styleState.initialized) {
    loadSettings()
    applyStyles()
    styleState.initialized = true

    // 监听变化自动保存和应用
    watch(
      () => [styleState.backgroundType, styleState.customBgImage, styleState.customBgColor, styleState.cardOpacity, styleState.cardBlur, styleState.isDark],
      () => {
        applyStyles()
        saveSettings()
      },
      { deep: true }
    )
  }
}

// 重置为默认设置
const resetToDefault = () => {
  styleState.backgroundType = 'default'
  styleState.customBgImage = ''
  styleState.customBgColor = '#f5f7fa'
  styleState.cardOpacity = 0.87
  styleState.cardBlur = 0
  // 不重置 isDark，因为它可能由主题切换控制
}

export const styleStore = {
  state: styleState,
  init: initStyleStore,
  reset: resetToDefault,
  apply: applyStyles
}
