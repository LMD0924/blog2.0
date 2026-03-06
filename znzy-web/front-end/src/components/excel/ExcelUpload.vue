<template>
  <el-card shadow="hover">
    <template #header>
      <div class="card-header">
        <span>Excel 文件上传</span>
        <div class="header-actions">
          <el-button type="primary" link @click="$emit('download-template')">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
          <el-button type="info" link @click="$emit('show-guide')">
            <el-icon><QuestionFilled /></el-icon>
            快速指南
          </el-button>
        </div>
      </div>
    </template>

    <!-- 上传区域 -->
    <div class="upload-area" @dragover.prevent @drop="handleDrop">
      <input
        type="file"
        ref="fileInput"
        @change="handleFileChange"
        accept=".xlsx,.xls,.csv"
        style="display: none"
      />

      <div class="upload-placeholder" @click="triggerFileInput">
        <el-icon size="80" :color="file ? '#409EFF' : '#C0C4CC'">
          <UploadFilled />
        </el-icon>
        <div class="upload-text">
          <p class="upload-title">点击或将文件拖拽到这里上传</p>
          <p class="upload-tip">支持 .xlsx, .xls, .csv 格式，最大100MB</p>
          <p class="upload-tip">建议使用最新的Excel模板</p>
        </div>
      </div>

      <!-- 文件信息 -->
      <div v-if="file" class="file-info">
        <el-icon><Document /></el-icon>
        <div class="file-details">
          <span class="file-name">{{ file.name }}</span>
          <div class="file-meta">
            <el-tag size="small">{{ formatFileSize(file.size) }}</el-tag>
            <el-tag size="small" type="info">
              {{ getFileExtension(file.name) }}
            </el-tag>
            <el-tag size="small" type="success" v-if="file.lastModified">
              最后修改: {{ formatDate(file.lastModified) }}
            </el-tag>
          </div>
        </div>
        <div class="file-actions">
          <el-button type="success" size="small" @click="$emit('preview-file', file)">
            <el-icon><View /></el-icon>
            预览
          </el-button>
          <el-button type="primary" size="small" @click="$emit('parse-excel', file)">
            <el-icon><Upload /></el-icon>
            解析
          </el-button>
          <el-button type="danger" size="small" @click="removeFile">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 近期文件 -->
      <div v-if="props.recentFiles.length > 0 && !file" class="recent-files">
        <div class="recent-header">
          <span>最近上传的文件</span>
          <el-button type="text" size="small" @click="$emit('clear-recent')">
            清空记录
          </el-button>
        </div>
        <div class="recent-list">
          <div v-for="(recentFile, index) in props.recentFiles"
               :key="index"
               class="recent-item"
               @click="$emit('load-recent-file', recentFile)">
            <el-icon><Document /></el-icon>
            <span class="recent-name">{{ recentFile.name }}</span>
            <el-tag size="mini">{{ formatFileSize(recentFile.size) }}</el-tag>
            <el-button type="text" size="small" @click.stop="$emit('remove-recent-file', index)">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 导入选项 -->
    <div class="import-options">
      <el-collapse v-model="optionsActive">
        <el-collapse-item name="1" title="高级选项">
          <el-form :model="form" label-width="100px">
            <el-form-item label="解析模式">
              <el-select v-model="form.parseMode" @change="$emit('update:form', form)">
                <el-option label="通用解析" value="universal" description="支持任意格式Excel" />
                <el-option label="用户数据" value="user" description="用户导入模板" />
                <el-option label="产品数据" value="product" description="产品导入模板" />
                <el-option label="自定义映射" value="custom" description="手动配置字段映射" />
              </el-select>
            </el-form-item>

            <el-form-item label="工作表选择">
              <el-select v-model="form.sheetIndex" placeholder="自动选择" @change="$emit('update:form', form)">
                <el-option label="自动检测" value="-1" />
                <el-option
                  v-for="(sheet, index) in excelMeta.sheets || []"
                  :key="index"
                  :label="`${sheet.name} (${sheet.dataRows}行)`"
                  :value="index" />
              </el-select>
            </el-form-item>

            <el-form-item label="表头检测">
              <el-switch v-model="form.autoDetectHeader" @change="$emit('update:form', form)" />
              <el-input-number
                v-if="!form.autoDetectHeader"
                v-model="form.headerRowIndex"
                :min="0"
                :max="10"
                size="small"
                @change="$emit('update:form', form)" />
            </el-form-item>

            <el-form-item label="数据起始行">
              <el-input-number
                v-model="form.dataStartRow"
                :min="1"
                :max="1000"
                size="small"
                @change="$emit('update:form', form)" />
            </el-form-item>

            <el-form-item label="编码格式">
              <el-select v-model="form.encoding" size="small" @change="$emit('update:form', form)">
                <el-option label="UTF-8" value="UTF-8" />
                <el-option label="GBK" value="GBK" />
                <el-option label="GB2312" value="GB2312" />
              </el-select>
            </el-form-item>

            <el-form-item label="重复处理">
              <el-radio-group v-model="form.duplicateAction" @change="$emit('update:form', form)">
                <el-radio label="skip">跳过</el-radio>
                <el-radio label="overwrite">覆盖</el-radio>
                <el-radio label="update">更新</el-radio>
                <el-radio label="create">新增</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="错误处理">
              <el-radio-group v-model="form.errorAction" @change="$emit('update:form', form)">
                <el-radio label="stop">遇到错误停止</el-radio>
                <el-radio label="continue">继续处理</el-radio>
                <el-radio label="skip">跳过错误行</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 批量上传 -->
    <div v-if="supportBatch" class="batch-upload">
      <el-upload
        class="batch-uploader"
        action="#"
        :multiple="true"
        :file-list="batchFiles"
        :before-upload="beforeBatchUpload"
        :on-remove="handleBatchRemove"
        :on-exceed="handleBatchExceed"
        :limit="10"
        accept=".xlsx,.xls,.csv"
        drag
      >
        <el-icon size="60" color="#C0C4CC"><UploadFilled /></el-icon>
        <div class="batch-text">
          <p>批量上传多个Excel文件</p>
          <p class="batch-tip">最多支持10个文件同时上传</p>
        </div>
      </el-upload>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { UploadFilled, Download, QuestionFilled, Document, Delete, View, Upload, Close } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  file: {
    type: Object,
    default: null
  },
  form: {
    type: Object,
    required: true
  },
  excelMeta: {
    type: Object,
    default: () => ({})
  },
  supportBatch: {
    type: Boolean,
    default: true
  },
  recentFiles: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['download-template', 'show-guide', 'preview-file', 'parse-excel', 'update:form', 'update:file', 'remove-file', 'load-recent-file', 'remove-recent-file', 'clear-recent'])

// 本地状态
const fileInput = ref(null)
const file = ref(props.file)
const form = ref({ ...props.form })
const recentFiles = ref([...props.recentFiles])
const optionsActive = ref(['1'])
const batchFiles = ref([])

// 生命周期钩子
watch(() => props.form, (newForm) => {
  // 确保本地表单与父组件保持同步
  form.value = { ...newForm }
}, { deep: true })

// 监听文件变化
watch(() => props.file, (newFile) => {
  file.value = newFile
})

// 监听recentFiles变化
watch(() => props.recentFiles, (newRecentFiles) => {
  recentFiles.value = [...newRecentFiles]
}, { deep: true })

// 方法
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = (event) => {
  const selectedFile = event.target.files?.[0]
  if (selectedFile) {
    file.value = selectedFile
    saveToRecentFiles(selectedFile)
    emit('update:file', selectedFile)
    emit('update:form', { ...props.form, fileName: selectedFile.name })
  }
}

const handleDrop = (event) => {
  event.preventDefault()
  const droppedFile = event.dataTransfer.files?.[0]
  if (droppedFile) {
    file.value = droppedFile
    saveToRecentFiles(droppedFile)
    emit('update:file', droppedFile)
    emit('update:form', { ...props.form, fileName: droppedFile.name })
  }
}

const removeFile = () => {
  file.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  emit('update:file', null)
  emit('update:form', { ...props.form, fileName: '' })
  emit('remove-file')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileExtension = (fileName) => {
  return fileName.slice(((fileName.lastIndexOf('.') - 1) >>> 0) + 2)
}

const formatDate = (timestamp) => {
  return new Date(timestamp).toLocaleString()
}

const saveToRecentFiles = (file) => {
  // 保存到localStorage
  const recent = JSON.parse(localStorage.getItem('recentExcelFiles') || '[]')
  // 移除已存在的相同文件
  const filtered = recent.filter(item => item.name !== file.name)
  // 添加到开头
  filtered.unshift({
    name: file.name,
    size: file.size,
    lastModified: file.lastModified
  })
  // 限制最多保存10个
  const limited = filtered.slice(0, 10)
  localStorage.setItem('recentExcelFiles', JSON.stringify(limited))
  
  // 更新本地状态
  recentFiles.value = [...limited]
  // 通知父组件
  emit('update:recentFiles', [...limited])
}

const loadRecentFiles = () => {
  // 从localStorage加载
  recentFiles.value = JSON.parse(localStorage.getItem('recentExcelFiles') || '[]')
}

const loadRecentFile = (recentFile) => {
  // 这里需要处理如何加载最近文件，可能需要额外的逻辑
  console.log('加载最近文件:', recentFile)
}

const clearRecent = () => {
  recentFiles.value = []
  localStorage.removeItem('recentExcelFiles')
}

const removeRecentFile = (index) => {
  recentFiles.value.splice(index, 1)
  localStorage.setItem('recentExcelFiles', JSON.stringify(recentFiles.value))
}

// 批量上传方法
const beforeBatchUpload = (file) => {
  console.log('批量上传文件:', file)
  // 这里可以添加文件验证逻辑
  return false // 阻止自动上传
}

const handleBatchRemove = (file, fileList) => {
  console.log('移除批量上传文件:', file, fileList)
}

const handleBatchExceed = (files, fileList) => {
  console.log('批量上传文件超过限制:', files, fileList)
}

// 初始化最近文件
loadRecentFiles()
</script>

<style scoped>
/* 现代化卡片样式 */
:deep(.el-card) {
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 16px;
}

.card-header span {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 上传区域样式 */
.upload-area {
  padding: 60px 40px;
  margin: 0 24px;
  border: 2px dashed var(--el-border-color);
  border-radius: 12px;
  text-align: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.02) 0%, rgba(103, 194, 58, 0.02) 100%);
}

.upload-area:hover {
  border-color: #409EFF;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(103, 194, 58, 0.05) 100%);
  transform: translateY(-2px);
}

.upload-placeholder {
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-placeholder:hover .el-icon {
  transform: scale(1.1) rotate(5deg);
}

.upload-text {
  margin-top: 24px;
}

.upload-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.upload-tip {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 6px 0;
}

/* 文件信息样式 */
.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  margin: 16px 24px 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
  border: 1px solid var(--el-border-color-light);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.file-info:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.file-details {
  flex: 1;
  margin-left: 20px;
}

.file-name {
  display: block;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.file-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

:deep(.file-meta .el-tag) {
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 12px;
  border: none;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.2) 100%);
  color: #409EFF;
}

:deep(.file-meta .el-tag:nth-child(2)) {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0.2) 100%);
  color: #67C23A;
}

:deep(.file-meta .el-tag:nth-child(3)) {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(230, 162, 60, 0.2) 100%);
  color: #E6A23C;
}

.file-actions {
  display: flex;
  gap: 12px;
}

:deep(.file-actions .el-button) {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.file-actions .el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 近期文件样式 */
.recent-files {
  margin: 24px;
}

.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.recent-header span {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.recent-list {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
}

.recent-item:last-child {
  margin-bottom: 0;
}

.recent-item:hover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(103, 194, 58, 0.08) 100%);
  transform: translateX(4px);
}

.recent-name {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

:deep(.recent-item .el-tag) {
  border-radius: 16px;
  padding: 2px 10px;
  font-size: 11px;
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.1) 0%, rgba(144, 147, 153, 0.2) 100%);
  color: #909399;
}

/* 导入选项样式 */
.import-options {
  margin: 24px;
}

:deep(.import-options .el-collapse) {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  overflow: hidden;
}

:deep(.import-options .el-collapse-item__header) {
  padding: 16px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid var(--el-border-color-light);
  font-weight: 600;
  color: var(--el-text-color-primary);
}

:deep(.import-options .el-collapse-item__content) {
  padding: 24px;
}

:deep(.import-options .el-form-item) {
  margin-bottom: 24px;
}

:deep(.import-options .el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

:deep(.import-options .el-select),
:deep(.import-options .el-input-number),
:deep(.import-options .el-radio-group) {
  width: 100%;
}

:deep(.import-options .el-select .el-input__wrapper) {
  border-radius: 8px;
}

/* 批量上传样式 */
.batch-upload {
  margin: 0 24px 24px;
}

:deep(.batch-uploader) {
  border-radius: 12px;
  border: 2px dashed var(--el-border-color);
  padding: 40px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.02) 0%, rgba(237, 88, 100, 0.02) 100%);
}

:deep(.batch-uploader:hover) {
  border-color: #E6A23C;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.05) 0%, rgba(237, 88, 100, 0.05) 100%);
}

.batch-text {
  margin-top: 20px;
}

.batch-text p:first-child {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.batch-tip {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* 按钮样式优化 */
:deep(.card-header .el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.card-header .el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.file-info,
.recent-files,
.import-options,
.batch-upload {
  animation: fadeInUp 0.5s ease-out;
}
</style>