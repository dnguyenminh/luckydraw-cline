/**
 * Environment configuration interface
 */
export interface Environment {
  production: boolean;
  apiUrl: string;
  baseHref: string;
  enableDebugTools?: boolean;
  logLevel?: 'debug' | 'info' | 'warn' | 'error';
}