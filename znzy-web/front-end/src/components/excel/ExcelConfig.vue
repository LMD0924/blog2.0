<template>
  <el-card shadow="hover">
    <template #header>
      <div class="config-header">
        <div class="header-left">
          <span>导入配置</span>
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
        </div>
      </div>
    </template>

    <!-- 字段映射配置 -->
    <div class="config-section">
      <h3 class="section-title">
        <el-icon><Grid /></el-icon>
        字段映射
      </h3>
      <div class="field-mapping">
        <div v-for="(column, index) in previewData.columns" :key="index" class="mapping-item">
          <div class="source-field">
            <el-tag size="small" type="info">{{ column }}</el-tag>
          </div>
          <el-select
            v-model="fieldMapping[column]"
            placeholder="选择目标字段"
            style="width: 200px"
            @change="handleFieldMappingChange"
          >
            <el-option
              v-for="field in availableFields"
              :key="field.value"
              :label="field.label"
              :value="field.value"
            />
          </el-select>
          <el-button
            type="danger"
            size="small"
            @click="clearFieldMapping(column)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 转换规则配置 -->
    <div class="config-section">
      <h3 class="section-title">
        <el-icon><Switch /></el-icon>
        转换规则
      </h3>
      <div class="transform-options">
        <el-collapse v-model="transformActive">
          <el-collapse-item name="1" title="通用转换">
            <el-form :model="transformRules" label-width="120px">
              <el-form-item label="日期格式转换">
                <el-switch v-model="transformRules.convertDates" />
                <el-input
                  v-if="transformRules.convertDates"
                  v-model="transformRules.dateFormat"
                  placeholder="YYYY-MM-DD"
                  style="width: 160px; margin-left: 12px"
                />
              </el-form-item>
              <el-form-item label="数字格式化">
                <el-switch v-model="transformRules.formatNumbers" />
                <el-select
                  v-if="transformRules.formatNumbers"
                  v-model="transformRules.numberFormat"
                  placeholder="选择格式"
                  style="width: 160px; margin-left: 12px"
                >
                  <el-option label="保留两位小数" value="#.##" />
                  <el-option label="保留一位小数" value="#.#" />
                  <el-option label="整数" value="#" />
                </el-select>
              </el-form-item>
              <el-form-item label="文本处理">
                <el-checkbox-group v-model="transformRules.textTransforms">
                  <el-checkbox label="trim">去除首尾空格</el-checkbox>
                  <el-checkbox label="uppercase">转为大写</el-checkbox>
                  <el-checkbox label="lowercase">转为小写</el-checkbox>
                  <el-checkbox label="capitalize">首字母大写</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>
          </el-collapse-item>
          <el-collapse-item name="2" title="字段转换">
            <div v-for="(column, index) in previewData.columns" :key="index" class="field-transform-item">
              <div class="field-label">
                <el-tag size="small" type="info">{{ column }}</el-tag>
              </div>
              <el-select
                v-model="columnTransforms[column]"
                placeholder="选择转换规则"
                style="width: 180px"
                size="small"
              >
                <el-option label="无" value="none" />
                <el-option label="日期格式化" value="date" />
                <el-option label="数字格式化" value="number" />
                <el-option label="文本转换" value="text" />
              </el-select>
              <el-input
                v-if="columnTransforms[column] && columnTransforms[column] !== 'none'"
                v-model="transformParams[column]"
                placeholder="转换参数"
                style="width: 120px; margin-left: 8px"
                size="small"
              />
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- 默认值配置 -->
    <div class="config-section">
      <h3 class="section-title">
        <el-icon><Edit /></el-icon>
        默认值设置
      </h3>
      <div class="default-values">
        <div v-for="field in availableFields" :key="field.value" class="default-item">
          <span>{{ field.label }}</span>
          <el-input
            v-model="defaultValues[field.value]"
            placeholder="设置默认值"
            style="width: 200px"
            @change="handleDefaultValueChange"
          />
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Back, ArrowRight, Grid, Switch, Delete, Edit } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  previewData: {
    type: Object,
    required: true,
    default: () => ({ rows: [], columns: [] })
  },
  fieldMapping: {
    type: Object,
    required: true,
    default: () => ({})
  },
  transformRules: {
    type: Object,
    default: () => ({
      convertDates: false,
      dateFormat: 'YYYY-MM-DD',
      formatNumbers: false,
      numberFormat: '#.##',
      textTransforms: []
    })
  },
  defaultValues: {
    type: Object,
    required: true,
    default: () => ({})
  },
  availableFields: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['back', 'next', 'update:fieldMapping', 'update:transformRules', 'update:defaultValues'])

// 本地状态
const transformActive = ref(['1'])
const localFieldMapping = ref({ ...props.fieldMapping })
const localTransformRules = ref({ ...props.transformRules })
const localDefaultValues = ref({ ...props.defaultValues })
const columnTransforms = ref({})
const transformParams = ref({})

// 监听props变化，保持本地状态同步
watch(() => props.fieldMapping, (newValue) => {
  localFieldMapping.value = { ...newValue }
}, { deep: true })

watch(() => props.transformRules, (newValue) => {
  localTransformRules.value = { ...newValue }
}, { deep: true })

watch(() => props.defaultValues, (newValue) => {
  localDefaultValues.value = { ...newValue }
}, { deep: true })

// 方法
const handleFieldMappingChange = () => {
  emit('update:fieldMapping', { ...localFieldMapping.value })
}

const clearFieldMapping = (column) => {
  delete localFieldMapping.value[column]
  emit('update:fieldMapping', { ...localFieldMapping.value })
}

const handleDefaultValueChange = () => {
  emit('update:defaultValues', { ...localDefaultValues.value })
}
</script>

<style scoped>
.config-header {
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

.config-section {
  margin-top: 24px;
  padding: 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background-color: var(--el-color-primary-light-9);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

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
  background-color: #fff;
}

.source-field {
  font-weight: 500;
  min-width: 120px;
}

.transform-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-transform-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background-color: #fff;
  margin-bottom: 8px;
}

.field-label {
  min-width: 120px;
}

.default-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.default-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background-color: #fff;
}

.default-item span {
  min-width: 120px;
  font-weight: 500;
}
</style>