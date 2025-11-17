package br.com.construcao.sistemas.controller.dto.request.emergency;

import br.com.construcao.sistemas.model.enums.ServiceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateEmergencyContactRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        CreateEmergencyContactRequest request = new CreateEmergencyContactRequest();
        request.setName("John Doe");
        request.setPhone("123456789");
        request.setServiceType(ServiceType.BOMBEIROS);

        assertEquals("John Doe", request.getName());
        assertEquals("123456789", request.getPhone());
        assertEquals(ServiceType.BOMBEIROS, request.getServiceType());
    }

    @Test
    void testAllArgsConstructor() {
        CreateEmergencyContactRequest request = new CreateEmergencyContactRequest(
                "Jane Doe",
                "987654321",
                ServiceType.PM
        );

        assertEquals("Jane Doe", request.getName());
        assertEquals("987654321", request.getPhone());
        assertEquals(ServiceType.PM, request.getServiceType());
    }

    @Test
    void testBuilder() {
        CreateEmergencyContactRequest request = CreateEmergencyContactRequest.builder()
                .name("Alice")
                .phone("111222333")
                .serviceType(ServiceType.BOMBEIROS)
                .build();

        assertEquals("Alice", request.getName());
        assertEquals("111222333", request.getPhone());
        assertEquals(ServiceType.BOMBEIROS, request.getServiceType());
    }
}