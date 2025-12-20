import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { COLORS, SPACING } from '../styles/theme'; // Ensure this is the correct path

export default function AppButton({ title, onPress, variant = 'primary' }) {
  return (
    <TouchableOpacity 
      style={[
        styles.button, 
        { backgroundColor: variant === 'primary' ? COLORS.primary : COLORS.secondary }
      ]} 
      onPress={onPress}
    >
      <Text style={styles.text}>{title}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  button: {
    width: '100%',
    padding: SPACING.m,
    borderRadius: 12,
    alignItems: 'center',
    marginVertical: SPACING.s,
    elevation: 3,
  },
  text: {
    color: COLORS.white,
    fontSize: 16,
    fontWeight: '700',
  },
});