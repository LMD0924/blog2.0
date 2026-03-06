<template>
  <el-card shadow="hover">
    <template #header>
      <div class="preview-header">
        <div class="header-left">
          <span>数据预览</span>
          <div class="header-stats">
            <el-tag type="success" size="small">
              <el-icon><Document /></el-icon>
              共 {{ previewData.rows.length }} 行
            </el-tag>
            <el-tag type="info" size="small">
              <el-icon><Grid /></el-icon>
              {{ previewData.columns.length }} 列
            </el-tag>
            <el-tag type="warning" size="small" v-if="validatedData">
              <el-icon><Check /></el-icon>
              {{ validatedData.validCount }} 条有效
            </el-tag>
            <el-tag type="danger" size="small" v-if="validatedData?.errors?.length">
              <el-icon><Warning /></el-icon>
              {{ validatedData.errors.length }} 条错误
            </el-tag>
          </div>
        </div>
        <div class="header-actions">
          <el-button-group>
            <el-button @click="$emit('back')">
              <el-icon><Back /></el-icon>
              上一步
            </el-button>
            <el-button type="primary" @click="$emit('next')">
              下一步
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </el-button-group>
          <el-button @click="$emit('export-preview')">
            <el-icon><Download /></el-icon>
            导出预览
          </el-button>
          <el-button @click="$emit('validate-data')">
            <el-icon><Finished /></el-icon>
            数据验证
          </el-button>
        </div>
      </div>
    </template>

    <!-- 预览模式选择 -->
    <div class="preview-mode">
      <el-radio-group v-model="previewMode" size="small">
        <el-radio-button label="table">表格视图</el-radio-button>
        <el-radio-button label="json">JSON视图</el-radio-button>
        <el-radio-button label="raw">原始数据</el-radio-button>
      </el-radio-group>

      <el-button-group class="preview-actions">
        <el-button size="small" @click="toggleSelectAll">
          {{ isAllSelected ? '取消全选' : '全选' }}
        </el-button>
        <el-button size="small" @click="invertSelection">
          反选
        </el-button>
        <el-button size="small" @click="clearSelection">
          清空选择
        </el-button>
      </el-button-group>
    </div>

    <!-- 表格视图 -->
    <div v-if="previewMode === 'table'" class="table-preview">
      <el-table
        ref="previewTable"
        :data="previewData.rows"
        height="500"
        border
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="80" fixed />
        <el-table-column
          v-for="(col, index) in previewData.columns"
          :key="index"
          :prop="col"
          :label="col"
          :min-width="col.length * 12 + 80"
          :sortable="true"
          show-overflow-tooltip
          resizable
        >
          <template #header="{ column }">
            <div class="column-header">
              {{ column.label }}
              <el-tag size="mini" type="danger" v-if="isRequiredField(col)">
                必填
              </el-tag>
            </div>
          </template>
          <template #default="{ row, column }">
            <div class="cell-content" :class="{ 'cell-error': hasError(row, column) }">
              {{ formatCellValue(row[column.property]) }}
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="previewData.rows.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- JSON视图 -->
    <div v-if="previewMode === 'json'" class="json-preview">
      <pre><code>{{ JSON.stringify(previewData.rows, null, 2) }}</code></pre>
    </div>

    <!-- 原始数据视图 -->
    <div v-if="previewMode === 'raw'" class="raw-preview">
      <pre><code>{{ previewData.rows.map(row => Object.values(row)).join('\n') }}</code></pre>
    </div>

    <!-- 数据统计 -->
    <div class="data-statistics">
      <el-card shadow="hover" size="small">
        <template #header>
          <div class="stats-header">
            <span>数据统计</span>
          </div>
        </template>
        <div class="stats-content">
          <div class="stats-item">
            <span class="stats-label">总行数:</span>
            <span class="stats-value">{{ previewData.rows.length }}</span>
          </div>
          <div class="stats-item">
            <span class="stats-label">总列数:</span>
            <span class="stats-value">{{ previewData.columns.length }}</span>
          </div>
          <div class="stats-item">
            <span class="stats-label">空行比例:</span>
            <span class="stats-value">{{ calculateEmptyRowRatio() }}%</span>
          </div>
          <div class="stats-item">
            <span class="stats-label">数据完整性:</span>
            <span class="stats-value">{{ calculateDataCompleteness() }}%</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据验证结果 -->
    <div class="validation-panel" v-if="validatedData">
      <el-card shadow="hover" size="small">
        <template #header>
          <div class="validation-header">
            <span>数据验证结果</span>
            <el-tag :type="validatedData.errors.length > 0 ? 'danger' : 'success'" size="small">
              {{ validatedData.validCount }} 条有效 / {{ validatedData.errors.length }} 条错误
            </el-tag>
          </div>
        </template>

        <div class="validation-stats">
          <el-progress
            :percentage="(validatedData.validCount / previewData.rows.length) * 100"
            :status="validatedData.errors.length > 0 ? 'exception' : 'success'"
            :stroke-width="20"
            :text-inside="true"
            :color="validatedData.errors.length > 0 ? '#F56C6C' : '#67C23A'"
          />
        </div>

        <div class="error-details" v-if="validatedData.errors.length > 0">
          <el-collapse v-model="errorActive">
            <el-collapse-item name="errors" title="查看错误详情">
              <div class="error-content">
                <div v-for="(error, index) in validatedData.errors.slice(0, 10)" :key="index" class="error-item">
                  <el-alert
                    :title="`第 ${error.rowIndex + 1} 行 - ${error.field}`"
                    :description="error.message"
                    type="error"
                    :closable="false"
                    show-icon
                    size="small"
                  />
                </div>
                <div v-if="validatedData.errors.length > 10" class="error-more">
                  还有 {{ validatedData.errors.length - 10 }} 条错误未显示...
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Document, Grid, Check, Warning, Back, ArrowRight, Download, Finished } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
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
    default: () => ({})
  },
  validationRules: {
    type: Object,
    default: () => ({})
  }
})

// 定义事件
const emit = defineEmits(['back', 'next', 'export-preview', 'validate-data', 'selection-change'])

// 本地状态
const previewTable = ref(null)
const previewMode = ref('table')
const selectedRows = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const errorActive = ref(['errors'])

// 计算属性
const isAllSelected = computed(() => {
  return selectedRows.value.length === props.previewData.rows.length && props.previewData.rows.length > 0
})

// 方法
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    clearSelection()
  } else {
    selectedRows.value = [...props.previewData.rows]
    emit('selection-change', selectedRows.value)
  }
}

const invertSelection = () => {
  const allRows = props.previewData.rows
  const selectedSet = new Set(selectedRows.value.map(row => row))
  selectedRows.value = allRows.filter(row => !selectedSet.has(row))
  emit('selection-change', selectedRows.value)
}

const clearSelection = () => {
  selectedRows.value = []
  emit('selection-change', selectedRows.value)
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const handleSortChange = (sort) => {
  // 可以在这里处理排序逻辑
  console.log('排序变化:', sort)
}

const formatCellValue = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return value.toString()
}

const isRequiredField = (column) => {
  // 检查字段是否为必填项
  const fieldKey = Object.keys(props.fieldMapping).find(key => props.fieldMapping[key] === column)
  return fieldKey && props.validationRules[fieldKey]?.required
}

const hasError = (row, column) => {
  // 检查单元格是否有错误
  if (!props.validatedData?.errors) return false
  const rowIndex = props.previewData.rows.indexOf(row)
  return props.validatedData.errors.some(error => 
    error.rowIndex === rowIndex && error.field === column.property
  )
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

const calculateEmptyRowRatio = () => {
  if (props.previewData.rows.length === 0) return 0
  const emptyRows = props.previewData.rows.filter(row => {
    return Object.values(row).every(value => value === null || value === undefined || value === '')
  })
  return ((emptyRows.length / props.previewData.rows.length) * 100).toFixed(1)
}

const calculateDataCompleteness = () => {
  if (props.previewData.rows.length === 0 || props.previewData.columns.length === 0) return 0
  
  let totalCells = props.previewData.rows.length * props.previewData.columns.length
  let filledCells = 0
  
  props.previewData.rows.forEach(row => {
    props.previewData.columns.forEach(col => {
      const value = row[col]
      if (value !== null && value !== undefined && value !== '') {
        filledCells++
      }
    })
  })
  
  return ((filledCells / totalCells) * 100).toFixed(1)
}
</script>

<style scoped>
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-stats {
  display: flex;
  gap: 8px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.preview-mode {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color);
}

.preview-actions {
  display: flex;
  gap: 4px;
}

.table-preview {
  margin-top: 16px;
}

.column-header {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.cell-content {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

.cell-error {
  background-color: var(--el-color-error-light-9) !important;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.json-preview,
.raw-preview {
  max-height: 500px;
  overflow: auto;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 16px;
  margin-top: 16px;
}

.json-preview pre,
.raw-preview pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.data-statistics {
  margin-top: 24px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-top: 12px;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
}

.stats-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.stats-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.validation-panel {
  margin-top: 20px;
}

.validation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.validation-stats {
  text-align: center;
  margin: 20px 0;
}

.error-details {
  margin-top: 16px;
}

.error-content {
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.error-item {
  margin-bottom: 8px;
}

.error-more {
  margin-top: 12px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style>