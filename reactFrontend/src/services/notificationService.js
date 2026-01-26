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