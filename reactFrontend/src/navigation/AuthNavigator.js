// src/navigation/AuthNavigator.js
import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import LoginScreen from '../screens/LoginScreen';
import SignUpScreen from '../screens/SignUpScreen';
import VerificationScreen from '../screens/VerificationScreen';
import HomeScreen from '../screens/HomeScreen';
import DeviceSelectionScreen from '../screens/DeviceSelectionScreen';
import DeviceManagementScreen from '../screens/DeviceManagementScreen';
import ThreatLogScreen from '../screens/ThreatLogScreen';
import LiveScreen from '../screens/LiveScreen';
import FullScreenStream from '../screens/FullStreamScreen';
import UserSettingsScreen from '../screens/UserSettingsScreen';

const Stack = createStackNavigator();

export default function AuthNavigator() {
  return (
    <Stack.Navigator
    //   initialRouteName="Home" // <--- Add this temporarily
      screenOptions={{
        headerShown: false,
        cardStyle: { backgroundColor: '#2C2C2C' }
      }}
    >
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="SignUp" component={SignUpScreen} />
      <Stack.Screen name="Verify" component={VerificationScreen} />
      <Stack.Screen name="Home" component={HomeScreen} />
      <Stack.Screen name="DeviceManagement" component={DeviceManagementScreen} />
      <Stack.Screen name="DeviceSelectionScreen" component={DeviceSelectionScreen} />
      <Stack.Screen name="ThreatLogs" component={ThreatLogScreen} />
      <Stack.Screen name="Live" component={LiveScreen} />
      <Stack.Screen name="FullStreamScreen" component={FullScreenStream} options={{ headerShown: false }} />
      <Stack.Screen name="UserSettings" component={UserSettingsScreen} />
    </Stack.Navigator>
  );
}