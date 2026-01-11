<script setup>
import { ref, onMounted, nextTick } from 'vue';

const messages = ref([]);
const inputText = ref('');
const isLoading = ref(false);
const messagesContainer = ref(null);

const examples = ref([
  "请用简单的语言解释人工智能",
  "写一首关于春天的短诗",
  "如何学习编程？给我一些建议",
  "帮我制定一个健身计划"
]);

// 滚动到消息底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || isLoading.value) return;

  const userMessage = {
    id: Date.now(),
    text: inputText.value,
    sender: 'user',
    timestamp: new Date()
  };

  messages.value.push(userMessage);
  const prompt = inputText.value;
  inputText.value = '';
  isLoading.value = true;

  scrollToBottom();

  try {
    // 调用后端API
    const response = await fetch(`/ai/chat?prompt=${encodeURIComponent(prompt)}`);

    if (!response.ok) {
      throw new Error('网络响应不正常');
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let aiMessage = {
      id: Date.now() + 1,
      text: '',
      sender: 'ai',
      timestamp: new Date()
    };

    messages.value.push(aiMessage);

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true });
      aiMessage.text += chunk;
      scrollToBottom();
    }
  } catch (error) {
    console.error('发送消息时出错:', error);
    messages.value.push({
      id: Date.now() + 1,
      text: '抱歉，发生错误，请稍后重试。',
      sender: 'ai',
      timestamp: new Date()
    });
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 使用示例问题
const useExample = (example) => {
  inputText.value = example;
};

// 清空对话
const clearChat = () => {
  messages.value = [];
};

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 处理键盘事件
const handleKeydown = (event) => {
  if (event.key === 'Enter' && event.shiftKey) {
    // Shift+Enter 换行，不做任何处理
    return;
  } else if (event.key === 'Enter') {
    // 仅按 Enter 发送消息
    event.preventDefault();
    sendMessage();
  }
};

onMounted(() => {
  // 添加全局键盘事件监听
  window.addEventListener('keydown', handleKeydown);

  // 初始加载时滚动到底部
  scrollToBottom();
});
</script>
<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <header class=" text-white">
      <div class="container mx-auto px-4 py-4 flex justify-between items-center">
        <div class="flex items-center space-x-3">
          <i class="fas fa-robot text-2xl"></i>
        </div>
        <div class="flex items-center space-x-4">
          <button @click="clearChat" class="bg-white text-purple-700 hover:bg-gray-100 px-4 py-2 rounded-lg font-medium transition duration-200">
            <i class="fas fa-trash-alt mr-2"></i>清空对话
          </button>
          <button class="bg-white text-purple-700 hover:bg-gray-100 px-4 py-2 rounded-lg font-medium transition duration-200">
            <i class="fas fa-cog mr-2"></i>设置
          </button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="container mx-auto px-4 py-6 max-w-4xl">
      <!-- 欢迎区域 -->
      <div v-if="messages.length === 0" class="text-center py-12">
        <div class="bg-white rounded-2xl shadow-lg p-8 max-w-2xl mx-auto">
          <i class="fas fa-robot text-6xl text-purple-500 mb-4"></i>
          <h2 class="text-3xl font-bold text-gray-800 mb-4">欢迎使用 AI 智能助手</h2>
          <p class="text-gray-600 mb-6">我可以帮助您解答问题、提供建议和进行对话。请在下方的输入框中输入您的问题。</p>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-8">
            <div @click="useExample(example)" v-for="example in examples" :key="example" class="bg-gray-50 hover:bg-gray-100 p-4 rounded-lg cursor-pointer transition duration-200 border border-gray-200">
              <p class="text-gray-700">{{ example }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div v-else class="bg-white rounded-2xl shadow-lg overflow-hidden mb-6">
        <div class="p-4 border-b border-gray-200 flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-800">对话记录</h3>
          <span class="text-sm text-gray-500">{{ messages.length }} 条消息</span>
        </div>
        <div ref="messagesContainer" class="h-full overflow-y-auto p-4 space-y-4">
          <transition-group name="message" tag="div">
            <div v-for="message in messages" :key="message.id"
                 :class="['flex', message.sender === 'user' ? 'justify-end' : 'justify-start']">
              <div :class="['max-w-xs md:max-w-md lg:max-w-lg rounded-2xl p-4 message-shadow',
                                        message.sender === 'user' ? 'bg-purple-500 text-white rounded-br-none' : 'bg-gray-100 text-gray-800 rounded-bl-none']">
                <div class="flex items-start space-x-2">
                  <div v-if="message.sender === 'ai'" class="flex-shrink-0">
                    <i class="fas fa-robot text-purple-500 mt-1"></i>
                  </div>
                  <div class="flex-1">
                    <p class="whitespace-pre-wrap">{{ message.text }}</p>
                    <p class="text-xs mt-2 opacity-70">
                      {{ formatTime(message.timestamp) }}
                    </p>
                  </div>
                  <div v-if="message.sender === 'user'" class="flex-shrink-0">
                    <i class="fas fa-user text-white mt-1"></i>
                  </div>
                </div>
              </div>
            </div>
          </transition-group>

          <!-- 输入指示器 -->
          <div v-if="isLoading" class="flex justify-start">
            <div class="max-w-xs md:max-w-md lg:max-w-lg rounded-2xl p-4 message-shadow bg-gray-100 text-gray-800 rounded-bl-none">
              <div class="flex items-center space-x-2">
                <div class="flex-shrink-0">
                  <i class="fas fa-robot text-purple-500 mt-1"></i>
                </div>
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="bg-white rounded-2xl shadow-lg p-4">
        <div class="flex space-x-2">
          <div class="flex-1">
                        <textarea
                          v-model="inputText"
                          @keydown.enter.exact.prevent="sendMessage"
                          placeholder="输入您的问题..."
                          rows="2"
                          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-none"
                        ></textarea>
          </div>
          <button
            @click="sendMessage"
            :disabled="isLoading || !inputText.trim()"
            :class="['px-6 py-3 rounded-lg font-medium transition duration-200 flex items-center justify-center',
                                isLoading || !inputText.trim() ? 'bg-gray-300 text-gray-500 cursor-not-allowed' : 'gradient-bg text-white hover:opacity-90']"
          >
            <i class="fas fa-paper-plane mr-2"></i>
            {{ isLoading ? '发送中...' : '发送' }}
          </button>
        </div>
        <div class="flex justify-between items-center mt-3 text-sm text-gray-500">
          <div>
            <span class="mr-4"><i class="fas fa-lightbulb mr-1"></i> 按 Enter 发送</span>
            <span><i class="fas fa-shift mr-1"></i> Shift+Enter 换行</span>
          </div>
          <div>
            <span>{{ inputText.length }}/1000</span>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部信息 -->
    <footer class="text-center py-6 text-gray-500 text-sm">
      <p>Powered by Spring AI & OpenAI | 由总会落叶开发</p>
    </footer>
  </div>
</template>

<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
.message-enter-active {
  transition: all 0.3s ease;
}
.message-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.typing-indicator {
  display: inline-block;
  position: relative;
  width: 40px;
  height: 20px;
}
.typing-indicator span {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-indicator span:nth-child(1) {
  left: 0;
  animation-delay: -0.32s;
}
.typing-indicator span:nth-child(2) {
  left: 12px;
  animation-delay: -0.16s;
}
.typing-indicator span:nth-child(3) {
  left: 24px;
}
@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
.gradient-bg {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.message-shadow {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}
</style>
