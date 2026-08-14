package com.bengj.hirers.contact.service;

import com.bengj.hirers.dto.ContactRequestDto;

public interface IContactService {

    boolean saveContact(ContactRequestDto contactRequestDto);
}
