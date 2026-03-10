import React, { useRef, useState, useCallback } from 'react';
import { View, StyleSheet } from 'react-native';
import { WebView } from 'react-native-webview';
import { useFocusEffect } from '@react-navigation/native';
import { COLORS } from '../styles/theme';

export const VideoPlayer = ({ videoUrl, fullscreen = false }) => {

  const webviewRef = useRef(null);
  const [webKey, setWebKey] = useState(0);

  if (!videoUrl) return null;

  const cleanUrl = videoUrl.replace(/[\\"]/g, '').trim();

  // Reload stream when returning to screen
  useFocusEffect(
    useCallback(() => {
      setWebKey(prev => prev + 1);
    }, [])
  );

  return (
    <View style={[
      styles.container,
      fullscreen && styles.fullscreenContainer
    ]}>

      <WebView
        key={webKey}
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
        allowsFullscreenVideo={true}
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