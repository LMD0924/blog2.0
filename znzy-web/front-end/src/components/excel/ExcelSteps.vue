<template>
  <div class="steps-container">
    <el-steps :active="stepActive" finish-status="success" align-center>
      <el-step title="选择文件" description="上传Excel文件" />
      <el-step title="预览数据" description="查看解析结果" />
      <el-step title="配置导入" description="设置导入选项" />
      <el-step title="确认导入" description="完成数据导入" />
    </el-steps>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 定义props
const props = defineProps({
  stepActive: {
    type: Number,
    required: true,
    default: 0
  }
})
</script>

<style scoped>
.steps-container {
  margin-bottom: 24px;
  padding: 0 8px;
}

/* 步骤样式调整 */
:deep(.el-step__head.is-success) {
  color: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
}

:deep(.el-step__title.is-success) {
  color: var(--el-color-primary) !important;
}

:deep(.el-step__head.is-process) {
  border-color: var(--el-color-primary) !important;
}

:deep(.el-step__title.is-process) {
  color: var(--el-color-primary) !important;
}
</style>