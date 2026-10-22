import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// La URL del backend sale de VITE_API_URL (.env.development / .env.production o variable de entorno al construir).
export default defineConfig({
  plugins: [react()],
});
