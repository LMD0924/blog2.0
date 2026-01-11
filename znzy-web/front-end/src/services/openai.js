import axios from 'axios';

// OpenAI API 配置
const OPENAI_API_KEY = 'sk-HhDe9LTWQRl8XevaB6IO4Qmr4oWdLhlgx9ATDZbtvhF1Ys6i';
const OPENAI_API_BASE_URL = 'https://yinli.one/';

// 创建专门的axios实例用于OpenAI API调用
const openaiClient = axios.create({
  baseURL: OPENAI_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${OPENAI_API_KEY}`
  }
});

// OpenAI服务类
class OpenAIService {
  /**
   * 生成聊天响应
   * @param {Array} messages - 消息历史数组
   * @param {Object} options - 配置选项
   * @returns {Promise} 响应结果
   */
  async chat(messages, options = {}) {
    try {
      const response = await openaiClient.post('/chat/completions', {
        model: options.model || 'gpt-3.5-turbo',
        messages,
        temperature: options.temperature || 0.7,
        max_tokens: options.max_tokens || 800,
        ...options
      });
      return response.data;
    } catch (error) {
      console.error('OpenAI Chat API Error:', error);
      throw error;
    }
  }

  /**
   * 生成文本
   * @param {string} prompt - 提示文本
   * @param {Object} options - 配置选项
   * @returns {Promise} 响应结果
   */
  async completions(prompt, options = {}) {
    try {
      const response = await openaiClient.post('/completions', {
        model: options.model || 'text-davinci-003',
        prompt,
        temperature: options.temperature || 0.7,
        max_tokens: options.max_tokens || 1000,
        ...options
      });
      return response.data;
    } catch (error) {
      console.error('OpenAI Completions API Error:', error);
      throw error;
    }
  }

  /**
   * 生成图像
   * @param {string} prompt - 图像描述
   * @param {Object} options - 配置选项
   * @returns {Promise} 响应结果
   */
  async generateImage(prompt, options = {}) {
    try {
      const response = await openaiClient.post('/images/generations', {
        prompt,
        n: options.n || 1,
        size: options.size || '1024x1024',
        ...options
      });
      return response.data;
    } catch (error) {
      console.error('OpenAI Image Generation API Error:', error);
      throw error;
    }
  }

  /**
   * 嵌入文本
   * @param {string|Array} input - 输入文本或文本数组
   * @param {Object} options - 配置选项
   * @returns {Promise} 响应结果
   */
  async embeddings(input, options = {}) {
    try {
      const response = await openaiClient.post('/embeddings', {
        model: options.model || 'text-embedding-ada-002',
        input,
        ...options
      });
      return response.data;
    } catch (error) {
      console.error('OpenAI Embeddings API Error:', error);
      throw error;
    }
  }

  /**
   * 设置API密钥
   * @param {string} apiKey - OpenAI API密钥
   */
  setApiKey(apiKey) {
    openaiClient.defaults.headers.common['Authorization'] = `Bearer ${apiKey}`;
  }

  /**
   * 设置API基础URL
   * @param {string} baseUrl - API基础URL
   */
  setBaseUrl(baseUrl) {
    openaiClient.defaults.baseURL = baseUrl;
  }
}

// 导出单例实例
export default new OpenAIService();
