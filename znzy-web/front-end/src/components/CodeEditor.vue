<template>
  <div ref="editorContainer" class="w-full h-full"></div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue'

export default {
  name: 'CodeEditor',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    language: {
      type: String,
      default: 'javascript'
    },
    theme: {
      type: String,
      default: 'vs-dark'
    },
    height: {
      type: String,
      default: '500px'
    }
  },
  emits: ['update:modelValue'],

  setup(props, { emit }) {
    const editorContainer = ref(null)
    let editor = null

    // 动态加载 Monaco Editor
    const loadMonacoEditor = () => {
      return new Promise((resolve) => {
        // 检查是否已加载
        if (window.monaco) {
          resolve(window.monaco)
          return
        }

        // 创建 script 标签加载 Monaco Editor
        const script = document.createElement('script')
        script.src = 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs/loader.min.js'
        script.onload = () => {
          require.config({
            paths: {
              vs: 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs'
            }
          })

          require(['vs/editor/editor.main'], () => {
            resolve(window.monaco)
          })
        }
        document.head.appendChild(script)
      })
    }

    // 初始化编辑器
    const initEditor = async () => {
      if (!editorContainer.value) return

      const monaco = await loadMonacoEditor()

      // 创建编辑器实例
      editor = monaco.editor.create(editorContainer.value, {
        value: props.modelValue,
        language: props.language,
        theme: props.theme,
        fontSize: 14,
        lineNumbers: 'on',
        minimap: { enabled: true },
        scrollBeyondLastLine: false,
        automaticLayout: true,
        formatOnPaste: true,
        formatOnType: true,
        wordWrap: 'on',
        tabSize: 2,
        insertSpaces: true,
        folding: true,
        renderLineHighlight: 'all'
      })

      // 监听内容变化
      editor.onDidChangeModelContent(() => {
        const value = editor.getValue()
        emit('update:modelValue', value)
      })

      // 调整编辑器容器高度
      editorContainer.value.style.height = props.height
    }

    // 格式化代码
    const formatCode = () => {
      if (editor) {
        editor.getAction('editor.action.formatDocument').run()
      }
    }

    // 监听语言变化
    watch(() => props.language, (newLang) => {
      if (editor) {
        const model = editor.getModel()
        if (model) {
          window.monaco.editor.setModelLanguage(model, newLang)
        }
      }
    })

    // 监听主题变化
    watch(() => props.theme, (newTheme) => {
      if (editor) {
        window.monaco.editor.setTheme(newTheme)
      }
    })

    // 监听代码变化
    watch(() => props.modelValue, (newValue) => {
      if (editor && editor.getValue() !== newValue) {
        editor.setValue(newValue)
      }
    })

    onMounted(() => {
      initEditor()
    })

    onUnmounted(() => {
      if (editor) {
        editor.dispose()
      }
    })

    return {
      editorContainer,
      formatCode
    }
  }
}
</script>

<style scoped>
.editor-container {
  width: 100%;
}
</style>
