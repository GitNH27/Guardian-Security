import React, { useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from '@react-navigation/native';

import { VideoPlayer } from '../components/VideoPlayer';
import { COLORS, SPACING } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import { setNotificationSilence } from '../services/notificationService';

export default function FullStreamScreen({ route, navigation }) {
  const { url, title } = route.params;

  useFocusEffect(
    useCallback(() => {
      setNotificationSilence(true);
      console.log(`[FullStreamScreen] Silence Active for: ${title}`);

      return () => {
        // Delayed release prevents a gap if navigating back to LiveScreen,
        // which also sets silence — without this, there's a window where
        // both screens have released silence before LiveScreen re-acquires it.
        setTimeout(() => {
          setNotificationSilence(false);
          console.log('[FullStreamScreen] Silence Released');
        }, 100);
      };
    }, [title])
  );

  return (
    <View style={styles.container}>
      {/* Header Overlaid on Video */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Ionicons name="close-circle" size={35} color="white" />
        </TouchableOpacity>
        <Text style={styles.streamTitle}>{title?.toUpperCase() || 'LIVE FEED'}</Text>
      </View>

      {/* Main Video View */}
      <View style={styles.videoContainer}>
        <VideoPlayer 
          videoUrl={url}
          fullscreen
        />
      </View>

      <View style={styles.footer}>
        <View style={styles.liveIndicator}>
          <View style={styles.dot} />
          <Text style={styles.liveText}>LIVE STREAM ACTIVE</Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'black',
  },
  header: {
    position: 'absolute',
    top: 50,
    left: 20,
    right: 20,
    flexDirection: 'row',
    alignItems: 'center',
    zIndex: 10,
  },
  streamTitle: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
    marginLeft: 15,
    textShadowColor: 'rgba(0, 0, 0, 0.75)',
    textShadowOffset: { width: -1, height: 1 },
    textShadowRadius: 10
  },
  videoContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  fullVideo: {
    width: '100%',
    height: '100%',
  },
  footer: {
    position: 'absolute',
    bottom: 40,
    alignSelf: 'center',
  },
  liveIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 0, 0, 0.2)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: COLORS.error,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: COLORS.error,
    marginRight: 8,
  },
  liveText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  }
});