<template>
  <el-card shadow="hover">
    <template #header>
      <div class="confirm-header">
        <div class="header-left">
          <span>导入确认</span>
        </div>
        <div class="header-actions">
          <el-button-group>
            <el-button @click="$emit('back')">
              <el-icon><Back /></el-icon>
              上一步
            </el-button>
            <el-button type="success" @click="$emit('start-import')" :loading="isImporting">
              <el-icon><Upload /></el-icon>
              开始导入
            </el-button>
          </el-button-group>
        </div>
      </div>
    </template>

    <!-- 导入摘要 -->
    <div class="import-summary">
      <el-card shadow="hover" size="small">
        <template #header>
          <div class="summary-header">
            <span>导入信息</span>
          </div>
        </template>
        <div class="summary-content">
          <div class="summary-item">
            <span class="summary-label">文件名称:</span>
            <span class="summary-value">{{ fileName }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">数据行数:</span>
            <span class="summary-value">{{ previewData.rows.length }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">字段数量:</span>
            <span class="summary-value">{{ mappedFieldsCount }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">有效数据:</span>
            <span class="summary-value">{{ validatedData?.validCount || 0 }} 条</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">错误数据:</span>
            <span class="summary-value text-danger">{{ validatedData?.errors?.length || 0 }} 条</span>
          </div>
        </div>
      </el-card>

      <!-- 导入选项 -->
      <el-card shadow="hover" size="small">
        <template #header>
          <div class="summary-header">
            <span>导入选项</span>
          </div>
        </template>
        <div class="summary-content">
          <div class="summary-item">
            <span class="summary-label">解析模式:</span>
            <el-tag size="small">{{ parseModeLabel }}</el-tag>
          </div>
          <div class="summary-item">
            <span class="summary-label">重复处理:</span>
            <el-tag size="small">{{ duplicateActionLabel }}</el-tag>
          </div>
          <div class="summary-item">
            <span class="summary-label">错误处理:</span>
            <el-tag size="small">{{ errorActionLabel }}</el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 导入进度 -->
    <div class="import-progress" v-if="isImporting">
      <el-card shadow="hover" size="small">
        <template #header>
          <div class="progress-header">
            <span>导入进度</span>
            <el-button type="danger" size="small" @click="$emit('cancel-import')">
              <el-icon><CircleClose /></el-icon>
              取消
            </el-button>
          </div>
        </template>
        <div class="progress-content">
          <el-progress
            :percentage="importProgress.percentage"
            :status="importProgress.status"
            :color="customProgressColor"
          />
          <div class="progress-details">
            <span>{{ importProgress.processed }}/{{ importProgress.total }} 条</span>
            <span>{{ importProgress.statusText }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 导入日志 -->
    <div class="import-logs" v-if="importLogs.length > 0">
      <el-card shadow="hover" size="small">
        <template #header>
          <div class="logs-header">
            <span>导入日志</span>
            <el-button type="text" size="small" @click="clearLogs">
              清空日志
            </el-button>
          </div>
        </template>
        <div class="logs-content">
          <div
            v-for="(log, index) in importLogs"
            :key="index"
            class="log-item"
            :class="log.type"
          >
            <span class="log-time">{{ formatLogTime(log.timestamp) }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Back, Upload, CircleClose } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  fileName: {
    type: String,
    default: ''
  },
  previewData: {
    type: Object,
    required: true,
    default: () => ({ rows: [], columns: [] })
  },
  validatedData: {
    type: Object,
    default: null
  },
  fieldMapping: {
    type: Object,
    required: true,
    default: () => ({})
  },
  form: {
    type: Object,
    required: true
  },
  isImporting: {
    type: Boolean,
    default: false
  },
  importProgress: {
    type: Object,
    default: () => ({
      percentage: 0,
      processed: 0,
      total: 0,
      status: 'success',
      statusText: ''
    })
  },
  importLogs: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['back', 'start-import', 'cancel-import'])

// 计算属性
const mappedFieldsCount = computed(() => {
  return Object.values(props.fieldMapping).filter(v => v).length
})

const parseModeLabel = computed(() => {
  const labels = {
    'universal': '通用解析',
    'user': '用户数据',
    'product': '产品数据',
    'custom': '自定义映射'
  }
  return labels[props.form.parseMode] || '未知'
})

const duplicateActionLabel = computed(() => {
  const labels = {
    'skip': '跳过',
    'overwrite': '覆盖',
    'update': '更新',
    'create': '新增'
  }
  return labels[props.form.duplicateAction] || '未知'
})

const errorActionLabel = computed(() => {
  const labels = {
    'stop': '遇到错误停止',
    'continue': '继续处理',
    'skip': '跳过错误行'
  }
  return labels[props.form.errorAction] || '未知'
})

const customProgressColor = computed(() => {
  const { percentage } = props.importProgress
  if (percentage < 30) return '#67C23A'
  if (percentage < 70) return '#E6A23C'
  return '#F56C6C'
})

// 方法
const formatLogTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

const clearLogs = () => {
  // 触发清空日志事件
  emit('clear-logs')
}
</script>

<style scoped>
.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.import-summary {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-content {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.summary-label {
  width: 120px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
}

.summary-value {
  color: var(--el-text-color-primary);
}

.text-danger {
  color: var(--el-color-error);
}

.import-progress {
  margin-top: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-content {
  margin-top: 16px;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.import-logs {
  margin-top: 20px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color);
}

.logs-content {
  max-height: 200px;
  overflow-y: auto;
}

.log-item {
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-light);
  font-size: 14px;
  line-height: 1.5;
}

.log-item:last-child {
  border-bottom: none;
}

.log-item.success {
  color: var(--el-color-success);
}

.log-item.error {
  color: var(--el-color-error);
}

.log-item.warning {
  color: var(--el-color-warning);
}

.log-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-right: 8px;
}
</style>