// src/components/FormSection.js
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS, SPACING } from '../styles/theme';

export const FormSection = ({ title, subtitle, children, style }) => {
  return (
    <View style={[styles.container, style]}>
      {title && <Text style={styles.title}>{title}</Text>}
      {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
      <View style={styles.content}>
        {children}
      </View>
    </View>
  );
};

export const Divider = ({ text }) => {
  return (
    <View style={styles.divider}>
      <View style={styles.dividerLine} />
      {text && <Text style={styles.dividerText}>{text}</Text>}
      <View style={styles.dividerLine} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
    marginBottom: SPACING.l,
  },
  title: {
    color: COLORS.primary,
    fontWeight: 'bold',
    fontSize: 16,
    marginBottom: SPACING.s,
  },
  subtitle: {
    color: COLORS.textHint,
    fontSize: 14,
    marginBottom: SPACING.m,
  },
  content: {
    width: '100%',
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    marginVertical: SPACING.l,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: COLORS.primary,
    opacity: 0.4,
  },
  dividerText: {
    color: COLORS.text,
    paddingHorizontal: SPACING.m,
    fontWeight: 'bold',
  },
});