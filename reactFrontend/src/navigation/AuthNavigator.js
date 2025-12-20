// src/navigation/AuthNavigator.js
import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import LoginScreen from '../screens/LoginScreen';
import SignUpScreen from '../screens/SignUpScreen';
// import VerificationScreen from '../screens/VerificationScreen'; // We will create this next

const Stack = createStackNavigator();

export default function AuthNavigator() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false, // Hides the default white header bar
        cardStyle: { backgroundColor: '#2C2C2C' } // Matches your DarkBackground
      }}
    >
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="SignUp" component={SignUpScreen} />
      {/* <Stack.Screen name="Verify" component={VerificationScreen} /> */}
    </Stack.Navigator>
  );
}