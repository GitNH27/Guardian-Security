import apiClient from '../api/client';

/**
 * AuthRepository Equivalent in React Native
 */
export const authService = {
  
  // Mirrors: suspend fun sendVerificationCode(registerRequest: RegisterRequest)
  sendVerificationCode: async (registerRequest) => {
    try {
      // apiClient.post returns the equivalent of Response<VerifyUserResponse>
      const response = await apiClient.post('/verifycode', registerRequest);
      
      // Axios only enters this block if status is 2xx (response.isSuccessful)
      return response.data; 
    } catch (error) {
      // Mirrors: Result.failure(IOException(...))
      const errorMessage = error.response?.data?.message || error.message || "Failed to send verification code";
      throw new Error(errorMessage);
    }
  },

  // Mirrors: suspend fun verifyRegistration(registerRequest: RegisterRequest, code: String)
  verifyRegistration: async (registerRequest, code) => {
    try {
      const response = await apiClient.post('/verify-registration', registerRequest, {
        params: { code } // Add code as query parameter
        });
      return response.data;
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Registration verification failed";
      throw new Error(errorMessage);
    }
  },

  // Mirrors: suspend fun login(loginRequest: LoginRequest)
  login: async (loginRequest) => {
    try {
      const response = await apiClient.post('/login', loginRequest);
      
      return response.data;
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Login failed";
      throw new Error(errorMessage);
    }
  }
};