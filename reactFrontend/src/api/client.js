import axios from 'axios';
import { Platform } from 'react-native';

const apiClient = axios.create({
  // Use 10.0.2.2 for Android Emulator, localhost for iOS
  baseURL: Platform.OS === 'android' ? 'http://10.0.2.2:8080/api/auth' : 'http://localhost:8080/api/auth',
  headers: {
    'Content-Type': 'application/json',
  },
});

export default apiClient;