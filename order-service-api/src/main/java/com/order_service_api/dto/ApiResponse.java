package com.order_service_api.dto;

import lombok.*;

/**
 * Generic wrapper DTO for API responses from other services.
 * 
 * <p>Matches the response structure used by internal microservices:
 * <ul>
 *   <li>status - SUCCESS or ERROR</li>
 *   <li>message - Response message</li>
 *   <li>data - The actual response payload</li>
 * </ul>
 * 
 * @param <T> the type of data in the response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    /**
     * The status of the operation (SUCCESS or ERROR).
     */
    private String status;

    /**
     * The response message.
     */
    private String message;

    /**
     * The response data payload.
     */
    private T data;

    /**
     * Checks if the response indicates success.
     * 
     * @return true if status is SUCCESS
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

}

