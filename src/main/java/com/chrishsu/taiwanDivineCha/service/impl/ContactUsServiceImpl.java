package com.chrishsu.taiwanDivineCha.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chrishsu.taiwanDivineCha.model.ContactUs;
import com.chrishsu.taiwanDivineCha.repository.ContactUsRepository;
import com.chrishsu.taiwanDivineCha.service.ContactUsService;

@Service
public class ContactUsServiceImpl implements ContactUsService {

    @Autowired
    private ContactUsRepository contactUsRepository;

    @Override
    public void createContactUs(ContactUs contactUs) {
        contactUsRepository.save(contactUs);
    }
}
