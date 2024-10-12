package com.gestion_des_compte_bancaire_spring.service;

import com.gestion_des_compte_bancaire_spring.Repository.CompteRepository;
import com.gestion_des_compte_bancaire_spring.entites.Compte;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompteService {

    private CompteRepository compteRepository;

    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    public List<Compte> GetAllComptes(){
        return compteRepository.findAll();
    }

    public Compte FindCompteById(Long id){
        return compteRepository.findById(id).orElse(null);
    }

    public Compte CreateCompte(Compte compte){
        return compteRepository.save(compte);
    }

    public void DeleteCompte(Long id){
        compteRepository.deleteById(id);
    }

    public Compte UpdateCompte(Long id, Compte NewCompte){
        return compteRepository.findById(id).map(compte -> {
                compte.setNom(NewCompte.getNom());
                compte.setTel(NewCompte.getTel());
                compte.setSolde(NewCompte.getSolde());
                return compteRepository.save(compte);
        }).orElseThrow(()-> new RuntimeException("Non trouve"));
    }

    public Compte Crediter(Long id, float m){

        return compteRepository.findById(id).map(compte -> {
            compte.setSolde(compte.getSolde()+m);
            return  compteRepository.save(compte);
        }).orElseThrow(() -> new RuntimeException("non trouvé"));
    }

    public  Compte Debiter(Long id, float m){
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé avec l'ID : " + id));

        if (compte.getSolde()>= m){
            compte.setSolde(compte.getSolde()-m);
        }

        return compteRepository.save(compte);
    }
}
