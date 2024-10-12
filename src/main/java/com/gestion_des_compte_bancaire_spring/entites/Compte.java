package com.gestion_des_compte_bancaire_spring.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Compte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nom", length = 255)
    private String nom;
    @Column(name = "tel", length = 255)
    private String tel;
    @Column(name="solde")
    private double solde;
}
