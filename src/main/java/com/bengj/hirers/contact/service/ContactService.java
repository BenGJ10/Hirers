package com.bengj.hirers.contact.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.ContactRequestDto;
import com.bengj.hirers.dto.ContactResponseDto;
import com.bengj.hirers.entity.Contact;
import com.bengj.hirers.repository.ContactRepository;
import com.bengj.hirers.util.ApplicationUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService implements IContactService{

    private final ContactRepository contactRepository;

    // Method to save a contact based on the provided ContactRequestDto
    @Override
    @Transactional
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
        List<Contact> contacts = contactRepository.findContactsByStatusOrderByCreatedAtAsc(ApplicationConstants.NEW_MESSAGE);

        return contacts.stream()
                .map(this::transformToDto)
                .toList();
    }

    @Override
    public List<ContactResponseDto> fetchNewContactMessagesWithSort(String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        List<Contact> contacts = contactRepository.findContactsByStatus(
                ApplicationConstants.NEW_MESSAGE, sort);

        return contacts.stream()
                .map(this::transformToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ContactResponseDto> fetchNewContactMessagesWithPaginationAndSort(
            int pageNumber, int pageSize, String sortBy, String sortDir) {
        // Create a Sort object based on the provided sort parameters
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Create a pageable object with the specified page number and page size
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Fetch the contacts based on the provided criteria and pageable object
        Page<Contact> contactPage = contactRepository.findContactsByStatus(
                ApplicationConstants.NEW_MESSAGE, pageable);

        // Convert the page of contacts to a page of ContactResponseDto objects
        return contactPage.map(this::transformToDto);
    }


    @Override
    @Transactional
    public boolean closeContactMessage(Long id, String status) {
        int updatedRows = contactRepository.updateStatusById(status, id, ApplicationUtility.getLoggedInUser());
        return updatedRows > 0;
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

    // Utility method to transform Contact entity to ContactResponseDto
    private ContactResponseDto transformToDto(Contact contact) {
        return new ContactResponseDto(contact.getId(),
                contact.getName(), contact.getEmail(), contact.getUserType(), contact.getSubject(),
                contact.getMessage(), contact.getStatus(), contact.getCreatedAt());
    }

}
