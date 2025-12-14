package com.shopping_service_api.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign request interceptor for forwarding authentication headers.
 * 
 * <p>This interceptor ensures that user identity headers are propagated
 * to downstream services when making Feign client calls. This maintains
 * the security context across service-to-service communication.
 * 
 * <p>Headers forwarded:
 * <ul>
 *   <li>X-USER-ID - The authenticated user's ID</li>
 *   <li>X-USER-EMAIL - The authenticated user's email</li>
 *   <li>X-USER-ROLE - The authenticated user's role</li>
 * </ul>
 */
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    /**
     * Applies authentication headers to outgoing Feign requests.
     * 
     * <p>Extracts user identity headers from the current HTTP request
     * and adds them to the outgoing Feign request template.
     * 
     * @param template the Feign request template to modify
     */
    @Override
    public void apply(RequestTemplate template) {
        // Get current request attributes
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return;
        }

        var request = attrs.getRequest();

        // Forward user identity headers
        String userId = request.getHeader("X-USER-ID");
        String email = request.getHeader("X-USER-EMAIL");
        String role = request.getHeader("X-USER-ROLE");

        if (userId != null) {
            template.header("X-USER-ID", userId);
        }
        if (email != null) {
            template.header("X-USER-EMAIL", email);
        }
        if (role != null) {
            template.header("X-USER-ROLE", role);
        }
    }

}
