<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 transition-colors duration-500">
    <!-- 主内容区域 -->
    <main class="container mx-auto px-6 py-8">
      <div class="grid grid-cols-12 gap-6">
        <!-- 左侧边栏 -->
        <aside class="col-span-3 space-y-6">
          <!-- 快速创建卡片 -->
          <div class="bg-gradient-to-br from-white to-blue-50 dark:from-gray-800 dark:to-gray-800/50 rounded-2xl p-6 shadow-lg border border-gray-200/50 dark:border-gray-700/50 transition-all duration-300 hover:shadow-xl global-card">
            <button
              @click="openQuickCreate"
              class="w-full bg-gradient-to-r from-blue-500 via-blue-600 to-purple-600 hover:from-blue-600 hover:via-blue-700 hover:to-purple-700 text-white font-semibold py-4 px-6 rounded-xl flex items-center justify-center space-x-3 transition-all duration-300 transform hover:-translate-y-0.5 active:translate-y-0 shadow-lg hover:shadow-xl"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span class="text-lg">创建日程</span>
            </button>
            <div class="mt-4 grid grid-cols-2 gap-3">
              <button
                v-for="quickOption in quickCreateOptions"
                :key="quickOption.type"
                @click="quickCreate(quickOption.type)"
                class="p-3 rounded-lg bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 hover:border-blue-500 dark:hover:border-blue-500 hover:shadow-md transition-all duration-300 flex flex-col items-center justify-center group"
              >
                <svg class="w-5 h-5 mb-2 text-gray-500 dark:text-gray-400 group-hover:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path v-if="quickOption.type === 'meeting'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                  <path v-else-if="quickOption.type === 'task'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  <path v-else-if="quickOption.type === 'reminder'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ quickOption.label }}</span>
              </button>
            </div>
          </div>

          <!-- 小型日历 -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-lg border border-gray-200/50 dark:border-gray-700/50 global-card">
            <div class="flex items-center justify-between mb-6">
              <h3 class="text-lg font-semibold text-gray-800 dark:text-white">日历</h3>
              <div class="flex items-center space-x-2">
                <button @click="prevMonth" class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                  <svg class="w-4 h-4 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                  </svg>
                </button>
                <button @click="nextMonth" class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                  <svg class="w-4 h-4 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
            </div>

            <div class="mb-4">
              <h4 class="text-xl font-bold text-gray-800 dark:text-white text-center">
                {{ currentMonthYear }}
              </h4>
            </div>

            <!-- 星期标题 -->
            <div class="grid grid-cols-7 gap-1 mb-3">
              <div v-for="day in weekDays" :key="day" class="text-center py-2 text-sm font-medium text-gray-500 dark:text-gray-400">
                {{ day }}
              </div>
            </div>

            <!-- 日期网格 -->
            <div class="grid grid-cols-7 gap-1">
              <div
                v-for="day in calendarDays"
                :key="day.date"
                @click="selectDate(day.date)"
                :class="[
                  'aspect-square flex flex-col items-center justify-center rounded-xl text-sm transition-all duration-300 cursor-pointer relative',
                  {
                    'bg-blue-500 text-white shadow-md scale-105': day.isSelected,
                    'text-gray-300 dark:text-gray-600 cursor-default': day.isOtherMonth,
                    'hover:bg-gray-100 dark:hover:bg-gray-700': !day.isSelected && !day.isOtherMonth,
                    'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400': day.hasEvents,
                    'font-semibold': day.isToday
                  }
                ]"
              >
                <span>{{ day.day }}</span>
                <div v-if="day.eventCount > 0" class="mt-1 flex space-x-1">
                  <div v-for="color in day.eventColors" :key="color"
                       class="w-1 h-1 rounded-full" :style="{ backgroundColor: color }"></div>
                </div>
                <div v-if="day.isToday" class="absolute top-1 right-1 w-1 h-1 bg-blue-500 rounded-full"></div>
              </div>
            </div>
          </div>

          <!-- 我的日历 -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-lg border border-gray-200/50 dark:border-gray-700/50 global-card">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">我的日历</h3>
            <div class="space-y-3">
              <div
                v-for="calendar in calendars"
                :key="calendar.id"
                @click="toggleCalendar(calendar.id)"
                class="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer group"
              >
                <div class="flex items-center space-x-3">
                  <div class="relative">
                    <div
                      class="w-4 h-4 rounded"
                      :style="{ backgroundColor: calendar.color }"
                    ></div>
                    <div v-if="calendar.visible" class="absolute inset-0 border-2 border-white dark:border-gray-800 rounded"></div>
                  </div>
                  <span class="text-gray-700 dark:text-gray-300">{{ calendar.name }}</span>
                </div>
                <div class="flex items-center space-x-3">
                  <span class="text-xs text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded">
                    {{ calendar.eventCount }}
                  </span>
                  <svg class="w-4 h-4 text-gray-400 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- 日程概览 -->
          <div class="bg-gradient-to-br from-blue-50 to-purple-50 dark:from-blue-900/20 dark:to-purple-900/20 rounded-2xl p-6 shadow-lg border border-blue-200/50 dark:border-blue-800/50 global-card">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">今日概览</h3>
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600 dark:text-gray-400">会议</span>
                <span class="font-semibold text-blue-600 dark:text-blue-400">{{ todayStats.meetings }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600 dark:text-gray-400">任务</span>
                <span class="font-semibold text-green-600 dark:text-green-400">{{ todayStats.tasks }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600 dark:text-gray-400">空闲时间</span>
                <span class="font-semibold text-purple-600 dark:text-purple-400">{{ todayStats.freeTime }}</span>
              </div>
            </div>
            <div class="mt-6 pt-4 border-t border-blue-200/50 dark:border-blue-800/50">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600 dark:text-gray-400">专注度</span>
                <span class="font-semibold text-gray-800 dark:text-white">{{ focusScore }}%</span>
              </div>
              <div class="mt-2 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-green-500 to-blue-500 rounded-full" :style="{ width: focusScore + '%' }"></div>
              </div>
            </div>
          </div>
        </aside>

        <!-- 主日历区域 -->
        <section class="col-span-9">
          <!-- 日历头部控制 -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-lg border border-gray-200/50 dark:border-gray-700/50 mb-6 global-card">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <h2 class="text-2xl font-bold text-gray-800 dark:text-white">{{ currentRangeText }}</h2>
                <button @click="goToToday" class="px-4 py-2 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-lg font-medium hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors">
                  今天
                </button>
              </div>

              <div class="flex items-center space-x-4">
                <!-- 视图切换 -->
                <div class="flex items-center bg-gray-100 dark:bg-gray-700 p-1 rounded-xl">
                  <button
                    v-for="view in views"
                    :key="view.id"
                    @click="switchView(view.id)"
                    :class="[
                      'px-4 py-2 rounded-lg font-medium transition-all duration-300',
                      currentView === view.id
                        ? 'bg-white dark:bg-gray-800 text-blue-600 dark:text-blue-400 shadow-sm'
                        : 'text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-white'
                    ]"
                  >
                    {{ view.name }}
                  </button>
                </div>

                <!-- 导航控制 -->
                <div class="flex items-center space-x-2">
                  <button @click="navigateTime(-1)" class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                    <svg class="w-5 h-5 text-gray-600 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                    </svg>
                  </button>
                  <button @click="navigateTime(1)" class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                    <svg class="w-5 h-5 text-gray-600 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>

            <!-- 快捷时间范围 -->
            <div class="mt-6 flex items-center space-x-3">
              <span class="text-sm text-gray-500 dark:text-gray-400">快捷跳转:</span>
              <button
                v-for="period in quickPeriods"
                :key="period.value"
                @click="jumpToPeriod(period.value)"
                class="px-3 py-1 text-sm rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300 transition-colors"
              >
                {{ period.label }}
              </button>
            </div>
          </div>

          <!-- 周视图 -->
          <div v-if="currentView === 'week'" class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200/50 dark:border-gray-700/50 overflow-hidden global-card">
            <!-- 时间轴头部 -->
            <div class="grid grid-cols-8 border-b border-gray-200 dark:border-gray-700">
              <div class="p-4"></div>
              <div
                v-for="day in weekDaysWithDates"
                :key="day.date"
                class="p-4 text-center border-l border-gray-200 dark:border-gray-700"
                :class="{ 'bg-blue-50 dark:bg-blue-900/20': day.isToday }"
              >
                <div class="text-sm text-gray-500 dark:text-gray-400">{{ day.dayName }}</div>
                <div class="mt-1">
                  <div :class="[
                    'inline-flex items-center justify-center w-8 h-8 rounded-full text-lg font-semibold',
                    day.isToday
                      ? 'bg-blue-500 text-white'
                      : 'text-gray-800 dark:text-white'
                  ]">
                    {{ day.dayNumber }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 时间轴内容 -->
            <div class="overflow-y-auto max-h-[600px]">
              <div
                v-for="hour in hours"
                :key="hour"
                class="grid grid-cols-8 border-b border-gray-100 dark:border-gray-700/50"
              >
                <!-- 时间标签 -->
                <div class="p-3 border-r border-gray-200 dark:border-gray-700">
                  <div class="text-sm text-gray-500 dark:text-gray-400 text-right pr-3">
                    {{ formatHour(hour) }}
                  </div>
                </div>

                <!-- 每个时间段 -->
                <div
                  v-for="day in weekDaysWithDates"
                  :key="`${day.date}-${hour}`"
                  @click="createEventAt(day.date, hour)"
                  class="h-16 border-l border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors cursor-pointer relative group"
                  :class="{ 'bg-blue-50/50 dark:bg-blue-900/10': day.isToday }"
                >
                  <!-- 事件 -->
                  <div
                    v-for="event in getEventsForSlot(day.date, hour)"
                    :key="event.id"
                    :style="{
                      top: `${(event.startMinute / 60) * 64}px`,
                      height: `${Math.max(32, event.duration * 64 / 60)}px`
                    }"
                    class="absolute left-1 right-1 rounded-lg p-2 overflow-hidden shadow-sm hover:shadow-md transition-all duration-200 border"
                    :class="[
                      getEventBorderColor(event.type),
                      event.status === 'ongoing' ? 'border-l-4 border-l-green-500' : ''
                    ]"
                    @click.stop="openEventDetails(event)"
                  >
                    <div class="flex items-start justify-between">
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center space-x-2 mb-1">
                          <div class="w-2 h-2 rounded-full" :style="{ backgroundColor: getEventColor(event.type) }"></div>
                          <p class="text-xs font-semibold text-gray-800 dark:text-white truncate">{{ event.title }}</p>
                        </div>
                        <div class="flex items-center space-x-2">
                          <svg class="w-3 h-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                          <span class="text-xs text-gray-500 dark:text-gray-400">{{ event.timeRange }}</span>
                        </div>
                        <div v-if="event.participants" class="flex items-center space-x-1 mt-1">
                          <div class="flex -space-x-2">
                            <img
                              v-for="participant in event.participants.slice(0, 3)"
                              :key="participant.id"
                              :src="participant.avatar"
                              :alt="participant.name"
                              class="w-6 h-6 rounded-full border-2 border-white dark:border-gray-800"
                            />
                          </div>
                          <span v-if="event.participants.length > 3" class="text-xs text-gray-400">+{{ event.participants.length - 3 }}</span>
                        </div>
                      </div>
                      <div v-if="event.priority" class="flex-shrink-0">
                        <span :class="[
                          'text-xs px-2 py-0.5 rounded-full',
                          event.priority === 'high' ? 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400' :
                          event.priority === 'medium' ? 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400' :
                          'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400'
                        ]">
                          {{ event.priority === 'high' ? '高' : event.priority === 'medium' ? '中' : '低' }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 月视图 -->
          <div v-else-if="currentView === 'month'" class="grid grid-cols-7 gap-4">
            <!-- 月视图实现 -->
            <div
              v-for="day in calendarDays"
              :key="day.date"
              @click="selectDate(day.date)"
              :class="[
                'p-4 rounded-xl border border-gray-200 dark:border-gray-700 hover:border-blue-300 dark:hover:border-blue-700 transition-colors cursor-pointer min-h-24',
                {
                  'bg-blue-50 dark:bg-blue-900/20 border-blue-300 dark:border-blue-700': day.isToday,
                  'text-gray-300 dark:text-gray-600': day.isOtherMonth
                }
              ]"
            >
              <div class="flex items-center justify-between mb-2">
                <span class="font-medium text-gray-800 dark:text-white">{{ day.day }}</span>
                <div v-if="day.eventCount > 0" class="flex space-x-1">
                  <div v-for="color in day.eventColors.slice(0, 2)" :key="color" class="w-2 h-2 rounded-full" :style="{ backgroundColor: color }"></div>
                  <span v-if="day.eventCount > 2" class="text-xs text-gray-400">+{{ day.eventCount - 2 }}</span>
                </div>
              </div>
              <div class="space-y-1">
                <div v-for="event in getEventsForDay(day.date).slice(0, 2)" :key="event.id"
                     class="text-xs p-1 rounded truncate"
                     :style="{ backgroundColor: getEventColor(event.type) + '20', color: getEventColor(event.type) }">
                  {{ event.title }}
                </div>
              </div>
            </div>
          </div>

          <!-- 日视图 -->
          <div v-else class="grid grid-cols-1 gap-4">
            <!-- 日视图实现 -->
            <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200/50 dark:border-gray-700/50 overflow-hidden global-card">
              <div class="p-6 border-b border-gray-200 dark:border-gray-700">
                <h3 class="text-xl font-bold text-gray-800 dark:text-white">{{ formatDate(currentDate) }}</h3>
              </div>
              <div class="p-6">
                <div v-for="hour in hours" :key="hour" class="mb-4">
                  <div class="flex items-start">
                    <div class="w-20 text-gray-500 dark:text-gray-400 text-sm font-medium">{{ formatHour(hour) }}</div>
                    <div class="flex-1 min-h-16 border-l border-gray-200 dark:border-gray-700 pl-4 relative">
                      <div
                        v-for="event in getEventsForDay(currentDate.toISOString().split('T')[0]).filter(e => e.startHour === hour)"
                        :key="event.id"
                        @click="openEventDetails(event)"
                        class="mb-2 p-3 rounded-lg border cursor-pointer hover:shadow-md transition-all"
                        :class="getEventBorderColor(event.type)"
                      >
                        <div class="flex items-center justify-between">
                          <div>
                            <h4 class="font-semibold text-gray-800 dark:text-white">{{ event.title }}</h4>
                            <p class="text-sm text-gray-500 dark:text-gray-400">{{ event.timeRange }}</p>
                          </div>
                          <span :class="[
                            'text-xs px-2 py-0.5 rounded-full',
                            event.priority === 'high' ? 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400' :
                            event.priority === 'medium' ? 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400' :
                            'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400'
                          ]">
                            {{ event.priority === 'high' ? '高' : event.priority === 'medium' ? '中' : '低' }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 日程统计 -->
          <div class="mt-6 grid grid-cols-4 gap-4">
            <div class="bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl p-6 text-white shadow-lg global-card">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm opacity-90">今日会议</p>
                  <p class="text-2xl font-bold mt-2">{{ stats.todayMeetings }}</p>
                </div>
                <svg class="w-10 h-10 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div class="mt-4 text-sm opacity-90">↑ {{ stats.meetingTrend }}% 对比昨天</div>
            </div>

            <div class="bg-gradient-to-br from-green-500 to-green-600 rounded-2xl p-6 text-white shadow-lg global-card">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm opacity-90">已完成任务</p>
                  <p class="text-2xl font-bold mt-2">{{ stats.completedTasks }}</p>
                </div>
                <svg class="w-10 h-10 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div class="mt-4 text-sm opacity-90">{{ stats.completionRate }}% 完成率</div>
            </div>

            <div class="bg-gradient-to-br from-purple-500 to-purple-600 rounded-2xl p-6 text-white shadow-lg global-card">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm opacity-90">预计耗时</p>
                  <p class="text-2xl font-bold mt-2">{{ stats.estimatedHours }}h</p>
                </div>
                <svg class="w-10 h-10 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div class="mt-4 text-sm opacity-90">平均 {{ stats.avgDuration }}h/任务</div>
            </div>

            <div class="bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl p-6 text-white shadow-lg global-card">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm opacity-90">专注时间</p>
                  <p class="text-2xl font-bold mt-2">{{ stats.focusTime }}h</p>
                </div>
                <svg class="w-10 h-10 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
              </div>
              <div class="mt-4 text-sm opacity-90">{{ stats.focusPercentage }}% 有效率</div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- 创建日程弹窗 -->
    <div v-if="showEventDialog" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity" @click="closeEventDialog"></div>

      <div class="relative min-h-screen flex items-center justify-center p-4">
        <div class="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-hidden animate-slide-in">
          <!-- 头部 -->
          <div class="border-b border-gray-200 dark:border-gray-700 p-6">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-bold text-gray-900 dark:text-white">
                {{ isEditMode ? '编辑日程' : '创建日程' }}
              </h3>
              <button @click="closeEventDialog" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                <svg class="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <!-- 表单内容 -->
          <div class="overflow-y-auto max-h-[calc(90vh-200px)] p-6">
            <form @submit.prevent="handleSubmit" class="space-y-6">
              <!-- 标题 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  标题 *
                </label>
                <input
                  v-model="eventForm.title"
                  type="text"
                  required
                  class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white"
                  placeholder="请输入日程标题"
                />
              </div>

              <!-- 日期和时间 -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    日期 *
                  </label>
                  <input
                    v-model="eventForm.date"
                    type="date"
                    required
                    class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    时间 *
                  </label>
                  <div class="flex items-center space-x-3">
                    <input
                      v-model="eventForm.startTime"
                      type="time"
                      required
                      class="flex-1 px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white"
                    />
                    <span class="text-gray-500 dark:text-gray-400">至</span>
                    <input
                      v-model="eventForm.endTime"
                      type="time"
                      required
                      class="flex-1 px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white"
                    />
                  </div>
                </div>
              </div>

              <!-- 日程类型和优先级 -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    类型
                  </label>
                  <select
                    v-model="eventForm.type"
                    class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white"
                  >
                    <option value="work">工作</option>
                    <option value="personal">个人</option>
                    <option value="team">团队</option>
                    <option value="family">家庭</option>
                    <option value="meeting">会议</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    优先级
                  </label>
                  <select
                    v-model="eventForm.priority"
                    class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white"
                  >
                    <option value="low">低</option>
                    <option value="medium">中</option>
                    <option value="high">高</option>
                  </select>
                </div>
              </div>

              <!-- 日历选择 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  添加到日历
                </label>
                <div class="grid grid-cols-2 gap-3">
                  <div
                    v-for="calendar in calendarOptions"
                    :key="calendar.id"
                    @click="eventForm.calendarId = calendar.id"
                    :class="[
                      'p-3 rounded-xl border-2 cursor-pointer transition-all',
                      eventForm.calendarId === calendar.id
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                        : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                    ]"
                  >
                    <div class="flex items-center space-x-3">
                      <div
                        class="w-3 h-3 rounded-full"
                        :style="{ backgroundColor: calendar.color }"
                      ></div>
                      <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
                        {{ calendar.name }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 描述 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  描述
                </label>
                <textarea
                  v-model="eventForm.description"
                  rows="3"
                  class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-gray-900 dark:text-white resize-none"
                  placeholder="请输入日程描述..."
                ></textarea>
              </div>

              <!-- 重复设置 -->
              <div>
                <div class="flex items-center justify-between mb-2">
                  <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                    重复
                  </label>
                  <div class="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      v-model="eventForm.repeat.enabled"
                      class="w-4 h-4 text-blue-500 rounded focus:ring-blue-500 focus:ring-offset-0"
                    />
                    <span class="text-sm text-gray-600 dark:text-gray-400">重复日程</span>
                  </div>
                </div>
                <div v-if="eventForm.repeat.enabled" class="pl-6 space-y-3">
                  <div class="flex items-center space-x-4">
                    <select
                      v-model="eventForm.repeat.frequency"
                      class="px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm text-gray-900 dark:text-white"
                    >
                      <option value="daily">每天</option>
                      <option value="weekly">每周</option>
                      <option value="monthly">每月</option>
                      <option value="yearly">每年</option>
                    </select>
                    <span class="text-sm text-gray-500 dark:text-gray-400">重复一次</span>
                  </div>
                </div>
              </div>
            </form>
          </div>

          <!-- 底部操作 -->
          <div class="border-t border-gray-200 dark:border-gray-700 p-6">
            <div class="flex items-center justify-between">
              <div>
                <button
                  v-if="isEditMode"
                  type="button"
                  @click="handleDeleteEvent"
                  class="px-4 py-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                >
                  删除日程
                </button>
              </div>
              <div class="flex items-center space-x-3">
                <button
                  type="button"
                  @click="closeEventDialog"
                  class="px-6 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                >
                  取消
                </button>
                <button
                  type="submit"
                  @click="handleSaveEvent"
                  class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  {{ isEditMode ? '更新' : '创建' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷创建弹窗 -->
    <div v-if="showQuickCreateDialog" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity" @click="closeQuickCreateDialog"></div>

      <div class="relative min-h-screen flex items-center justify-center p-4">
        <div class="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-md w-full animate-slide-in">
          <!-- 头部 -->
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-bold text-gray-900 dark:text-white">快速创建</h3>
              <button @click="closeQuickCreateDialog" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                <svg class="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">选择日程类型快速创建</p>
          </div>

          <!-- 快速选项 -->
          <div class="p-6">
            <div class="grid grid-cols-2 gap-4">
              <button
                v-for="option in quickCreateOptions"
                :key="option.type"
                @click="selectQuickOption(option)"
                :class="[
                  'p-6 rounded-xl border-2 transition-all duration-300 transform hover:-translate-y-1',
                  selectedQuickType === option.type
                    ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 shadow-md'
                    : 'border-gray-200 dark:border-gray-700 hover:border-blue-300 dark:hover:border-blue-700 hover:shadow-lg'
                ]"
              >
                <div class="flex flex-col items-center space-y-3">
                  <div :class="[
                    'p-3 rounded-full',
                    option.type === 'meeting' ? 'bg-blue-100 dark:bg-blue-900/30' :
                    option.type === 'task' ? 'bg-green-100 dark:bg-green-900/30' :
                    option.type === 'reminder' ? 'bg-yellow-100 dark:bg-yellow-900/30' :
                    'bg-red-100 dark:bg-red-900/30'
                  ]">
                    <svg class="w-6 h-6" :class="[
                      option.type === 'meeting' ? 'text-blue-600 dark:text-blue-400' :
                      option.type === 'task' ? 'text-green-600 dark:text-green-400' :
                      option.type === 'reminder' ? 'text-yellow-600 dark:text-yellow-400' :
                      'text-red-600 dark:text-red-400'
                    ]" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path v-if="option.type === 'meeting'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                      <path v-else-if="option.type === 'task'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                      <path v-else-if="option.type === 'reminder'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                      <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div class="text-center">
                    <p class="font-semibold text-gray-900 dark:text-white">{{ option.label }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ option.description }}</p>
                  </div>
                </div>
              </button>
            </div>

            <!-- 详细信息表单 -->
            <div v-if="selectedQuickType" class="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    标题
                  </label>
                  <input
                    v-model="quickForm.title"
                    type="text"
                    class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-white"
                    :placeholder="`输入${selectedQuickOption?.label}标题`"
                  />
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      日期
                    </label>
                    <input
                      v-model="quickForm.date"
                      type="date"
                      class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-white"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      时间
                    </label>
                    <input
                      v-model="quickForm.time"
                      type="time"
                      class="w-full px-4 py-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-white"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="p-6 border-t border-gray-200 dark:border-gray-700">
            <div class="flex items-center justify-between">
              <button
                v-if="selectedQuickType"
                @click="resetQuickForm"
                type="button"
                class="px-4 py-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                重置
              </button>
              <div class="flex items-center space-x-3 ml-auto">
                <button
                  @click="closeQuickCreateDialog"
                  type="button"
                  class="px-6 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                >
                  取消
                </button>
                <button
                  @click="handleQuickCreate"
                  :disabled="!selectedQuickType || !quickForm.title"
                  :class="[
                    'px-6 py-2 rounded-lg transition-colors',
                    selectedQuickType && quickForm.title
                      ? 'bg-blue-500 text-white hover:bg-blue-600'
                      : 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
                  ]"
                >
                  创建
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

// 响应式状态
const searchQuery = ref('')
const isDarkMode = ref(true)
const currentView = ref('week')
const currentDate = ref(new Date())
const showEventDialog = ref(false)
const showQuickCreateDialog = ref(false)
const showNotifications = ref(false)
const selectedEvent = ref(null)
const selectedQuickType = ref('')
const unreadNotifications = ref(3)

// 表单数据
const eventForm = ref({
  title: '',
  date: '',
  startTime: '09:00',
  endTime: '10:00',
  type: 'work',
  priority: 'medium',
  calendarId: 'work',
  description: '',
  repeat: {
    enabled: false,
    frequency: 'weekly'
  },
  participants: []
})

const quickForm = ref({
  title: '',
  date: '',
  time: '',
  meetingLink: '',
  description: ''
})

// 数据
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const views = [
  { id: 'day', name: '日视图' },
  { id: 'week', name: '周视图' },
  { id: 'month', name: '月视图' }
]

const quickPeriods = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'thisWeek' },
  { label: '本月', value: 'thisMonth' },
  { label: '下月', value: 'nextMonth' }
]

const quickCreateOptions = [
  {
    type: 'meeting',
    label: '会议',
    description: '安排团队会议',
    defaultTitle: '团队会议'
  },
  {
    type: 'task',
    label: '任务',
    description: '创建待办事项',
    defaultTitle: '新任务'
  },
  {
    type: 'reminder',
    label: '提醒',
    description: '设置重要提醒',
    defaultTitle: '重要提醒'
  },
  {
    type: 'deadline',
    label: '截止',
    description: '设定最后期限',
    defaultTitle: '截止日期'
  }
]

const calendarOptions = [
  { id: 'personal', name: '个人日程', color: '#3B82F6' },
  { id: 'work', name: '工作日程', color: '#10B981' },
  { id: 'team', name: '团队协作', color: '#8B5CF6' },
  { id: 'family', name: '家庭生活', color: '#F59E0B' }
]

const calendars = [
  { id: 'personal', name: '个人日程', color: '#3B82F6', eventCount: 8, visible: true },
  { id: 'work', name: '工作日程', color: '#10B981', eventCount: 12, visible: true },
  { id: 'team', name: '团队协作', color: '#8B5CF6', eventCount: 5, visible: true },
  { id: 'family', name: '家庭生活', color: '#F59E0B', eventCount: 3, visible: true }
]

const hours = Array.from({ length: 12 }, (_, i) => i + 8) // 8:00 - 20:00

const currentUser = {
  name: '张明',
  email: 'zhangming@example.com',
  role: '高级产品经理',
  avatar: 'https://randomuser.me/api/portraits/men/32.jpg'
}

const notifications = [
  {
    id: 1,
    title: '团队会议将在10分钟后开始',
    type: 'meeting',
    time: '5分钟前',
    read: false
  },
  {
    id: 2,
    title: '项目报告提交截止提醒',
    type: 'deadline',
    time: '1小时前',
    read: false
  },
  {
    id: 3,
    title: '您有一个新的日程邀请',
    type: 'meeting',
    time: '2小时前',
    read: true
  }
]

const events = ref([
  {
    id: 1,
    title: '产品需求评审会议',
    date: new Date().toISOString().split('T')[0],
    startHour: 10,
    startMinute: 0,
    duration: 90,
    type: 'work',
    priority: 'high',
    participants: [
      { id: 1, name: '张三', avatar: 'https://randomuser.me/api/portraits/men/1.jpg' },
      { id: 2, name: '李四', avatar: 'https://randomuser.me/api/portraits/women/2.jpg' },
      { id: 3, name: '王五', avatar: 'https://randomuser.me/api/portraits/men/3.jpg' }
    ],
    status: 'upcoming',
    timeRange: '10:00 - 11:30'
  },
  {
    id: 2,
    title: '代码审查',
    date: new Date().toISOString().split('T')[0],
    startHour: 14,
    startMinute: 30,
    duration: 60,
    type: 'work',
    priority: 'medium',
    participants: [
      { id: 4, name: '赵六', avatar: 'https://randomuser.me/api/portraits/women/4.jpg' }
    ],
    status: 'ongoing',
    timeRange: '14:30 - 15:30'
  }
])

const stats = ref({
  todayMeetings: 4,
  meetingTrend: 12,
  completedTasks: 8,
  completionRate: 85,
  estimatedHours: 32,
  avgDuration: 2.5,
  focusTime: 6,
  focusPercentage: 75
})

const todayStats = ref({
  meetings: 3,
  tasks: 5,
  freeTime: '4小时'
})

// 计算属性
const isEditMode = computed(() => !!selectedEvent.value?.id)

const selectedQuickOption = computed(() => {
  return quickCreateOptions.find(opt => opt.type === selectedQuickType.value)
})

const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
})

const currentRangeText = computed(() => {
  const start = new Date(currentDate.value)
  const end = new Date(currentDate.value)

  if (currentView.value === 'week') {
    start.setDate(start.getDate() - start.getDay())
    end.setDate(start.getDate() + 6)
    return `${formatDate(start)} - ${formatDate(end)}`
  } else if (currentView.value === 'month') {
    start.setDate(1)
    end.setMonth(start.getMonth() + 1)
    end.setDate(0)
    return `${formatDate(start)} - ${formatDate(end)}`
  }

  return formatDate(currentDate.value)
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const today = new Date()

  const days = []

  // 上个月的日期
  const prevMonthDays = firstDay.getDay()
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push({
      day: date.getDate(),
      date: date.toISOString().split('T')[0],
      isOtherMonth: true,
      isToday: false,
      isSelected: false,
      hasEvents: false,
      eventCount: 0,
      eventColors: []
    })
  }

  // 当前月的日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(year, month, i)
    const dateStr = date.toISOString().split('T')[0]
    const dayEvents = getEventsForDay(dateStr)

    days.push({
      day: i,
      date: dateStr,
      isOtherMonth: false,
      isToday: date.toDateString() === today.toDateString(),
      isSelected: false,
      hasEvents: dayEvents.length > 0,
      eventCount: dayEvents.length,
      eventColors: [...new Set(dayEvents.map(e => getEventColor(e.type)))]
    })
  }

  // 下个月的日期
  const nextMonthDays = 42 - days.length // 6行 * 7列
  for (let i = 1; i <= nextMonthDays; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      day: i,
      date: date.toISOString().split('T')[0],
      isOtherMonth: true,
      isToday: false,
      isSelected: false,
      hasEvents: false,
      eventCount: 0,
      eventColors: []
    })
  }

  return days
})

const weekDaysWithDates = computed(() => {
  const start = new Date(currentDate.value)
  start.setDate(start.getDate() - start.getDay()) // Start from Sunday

  return Array.from({ length: 7 }, (_, i) => {
    const date = new Date(start)
    date.setDate(start.getDate() + i)

    return {
      date: date.toISOString().split('T')[0],
      dayName: weekDays[date.getDay()],
      dayNumber: date.getDate(),
      isToday: date.toDateString() === new Date().toDateString()
    }
  })
})

const focusScore = computed(() => {
  return Math.floor(Math.random() * 30) + 70 // 模拟专注度分数
})

// 方法
const formatDate = (date) => {
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

const formatHour = (hour) => {
  return `${hour}:00`
}

const getEventsForDay = (date) => {
  return events.value.filter(event => {
    const eventDate = new Date(event.date)
    return eventDate.toISOString().split('T')[0] === date
  })
}

const getEventsForSlot = (date, hour) => {
  return events.value.filter(event => {
    const eventDate = new Date(event.date)
    return eventDate.toISOString().split('T')[0] === date &&
      event.startHour === hour
  })
}

const getEventColor = (type) => {
  const colors = {
    work: '#3B82F6',
    personal: '#10B981',
    family: '#8B5CF6',
    team: '#F59E0B',
    meeting: '#EF4444'
  }
  return colors[type] || '#6B7280'
}

const getEventBorderColor = (type) => {
  const colors = {
    work: 'border-blue-200 dark:border-blue-800/50 bg-blue-50/50 dark:bg-blue-900/20',
    personal: 'border-green-200 dark:border-green-800/50 bg-green-50/50 dark:bg-green-900/20',
    family: 'border-purple-200 dark:border-purple-800/50 bg-purple-50/50 dark:bg-purple-900/20',
    team: 'border-yellow-200 dark:border-yellow-800/50 bg-yellow-50/50 dark:bg-yellow-900/20',
    meeting: 'border-red-200 dark:border-red-800/50 bg-red-50/50 dark:bg-red-900/20'
  }
  return colors[type] || 'border-gray-200 dark:border-gray-800/50 bg-gray-50/50 dark:bg-gray-900/20'
}

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

const navigateTime = (direction) => {
  const newDate = new Date(currentDate.value)
  if (currentView.value === 'week') {
    newDate.setDate(newDate.getDate() + direction * 7)
  } else if (currentView.value === 'day') {
    newDate.setDate(newDate.getDate() + direction)
  } else if (currentView.value === 'month') {
    newDate.setMonth(newDate.getMonth() + direction)
  }
  currentDate.value = newDate
}

const goToToday = () => {
  currentDate.value = new Date()
}

const prevMonth = () => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() - 1)
  currentDate.value = newDate
}

const nextMonth = () => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + 1)
  currentDate.value = newDate
}

const switchView = (view) => {
  currentView.value = view
}

const selectDate = (date) => {
  currentDate.value = new Date(date)
  currentView.value = 'day'
}

const jumpToPeriod = (period) => {
  const today = new Date()
  const newDate = new Date(today)

  switch (period) {
    case 'today':
      currentDate.value = today
      break
    case 'thisWeek':
      currentDate.value = today
      currentView.value = 'week'
      break
    case 'thisMonth':
      currentDate.value = new Date(today.getFullYear(), today.getMonth(), 1)
      currentView.value = 'month'
      break
    case 'nextMonth':
      newDate.setMonth(today.getMonth() + 1)
      currentDate.value = newDate
      currentView.value = 'month'
      break
  }
}

const createEventAt = (date, hour) => {
  selectedEvent.value = null
  eventForm.value = {
    title: '',
    date,
    startTime: `${hour.toString().padStart(2, '0')}:00`,
    endTime: `${(hour + 1).toString().padStart(2, '0')}:00`,
    type: 'work',
    priority: 'medium',
    calendarId: 'work',
    description: '',
    repeat: {
      enabled: false,
      frequency: 'weekly'
    },
    participants: []
  }
  showEventDialog.value = true
}

const openEventDetails = (event) => {
  selectedEvent.value = event
  eventForm.value = {
    ...event,
    date: event.date,
    startTime: `${event.startHour.toString().padStart(2, '0')}:${event.startMinute.toString().padStart(2, '0')}`,
    endTime: `${(event.startHour + Math.floor(event.duration / 60)).toString().padStart(2, '0')}:${((event.startMinute + event.duration % 60) % 60).toString().padStart(2, '0')}`,
    repeat: event.repeat || { enabled: false, frequency: 'weekly' }
  }
  showEventDialog.value = true
}

const openQuickCreate = () => {
  showQuickCreateDialog.value = true
  const today = new Date()
  quickForm.value.date = today.toISOString().split('T')[0]
  quickForm.value.time = `${(today.getHours() + 1).toString().padStart(2, '0')}:00`
}

const quickCreate = (type) => {
  showQuickCreateDialog.value = true
  selectQuickOption(quickCreateOptions.find(opt => opt.type === type))
}

const selectQuickOption = (option) => {
  selectedQuickType.value = option.type
  if (!quickForm.value.title) {
    quickForm.value.title = option.defaultTitle
  }
}

const resetQuickForm = () => {
  selectedQuickType.value = ''
  const today = new Date()
  quickForm.value = {
    title: '',
    date: today.toISOString().split('T')[0],
    time: `${(today.getHours() + 1).toString().padStart(2, '0')}:00`,
    meetingLink: '',
    description: ''
  }
}

const handleSaveEvent = () => {
  const eventData = {
    id: selectedEvent.value?.id || Date.now().toString(),
    ...eventForm.value,
    startHour: parseInt(eventForm.value.startTime.split(':')[0]),
    startMinute: parseInt(eventForm.value.startTime.split(':')[1]),
    duration: calculateDuration(eventForm.value.startTime, eventForm.value.endTime),
    timeRange: `${eventForm.value.startTime} - ${eventForm.value.endTime}`,
    status: 'upcoming'
  }

  if (selectedEvent.value) {
    // 更新现有事件
    const index = events.value.findIndex(e => e.id === selectedEvent.value.id)
    if (index !== -1) {
      events.value[index] = eventData
    }
  } else {
    // 添加新事件
    events.value.push(eventData)
  }

  console.log('保存日程:', eventData)
  closeEventDialog()
}

const handleDeleteEvent = () => {
  if (selectedEvent.value && confirm('确定要删除这个日程吗？')) {
    events.value = events.value.filter(e => e.id !== selectedEvent.value.id)
    console.log('删除日程:', selectedEvent.value.id)
  }
  closeEventDialog()
}

const handleQuickCreate = () => {
  if (!selectedQuickType.value || !quickForm.value.title) return

  const eventData = {
    id: Date.now().toString(),
    title: quickForm.value.title,
    date: quickForm.value.date,
    startHour: parseInt(quickForm.value.time.split(':')[0]),
    startMinute: parseInt(quickForm.value.time.split(':')[1]),
    duration: 60,
    type: selectedQuickType.value,
    priority: 'medium',
    status: 'upcoming',
    timeRange: `${quickForm.value.time} - ${(parseInt(quickForm.value.time.split(':')[0]) + 1).toString().padStart(2, '0')}:${quickForm.value.time.split(':')[1]}`
  }

  events.value.push(eventData)
  console.log('快速创建:', eventData)
  closeQuickCreateDialog()
}

const closeEventDialog = () => {
  showEventDialog.value = false
  selectedEvent.value = null
  eventForm.value = {
    title: '',
    date: '',
    startTime: '09:00',
    endTime: '10:00',
    type: 'work',
    priority: 'medium',
    calendarId: 'work',
    description: '',
    repeat: {
      enabled: false,
      frequency: 'weekly'
    },
    participants: []
  }
}

const closeQuickCreateDialog = () => {
  showQuickCreateDialog.value = false
  resetQuickForm()
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}

const markAllAsRead = () => {
  unreadNotifications.value = 0
}

const toggleCalendar = (calendarId) => {
  const calendar = calendars.find(c => c.id === calendarId)
  if (calendar) {
    calendar.visible = !calendar.visible
  }
}

const handleSearch = () => {
  console.log('搜索:', searchQuery.value)
}

const clearSearch = () => {
  searchQuery.value = ''
}

const logout = () => {
  console.log('退出登录')
}

const calculateDuration = (startTime, endTime) => {
  const [startHour, startMinute] = startTime.split(':').map(Number)
  const [endHour, endMinute] = endTime.split(':').map(Number)
  return (endHour - startHour) * 60 + (endMinute - startMinute)
}

// 监听器
watch(currentView, (newView) => {
  console.log('切换到视图:', newView)
})

onMounted(() => {

  // 初始化表单日期
  const today = new Date()
  eventForm.value.date = today.toISOString().split('T')[0]
  quickForm.value.date = today.toISOString().split('T')[0]

  // 监听点击外部关闭通知面板
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.notification-area')) {
      showNotifications.value = false
    }
  })
})
</script>

<style scoped>
/* 自定义滚动条 */
.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 3px;
}

.dark .scrollbar-thin::-webkit-scrollbar-thumb {
  background: #4B5563;
}

.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

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

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.animate-slide-in {
  animation: slideIn 0.3s ease-out;
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* 渐变文字 */
.gradient-text {
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .grid-cols-8 {
    grid-template-columns: 60px repeat(7, 1fr);
  }

  .col-span-3, .col-span-9 {
    grid-column: span 12;
  }
}
</style>
