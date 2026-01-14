import React from 'react';
import { View, Text } from 'react-native'; // Fix: View added to prevent ReferenceError
import { Ionicons } from '@expo/vector-icons';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

export const StatusBanner = ({ 
  status, 
  message, 
  type = 'OFFLINE' 
}) => {
  
  // Map backend DTO statuses to your UI types
  const getUiType = () => {
    const normalized = type?.toUpperCase();
    if (normalized === 'DANGER') return 'error';
    if (normalized === 'ACTIVE') return 'success';
    return 'offline';
  };

  const uiType = getUiType();

  const getColors = () => {
    switch (uiType) {
      case 'success': return { border: '#4CAF50', text: '#4CAF50' };
      case 'error':   return { border: '#FF4444', text: '#FF4444' };
      case 'offline': return { border: '#444444', text: '#666666' };
      default:        return { border: COLORS.primary, text: COLORS.primary };
    }
  };

  const getIcon = () => {
    switch (uiType) {
      case 'success': return 'checkmark-circle';
      case 'error':   return 'alert-circle';
      case 'offline': return 'cloud-offline';
      default:        return 'information-circle';
    }
  };

  const colors = getColors();

  return (
    <View style={[
      sharedStyles.statusBanner, 
      { 
        borderLeftColor: colors.border, 
        flexDirection: 'row', 
        alignItems: 'center',
        backgroundColor: COLORS.surface, // Matches your app's theme
      }
    ]}>
      <Ionicons 
        name={getIcon()} 
        size={20} 
        color={colors.border} 
        style={{ marginRight: 12 }} 
      />
      <View style={{ flex: 1 }}>
        <Text style={[sharedStyles.headerTitle, { fontSize: 15, letterSpacing: 0, color: COLORS.text }]}>
            {String(status || '')}
            {message ? (
                <Text style={{ color: colors.text, fontWeight: '600' }}>
                {' '}{String(message)}
                </Text>
            ) : null}
        </Text>
      </View>
    </View>
  );
};