import React from 'react';
import { View, Text, TouchableOpacity, Image, Linking, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

export const LogEntryCard = ({ item }) => {
  // Logic to color-code based on threat_level from database
  const getThreatColor = (level) => {
    switch (level?.toUpperCase()) {
      case 'HIGH': return '#FF4444';
      case 'MEDIUM': return COLORS.primary;
      case 'LOW': return '#44FF44';
      default: return COLORS.textHint;
    }
  };

  const handleViewImage = () => {
    if (item.photoUrl) {
      Linking.openURL(item.photoUrl).catch(() =>   
        Alert.alert("Error", "Could not open image link.")
      );
    } else {
      Alert.alert("No Image", "No photo was captured for this event.");
    }
  };

  const formattedDate = new Date(item.timestamp).toLocaleString([], { 
    hour: '2-digit', 
    minute: '2-digit', 
    month: 'short', 
    day: 'numeric' 
  });

  return (
    <View style={[
      sharedStyles.card, 
      { marginBottom: 12, borderLeftWidth: 4, borderLeftColor: getThreatColor(item.threatLevel) }
    ]}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <View style={{ flex: 1 }}>
          <Text style={[sharedStyles.label, { color: getThreatColor(item.threatLevel), marginBottom: 2 }]}>
            {item.threatLevel} THREAT
          </Text>
          <Text style={[sharedStyles.headerTitle, { fontSize: 18 }]}>
            {item.objectDetected || 'Unknown Object'}
          </Text>
        </View>

        {/* Image Preview - Uses photo_url from threat_records */}
        {item.photoUrl ? (
          <TouchableOpacity onPress={handleViewImage}>
            <Image 
              source={{ uri: item.photoUrl }} 
              style={{ width: 60, height: 60, borderRadius: 8, backgroundColor: '#000' }} 
            />
          </TouchableOpacity>
        ) : (
          <Ionicons name="image-outline" size={30} color={COLORS.textHint} />
        )}
      </View>

      <View style={[sharedStyles.divider, { marginVertical: 10, height: 0.5, opacity: 0.2 }]} />

      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          <Ionicons name="videocam-outline" size={14} color={COLORS.textHint} />
          <Text style={[sharedStyles.headerSubtitle, { marginTop: 0, marginLeft: 5 }]}>
            {item.cameraTopic}
          </Text>
        </View>
        <Text style={sharedStyles.headerSubtitle}>{formattedDate}</Text>
      </View>
    </View>
  );
};