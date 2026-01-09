import apiClient from '../api/client';

/**
 * Service to handle all device-related interactions with the 
 * @RequestMapping("/api/device") Spring Boot controller.
 */
export const deviceService = {

  /**
   * Hits @PostMapping("/claim") 
   * Used to register a new device hardware ownership via pairing password.
   */
  claimDevice: async (claimRequest) => {
    try {
      const response = await apiClient.post('/device/claim', claimRequest);
      return response.data;
    } catch (error) {
      console.error("DeviceService [claimDevice] Error:", error);
      throw error;
    }
  },

  /**
   * Hits @PostMapping("/requestAccess")
   * Used for non-owners to request permission to join an existing device.
   */
  requestAccess: async (accessRequest) => {
    try {
      const response = await apiClient.post('/device/requestAccess', accessRequest);
      return response.data;
    } catch (error) {
      console.error("DeviceService [requestAccess] Error:", error);
      throw error;
    }
  },

  /**
   * Hits @PostMapping("/memberAccessDecision")
   * Used by Owners to Approve or Deny a pending access request.
   */
  memberAccessDecision: async (decisionRequest) => {
    try {
      const response = await apiClient.post('/device/memberAccessDecision', decisionRequest);
      return response.data;
    } catch (error) {
      console.error("DeviceService [memberAccessDecision] Error:", error);
      throw error;
    }
  },

  /**
   * Hits @GetMapping("/myDevices")
   * Critical for the Hybrid Approach login logic to determine navigation.
   */
  getDeviceSelectionContext: async () => {
    try {
      const response = await apiClient.get('/device/myDevices');
      return response.data;
    } catch (error) {
      console.error("DeviceService [getDeviceSelectionContext] Error:", error);
      throw error;
    }
  },

  // src/services/deviceService.js
  fetchPendingRequests: async (serialNumber) => {
    try {
      const response = await apiClient.get('/device/pendingRequests', {
        params: { serialNumber }  // Correctly sends ?serialNumber=...
      });
      return response.data;
    } catch (error) {
      console.error("DeviceService Error:", error.response?.data || error.message);
      throw error;
    }
  },

  // Determine Live Feed Status for Device Cameras
  getLiveFeeds: async (serialNumber, deviceId) => {
    try {
      const response = await apiClient.get(`/device/liveFeed`, {
        params: { serialNumber, deviceId }
      });
      return response.data.activeFeeds || {};
    } catch (error) {
      console.error("Failed to fetch live feeds", error);
      return {};
    }
  }
};