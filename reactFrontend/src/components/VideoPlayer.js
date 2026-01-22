import React from 'react';
import { View, StyleSheet } from 'react-native';
import { WebView } from 'react-native-webview';
import { COLORS } from '../styles/theme';

export const VideoPlayer = ({ videoUrl }) => {
  if (!videoUrl) return null;

  return (
    <View style={styles.container}>
      <WebView
        source={{ uri: videoUrl }}
        // 1. Critical: Allows the http://192.168... stream to load
        mixedContentMode="always" 
        // 2. Critical: Uses GPU to decode the video stream (saves battery)
        androidLayerType="hardware"
        // 3. Fixes the "Zoomed in" scaling issue
        scalesPageToFit={true}
        // 4. Ensures video doesn't white-flash while loading
        style={{ backgroundColor: 'black' }}
        javaScriptEnabled
        domStorageEnabled
        allowsInlineMediaPlayback
        mediaPlaybackRequiresUserAction={false}
      />
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    height: 150,
    backgroundColor: '#000',
    borderRadius: 8,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: COLORS.primary,
  },
});
