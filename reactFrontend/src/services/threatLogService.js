import apiClient from '../api/client';

/**
 * Service to handle all log-related interactions with the 
 * @RequestMapping("/api/logs") Spring Boot controller.
 */
export const threatLogService = {

  /**
   * Hits @GetMapping("/threats")
   * Returns list of ThreatLogResponse DTOs for a specific device.
   * Validates user access on the backend before returning data.
   */
  getThreatLogs: async (serialNumber) => {
    try {
      const response = await apiClient.get('/logs/threats', {
        params: { serialNumber } // Sends as ?serialNumber=DEV-GAMMA-789-RPI
      });
      return response.data;
    } catch (error) {
      console.error("ThreatLogService [getThreatLogs] Error:", error.response?.data || error.message);
      throw error;
    }
  }

};