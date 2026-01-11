<script setup>
import { ref } from 'vue';
import { message } from 'ant-design-vue';

const [messageApi] = message.useMessage();

const files = ref([]);
const isDragging = ref(false);
const uploadProgress = ref({});
const isUploading = ref(false);

// 处理拖放事件
const handleDragEnter = (e) => {
  e.preventDefault();
  isDragging.value = true;
};

const handleDragLeave = (e) => {
  e.preventDefault();
  isDragging.value = false;
};

const handleDragOver = (e) => {
  e.preventDefault();
};

const handleDrop = (e) => {
  e.preventDefault();
  isDragging.value = false;
  const droppedFiles = e.dataTransfer.files;
  if (droppedFiles.length) {
    addFiles(droppedFiles);
  }
};

// 处理文件选择
const handleFileSelect = (e) => {
  const selectedFiles = e.target.files;
  if (selectedFiles.length) {
    addFiles(selectedFiles);
  }
  e.target.value = ''; // 重置input以便可以重复选择相同文件
};

// 添加文件到列表
const addFiles = (fileList) => {
  for (let i = 0; i < fileList.length; i++) {
    const file = fileList[i];
    if (!files.value.some(f => f.name === file.name && f.size === file.size)) {
      files.value.push({
        file,
        name: file.name,
        size: formatFileSize(file.size),
        type: file.type,
        status: 'pending'
      });
    }
  }
};

// 移除文件
const removeFile = (index) => {
  files.value.splice(index, 1);
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 模拟上传过程
const uploadFiles = () => {
  if (files.value.length === 0) {
    messageApi.warning('请先添加文件');
    return;
  }

  isUploading.value = true;

  files.value.forEach((fileObj, index) => {
    fileObj.status = 'uploading';
    uploadProgress.value[index] = 0;

    // 模拟上传进度
    const interval = setInterval(() => {
      uploadProgress.value[index] += Math.random() * 10;
      if (uploadProgress.value[index] >= 100) {
        clearInterval(interval);
        fileObj.status = 'done';

        // 检查是否所有文件都上传完成
        if (files.value.every(f => f.status === 'done')) {
          isUploading.value = false;
          messageApi.success('所有文件上传完成');
        }
      }
    }, 200);
  });
};
</script>

<template>
  <div class="file-upload-container">
    <div
      class="drop-zone"
      :class="{ 'dragging': isDragging }"
      @dragenter="handleDragEnter"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <div class="drop-content">
        <svg class="upload-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
        </svg>
        <p class="drop-text">拖放文件到这里或</p>
        <label class="file-select-button">
          <input
            type="file"
            multiple
            @change="handleFileSelect"
            class="file-input"
          />
          选择文件
        </label>
        <p class="drop-hint">支持单个或多个文件上传</p>
      </div>
    </div>

    <div class="file-list" v-if="files.length > 0">
      <div class="file-list-header">
        <h3>待上传文件</h3>
        <button
          class="upload-button"
          @click="uploadFiles"
          :disabled="isUploading"
        >
          {{ isUploading ? '上传中...' : '开始上传' }}
        </button>
      </div>

      <div class="file-item" v-for="(file, index) in files" :key="index">
        <div class="file-info">
          <svg class="file-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <div class="file-details">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-meta">{{ file.size }} · {{ file.type || '未知类型' }}</div>
          </div>
        </div>

        <div class="file-actions">
          <div v-if="file.status === 'uploading'" class="upload-progress">
            <div class="progress-bar" :style="{ width: uploadProgress[index] + '%' }"></div>
            <span class="progress-text">{{ Math.round(uploadProgress[index]) }}%</span>
          </div>

          <button
            v-if="file.status !== 'uploading'"
            class="remove-button"
            @click="removeFile(index)"
          >
            <svg class="remove-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
            </svg>
          </button>

          <div v-if="file.status === 'done'" class="upload-success">
            <svg class="success-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.file-upload-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.drop-zone {
  border: 2px dashed #d1d5db;
  height: 300px;
  border-radius: 6px;
  padding: 40px 20px;
  text-align: center;
  background-color: #f9fafb;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.drop-zone.dragging {
  border-color: #3b82f6;
  background-color: #ebf5ff;
}

.upload-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  color: #9ca3af;
}

.drop-text {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #374151;
}

.file-select-button {
  display: inline-block;
  padding: 8px 16px;
  background-color: #3b82f6;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  font-weight: 500;
}

.file-select-button:hover {
  background-color: #2563eb;
}

.file-input {
  display: none;
}

.drop-hint {
  font-size: 14px;
  color: #6b7280;
  margin-top: 12px;
}

.file-list {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.file-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}

.file-list-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #374151;
}

.upload-button {
  padding: 6px 12px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.upload-button:hover:not(:disabled) {
  background-color: #059669;
}

.upload-button:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background-color: white;
  transition: background-color 0.3s ease;
}

.file-item:last-child {
  border-bottom: none;
}

.file-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.file-icon {
  width: 24px;
  height: 24px;
  margin-right: 12px;
  color: #6b7280;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.file-actions {
  display: flex;
  align-items: center;
  margin-left: 16px;
}

.upload-progress {
  width: 120px;
  height: 24px;
  background-color: #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background-color: #3b82f6;
  transition: width 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.remove-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.remove-button:hover {
  background-color: #f3f4f6;
}

.remove-icon {
  width: 20px;
  height: 20px;
  color: #6b7280;
}

.upload-success {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.success-icon {
  width: 20px;
  height: 20px;
  color: #10b981;
}

/* 暗色模式适配 */
.dark .drop-zone {
  border-color: #4b5563;
  background-color: #1f2937;
}

.dark .drop-zone.dragging {
  border-color: #3b82f6;
  background-color: #1e3a8a;
}

.dark .upload-icon {
  color: #6b7280;
}

.dark .drop-text {
  color: #e5e7eb;
}

.dark .drop-hint {
  color: #9ca3af;
}

.dark .file-list {
  border-color: #4b5563;
}

.dark .file-list-header {
  background-color: #111827;
  border-color: #4b5563;
}

.dark .file-list-header h3 {
  color: #e5e7eb;
}

.dark .file-item {
  background-color: #1f2937;
  border-color: #4b5563;
}

.dark .file-name {
  color: #e5e7eb;
}

.dark .file-meta {
  color: #9ca3af;
}

.dark .upload-progress {
  background-color: #4b5563;
}

.dark .remove-button:hover {
  background-color: #374151;
}

.dark .remove-icon {
  color: #9ca3af;
}
</style>
