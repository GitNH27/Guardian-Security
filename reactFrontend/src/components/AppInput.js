// src/components/AppInput.js
import React from 'react';
import { View, TextInput, StyleSheet, Text } from 'react-native';
import { COLORS, SPACING } from '../styles/theme';
import { Ionicons } from '@expo/vector-icons'; // Built into Expo

export default function AppInput({ icon, label, ...rest }) {
  return (
    <View style={styles.container}>
      <View style={styles.inputWrapper}>
        {icon && <Ionicons name={icon} size={20} color={COLORS.primary} style={styles.icon} />}
        <TextInput 
          style={styles.input} 
          placeholderTextColor={COLORS.textHint}
          {...rest} 
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { marginBottom: SPACING.m },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.textHint,
    borderRadius: 8,
    paddingHorizontal: 12,
    height: 55,
  },
  icon: { marginRight: 10 },
  input: {
    flex: 1,
    color: COLORS.text,
    fontSize: 16,
  },
});