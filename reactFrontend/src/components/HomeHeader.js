import { View, Text, Image, TouchableOpacity } from 'react-native';
import { sharedStyles } from '../styles/sharedStyles'; // Use your shared styles
import { COLORS } from '../styles/theme';
import { Ionicons } from '@expo/vector-icons';

export default function HomeHeader({ userName, activeDevice, hasDevice, role, onSwitchDevice }) {
  return (
    <View style={sharedStyles.header}>
      <Image 
        source={require('../../assets/logo.png')} 
        style={sharedStyles.smallLogo} 
        resizeMode="contain" 
      />
      <View style={{ flex: 1 }}>
        <Text style={sharedStyles.headerSubtitle}>Welcome Home,</Text>
        <Text style={sharedStyles.headerTitle}>{userName}!</Text>
        <Text style={[
            sharedStyles.label,
            { fontSize: 11, marginTop: 4 },
            !hasDevice && { color: '#888' }
        ]}
        >
        {hasDevice && activeDevice
            ? `${activeDevice.serialNumber} • ${role}`
            : 'No Active Device'}
        </Text>

      </View>

      {hasDevice && (
        <TouchableOpacity onPress={onSwitchDevice}>
          <Ionicons name="car-outline" size={28} color={COLORS.primary} />
        </TouchableOpacity>
      )}
    </View>
  );
}