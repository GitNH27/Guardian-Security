import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, ActivityIndicator, RefreshControl, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import * as SecureStore from 'expo-secure-store';
import { Ionicons } from '@expo/vector-icons';

import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import { threatLogService } from '../services/threatLogService';
import { LogEntryCard } from '../components/LogEntryCard';
import { LogFilter } from '../components/LogFilter';
import { setNotificationSilence } from '../services/notificationService';

export default function ThreatLogScreen({ navigation }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [serialNumber, setSerialNumber] = useState(null);
  const [userEmail, setUserEmail] = useState(null);
  const [activeFilters, setActiveFilters] = useState({});

  useFocusEffect(
    useCallback(() => {
      setNotificationSilence(true);
      return () => setNotificationSilence(false);
    }, [])
  );

  // Wrapped in useCallback so useFocusEffect always has a stable, up-to-date reference
  const fetchLogs = useCallback(async (isRefreshing = false) => {
    try {
      if (!isRefreshing) setLoading(true);

      const [storedSerial, storedEmail] = await Promise.all([
        SecureStore.getItemAsync('activeDeviceSerial'),
        SecureStore.getItemAsync('userEmail')
      ]);

      setUserEmail(storedEmail);
      setSerialNumber(storedSerial || "Unknown Device");

      if (storedSerial) {
        const data = await threatLogService.getThreatLogs(storedSerial, activeFilters);
        const sortedLogs = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setLogs(sortedLogs);
      }
    } catch (error) {
      console.error('Failed to fetch logs:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [activeFilters]); // fetchLogs updates whenever filters change

  // Fires when filters change
  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  // Fires when screen comes into focus — depends on fetchLogs, not activeFilters directly
  useFocusEffect(
    useCallback(() => {
      fetchLogs();
    }, [fetchLogs])
  );

  const onRefresh = () => {
    setRefreshing(true);
    fetchLogs(true);
  };

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      <View style={[sharedStyles.container, { flex: 1 }]}>

        <View style={sharedStyles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={24} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={sharedStyles.headerTitle}>ACTIVITY LOGS</Text>
          <View style={{ width: 24 }} />
        </View>

        {loading && !refreshing ? (
          <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <ActivityIndicator size="large" color={COLORS.primary} />
            <Text style={{ color: COLORS.textHint, marginTop: 10 }}>Loading Logs...</Text>
          </View>
        ) : (
          <FlatList
            data={logs}
            keyExtractor={(item) => item.id.toString()}
            renderItem={({ item }) => <LogEntryCard item={item} userEmail={userEmail} />}
            contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 40 }}

            ListHeaderComponent={
              <View style={{ paddingTop: 10 }}>
                <LogFilter onApplyFilters={(filters) => setActiveFilters(filters)} />

                <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 15 }}>
                  <Text style={[sharedStyles.subtitle, { marginBottom: 0 }]}>
                    Device: {serialNumber}
                  </Text>
                  {Object.keys(activeFilters).length > 0 && (
                    <View style={{ backgroundColor: 'rgba(212, 175, 55, 0.1)', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 4 }}>
                      <Text style={{ color: COLORS.primary, fontSize: 10, fontWeight: 'bold' }}>FILTER ACTIVE</Text>
                    </View>
                  )}
                </View>
              </View>
            }

            refreshControl={
              <RefreshControl
                refreshing={refreshing}
                onRefresh={onRefresh}
                tintColor={COLORS.primary}
                colors={[COLORS.primary]}
              />
            }

            ListEmptyComponent={
              <View style={{ alignItems: 'center', marginTop: 50 }}>
                <Ionicons name="document-text-outline" size={60} color={COLORS.textHint} />
                <Text style={[sharedStyles.hintText, { fontSize: 16, marginTop: 10 }]}>
                  {Object.keys(activeFilters).length > 0
                    ? "No logs match your filters."
                    : "No recent activity detected."}
                </Text>
              </View>
            }
          />
        )}
      </View>
    </SafeAreaView>
  );
}