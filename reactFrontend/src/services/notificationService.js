import apiClient from '../api/client';
import messaging from '@react-native-firebase/messaging';
import * as SecureStore from 'expo-secure-store';

const THROTTLE_LIMIT = 30000; // 30 seconds
const SILENCE_KEY = 'notification_silence_active';
const LAST_NOTIFICATION_TIME_KEY = 'last_notification_time';

// --- IN-MEMORY STATE (synchronous, no await needed) ---
let isProcessing = false;
let inMemorySilence = false;
let inMemoryLastTime = 0;

// --- SILENCE CONTROL (called by LiveScreen & FullStreamScreen) ---

export const setNotificationSilence = async (isSilent) => {
  inMemorySilence = isSilent; // Synchronous — instant effect
  await SecureStore.setItemAsync(SILENCE_KEY, isSilent ? 'true' : 'false'); // Persisted for background context
  console.log(`[Notification Service] Silence Mode: ${isSilent}`);
};

// --- GATE: Runs before every foreground & background notification ---

const shouldProcessNotification = async () => {
  // 1. Synchronous lock — rejects entire spam burst instantly, no await
  if (isProcessing) {
    console.log("[Throttled] Rejected — another notification is being processed");
    return false;
  }

  isProcessing = true; // Claim lock before any await

  try {
    // 2. In-memory silence check (synchronous — survives WebSocket spam)
    if (inMemorySilence) {
      console.log("[Ignored] Silenced in-memory");
      return false;
    }

    // 3. In-memory throttle check (synchronous — no SecureStore needed)
    const now = Date.now();
    if (now - inMemoryLastTime < THROTTLE_LIMIT) {
      console.log("[Throttled] In-memory throttle hit");
      return false;
    }

    // 4. Persisted silence check (catches fresh background JS context where inMemorySilence resets)
    const silenced = await SecureStore.getItemAsync(SILENCE_KEY);
    if (silenced === 'true') {
      console.log("[Ignored] Silenced via SecureStore (background context)");
      return false;
    }

    // 5. Persisted throttle check (catches fresh background JS context where inMemoryLastTime resets)
    const lastTimeStr = await SecureStore.getItemAsync(LAST_NOTIFICATION_TIME_KEY);
    const lastTime = lastTimeStr ? parseInt(lastTimeStr) : 0;
    if (now - lastTime < THROTTLE_LIMIT) {
      console.log("[Throttled] Persisted throttle hit (background context)");
      return false;
    }

    // 6. Commit both timestamps — in-memory first (synchronous), then persisted
    inMemoryLastTime = now;
    await SecureStore.setItemAsync(LAST_NOTIFICATION_TIME_KEY, String(now));

    console.log("[Notification Allowed]");
    return true;

  } finally {
    isProcessing = false; // Always release lock, even if SecureStore throws
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