import React, { useState } from 'react';
import { View, Text, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { COLORS, SPACING } from '../styles/theme';
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
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <Text style={styles.title}>VERIFY ACCOUNT</Text>
        <Text style={styles.subtitle}>
          Enter the code sent to your email
        </Text>

        <View style={styles.form}>
          <AppInput 
            icon="key-outline" // Matches the VpnKey icon from your Kotlin code
            placeholder="Verification Code" 
            value={code}
            onChangeText={setCode}
            keyboardType="number-pad" // Better UX for numeric codes
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

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: COLORS.background },
  container: { 
    flex: 1, 
    padding: SPACING.l, 
    alignItems: 'center', 
    justifyContent: 'center' 
  },
  title: { 
    fontSize: 22, 
    fontWeight: 'bold', 
    color: COLORS.text, 
    letterSpacing: 2 
  },
  subtitle: { 
    fontSize: 14, 
    color: COLORS.textHint, 
    marginTop: SPACING.s,
    marginBottom: SPACING.xl,
    textAlign: 'center'
  },
  form: { width: '100%' },
});