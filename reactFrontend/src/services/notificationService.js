import apiClient from '../api/client';
import messaging from '@react-native-firebase/messaging';
import * as SecureStore from 'expo-secure-store';

export const registerForPushNotifications = async () => {
    try {
    const authStatus = await messaging().requestPermission();
    const enabled =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    if (enabled) {
      const token = await messaging().getToken();
      console.log('FCM TOKEN: ', token);
      
      await SecureStore.setItemAsync('fcm_token', token);
      
      return token;
    }
  } catch (error) {
    console.error('Notification setup failed:', error);
  }
};

// FCM Token Refresh (Update database when token changes)
export const setupFCMTokenRefreshListener = () => {
    messaging().onTokenRefresh(token => {
        console.log("FCM Token Refreshed:", token);
        authService.updateFcmToken(token); 
    });
};

// Add this to your existing notification file
export const syncFcmTokenWithBackend = async (fcmToken, userJwt) => {
  try {
    // 1. Data Body (Second Argument)
    const data = {
      token: fcmToken,
      deviceName: "Android Emulator"
    };

    // 2. Config/Headers (Third Argument)
    const config = {
      headers: {
        'Authorization': `Bearer ${userJwt}`
      }
    };

    const response = await apiClient.post('/auth/register-fcm', data, config);

    // Axios considers 2xx status as success automatically
    console.log("Database synced with FCM Token");
    return response.data;

  } catch (error) {
    // Better error logging to see what the server says
    const serverMessage = error.response?.data?.message || error.message;
    console.error("Sync failed:", serverMessage);
  }
};