package com.gestaoformativa.interceptor;

import com.gestaoformativa.context.TenantContext;
import com.gestaoformativa.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

    private final TenantService tenantService;

    // Endpoints que não requerem tenant (públicos)
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/tenants/register",
        "/api/auth/login",
        "/api/tenants/subdomain/",
        "/swagger-ui",
        "/api-docs",
        "/v3/api-docs"
    );

    public TenantInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Verificar se é um endpoint público
        if (isPublicEndpoint(requestURI)) {
            logger.debug("Public endpoint accessed: {}", requestURI);
            return true;
        }

        // Tentar obter tenant ID do header
        String tenantIdHeader = request.getHeader("X-Tenant-ID");

        if (tenantIdHeader == null || tenantIdHeader.isEmpty()) {
            logger.warn("Missing X-Tenant-ID header for request: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"X-Tenant-ID header is required\"}");
            return false;
        }

        try {
            Long tenantId = Long.parseLong(tenantIdHeader);

            // Validar se o tenant está ativo
            if (!tenantService.isTenantActive(tenantId)) {
                logger.warn("Inactive tenant attempted access: {}", tenantId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Tenant is suspended or inactive\"}");
                return false;
            }

            // Definir o tenant no contexto
            TenantContext.setTenantId(tenantId);
            logger.debug("Tenant context set: tenantId={}", tenantId);

            return true;

        } catch (NumberFormatException e) {
            logger.error("Invalid X-Tenant-ID format: {}", tenantIdHeader);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid X-Tenant-ID format\"}");
            return false;

        } catch (IllegalArgumentException e) {
            logger.error("Tenant not found: {}", tenantIdHeader);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Tenant not found\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
        logger.debug("Tenant context cleared");
    }

    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestURI::startsWith);
    }
}
