import React, { useState } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { COLORS, SPACING } from '../styles/theme';
import AppInput from '../components/AppInput';
import AppButton from '../components/AppButton';

export default function LoginScreen({ navigation}) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    if (!email || !password) {
      alert("Email and password are required.");
      return;
    }
    console.log("Attempting login for:", email);
    // Logic for your Spring Boot API will go here later
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <View style={styles.container}>
          
          {/* Logo Section */}
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

            <View style={{ marginTop: 10 }}>
              <AppButton 
                title="LOG IN" 
                onPress={handleLogin} 
              />
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