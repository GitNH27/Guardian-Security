import React, { useState } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, ScrollView, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as SecureStore from 'expo-secure-store'; // 1. Import SecureStore
import { COLORS, SPACING } from '../styles/theme';
import AppInput from '../components/AppInput';
import AppButton from '../components/AppButton';
import { authService } from '../services/authService';
import { deviceService } from '../services/deviceService';

export default function LoginScreen({ navigation }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert("Error", "Email and password are required.");
      return;
    }

    setLoading(true);
    try {
      const loginRequest = { 
        email: email.trim().toLowerCase(), 
        password: password 
      };
      
      const authResponse = await authService.login(loginRequest);

      // 1. Defensive Check: Ensure token exists and is a string
      if (authResponse && authResponse.token) {
        await SecureStore.setItemAsync('userToken', String(authResponse.token));
      } else {
        throw new Error("Server did not return an authentication token.");
      }
      
      // 2. Defensive Check: Ensure userResponse exists before stringifying
      if (authResponse && authResponse.userResponse) {
        await SecureStore.setItemAsync('userData', JSON.stringify(authResponse.userResponse));
        console.log("User Data Saved to SecureStore");
      } else {
        throw new Error("Server did not return user data.");
      }

      console.log("Login Successful, Token Saved!");

      // User Data
      console.log("User Data:", authResponse.userResponse);

      // ... inside handleLogin ...

      console.log("Fetching device selection context...");

      // CHANGE THIS LINE: Use deviceService instead of authService
      const deviceContextResponse = await deviceService.getDeviceSelectionContext();

      // Log the actual data to verify structure
      console.log("Device Selection Context:", deviceContextResponse);

      // Match the 'action' key from your Spring Boot Map response
      if (deviceContextResponse.action === "Auto-Redirect-Device") {
          // Save the specific device object returned by the backend
          await SecureStore.setItemAsync('activeDevice', JSON.stringify(deviceContextResponse.device));
          navigation.replace('Home');
      } 
      else if (deviceContextResponse.action === "SHOW_PICKER") {
          // Navigate to the selection screen with the list of devices
          navigation.replace('DeviceSelectionScreen', { devices: deviceContextResponse.devices });
      } 
      else {
          // Fallback for new users (REDIRECT_TO_HOME_NEW or similar)
          navigation.replace('DeviceManagement');
      }
      
    } catch (error) {
      // This will now catch both Network errors and SecureStore validation errors
      console.error("Login Handler Error:", error);
      Alert.alert("Login Failed", error.message || "An unexpected error occurred.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <View style={styles.container}>
          <Image 
            source={require('../../assets/logo.png')} 
            style={styles.logo}
            resizeMode="contain"
          />

          <Text style={styles.title}>LOG IN</Text>
          
          <View style={styles.form}>
            <AppInput 
              icon="mail-outline" 
              placeholder="Email" 
              keyboardType="email-address"
              value={email}
              onChangeText={setEmail}
            />
            
            <AppInput 
              icon="lock-closed-outline" 
              placeholder="Password" 
              secureTextEntry={true} 
              value={password}
              onChangeText={setPassword}
            />

            <View style={{ marginTop: 20 }}>
              {loading ? (
                <ActivityIndicator color={COLORS.primary} size="large" />
              ) : (
                <AppButton title="LOG IN" onPress={handleLogin} />
              )}
            </View>

            <View style={styles.footer}>
              <Text style={styles.footerText}>Don't have an account? </Text>
              <TouchableOpacity onPress={() => navigation.navigate('SignUp')}>
                <Text style={styles.goldText}>Register Now</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: COLORS.background },
  scrollContainer: { flexGrow: 1, justifyContent: 'center' },
  container: { padding: SPACING.l, alignItems: 'center' },
  logo: {
    width: 150,
    height: 150,
    marginBottom: SPACING.m,
  },
  title: { 
    fontSize: 22, 
    fontWeight: 'bold', 
    color: COLORS.text, 
    letterSpacing: 2,
    marginBottom: SPACING.xl 
  },
  form: { width: '100%' },
  footer: { 
    flexDirection: 'row', 
    marginTop: 30, 
    justifyContent: 'center',
    alignItems: 'center' 
  },
  footerText: { color: COLORS.text, fontSize: 14, opacity: 0.7 },
  goldText: { color: COLORS.primary, fontWeight: 'bold', fontSize: 16 }
});