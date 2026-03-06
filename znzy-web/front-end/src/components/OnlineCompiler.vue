<template>
  <div class="min-h-screen bg-gray-900 text-gray-100 p-4">
    <!-- 顶部控制栏 -->
    <ControlBar
      :language="language"
      :isLoading="isLoading"
      @change-language="changeLanguage"
      @run-code="runCode"
      @clear-output="clearOutput"
    />

    <!-- 状态栏 -->
    <StatusBar
      :status="status"
      :executionTime="executionTime"
    />

    <!-- 主编辑区 -->
    <div class="flex flex-col lg:flex-row gap-4 mt-4">
      <!-- 代码编辑区 -->
      <div class="lg:w-2/3">
        <div class="bg-gray-800 rounded-xl overflow-hidden border border-gray-700">
          <div class="px-4 py-3 border-b border-gray-700 bg-gray-800/50 flex justify-between items-center">
            <span class="font-mono text-sm text-gray-400">
              main.{{ getFileExtension(language) }}
            </span>
            <div class="flex items-center space-x-2">
              <button
                @click="formatCode"
                class="px-3 py-1 text-sm bg-gray-700 hover:bg-gray-600 rounded transition"
                title="格式化代码"
              >
                <span class="flex items-center space-x-1">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
                  </svg>
                  <span>格式化</span>
                </span>
              </button>
              <button
                @click="saveCode"
                class="px-3 py-1 text-sm bg-blue-600 hover:bg-blue-700 rounded transition"
                title="保存代码"
              >
                <span class="flex items-center space-x-1">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                  </svg>
                  <span>保存</span>
                </span>
              </button>
            </div>
          </div>
          <CodeEditor
            v-model="code"
            :language="language"
            :theme="theme"
            class="h-[500px]"
            ref="codeEditorRef"
          />
        </div>
      </div>

      <!-- 输入输出区 -->
      <div class="lg:w-1/3 flex flex-col gap-4">
        <!-- 输入区 -->
        <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
          <div class="px-4 py-3 border-b border-gray-700 bg-gray-800/50">
            <span class="text-sm font-medium text-gray-300">输入</span>
          </div>
          <textarea
            v-model="input"
            placeholder="输入测试数据（可选）"
            class="w-full h-40 bg-gray-800 text-gray-100 p-4 font-mono text-sm resize-none focus:outline-none"
            spellcheck="false"
          ></textarea>
        </div>

        <!-- 输出区 -->
        <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden flex-1">
          <div class="px-4 py-3 border-b border-gray-700 bg-gray-800/50 flex justify-between items-center">
            <span class="text-sm font-medium text-gray-300">输出</span>
            <button
              @click="clearOutput"
              class="px-3 py-1 text-sm bg-gray-700 hover:bg-gray-600 rounded transition"
            >
              清空
            </button>
          </div>
          <OutputPanel
            :response="{ output: output, status: status }"
            :isLoading="isLoading"
            class="h-[calc(500px-12rem)]"
        />
        </div>
      </div>
    </div>

    <!-- 快捷操作栏 -->
    <div class="mt-6 flex flex-wrap gap-3 justify-center">
      <button
        v-for="template in quickTemplates"
        :key="template.name"
        @click="loadQuickTemplate(template)"
        class="px-4 py-2 bg-gray-800 hover:bg-gray-700 border border-gray-700 rounded-lg transition flex items-center space-x-2"
      >
        <span>{{ template.name }}</span>
        <span class="text-xs px-1.5 py-0.5 bg-gray-700 rounded">{{ template.lang }}</span>
      </button>
    </div>

    <!-- 信息提示 -->
    <div v-if="showInfo" class="mt-4 p-4 bg-blue-900/20 border border-blue-700/30 rounded-lg">
      <div class="flex items-start space-x-3">
        <svg class="w-5 h-5 text-blue-400 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
        </svg>
        <div class="text-sm text-blue-300">
          提示：支持 JavaScript、Python、Java、C、C++ 等多种语言在线编译
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import CodeEditor from './CodeEditor.vue'
import OutputPanel from './OutputPanel.vue'
import ControlBar from './ControlBar.vue'
import StatusBar from './StatusBar.vue'
import { compileCode } from '../api/compiler'

// 响应式数据
const language = ref('javascript')
const code = ref('')
const input = ref('')
const output = ref('')
const isLoading = ref(false)
const theme = ref('vs-dark')
const status = ref('ready')
const executionTime = ref(0)
const showInfo = ref(true)
const codeEditorRef = ref()

// 快捷代码模板
const quickTemplates = [
  {
    name: 'Hello World',
    lang: 'javascript',
    code: 'console.log("Hello, World!")'
  },
  {
    name: '斐波那契数列',
    lang: 'python',
    code: `def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

print("Fibonacci(10):", fibonacci(10))`
  },
  {
    name: '冒泡排序',
    lang: 'java',
    code: `public class Main {
    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        bubbleSort(arr);
        System.out.println("Sorted array:");
        for (int num : arr) {
            System.out.print(num + " ");
        }
    }

    static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (arr[j] > arr[j+1]) {
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
    }
}`
  },
  {
    name: '计算阶乘',
    lang: 'c',
    code: `#include <stdio.h>

int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
}

int main() {
    int n = 10;
    printf("Factorial of %d is %d\\n", n, factorial(n));
    return 0;
}`
  }
]

// 默认代码模板
const defaultTemplates = {
  javascript: 'console.log("Hello, World!")',
  python: 'print("Hello, World!")',
  java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
  c: `#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    return 0;
}`,
  cpp: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, World!" << endl;
    return 0;
}`
}

// 生命周期
onMounted(() => {
  updateCodeTemplate()
})

// 切换语言时更新代码
const changeLanguage = (newLang) => {
  language.value = newLang
  updateCodeTemplate()
}

// 更新代码模板
const updateCodeTemplate = () => {
  code.value = defaultTemplates[language.value] || ''
}

// 运行代码
const runCode = async () => {
  if (!code.value.trim()) {
    alert('请输入代码')
    return
  }

  isLoading.value = true
  status.value = 'running'
  output.value = '🚀 正在编译运行...\n'
  executionTime.value = 0

  const startTime = Date.now()

  try {
    const result = await compileCode({
      language: language.value,
      code: code.value,
      input: input.value
    })

    executionTime.value = Date.now() - startTime

    // 从result.data中提取实际的编译结果
    const compileResult = result.data || result;

    if (compileResult.status === 'success') {
      status.value = 'success'
      output.value = compileResult.output || '✅ 程序执行成功，无输出'
    } else {
      status.value = 'error'
      output.value = `❌ 错误:\n${compileResult.error || compileResult.message || result.message}`
    }
  } catch (error) {
    status.value = 'error'
    executionTime.value = Date.now() - startTime
    output.value = `❌ 请求失败: ${error.message}`
  } finally {
    isLoading.value = false
  }
}

// 清空输出
const clearOutput = () => {
  output.value = ''
  status.value = 'ready'
}

// 格式化代码
const formatCode = () => {
  if (codeEditorRef.value && codeEditorRef.value.formatCode) {
    codeEditorRef.value.formatCode()
  }
}

// 保存代码
const saveCode = () => {
  const blob = new Blob([code.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `code_${language.value}_${Date.now()}.${getFileExtension(language.value)}`
  a.click()
  URL.revokeObjectURL(url)
}

// 加载快捷模板
const loadQuickTemplate = (template) => {
  language.value = template.lang
  code.value = template.code
}

// 获取文件扩展名
const getFileExtension = (lang) => {
  const extensions = {
    javascript: 'js',
    python: 'py',
    java: 'java',
    c: 'c',
    cpp: 'cpp'
  }
  return extensions[lang] || 'txt'
}
</script>

<style scoped>
/* 自定义滚动条样式 */
textarea::-webkit-scrollbar {
  width: 8px;
}

textarea::-webkit-scrollbar-track {
  background: #374151;
}

textarea::-webkit-scrollbar-thumb {
  background: #4B5563;
  border-radius: 4px;
}

textarea::-webkit-scrollbar-thumb:hover {
  background: #6B7280;
}
</style>
