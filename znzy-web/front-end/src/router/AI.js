export async function callKimiAPI(apiKey, userInput) {
  const chatRequest = {
    model: "moonshot-v1-8k", // 使用 Kimi 的模型
    messages: [
      { role: "system", content: "你是 Kimi，由 Moonshot AI 提供的人工智能助手。" },
      { role: "user", content: userInput }
    ],
    temperature: 0.3, // 可选参数，控制生成的随机性
  };

  try {
    const response = await fetch("https://api.moonshot.cn/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${apiKey}`,
      },
      body: JSON.stringify(chatRequest),
    });

    if (!response.ok) {
      throw new Error(`API 请求失败: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    return data.choices[0].message.content; // 提取回复内容
  } catch (error) {
    console.error("API 请求失败:", error);
    throw error; // 抛出错误，以便在调用处处理
  }
}
