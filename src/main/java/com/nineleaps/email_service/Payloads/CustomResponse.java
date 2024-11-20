package com.nineleaps.email_service.Payloads;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomResponse {

    private String message;
    private HttpStatus status;
    private boolean success = false;
}
