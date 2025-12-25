package com.clinique.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiApplicationTests {

	/**
	 * Vérifie que le contexte de l'application Spring se charge correctement.
	 * Ce test échouera si la configuration de l'application est invalide ou s'il
	 * manque des beans.
	 */
	@Test
	void contextLoads() {
		assertTrue(true, "Le contexte Spring doit se charger correctement");
	}

}
