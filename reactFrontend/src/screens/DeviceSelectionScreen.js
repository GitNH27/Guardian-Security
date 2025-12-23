import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import * as SecureStore from 'expo-secure-store';
import { COLORS, SPACING } from '../styles/theme';

export default function DeviceSelectionScreen({ route, navigation }) {
  // If we came from Login, devices are in route.params. 
  // If we came from Home, we might need a useEffect to fetch them.
  const devices = route.params?.devices || [];

  const handleDeviceSelect = async (device) => {
    try {
      // 1. Update the 'Active' device context in storage
      await SecureStore.setItemAsync('activeDevice', JSON.stringify(device));
      
      // 2. Redirect to Home which will now load this specific device
      navigation.replace('Home');
    } catch (e) {
      console.error("Failed to switch device", e);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Ionicons name="arrow-back" size={28} color={COLORS.text} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>SWITCH VEHICLE</Text>
        <View style={{ width: 28 }} /> {/* Spacing for alignment */}
      </View>

      <ScrollView contentContainerStyle={styles.list}>
        <Text style={styles.subtitle}>Select the device you wish to monitor:</Text>
        
        {devices.map((item) => (
          <TouchableOpacity 
            key={item.deviceId} 
            style={styles.deviceButton} 
            onPress={() => handleDeviceSelect(item)}
          >
            <View style={styles.iconCircle}>
              <Ionicons name="car-sport" size={24} color={COLORS.primary} />
            </View>
            <View style={styles.info}>
              <Text style={styles.serialText}>{item.serialNumber}</Text>
              <Text style={styles.roleText}>{item.role}</Text>
            </View>
            <Ionicons name="chevron-forward" size={20} color={COLORS.textHint} />
          </TouchableOpacity>
        ))}

        <TouchableOpacity 
          style={styles.addButton} 
          onPress={() => navigation.navigate('DeviceManagement')}
        >
          <Ionicons name="add-circle-outline" size={20} color={COLORS.primary} />
          <Text style={styles.addButtonText}>ADD NEW DEVICE</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  header: { 
    flexDirection: 'row', 
    alignItems: 'center', 
    justifyContent: 'space-between', 
    padding: SPACING.l 
  },
  headerTitle: { color: COLORS.text, fontSize: 18, fontWeight: 'bold', letterSpacing: 1 },
  list: { padding: SPACING.l },
  subtitle: { color: COLORS.textHint, marginBottom: 20 },
  deviceButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#1A1A1A',
    padding: 15,
    borderRadius: 12,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: '#333'
  },
  iconCircle: {
    width: 45,
    height: 45,
    borderRadius: 22.5,
    backgroundColor: 'rgba(212, 175, 55, 0.1)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15
  },
  info: { flex: 1 },
  serialText: { color: COLORS.text, fontWeight: 'bold', fontSize: 16 },
  roleText: { color: COLORS.primary, fontSize: 12, marginTop: 2, fontWeight: '600' },
  addButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 10,
    padding: 15
  },
  addButtonText: { color: COLORS.primary, marginLeft: 8, fontWeight: 'bold' }
});