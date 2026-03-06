<template>
  <div class="excel-upload-container">
    <!-- 多步骤向导 -->
    <ExcelSteps v-if="showSteps" :step-active="stepActive" />

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 第1步：文件上传 -->
      <div v-show="stepActive === 0" class="step-content">
        <ExcelUpload
          v-model:file="file"
          v-model:form="form"
          :excel-meta="excelMeta"
          :support-batch="supportBatch"
          :recent-files="recentFiles"
          @download-template="downloadTemplate"
          @show-guide="showQuickGuide"
          @preview-file="previewFile"
          @parse-excel="parseExcel"
          @remove-file="removeFile"
          @load-recent-file="loadRecentFile"
          @remove-recent-file="removeRecentFile"
          @clear-recent="clearRecent"
        />
      </div>

      <!-- 第2步：数据预览 -->
      <div v-show="stepActive === 1" class="step-content">
        <excel-preview
          v-model:preview-data="previewData"
          v-model:validated-data="validatedData"
          v-model:preview-mode="previewMode"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :column-validations="columnValidations"
          :selected-rows="selectedRows"
          @back="stepActive = 0"
          @next="stepActive = 2"
          @export-preview="exportPreviewData"
          @validate-data="validateData"
          @toggle-select-all="toggleSelectAll"
          @invert-selection="invertSelection"
          @clear-selection="clearSelection"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @edit-row="editRow"
          @delete-row="deleteRow"
          @fix-errors="fixValidationErrors"
        />
      </div>

      <!-- 第3步：导入配置 -->
      <div v-show="stepActive === 2" class="step-content">
        <excel-config
          :preview-data="previewData"
          :field-mapping="fieldMapping"
          :transform-rules="transformRules"
          :default-values="defaultValues"
          :available-fields="targetFields"
          @back="stepActive = 1"
          @next="stepActive = 3"
          @update:field-mapping="fieldMapping = $event"
          @update:transform-rules="transformRules = $event"
          @update:default-values="defaultValues = $event"
        />
      </div>

      <!-- 第4步：确认导入 -->
      <div v-show="stepActive === 3" class="step-content">
        <excel-confirm
          :file-name="file?.name"
          :preview-data="previewData"
          :validated-data="validatedData"
          :field-mapping="fieldMapping"
          :form="form"
          :is-importing="importLoading"
          :import-progress="{ percentage: importProgress, processed: processedRows, total: totalRows, status: importProgressStatus }"
          :import-logs="importLogs"
          @back="stepActive = 2"
          @start-import="startImport"
          @cancel-import="cancelImport"
        />
      </div>
    </div>

    <!-- 侧边工具栏 -->
    <div class="sidebar-tools" v-if="file">
      <el-card shadow="hover">
        <template #header>
          <span>工具箱</span>
        </template>

        <div class="tool-buttons">
          <el-button type="primary" plain @click="validateData" block>
            <el-icon><Finished /></el-icon>
            数据验证
          </el-button>

          <el-button type="success" plain @click="exportData" block>
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>

          <el-button type="warning" plain @click="showStatistics" block>
            <el-icon><DataAnalysis /></el-icon>
            数据分析
          </el-button>

          <el-button type="info" plain @click="showDataCleaning" block>
            <el-icon><MagicStick /></el-icon>
            数据清洗
          </el-button>

          <el-button type="danger" plain @click="resetAll" block>
            <el-icon><Refresh /></el-icon>
            重置所有
          </el-button>
        </div>

        <!-- 快速操作 -->
        <div class="quick-actions">
          <h4>快速操作</h4>
          <el-space direction="vertical" fill>
            <el-button size="small" @click="fillDownValues">
              向下填充
            </el-button>
            <el-button size="small" @click="removeDuplicates">
              删除重复行
            </el-button>
            <el-button size="small" @click="convertToUpperCase">
              转换为大写
            </el-button>
            <el-button size="small" @click="convertToLowerCase">
              转换为小写
            </el-button>
          </el-space>
        </div>
      </el-card>
    </div>

    <!-- 快速指南弹窗 -->
    <el-dialog v-model="guideVisible" title="Excel导入快速指南" width="600px">
      <div class="guide-content">
        <el-steps direction="vertical" :active="guideStep">
          <el-step title="准备文件" description="下载模板并按照要求填写数据" />
          <el-step title="上传文件" description="支持拖拽上传，文件大小不超过100MB" />
          <el-step title="数据预览" description="检查数据是否正确，可进行编辑和验证" />
          <el-step title="配置导入" description="设置字段映射和导入选项" />
          <el-step title="确认导入" description="查看导入摘要并开始导入" />
        </el-steps>

        <div class="guide-tips">
          <h4>重要提示：</h4>
          <ul>
            <li>确保Excel文件的列名与模板一致</li>
            <li>日期格式请使用YYYY-MM-DD格式</li>
            <li>必填字段不能为空</li>
            <li>建议先进行数据验证再导入</li>
            <li>大文件建议分批导入</li>
          </ul>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="guideVisible = false">取消</el-button>
          <el-button type="primary" @click="guideVisible = false">
            我知道了
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 数据清洗弹窗 -->
    <el-dialog v-model="cleaningVisible" title="数据清洗" width="800px">
      <div class="cleaning-content">
        <!-- 清洗选项 -->
      </div>
    </el-dialog>

    <!-- 数据分析弹窗 -->
    <el-dialog v-model="analysisVisible" title="数据分析" width="900px">
      <div class="analysis-content">
        <!-- 分析图表 -->
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import {
  ElMessage,
  ElLoading,
  ElMessageBox,
  ElNotification
} from 'element-plus'
import {
  UploadFilled,
  Download,
  Document,
  Close,
  Upload,
  Check,
  InfoFilled,
  View,
  Delete,
  Back,
  ArrowRight,
  Edit,
  Grid,
  Warning,
  MagicStick,
  Finished,
  Refresh,
  QuestionFilled,
  DataAnalysis,
  Right
} from '@element-plus/icons-vue'
import axios from 'axios'
import * as XLSX from 'xlsx'
import ExcelSteps from '../components/excel/ExcelSteps.vue'
import ExcelUpload from '../components/excel/ExcelUpload.vue'
import ExcelPreview from '../components/excel/ExcelPreview.vue'
import ExcelConfig from '../components/excel/ExcelConfig.vue'
import ExcelConfirm from '../components/excel/ExcelConfirm.vue'

// 响应式数据
const fileInput = ref(null)
const file = ref(null)
const loading = ref(false)
const importLoading = ref(false)
const progress = ref(0)
const importProgress = ref(0)
const selectedRows = ref([])
const filePreview = ref('')
const guideVisible = ref(false)
const cleaningVisible = ref(false)
const analysisVisible = ref(false)
const recentFiles = ref(JSON.parse(localStorage.getItem('recentExcelFiles') || '[]'))

// 步骤控制
const stepActive = ref(0)
const showSteps = computed(() => previewData.rows.length > 0)
const guideStep = ref(0)

// 表单数据
const form = reactive({
  parseMode: 'universal',
  sheetIndex: -1,
  autoDetectHeader: true,
  headerRowIndex: 0,
  dataStartRow: 2,
  encoding: 'UTF-8',
  duplicateAction: 'skip',
  errorAction: 'continue'
})

// 预览相关
const previewData = reactive({
  rows: [],
  columns: []
})

const previewMode = ref('table')
const currentPage = ref(1)
const pageSize = ref(100)
const optionsActive = ref([])
const excelMeta = reactive({
  sheets: []
})

// 导入配置
const importConfig = reactive({
  mode: 'upsert',
  useTransaction: true,
  errorStrategy: 'skip',
  duplicateStrategy: 'skip',
  cleanBeforeImport: false,
  batchSize: 100
})

const fieldMapping = ref({})
const transformRules = ref({})
const defaultValues = ref({})
const targetFields = ref([
  { label: '用户名', value: 'username' },
  { label: '手机号', value: 'phone' },
  { label: '邮箱', value: 'email' },
  { label: '部门', value: 'department' },
  { label: '职位', value: 'position' }
])

// 验证相关
const validatedData = ref(null)
const columnValidations = ref({})

// 导入进度
const processedRows = ref(0)
const totalRows = ref(0)
const successCount = ref(0)
const errorCount = ref(0)
const importLogs = ref([])
const importStartTime = ref(null)

// 批量上传
const supportBatch = ref(false)
const batchFiles = ref([])

// 计算属性
const estimatedTime = computed(() => {
  const rows = previewData.rows.length
  const batchSize = importConfig.batchSize
  const batches = Math.ceil(rows / batchSize)
  const timePerBatch = 2 // 假设每批2秒
  const totalSeconds = batches * timePerBatch
  return totalSeconds > 60 ?
    `${Math.floor(totalSeconds / 60)}分${totalSeconds % 60}秒` :
    `${totalSeconds}秒`
})

const importProgressStatus = computed(() => {
  if (importProgress.value === 100) return 'success'
  if (errorCount.value > 0) return 'exception'
  return ''
})

const importSpeed = computed(() => {
  if (!importStartTime.value) return 0
  const elapsedSeconds = (Date.now() - importStartTime.value) / 1000
  return elapsedSeconds > 0 ? Math.round(processedRows.value / elapsedSeconds) : 0
})

const validationTitle = computed(() => {
  if (!validatedData.value) return ''
  const errors = validatedData.value.errors?.length || 0
  return errors === 0 ? '数据验证通过' : `发现 ${errors} 个错误`
})

const validationType = computed(() => {
  const errors = validatedData.value?.errors?.length || 0
  return errors === 0 ? 'success' : 'error'
})

const emptyCellRate = computed(() => {
  if (previewData.rows.length === 0) return 0
  let emptyCount = 0
  let totalCells = previewData.rows.length * previewData.columns.length

  previewData.rows.forEach(row => {
    previewData.columns.forEach(col => {
      if (!row[col] && row[col] !== 0 && row[col] !== false) {
        emptyCount++
      }
    })
  })

  return Math.round((emptyCount / totalCells) * 100)
})

const duplicateRowsCount = computed(() => {
  const uniqueRows = new Set()
  let duplicates = 0

  previewData.rows.forEach(row => {
    const rowString = JSON.stringify(row)
    if (uniqueRows.has(rowString)) {
      duplicates++
    } else {
      uniqueRows.add(rowString)
    }
  })

  return duplicates
})

const jsonPreview = computed(() => {
  return JSON.stringify(previewData.rows.slice(0, 10), null, 2)
})

const rawDataPreview = computed(() => {
  return previewData.rows.slice(0, 20)
})

// 方法
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event) => {
  const selectedFile = event.target.files[0]
  if (await validateFile(selectedFile)) {
    file.value = selectedFile
    addToRecentFiles(selectedFile)
    await getExcelMetaData(selectedFile)
  }
}

const handleDrop = async (event) => {
  event.preventDefault()
  const droppedFile = event.dataTransfer.files[0]
  if (await validateFile(droppedFile)) {
    file.value = droppedFile
    addToRecentFiles(droppedFile)
    await getExcelMetaData(droppedFile)
  }
}

const validateFile = async (selectedFile) => {
  if (!selectedFile) {
    ElMessage.warning('请选择文件')
    return false
  }

  const validTypes = ['.xlsx', '.xls', '.csv']
  const fileExtension = selectedFile.name.toLowerCase().slice(
    selectedFile.name.lastIndexOf('.')
  )

  if (!validTypes.includes(fileExtension)) {
    ElMessage.error('仅支持 .xlsx, .xls, .csv 格式的文件')
    return false
  }

  const maxSize = 100 * 1024 * 1024 // 100MB
  if (selectedFile.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }

  // 检查文件是否损坏
  try {
    if (selectedFile.type.includes('excel') || selectedFile.type.includes('sheet')) {
      const buffer = await selectedFile.arrayBuffer()
      XLSX.read(buffer, { type: 'array' })
    }
  } catch (error) {
    ElMessage.error('文件可能已损坏，请重新上传')
    return false
  }

  return true
}

const getExcelMetaData = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/api/excel/meta', formData)
    if (response.data.code === 200) {
      Object.assign(excelMeta, response.data.data)
    }
  } catch (error) {
    console.error('获取元数据失败:', error)
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (timestamp) => {
  return new Date(timestamp).toLocaleDateString()
}

const getFileExtension = (filename) => {
  return filename.slice(filename.lastIndexOf('.')).toUpperCase()
}

const removeFile = () => {
  file.value = null
  previewData.rows = []
  previewData.columns = []
  stepActive.value = 0
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const addToRecentFiles = (file) => {
  const recentFile = {
    name: file.name,
    size: file.size,
    type: file.type,
    lastModified: file.lastModified
  }

  // 移除重复的
  const index = recentFiles.value.findIndex(f => f.name === file.name)
  if (index > -1) {
    recentFiles.value.splice(index, 1)
  }

  // 添加到开头
  recentFiles.value.unshift(recentFile)

  // 保持最近5个文件
  if (recentFiles.value.length > 5) {
    recentFiles.value = recentFiles.value.slice(0, 5)
  }

  // 保存到localStorage
  localStorage.setItem('recentExcelFiles', JSON.stringify(recentFiles.value))
}

const loadRecentFile = async (recentFile) => {
  try {
    // 这里需要根据实际情况实现
    ElMessage.info('加载最近文件功能待实现')
  } catch (error) {
    ElMessage.error('加载文件失败')
  }
}

const clearRecent = () => {
  recentFiles.value = []
  localStorage.removeItem('recentExcelFiles')
}

const removeRecentFile = (index) => {
  recentFiles.value.splice(index, 1)
  localStorage.setItem('recentExcelFiles', JSON.stringify(recentFiles.value))
}

const parseExcel = async (selectedFile) => {
  // 如果组件传递了文件，则使用传递的文件
  const targetFile = selectedFile || file.value
  if (!targetFile) {
    ElMessage.warning('请先选择文件')
    return
  }

  loading.value = true
  progress.value = 0

  try {
    const formData = new FormData()
    formData.append('file', targetFile)
    formData.append('parseMode', form.parseMode)
    formData.append('sheetIndex', form.sheetIndex)
    formData.append('autoDetectHeader', form.autoDetectHeader)
    formData.append('headerRowIndex', form.headerRowIndex)
    formData.append('dataStartRow', form.dataStartRow)

    const response = await axios.post('/api/excel/parse/universal', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percent = Math.round(
          (progressEvent.loaded * 100) / (progressEvent.total || 100)
        )
        progress.value = Math.min(percent, 90)
      }
    })

    progress.value = 100

    if (response.data.code === 200) {
      const result = response.data.data
      previewData.rows = result.data || []
      previewData.columns = result.columns || []

      // 初始化字段映射
      previewData.columns.forEach(col => {
        if (!fieldMapping.value[col]) {
          const matchedField = targetFields.value.find(f =>
            f.label === col || col.includes(f.label)
          )
          fieldMapping.value[col] = matchedField?.value || ''
        }

        // 初始化转换规则
        if (!transformRules.value[col]) {
          transformRules.value[col] = {
            trim: true,
            uppercase: false,
            lowercase: false,
            regex: '',
            replace: ''
          }
        }

        // 初始化默认值
        if (!defaultValues.value[col]) {
          defaultValues.value[col] = ''
        }
      })

      ElMessage.success({
        message: `成功解析 ${previewData.rows.length} 条数据`,
        duration: 3000
      })

      // 自动跳转到预览步骤
      stepActive.value = 1

      // 自动执行数据验证
      await validateData()
    } else {
      throw new Error(response.data.message || '解析失败')
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message
    ElMessage.error(`解析失败: ${errorMsg}`)

    // 如果服务器解析失败，尝试客户端解析
    try {
      const clientResult = await parseExcelInBrowser(targetFile)
      previewData.rows = clientResult.rows || []
      previewData.columns = clientResult.columns || []

      if (previewData.rows.length > 0) {
        ElMessage.warning('服务器解析失败，已使用客户端解析')
        stepActive.value = 1
      }
    } catch (clientError) {
      console.error('客户端解析失败:', clientError)
    }
  } finally {
    loading.value = false
    setTimeout(() => {
      progress.value = 0
    }, 1000)
  }
}

const parseExcelInBrowser = async (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })

        // 使用配置的工作表索引
        let sheetIndex = form.sheetIndex === -1 ? 0 : form.sheetIndex
        const sheetName = workbook.SheetNames[sheetIndex]
        const sheet = workbook.Sheets[sheetName]
        const jsonData = XLSX.utils.sheet_to_json(sheet)

        // 提取表头
        const columns = []
        if (jsonData.length > 0) {
          columns.push(...Object.keys(jsonData[0]))
        }

        resolve({ rows: jsonData, columns })
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = (error) => reject(error)
    reader.readAsArrayBuffer(file)
  })
}

const validateData = async () => {
  try {
    const validationRules = {
      username: { required: true, maxLength: 50 },
      phone: { required: true, pattern: /^1[3-9]\d{9}$/ },
      email: { pattern: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/ }
    }

    const result = {
      validCount: 0,
      errors: [],
      warnings: []
    }

    previewData.rows.forEach((row, index) => {
      let isValid = true
      let rowErrors = []

      previewData.columns.forEach(col => {
        const value = row[col]
        const fieldName = fieldMapping.value[col] || col

        if (validationRules[fieldName]) {
          const rule = validationRules[fieldName]

          if (rule.required && (!value && value !== 0 && value !== false)) {
            rowErrors.push({
              field: col,
              message: `${col}不能为空`,
              rule: 'required',
              value,
              row: index + 2
            })
            isValid = false
          }

          if (rule.maxLength && value && value.toString().length > rule.maxLength) {
            rowErrors.push({
              field: col,
              message: `${col}长度不能超过${rule.maxLength}个字符`,
              rule: 'maxLength',
              value,
              row: index + 2
            })
            isValid = false
          }

          if (rule.pattern && value && !rule.pattern.test(value.toString())) {
            rowErrors.push({
              field: col,
              message: `${col}格式不正确`,
              rule: 'pattern',
              value,
              row: index + 2
            })
            isValid = false
          }
        }
      })

      if (isValid) {
        result.validCount++
      } else {
        result.errors.push(...rowErrors)
      }
    })

    validatedData.value = result

    if (result.errors.length === 0) {
      ElMessage.success('数据验证通过')
    } else {
      ElMessage.warning(`发现 ${result.errors.length} 个错误`)
    }
  } catch (error) {
    console.error('数据验证失败:', error)
    ElMessage.error('数据验证失败')
  }
}

const startImport = async () => {
  if (previewData.rows.length === 0) {
    ElMessage.warning('没有可导入的数据')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认导入 ${previewData.rows.length} 条数据吗？`,
      '确认导入',
      {
        confirmButtonText: '确认导入',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        message: `
          <div style="text-align: left;">
            <p><strong>导入摘要：</strong></p>
            <p>• 总行数：${previewData.rows.length}</p>
            <p>• 导入模式：${importConfig.mode}</p>
            <p>• 分批大小：${importConfig.batchSize}</p>
            <p>• 预计耗时：${estimatedTime.value}</p>
            ${validatedData.value?.errors?.length ?
          `<p style="color: #f56c6c;">• 发现 ${validatedData.value.errors.length} 个错误</p>` : ''}
          </div>
        `
      }
    )

    importLoading.value = true
    importProgress.value = 0
    processedRows.value = 0
    successCount.value = 0
    errorCount.value = 0
    importLogs.value = []
    importStartTime.value = Date.now()
    totalRows.value = previewData.rows.length

    // 准备导入数据
    const importData = {
      data: previewData.rows,
      config: importConfig,
      fieldMapping: fieldMapping.value,
      transformRules: transformRules.value,
      defaultValues: defaultValues.value
    }

    // 分批导入
    const batchSize = importConfig.batchSize
    const batches = Math.ceil(previewData.rows.length / batchSize)

    for (let i = 0; i < batches; i++) {
      const start = i * batchSize
      const end = start + batchSize
      const batchData = previewData.rows.slice(start, end)

      try {
        const response = await axios.post('/api/excel/import/batch', {
          data: batchData,
          batchIndex: i,
          totalBatches: batches,
          ...importData
        })

        if (response.data.code === 200) {
          const result = response.data.data
          successCount.value += result.successCount || 0
          errorCount.value += result.errorCount || 0
          processedRows.value += batchData.length

          // 添加日志
          addLog('success', `第 ${i + 1} 批导入成功，成功 ${result.successCount} 条，失败 ${result.errorCount} 条`)

          // 如果有错误，添加到错误日志
          if (result.errors?.length) {
            result.errors.forEach(error => {
              addLog('error', `第 ${start + error.row} 行: ${error.message}`)
            })
          }
        } else {
          throw new Error(response.data.message)
        }
      } catch (error) {
        const errorMsg = error.response?.data?.message || error.message
        addLog('error', `第 ${i + 1} 批导入失败: ${errorMsg}`)

        if (importConfig.errorStrategy === 'stop') {
          throw error
        }
      }

      // 更新进度
      importProgress.value = Math.round(((i + 1) / batches) * 100)

      // 如果批次很多，添加延迟避免请求过多
      if (batches > 10) {
        await new Promise(resolve => setTimeout(resolve, 100))
      }
    }

    importProgress.value = 100
    importLoading.value = false

    // 显示导入结果
    ElNotification({
      title: '导入完成',
      message: `成功导入 ${successCount.value} 条数据，失败 ${errorCount.value} 条`,
      type: errorCount.value === 0 ? 'success' : 'warning',
      duration: 5000
    })

    // 生成导入报告
    generateImportReport()

  } catch (error) {
    if (error !== 'cancel') {
      importLoading.value = false
      const errorMsg = error.response?.data?.message || error.message
      ElMessage.error(`导入失败: ${errorMsg}`)
      addLog('error', `导入失败: ${errorMsg}`)
    }
  }
}

const addLog = (type, message) => {
  importLogs.value.push({
    type,
    message,
    time: new Date().toLocaleTimeString()
  })
}

const generateImportReport = () => {
  const report = {
    fileName: file.value?.name,
    importTime: new Date().toLocaleString(),
    totalRows: previewData.rows.length,
    successCount: successCount.value,
    errorCount: errorCount.value,
    importDuration: ((Date.now() - importStartTime.value) / 1000).toFixed(2),
    errors: importLogs.value.filter(log => log.type === 'error').map(log => log.message)
  }

  // 可以导出报告
  const blob = new Blob([JSON.stringify(report, null, 2)], {
    type: 'application/json'
  })

  // 可选：自动保存报告
  // saveAs(blob, `import_report_${Date.now()}.json`)
}

/*const exportPreviewData = () => {
  const workbook = XLSX.utils.book_new()
  const worksheet = XLSX.utils.json_to_sheet(previewData.rows)
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Data')

  const wbout = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
  const blob = new Blob([wbout], { type: 'application/octet-stream' })
  saveAs(blob, `preview_${Date.now()}.xlsx`)
}*/

const showQuickGuide = () => {
  guideVisible.value = true
}

const resetAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确认要重置所有数据吗？这将清除当前的所有操作记录。',
      '确认重置',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    removeFile()
    stepActive.value = 0
    importResult.value = null
    validatedData.value = null
    importLogs.value = []

    ElMessage.success('已重置所有数据')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置失败:', error)
    }
  }
}

/*const downloadTemplate = async () => {
  try {
    const response = await axios.get('/api/excel/template', {
      responseType: 'blob'
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '用户导入模板.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}*/

// 检查字段是否为必填项
const isRequiredField = (fieldName) => {
  const validationRules = {
    username: { required: true, maxLength: 50 },
    phone: { required: true, pattern: /^1[3-9]\d{9}$/ },
    email: { pattern: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/ }
  }
  const field = fieldMapping.value[fieldName] || fieldName
  return validationRules[field]?.required || false
}

// 其他辅助方法
const formatCellValue = (value) => {
  if (value instanceof Date) {
    return value.toLocaleDateString()
  }
  if (value === null || value === undefined) {
    return ''
  }
  return value.toString()
}

const hasCellError = (row, column) => {
  if (!validatedData.value?.errors) return false
  return validatedData.value.errors.some(error =>
    error.field === column && error.row === findRowIndex(row) + 2
  )
}

const findRowIndex = (row) => {
  return previewData.rows.findIndex(r => r === row)
}

const goToRow = (rowIndex) => {
  currentPage.value = Math.floor(rowIndex / pageSize.value) + 1
  nextTick(() => {
    const table = document.querySelector('.el-table__body-wrapper')
    const row = table.querySelectorAll('tr')[rowIndex % pageSize.value]
    if (row) {
      row.scrollIntoView({ behavior: 'smooth', block: 'center' })
      row.classList.add('highlight-row')
      setTimeout(() => {
        row.classList.remove('highlight-row')
      }, 2000)
    }
  })
}

const clearLogs = () => {
  importLogs.value = []
}

const clearSelection = () => {
  selectedRows.value = []
}

const toggleSelectAll = () => {
  // 实现全选/取消全选逻辑
}

const invertSelection = () => {
  // 实现反选逻辑
}

const editRow = (index) => {
  // 实现编辑行逻辑
}

const deleteRow = (index) => {
  previewData.rows.splice(index, 1)
  ElMessage.success('删除成功')
}



// 文件下载工具函数
const downloadFile = (blob, filename) => {
  // 创建下载链接
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'

  // 添加到文档并触发点击
  document.body.appendChild(link)
  link.click()

  // 清理
  setTimeout(() => {
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }, 100)
}

// 导出预览数据
const exportPreviewData = () => {
  if (previewData.rows.length === 0) {
    ElMessage.warning('没有数据可以导出')
    return
  }

  try {
    const workbook = XLSX.utils.book_new()
    const worksheet = XLSX.utils.json_to_sheet(previewData.rows)
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Data')

    const wbout = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array'
    })

    const blob = new Blob([wbout], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    downloadFile(blob, `数据预览_${Date.now()}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + error.message)
  }
}

// 导出数据（支持多种格式）
const exportData = (format = 'excel') => {
  if (previewData.rows.length === 0) {
    ElMessage.warning('没有数据可以导出')
    return
  }

  const timestamp = new Date().getTime()

  try {
    switch (format) {
      case 'excel':
        const workbook = XLSX.utils.book_new()
        const worksheet = XLSX.utils.json_to_sheet(previewData.rows)
        XLSX.utils.book_append_sheet(workbook, worksheet, '导出数据')

        const wbout = XLSX.write(workbook, {
          bookType: 'xlsx',
          type: 'array'
        })

        const excelBlob = new Blob([wbout], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        downloadFile(excelBlob, `数据导出_${timestamp}.xlsx`)
        break

      case 'json':
        const jsonData = JSON.stringify(previewData.rows, null, 2)
        const jsonBlob = new Blob([jsonData], {
          type: 'application/json'
        })
        downloadFile(jsonBlob, `数据导出_${timestamp}.json`)
        break

      case 'csv':
        const headers = previewData.columns.join(',')
        const rows = previewData.rows.map(row =>
          previewData.columns.map(col => {
            let value = row[col]
            if (value === null || value === undefined) value = ''
            // 处理特殊字符
            if (typeof value === 'string') {
              value = value.replace(/"/g, '""')
              if (value.includes(',') || value.includes('\n') || value.includes('"')) {
                value = `"${value}"`
              }
            }
            return value
          }).join(',')
        ).join('\n')

        const csvContent = headers + '\n' + rows
        const csvBlob = new Blob(['\uFEFF' + csvContent], {
          type: 'text/csv;charset=utf-8;'
        })
        downloadFile(csvBlob, `数据导出_${timestamp}.csv`)
        break
    }

    ElMessage.success(`导出${format.toUpperCase()}格式成功`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + error.message)
  }
}

// 模板下载函数
const downloadTemplate = async () => {
  try {
    const response = await axios.get('/api/excel/template', {
      responseType: 'blob'
    })

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户导入模板.xlsx'
    link.style.display = 'none'

    document.body.appendChild(link)
    link.click()

    // 清理
    setTimeout(() => {
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }, 100)

    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('模板下载失败: ' + (error.message || '未知错误'))

    // 如果API不可用，创建本地模板
    createLocalTemplate()
  }
}

// 创建本地模板
const createLocalTemplate = () => {
  try {
    const workbook = XLSX.utils.book_new()
    const worksheet = XLSX.utils.aoa_to_sheet([
      ['用户名', '手机号', '邮箱', '部门', '职位', '入职日期'],
      ['张三', '13800138000', 'zhangsan@example.com', '技术部', '工程师', '2023-01-01'],
      ['李四', '13900139000', 'lisi@example.com', '市场部', '经理', '2023-02-01'],
      ['', '', '', '', '', '请按照此格式填写数据']
    ])

    // 设置列宽
    worksheet['!cols'] = [
      { wch: 20 }, { wch: 15 }, { wch: 25 },
      { wch: 15 }, { wch: 15 }, { wch: 12 }
    ]

    XLSX.utils.book_append_sheet(workbook, worksheet, '用户数据')

    const wbout = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array'
    })

    const blob = new Blob([wbout], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户导入模板.xlsx'
    link.style.display = 'none'

    document.body.appendChild(link)
    link.click()

    setTimeout(() => {
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }, 100)

    ElMessage.success('本地模板生成成功')
  } catch (error) {
    console.error('生成模板失败:', error)
    ElMessage.error('无法生成模板: ' + error.message)
  }
}

// 生命周期
onMounted(() => {
  // 初始化代码
})
</script>

<style scoped>
.excel-upload-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
}

.steps-container {
  grid-column: 1 / -1;
  margin-bottom: 30px;
}

.main-content {
  grid-column: 1;
}

.sidebar-tools {
  grid-column: 2;
  grid-row: 2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.upload-area {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  transition: border-color 0.3s;
  margin-bottom: 20px;
}

.upload-area:hover {
  border-color: var(--el-color-primary);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.upload-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
  margin-top: 20px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.recent-files {
  margin-top: 20px;
  border-top: 1px solid var(--el-border-color);
  padding-top: 16px;
}

.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recent-item:hover {
  background-color: var(--el-fill-color);
}

.recent-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.import-options {
  margin: 20px 0;
}

.batch-upload {
  margin-top: 20px;
}

.batch-uploader {
  width: 100%;
}

.batch-text {
  margin-top: 10px;
}

.batch-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 预览样式 */
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-mode {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.preview-actions {
  margin-left: 16px;
}

.table-preview {
  margin-bottom: 20px;
}

.column-header {
  display: flex;
  align-items: center;
  gap: 4px;
}

.validation-icon {
  cursor: help;
}

.cell-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cell-empty {
  color: var(--el-text-color-placeholder);
  font-style: italic;
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
}

.json-preview pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.data-statistics {
  margin-top: 24px;
}

.validation-panel {
  margin-top: 20px;
}

.validation-stats {
  text-align: center;
}

.error-details {
  margin-top: 16px;
}

.error-content {
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

/* 配置样式 */
.field-mapping {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mapping-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.source-field {
  font-weight: 500;
  min-width: 120px;
}

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 8px;
}

.transform-options,
.default-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.default-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.default-item span {
  min-width: 120px;
}

/* 导入确认样式 */
.import-summary {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-content {
  margin-top: 12px;
}

.import-progress {
  margin-top: 20px;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.import-logs {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
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

/* 侧边栏样式 */
.tool-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.quick-actions {
  border-top: 1px solid var(--el-border-color);
  padding-top: 16px;
}

.quick-actions h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
}

/* 弹窗样式 */
.guide-content {
  padding: 0 20px;
}

.guide-tips {
  margin-top: 20px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.guide-tips h4 {
  margin-top: 0;
  margin-bottom: 12px;
}

.guide-tips ul {
  margin: 0;
  padding-left: 20px;
}

.guide-tips li {
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
}

/* 高亮行样式 */
.highlight-row {
  animation: highlight 2s ease;
}

@keyframes highlight {
  0%, 100% {
    background-color: transparent;
  }
  50% {
    background-color: var(--el-color-primary-light-9);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .excel-upload-container {
    grid-template-columns: 1fr;
  }

  .sidebar-tools {
    grid-column: 1;
    grid-row: auto;
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .excel-upload-container {
    padding: 12px;
  }

  .upload-area {
    padding: 20px;
  }

  .preview-header,
  .config-header,
  .confirm-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .header-actions {
    flex-direction: column;
    width: 100%;
  }

  .tool-buttons .el-button {
    width: 100%;
  }

  .field-mapping,
  .default-values {
    max-height: 300px;
    overflow-y: auto;
  }
}
</style>

<style>
/* 全局表格样式 */
.el-table .highlight-row > td {
  background-color: var(--el-color-primary-light-9) !important;
}

/* 预览表格滚动条样式 */
.table-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: var(--el-fill-color);
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: var(--el-border-color-darker);
  border-radius: 3px;
}

.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color);
}

/* JSON预览代码样式 */
.json-preview code {
  color: var(--el-text-color-primary);
  line-height: 1.5;
}

/* 步骤样式调整 */
.el-step__head.is-success {
  color: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
}

.el-step__title.is-success {
  color: var(--el-color-primary) !important;
}
</style>
