/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#00796B', // Deep Teal
          light: '#B2DFDB',   // Soft Mint
          dark: '#004D40',
        },
        background: '#F5F7FA', // Light Blue-Grey
        surface: '#FFFFFF',
        text: {
          primary: '#263238', // Dark Blue-Grey
          secondary: '#607D8B', // Blue-Grey
        },
        error: '#D32F2F',
        success: '#388E3C',
        warning: '#FFA000',
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
