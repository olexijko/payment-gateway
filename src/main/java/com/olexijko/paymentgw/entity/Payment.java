package com.olexijko.paymentgw.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String invoice;

    private double amount;

    private String currency;

    @ManyToOne(cascade = CascadeType.ALL)
    private Cardholder cardholder;

    @ManyToOne(cascade = CascadeType.ALL)
    private Card card;

}
