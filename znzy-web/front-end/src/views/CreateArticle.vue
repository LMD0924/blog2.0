<script setup>
import { message } from "ant-design-vue";
import {ref, reactive} from "vue";
import {get, post, postJSON} from "@/net/index.js";
import { MdEditor } from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import router from "@/router/index.js";

const [messageApi, contextHolder] = message.useMessage();

// 表单数据
const formState = reactive({
  title: '',
  category: '',
  tags: '',
  content: '',
  isPublic: true,
  time: new Date().toISOString()
});

const getCurrentUserId = async () => {
  return new Promise((resolve, reject) => {
    get('api/user/current',
      {},
      (message, data) => {
        if (data) {
          formState.user = {
            avatar: data.avatar,
            account: data.account,
          };
          resolve(data.id);
        } else {
          console.error("获取信息失败");
          reject("获取信息失败");
        }
      })
  })
}

const handleSubmit = async () => {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      messageApi.error('请先登录');
      return;
    }
    if (!formState.title || !formState.content) {
      messageApi.error('请填写标题和内容');
      return;
    }

    // 处理标签和分类
    const tagsArray = formState.tags
      ? formState.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
      : [];
    const categoryArray = formState.category
      ? formState.category.split(',').map(cat => cat.trim()).filter(cat => cat)
      : [];

    // 创建文章对象
    const article = {
      title: formState.title,
      content: formState.content,
      ispublic: formState.isPublic,
      authorId: userId,
      time: new Date().toISOString()
    };

    // 创建文章信息对象
    const articleInfo = {
      authorId: userId,
      tag: tagsArray.join(','),
      classification: categoryArray.join(','),
      createTime: new Date().toISOString()
    };

    postJSON('api/article/addArticle',
      {
        article: article,
        articleInfo: articleInfo
      }, (message, data) => {
        messageApi.success(message);
        setTimeout(()=>{
          router.push('/Article');
        },1000)
      })
  } catch (error) {
    console.error("添加文章失败", error);
    messageApi.error("添加文章失败");
  }
}

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  category: [{ required: true, message: '请输入文章分类', trigger: 'blur' }],
  tags: [{ required: true, message: '请输入文章标签', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
};

// 处理图片上传
const handleUploadImage = async (file, insertImage) => {
  if (file.size > 5 * 1024 * 1024) {
    messageApi.error('图片大小不能超过5MB');
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await fetch('/api/article/upload-image', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('上传失败');
    }

    const data = await response.json();
    insertImage(data.url);
  } catch (error) {
    messageApi.error('图片上传失败，请重试');
    console.error('图片上传失败:', error);
  }
};
</script>

<template>
  <contextHolder />
  <div class="create-article-container container mx-auto px-4 py-12">
    <!-- 标题区域 -->
    <div class="header mb-10 animate-fade-in">
      <h1 class="text-3xl md:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary dark:from-primary-light dark:to-secondary-light mb-2">发布新文章</h1>
      <p class="text-gray-500 dark:text-gray-400">分享你的想法和见解</p>
    </div>

    <!-- 表单卡片 -->
    <div class="article-card bg-white dark:bg-dark-800 rounded-2xl shadow-modern overflow-hidden animate-slide-up animate-delay-100 global-card">
      <a-form
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
        class="article-form p-8"
      >
        <!-- 标题 -->
        <a-form-item label="文章标题" name="title" class="mb-6">
          <a-input
            v-model:value="formState.title"
            placeholder="请输入引人入胜的文章标题"
            class="input-title h-14 text-lg rounded-xl border-gray-200 dark:border-dark-700 focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
            :status="formState.title ? 'success' : ''"
          />
        </a-form-item>

        <!-- 分类和标签 -->
        <div class="flex flex-col sm:flex-row gap-6 mb-6">
          <a-form-item label="文章分类" name="category" class="flex-1">
            <a-input
              v-model:value="formState.category"
              placeholder="请输入文章分类（如：技术、生活、阅读等）"
              class="h-14 rounded-xl border-gray-200 dark:border-dark-700 focus:border-primary transition-all"
            />
          </a-form-item>

          <a-form-item label="文章标签" name="tags" class="flex-1">
            <a-input
              v-model:value="formState.tags"
              placeholder="请输入标签，多个标签用逗号分隔（如：Vue,JavaScript,前端）"
              class="h-14 rounded-xl border-gray-200 dark:border-dark-700 focus:border-primary transition-all"
            />
          </a-form-item>
        </div>

        <!-- 内容编辑器 -->
        <a-form-item label="文章内容" name="content" class="mb-8">
          <div class="rounded-xl border border-gray-200 dark:border-dark-700 overflow-hidden shadow-sm transition-all hover:shadow-md">
            <MdEditor
              v-model="formState.content"
              language="zh-CN"
              placeholder="请输入文章内容..."
              class="markdown-editor"
              :uploadImg="{
                accept: ['image/*'],
                maxSize: 5 * 1024 * 1024,
                handle: handleUploadImage
              }"
            />
          </div>
        </a-form-item>

        <!-- 发布设置 -->
        <a-form-item label="发布设置" class="mb-8">
          <a-space align="center">
            <a-switch
              v-model:checked="formState.isPublic"
              checked-children="公开"
              un-checked-children="私密"
              checked-color="#1677ff"
              class="transition-all"
            />
            <span class="text-gray-500 text-sm">设为私密后仅自己可见</span>
          </a-space>
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item>
          <a-space>
            <a-button
              type="primary"
              html-type="submit"
              class="publish-btn px-8 py-2.5 text-base rounded-xl bg-primary hover:bg-primary/90 border-none shadow-modern hover:shadow-hover transform hover:-translate-y-0.5 transition-all"
            >
              立即发布
            </a-button>
            <a-button
              @click="router.back()"
              class="cancel-btn px-8 py-2.5 text-base rounded-xl bg-white dark:bg-dark-700 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-dark-600 hover:bg-gray-50 dark:hover:bg-dark-600 transition-all"
            >
              取消
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
/* 表单容器样式 */
.article-card {
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
}

/* 输入框样式增强 */
.input-title {
  transition: all 0.3s ease;
}

.input-title:focus {
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2);
}

/* Markdown编辑器样式定制 */
.markdown-editor {
  min-height: 500px;
  border: none;
  background: transparent;
}

/* 暗色模式样式增强 */
.dark .article-card {
  border-color: rgba(255, 255, 255, 0.1);
  background-color: var(--dark-800);
}

.dark .input-title {
  background-color: var(--dark-700);
  color: white;
  border-color: var(--dark-600);
}

/* 确保表单标签文字在暗色模式下为白色 */
.dark .article-form :deep(.ant-form-item-label > label) {
  color: white;
  font-weight: 500;
}

/* 确保表单标签文字在暗色模式下为白色 */
.dark .article-form :deep(.ant-form-item-label > label.ant-form-item-required:not(.ant-form-item-required-mark-optional))::before {
  color: white;
}

/* 按钮动画效果 */
.publish-btn,
.cancel-btn {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.publish-btn:active,
.cancel-btn:active {
  transform: translateY(0);
}

/* 确保选择器占位文字为浅灰色 */
.dark .article-form :deep(.ant-select-selection-placeholder) {
  color: rgba(255, 255, 255, 0.65);
}

/* 确保开关文字为白色 */
.dark .article-form :deep(.ant-switch-inner) {
  color: white;
}

/* 确保标题文字为白色 */
.dark .header h1 {
  color: white;
}

.create-article-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 1.5rem;
  margin-bottom: 2rem;
}

.article-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.input-title {
  font-size: 1.25rem;
  padding: 0.75rem;
}

.markdown-editor {
  height: 500px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.publish-btn {
  background-color: #1890ff;
  padding: 0 24px;
  height: 40px;
  font-weight: 500;
}

.cancel-btn {
  padding: 0 24px;
  height: 40px;
}

/* 暗黑模式适配 */
.dark .article-form {
  background: #17222d;
  border: 1px solid #333;
}

.dark .input-title {
  background: #2a2a2a;
  color: white;
  border-color: #444;
}
</style>
