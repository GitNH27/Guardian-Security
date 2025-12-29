// src/styles/sharedStyles.js
import { StyleSheet } from 'react-native';
import { COLORS, SPACING } from './theme';

export const sharedStyles = StyleSheet.create({
  // Layout
  safeArea: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  scrollContainer: {
    flexGrow: 1,
    padding: SPACING.l,
  },
  centeredScrollContainer: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: SPACING.l,
  },
  container: {
    flex: 1,
    padding: SPACING.l,
  },

  // Headers
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: SPACING.m,
  },
  headerTitle: {
    color: COLORS.text,
    fontSize: 20,
    fontWeight: 'bold',
    letterSpacing: 1,
  },
  headerSubtitle: {
    color: COLORS.textHint,
    fontSize: 14,
    marginTop: SPACING.s,
  },

  // Cards
  card: {
    backgroundColor: '#1A1A1A',
    borderRadius: 12,
    padding: SPACING.m, // Set padding: 0 to square cards if needed
    borderWidth: 1,
    borderColor: '#333',
  },
  cardDisabled: {
    backgroundColor: '#0D0D0D',
    borderColor: '#1A1A1A',
    opacity: 0.5,
  },

  // Buttons
  primaryButton: {
    backgroundColor: COLORS.primary,
    paddingVertical: 14,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  primaryButtonText: {
    color: '#000',
    fontWeight: 'bold',
    fontSize: 16,
    letterSpacing: 0.5,
  },
  secondaryButton: {
    backgroundColor: 'transparent',
    borderWidth: 1.5,
    borderColor: COLORS.primary,
    paddingVertical: 14,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  secondaryButtonText: {
    color: COLORS.primary,
    fontWeight: 'bold',
    fontSize: 16,
    letterSpacing: 0.5,
  },

  // Text
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    color: COLORS.text,
    letterSpacing: 2,
  },
  subtitle: {
    fontSize: 16,
    color: COLORS.textHint,
    marginBottom: SPACING.m,
  },
  label: {
    color: COLORS.primary,
    fontWeight: 'bold',
    fontSize: 14,
    marginBottom: SPACING.s,
  },
  hintText: {
    color: COLORS.textHint,
    fontSize: 12,
    textAlign: 'center',
    marginTop: SPACING.m,
  },

  // Inputs
  inputWrapper: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: COLORS.primary,
    borderRadius: 8,
    paddingHorizontal: SPACING.m,
    height: 55,
    backgroundColor: 'transparent',
    marginBottom: SPACING.m,
  },
  input: {
    flex: 1,
    color: COLORS.text,
    fontSize: 16,
  },

  // Divider
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

  // Logo
  logo: {
    width: 150,
    height: 150,
    marginBottom: SPACING.m,
  },
  smallLogo: {
    width: 70,
    height: 70,
  },

  // Badges
  badge: {
    paddingHorizontal: SPACING.s,
    paddingVertical: 4,
    borderRadius: 4,
    alignSelf: 'flex-start',
  },
  badgeText: {
    fontSize: 11,
    fontWeight: 'bold',
  },

  // Status Banner
  statusBanner: {
    backgroundColor: '#1A1A1A',
    padding: SPACING.m,
    borderRadius: 10,
    borderLeftWidth: 4,
    marginBottom: SPACING.l,
  },

  // Add these to your sharedStyles.js
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    width: '100%',
  },
  columnHalf: {
    width: '48%',
  },
});