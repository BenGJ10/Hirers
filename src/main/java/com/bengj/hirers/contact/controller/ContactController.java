package com.bengj.hirers.contact.controller;

import com.bengj.hirers.contact.service.IContactService;
import com.bengj.hirers.dto.ContactRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final IContactService contactService;

    @PostMapping(version = "1.0")
    public ResponseEntity<String> saveContactMessage(
            @RequestBody ContactRequestDto contactRequestDto) {
        boolean isSavedToDatabase = contactService.saveContact(contactRequestDto);

        if(isSavedToDatabase){
            // Returns status code 200 OK
            return ResponseEntity.ok().body("Message saved to database");
        }
        else{
            // Returns status code 500 Internal Server Error
            return ResponseEntity.internalServerError().body("Failed to save message to database");
        }
    }
}
