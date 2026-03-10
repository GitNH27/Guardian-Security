import apiClient from '../api/client';
import messaging from '@react-native-firebase/messaging';
import * as SecureStore from 'expo-secure-store';

let lastNotificationTime = 0;
const THROTTLE_LIMIT = 30000; // 30 seconds

const shouldProcessNotification = () => {
  const now = Date.now();

  if (now - lastNotificationTime < THROTTLE_LIMIT) {
    console.log("[Throttled] Notification ignored");
    return false;
  }

  lastNotificationTime = now;
  console.log("[Notification Allowed]");
  return true;
};

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

// FCM Token Refresh
export const setupFCMTokenRefreshListener = (userJwt) => {
  return messaging().onTokenRefresh(token => {
    console.log("FCM Token Refreshed:", token);
    // Ensure you pass the JWT if syncing immediately
    syncFcmTokenWithBackend(token, userJwt); 
  });
};

/**
 * LISTENERS: This is where the 30s limit is enforced
 */
export const setupNotificationHandlers = () => {
  // Foreground Listener
  const unsubscribeForeground = messaging().onMessage(async remoteMessage => {
    if (!shouldProcessNotification()) return;

    console.log('New Foreground Notification:', remoteMessage.data);
    // Trigger your Alert, Sound, or UI update here
  });

  // Background/Quit State handler (Must be outside any component)
  messaging().setBackgroundMessageHandler(async remoteMessage => {
    if (!shouldProcessNotification()) return;
    
    console.log('Handling Background Message:', remoteMessage.data);
    // Note: Background handlers should return a promise
    return Promise.resolve();
  });

  return unsubscribeForeground;
};

export const syncFcmTokenWithBackend = async (fcmToken, userJwt) => {
  if (!userJwt) return;
  
  try {
    const data = {
      token: fcmToken,
      deviceName: "Mobile Device" 
    };

    const config = {
      headers: {
        'Authorization': `Bearer ${userJwt}`
      }
    };

    const response = await apiClient.post('/auth/register-fcm', data, config);
    console.log("Database synced with FCM Token");
    return response.data;

  } catch (error) {
    const serverMessage = error.response?.data?.message || error.message;
    console.error("Sync failed:", serverMessage);
  }
};