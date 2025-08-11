package com.chrishsu.taiwanDivineCha.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "contact_us")
public class ContactUs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "inquiry_item")
    private String inquiryItem;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "email")
    private String email;

    @Column(name = "estimated_quantity")
    private Integer estimatedQuantity;

    @Column(name = "budget_per_unit")
    private String budgetPerUnit;

    @Column(name = "delivery_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deliveryDate;

    @Column(name = "remarks")
    private String remarks;
}
