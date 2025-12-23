import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, LayoutAnimation, ActivityIndicator, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS } from '../styles/theme';
import { deviceService } from '../services/deviceService';

export const RequestDropdown = ({ serialNumber }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadRequests = async () => {
    try {
      setLoading(true);
      const data = await deviceService.fetchPendingRequests(serialNumber);
      setRequests(data); // Expects: [{id, requesterEmail, deviceSerial, ...}]
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const toggleDropdown = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setIsExpanded(!isExpanded);
    if (!isExpanded) loadRequests();
  };

  const handleDecision = async (requestId, decision) => {
    try {
      await deviceService.memberAccessDecision({ requestId, decision });
      setRequests(prev => prev.filter(req => req.id !== requestId));
      Alert.alert("Success", `Request ${decision.toLowerCase()}!`);
    } catch (err) {
      Alert.alert("Error", "Could not process decision.");
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.mainCard} onPress={toggleDropdown}>
        <Ionicons name="person-add-outline" size={24} color="#FFD700" />
        <Text style={styles.mainText}>Grant Access</Text>
        {requests.length > 0 && (
          <View style={styles.badge}><Text style={styles.badgeText}>{requests.length}</Text></View>
        )}
        <Ionicons name={isExpanded ? "chevron-up" : "chevron-down"} size={20} color={COLORS.text} />
      </TouchableOpacity>

      {isExpanded && (
        <View style={styles.dropdown}>
          {loading ? (
            <ActivityIndicator color={COLORS.primary} style={{ margin: 10 }} />
          ) : requests.length === 0 ? (
            <Text style={styles.emptyText}>No pending requests</Text>
          ) : (
            requests.map((req) => (
              <View key={req.id} style={styles.requestItem}>
                <View style={styles.requestInfo}>
                  <Text style={styles.requesterName}>{req.requesterEmail}</Text>
                  <Text style={styles.requestSub}>{req.deviceSerial}</Text>
                </View>
                <View style={styles.actionGroup}>
                  <TouchableOpacity 
                    style={[styles.actionBtn, styles.approveBtn]} 
                    onPress={() => handleDecision(req.id, 'APPROVED')}
                  >
                    <Ionicons name="checkmark" size={18} color="#000" />
                  </TouchableOpacity>
                  <TouchableOpacity 
                    style={[styles.actionBtn, styles.denyBtn]} 
                    onPress={() => handleDecision(req.id, 'REJECTED')}
                  >
                    <Ionicons name="close" size={18} color="#FFF" />
                  </TouchableOpacity>
                </View>
              </View>
            ))
          )}
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { marginBottom: 12 },
  mainCard: { backgroundColor: '#1A1A1A', height: 70, borderRadius: 12, flexDirection: 'row', alignItems: 'center', paddingHorizontal: 20, borderWidth: 1, borderColor: '#333' },
  mainText: { flex: 1, color: '#FFF', marginLeft: 15, fontWeight: '600', fontSize: 16 },
  badge: { backgroundColor: '#FF4444', borderRadius: 10, paddingHorizontal: 6, marginRight: 10 },
  badgeText: { color: '#FFF', fontSize: 10, fontWeight: 'bold' },
  dropdown: { backgroundColor: '#121212', borderBottomLeftRadius: 12, borderBottomRightRadius: 12, padding: 10, borderLeftWidth: 1, borderRightWidth: 1, borderBottomWidth: 1, borderColor: '#333' },
  requestItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#222' },
  requestInfo: { flex: 1 },
  requesterName: { color: '#FFF', fontWeight: 'bold' },
  requestSub: { color: '#888', fontSize: 12 },
  actionGroup: { flexDirection: 'row' },
  actionBtn: { width: 35, height: 35, borderRadius: 17.5, justifyContent: 'center', alignItems: 'center', marginLeft: 10 },
  approveBtn: { backgroundColor: '#FFD700' },
  denyBtn: { backgroundColor: '#444' },
  emptyText: { color: '#666', textAlign: 'center', padding: 10 }
});