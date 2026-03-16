import apiClient from '../api/client';
import messaging from '@react-native-firebase/messaging';
import * as SecureStore from 'expo-secure-store';

const THROTTLE_LIMIT = 30000; // 30 seconds
const SILENCE_KEY = 'notification_silence_active';
const LAST_NOTIFICATION_TIME_KEY = 'last_notification_time';

let isProcessing = false; // In-memory lock — synchronous, no race condition

// --- SILENCE CONTROL (called by LiveScreen & FullStreamScreen) ---

export const setNotificationSilence = async (isSilent) => {
  await SecureStore.setItemAsync(SILENCE_KEY, isSilent ? 'true' : 'false');
  console.log(`[Notification Service] Silence Mode: ${isSilent}`);
};

// --- GATE: Runs before every foreground & background notification ---

const shouldProcessNotification = async () => {
  // 1. Synchronous lock — if another notification is mid-check, reject immediately
  if (isProcessing) {
    console.log("[Throttled] Rejected — another notification is being processed");
    return false;
  }

  isProcessing = true; // Claim the lock before any await

  try {
    // 2. Silence check (persisted — survives background JS context)
    const silenced = await SecureStore.getItemAsync(SILENCE_KEY);
    if (silenced === 'true') {
      console.log("[Notification Ignored] User is in a Silent Zone");
      return false;
    }

    // 3. 30-second throttle check (persisted for background context)
    const lastTimeStr = await SecureStore.getItemAsync(LAST_NOTIFICATION_TIME_KEY);
    const lastTime = lastTimeStr ? parseInt(lastTimeStr) : 0;
    const now = Date.now();

    if (now - lastTime < THROTTLE_LIMIT) {
      console.log("[Throttled] Notification ignored (limit: 30s)");
      return false;
    }

    await SecureStore.setItemAsync(LAST_NOTIFICATION_TIME_KEY, String(now));
    console.log("[Notification Allowed]");
    return true;

  } finally {
    isProcessing = false; // Always release the lock, even if SecureStore throws
  }
};

// --- REGISTRATION ---

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

// --- FCM TOKEN REFRESH ---

export const setupFCMTokenRefreshListener = (userJwt) => {
  return messaging().onTokenRefresh(token => {
    console.log("FCM Token Refreshed:", token);
    syncFcmTokenWithBackend(token, userJwt);
  });
};

// --- LISTENERS ---

export const setupNotificationHandlers = () => {
  // Foreground Listener
  const unsubscribeForeground = messaging().onMessage(async remoteMessage => {
    if (!await shouldProcessNotification()) return;

    console.log('New Foreground Notification:', remoteMessage.data);
    // Trigger your Alert, Sound, or UI update here
  });

  // Background/Quit State handler (Must be outside any component)
  messaging().setBackgroundMessageHandler(async remoteMessage => {
    if (!await shouldProcessNotification()) return;

    console.log('Handling Background Message:', remoteMessage.data);
    return Promise.resolve();
  });

  return unsubscribeForeground;
};

// --- BACKEND SYNC ---

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