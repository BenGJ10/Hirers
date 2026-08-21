package com.bengj.hirers.contact.service;

import com.bengj.hirers.dto.ContactRequestDto;
import com.bengj.hirers.dto.ContactResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IContactService {

    boolean saveContact(ContactRequestDto contactRequestDto);

    List<ContactResponseDto> fetchNewContactMessages();

    List<ContactResponseDto> fetchNewContactMessagesWithSort(String sortBy, String sortDir);

    Page<ContactResponseDto> fetchNewContactMessagesWithPaginationAndSort(
            int pageNumber, int pageSize, String sortBy, String sortDir);

    boolean closeContactMessage(Long id, String status);
}
