import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles'; // 1. Import shared styles

export default function DashboardCard({ 
  title, 
  icon, 
  onPress, 
  disabled = false, 
  color = COLORS.primary, 
  isFullWidth = false 
}) {
  return (
    <TouchableOpacity
      style={[
        sharedStyles.card, // 2. Use base card style from sharedStyles
        isFullWidth ? styles.wideCard : styles.squareCard,
        disabled && sharedStyles.cardDisabled // 3. Use shared disabled style
      ]}
      onPress={onPress}
      disabled={disabled}
    >
      <Ionicons 
        name={icon} 
        size={isFullWidth ? 26 : 32} 
        color={disabled ? '#444' : color} 
      />
      <Text
        style={[
          isFullWidth ? styles.wideCardText : styles.squareCardText, 
          disabled && styles.cardDisabledText
        ]}
        numberOfLines={1}
      >
        {title}
      </Text>
      {disabled && (
        <Ionicons name="lock-closed" size={14} color="#555" style={styles.lockIcon} />
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  squareCard: {
    width: '48%',
    aspectRatio: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 15,
    // Note: Background, border, and radius now come from sharedStyles.card
  },
  wideCard: {
    width: '100%',
    height: 70,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 12,
    // Note: Background, border, and radius now come from sharedStyles.card
  },
  squareCardText: { 
    color: '#FFFFFF', 
    marginTop: 10, 
    fontWeight: '600', 
    fontSize: 14 
  },
  wideCardText: { 
    color: '#FFFFFF', 
    marginLeft: 15, 
    fontWeight: '600', 
    fontSize: 16 
  },
  cardDisabledText: { 
    color: '#444' 
  },
  lockIcon: { 
    position: 'absolute', 
    top: 12, 
    right: 12 
  }
});