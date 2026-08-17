package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByUtilisateur(Utilisateur utilisateur);
}