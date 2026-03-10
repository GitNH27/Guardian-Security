import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, ActivityIndicator, TouchableOpacity, RefreshControl } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import * as SecureStore from 'expo-secure-store';

import { COLORS, SPACING } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import { VideoPlayer } from '../components/VideoPlayer';
import { deviceService } from '../services/deviceService';
import { useThreatMonitor } from '../hooks/useThreatMonitor';

export default function LiveScreen({ navigation }) {
  const [activeFeeds, setActiveFeeds] = useState({});
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [deviceInfo, setDeviceInfo] = useState({ id: null, serial: null });

  const deviceIds = React.useMemo(() => {
    return deviceInfo.id ? [deviceInfo.id] : [];
  }, [deviceInfo.id]);

  const threats = useThreatMonitor(deviceIds);
  const lastThreat = deviceInfo.id ? threats[deviceInfo.id] : null;

  console.log('[LiveScreen] Device Info:', deviceInfo);
  console.log('[LiveScreen] Last Threat:', lastThreat);

  const fetchLiveStatus = async (isRefreshing = false) => {
    try {
      if (!isRefreshing) setLoading(true);

      const storedSerial = await SecureStore.getItemAsync('activeDeviceSerial');
      const storedId = await SecureStore.getItemAsync('activeDeviceId');

      if (storedSerial && storedId) {
        const numericId = Number(storedId);
        setDeviceInfo({ id: numericId, serial: storedSerial });

        const feeds = await deviceService.getLiveFeeds(storedSerial, numericId);

        const sanitizedFeeds = {};
        Object.entries(feeds).forEach(([camera, url]) => {
          if (url) {
            const cleanUrl = url.replace(/["\\]/g, '').trim();
            sanitizedFeeds[camera] = cleanUrl;
          }
        });
        console.log('[LiveScreen] Initial feeds from Redis:', sanitizedFeeds);
        setActiveFeeds(sanitizedFeeds);
      }
    } catch (error) {
      console.error('Failed to load initial live status:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchLiveStatus();
  }, []);

  // FIX: Backend broadcasts a flat ThreatRecord, not a nested ml_data object.
  // Read liveStreamUrl and cameraTopic directly from the top-level payload.
  useEffect(() => {
    if (!lastThreat) return;

    console.log('[LiveScreen] WebSocket threat received:', lastThreat);

    // Try flat field first (ThreatRecord from backend)
    // Fall back to nested ml_data in case backend sends raw payload
    const liveUrl = lastThreat.liveStreamUrl ?? lastThreat.ml_data?.liveStreamUrl;
    const topic   = lastThreat.cameraTopic   ?? lastThreat.ml_data?.cameraTopic ?? 'front';
    const level   = lastThreat.threatLevel   ?? lastThreat.ml_data?.level;
    const object  = lastThreat.objectDetected ?? lastThreat.ml_data?.object;

    console.log(`[LiveScreen] level=${level} object=${object} topic=${topic} url=${liveUrl}`);

    if (liveUrl) {
      const cleanUrl = liveUrl.replace(/["\\]/g, '').trim();
      console.log('[LiveScreen] Setting live feed URL:', cleanUrl);

      setActiveFeeds(prev => ({
        ...prev,
        [topic]: cleanUrl
      }));
    }
  }, [lastThreat]);

  const onRefresh = () => {
    setRefreshing(true);
    fetchLiveStatus(true);
  };

  return (
    <SafeAreaView style={sharedStyles.safeArea}>
      <View style={sharedStyles.container}>

        <View style={sharedStyles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={24} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={sharedStyles.headerTitle}>LIVE SURVEILLANCE</Text>
          <TouchableOpacity onPress={onRefresh}>
            <Ionicons name="refresh" size={24} color={COLORS.primary} />
          </TouchableOpacity>
        </View>

        {loading ? (
          <View style={{ flex: 1, justifyContent: 'center' }}>
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        ) : (
          <ScrollView
            contentContainerStyle={{ paddingBottom: 40 }}
            refreshControl={
              <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={COLORS.primary} />
            }
          >
            {Object.keys(activeFeeds).length === 0 ? (
              <View style={{ alignItems: 'center', marginTop: 100 }}>
                <Ionicons name="videocam-off-outline" size={80} color={COLORS.textHint} />
                <Text style={sharedStyles.hintText}>No active live streams detected.</Text>
                <Text style={[sharedStyles.hintText, { marginTop: 5 }]}>
                  Feeds activate during high-threat events.
                </Text>
              </View>
            ) : (
              <View style={sharedStyles.grid}>
                {Object.entries(activeFeeds).map(([cameraName, streamUrl]) => (
                  <View key={cameraName} style={[sharedStyles.columnHalf, { marginBottom: SPACING.m }]}>
                    <View style={sharedStyles.card}>
                      <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 }}>
                        <Text style={[sharedStyles.label, { marginBottom: 0 }]}>
                          {cameraName.toUpperCase()}
                        </Text>
                        <View style={{ backgroundColor: COLORS.error, borderRadius: 4, paddingHorizontal: 5 }}>
                          <Text style={{ color: 'white', fontSize: 10, fontWeight: 'bold' }}>LIVE</Text>
                        </View>
                      </View>

                      <VideoPlayer videoUrl={streamUrl} />

                      <TouchableOpacity
                        style={[sharedStyles.primaryButton, { marginTop: 12, paddingVertical: 8 }]}
                        onPress={() => navigation.navigate('FullStreamScreen', {
                          url: streamUrl,
                          title: cameraName
                        })}
                      >
                        <Text style={sharedStyles.primaryButtonText}>EXPAND</Text>
                      </TouchableOpacity>
                    </View>
                  </View>
                ))}
              </View>
            )}
          </ScrollView>
        )}
      </View>
    </SafeAreaView>
  );
}