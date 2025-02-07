export const environment = {
  production: true,
  apiUrl: '/api',
  auth: {
    loginUrl: '/auth/login',
    refreshTokenUrl: '/auth/refresh-token',
    tokenKey: 'access_token',
    refreshTokenKey: 'refresh_token'
  },
  defaultLanguage: 'vi',
  supportedLanguages: ['en', 'vi'],
  appName: 'Lucky Draw',
  appVersion: '1.0.0'
};