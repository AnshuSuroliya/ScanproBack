package com.mavericks.scanpro.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter@Setter
public class Invoice {
	    private Long id;

	    private String invoiceId;

	    private Double subTotal;

	    private Double totalTax;

	    private String customerName;

	    private LocalDate invoiceDate;

	    private Double amountDue;

	    private String billingAddress;

	    private Double invoiceTotal;
	    
}