import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Video, ResizeMode } from 'expo-av';
import { COLORS } from '../styles/theme'; //

export const VideoPlayer = ({ videoUrl }) => {
  return (
    <View style={styles.container}>
      <Video
        source={{ uri: videoUrl }}
        rate={1.0}
        volume={1.0}
        isMuted={false}
        resizeMode={ResizeMode.CONTAIN}
        shouldPlay
        isLooping={false}
        useNativeControls
        style={styles.video}
        // Error handling for dead Redis links
        onError={(error) => console.log('Video Error: ', error)}
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
    borderColor: COLORS.primary, //
  },
  video: {
    flex: 1,
  },
});