/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        'sans': ['Inter', 'system-ui', 'sans-serif'],
        'kai': ['KaiTi', '楷体', 'serif'],
        'display': ['Poppins', 'sans-serif'],
      },
      colors: {
        primary: '#4F46E5', // 现代化的主色调
        secondary: '#10B981', // 辅助色
        accent: '#F59E0B', // 强调色
        dark: {
          900: '#111827',
          800: '#1F2937',
          700: '#374151',
        },
        light: {
          50: '#F9FAFB',
          100: '#F3F4F6',
          200: '#E5E7EB',
        },
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-in-out',
        'slide-up': 'slideUp 0.5s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
      boxShadow: {
        'modern': '0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)',
        'hover': '0 20px 35px -10px rgba(0, 0, 0, 0.1), 0 10px 15px -5px rgba(0, 0, 0, 0.04)',
      },
    },
  },
  plugins: [],
}
