import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import * as SecureStore from 'expo-secure-store';
import { COLORS, SPACING } from '../styles/theme';
import { userService } from '../services/userService';
import { deviceService } from '../services/deviceService';

export default function UserSettingsScreen({ navigation, route }) {
  const [loading, setLoading] = useState(false);
  const [isMembersLoading, setIsMembersLoading] = useState(false); // ADDED THIS
  const [userId, setUserId] = useState(null);
  const [members, setMembers] = useState([]);
  
  const userRole = route.params?.currentRole || 'MEMBER';
  const activeSerial = route.params?.activeSerial;

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [transferSerial, setTransferSerial] = useState('');
  const [newOwnerEmail, setNewOwnerEmail] = useState('');

  useEffect(() => {
    const loadUserData = async () => {
      const userValue = await SecureStore.getItemAsync('userData');
      if (userValue) {
        const user = JSON.parse(userValue);
        setUserId(user.id);
        setFirstName(user.firstName);
        setLastName(user.lastName);
        setEmail(user.email);
      }
    };
    loadUserData();
  }, []);

  useEffect(() => {
    if (userRole === 'OWNER' && activeSerial) {
      const fetchMembers = async () => {
        setIsMembersLoading(true);
        try {
          const data = await deviceService.getDeviceMembers(activeSerial);
          setMembers(data);
        } catch (error) {
          console.error("Settings [fetchMembers] Error:", error);
        } finally {
          setIsMembersLoading(false);
        }
      };
      fetchMembers();
    }
  }, [activeSerial, userRole]);

  const handleUpdateProfile = async () => {
    setLoading(true);
    try {
      const updatedUser = await userService.updateProfile(userId, { firstName, lastName, email });
      if (updatedUser) {
        await SecureStore.setItemAsync('userData', JSON.stringify(updatedUser));
        Alert.alert("Success", "Profile updated successfully.");
      }
    } catch (error) {
      Alert.alert("Error", error.response?.data?.message || "Update failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async () => {
    if (newPassword !== confirmPassword) return Alert.alert("Error", "Passwords do not match.");
    setLoading(true);
    try {
      await userService.updatePassword(userId, { oldPassword, newPassword, confirmPassword });
      setOldPassword(''); setNewPassword(''); setConfirmPassword('');
      Alert.alert("Success", "Password changed.");
    } catch (error) {
      Alert.alert("Error", error.response?.data?.message || "Password change failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleTransfer = async () => {
    Alert.alert(
      "Confirm Transfer",
      `Transfer ownership of ${transferSerial} to ${newOwnerEmail}? You will lose administrative rights.`,
      [
        { text: "Cancel", style: "cancel" },
        { 
          text: "Transfer", 
          style: "destructive", 
          onPress: async () => {
            setLoading(true);
            try {
              await userService.transferOwnership(userId, { 
                deviceSerialNumber: transferSerial.trim(), 
                newOwnerEmail: newOwnerEmail.trim().toLowerCase() 
              });
              Alert.alert("Success", "Ownership transferred.");
              navigation.replace('Home');
            } catch (error) {
              Alert.alert("Error", error.response?.data?.message || "Transfer failed.");
            } finally {
              setLoading(false);
            }
          }
        }
      ]
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.headerBar}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={28} color={COLORS.primary} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>USER SETTINGS</Text>
        <View style={{ width: 28 }} />
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        
        {/* PROFILE SECTION */}
        <View style={styles.section}>
          <Text style={styles.label}>EDIT PROFILE</Text>
          <TextInput style={styles.input} placeholder="First Name" value={firstName} onChangeText={setFirstName} placeholderTextColor="#888" />
          <TextInput style={styles.input} placeholder="Last Name" value={lastName} onChangeText={setLastName} placeholderTextColor="#888" />
          <TextInput style={styles.input} placeholder="Email" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" placeholderTextColor="#888" />
          <TouchableOpacity style={styles.goldButton} onPress={handleUpdateProfile}>
            <Text style={styles.buttonText}>UPDATE PROFILE</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.divider} />

        {/* PASSWORD SECTION */}
        <View style={styles.section}>
          <Text style={styles.label}>CHANGE PASSWORD</Text>
          <TextInput style={styles.input} placeholder="Current Password" secureTextEntry value={oldPassword} onChangeText={setOldPassword} placeholderTextColor="#888" />
          <TextInput style={styles.input} placeholder="New Password" secureTextEntry value={newPassword} onChangeText={setNewPassword} placeholderTextColor="#888" />
          <TextInput style={styles.input} placeholder="Confirm New Password" secureTextEntry value={confirmPassword} onChangeText={setConfirmPassword} placeholderTextColor="#888" />
          <TouchableOpacity style={styles.goldButton} onPress={handleChangePassword}>
            <Text style={styles.buttonText}>CHANGE PASSWORD</Text>
          </TouchableOpacity>
        </View>

        {/* MANAGE MEMBERS SECTION - ONLY VISIBLE TO OWNER */}
        {userRole === 'OWNER' && (
          <>
            <View style={styles.divider} />
            <View style={styles.section}>
              <Text style={styles.label}>MANAGE MEMBERS</Text>
              {isMembersLoading ? (
                <ActivityIndicator color={COLORS.primary} size="small" />
              ) : members.length > 0 ? (
                members.map((member) => (
                  <View key={member.id} style={styles.memberCard}>
                    <View>
                      <Text style={styles.memberMainText}>{member.firstName} {member.lastName}</Text>
                      <Text style={styles.memberSubText}>{member.email}</Text>
                    </View>
                    <Ionicons name="person-circle" size={26} color={COLORS.primary} />
                  </View>
                ))
              ) : (
                <Text style={styles.hintText}>No other members found for this device.</Text>
              )}
            </View>
          </>
        )}

        {/* SECURITY / TRANSFER SECTION - ONLY VISIBLE TO OWNER */}
        {userRole === 'OWNER' && (
          <>
            <View style={styles.divider} />
            <View style={styles.section}>
              <Text style={[styles.label, { color: '#FF4444' }]}>DANGER ZONE: TRANSFER OWNERSHIP</Text>
              <TextInput style={styles.input} placeholder="Device Serial Number" value={transferSerial} onChangeText={setTransferSerial} placeholderTextColor="#888" />
              <TextInput style={styles.input} placeholder="New Owner Email" value={newOwnerEmail} onChangeText={setNewOwnerEmail} keyboardType="email-address" autoCapitalize="none" placeholderTextColor="#888" />
              <TouchableOpacity style={[styles.goldButton, { backgroundColor: '#FF4444' }]} onPress={handleTransfer}>
                <Text style={styles.buttonText}>TRANSFER OWNERSHIP</Text>
              </TouchableOpacity>
              <Text style={styles.hintText}>Only the current owner can perform this. The new owner must already be a member of the device.</Text>
            </View>
          </>
        )}

        {loading && <ActivityIndicator color={COLORS.primary} style={{ marginTop: 20 }} />}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  headerBar: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 20, paddingVertical: 10 },
  scrollContent: { padding: SPACING.l, paddingBottom: 50 },
  headerTitle: { color: COLORS.text, fontSize: 18, fontWeight: 'bold', letterSpacing: 1 },
  section: { width: '100%', marginBottom: 20 },
  label: { color: COLORS.primary, fontWeight: 'bold', marginBottom: 15, fontSize: 14 },
  input: {
    width: '100%',
    borderWidth: 1,
    borderColor: 'rgba(212, 175, 55, 0.3)',
    borderRadius: 8,
    paddingHorizontal: 15,
    height: 50,
    color: COLORS.text,
    marginBottom: 10,
    backgroundColor: 'rgba(255,255,255,0.05)'
  },
  goldButton: {
    backgroundColor: COLORS.primary,
    paddingVertical: 12,
    borderRadius: 5,
    marginTop: 10,
    alignItems: 'center'
  },
  buttonText: { color: '#000', fontWeight: 'bold', fontSize: 14 },
  divider: { height: 1, backgroundColor: 'rgba(255,255,255,0.1)', marginVertical: 30 },
  hintText: { color: COLORS.text, opacity: 0.5, textAlign: 'center', marginTop: 10, fontSize: 11 },
  // ADDED THESE STYLES:
  memberCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: 'rgba(212, 175, 55, 0.1)',
  },
  memberMainText: { 
    color: COLORS.text, 
    fontSize: 14, 
    fontWeight: '600' 
  },
  memberSubText: { 
    color: COLORS.text, 
    opacity: 0.5, 
    fontSize: 12 
  },
});