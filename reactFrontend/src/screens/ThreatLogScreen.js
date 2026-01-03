import React, { useState } from 'react';
import { View, Text, FlatList, ActivityIndicator, RefreshControl, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import * as SecureStore from 'expo-secure-store';
import { Ionicons } from '@expo/vector-icons';

// Styles & Themes
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

// Services
import { threatLogService } from '../services/threatLogService';

// 🔥 NEW: Import the modular component
import { LogEntryCard } from '../components/LogEntryCard';

export default function ThreatLogScreen({ navigation }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [serialNumber, setSerialNumber] = useState(null);

  const fetchLogs = async (isRefreshing = false) => {
    try {
      if (!isRefreshing) setLoading(true);
      
      const storedSerial = await SecureStore.getItemAsync('activeDeviceSerial');
      setSerialNumber(storedSerial || "Unknown Device");

      if (storedSerial) {
        const data = await threatLogService.getThreatLogs(storedSerial);
        const sortedLogs = data.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        setLogs(sortedLogs);
      }
    } catch (error) {
      console.error('Failed to fetch logs:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      fetchLogs();
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    fetchLogs(true);
  };

  // 🔥 CHANGED: Now using the modular component instead of inline View
  const renderLogItem = ({ item }) => (
    <LogEntryCard item={item} />
  );

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      <View style={sharedStyles.container}>
        
        <View style={sharedStyles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={24} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={sharedStyles.headerTitle}>ACTIVITY LOGS</Text>
          <View style={{ width: 24 }} /> 
        </View>

        <Text style={[sharedStyles.subtitle, { marginBottom: 20 }]}>
          Device: {serialNumber}
        </Text>

        {loading ? (
          <View style={{ flex: 1, justifyContent: 'center' }}>
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        ) : (
          <FlatList
            data={logs}
            keyExtractor={(item) => item.id.toString()}
            renderItem={renderLogItem}
            contentContainerStyle={{ paddingBottom: 20 }}
            refreshControl={
              <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={COLORS.primary} />
            }
            ListEmptyComponent={
              <View style={{ alignItems: 'center', marginTop: 50 }}>
                <Ionicons name="document-text-outline" size={60} color={COLORS.textHint} />
                <Text style={[sharedStyles.hintText, { fontSize: 16 }]}>No recent activity detected.</Text>
              </View>
            }
          />
        )}
      </View>
    </SafeAreaView>
  );
}