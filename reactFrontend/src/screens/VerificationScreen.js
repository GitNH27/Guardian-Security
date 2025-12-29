import React, { useState } from 'react';
import { View, Text, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import AppInput from '../components/AppInput';
import AppButton from '../components/AppButton';
import { authService } from '../services/authService';

export default function VerificationScreen({ route, navigation }) {
  // We get the userData (email, etc.) passed from the SignUpScreen
  const { userData } = route.params || {};
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  const handleVerify = async () => {
    if (!code) {
      Alert.alert("Error", "Please enter the verification code.");
      return;
    }

    console.log("Sending to Backend:", userData);

    setLoading(true);
    try {
      // Calls @PostMapping("/verify-registration") on your Spring Boot backend
      await authService.verifyRegistration(userData, code);
      
      Alert.alert(
        "Success", 
        "Registration successful! Please log in.",
        [{ text: "OK", onPress: () => navigation.navigate('Login') }]
      );
    } catch (error) {
        // Use the message you normalized in your authService
        Alert.alert("Verification Failed", error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      <View style={[sharedStyles.container, { justifyContent: 'center', alignItems: 'center' }]}>
        <Text style={sharedStyles.title}>VERIFY ACCOUNT</Text>
        <Text style={[sharedStyles.subtitle, { textAlign: 'center', marginTop: SPACING.s }]}>
          Enter the code sent to your email
        </Text>

        <View style={{ width: '100%', marginTop: SPACING.xl }}>
          <AppInput 
            icon="key-outline"
            placeholder="Verification Code" 
            value={code}
            onChangeText={setCode}
            keyboardType="number-pad"
          />

          <View style={{ marginTop: SPACING.m }}>
            {loading ? (
              <ActivityIndicator color={COLORS.primary} size="large" />
            ) : (
              <AppButton 
                title="VERIFY & REGISTER" 
                onPress={handleVerify} 
              />
            )}
          </View>
        </View>
      </View>
    </SafeAreaView>
  );
}