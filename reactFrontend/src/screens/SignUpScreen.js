import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
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
  <SafeAreaView style={sharedStyles.safeArea}>
        <ScrollView contentContainerStyle={sharedStyles.centeredScrollContainer}>
          <View style={{ alignItems: 'center', width: '100%' }}>
            
            <Image 
              source={require('../../assets/logo.png')} 
              style={sharedStyles.logo}
              resizeMode="contain"
            />

            <Text style={sharedStyles.title}>CREATE ACCOUNT</Text>
            
            <View style={{ width: '100%', marginTop: 40 }}>
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
                secureTextEntry={true} // Changed to true for security
                value={password}
                onChangeText={setPassword}
              />

              <View style={{ marginTop: 20 }}>
                {loading ? (
                  <ActivityIndicator color={COLORS.primary} size="large" />
                ) : (
                  <AppButton 
                    title="GET VERIFICATION CODE" 
                    onPress={handleGetCode} 
                  />
                )}
              </View>

              <View style={{ flexDirection: 'row', marginTop: 30, justifyContent: 'center', alignItems: 'center' }}>
                <Text style={{ color: COLORS.text, fontSize: 14, opacity: 0.7 }}>
                  Already have an account? 
                </Text>
                <TouchableOpacity onPress={() => navigation.navigate('Login')}>
                  <Text style={{ color: COLORS.primary, fontWeight: 'bold', fontSize: 16, marginLeft: 4 }}>
                    Login
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    );
  }