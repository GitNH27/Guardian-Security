import { View, Text, Image } from 'react-native';
import { sharedStyles } from '../styles/sharedStyles';
import { COLORS } from '../styles/theme';

export default function HomeHeader({ userName, activeDevice, hasDevice, role }) {
  return (
    <View style={{ 
      flexDirection: 'row', 
      alignItems: 'center', 
      paddingVertical: 15,
      paddingHorizontal: 5,
      marginBottom: 10,
      width: '100%'
    }}>
      {/* Logo on the Left */}
      <Image 
        source={require('../../assets/logo.png')} 
        style={{
          width: 60,   // Adjust based on your logo shape
          height: 60,
          marginRight: 15
        }} 
        resizeMode="contain" 
      />

      {/* Text Info on the Right */}
      <View style={{ flex: 1, justifyContent: 'center' }}>
        <Text style={{ 
          color: COLORS.textHint || '#888', 
          fontSize: 14, 
          marginBottom: -2 
        }}>
          Welcome Home,
        </Text>
        
        <Text style={{ 
          color: COLORS.text, 
          fontSize: 22, 
          fontWeight: 'bold' 
        }}>
          {userName}!
        </Text>

        {/* Device Status Tag */}
        <View style={{ 
          flexDirection: 'row', 
          alignItems: 'center', 
          marginTop: 4 
        }}>
          <View style={{
            paddingHorizontal: 8,
            paddingVertical: 2,
            borderRadius: 6,
            backgroundColor: hasDevice ? 'rgba(212, 175, 55, 0.15)' : '#f0f0f0',
            borderWidth: 0.5,
            borderColor: hasDevice ? COLORS.primary : '#ccc'
          }}>
            <Text style={{ 
              fontSize: 10, 
              color: hasDevice ? COLORS.primary : '#666', 
              fontWeight: '700',
              letterSpacing: 0.5
            }}>
              {hasDevice && activeDevice
                ? `${activeDevice.serialNumber} • ${role}`
                : 'NO ACTIVE DEVICE'}
            </Text>
          </View>
        </View>
      </View>
    </View>
  );
}