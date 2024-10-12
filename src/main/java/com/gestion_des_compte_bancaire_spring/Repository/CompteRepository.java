package com.gestion_des_compte_bancaire_spring.Repository;

import com.gestion_des_compte_bancaire_spring.entites.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteRepository extends JpaRepository<Compte,Long> {
}
