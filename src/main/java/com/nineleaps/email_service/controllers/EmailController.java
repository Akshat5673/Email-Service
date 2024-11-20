package com.nineleaps.email_service.controllers;

import com.nineleaps.email_service.Payloads.CustomResponse;
import com.nineleaps.email_service.Payloads.EmailRequest;
import com.nineleaps.email_service.Payloads.MyMessage;
import com.nineleaps.email_service.services.EmailService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService service;
    @PostMapping("/send")
    public ResponseEntity<CustomResponse> sendEmail(@RequestBody EmailRequest request){
        service.sendEmailWithHtml(request.getTo(),request.getSubject(),request.getMessage());
        return ResponseEntity.ok(
                CustomResponse.builder()
                        .message("Email sent successfully !")
                        .status(HttpStatus.OK)
                        .success(true)
                        .build()
        );
    }

    @PostMapping("/send-with-file")
    public ResponseEntity<CustomResponse> sendEmailWithFile(@RequestPart EmailRequest request,
                                                            @RequestPart MultipartFile file) throws IOException {
        service.sendEmailWithFile(request.getTo(),request.getSubject(),request.getMessage(),file.getInputStream());
        return ResponseEntity.ok(
                CustomResponse.builder()
                        .message("Email sent successfully !")
                        .status(HttpStatus.OK)
                        .success(true)
                        .build()
        );
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<MyMessage>> getInboxMails(){
        return ResponseEntity.ok(service.getInboxMessages());
    }

}
