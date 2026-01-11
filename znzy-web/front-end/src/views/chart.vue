<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import router from "@/router/index.js"
import { get, post } from "@/net/index.js";
import {message} from "ant-design-vue";

const [messageApi,contextHolder] = message.useMessage();

// 用户信息
const User = ref({ id: null })

// 响应式数据
const metrics = ref({
  totalArticles: 0,
  totalViews: 0,
  totalLikes: 0,
  totalComments: 0,
  totalFavorites: 0
})

const selectedPeriod = ref('week')
const timePeriods = ref([
  { label: '周', value: 'week' },
  { label: '月', value: 'month' },
  { label: '年', value: 'year' }
])

const popularArticles = ref([])

// 专门用于存储分类数据的响应式变量
const categoryData = ref([])

// 检测当前主题
const isDarkTheme = ref(false)

const checkTheme = () => {
  isDarkTheme.value = document.documentElement.classList.contains('dark')
}

// 获取当前用户信息
const getCurrentUser = () => {
  get('api/user/current', {}, (message, data) => {
    User.value = data
    console.log('当前用户ID:', User.value.id)
  })
}

const getTopArticles = () => {
  get("api/article/getTopArticles", {}, (message, data) => {
    popularArticles.value = data;
  })
}

const getArticleCount = () => {
  get("api/dataVisualization/getArticleCount", {}, (message, data) => {
    metrics.value.totalArticles = data;
  })
}

const getViewsCount = () => {
  get("api/dataVisualization/getViewsCount", {}, (message, data) => {
    metrics.value.totalViews = data;
  })
}

const getLikesCount = () => {
  get("api/dataVisualization/getLikesCount", {}, (message, data) => {
    metrics.value.totalLikes = data;
  })
}

const getCommentCount = () => {
  get("api/dataVisualization/getCommentCount", {}, (message, data) => {
    metrics.value.totalComments = data;
  })
}

const getFavoritesCount = () => {
  get("api/dataVisualization/getFavoritesCount", {}, (message, data) => {
    metrics.value.totalFavorites = data;
    messageApi.success(message)
  })
}

// 获取分类数据的方法
const getCategoryCount = () => {
  if (!User.value.id) {
    console.log('等待用户信息加载...')
    return
  }

  get("api/dataVisualization/getCategoryCount", {
    authorId: User.value.id
  }, (message, data) => {
    if (data && Array.isArray(data)) {
      categoryData.value = data
      console.log("文章分类数据:", categoryData.value)
      updateCategoriesChart()
    } else {
      console.error("分类数据格式错误:", data)
      categoryData.value = []
    }
  }, (error) => {
    console.error("获取分类数据失败:", error)
    categoryData.value = []
  })
}

// 图表引用
const viewsChart = ref(null)
const categoriesChart = ref(null)
const activityChart = ref(null)

// 图表实例
let viewsChartInstance = null
let categoriesChartInstance = null
let activityChartInstance = null

// 方法
const goArticleById = (id) => {
  router.push("/view/" + id)
}

// 获取主题相关的颜色
const getThemeColors = () => {
  const isDark = isDarkTheme.value
  return {
    textColor: isDark ? '#d1d5db' : '#1f2937',
    textSecondary: isDark ? '#9ca3af' : '#6b7280',
    borderColor: isDark ? '#374151' : '#e5e7eb',
    gridColor: isDark ? '#374151' : '#f3f4f6',
    backgroundColor: 'transparent'
  }
}

// 更新分类图表
const updateCategoriesChart = () => {
  if (!categoriesChartInstance) return

  const colors = getThemeColors()

  // 转换数据格式为 ECharts 需要的格式
  const chartData = categoryData.value.map(item => ({
    value: item.count || 0,
    name: item.category || '未分类'
  }))

  // 如果没有数据，显示空状态
  if (chartData.length === 0) {
    const emptyOption = {
      backgroundColor: colors.backgroundColor,
      title: {
        text: '暂无分类数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: colors.textSecondary,
          fontSize: 14,
          fontWeight: 'normal'
        }
      },
      series: []
    }
    categoriesChartInstance.setOption(emptyOption)
    return
  }

  const option = {
    backgroundColor: colors.backgroundColor,
    tooltip: {
      trigger: 'item',
      textStyle: {
        color: colors.textColor
      },
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: colors.textColor
      }
    },
    series: [
      {
        name: '文章分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: isDarkTheme.value ? '#1f2937' : '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center',
          color: colors.textColor
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold',
            color: colors.textColor
          }
        },
        labelLine: {
          show: false
        },
        data: chartData
      }
    ]
  }

  categoriesChartInstance.setOption(option)
}

// 初始化图表
const initCharts = () => {
  checkTheme()

  // 销毁现有实例
  if (viewsChartInstance) viewsChartInstance.dispose()
  if (categoriesChartInstance) categoriesChartInstance.dispose()
  if (activityChartInstance) activityChartInstance.dispose()

  const colors = getThemeColors()

  // 初始化浏览量趋势图
  viewsChartInstance = echarts.init(viewsChart.value)
  const viewsOption = {
    backgroundColor: colors.backgroundColor,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
      backgroundColor: colors.backgroundColor
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: {
        lineStyle: {
          color: colors.borderColor
        }
      },
      axisLabel: {
        color: colors.textColor
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: colors.borderColor
        }
      },
      axisLabel: {
        color: colors.textColor
      },
      splitLine: {
        lineStyle: {
          color: colors.gridColor
        }
      }
    },
    series: [
      {
        name: '浏览量',
        type: 'bar',
        data: [3200, 2800, 3500, 4200, 3800, 4500, 5200],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#8b5cf6' }
          ])
        }
      }
    ]
  }
  viewsChartInstance.setOption(viewsOption)

  // 初始化分类分布图（先设置空图表）
  categoriesChartInstance = echarts.init(categoriesChart.value)
  const emptyCategoriesOption = {
    backgroundColor: colors.backgroundColor,
    title: {
      text: '文章分类',
      left: 'center',
      top: 'center',
      textStyle: {
        color: colors.textSecondary,
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    series: []
  }
  categoriesChartInstance.setOption(emptyCategoriesOption)

  // 初始化活跃时段图
  activityChartInstance = echarts.init(activityChart.value)
  const activityOption = {
    backgroundColor: colors.backgroundColor,
    tooltip: {
      trigger: 'axis',
      textStyle: {
        color: colors.textColor
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
      backgroundColor: colors.backgroundColor
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
      axisLine: {
        lineStyle: {
          color: colors.borderColor
        }
      },
      axisLabel: {
        color: colors.textColor
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: colors.borderColor
        }
      },
      axisLabel: {
        color: colors.textColor
      },
      splitLine: {
        lineStyle: {
          color: colors.gridColor
        }
      }
    },
    series: [
      {
        name: '活跃用户',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 4,
          color: '#8b5cf6'
        },
        itemStyle: {
          color: '#8b5cf6'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139, 92, 246, 0.5)' },
            { offset: 1, color: 'rgba(139, 92, 246, 0.1)' }
          ])
        },
        data: [120, 80, 200, 150, 300, 450]
      }
    ]
  }
  activityChartInstance.setOption(activityOption)
}

// 监听窗口大小变化
const handleResize = () => {
  if (viewsChartInstance) viewsChartInstance.resize()
  if (categoriesChartInstance) categoriesChartInstance.resize()
  if (activityChartInstance) activityChartInstance.resize()
}

// 监听主题变化
const observeThemeChange = () => {
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === 'class') {
        checkTheme()
        nextTick(() => {
          initCharts()
          // 重新设置分类数据
          if (categoryData.value.length > 0) {
            updateCategoriesChart()
          }
        })
      }
    })
  })

  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class']
  })
}

// 生命周期
onMounted(() => {
  // 先获取用户信息
  getCurrentUser()

  // 等待用户信息加载完成后获取其他数据
  setTimeout(() => {
    getFavoritesCount()
    getArticleCount()
    getViewsCount()
    getLikesCount()
    getCommentCount()
    getTopArticles()
    getCategoryCount() // 这个依赖用户ID

    nextTick(() => {
      initCharts()
      observeThemeChange()
    })
  }, 100)

  window.addEventListener('resize', handleResize)
})

// 监听分类数据变化
watch(categoryData, (newData) => {
  if (newData && newData.length > 0) {
    updateCategoriesChart()
  }
})

// 监听时间周期变化
watch(selectedPeriod, (newPeriod) => {
  console.log('时间周期变化:', newPeriod)
})
</script>

<template>
  <contextHolder></contextHolder>
  <div class="analytics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">博客数据统计</h1>
      <p class="page-subtitle">深入了解您的博客表现和读者行为</p>
    </div>

    <!-- 关键指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
            <path fill="#666666" d="M17.537 12.625a4.42 4.42 0 0 0 2.684 4.047a11 11 0 0 1-1.384 2.845c-.834 1.218-1.7 2.432-3.062 2.457c-1.34.025-1.77-.794-3.3-.794c-1.531 0-2.01.769-3.275.82c-1.316.049-2.317-1.318-3.158-2.532c-1.72-2.484-3.032-7.017-1.27-10.077A4.9 4.9 0 0 1 8.91 6.884c1.292-.025 2.51.869 3.3.869c.789 0 2.27-1.075 3.828-.917a4.67 4.67 0 0 1 3.66 1.984a4.52 4.52 0 0 0-2.16 3.805m-2.52-7.432A4.4 4.4 0 0 0 16.06 2a4.48 4.48 0 0 0-2.945 1.516a4.18 4.18 0 0 0-1.061 3.093a3.7 3.7 0 0 0 2.967-1.416Z"/>
          </svg>
        </div>
        <div class="metric-content">
          <h3 class="metric-value">{{ metrics.totalArticles }}</h3>
          <p class="metric-label">总文章数</p>
        </div>
        <div class="metric-trend positive">
          <span>+12%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
            <path fill="#666666" d="M17.537 12.625a4.42 4.42 0 0 0 2.684 4.047a11 11 0 0 1-1.384 2.845c-.834 1.218-1.7 2.432-3.062 2.457c-1.34.025-1.77-.794-3.3-.794c-1.531 0-2.01.769-3.275.82c-1.316.049-2.317-1.318-3.158-2.532c-1.72-2.484-3.032-7.017-1.27-10.077A4.9 4.9 0 0 1 8.91 6.884c1.292-.025 2.51.869 3.3.869c.789 0 2.27-1.075 3.828-.917a4.67 4.67 0 0 1 3.66 1.984a4.52 4.52 0 0 0-2.16 3.805m-2.52-7.432A4.4 4.4 0 0 0 16.06 2a4.48 4.48 0 0 0-2.945 1.516a4.18 4.18 0 0 0-1.061 3.093a3.7 3.7 0 0 0 2.967-1.416Z"/>
          </svg>
        </div>
        <div class="metric-content">
          <h3 class="metric-value">{{ metrics.totalFavorites }}</h3>
          <p class="metric-label">总收藏数</p>
        </div>
        <div class="metric-trend positive">
          <span>+12%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
          </svg>
        </div>
        <div class="metric-content">
          <h3 class="metric-value">{{ metrics.totalViews.toLocaleString() }}</h3>
          <p class="metric-label">总浏览量</p>
        </div>
        <div class="metric-trend positive">
          <span>+23%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
          </svg>
        </div>
        <div class="metric-content">
          <h3 class="metric-value">{{ metrics.totalLikes.toLocaleString() }}</h3>
          <p class="metric-label">总点赞数</p>
        </div>
        <div class="metric-trend positive">
          <span>+8%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"></path>
          </svg>
        </div>
        <div class="metric-content">
          <h3 class="metric-value">{{ metrics.totalComments }}</h3>
          <p class="metric-label">总评论数</p>
        </div>
        <div class="metric-trend negative">
          <span>-3%</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 浏览量趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">浏览量趋势</h3>
          <div class="chart-actions">
            <button
              v-for="period in timePeriods"
              :key="period.value"
              :class="['period-btn', { active: selectedPeriod === period.value }]"
              @click="selectedPeriod = period.value"
            >
              {{ period.label }}
            </button>
          </div>
        </div>
        <div class="chart-container">
          <div ref="viewsChart" class="chart"></div>
        </div>
      </div>

      <!-- 文章分类分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">文章分类分布</h3>
        </div>
        <div class="chart-container">
          <div ref="categoriesChart" class="chart"></div>
        </div>
      </div>

      <!-- 热门文章排行 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">本站热门文章排行</h3>
        </div>
        <div class="popular-articles">
          <div
            v-for="(article, index) in popularArticles"
            :key="article.id"
            class="article-item"
            @click="goArticleById(article.id)"
          >
            <div class="article-rank">
              <span :class="['rank-number', { top3: index < 3 }]">{{ index + 1 }}</span>
            </div>
            <div class="article-info">
              <h4 class="article-title">{{ article.title }}</h4>
              <div class="article-meta">
                <span class="article-views">{{ article.views.toLocaleString() }} 次浏览</span>
              </div>
            </div>
            <div class="article-actions">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 读者活跃时段 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">读者活跃时段</h3>
        </div>
        <div class="chart-container">
          <div ref="activityChart" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 明暗主题变量定义 */
:root {
  /* 浅色主题 */
  --bg-primary: #ffffff;
  --bg-secondary: #f9fafb;
  --bg-card: #ffffff;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-muted: #9ca3af;
  --border-color: #e5e7eb;
  --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-hover: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* 深色主题 - 使用 .dark 类选择器 */
:global(.dark) {
  --bg-primary: #111827;
  --bg-secondary: #1f2937;
  --bg-card: #1f2937;
  --text-primary: #f9fafb;
  --text-secondary: #d1d5db;
  --text-muted: #9ca3af;
  --border-color: #374151;
  --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -1px rgba(0, 0, 0, 0.2);
  --shadow-hover: 0 20px 25px -5px rgba(0, 0, 0, 0.4), 0 10px 10px -5px rgba(0, 0, 0, 0.3);
}

.analytics-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
  background: var(--bg-primary);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.metric-card {
  border-radius: 1rem;
  padding: 1.5rem;
  background: var(--bg-card);
  box-shadow: var(--shadow);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.metric-icon {
  width: 5rem;
  height: 5rem;
  border-radius: 0.75rem;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
}

.metric-icon svg {
  width: 1.5rem;
  height: 1.5rem;
  color: white;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 0.25rem;
}

.metric-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.metric-trend {
  padding: 0.25rem 0.5rem;
  border-radius: 0.5rem;
  font-size: 0.75rem;
  font-weight: 600;
}

.metric-trend.positive {
  background: rgba(34, 197, 94, 0.1);
  color: rgb(34, 197, 94);
}

.metric-trend.negative {
  background: rgba(239, 68, 68, 0.1);
  color: rgb(239, 68, 68);
}

/* 深色模式下的趋势标签 */
:global(.dark) .metric-trend.positive {
  background: rgba(34, 197, 94, 0.2);
  color: rgb(74, 222, 128);
}

:global(.dark) .metric-trend.negative {
  background: rgba(239, 68, 68, 0.2);
  color: rgb(248, 113, 113);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.chart-card {
  border-radius: 1rem;
  padding: 1.5rem;
  background: var(--bg-card);
  box-shadow: var(--shadow);
  border: 1px solid var(--border-color);
}

.chart-card:nth-child(3) {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.chart-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-actions {
  display: flex;
  gap: 0.5rem;
}

.period-btn {
  padding: 0.375rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.period-btn:hover {
  background: var(--bg-primary);
}

.period-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.chart-container {
  height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
  background: transparent !important;
}

.popular-articles {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.article-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-radius: 0.75rem;
  background: var(--bg-secondary);
  transition: all 0.2s;
  cursor: pointer;
  border: 1px solid var(--border-color);
}

.article-item:hover {
  background: var(--bg-primary);
  transform: translateX(4px);
  box-shadow: var(--shadow);
}

.article-rank {
  margin-right: 1rem;
}

.rank-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 0.5rem;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.875rem;
  border: 1px solid var(--border-color);
}

.rank-number.top3 {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  color: white;
  border: none;
}

.article-info {
  flex: 1;
}

.article-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.article-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.article-actions {
  color: var(--text-muted);
  transition: color 0.2s;
}

.article-item:hover .article-actions {
  color: #3b82f6;
}

/* 动画效果 */
.animate-fade-in {
  animation: fadeIn 0.6s ease-out;
}

.animate-slide-up {
  animation: slideUp 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-card:nth-child(3) {
    grid-column: span 1;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .chart-actions {
    align-self: stretch;
    justify-content: center;
  }
}

/* 确保 SVG 图标颜色适配主题 */
.metric-icon svg path {
  fill: white;
}
</style>
