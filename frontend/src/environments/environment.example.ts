// Copy this file to environment.ts and environment.prod.ts
// and update the values according to your environment

export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api', // Update with your API URL
  auth: {
    tokenStorageKey: 'accessToken',
    refreshTokenStorageKey: 'refreshToken',
    rolesStorageKey: 'roles',
    userStorageKey: 'user',
    tokenExpiryKey: 'tokenExpiry'
  },
  defaultLanguage: 'en',
  supportedLanguages: ['en', 'vi'],
  pagination: {
    defaultPageSize: 10,
    pageSizeOptions: [5, 10, 25, 50, 100]
  },
  fileUpload: {
    maxSize: 5 * 1024 * 1024, // 5MB, adjust as needed
    allowedTypes: [
      'image/jpeg',
      'image/png',
      'image/gif',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ],
    maxConcurrent: 3
  },
  alerts: {
    defaultDuration: 5000,
    position: 'top-right', // Accepts: top-right, top-left, bottom-right, bottom-left
    maxAlerts: 5
  },
  dateFormat: 'dd/MM/yyyy', // Adjust according to locale
  timeFormat: 'HH:mm',      // Adjust according to locale
  timezone: 'Asia/Saigon',  // Set your timezone
  enableDebugTools: true,   // Set to false in production
  logLevel: 'debug',        // debug, info, warn, error
  caching: {
    defaultTTL: 300,  // 5 minutes
    userTTL: 3600,    // 1 hour
    staticTTL: 86400  // 24 hours
  },
  security: {
    passwordMinLength: 8,
    requireUppercase: true,
    requireLowercase: true,
    requireNumbers: true,
    requireSpecialChars: true
  }
};