import React from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import * as SecureStore from 'expo-secure-store';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

export default function DeviceSelectionScreen({ route, navigation }) {
  const devices = route.params?.devices || [];

  const handleDeviceSelect = async (device) => {
    try {
      await SecureStore.setItemAsync('activeDeviceId', device.deviceId.toString());
      navigation.replace('Home');
    } catch (e) {
      console.error("Failed to switch device", e);
    }
  };

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      {/* Header - using sharedStyles + small inline adjustment */}
      <View style={[sharedStyles.header, { paddingHorizontal: 20 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Ionicons name="arrow-back" size={28} color={COLORS.text} />
        </TouchableOpacity>
        <Text style={sharedStyles.headerTitle}>SWITCH VEHICLE</Text>
        <View style={{ width: 28 }} />
      </View>

      <ScrollView contentContainerStyle={sharedStyles.scrollContainer}>
        <Text style={sharedStyles.subtitle}>Select the device you wish to monitor:</Text>
        
        {devices.map((item) => (
          <TouchableOpacity 
            key={item.deviceId?.toString()} 
            style={[sharedStyles.card, { flexDirection: 'row', alignItems: 'center', marginBottom: 15 }]} 
            onPress={() => handleDeviceSelect(item)}
          >
            {/* Inline style for the circle icon */}
            <View style={{
              width: 45, height: 45, borderRadius: 22.5,
              backgroundColor: 'rgba(212, 175, 55, 0.1)',
              justifyContent: 'center', alignItems: 'center', marginRight: 15
            }}>
              <Ionicons name="car-sport" size={24} color={COLORS.primary} />
            </View>

            <View style={{ flex: 1 }}>
              <Text style={{ color: COLORS.text, fontWeight: 'bold', fontSize: 16 }}>{item.serialNumber ? String(item.serialNumber) : 'Unknown Device'}</Text>
              <Text style={{ color: COLORS.primary, fontSize: 12, marginTop: 2, fontWeight: '600' }}>{item.role ? String(item.role) : 'USER'}</Text>
            </View>

            <Ionicons name="chevron-forward" size={20} color={COLORS.textHint} />
          </TouchableOpacity>
        ))}

        {/* Inline style for the Add Button */}
        <TouchableOpacity 
          style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginTop: 10, padding: 15 }} 
          onPress={() => navigation.navigate('DeviceManagement')}
        >
          <Ionicons name="add-circle-outline" size={20} color={COLORS.primary} />
          <Text style={{ color: COLORS.primary, marginLeft: 8, fontWeight: 'bold' }}>ADD NEW DEVICE</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}