import React, { useEffect, useState } from 'react';
import { View, Text, ScrollView, Alert, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as SecureStore from 'expo-secure-store';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import DashboardCard from '../components/DashboardCard';
import HomeHeader from '../components/HomeHeader';
import { RequestDropdown } from '../components/RequestDropdown';
import { deviceService } from '../services/deviceService';
import { StatusBanner } from '../components/StatusBanner';
import { useThreatMonitor } from '../hooks/useThreatMonitor';

export default function HomeScreen({ navigation }) {
  const [userName, setUserName] = useState('User');
  const [role, setRole] = useState('USER');
  const [hasDevice, setHasDevice] = useState(false);
  const [activeDevice, setActiveDevice] = useState(null);
  const [loading, setLoading] = useState(true);

  const [allDevices, setAllDevices] = useState([]);

  // ... inside HomeScreen component
  const memoizedIds = React.useMemo(() => allDevices.map(d => String(d.deviceId)), [allDevices]);
  const fleetThreats = useThreatMonitor(memoizedIds);

  // Access the active device threat data using the normalized fields from our hook
  const activeThreatData = activeDevice ? fleetThreats[activeDevice.deviceId] : null;

  // Use the normalized 'level' and 'isThreat' properties
  const currentThreatLevel = activeThreatData?.level || 'Low';
  const isActiveThreat = activeThreatData?.isThreat || false;

  useFocusEffect(
    React.useCallback(() => {
      const loadSessionData = async () => {
        try {
          setLoading(true);

          // Load user info (unchanged)
          const userValue = await SecureStore.getItemAsync('userData');
          if (userValue) {
            const user = JSON.parse(userValue);
            setUserName(user.firstName || 'User');
          }

          // 🔥 ALWAYS refresh devices from backend
          const response = await deviceService.getDeviceSelectionContext();
          
          if (response.devices && response.devices.length > 0) {
            setAllDevices(response.devices);  // Update all devices for threat monitoring

            const storedActiveId = await SecureStore.getItemAsync('activeDeviceId');

            let selectedDevice =
              response.devices.find(
                d => d.deviceId.toString() === storedActiveId
              ) || response.devices[0]; // fallback only

            setActiveDevice(selectedDevice);
            setRole(selectedDevice.role || 'USER');
            setHasDevice(true);

            // Keep ID in sync (important when new device is added)
            await SecureStore.setItemAsync(
              'activeDeviceId',
              selectedDevice.deviceId.toString()
            );
            // Save the serial number so the Logs screen can use it
            await SecureStore.setItemAsync(
              'activeDeviceSerial',
              selectedDevice.serialNumber);
          } else {
            setActiveDevice(null);
            setRole('USER');
            setHasDevice(false);
            await SecureStore.deleteItemAsync('activeDeviceId');
          }


        } catch (e) {
          console.error('Failed to refresh home context', e);
        } finally {
          setLoading(false);
        }
      };

      loadSessionData();
    }, [])
  );


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

        {/* Status Banner */}
        <StatusBanner 
          status="System Status:" 
          message={hasDevice ? (isActiveThreat ? `THREAT: ${activeThreatData.object}` : "Guardian Active") : "Offline"}
          type={!hasDevice ? 'offline' : (isActiveThreat ? 'error' : 'success')}
        />

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
            onPress={() => Alert.alert("Status", `Level: ${currentThreatLevel}`)} 
            disabled={!hasDevice}
          />
          
          <DashboardCard 
            title="Activity Logs" 
            icon="list-outline" 
            onPress={() => navigation.navigate('ThreatLogs')} 
            disabled={!hasDevice}
          />
          
          <DashboardCard
            title="Live Feed"
            icon="videocam-outline"
            onPress={() => navigation.navigate('Live')}
            // Now activates automatically when a Person/Object is detected
            disabled={!hasDevice || !isActiveThreat}
            color={isActiveThreat ? "#FF4444" : COLORS.primary}
          />
        </View>

        {/* Fleet Alert Notification (Optional) */}
        {Object.keys(fleetThreats).length > 0 && Object.entries(fleetThreats).some(([id, data]) => id !== activeDevice?.deviceId?.toString() && data.ml_data?.level === 'HIGH') && (
           <View style={[sharedStyles.card, { marginTop: 20, borderColor: '#FF4444', backgroundColor: 'rgba(255, 68, 68, 0.1)' }]}>
             <Text style={{ color: '#FF4444', fontWeight: 'bold', textAlign: 'center' }}>
               🚨 ALERT: High Threat on another vehicle!
             </Text>
           </View>
        )}

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