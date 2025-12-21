import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import AppButton from '../components/AppButton';
import { COLORS } from '../styles/theme';

export default function HomeScreen({ navigation }) {
  const [name, setName] = useState('');

  useEffect(() => {
    const loadProfile = async () => {
      const data = await SecureStore.getItemAsync('userData');
      if (data) {
        const user = JSON.parse(data);
        setName(user.firstName);
      }
    };
    loadProfile();
  }, []);

  const handleLogout = async () => {
    await SecureStore.deleteItemAsync('userToken');
    await SecureStore.deleteItemAsync('userData');
    navigation.replace('Login');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Welcome Home, {name}!</Text>
      <AppButton title="LOGOUT" onPress={handleLogout} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: COLORS.background },
  text: { color: COLORS.text, fontSize: 20, marginBottom: 20 }
});