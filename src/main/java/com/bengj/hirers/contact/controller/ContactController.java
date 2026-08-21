package com.bengj.hirers.contact.controller;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.contact.service.IContactService;
import com.bengj.hirers.dto.ContactRequestDto;
import com.bengj.hirers.dto.ContactResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final IContactService contactService;

    @PostMapping(path = "/public", version = "1.0")
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


    @GetMapping(value = "/admin", version = "1.0")
    public ResponseEntity<List<ContactResponseDto>> fetchNewContactMessages(){
        List<ContactResponseDto> contactResponseDtoList = contactService.fetchNewContactMessages();
        return ResponseEntity.status(HttpStatus.OK).
                body(contactResponseDtoList);
    }


    @GetMapping(value = "/sort/admin", version = "1.0")
    public ResponseEntity<List<ContactResponseDto>> fetchNewContactMessagesWithSort(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        List<ContactResponseDto> contactResponseDtos = contactService
                .fetchNewContactMessagesWithSort(sortBy, sortDir);
        return ResponseEntity.status(HttpStatus.OK).body(contactResponseDtos);
    }


    @GetMapping(value = "/page/admin", version = "1.0")
    public ResponseEntity<Page<ContactResponseDto>> fetchNewContactMessagesWithPaginationAndSort(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<ContactResponseDto> contactResponseDtoPage = contactService
                .fetchNewContactMessagesWithPaginationAndSort(pageNumber, pageSize, sortBy, sortDir);
        return ResponseEntity.status(HttpStatus.OK).body(contactResponseDtoPage);
    }


    @PatchMapping("{id}/status/admin")
    public ResponseEntity<String> closeContactMessage(@PathVariable String id){
        boolean isUpdated = contactService.closeContactMessage(Long.valueOf(id),
                ApplicationConstants.CLOSED_MESSAGE);

        if(isUpdated){
            return ResponseEntity.status(HttpStatus.OK).body("Contact message closed successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to close contact message");
        }
    }
}
