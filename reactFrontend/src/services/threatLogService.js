import apiClient from '../api/client';

/**
 * Service to handle all log-related interactions with the 
 * @RequestMapping("/api/logs") Spring Boot controller.
 */
export const threatLogService = {

/**
 * Hits @GetMapping("/api/logs/threats")
 * Fetches threat logs with optional filtering.
 * @param {string} serialNumber - Mandatory
 * @param {object} filters - Optional: { cameraTopic, threatLevel, objectDetected, start, end }
 */
getThreatLogs: async (serialNumber, filters = {}) => {
    try {
      const response = await apiClient.get('/logs/threats', {
        params: { 
          serialNumber, 
          ...filters // Dynamically adds cameraTopic, threatLevel, etc. if present
        }
      });
      return response.data;
    } catch (error) {
      console.error("ThreatLogService [getThreatLogs] Error:", error.response?.data || error.message);
      throw error;
    }
  }
};