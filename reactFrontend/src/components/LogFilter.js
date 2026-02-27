import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SPACING } from '../styles/theme';
import { sharedStyles } from '../styles/sharedStyles';
import DateTimePickerModal from "react-native-modal-datetime-picker";

const THREAT_LEVELS = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'DANGER']; 
const TOPICS = ['ALL', 'FRONT', 'BACK', 'LEFT', 'RIGHT', 'Interior'];
const OBJECT_TYPES = ['ALL', 'Person', 'Animal', 'Vehicle', 'Intruder']; 

export const LogFilter = ({ onApplyFilters }) => {
  // Local UI State
  const [selectedLevel, setSelectedLevel] = useState('ALL');
  const [selectedTopic, setSelectedTopic] = useState('ALL');
  const [selectedObject, setSelectedObject] = useState('ALL'); 
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  
  const [isExpanded, setIsExpanded] = useState(false);
  const [isStartPickerVisible, setStartPickerVisibility] = useState(false);
  const [isEndPickerVisible, setEndPickerVisibility] = useState(false);

  // Triggered ONLY by the "Apply Filters" button
  const handleApplyAction = () => {
    const filters = {};
    if (selectedLevel !== 'ALL') filters.threatLevel = selectedLevel;
    if (selectedTopic !== 'ALL') filters.cameraTopic = selectedTopic.toLowerCase();
    if (selectedObject !== 'ALL') filters.objectDetected = selectedObject;
    if (startDate) filters.start = startDate.toISOString();
    if (endDate) filters.end = endDate.toISOString();
    
    onApplyFilters(filters);
    // Optional: Close filter after applying
    // setIsExpanded(false); 
  };

  const handleReset = () => {
    setSelectedLevel('ALL');
    setSelectedTopic('ALL');
    setSelectedObject('ALL');
    setStartDate(null);
    setEndDate(null);
    onApplyFilters({});
  };

  const renderChips = (data, selectedValue, onSelect) => (
    <View style={styles.chipGrid}>
      {data.map((item) => (
        <TouchableOpacity
          key={item}
          onPress={() => onSelect(item)} // Now only updates local UI state
          style={[styles.chip, selectedValue === item && styles.activeChip]}
        >
          <Text style={[styles.chipText, selectedValue === item && styles.activeChipText]}>
            {item}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  return (
    <View key={isExpanded ? 'expanded' : 'collapsed'} style={[sharedStyles.card, styles.filterWrapper]}>
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
        <Ionicons name={isExpanded ? "chevron-up" : "chevron-down"} size={20} color={COLORS.textHint} />
      </TouchableOpacity>

      {isExpanded && (
        <View style={styles.expandedContent}>
          <View style={[sharedStyles.dividerLine, { marginVertical: SPACING.s, opacity: 0.2 }]} />
          
          <Text style={styles.sectionLabel}>Time Range</Text>
          <View style={styles.dateRow}>
            <TouchableOpacity 
              style={[styles.dateSelector, startDate && styles.activeDateSelector]} 
              onPress={() => setStartPickerVisibility(true)}
            >
              <Ionicons name="calendar-outline" size={14} color={startDate ? COLORS.primary : COLORS.textHint} />
              <Text style={[styles.dateText, startDate && styles.activeDateText]}>
                {startDate ? startDate.toLocaleDateString() : 'START DATE'}
              </Text>
            </TouchableOpacity>

            <TouchableOpacity 
              style={[styles.dateSelector, endDate && styles.activeDateSelector]} 
              onPress={() => setEndPickerVisibility(true)}
            >
              <Ionicons name="calendar-outline" size={14} color={endDate ? COLORS.primary : COLORS.textHint} />
              <Text style={[styles.dateText, endDate && styles.activeDateText]}>
                {endDate ? endDate.toLocaleDateString() : 'END DATE'}
              </Text>
            </TouchableOpacity>
          </View>

          <DateTimePickerModal
            isVisible={isStartPickerVisible}
            mode="datetime"
            onConfirm={(date) => { setStartDate(date); setStartPickerVisibility(false); }}
            onCancel={() => setStartPickerVisibility(false)}
            themeVariant="dark"
          />
          <DateTimePickerModal
            isVisible={isEndPickerVisible}
            mode="datetime"
            onConfirm={(date) => { setEndDate(date); setEndPickerVisibility(false); }}
            onCancel={() => setEndPickerVisibility(false)}
            themeVariant="dark"
          />

          <Text style={styles.sectionLabel}>Threat Level</Text>
          {renderChips(THREAT_LEVELS, selectedLevel, setSelectedLevel)}

          <Text style={styles.sectionLabel}>Camera Source</Text>
          {renderChips(TOPICS, selectedTopic, setSelectedTopic)}

          <Text style={styles.sectionLabel}>Object Type</Text>
          {renderChips(OBJECT_TYPES, selectedObject, setSelectedObject)}

          {/* ACTION BUTTONS */}
          <View style={styles.actionRow}>
            <TouchableOpacity style={styles.applyButton} onPress={handleApplyAction}>
              <Text style={styles.applyButtonText}>APPLY FILTERS</Text>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.resetButton} onPress={handleReset}>
              <Text style={styles.resetText}>RESET</Text>
            </TouchableOpacity>
          </View>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  filterWrapper: { marginBottom: SPACING.m, backgroundColor: COLORS.surface, padding: SPACING.m },
  filterHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  expandedContent: { marginTop: SPACING.s },
  sectionLabel: { color: COLORS.textHint, fontSize: 11, fontWeight: 'bold', textTransform: 'uppercase', marginTop: SPACING.m, marginBottom: SPACING.s, letterSpacing: 1 },
  chipGrid: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 4 },
  chip: { paddingHorizontal: 10, paddingVertical: 6, borderRadius: 6, borderWidth: 1, borderColor: '#444', marginRight: 6, marginBottom: 8, backgroundColor: 'rgba(255,255,255,0.03)' },
  activeChip: { borderColor: COLORS.primary, backgroundColor: 'rgba(212, 175, 55, 0.1)' },
  chipText: { color: COLORS.textHint, fontSize: 11, fontWeight: '600' },
  activeChipText: { color: COLORS.primary },
  
  dateRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 4 },
  dateSelector: { flex: 0.48, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(255,255,255,0.03)', paddingVertical: 10, borderRadius: 6, borderWidth: 1, borderColor: '#444' },
  activeDateSelector: { borderColor: COLORS.primary, backgroundColor: 'rgba(212, 175, 55, 0.1)' },
  dateText: { color: COLORS.textHint, fontSize: 11, fontWeight: 'bold', marginLeft: 8 },
  activeDateText: { color: COLORS.primary },

  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: SPACING.l,
    paddingTop: SPACING.m,
    borderTopWidth: 1,
    borderTopColor: 'rgba(255,255,255,0.05)',
  },
  applyButton: {
    flex: 2,
    backgroundColor: COLORS.primary,
    paddingVertical: 12,
    borderRadius: 6,
    alignItems: 'center',
  },
  applyButtonText: {
    color: '#000',
    fontWeight: 'bold',
    fontSize: 12,
    letterSpacing: 1,
  },
  resetButton: {
    flex: 1,
    alignItems: 'center',
  },
  resetText: {
    color: '#FF4444',
    fontSize: 11,
    fontWeight: 'bold',
    letterSpacing: 1,
  }
});