// package com.GuardianSecurity.security_backend.service;

// import com.GuardianSecurity.security_backend.model.User;
// import com.GuardianSecurity.security_backend.repository.UserRepository;
// import com.GuardianSecurity.security_backend.dto.RegisterRequest;
// import com.GuardianSecurity.security_backend.dto.LoginRequest;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// /**
//  * Unit tests for AuthService
//  * 
//  * Test coverage:
//  * - User registration with valid data
//  * - User registration with duplicate email
//  * - User login with valid credentials
//  * - User login with invalid credentials
//  * - JWT token generation and validation
//  * - Password hashing verification
//  * - UserDetailsService implementation
//  * 
//  * Mock dependencies:
//  * - UserRepository for database operations
//  * - PasswordEncoder for password hashing
//  * - JwtTokenProvider for token operations
//  * 
//  * Test scenarios:
//  * - Successful registration flow
//  * - Successful login flow
//  * - Error handling for invalid inputs
//  * - Security validation (password hashing)
//  * - Token validation and expiration
//  */
// class AuthServiceTest {
    
//     @Mock
//     private UserRepository userRepository;
    
//     @Mock
//     private PasswordEncoder passwordEncoder;
    
//     @Mock
//     private JwtTokenProvider jwtTokenProvider;
    
//     private AuthService authService;
    
//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
//     }
    
//     /**
//      * Test successful user registration
//      * - Valid registration request
//      * - Email uniqueness check
//      * - Password hashing
//      * - User entity creation
//      * - JWT token generation
//      */
//     @Test
//     void testRegisterUser_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test registration with duplicate email
//      * - Duplicate email in request
//      * - Repository returns existing user
//      * - Service throws appropriate exception
//      */
//     @Test
//     void testRegisterUser_DuplicateEmail() {
//         // Implementation needed
//     }
    
//     /**
//      * Test successful user login
//      * - Valid login credentials
//      * - User found in repository
//      * - Password verification
//      * - JWT token generation
//      */
//     @Test
//     void testLoginUser_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test login with invalid credentials
//      * - Invalid email or password
//      * - User not found or password mismatch
//      * - Service throws authentication exception
//      */
//     @Test
//     void testLoginUser_InvalidCredentials() {
//         // Implementation needed
//     }
    
//     /**
//      * Test JWT token validation
//      * - Valid token provided
//      * - Token signature verification
//      * - User extraction from token
//      * - Security context setup
//      */
//     @Test
//     void testValidateToken_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test JWT token validation with expired token
//      * - Expired token provided
//      * - Token validation fails
//      * - Service throws appropriate exception
//      */
//     @Test
//     void testValidateToken_Expired() {
//         // Implementation needed
//     }
    
//     /**
//      * Test UserDetailsService implementation
//      * - Load user by email
//      * - Return UserDetails object
//      * - Handle user not found case
//      */
//     @Test
//     void testLoadUserByUsername_Success() {
//         // Implementation needed
//     }
// }
