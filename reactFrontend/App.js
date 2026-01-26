import React from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import AuthNavigator from './src/navigation/AuthNavigator'; // Check this path!
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';

import messaging from '@react-native-firebase/messaging';
import { Alert } from 'react-native';

export default function App() {
  useEffect(() => {
    const unsubscribe = messaging().onMessage(async (remoteMessage) => {
      Alert.alert(
        remoteMessage.notification?.title || "Security Alert",
        remoteMessage.notification?.body || "A new threat was detected."
      );
    });

    messaging().onNotificationOpenedApp((remoteMessage) => {
      console.log('Notification caused app to open from background:', remoteMessage.data);
      // Logic to navigate to ActivityLogScreen goes here
      navigator.navigate('ActivityLogScreen');
    });

    messaging()
      .getInitialNotification()
      .then((remoteMessage) => {
        if (remoteMessage) {
          console.log('Notification caused app to open from quit state:', remoteMessage.data);
        }
      });

    return unsubscribe;
  }, []);

  return (
    <SafeAreaProvider>
      <StatusBar style="light" />
      <NavigationContainer>
        <AuthNavigator />
      </NavigationContainer>
    </SafeAreaProvider>
  );
}