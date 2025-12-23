import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, ScrollView, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as SecureStore from 'expo-secure-store';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING } from '../styles/theme';
import { RequestDropdown } from '../components/RequestDropdown';
import { deviceService } from '../services/deviceService'; // Ensure this is imported

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
        // 1. Load general user info
        const userValue = await SecureStore.getItemAsync('userData');
        if (userValue) {
          const user = JSON.parse(userValue);
          setUserName(user.firstName || 'User');
        }

        // 2. Load the specific device context
        const deviceValue = await SecureStore.getItemAsync('activeDevice');
        if (deviceValue) {
          const device = JSON.parse(deviceValue);
          setActiveDevice(device);
          setRole(device.role || 'USER');
          setHasDevice(true);
        } else {
          // No active device found, but we stay on this screen
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
      const response = await deviceService.getSelectionContext();
      navigation.navigate('DeviceSelectionScreen', { devices: response.devices });
    } catch (error) {
      Alert.alert("Error", "Could not load device list.");
    }
  };

  const NavCard = ({ title, icon, onPress, disabled = false, color = COLORS.primary, isFullWidth = false }) => (
    <TouchableOpacity
      style={[
        isFullWidth ? styles.wideCard : styles.squareCard,
        disabled && styles.cardDisabled
      ]}
      onPress={onPress}
      disabled={disabled}
    >
      <Ionicons name={icon} size={isFullWidth ? 28 : 32} color={disabled ? '#444' : color} />
      <Text
        style={[isFullWidth ? styles.wideCardText : styles.squareCardText, disabled && styles.cardDisabledText]}
        numberOfLines={1}
      >
        {title}
      </Text>
      {disabled && <Ionicons name="lock-closed" size={14} color="#555" style={styles.lockIcon} />}
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <SafeAreaView style={[styles.safeArea, { justifyContent: 'center' }]}>
        <ActivityIndicator size="large" color={COLORS.primary} />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.scrollContainer}>

        {/* Header Section */}
        <View style={styles.header}>
          <Image source={require('../../assets/logo.png')} style={styles.smallLogo} resizeMode="contain" />
          <View style={styles.headerTextContainer}>
            <Text style={styles.welcomeText}>Welcome Home,</Text>
            <Text style={styles.nameText}>{userName}!</Text>
            <Text style={[styles.roleBadge, !hasDevice && { color: '#888' }]}>
              {hasDevice ? `${activeDevice?.serialNumber} • ${role}` : "No Active Device"}
            </Text>
          </View>

          {/* Only show switch button if they actually have a device context */}
          {hasDevice && (
            <TouchableOpacity onPress={handleSwitchDevice}>
              <Ionicons name="car-outline" size={28} color={COLORS.primary} />
            </TouchableOpacity>
          )}
        </View>

        {/* DASHBOARD VIEW */}
        <View style={[styles.statusBanner, !hasDevice && { borderLeftColor: '#444' }]}>
          <Text style={styles.statusTitle}>System Status: 
            <Text style={{ color: !hasDevice ? '#666' : (threatLevel === 'High' ? '#FF4444' : COLORS.primary) }}>
              {hasDevice ? " Guardian Active" : " Offline"}
            </Text>
          </Text>
        </View>

        {/* Main Grid: Features are disabled if no device is active */}
        <View style={styles.grid}>
          <NavCard title="User Settings" icon="settings-outline" onPress={() => navigation.navigate('Settings')} />
          
          <NavCard 
            title="Threat Status" 
            icon="shield-checkmark-outline" 
            onPress={() => Alert.alert("Status", threatLevel)} 
            disabled={!hasDevice}
          />
          
          <NavCard 
            title="Activity Logs" 
            icon="list-outline" 
            onPress={() => navigation.navigate('Logs')} 
            disabled={!hasDevice}
          />
          
          <NavCard
            title="Live Feed"
            icon="videocam-outline"
            onPress={() => navigation.navigate('LiveView')}
            disabled={!hasDevice || threatLevel !== 'High'}
            color="#FF4444"
          />
        </View>

        {/* Device Management Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Device Management</Text>

          {/* Request Dropdown only for Owners with an active device */}
          {role === 'OWNER' && hasDevice && activeDevice && (
            <RequestDropdown serialNumber={activeDevice.serialNumber} />
          )}

          <NavCard
            isFullWidth
            title="Add/Pair Another Device"
            icon="add-circle-outline"
            onPress={() => navigation.navigate('DeviceManagement')}
          />
        </View>

        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <Text style={styles.logoutText}>LOG OUT</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: COLORS.background },
  scrollContainer: { padding: SPACING.l, flexGrow: 1 },
  header: { flexDirection: 'row', alignItems: 'center', marginBottom: 30 },
  smallLogo: { width: 70, height: 70, marginRight: 15 },
  headerTextContainer: { flex: 1 },
  welcomeText: { color: COLORS.text, fontSize: 16, opacity: 0.7 },
  nameText: { color: COLORS.text, fontSize: 26, fontWeight: 'bold' },
  roleBadge: { color: COLORS.primary, fontSize: 11, fontWeight: 'bold', marginTop: 4 },

  statusBanner: {
    backgroundColor: '#1A1A1A',
    padding: 15,
    borderRadius: 10,
    borderLeftWidth: 4,
    borderLeftColor: COLORS.primary,
    marginBottom: 25
  },
  statusTitle: { color: COLORS.text, fontWeight: '600' },

  section: { marginBottom: 25 },
  sectionTitle: { color: COLORS.text, fontSize: 18, marginBottom: 15, fontWeight: 'bold' },
  grid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between' },

  squareCard: {
    backgroundColor: '#1A1A1A',
    width: '48%',
    aspectRatio: 1,
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 15,
    borderWidth: 1,
    borderColor: '#333'
  },
  squareCardText: { color: COLORS.text, marginTop: 10, fontWeight: '600', fontSize: 14 },

  wideCard: {
    backgroundColor: '#1A1A1A',
    width: '100%',
    height: 70,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#333'
  },
  wideCardText: { color: COLORS.text, marginLeft: 15, fontWeight: '600', fontSize: 16 },

  cardDisabled: { backgroundColor: '#0D0D0D', borderColor: '#1A1A1A', opacity: 0.5 },
  cardDisabledText: { color: '#444' },
  lockIcon: { position: 'absolute', top: 12, right: 12 },

  logoutButton: { marginTop: 'auto', paddingVertical: 30, alignItems: 'center' },
  logoutText: { color: '#FF4444', fontWeight: 'bold', fontSize: 16, letterSpacing: 1 }
});