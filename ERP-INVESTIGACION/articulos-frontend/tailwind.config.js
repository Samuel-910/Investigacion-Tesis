/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        // Nueva paleta corporativa (Figma)
        'corp': {
          'primary': '#1E293B',      // Fondo principal
          'secondary': '#1D4ED8',    // Botón primario
          'dark': '#0F172A',         // Texto principal
          'gray-medium': '#475569',  // Texto secundario
          'gray-light': '#CBD5E1',   // Bordes/Inputs
          'gray-disabled': '#E5E7EB', // Deshabilitado
          'hover': '#1E40AF',        // Hover botón
        },
        // Colores legacy (mantener compatibilidad)
        'primary-blue': '#1e3a8a',
        'primary-green': '#16a34a',
        'primary-red': '#dc2626',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        }
      },
      maxWidth: {
        '8xl': '88rem',
        '9xl': '96rem',
        '10xl': '104rem',
        '11xl': '112rem',
        '12xl': '120rem',
      }
    },
  },
  plugins: [],
}
