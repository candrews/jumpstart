import { defineConfig } from 'cypress'

export default defineConfig({
  "reporter": "cypress-multi-reporters",
  "requestTimeout": 80000,
  "screenshotsFolder": "../build/reports/cypress/screenshots",
  "videosFolder": "../build/reports/cypress/videos",
  "viewportHeight": 1080,
  "viewportWidth": 1920
  "e2e": {
    "baseUrl": "https://localhost:8443",
    "supportFile" : false,
  },
  "env": {
    "username": "user",
    "password": "password"
  },
  "reporterOptions": {
    "configFile": "reporter-config.json"
  }
})
