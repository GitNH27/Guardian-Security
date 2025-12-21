import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { COLORS, SPACING } from '../styles/theme';
import AppInput from '../components/AppInput';
import AppButton from '../components/AppButton';
import { authService } from '../services/authService';

export default function SignUpScreen({ navigation }) {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

const handleGetCode = async () => {
  if (!firstName || !lastName || !email || !password) {
    Alert.alert("Error", "Please fill in all fields");
    return;
  }

  setLoading(true);
  try {
    // Wrap your state into a DTO object
    const registerRequest = {
      firstName: String(firstName).trim(),
      lastName: String(lastName).trim(),
      email: String(email).trim().toLowerCase(),
      password: String(password).trim(),
    };

    // Pass the SINGLE object to the service
    await authService.sendVerificationCode(registerRequest);
    
    // Pass the same object to the next screen for step 2
    navigation.navigate('Verify', { userData: registerRequest });
    
  } catch (error) {
    Alert.alert("Registration Failed", error.message);
  } finally {
    setLoading(false);
  }
};

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <View style={styles.container}>

          {/* Top Center Logo */}
          <Image 
            source={require('../../assets/logo.png')} 
            style={styles.logo}
            resizeMode="contain"
          />

          <Text style={styles.title}>CREATE ACCOUNT</Text>
          
          <View style={styles.form}>
            <AppInput 
              icon="person-outline" 
              placeholder="First Name" 
              value={firstName}
              onChangeText={setFirstName}
            />
            <AppInput 
              icon="person-outline" 
              placeholder="Last Name" 
              value={lastName}
              onChangeText={setLastName}
            />
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
              secureTextEntry={false} // Typing is now visible
              value={password}
              onChangeText={setPassword}
            />

            <View style={{ marginTop: 20 }}>
              {/* 5. Show loading spinner while API is working */}
              {loading ? (
                <ActivityIndicator color={COLORS.primary} size="large" />
              ) : (
                <AppButton 
                  title="GET VERIFICATION CODE" 
                  onPress={handleGetCode} 
                />
              )}
            </View>

            <TouchableOpacity 
              style={styles.footerLink} 
              onPress={() => navigation.navigate('Login')}
            >
              <Text style={styles.footerText}>
                Already have an account? <Text style={styles.goldText}>Login</Text>
              </Text>
            </TouchableOpacity>
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
  footerLink: { marginTop: 30, alignItems: 'center' },
  footerText: { color: COLORS.text, fontSize: 14 },
  goldText: { color: COLORS.primary, fontWeight: 'bold' }
});