package com.bengj.hirers.contact.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.ContactRequestDto;
import com.bengj.hirers.dto.ContactResponseDto;
import com.bengj.hirers.entity.Contact;
import com.bengj.hirers.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService implements IContactService{

    private final ContactRepository contactRepository;

    // Method to save a contact based on the provided ContactRequestDto
    @Override
    public boolean saveContact(ContactRequestDto contactRequestDto) {
        boolean result = false;
        Contact contact = contactRepository.save(transformToEntity(contactRequestDto));
        if (contact.getId() != null){
            result = true;
        }
        return result;
    }

    @Override
    public List<ContactResponseDto> fetchNewContactMessages() {
        List<Contact> contacts = contactRepository.findContactsByStatus(ApplicationConstants.NEW_MESSAGE);

        return contacts.stream()
                .map(this::transformToDto)
                .toList();
    }

    // Utility method to transform ContactRequestDto to Contact entity
    private Contact transformToEntity(ContactRequestDto contactRequestDto){
        Contact contact = new Contact();
        
        // Use BeanUtils to copy properties from the DTO to the entity
        // BeanUtils is a utility class provided by Spring that allows for easy copying of properties between JavaBeans
        BeanUtils.copyProperties(contactRequestDto, contact);

        contact.setCreatedAt(Instant.now());
        contact.setCreatedBy("System");
        contact.setStatus(ApplicationConstants.NEW_MESSAGE);
        return contact;
    }

    private ContactResponseDto transformToDto(Contact contact) {
        return new ContactResponseDto(contact.getId(),
                contact.getName(), contact.getEmail(), contact.getUserType(), contact.getSubject(),
                contact.getMessage(), contact.getStatus(), contact.getCreatedAt());
    }

}
