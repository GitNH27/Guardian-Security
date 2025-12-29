import React, { useEffect, useState } from 'react';
import { View, Text, ScrollView, Alert, ActivityIndicator, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as SecureStore from 'expo-secure-store';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import DashboardCard from '../components/DashboardCard';
import HomeHeader from '../components/HomeHeader';
import { RequestDropdown } from '../components/RequestDropdown';
import { deviceService } from '../services/deviceService';

export default function HomeScreen({ navigation }) {
  const [userName, setUserName] = useState('User');
  const [threatLevel, setThreatLevel] = useState('Low');
  const [role, setRole] = useState('USER');
  const [hasDevice, setHasDevice] = useState(false);
  const [activeDevice, setActiveDevice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadSessionData = async () => {
      try {
        setLoading(true);
        const userValue = await SecureStore.getItemAsync('userData');
        if (userValue) {
          const user = JSON.parse(userValue);
          setUserName(user.firstName || 'User');
        }

        const deviceValue = await SecureStore.getItemAsync('activeDevice');
        if (deviceValue) {
          const device = JSON.parse(deviceValue);
          setActiveDevice(device);
          setRole(device.role || 'USER');
          setHasDevice(true);
        } else {
          setHasDevice(false);
          setActiveDevice(null);
          setRole('USER');
        }
      } catch (e) {
        console.error("Failed to load session data", e);
      } finally {
        setLoading(false);
      }
    };
    loadSessionData();
  }, []);

  const handleLogout = async () => {
    try {
      await SecureStore.deleteItemAsync('userToken');
      await SecureStore.deleteItemAsync('userData');
      await SecureStore.deleteItemAsync('activeDevice');
      navigation.replace('Login');
    } catch (e) {
      Alert.alert("Error", "Failed to logout safely.");
    }
  };

  const handleSwitchDevice = async () => {
    try {
      const response = await deviceService.getDeviceSelectionContext();
      navigation.navigate('DeviceSelectionScreen', { devices: response.devices });
    } catch (error) {
      Alert.alert("Error", "Could not load device list.");
    }
  };

  if (loading) {
    return (
      <SafeAreaView style={[sharedStyles.safeArea, { justifyContent: 'center' }]}>
        <ActivityIndicator size="large" color={COLORS.primary} />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      <ScrollView contentContainerStyle={{ padding: 20, flexGrow: 1 }}>
        
        {/* Modular Header */}
        <HomeHeader 
          userName={userName}
          activeDevice={activeDevice}
          hasDevice={hasDevice}
          role={role}
          onSwitchDevice={handleSwitchDevice}
        />

        {/* DASHBOARD STATUS BANNER (From Snippet 1) */}
        <View style={[sharedStyles.statusBanner, !hasDevice && { borderLeftColor: '#444' }]}>
          <Text style={{ color: COLORS.text, fontWeight: '600' }}>System Status: 
            <Text style={{ color: !hasDevice ? '#666' : (threatLevel === 'High' ? '#FF4444' : COLORS.primary) }}>
              {hasDevice ? " Guardian Active" : " Offline"}
            </Text>
          </Text>
        </View>

        {/* Main Grid using Modular Cards */}
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between' }}>
          <DashboardCard 
            title="User Settings" 
            icon="settings-outline" 
            onPress={() => navigation.navigate('Settings')} 
          />
          
          <DashboardCard 
            title="Threat Status" 
            icon="shield-checkmark-outline" 
            onPress={() => Alert.alert("Status", threatLevel)} 
            disabled={!hasDevice}
          />
          
          <DashboardCard 
            title="Activity Logs" 
            icon="list-outline" 
            onPress={() => navigation.navigate('Logs')} 
            disabled={!hasDevice}
          />
          
          <DashboardCard
            title="Live Feed"
            icon="videocam-outline"
            onPress={() => navigation.navigate('LiveView')}
            disabled={!hasDevice || threatLevel !== 'High'}
            color="#FF4444"
          />
        </View>

        {/* Device Management Section */}
        <View style={{ marginTop: 25 }}>
          <Text style={{ color: COLORS.text, fontSize: 18, marginBottom: 15, fontWeight: 'bold' }}>
            Device Management
          </Text>

          {role === 'OWNER' && hasDevice && activeDevice && (
            <RequestDropdown serialNumber={activeDevice.serialNumber} />
          )}

          <DashboardCard
            isFullWidth
            title="Add/Pair Another Device"
            icon="add-circle-outline"
            onPress={() => navigation.navigate('DeviceManagement')}
          />
        </View>

        {/* LOGOUT BUTTON (From Snippet 1) */}
        <TouchableOpacity 
          style={{ marginTop: 'auto', paddingVertical: 30, alignItems: 'center' }} 
          onPress={handleLogout}
        >
          <Text style={{ color: '#FF4444', fontWeight: 'bold', fontSize: 16, letterSpacing: 1 }}>
            LOG OUT
          </Text>
        </TouchableOpacity>

      </ScrollView>
    </SafeAreaView>
  );
}