// src/components/StatusBanner.js
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING } from '../styles/theme';

export const StatusBanner = ({ 
  status, 
  message, 
  type = 'info', // 'info', 'success', 'warning', 'error', 'offline'
  icon 
}) => {
  const getColors = () => {
    switch (type) {
      case 'success':
        return { border: '#4CAF50', text: '#4CAF50' };
      case 'warning':
        return { border: '#FF9800', text: '#FF9800' };
      case 'error':
        return { border: '#FF4444', text: '#FF4444' };
      case 'offline':
        return { border: '#444', text: '#666' };
      default:
        return { border: COLORS.primary, text: COLORS.primary };
    }
  };

  const getIcon = () => {
    if (icon) return icon;
    switch (type) {
      case 'success':
        return 'checkmark-circle';
      case 'warning':
        return 'warning';
      case 'error':
        return 'alert-circle';
      case 'offline':
        return 'cloud-offline';
      default:
        return 'information-circle';
    }
  };

  const colors = getColors();

  return (
    <View style={[styles.container, { borderLeftColor: colors.border }]}>
      <Ionicons 
        name={getIcon()} 
        size={20} 
        color={colors.border} 
        style={styles.icon} 
      />
      <View style={styles.content}>
        {status && (
          <Text style={styles.status}>
            {status}
            {message && (
              <Text style={[styles.message, { color: colors.text }]}>
                {` ${message}`}
              </Text>
            )}
          </Text>
        )}
        {!status && message && (
          <Text style={[styles.message, { color: colors.text }]}>
            {message}
          </Text>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#1A1A1A',
    padding: SPACING.m,
    borderRadius: 10,
    borderLeftWidth: 4,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: SPACING.l,
  },
  icon: {
    marginRight: SPACING.m,
  },
  content: {
    flex: 1,
  },
  status: {
    color: COLORS.text,
    fontWeight: '600',
    fontSize: 15,
  },
  message: {
    fontWeight: '600',
    fontSize: 15,
  },
});