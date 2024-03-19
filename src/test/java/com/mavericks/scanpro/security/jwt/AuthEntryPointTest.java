package com.mavericks.scanpro.security.jwt;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointTest {

    @InjectMocks
    private AuthEntryPointJwt authEntryPoint;

    @Mock
    private Logger logger;

//    @Test
//    void testCommence() throws IOException, AuthenticationException, ServletException {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        AuthenticationException authException = new AuthenticationException("Unauthorized message") {};
//
//        authEntryPoint.commence(request, response, authException);
//
//        // Verify logging
//        verify(logger, times(1)).error(eq("Unauthorized Error : {}"), eq("Unauthorized message"));
//        // You can also use ArgumentCaptor to capture the log arguments and assert on them
//        // ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
//        // verify(logger).error(eq("Unauthorized Error : {}"), messageCaptor.capture());
//        // assertEquals("Unauthorized message", messageCaptor.getValue());
//
//        // Verify response details
//        assertEquals("application/json", response.getContentType());
//        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
//
//        // Verify response body content
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> expectedBody = new HashMap<>();
//        expectedBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
//        expectedBody.put("error", "Unauthorized");
//        expectedBody.put("message", authException.getMessage());
//        expectedBody.put("path", request.getServletPath());
//
//        assertEquals(objectMapper.writeValueAsString(expectedBody), response.getContentAsString());
//    }
}

