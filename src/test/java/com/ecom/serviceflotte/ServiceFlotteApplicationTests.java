package com.ecom.serviceflotte;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifie que le contexte Spring demarre avec la vraie configuration
 * (MySQL requis, car JPA connection a la base au demarrage).
 * Eureka est desactive dans ce test pour eviter des tentatives
 * d'enregistrement inutiles pendant les tests.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
class ServiceFlotteApplicationTests {

	@Test
	void contextLoads() {
	}

}
