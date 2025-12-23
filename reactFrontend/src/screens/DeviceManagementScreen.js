import React, { useState } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING } from '../styles/theme';
import { deviceService } from '../services/deviceService';

export default function DeviceManagementScreen({ navigation }) {
  const [loading, setLoading] = useState(false);
  const [pairingCode, setPairingCode] = useState('');
  const [serialNumber, setSerialNumber] = useState('');
  const [ownerEmail, setOwnerEmail] = useState('');

  const handleAction = async (type) => {
    setLoading(true);
    try {
      if (type === 'CLAIM') {
        await deviceService.claimDevice({ deviceCode: pairingCode });
        Alert.alert("Success", "Device claimed! Re-login to refresh your session.", [
          { text: "OK", onPress: () => navigation.replace('Login') }
        ]);
      } else {
        await deviceService.requestAccess({
          serialNumber: serialNumber.trim(),
          ownerEmail: ownerEmail.trim().toLowerCase()
        });
        Alert.alert("Request Sent", "The owner must approve your access.", [
          { text: "OK", onPress: () => navigation.goBack() }
        ]);
      }
    } catch (error) {
      Alert.alert("Error", error.message || "Action failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <Text style={styles.headerTitle}>DEVICE MANAGEMENT</Text>

        {/* CLAIM SECTION */}
        <View style={styles.section}>
          <Text style={styles.label}>CLAIM A DEVICE</Text>
          <View style={styles.inputWrapper}>
            <TextInput
              style={styles.input}
              placeholder="Pairing Password"
              placeholderTextColor="rgba(255,255,255,0.4)"
              value={pairingCode}
              onChangeText={setPairingCode}
            />
            <TouchableOpacity onPress={() => Alert.alert("Scanner", "Opening QR Scanner...")}>
              <Ionicons name="camera" size={24} color={COLORS.primary} />
            </TouchableOpacity>
          </View>
          <TouchableOpacity style={styles.goldButton} onPress={() => handleAction('CLAIM')}>
            <Text style={styles.buttonText}>CLAIM DEVICE</Text>
          </TouchableOpacity>
          <Text style={styles.hintText}>Own a new device? Enter the code or scan the QR to get started.</Text>
        </View>

        {/* DIVIDER */}
        <View style={styles.dividerContainer}>
          <View style={styles.line} />
          <Text style={styles.orText}>- OR -</Text>
          <View style={styles.line} />
        </View>

        {/* REQUEST SECTION */}
        <View style={styles.section}>
          <Text style={styles.label}>REQUEST ACCESS</Text>
          <View style={styles.inputWrapper}>
            <TextInput
              style={styles.input}
              placeholder="Member's Email"
              value={ownerEmail}
              onChangeText={setOwnerEmail}
            />
          </View>
          <TouchableOpacity style={styles.goldButton} onPress={() => handleAction('REQUEST')}>
            <Text style={styles.buttonText}>REQUEST ACCESS</Text>
          </TouchableOpacity>
          <Text style={styles.hintText}>Need access to someone else's device? Ask them for their email.</Text>
        </View>

        {loading && <ActivityIndicator color={COLORS.primary} style={{ marginTop: 20 }} />}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  scrollContent: { padding: SPACING.l, alignItems: 'center' },
  headerTitle: { color: COLORS.text, fontSize: 20, fontWeight: 'bold', marginBottom: 40, letterSpacing: 1 },
  section: { width: '100%', alignItems: 'center' },
  label: { color: COLORS.primary, fontWeight: 'bold', alignSelf: 'flex-start', marginBottom: 15, fontSize: 16 },
  inputWrapper: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: COLORS.primary,
    borderRadius: 8,
    paddingHorizontal: 15,
    height: 55,
    backgroundColor: 'transparent'
  },
  input: { flex: 1, color: COLORS.text, fontSize: 16 },
  goldButton: {
    backgroundColor: COLORS.primary,
    width: '50%',
    paddingVertical: 12,
    borderRadius: 5,
    marginTop: 25,
    alignItems: 'center'
  },
  buttonText: { color: '#000', fontWeight: 'bold', fontSize: 14 },
  hintText: { color: COLORS.text, opacity: 0.6, textAlign: 'center', marginTop: 15, fontSize: 12, paddingHorizontal: 20 },
  dividerContainer: { flexDirection: 'row', alignItems: 'center', width: '100%', marginVertical: 40 },
  line: { flex: 1, height: 1, backgroundColor: COLORS.primary, opacity: 0.4 },
  orText: { color: COLORS.text, paddingHorizontal: 10, fontWeight: 'bold' },
});