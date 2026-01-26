import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Image, Linking, Alert, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

// Added userEmail as a prop to handle the dispatch destination
export const LogEntryCard = ({ item, userEmail }) => {
  const [isSending, setIsSending] = useState(false);

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

  // --- NEW EMAIL FUNCTION ---
  const handleEmailThreatLog = async () => {
    if (!item.photoUrl) {
      Alert.alert("Unavailable", "Only records with captured images can be emailed.");
      return;
    }

    setIsSending(true);
    try {
      // Calls your @PostMapping("/email-threat-log")
      const response = await fetch('http://YOUR_BACKEND_IP:8080/api/logs/email-threat-log', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          logId: item.id,
          email: userEmail 
        }),
      });

      if (response.ok) {
        Alert.alert("Success", "Security report has been sent to your email.");
      } else {
        throw new Error("Server error while sending email.");
      }
    } catch (error) {
      Alert.alert("Error", "Failed to dispatch email. Check your connection.");
    } finally {
      setIsSending(false);
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

        {/* --- ACTIONS SECTION --- */}
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          <Text style={[sharedStyles.headerSubtitle, { marginRight: 15 }]}>{formattedDate}</Text>
          
          {/* Only show email button if there is a photoUrl */}
          {item.photoUrl && (
            isSending ? (
              <ActivityIndicator size="small" color={COLORS.primary} />
            ) : (
              <TouchableOpacity onPress={handleEmailThreatLog}>
                <Ionicons name="mail-outline" size={20} color={COLORS.primary} />
              </TouchableOpacity>
            )
          )}
        </View>
      </View>
    </View>
  );
};