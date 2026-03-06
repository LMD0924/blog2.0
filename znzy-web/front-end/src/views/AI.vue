<script setup>
import { ref, onMounted, nextTick } from 'vue';

const messages = ref([]);
const inputText = ref('');
const isLoading = ref(false);
const messagesContainer = ref(null);
const sessions = ref([]);
const activeSessionId = ref(localStorage.getItem('chatSessionId') || '');
const sessionsLoading = ref(false);
const creatingSession = ref(false);

// 从后端加载历史消息
const loadHistoryFromServer = async () => {
  if (!activeSessionId.value) return;
  try {
    const response = await fetch(`/ai/history?sessionId=${encodeURIComponent(activeSessionId.value)}`);
    if (!response.ok) {
      throw new Error('无法加载历史消息');
    }
    const data = await response.json();
    messages.value = data.map((msg, index) => ({
      id: `${msg.timestamp}-${index}`,
      text: msg.content,
      sender: msg.role === 'assistant' ? 'ai' : 'user',
      timestamp: msg.timestamp ? new Date(msg.timestamp) : new Date()
    }));
    scrollToBottom();
  } catch (error) {
    console.error('加载历史消息失败:', error);
  }
};

const refreshActiveSessionHistory = async () => {
  await loadHistoryFromServer();
};

const loadSessions = async () => {
  sessionsLoading.value = true;
  try {
    const response = await fetch('/ai/sessions');
    if (!response.ok) {
      throw new Error('无法获取会话列表');
    }
    const data = await response.json();
    sessions.value = data;
    if (!activeSessionId.value && data.length > 0) {
      await selectSession(data[0].sessionId);
    } else if (activeSessionId.value && !data.find(s => s.sessionId === activeSessionId.value)) {
      if (data.length > 0) {
        await selectSession(data[0].sessionId);
      } else {
        await createNewSession();
      }
    } else if (activeSessionId.value) {
      await refreshActiveSessionHistory();
    }
  } catch (error) {
    console.error('加载会话列表失败:', error);
    if (!activeSessionId.value) {
      await createNewSession();
    }
  } finally {
    sessionsLoading.value = false;
  }
};

const selectSession = async (sessionId) => {
  if (activeSessionId.value === sessionId) {
    await refreshActiveSessionHistory();
    return;
  }
  activeSessionId.value = sessionId;
  localStorage.setItem('chatSessionId', sessionId);
  messages.value = [];
  await refreshActiveSessionHistory();
};

const createNewSession = async (title = '') => {
  creatingSession.value = true;
  try {
    const response = await fetch('/ai/session', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ title })
    });
    if (!response.ok) {
      throw new Error('新建会话失败');
    }
    const data = await response.json();
    sessions.value = [data, ...sessions.value];
    await selectSession(data.sessionId);
  } catch (error) {
    console.error(error.message);
  } finally {
    creatingSession.value = false;
  }
};

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

const deleteCurrentSession = async () => {
  if (!activeSessionId.value) return;
  try {
    await fetch(`/ai/session?sessionId=${encodeURIComponent(activeSessionId.value)}`, { method: 'DELETE' });
    sessions.value = sessions.value.filter(s => s.sessionId !== activeSessionId.value);
    activeSessionId.value = '';
    localStorage.removeItem('chatSessionId');
    messages.value = [];
    if (sessions.value.length > 0) {
      await selectSession(sessions.value[0].sessionId);
    } else {
      await createNewSession();
    }
  } catch (error) {
    console.error('删除会话失败:', error);
  }
};

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || isLoading.value) return;
  if (!activeSessionId.value) {
    await createNewSession();
  }

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
    // 调用后端API，传递sessionId
    const url = `/ai/chat?prompt=${encodeURIComponent(prompt)}&sessionId=${encodeURIComponent(activeSessionId.value)}`;
    const response = await fetch(url);

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
    // 回写到后端后再拉取一次，确保数据与后端一致
    await refreshActiveSessionHistory();
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
const clearChat = async () => {
  await deleteCurrentSession();
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
  loadSessions();

  // 添加全局键盘事件监听
  window.addEventListener('keydown', handleKeydown);

  // 初始加载时滚动到底部
  scrollToBottom();
});
</script>
<template>
<div id="app">
  <!-- 顶部导航栏 -->
  <header class="gradient-bg text-white shadow-lg">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
      <div class="flex items-center space-x-3">
        <i class="fas fa-robot text-2xl"></i>
        <h1 class="text-2xl font-bold">AI 智能助手</h1>
      </div>
      <div class="flex items-center space-x-4">
        <button @click="createNewSession()" class="bg-white text-purple-700 hover:bg-gray-100 px-4 py-2 rounded-lg font-medium transition duration-200">
          <i class="fas fa-plus mr-2"></i>新建对话
        </button>
        <button @click="deleteCurrentSession" :disabled="!activeSessionId" class="bg-white text-purple-700 hover:bg-gray-100 px-4 py-2 rounded-lg font-medium transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed">
          <i class="fas fa-trash-alt mr-2"></i>删除当前
        </button>
      </div>
    </div>
  </header>

  <!-- 主要内容区域 -->
  <main class="container mx-auto px-4 py-6 max-w-6xl">
    <div class="flex flex-col lg:flex-row gap-6">
      <!-- 会话列表 -->
      <aside class="bg-white rounded-2xl shadow-lg p-4 lg:w-1/3">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold text-gray-800">历史会话</h3>
          <button @click="loadSessions" class="text-sm text-purple-600 hover:underline">刷新</button>
        </div>
        <div class="space-y-2 max-h-[600px] overflow-y-auto">
          <p v-if="sessionsLoading" class="text-gray-500 text-sm">加载中...</p>
          <p v-else-if="sessions.length === 0" class="text-gray-500 text-sm">暂无会话，点击上方按钮新建。</p>
          <div
            v-for="session in sessions"
            :key="session.sessionId"
            @click="selectSession(session.sessionId)"
            :class="[
              'p-3 rounded-xl border cursor-pointer transition',
              session.sessionId === activeSessionId ? 'border-purple-500 bg-purple-50' : 'border-gray-200 hover:border-purple-300'
            ]"
          >
            <p class="font-medium text-gray-800 truncate">{{ session.title || '未命名对话' }}</p>
            <p class="text-xs text-gray-500 mt-1">
              更新于 {{ formatTime(session.updatedAt || session.createdAt) }}
            </p>
          </div>
        </div>
      </aside>

      <!-- 聊天区域 -->
      <section class="flex-1">
        <div class="bg-white rounded-2xl shadow-lg overflow-hidden mb-6">
          <div class="p-4 border-b border-gray-200 flex justify-between items-center">
            <div>
              <h3 class="text-lg font-semibold text-gray-800">{{ sessions.find(s => s.sessionId === activeSessionId)?.title || '新对话' }}</h3>
              <p class="text-sm text-gray-500">共 {{ messages.length }} 条消息</p>
            </div>
            <button @click="createNewSession()" class="text-purple-600 text-sm hover:underline">新建对话</button>
          </div>

          <div v-if="messages.length === 0" class="p-8 text-center">
            <i class="fas fa-robot text-6xl text-purple-500 mb-4"></i>
            <h2 class="text-2xl font-bold text-gray-800 mb-2">欢迎使用 AI 智能助手</h2>
            <p class="text-gray-600 mb-6">输入第一条消息开始对话，或点击下方示例问题。</p>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div @click="useExample(example)" v-for="example in examples" :key="example" class="bg-gray-50 hover:bg-gray-100 p-4 rounded-lg cursor-pointer transition duration-200 border border-gray-200">
                <p class="text-gray-700">{{ example }}</p>
              </div>
            </div>
          </div>

          <div v-else ref="messagesContainer" class="h-[500px] overflow-y-auto p-4 space-y-4">
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
              :disabled="isLoading || !inputText.trim() || !activeSessionId"
              :class="['px-6 py-3 rounded-lg font-medium transition duration-200 flex items-center justify-center',
                                    isLoading || !inputText.trim() || !activeSessionId ? 'bg-gray-300 text-gray-500 cursor-not-allowed' : 'gradient-bg text-white hover:opacity-90']"
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
      </section>
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
