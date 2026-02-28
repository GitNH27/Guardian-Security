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
        // Keep this for now if your video stream isn't https yet
        mixedContentMode="always" 
        androidLayerType="hardware"
        scalesPageToFit={true}
        style={{ backgroundColor: 'black' }}
        // ADDED: Helps with Azure/Cloud authentication if needed
        sharedCookiesEnabled={true} 
        // ADDED: Standard for modern web-based video players
        originWhitelist={['*']} 
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
