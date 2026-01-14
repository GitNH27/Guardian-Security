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

export default function ThreatLogScreen({ navigation }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [serialNumber, setSerialNumber] = useState(null);
  
  // State to hold the current active filters
  const [activeFilters, setActiveFilters] = useState({});

  const fetchLogs = async (isRefreshing = false) => {
    try {
      if (!isRefreshing) setLoading(true);
      
      const storedSerial = await SecureStore.getItemAsync('activeDeviceSerial');
      setSerialNumber(storedSerial || "Unknown Device");

      if (storedSerial) {
        // Pass the activeFilters object to the service call
        const data = await threatLogService.getThreatLogs(storedSerial, activeFilters);
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

  // Reactively fetch logs whenever the filters are changed
  useEffect(() => {
    fetchLogs();
  }, [activeFilters]);

  useFocusEffect(
    useCallback(() => {
      fetchLogs();
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    fetchLogs(true);
  };

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      <View style={sharedStyles.container}>
        
        {/* Header */}
        <View style={sharedStyles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={24} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={sharedStyles.headerTitle}>ACTIVITY LOGS</Text>
          <View style={{ width: 24 }} /> 
        </View>

        {/* 4. Filter Component: Positioned above the list */}
        <LogFilter onApplyFilters={(filters) => setActiveFilters(filters)} />

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
            renderItem={({ item }) => <LogEntryCard item={item} />}
            contentContainerStyle={{ paddingBottom: 20 }}
            refreshControl={
              <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={COLORS.primary} />
            }
            ListEmptyComponent={
              <View style={{ alignItems: 'center', marginTop: 50 }}>
                <Ionicons name="document-text-outline" size={60} color={COLORS.textHint} />
                <Text style={[sharedStyles.hintText, { fontSize: 16 }]}>
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