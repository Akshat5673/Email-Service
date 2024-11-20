package com.nineleaps.email_service.Payloads;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyMessage {

    private String from;
    private String subject;
    private String content;
    private List<String> files;

}
