<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800 flex flex-col">
    <!-- Header -->
    <header class="glass rounded-xl mx-4 mt-4 p-4 flex items-center justify-between animate-fade-in">
      <div class="flex items-center">
        <CalendarIcon class="w-8 h-8 text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-white">日历应用</h1>
      </div>

      <div class="flex items-center flex-1 justify-center max-w-xl">
        <div class="relative w-full">
          <SearchIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索日程、事件..."
            class="w-full bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl py-3 px-4 pl-10 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-transparent transition-all duration-300"
          />
        </div>
      </div>

      <div class="flex items-center space-x-4">
        <button @click="toggleTheme" class="p-2 rounded-full hover:bg-white/10 transition-colors">
          <SunIcon v-if="isDarkMode" class="w-5 h-5 text-yellow-400" />
          <MoonIcon v-else class="w-5 h-5 text-gray-300" />
        </button>

        <div class="relative group cursor-pointer">
          <img
            src="https://randomuser.me/api/portraits/women/44.jpg"
            alt="User Avatar"
            class="w-10 h-10 rounded-full object-cover border-2 border-white/30 transition-transform group-hover:scale-105"
          />
          <span class="absolute -top-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-gray-800"></span>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 flex p-4 gap-3 mt-4">
      <!-- Sidebar -->
      <aside class="w-64 glass rounded-xl p-4 flex flex-col animate-fade-in-delay-1">
        <!-- Create Button -->
        <button
          @click="showCreateDialog = true"
          class="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-medium py-3 px-4 rounded-xl flex items-center justify-center gap-2 transition-all duration-300 mb-6 shadow-lg hover:shadow-xl"
        >
          <PlusIcon class="w-5 h-5" />
          <span>创建日程</span>
        </button>

        <!-- Mini Calendar -->
        <div class="sidebar-calendar mb-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="font-semibold text-white text-lg">{{ miniCalendarTitle }}</h3>
            <div class="flex gap-1">
              <button
                @click="prevMonth"
                class="p-2 rounded-lg hover:bg-white/10 transition-colors"
              >
                <ChevronLeftIcon class="w-4 h-4 text-white/70" />
              </button>
              <button
                @click="nextMonth"
                class="p-2 rounded-lg hover:bg-white/10 transition-colors"
              >
                <ChevronRightIcon class="w-4 h-4 text-white/70" />
              </button>
            </div>
          </div>

          <!-- Week Days -->
          <div class="grid grid-cols-7 gap-1 mb-2 text-center text-sm text-white/70">
            <div v-for="day in weekDays" :key="day" class="py-1">{{ day }}</div>
          </div>

          <!-- Days Grid -->
          <div class="grid grid-cols-7 gap-1 text-center">
            <div
              v-for="day in miniCalendarDays"
              :key="day.date"
              :class="[
                'py-2 rounded-lg transition-colors cursor-pointer',
                {
                  'bg-blue-500/20 text-blue-300': day.isToday,
                  'hover:bg-white/10': !day.isEmpty && !day.isToday,
                  'text-white/30': day.isEmpty,
                  'text-white': !day.isEmpty
                }
              ]"
              @click="selectDate(day.date)"
            >
              {{ day.display }}
            </div>
          </div>
        </div>

        <!-- My Calendars -->
        <div>
          <h3 class="font-semibold text-white text-lg mb-3">我的日历</h3>
          <ul class="space-y-2">
            <li
              v-for="calendar in calendars"
              :key="calendar.id"
              @click="toggleCalendar(calendar.id)"
              class="flex items-center justify-between p-2 rounded-lg hover:bg-white/5 transition-colors cursor-pointer group"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-3 h-3 rounded-full"
                  :style="{ backgroundColor: calendar.color }"
                ></div>
                <span class="text-white/90">{{ calendar.name }}</span>
              </div>
              <div class="flex items-center gap-2">
                <div
                  v-if="calendar.eventCount > 0"
                  class="text-xs bg-white/10 text-white/70 px-2 py-1 rounded"
                >
                  {{ calendar.eventCount }}
                </div>
                <ChevronRightIcon class="w-4 h-4 text-white/50 group-hover:translate-x-1 transition-transform" />
              </div>
            </li>
          </ul>
        </div>
      </aside>

      <!-- Calendar View -->
      <section class="flex-1 glass rounded-xl p-6 animate-fade-in-delay-2">
        <!-- View Switcher and Navigation -->
        <div class="flex justify-between items-center mb-6">
          <div class="flex items-center gap-2">
            <button
              v-for="view in views"
              :key="view.id"
              @click="currentView = view.id"
              :class="[
                'px-4 py-2 rounded-lg font-medium transition-all duration-300',
                currentView === view.id
                  ? 'bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md'
                  : 'bg-white/10 text-white/70 hover:bg-white/20'
              ]"
            >
              {{ view.name }}
            </button>
          </div>

          <div class="flex items-center gap-4">
            <button
              @click="navigateTime(-1)"
              class="p-2 rounded-full hover:bg-white/10 transition-colors"
            >
              <ChevronLeftIcon class="w-5 h-5 text-white" />
            </button>
            <h2 class="text-xl font-semibold text-white">{{ currentRangeText }}</h2>
            <button
              @click="navigateTime(1)"
              class="p-2 rounded-full hover:bg-white/10 transition-colors"
            >
              <ChevronRightIcon class="w-5 h-5 text-white" />
            </button>
            <button
              @click="goToToday"
              class="bg-white/10 hover:bg-white/20 text-white px-4 py-2 rounded-lg transition-colors"
            >
              今天
            </button>
          </div>
        </div>

        <!-- Week View -->
        <div class="week-view bg-white/5 rounded-xl overflow-hidden">
          <div class="grid grid-cols-8">
            <!-- Time Column -->
            <div class="time-column bg-white/5 p-2">
              <div
                v-for="timeSlot in timeSlots"
                :key="timeSlot"
                class="h-16 flex items-end justify-end pr-3 text-white/50 text-sm border-b border-white/10"
              >
                {{ timeSlot }}
              </div>
            </div>

            <!-- Day Columns -->
            <div
              v-for="day in weekDaysWithDates"
              :key="day.date"
              class="day-column border-l border-white/10"
            >
              <div class="text-center py-3 font-medium text-white border-b border-white/10">
                <div>{{ day.dayName }}</div>
                <div :class="[
                  'text-sm mx-auto w-8 h-8 flex items-center justify-center rounded-full mt-1',
                  day.isToday ? 'bg-blue-500 text-white' : 'text-white/70'
                ]">
                  {{ day.dayNumber }}
                </div>
              </div>

              <div class="relative">
                <div
                  v-for="slot in timeSlots"
                  :key="slot"
                  class="h-16 border-b border-white/10 hover:bg-white/2 transition-colors cursor-pointer"
                  @click="createEventAt(day.date, slot)"
                ></div>

                <!-- Events for this day -->
                <div
                  v-for="event in getEventsForDay(day.date)"
                  :key="event.id"
                  :style="{
                    top: `${(event.startHour - 8) * 64 + (event.startMinute / 60) * 64}px`,
                    height: `${event.duration * 64 / 60}px`
                  }"
                  class="absolute left-1 right-1 rounded-lg p-2 overflow-hidden shadow-md"
                  :class="`bg-gradient-to-r ${getEventColor(event.type)}`"
                >
                  <div class="text-xs font-semibold text-white truncate">{{ event.title }}</div>
                  <div class="text-xs text-white/80">{{ event.timeRange }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- Create Event Dialog -->
    <div v-if="showCreateDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-gray-800 rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
        <h3 class="text-xl font-bold text-white mb-4">创建新日程</h3>
        <!-- Dialog content here -->
        <div class="flex justify-end gap-3 mt-6">
          <button @click="showCreateDialog = false" class="px-4 py-2 rounded-lg text-white/70 hover:bg-white/10 transition-colors">
            取消
          </button>
          <button @click="saveEvent" class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors">
            保存
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  CalendarIcon,
  SunIcon,
  MoonIcon,
  PlusIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

// Reactive state
const searchQuery = ref('')
const isDarkMode = ref(true)
const currentView = ref('week')
const currentDate = ref(new Date())
const showCreateDialog = ref(false)

// Computed properties
const miniCalendarTitle = computed(() => {
  return currentDate.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
})

const currentRangeText = computed(() => {
  const start = new Date(currentDate.value)
  const end = new Date(currentDate.value)

  if (currentView.value === 'week') {
    start.setDate(start.getDate() - start.getDay() + 1)
    end.setDate(start.getDate() + 6)
    return `${formatDate(start)} - ${formatDate(end)}`
  }

  return formatDate(currentDate.value)
})

// Data
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const views = [
  { id: 'day', name: '日' },
  { id: 'week', name: '周' },
  { id: 'month', name: '月' }
]

const calendars = [
  { id: 'personal', name: '个人', color: '#3B82F6', eventCount: 3 },
  { id: 'work', name: '工作', color: '#10B981', eventCount: 5 },
  { id: 'family', name: '家庭', color: '#8B5CF6', eventCount: 2 }
]

const timeSlots = Array.from({ length: 10 }, (_, i) => `${i + 8}:00`)
const events = [
  { id: 1, title: '团队会议', date: '2025-07-21', startHour: 10, startMinute: 10, duration: 40, type: 'work' },
  { id: 2, title: '客户电话', date: '2025-07-21', startHour: 14, startMinute: 5, duration: 55, type: 'work' },
  { id: 3, title: '午餐约会', date: '2025-07-22', startHour: 12, startMinute: 0, duration: 60, type: 'personal' }
]

// Methods
const formatDate = (date) => {
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const getEventsForDay = (date) => {
  return events.filter(event => event.date === date).map(event => ({
    ...event,
    timeRange: `${event.startHour}:${event.startMinute.toString().padStart(2, '0')} - ${event.startHour + Math.floor(event.duration / 60)}:${(event.startMinute + event.duration % 60).toString().padStart(2, '0')}`
  }))
}

const getEventColor = (type) => {
  const colors = {
    work: 'from-blue-500/90 to-blue-600/90',
    personal: 'from-green-500/90 to-green-600/90',
    family: 'from-purple-500/90 to-purple-600/90'
  }
  return colors[type] || 'from-gray-500/90 to-gray-600/90'
}

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
}

const navigateTime = (direction) => {
  const newDate = new Date(currentDate.value)
  if (currentView.value === 'week') {
    newDate.setDate(newDate.getDate() + direction * 7)
  } else if (currentView.value === 'day') {
    newDate.setDate(newDate.getDate() + direction)
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

// Computed week days with dates
const weekDaysWithDates = computed(() => {
  const start = new Date(currentDate.value)
  start.setDate(start.getDate() - start.getDay() + 1) // Start from Monday

  return Array.from({ length: 7 }, (_, i) => {
    const date = new Date(start)
    date.setDate(start.getDate() + i)

    return {
      date: date.toISOString().split('T')[0],
      dayName: weekDays[(date.getDay()) % 7],
      dayNumber: date.getDate(),
      isToday: date.toDateString() === new Date().toDateString()
    }
  })
})

// Computed mini calendar days
const miniCalendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)

  const days = []

  // Empty cells for days before first day of month
  for (let i = 0; i < firstDay.getDay(); i++) {
    days.push({ display: '', isEmpty: true })
  }

  // Days of the month
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(year, month, i)
    days.push({
      display: i,
      date: date.toISOString().split('T')[0],
      isToday: date.toDateString() === new Date().toDateString(),
      isEmpty: false
    })
  }

  return days
})

const selectDate = (date) => {
  currentDate.value = new Date(date)
}

const createEventAt = (date, time) => {
  showCreateDialog.value = true
  console.log('Create event at:', date, time)
}

const saveEvent = () => {
  // Save event logic
  showCreateDialog.value = false
}

const toggleCalendar = (calendarId) => {
  console.log('Toggle calendar:', calendarId)
}

onMounted(() => {
  console.log('Calendar app mounted')
})
</script>

<style scoped>
.glass {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

.animate-fade-in-delay-1 {
  animation: fadeIn 0.5s ease-out 0.1s both;
}

.animate-fade-in-delay-2 {
  animation: fadeIn 0.5s ease-out 0.2s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.time-column {
  position: sticky;
  left: 0;
  z-index: 10;
}

.day-column {
  min-width: 0;
}

.event {
  transition: all 0.2s ease;
}

.event:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
</style>
