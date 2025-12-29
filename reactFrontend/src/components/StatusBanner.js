import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

export const StatusBanner = ({ 
  status, 
  message, 
  type = 'info' // 'info', 'success', 'warning', 'error', 'offline'
}) => {
  const getColors = () => {
    switch (type) {
      case 'success': return { border: '#4CAF50', text: '#4CAF50' };
      case 'warning': return { border: '#FF9800', text: '#FF9800' };
      case 'error':   return { border: '#FF4444', text: '#FF4444' };
      case 'offline': return { border: '#444444', text: '#666666' };
      default:        return { border: COLORS.primary, text: COLORS.primary };
    }
  };

  const getIcon = () => {
    switch (type) {
      case 'success': return 'checkmark-circle';
      case 'warning': return 'warning';
      case 'error':   return 'alert-circle';
      case 'offline': return 'cloud-offline';
      default:        return 'information-circle';
    }
  };

  const colors = getColors();

  return (
    <View style={[sharedStyles.statusBanner, { borderLeftColor: colors.border, flexDirection: 'row', alignItems: 'center' }]}>
      <Ionicons 
        name={getIcon()} 
        size={20} 
        color={colors.border} 
        style={{ marginRight: 12 }} 
      />
      <View style={{ flex: 1 }}>
        <Text style={[sharedStyles.headerTitle, { fontSize: 15, letterSpacing: 0 }]}>
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