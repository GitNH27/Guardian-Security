import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as SecureStore from 'expo-secure-store';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING } from '../styles/theme';

export default function HomeScreen({ navigation }) {
  const [userName, setUserName] = useState('User');
  const [threatLevel, setThreatLevel] = useState('Low'); 
  const [role, setRole] = useState('USER'); 
  const [hasDevice, setHasDevice] = useState(false);
  const [activeDevice, setActiveDevice] = useState(null);

  useEffect(() => {
    const loadSessionData = async () => {
      try {
        // 1. Load general user info
        const userValue = await SecureStore.getItemAsync('userData');
        if (userValue) {
          const user = JSON.parse(userValue);
          setUserName(user.firstName || 'User');
        }

        // 2. Load the specific device context from our Hybrid Login check
        const deviceValue = await SecureStore.getItemAsync('activeDevice');
        if (deviceValue) {
          const device = JSON.parse(deviceValue);
          setActiveDevice(device);
          setRole(device.role || 'USER');
          setHasDevice(true);
        } else {
          // If no active device is found in storage, and we landed here, 
          // redirect to the Device Management page as per your plan.
          navigation.replace('DeviceManagement');
        }
      } catch (e) {
        console.error("Failed to load session data", e);
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

  const NavCard = ({ title, icon, onPress, disabled = false, color = COLORS.primary, isFullWidth = false }) => (
    <TouchableOpacity 
      style={[
        isFullWidth ? styles.wideCard : styles.squareCard, 
        disabled && styles.cardDisabled
      ]} 
      onPress={onPress}
      disabled={disabled}
    >
      <Ionicons name={icon} size={isFullWidth ? 28 : 32} color={disabled ? '#555' : color} />
      <Text 
        style={[isFullWidth ? styles.wideCardText : styles.squareCardText, disabled && styles.cardDisabledText]}
        numberOfLines={1}
      >
        {title}
      </Text>
      {disabled && <Ionicons name="lock-closed" size={14} color="#555" style={styles.lockIcon} />}
    </TouchableOpacity>
  );

  // If loading or redirecting, return null or a loader to prevent UI flash
  if (!hasDevice && !activeDevice) return null;

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        
        {/* Header Section */}
        <View style={styles.header}>
          <Image source={require('../../assets/logo.png')} style={styles.smallLogo} resizeMode="contain" />
          <View style={styles.headerTextContainer}>
            <Text style={styles.welcomeText}>Welcome Home,</Text>
            <Text style={styles.nameText}>{userName}!</Text>
            <Text style={styles.roleBadge}>
              {activeDevice?.serialNumber} • {role}
            </Text>
          </View>
          
          {/* Switch Device Button (For Multi-device users) */}
          {/* Change inside the Header Section of HomeScreen */}
          <TouchableOpacity 
            onPress={async () => {
              try {
                // Import your deviceService at the top of the file
                const response = await deviceService.getSelectionContext();
                
                // Navigate using your specific screen title: DeviceSelectionScreen
                navigation.navigate('DeviceSelectionScreen', { devices: response.devices });
              } catch (error) {
                Alert.alert("Error", "Could not load device list.");
              }
            }}
          >
            <Ionicons name="car-outline" size={28} color={COLORS.primary} />
          </TouchableOpacity>
        </View>

        {/* DASHBOARD VIEW */}
        <View style={styles.statusBanner}>
          <Text style={styles.statusTitle}>System Status: 
            <Text style={{color: threatLevel === 'High' ? '#FF4444' : COLORS.primary}}> Guardian Active</Text>
          </Text>
        </View>

        <View style={styles.grid}>
          <NavCard title="User Settings" icon="settings-outline" onPress={() => navigation.navigate('Settings')} />
          <NavCard title="Threat Status" icon="shield-checkmark-outline" onPress={() => Alert.alert("Status", threatLevel)} />
          <NavCard title="Activity Logs" icon="list-outline" onPress={() => navigation.navigate('Logs')} />
          <NavCard 
            title="Live Feed" 
            icon="videocam-outline" 
            onPress={() => navigation.navigate('LiveView')} 
            disabled={threatLevel !== 'High'} 
            color="#FF4444"
          />
        </View>

        <View style={styles.section}>
           <Text style={styles.sectionTitle}>Device Management</Text>
           {role === 'OWNER' && (
             <NavCard isFullWidth title="Grant Access" icon="person-add-outline" onPress={() => navigation.navigate('GrantAccess')} color="#FFD700" />
           )}
           {/* This leads to your unified Device Management Page */}
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
  
  // Square Buttons
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
  
  // Wide Buttons (Fixed your layout issue)
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

  cardDisabled: { backgroundColor: '#0D0D0D', borderColor: '#1A1A1A', opacity: 0.6 },
  cardDisabledText: { color: '#555' },
  lockIcon: { position: 'absolute', top: 12, right: 12 },
  
  logoutButton: { marginTop: 'auto', paddingVertical: 30, alignItems: 'center' },
  logoutText: { color: '#FF4444', fontWeight: 'bold', fontSize: 16, letterSpacing: 1 }
});