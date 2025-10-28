// package com.GuardianSecurity.security_backend.service;

// import com.GuardianSecurity.security_backend.model.Device;
// import com.GuardianSecurity.security_backend.model.User;
// import com.GuardianSecurity.security_backend.model.DeviceAccess;
// import com.GuardianSecurity.security_backend.repository.DeviceRepository;
// import com.GuardianSecurity.security_backend.repository.DeviceAccessRepository;
// import com.GuardianSecurity.security_backend.dto.CommandRequest;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// /**
//  * Unit tests for CommandService
//  * 
//  * Test coverage:
//  * - Command execution with authorized user and online device
//  * - Command execution with unauthorized user
//  * - Command execution with offline device
//  * - Command validation
//  * - Device online status checking
//  * - WebSocket command forwarding
//  * - Command acknowledgment handling
//  * 
//  * Mock dependencies:
//  * - DeviceRepository for device operations
//  * - DeviceAccessRepository for authorization
//  * - WebSocketService for command forwarding
//  * 
//  * Test scenarios:
//  * - Successful command execution flow
//  * - Authorization failure scenarios
//  * - Device offline scenarios
//  * - Invalid command scenarios
//  * - WebSocket communication failures
//  */
// class CommandServiceTest {
    
//     @Mock
//     private DeviceRepository deviceRepository;
    
//     @Mock
//     private DeviceAccessRepository deviceAccessRepository;
    
//     @Mock
//     private WebSocketService webSocketService;
    
//     private CommandService commandService;
    
//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         commandService = new CommandService(deviceRepository, deviceAccessRepository, webSocketService);
//     }
    
//     /**
//      * Test successful command execution
//      * - Authorized user
//      * - Online device
//      * - Valid command
//      * - WebSocket forwarding success
//      * - Command acknowledgment received
//      */
//     @Test
//     void testExecuteCommand_Success() {
//         // Implementation needed
//     }
    
//     /**
//      * Test command execution with unauthorized user
//      * - User doesn't own device
//      * - No DeviceAccess record
//      * - Service throws forbidden exception
//      */
//     @Test
//     void testExecuteCommand_UnauthorizedUser() {
//         // Implementation needed
//     }
    
//     /**
//      * Test command execution with offline device
//      * - Authorized user
//      * - Device offline
//      * - Service throws bad request exception
//      */
//     @Test
//     void testExecuteCommand_OfflineDevice() {
//         // Implementation needed
//     }
    
//     /**
//      * Test command execution with invalid command
//      * - Authorized user
//      * - Online device
//      * - Invalid command type
//      * - Service throws bad request exception
//      */
//     @Test
//     void testExecuteCommand_InvalidCommand() {
//         // Implementation needed
//     }
    
//     /**
//      * Test command validation
//      * - Valid command types
//      * - Invalid command types
//      * - Missing parameters
//      * - Return validation result
//      */
//     @Test
//     void testValidateCommand() {
//         // Implementation needed
//     }
    
//     /**
//      * Test device online status checking
//      * - Online device
//      * - Offline device
//      * - Return correct status
//      */
//     @Test
//     void testCheckDeviceOnline() {
//         // Implementation needed
//     }
    
//     /**
//      * Test WebSocket command forwarding
//      * - Successful forwarding
//      * - Connection failure
//      * - Acknowledgment timeout
//      * - Return delivery status
//      */
//     @Test
//     void testForwardCommandToDevice() {
//         // Implementation needed
//     }
    
//     /**
//      * Test getting supported command types
//      * - Return list of supported commands
//      * - Verify command types
//      */
//     @Test
//     void testGetSupportedCommands() {
//         // Implementation needed
//     }
// }
