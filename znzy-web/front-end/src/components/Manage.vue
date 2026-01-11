<script setup>
import { ref, computed, onMounted } from 'vue';
import { get, post, put, del } from "@/net/index.js";
import {message} from "ant-design-vue";
const [messageApi, contextHolder] = message.useMessage();
// 用户数据
const admin = ref({});
const users = ref([]);
const currentUser = ref({
  id: '',
  username: '',
  email: '',
  role: '游客',
  status: '未审核', // 默认状态为未审核
  password: '',
  avatar: 'https://randomuser.me/api/portraits/lego/1.jpg' // 默认头像
});
const userToDelete = ref({});

// 过滤和排序
const searchQuery = ref('');
const roleFilter = ref('');
const statusFilter = ref(''); // 默认显示所有用户
const sortField = ref('createdAt');
const sortDirection = ref('desc');

// 分页
const currentPage = ref(1);
const pageSize = 10;

// 模态框状态
const showAddUserModal = ref(false);
const showDeleteModal = ref(false);
const isEditing = ref(false);
const isLoading = ref(false);
const current = () => {
  return new Promise((resolve, reject) => {
    get('api/user/current', {},
      (message, data) => {
        admin.value = data;
        resolve(data);
      },
      (error) => {
        reject(error);
      }
    );
  });
};

// 获取用户列表
const fetchUsers = async () => {
  try {
    await current();  // 现在会等待回调完成
    console.log("管理员ID:", admin.value.id);
    isLoading.value = true;
    get("api/user/getAllUser", { id: admin.value.id },
      (message, data) => {
        console.log("API响应:", message, data);
        users.value = data || [];
        console.log("用户列表:", users.value);
        console.log("用户数量:", users.value.length);
        console.log("过滤后用户数量:", filteredUsers.value.length);
        isLoading.value = false;
      }
    );
  } catch (error) {
    console.error("获取用户信息失败:", error);
    isLoading.value = false;
  }
};

// 计算属性：过滤后的用户
const filteredUsers = computed(() => {
  let result = users.value;

  // 搜索过滤 (用户名或邮箱)
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(user =>
      (user.username && user.username.toLowerCase().includes(query)) ||
      (user.email && user.email.toLowerCase().includes(query))
    );
  }

  // 角色过滤
  if (roleFilter.value) {
    result = result.filter(user => user.role === roleFilter.value);
  }

  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(user => user.status === statusFilter.value);
  }

  // 排序
  return [...result].sort((a, b) => {
    const field = sortField.value;
    if (a[field] < b[field]) return sortDirection.value === 'asc' ? -1 : 1;
    if (a[field] > b[field]) return sortDirection.value === 'asc' ? 1 : -1;
    return 0;
  });
});

// 分页相关计算属性
const totalPages = computed(() => Math.ceil(filteredUsers.value.length / pageSize));
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  const end = start + pageSize;
  return filteredUsers.value.slice(start, end);
});
const visiblePages = computed(() => {
  const pages = [];
  const maxVisible = 5;
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages.value, start + maxVisible - 1);

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1);
  }

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});

// 分页方法
const goToPage = (page) => {
  currentPage.value = page;
};
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// 排序方法
const sortBy = (field) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortField.value = field;
    sortDirection.value = 'asc';
  }
};

// 重置过滤条件
const resetFilters = () => {
  searchQuery.value = '';
  roleFilter.value = '';
  statusFilter.value = '';
  currentPage.value = 1;
};

// 用户操作方法
const editUser = (user) => {
  currentUser.value = { ...user };
  isEditing.value = true;
  showAddUserModal.value = true;
};

const confirmDeleteUser = (user) => {
  userToDelete.value = user;
  showDeleteModal.value = true;
};

// 切换用户状态
const toggleUserStatus = async (user) => {
  console.log("传入user对象：", user);
  if (!user) {
    console.error("user为undefined");
    return;
  }
  console.log("传入ID:", user.id, "status:", user.status);

  post('api/user/updateUserStatus',{
  id:user.id,
  status:'审核通过',
  role:'游客'
},(message,data)=>{
  messageApi.success(message);
  fetchUsers();
})
};

// 批量操作
const selectedUsers = ref([]);
const toggleSelectAll = (event) => {
  if (event.target.checked) {
    selectedUsers.value = paginatedUsers.value.map(user => user.id);
  } else {
    selectedUsers.value = [];
  }
};

const toggleSelectUser = (userId) => {
  const index = selectedUsers.value.indexOf(userId);
  if (index === -1) {
    selectedUsers.value.push(userId);
  } else {
    selectedUsers.value.splice(index, 1);
  }
};

const batchUpdateStatus = async (status) => {
  try {
    await post('api/admin/users/batch-update', {
      userIds: selectedUsers.value,
      status
    });
    await fetchUsers();
    selectedUsers.value = [];
  } catch (error) {
    console.error("批量更新状态失败:", error);
  }
};

// 辅助方法
const getRoleName = (role) => {
  const roles = {
    admin: '管理员',
    visitor: '游客',
    user: '普通用户',
  };
  return roles[role] || role;
};

const getStatusName = (status) => {
  const statuses = {
    approved: '审核通过',
    pending: '未审核',
    rejected: '审核未通过',
    banned: '已禁用'
  };
  return statuses[status] || status;
};

const getRoleBadgeClass = (role) => {
  switch (role) {
    case '管理员':
      return 'bg-red-500 text-white';
    case '游客':
      return 'bg-blue-500 text-white';
    case '普通用户':
      return 'bg-green-500 text-white';
    default:
      return 'bg-gray-500 text-white';
  }
};

const getStatusBadgeClass = (status) => {
  switch (status) {
    case '审核通过':
      return 'bg-green-500 text-white';
    case '未审核':
      return 'bg-yellow-500 text-white animate-pulse';
    case '审核未通过':
      return 'bg-red-500 text-white';
    case '已禁用':
      return 'bg-gray-500 text-white';
    default:
      return 'bg-gray-500 text-white';
  }
};

const formatDate = (dateString) => {
  const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
  return new Date(dateString).toLocaleDateString(undefined, options);
};

// 编辑用户
const updateUser = async () => {
  try {
    await post('api/user/updateUserStatus', currentUser.value, (msg, data) => {
      messageApi.success('修改成功');
      showAddUserModal.value = false;
      fetchUsers();
    });
  } catch (e) {
    messageApi.error('修改失败');
  }
};

// 初始化
onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <contextHolder />
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 p-6">
    <!-- 页面标题区域 -->
    <div class="max-w-7xl mx-auto">
      <div class="mb-8 animate-fadeIn">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div class="mb-4 sm:mb-0">
            <h1 class="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              用户管理中心
            </h1>
            <p class="text-gray-600 dark:text-gray-400 mt-2">管理系统中的用户账户、权限和审核状态</p>
          </div>
<!--          <div class="flex flex-wrap gap-3">
            <button @click="showAddUserModal = true"
              class="inline-flex items-center px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200">
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
              新增用户
            </button>
            <button v-if="selectedUsers.length > 0"
              class="inline-flex items-center px-4 py-2 bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200">
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7"></path>
              </svg>
              批量操作 ({{ selectedUsers.length }})
            </button>
          </div>-->
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-sm rounded-2xl p-6 border border-white/20 dark:border-gray-700/50 shadow-modern animate-fadeIn" style="animation-delay: 0.1s">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">总用户数</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{{ users.length }}</p>
            </div>
            <div class="p-3 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-sm rounded-2xl p-6 border border-white/20 dark:border-gray-700/50 shadow-modern animate-fadeIn" style="animation-delay: 0.2s">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">已审核通过</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{{ users.filter(u => u.status === 'approved').length }}</p>
            </div>
            <div class="p-3 bg-gradient-to-br from-green-500 to-green-600 rounded-xl">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-sm rounded-2xl p-6 border border-white/20 dark:border-gray-700/50 shadow-modern animate-fadeIn" style="animation-delay: 0.3s">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">待审核用户</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{{ users.filter(u => u.status === 'pending').length }}</p>
            </div>
            <div class="p-3 bg-gradient-to-br from-amber-500 to-orange-500 rounded-xl">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-sm rounded-2xl p-6 border border-white/20 dark:border-gray-700/50 shadow-modern animate-fadeIn" style="animation-delay: 0.4s">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">审核未通过</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{{ users.filter(u => u.status === 'rejected').length }}</p>
            </div>
            <div class="p-3 bg-gradient-to-br from-red-500 to-red-600 rounded-xl">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索和过滤区域 -->
      <div class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-sm rounded-2xl p-6 border border-white/20 dark:border-gray-700/50 shadow-modern mb-8 animate-fadeIn">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="md:col-span-2">
            <label class="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">搜索用户</label>
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索用户名或邮箱..."
                class="w-full pl-12 pr-4 py-3 bg-white/50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent dark:text-white transition-all duration-200"
              >
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
              </div>
            </div>
          </div>
          <div>
            <label class="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">角色筛选</label>
            <select
                v-model="roleFilter"
                class="w-full px-4 py-3 bg-white/50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent dark:text-white transition-all duration-200"
              >
              <option value="">全部角色</option>
              <option value="管理员">管理员</option>
              <option value="游客">游客</option>
              <option value="普通用户">普通用户</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">状态筛选</label>
            <select
                v-model="statusFilter"
                class="w-full px-4 py-3 bg-white/50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent dark:text-white transition-all duration-200"
              >
              <option value="">全部状态</option>
              <option value="审核通过">审核通过</option>
              <option value="未审核">未审核</option>
              <option value="审核未通过">审核未通过</option>
            </select>
          </div>
        </div>
        <div class="flex justify-end mt-4">
          <button @click="resetFilters"
            class="px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-gray-800 dark:hover:text-gray-100 transition-colors duration-200">
            重置筛选条件
          </button>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-sm rounded-2xl border border-white/20 dark:border-gray-700/50 shadow-modern overflow-hidden animate-fadeIn">
        <div class="overflow-x-auto">
          <table class="min-w-full">
            <thead class="bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800">
              <tr>
                <th class="px-6 py-4 text-left">
                  <input type="checkbox" @change="toggleSelectAll" :checked="selectedUsers.length === paginatedUsers.length && paginatedUsers.length > 0"
                    class="h-4 w-4 text-blue-500 rounded border-gray-300 focus:ring-blue-500 cursor-pointer">
                </th>
                <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                  <div class="flex items-center">
                    用户信息
                    <button @click="sortBy('username')" class="ml-2 focus:outline-none">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4"></path>
                      </svg>
                    </button>
                  </div>
                </th>
                <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                  角色
                </th>
                <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                  状态
                </th>
                <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                  注册时间
                  <button @click="sortBy('createdAt')" class="ml-2 focus:outline-none">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4"></path>
                    </svg>
                  </button>
                </th>
                <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
              <tr v-if="isLoading" class="bg-white/50 dark:bg-gray-800/50">
                <td colspan="6" class="px-6 py-8 text-center">
                  <div class="flex justify-center items-center">
                    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                    <span class="ml-3 text-gray-600 dark:text-gray-400">加载中...</span>
                  </div>
                </td>
              </tr>
              <tr v-for="user in paginatedUsers" :key="user.id"
                class="bg-white/30 dark:bg-gray-800/30 hover:bg-white/50 dark:hover:bg-gray-800/50 transition-all duration-200 animate-fadeIn">
                <td class="px-6 py-4">
                  <input type="checkbox" :checked="selectedUsers.includes(user.id)" @change="toggleSelectUser(user.id)"
                    class="h-4 w-4 text-blue-500 rounded border-gray-300 focus:ring-blue-500 cursor-pointer">
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-12 w-12">
                      <img class="h-12 w-12 rounded-full object-cover ring-2 ring-white dark:ring-gray-700" :src="user.avatar" :alt="user.username">
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-semibold text-gray-900 dark:text-white">
                      {{ user.account }}
                      <span v-if="user.status === 'pending'" class="ml-2 inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200 animate-pulse">
                        新用户
                      </span>
                    </div>
                      <div class="text-sm text-gray-500 dark:text-gray-400">
                        {{ user.email }}
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span :class="getRoleBadgeClass(user.role)" class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold">
                    {{ getRoleName(user.role) }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <span :class="getStatusBadgeClass(user.status)" class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold">
                    {{ getStatusName(user.status) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                  {{}}
                </td>
                <td class="px-6 py-4 text-right">
                  <div class="flex justify-end space-x-2">
                    <button @click="editUser(user)"
                      class="p-2 rounded-lg bg-primary/10 dark:bg-primary/20 text-primary hover:bg-primary/20 dark:hover:bg-primary/30 transition-colors duration-200"
                      title="编辑用户">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                      </svg>
                    </button>
                    <button @click="confirmDeleteUser(user)"
                      class="p-2 rounded-lg bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors duration-200"
                      title="删除用户">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                      </svg>
                    </button>
                    <button @click="toggleUserStatus(user)"
                      :class="user.status === '审核通过' ? 'bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 hover:bg-green-200 dark:hover:bg-green-900/50' : 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-600 dark:text-yellow-400 hover:bg-yellow-200 dark:hover:bg-yellow-900/50'"
                      class="p-2 rounded-lg transition-colors duration-200"
                      :title="user.status === '审核通过' ? '审核未通过' : '审核通过'">
                      <svg v-if="user.status === '审核通过'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z"></path>
                      </svg>
                      <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredUsers.length === 0 && !isLoading" class="text-center py-16">
          <div class="mx-auto h-24 w-24 text-gray-300 dark:text-gray-600 mb-4">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2 animate-fadeIn">没有找到用户</h3>
          <p class="text-gray-500 dark:text-gray-400 mb-6">尝试调整您的搜索或过滤条件</p>
          <button @click="resetFilters"
            class="inline-flex items-center px-6 py-3 bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
            </svg>
            重置过滤条件
          </button>
        </div>

        <!-- 分页 -->
        <div v-if="filteredUsers.length > 0" class="bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 px-6 py-4 border-t border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-700 dark:text-gray-300">
              显示 <span class="font-semibold">{{ (currentPage - 1) * pageSize + 1 }}</span> 到
              <span class="font-semibold">{{ Math.min(currentPage * pageSize, filteredUsers.length) }}</span> 条，
              共 <span class="font-semibold">{{ filteredUsers.length }}</span> 条结果
            </div>
            <div class="flex items-center space-x-2">
              <button @click="prevPage" :disabled="currentPage === 1"
                class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 hover:shadow-sm">
                上一页
              </button>
              <div class="flex space-x-1">
                <button v-for="page in visiblePages" :key="page" @click="goToPage(page)"
                  :class="currentPage === page ? 'bg-primary text-white' : 'bg-white text-gray-700 hover:bg-gray-50'"
                  class="px-3 py-2 text-sm font-medium border border-gray-300 rounded-lg transition-colors duration-200 hover:shadow-sm">
                  {{ page }}
                </button>
              </div>
              <button @click="nextPage" :disabled="currentPage === totalPages"
                class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 hover:shadow-sm">
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div v-if="showAddUserModal" class="fixed inset-0 overflow-y-auto z-50 animate-fadeIn">
      <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 transition-opacity" aria-hidden="true">
          <div class="absolute inset-0 bg-gray-500/50 dark:bg-gray-900/80 backdrop-blur-sm opacity-75"></div>
        </div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-2xl text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full animate-scale-in">
          <div class="bg-white dark:bg-gray-800 px-6 pt-6 pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                <h3 class="text-lg leading-6 font-semibold text-gray-900 dark:text-white mb-4">
                  编辑用户
                </h3>
                <form @submit.prevent="updateUser()" class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">用户名</label>
                    <input v-model="currentUser.account" type="text" required
                      class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent dark:bg-gray-700 dark:text-white transition-all duration-200">
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">邮箱</label>
                    <input v-model="currentUser.email" type="email" required
                      class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-all duration-200">
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">角色</label>
                    <select v-model="currentUser.role"
                      class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-all duration-200">
                      <option value="普通用户">普通用户</option>
                      <option value="游客">游客</option>
                      <option value="管理员">管理员</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">状态</label>
                    <select v-model="currentUser.status"
                      class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-all duration-200">
                      <option value="未审核">未审核</option>
                      <option value="审核通过">审核通过</option>
                      <option value="审核未通过">审核未通过</option>
                    </select>
                  </div>
                </form>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 dark:bg-gray-700 px-6 py-4 sm:flex sm:flex-row-reverse">
            <button @click="updateUser()" type="button"
              class="w-full inline-flex justify-center rounded-xl border border-transparent shadow-sm px-4 py-2 bg-gradient-to-r from-primary to-secondary text-base font-medium text-white hover:from-primary/90 hover:to-secondary/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:ml-3 sm:w-auto sm:text-sm transition-all duration-200">
              保存修改
            </button>
            <button @click="showAddUserModal = false" type="button"
              class="mt-3 w-full inline-flex justify-center rounded-xl border border-gray-300 dark:border-gray-600 shadow-sm px-4 py-2 bg-white dark:bg-gray-800 text-base font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm transition-all duration-200">
              取消
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认模态框 -->
    <div v-if="showDeleteModal" class="fixed inset-0 overflow-y-auto z-50 animate-fadeIn">
      <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 transition-opacity" aria-hidden="true">
          <div class="absolute inset-0 bg-gray-500/50 dark:bg-gray-900/80 backdrop-blur-sm opacity-75"></div>
        </div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-2xl text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full animate-scale-in">
          <div class="bg-white dark:bg-gray-800 px-6 pt-6 pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/30 sm:mx-0 sm:h-10 sm:w-10">
                <svg class="h-6 w-6 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                </svg>
              </div>
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                <h3 class="text-lg leading-6 font-semibold text-gray-900 dark:text-white">
                  确认删除用户
                </h3>
                <div class="mt-2">
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    您确定要删除用户 <span class="font-semibold text-gray-900 dark:text-white">{{ userToDelete.username }}</span> 吗？此操作无法撤销。
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 dark:bg-gray-700 px-6 py-4 sm:flex sm:flex-row-reverse">
            <button @click="deleteUser" type="button"
              class="w-full inline-flex justify-center rounded-xl border border-transparent shadow-sm px-4 py-2 bg-gradient-to-r from-red-500 to-red-600 text-base font-medium text-white hover:from-red-600 hover:to-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:ml-3 sm:w-auto sm:text-sm transition-all duration-200">
              确认删除
            </button>
            <button @click="showDeleteModal = false" type="button"
              class="mt-3 w-full inline-flex justify-center rounded-xl border border-gray-300 dark:border-gray-600 shadow-sm px-4 py-2 bg-white dark:bg-gray-800 text-base font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm transition-all duration-200">
              取消
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out forwards;
}

.animate-scale-in {
  animation: scaleIn 0.2s ease-out forwards;
}

/* 现代化阴影 */
.shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 暗色模式下的现代化阴影 */
.dark .shadow-modern {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}
</style>
