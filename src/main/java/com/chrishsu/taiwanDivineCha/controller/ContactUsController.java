package com.chrishsu.taiwanDivineCha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chrishsu.taiwanDivineCha.dto.ApiResponse;
import com.chrishsu.taiwanDivineCha.model.ContactUs;
import com.chrishsu.taiwanDivineCha.service.ContactUsService;

@RestController
public class ContactUsController {

    @Autowired
    private ContactUsService contactUsService;

    @PostMapping("/api/contactUs")
    public ResponseEntity<ApiResponse> createContactUs(@RequestBody ContactUs contactUs) {
        contactUsService.createContactUs(contactUs);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "表單已成功提交"));
    }

}
