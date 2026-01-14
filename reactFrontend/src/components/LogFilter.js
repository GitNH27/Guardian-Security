import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'; // Removed ScrollView
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';

const THREAT_LEVELS = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'DANGER']; 
const TOPICS = ['ALL', 'FRONT', 'BACK', 'LEFT', 'RIGHT', 'Interior'];
const OBJECT_TYPES = ['ALL', 'Person', 'Animal', 'Vehicle', 'Intruder']; 

export const LogFilter = ({ onApplyFilters }) => {
  const [selectedLevel, setSelectedLevel] = useState('ALL');
  const [selectedTopic, setSelectedTopic] = useState('ALL');
  const [selectedObject, setSelectedObject] = useState('ALL'); 
  const [isExpanded, setIsExpanded] = useState(false);

  const handleApply = (level, topic, object) => {
    const filters = {};
    if (level !== 'ALL') filters.threatLevel = level;
    if (topic !== 'ALL') filters.cameraTopic = topic.toLowerCase();
    if (object !== 'ALL') filters.objectDetected = object; 
    
    onApplyFilters(filters);
  };

  const handleReset = () => {
    setSelectedLevel('ALL');
    setSelectedTopic('ALL');
    setSelectedObject('ALL');
    onApplyFilters({});
  };

  // UPDATED: Removed ScrollView, added wrap container
  const renderChips = (data, selectedValue, onSelect) => (
    <View style={styles.chipGrid}>
      {data.map((item) => (
        <TouchableOpacity
          key={item}
          onPress={() => onSelect(item)}
          style={[
            styles.chip,
            selectedValue === item && styles.activeChip
          ]}
        >
          <Text style={[
            styles.chipText,
            selectedValue === item && styles.activeChipText
          ]}>
            {item}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  return (
    <View style={[sharedStyles.card, styles.filterWrapper]}>
      <TouchableOpacity 
        style={styles.filterHeader} 
        onPress={() => setIsExpanded(!isExpanded)}
      >
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          <Ionicons name="filter" size={20} color={COLORS.primary} />
          <Text style={[sharedStyles.label, { marginBottom: 0, marginLeft: 10 }]}>
            FILTER LOGS
          </Text>
        </View>
        <Ionicons 
          name={isExpanded ? "chevron-up" : "chevron-down"} 
          size={20} 
          color={COLORS.textHint} 
        />
      </TouchableOpacity>

      {isExpanded && (
        <View style={styles.expandedContent}>
          <View style={[sharedStyles.dividerLine, { marginVertical: SPACING.s, opacity: 0.2 }]} />
          
          <Text style={styles.sectionLabel}>Threat Level</Text>
          {renderChips(THREAT_LEVELS, selectedLevel, (val) => {
            setSelectedLevel(val);
            handleApply(val, selectedTopic, selectedObject);
          })}

          <Text style={styles.sectionLabel}>Camera Source</Text>
          {renderChips(TOPICS, selectedTopic, (val) => {
            setSelectedTopic(val);
            handleApply(selectedLevel, val, selectedObject);
          })}

          <Text style={styles.sectionLabel}>Object Type</Text>
          {renderChips(OBJECT_TYPES, selectedObject, (val) => {
            setSelectedObject(val);
            handleApply(selectedLevel, selectedTopic, val);
          })}

          <TouchableOpacity style={styles.resetButton} onPress={handleReset}>
            <Text style={styles.resetText}>RESET FILTERS</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  filterWrapper: {
    marginBottom: SPACING.m,
    backgroundColor: COLORS.surface,
    padding: SPACING.m,
  },
  filterHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  expandedContent: {
    marginTop: SPACING.s,
  },
  sectionLabel: {
    color: COLORS.textHint,
    fontSize: 11,
    fontWeight: 'bold',
    textTransform: 'uppercase',
    marginTop: SPACING.m,
    marginBottom: SPACING.s,
    letterSpacing: 1,
  },
  // NEW: Grid container for wrapping chips
  chipGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 4,
  },
  chip: {
    paddingHorizontal: 10, // Reduced padding to fit more in a row
    paddingVertical: 6,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#444',
    marginRight: 6, // Spacing between chips
    marginBottom: 8, // Spacing for wrapped rows
    backgroundColor: 'rgba(255,255,255,0.03)',
  },
  activeChip: {
    borderColor: COLORS.primary,
    backgroundColor: 'rgba(212, 175, 55, 0.1)',
  },
  chipText: {
    color: COLORS.textHint,
    fontSize: 11, // Slightly smaller text to ensure single-row fit
    fontWeight: '600',
  },
  activeChipText: {
    color: COLORS.primary,
  },
  resetButton: {
    marginTop: SPACING.m,
    paddingVertical: SPACING.s,
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: 'rgba(255,255,255,0.05)',
  },
  resetText: {
    color: '#FF4444',
    fontSize: 11,
    fontWeight: 'bold',
    letterSpacing: 1,
  }
});