import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 编译代码请求
 * @param {Object} request - 编译请求对象
 * @param {string} request.language - 编程语言
 * @param {string} request.code - 代码内容
 * @param {string} [request.input] - 输入数据（可选）
 * @returns {Promise<Object>} 编译结果
 */
export async function compileCode(request) {
  try {
    console.log('📤 发送编译请求:', {
      url: `${API_BASE_URL}/compile`,
      language: request.language,
      codeLength: request.code.length,
      timestamp: new Date().toISOString()
    })

    // 检查必填参数
    if (!request || !request.language || !request.code) {
      return {
        code: 400,
        message: '缺少必要参数：language 或 code',
        status: 'error'
      }
    }

    // 发送请求到后端
    const response = await axios.post(`${API_BASE_URL}/compile`, request, {
      timeout: 30000, // 30秒超时
      headers: {
        'Content-Type': 'application/json'
      }
    })

    console.log('📥 编译响应:', {
      status: response.status,
      data: response.data,
      timestamp: new Date().toISOString()
    })

    // 统一返回格式 - 修复：正确处理RestBean响应
    if (response.data.code === 200 && response.data.data) {
      // 从data字段中提取编译结果
      const compileData = response.data.data;
      return {
        code: 200,
        message: response.data.message || '编译成功',
        data: compileData, // 保留原始data供调试
        status: compileData.status || 'success', // 使用实际的编译状态
        output: compileData.output, // 直接暴露output字段
        error: compileData.error, // 直接暴露error字段
        executionTime: compileData.executionTime // 直接暴露执行时间
      }
    } else {
      // 处理错误响应
      const errorData = response.data.data || {};
      return {
        code: response.data.code || 500,
        message: response.data.message || '编译失败',
        data: errorData,
        status: 'error',
        error: errorData.error || response.data.message
      }
    }

  } catch (error) {
    console.error('❌ 编译请求失败:', error)

    // 根据错误类型返回不同的错误信息
    let errorMessage = '请求失败'
    let errorCode = 500

    if (error.response) {
      // 服务器返回错误状态码
      console.error('服务器错误响应:', {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers
      })

      errorCode = error.response.status
      errorMessage = error.response.data?.message ||
        error.response.data?.error ||
        `服务器错误: ${error.response.status}`
    } else if (error.request) {
      // 请求已发送但无响应
      errorMessage = '服务器无响应，请检查网络连接'
      errorCode = 503
    } else if (error.code === 'ECONNABORTED') {
      // 请求超时
      errorMessage = '请求超时，请稍后重试'
      errorCode = 504
    } else {
      // 其他错误
      errorMessage = error.message || '未知错误'
    }

    return {
      code: errorCode,
      message: errorMessage,
      status: 'error',
      details: error.response?.data || null
    }
  }
}

/**
 * 获取支持的编程语言列表
 * @returns {Promise<Array<string>>} 支持的语言列表
 */
export async function getSupportedLanguages() {
  try {
    console.log('📤 获取语言列表...')
    const response = await axios.get(`${API_BASE_URL}/languages`, {
      timeout: 5000
    })

    console.log('📥 语言列表响应:', response.data)

    if (response.data.code === 200) {
      return response.data.data?.languages || []
    }
    return []

  } catch (error) {
    console.error('获取语言列表失败:', error)
    return ['javascript', 'python', 'java', 'c', 'cpp'] // 默认列表
  }
}

/**
 * 获取代码模板/示例
 * @param {string} language - 编程语言
 * @returns {Promise<{hello: string, fibonacci: string}>} 代码示例
 */
export async function getCodeExamples(language) {
  try {
    console.log(`📤 获取 ${language} 示例代码...`)

    const response = await axios.get(`${API_BASE_URL}/examples/${language}`, {
      timeout: 5000
    })

    console.log(`📥 ${language} 示例响应:`, response.data)

    if (response.data.code === 200) {
      return response.data.data
    }
    return {}

  } catch (error) {
    console.error(`获取 ${language} 示例失败:`, error)
    return getDefaultExamples(language)
  }
}

/**
 * 获取代码模板（兼容旧接口）
 * @param {string} language - 编程语言
 * @returns {Promise<string>} 代码模板
 */
export async function getCodeTemplate(language) {
  const examples = await getCodeExamples(language)
  return examples.hello || ''
}

/**
 * 默认示例代码
 */
function getDefaultExamples(language) {
  const defaultExamples = {
    javascript: {
      hello: 'console.log("Hello, World!");',
      fibonacci: `function fibonacci(n) {
    if (n <= 1) return n;
    return fibonacci(n-1) + fibonacci(n-2);
}

const n = 10;
console.log(\`Fibonacci(\${n}): \${fibonacci(n)}\`);`
    },
    python: {
      hello: 'print("Hello, World!")',
      fibonacci: `def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

n = 10
print(f"Fibonacci({n}): {fibonacci(n)}")`
    },
    java: {
      hello: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
      fibonacci: `public class Main {
    public static void main(String[] args) {
        int n = 10;
        System.out.println("Fibonacci(" + n + "): " + fibonacci(n));
    }

    static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n-1) + fibonacci(n-2);
    }
}`
    },
    c: {
      hello: `#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    return 0;
}`,
      fibonacci: `#include <stdio.h>

int fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n-1) + fibonacci(n-2);
}

int main() {
    int n = 10;
    printf("Fibonacci(%d): %d\\n", n, fibonacci(n));
    return 0;
}`
    },
    cpp: {
      hello: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, World!" << endl;
    return 0;
}`,
      fibonacci: `#include <iostream>
using namespace std;

int fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n-1) + fibonacci(n-2);
}

int main() {
    int n = 10;
    cout << "Fibonacci(" << n << "): " << fibonacci(n) << endl;
    return 0;
}`
    }
  }

  return defaultExamples[language] || {}
}

/**
 * 测试API连接
 * @returns {Promise<{connected: boolean, data: any}>} 连接状态
 */
export async function testApiConnection() {
  try {
    const endpoints = [
      { name: '健康检查', url: `${API_BASE_URL}/health` },
      { name: '测试接口', url: `${API_BASE_URL}/test` },
      { name: '语言列表', url: `${API_BASE_URL}/languages` }
    ]

    const results = []

    for (const endpoint of endpoints) {
      try {
        const response = await axios.get(endpoint.url, { timeout: 3000 })
        results.push({
          name: endpoint.name,
          success: true,
          status: response.status,
          data: response.data
        })
      } catch (error) {
        results.push({
          name: endpoint.name,
          success: false,
          error: error.message,
          status: error.response?.status
        })
      }
    }

    const allSuccess = results.every(r => r.success)

    return {
      connected: allSuccess,
      results,
      apiBaseUrl: API_BASE_URL,
      timestamp: new Date().toISOString()
    }

  } catch (error) {
    console.error('API连接测试失败:', error)
    return {
      connected: false,
      error: error.message,
      timestamp: new Date().toISOString()
    }
  }
}

/**
 * 保存代码到本地
 * @param {string} language - 编程语言
 * @param {string} code - 代码内容
 * @returns {Promise<boolean>} 是否保存成功
 */
export async function saveCodeLocally(language, code) {
  return new Promise((resolve) => {
    try {
      const filename = `code_${language}_${Date.now()}.${getFileExtension(language)}`
      const blob = new Blob([code], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      console.log('💾 代码保存成功:', filename)
      resolve(true)
    } catch (error) {
      console.error('保存代码失败:', error)
      resolve(false)
    }
  })
}

/**
 * 从本地加载代码
 * @returns {Promise<{language: string, code: string}>} 加载的代码
 */
export async function loadCodeFromLocal() {
  return new Promise((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = '.js,.py,.java,.c,.cpp,.txt'

    input.onchange = (event) => {
      const file = event.target.files[0]
      if (!file) {
        resolve(null)
        return
      }

      const reader = new FileReader()
      reader.onload = (e) => {
        const code = e.target.result
        const extension = file.name.split('.').pop().toLowerCase()
        const language = getLanguageFromExtension(extension)
        console.log('📂 从文件加载代码:', { language, filename: file.name })
        resolve({ language, code })
      }
      reader.onerror = () => resolve(null)
      reader.readAsText(file)
    }

    input.click()
  })
}

/**
 * 获取文件扩展名
 * @param {string} language - 编程语言
 * @returns {string} 文件扩展名
 */
function getFileExtension(language) {
  const extensions = {
    javascript: 'js',
    python: 'py',
    java: 'java',
    c: 'c',
    cpp: 'cpp'
  }
  return extensions[language] || 'txt'
}

/**
 * 根据扩展名获取语言
 * @param {string} extension - 文件扩展名
 * @returns {string} 编程语言
 */
function getLanguageFromExtension(extension) {
  const languageMap = {
    js: 'javascript',
    py: 'python',
    java: 'java',
    c: 'c',
    cpp: 'cpp',
    txt: 'javascript'
  }
  return languageMap[extension] || 'javascript'
}

/**
 * 健康检查
 * @returns {Promise<boolean>} 是否连接成功
 */
export async function checkApiConnection() {
  try {
    console.log('🩺 进行健康检查...')
    const response = await axios.get(`${API_BASE_URL}/health`, {
      timeout: 3000
    })

    const isHealthy = response.status === 200 &&
      response.data?.code === 200

    console.log('✅ 健康检查结果:', {
      status: response.status,
      healthy: isHealthy,
      data: response.data
    })

    return isHealthy

  } catch (error) {
    console.warn('❌ API连接检查失败:', error.message)
    return false
  }
}

/**
 * 获取编译器信息
 * @returns {Promise<Object>} 编译器信息
 */
export async function getCompilerInfo() {
  try {
    const response = await axios.get(`${API_BASE_URL}/info`, {
      timeout: 5000
    })

    if (response.data.code === 200) {
      return response.data.data
    }
    return null

  } catch (error) {
    console.error('获取编译器信息失败:', error)
    return null
  }
}

// 导出所有函数
export default {
  compileCode,
  getSupportedLanguages,
  getCodeTemplate,
  getCodeExamples,
  saveCodeLocally,
  loadCodeFromLocal,
  checkApiConnection,
  testApiConnection,
  getCompilerInfo
}
