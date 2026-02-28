import axios from 'axios';
import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const apiClient = axios.create({
  // Use 10.0.2.2 for Android Emulator, localhost for iOS
  // School Wi-Fi - 10.216.124.196
  baseURL: 'https://guardian-backend-api-cehse7acc4bfhcdq.canadacentral-01.azurewebsites.net/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config) => {
    // 1. Check if the request is for an authentication route
    const isAuthRoute = config.url.startsWith('/auth');

    // 2. Only look for a token if it's NOT an auth route
    if (!isAuthRoute) {
      const token = await SecureStore.getItemAsync('userToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;