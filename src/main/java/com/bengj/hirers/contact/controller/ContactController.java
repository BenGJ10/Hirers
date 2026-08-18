package com.bengj.hirers.contact.controller;

import com.bengj.hirers.contact.service.IContactService;
import com.bengj.hirers.dto.ContactRequestDto;
import com.bengj.hirers.dto.ContactResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final IContactService contactService;

    @PostMapping(version = "1.0")
    public ResponseEntity<String> saveContactMessage(
            @RequestBody @Valid ContactRequestDto contactRequestDto){
        boolean isSavedToDatabase = contactService.saveContact(contactRequestDto);

       if(isSavedToDatabase){
           // Returns status code 201 Created
           return ResponseEntity.status(HttpStatus.CREATED)
                   .body("Request processed successfully");
       }
       else{
           // Returns status code 500 Internal Server Error
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Request processing failed");
       }
    }


    @GetMapping("/admin")
    public ResponseEntity<List<ContactResponseDto>> fetchNewContactMessages(){
        List<ContactResponseDto> contactResponseDtoList = contactService.fetchNewContactMessages();
        return ResponseEntity.status(HttpStatus.OK).
                body(contactResponseDtoList);
    }
}
