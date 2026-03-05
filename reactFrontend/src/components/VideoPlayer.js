import React, { useRef } from 'react';
import { View, StyleSheet } from 'react-native';
import { WebView } from 'react-native-webview';
import { useFocusEffect } from '@react-navigation/native';
import { COLORS } from '../styles/theme';

export const VideoPlayer = ({ videoUrl, fullscreen = false }) => {

  const webviewRef = useRef(null);

  if (!videoUrl) return null;

  const cleanUrl = videoUrl.replace(/[\\"]/g, '').trim();

  // Reload stream when returning to screen
  useFocusEffect(
    React.useCallback(() => {
      if (webviewRef.current) {
        webviewRef.current.reload();
      }
    }, [])
  );

  return (
    <View style={[
      styles.container,
      fullscreen && styles.fullscreenContainer
    ]}>

      <WebView
        ref={webviewRef}
        source={{ uri: cleanUrl }}
        originWhitelist={['*']}
        javaScriptEnabled
        domStorageEnabled
        allowsInlineMediaPlayback
        mediaPlaybackRequiresUserAction={false}
        mixedContentMode="always"
        cacheEnabled={false}
        sharedCookiesEnabled
        style={{ backgroundColor: 'black' }}
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

  fullscreenContainer: {
    flex: 1,
    height: '100%',
    borderWidth: 0,
    borderRadius: 0,
  }

});