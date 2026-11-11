package com.celectronix.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TEST DE DEMONSTRATION - HORS DES 20 TESTS DU PROJET.
 * Il echoue volontairement pour prouver que la capture d'ecran (PNG), la trace (TXT) et la page HTML
 * sont bien enregistrees en cas d'echec. Il n'est PAS dans testng.xml : il ne s'execute que
 * avec  mvn test -Pdemo  (rapport separe : reports/ExtentReport_demo.html).
 * Ce n'est pas une anomalie du site et ne doit jamais etre presente comme telle.
 */
public class DemoFailureTest extends BaseTest {

    @Test(description = "DEMO - Echec volontaire (demonstration de la capture et de la trace)")
    public void demo_voluntaryFailure() {
        Assert.assertEquals(driver.getTitle(), "TITRE-VOLONTAIREMENT-FAUX",
            "ECHEC VOLONTAIRE DE DEMONSTRATION - ce n'est pas une anomalie du site");
    }
}
