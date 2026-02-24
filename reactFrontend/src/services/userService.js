import apiClient from '../api/client';

/**
 * Service to handle all user-related interactions with the 
 * @RequestMapping("/api/user") Spring Boot controller.
 */
export const userService = {

  /**
   * Hits @PutMapping("/api/user/{id}")
   * Updates basic profile information for the authenticated user.
   * @param {number} id - The User ID
   * @param {object} updateRequest - { firstName, lastName, email }
   */
  updateProfile: async (id, updateRequest) => {
    try {
      const response = await apiClient.put(`/user/${id}`, updateRequest);
      return response.data;
    } catch (error) {
      console.error("UserService [updateProfile] Error:", error.response?.data || error.message);
      throw error;
    }
  },

  /**
   * Hits @PutMapping("/api/user/{id}/password")
   * Validates old password and updates to a new one.
   * @param {number} id - The User ID
   * @param {object} passwordRequest - { oldPassword, newPassword, confirmPassword }
   */
  updatePassword: async (id, passwordRequest) => {
    try {
      const response = await apiClient.put(`/user/${id}/password`, passwordRequest);
      return response.data;
    } catch (error) {
      console.error("UserService [updatePassword] Error:", error.response?.data || error.message);
      throw error;
    }
  },

  /**
   * Hits @PostMapping("/api/user/{id}/device/transfer")
   * Transfers OWNER role of a specific device to another existing MEMBER.
   * @param {number} id - The User ID (Current Owner)
   * @param {object} transferRequest - { deviceSerialNumber, newOwnerEmail }
   */
  transferOwnership: async (id, transferRequest) => {
    try {
      const response = await apiClient.post(`/user/${id}/device/transfer`, transferRequest);
      return response.data;
    } catch (error) {
      console.error("UserService [transferOwnership] Error:", error.response?.data || error.message);
      throw error;
    }
  }
};