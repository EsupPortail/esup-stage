package org.esup_portail.esup_stage.security;

import org.esup_portail.esup_stage.dto.ContextDto;

public class ServiceContext {
    private static ThreadLocal<ContextDto> serviceContext = new ThreadLocal<>();

    public static void initialize(ContextDto contextDto) {
        serviceContext.set(contextDto);
    }

    public static void cleanup() {
        serviceContext.set(null);
    }

    public static ContextDto getServiceContext() {
        if (serviceContext != null) {
            return serviceContext.get();
        }
        return new ContextDto();
    }
}
