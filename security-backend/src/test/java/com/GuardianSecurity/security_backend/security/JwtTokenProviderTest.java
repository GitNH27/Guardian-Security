// package com.GuardianSecurity.security_backend.security;

// import com.GuardianSecurity.security_backend.model.User;
// import com.GuardianSecurity.security_backend.service.AuthService;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.core.userdetails.UserDetails;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// /**
//  * Unit tests for JWT token provider
//  * 
//  * Test coverage:
//  * - JWT token generation
//  * - JWT token validation
//  * - Token expiration handling
//  * - User information extraction
//  * - Token signature verification
//  * - Invalid token handling
//  * 
//  * Mock dependencies:
//  * - UserDetails for token generation
//  * - AuthService for user validation
//  * 
//  * Test scenarios:
//  * - Successful token generation
//  * - Successful token validation
//  * - Expired token validation
//  * - Invalid token validation
//  * - User information extraction
//  * - Token signature verification
//  */
// class JwtTokenProviderTest {
    
//     @Mock
//     private UserDetails userDetails;
    
//     @Mock
//     private AuthService authService;
    
//     private JwtTokenProvider jwtTokenProvider;
    
//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         jwtTokenProvider = new JwtTokenProvider();
//         // Set test properties
//         jwtTokenProvider.setJwtSecret("testSecretKey123456789012345678901234567890");
//         jwtTokenProvider.setJwtExpirationMs(900000); // 15 minutes
//     }
    
//     /**
//      * Test JWT token generation
//      * - Valid user details provided
//      * - Token generated successfully
//      * - Token contains correct claims
//      * - Token is properly signed
//      */
//     @Test
//     void testGenerateToken_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test JWT token generation with custom claims
//      * - Custom claims provided
//      * - Token generated with custom claims
//      * - Standard claims included
//      * - Token properly signed
//      */
//     @Test
//     void testGenerateToken_WithCustomClaims() {
//         // Implementation needed
//     }
    
//     /**
//      * Test username extraction from token
//      * - Valid token provided
//      * - Username extracted correctly
//      * - Return user email
//      */
//     @Test
//     void testExtractUsername_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test username extraction from invalid token
//      * - Invalid token provided
//      * - Exception thrown
//      * - Error handled appropriately
//      */
//     @Test
//     void testExtractUsername_InvalidToken() {
//         // Implementation needed
//     }
    
//     /**
//      * Test token validation with valid token
//      * - Valid token provided
//      * - Valid user details
//      * - Token validation succeeds
//      * - Return true
//      */
//     @Test
//     void testValidateToken_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test token validation with expired token
//      * - Expired token provided
//      * - Token validation fails
//      * - Return false
//      */
//     @Test
//     void testValidateToken_Expired() {
//         // Implementation needed
//     }
    
//     /**
//      * Test token validation with invalid signature
//      * - Token with invalid signature
//      * - Token validation fails
//      * - Return false
//      */
//     @Test
//     void testValidateToken_InvalidSignature() {
//         // Implementation needed
//     }
    
//     /**
//      * Test token expiration checking
//      * - Valid token
//      * - Expired token
//      * - Return correct expiration status
//      */
//     @Test
//     void testIsTokenExpired() {
//         // Implementation needed
//     }
    
//     /**
//      * Test claim extraction from token
//      * - Valid token with claims
//      * - Specific claim extracted
//      * - Return claim value
//      */
//     @Test
//     void testExtractClaim() {
//         // Implementation needed
//     }
// }
