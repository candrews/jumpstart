import { defineConfig } from 'cypress'

export default defineConfig({
  "reporter": "cypress-multi-reporters",
  "reporterOptions": {
    "configFile": "reporter-config.json"
  },
  "screenshotsFolder": "../build/reports/cypress/screenshots",
  "videosFolder": "../build/reports/cypress/videos",
  e2e: {
    "baseUrl": "https://localhost:8443",
    "supportFile" : false,
  },
  "requestTimeout": 80000,
  "viewportHeight": 1080,
  "viewportWidth": 1920,
  "env": {
    "username": "user",
    "password": "password"
  }
})
