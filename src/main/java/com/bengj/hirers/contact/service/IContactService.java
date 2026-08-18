package com.bengj.hirers.contact.service;

import com.bengj.hirers.dto.ContactRequestDto;
import com.bengj.hirers.dto.ContactResponseDto;

import java.util.List;

public interface IContactService {

    boolean saveContact(ContactRequestDto contactRequestDto);

    List<ContactResponseDto> fetchNewContactMessages();
}
