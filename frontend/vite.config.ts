import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  build: {
    outDir: "build",
  },
  plugins: [react()],
  clearScreen: false,
  server: {
    port: 3000,
    proxy: {
      "^/(api)": {
        target: "https://localhost:8443",
      },
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: [path.resolve(__dirname, "src/vitestSetup.ts")],
    coverage: {
      enabled: true,
      reporter: ["html-spa", "text"],
      functions: 0,
      branches: 0,
      statements: 0,
    },
  },
});

