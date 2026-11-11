# Tests fonctionnels de https://www.celectronix.com/ (TestNG + Selenium)

Mini-projet : 20 tests fonctionnels automatises en Java (Selenium WebDriver 4 + TestNG 7), avec rapport HTML,
captures d'ecran des echecs et jeux de donnees CSV.

## 1. Prerequis

| Outil | Version | Verification |
|---|---|---|
| JDK | 17 ou plus | `java -version` |
| Maven | 3.8 ou plus | `mvn -version` |
| Navigateur | Chrome (par defaut), Edge ou Firefox a jour | - |

Aucun driver a telecharger : **Selenium Manager** (integre a Selenium 4.6+) installe automatiquement
chromedriver / geckodriver / msedgedriver. Une connexion Internet est necessaire.

## 2. Installation

> Maven n'est pas obligatoire : le projet contient le **Maven Wrapper**. Sous Windows, remplacez `mvn` par `mvnw.cmd`
> (ex. `mvnw.cmd clean test`) ; sous Linux/macOS par `./mvnw`. Le wrapper telecharge Maven la premiere fois
> (Internet requis, JDK 17+ et variable JAVA_HOME definie). Dans IntelliJ, le panneau Maven suffit.


```bash
git clone <votre-depot>   # ou dezipper le dossier
cd projet-selenium
mvn clean compile
```

Avec Eclipse / IntelliJ : *Import > Existing Maven Project* puis ouvrir `pom.xml`.

## 3. Configuration

Fichier `src/test/resources/config.properties` :

| Cle | Role | Defaut |
|---|---|---|
| `base.url` | Adresse du site | `https://www.celectronix.com/` |
| `browser` | `chrome`, `edge` ou `firefox` | `chrome` |
| `headless` | Navigateur sans fenetre | `false` |
| `demo.pause.ms` | Pause (ms) apres chaque page et avant chaque clic pour suivre le test a l'ecran (0 = rapide) | `1500` |
| `timeout` | Attente explicite (secondes) | `15` |
| `search.keyword` | Mot-cle de secours (chaque test a le sien dans `testdata/keywords.csv`) | `ordinateur` |

Toute cle peut etre surchargee en ligne de commande, par exemple `-Dbrowser=firefox -Dheadless=true`.

### IMPORTANT : adapter les selecteurs au site reel

Tous les localisateurs sont dans **un seul fichier** :
`src/main/java/com/celectronix/pages/Locators.java`.
Ils contiennent plusieurs alternatives CSS pour couvrir les themes e-commerce usuels, mais ils n'ont pas pu etre
verifies directement sur le site (protection anti-robot). Au premier lancement :
1. Lancez les tests et ouvrez `reports/ExtentReport.html`.
2. Pour chaque echec du type « introuvable (adapter Locators...) », faites clic droit > *Inspecter* sur l'element
   dans le navigateur et ajoutez son selecteur dans `Locators.java`.
3. Adaptez aussi `search.keyword` et `testdata/keywords.csv` a des produits reellement vendus.

## 4. Execution

```bash
mvn clean test                                   # suite complete (testng.xml)
mvn test -Dbrowser=firefox                       # autre navigateur
mvn test -Dheadless=true                         # sans interface
mvn test -Dtest=CelectronixTests#tc05_searchExistingProduct   # un seul test
```

Le build ne s'arrete pas sur un test en echec (`testFailureIgnore=true`) pour toujours produire les rapports.

## 5. Resultats

| Livrable | Emplacement |
|---|---|
| Rapport d'execution (Passed / Failed / Skipped) | `reports/ExtentReport.html` |
| Captures d'ecran des tests echoues | `screenshots/*.png` |
| Traces des exceptions | `errors/*.txt` |
| Rapport TestNG natif | `target/surefire-reports/index.html` |

## 6. Structure

```
testng.xml                          Configuration de la suite (livrable 5)
src/main/java/com/celectronix/
    pages/                          Page Object : HomePage, SearchResultsPage, ProductPage, CartPage,
                                    LoginPage, ContactPage, Locators, BasePage
    utils/                          Config, DriverFactory, DriverManager, ScreenshotUtil, CsvReader
src/test/java/com/celectronix/tests/
    CelectronixTests.java           Les 20 tests TC01 a TC20 (livrable 6)
    BaseTest.java                   Ouverture / fermeture du navigateur, aides communes
    DataProviders.java              Alimentation des tests par CSV
    TestListener.java               Rapport HTML + captures + traces
src/test/resources/
    config.properties
    testdata/*.csv                  Jeux de donnees (livrable 7)
```

## 7. Les 20 tests

| ID | Test | Donnees |
|---|---|---|
| TC01 | Chargement de la page d'accueil | - |
| TC02 | Code promo invalide refuse avec un message clair | promo_invalid.csv |
| TC03 | HTTPS force et sans contenu mixte (accueil, connexion, panier) | - |
| TC04 | Navigation vers une categorie | - |
| TC05 | Recherche par libelle ou reference exacte | keywords.csv (ESP32, Arduino Uno, Capteur DHT11, 59-03213) |
| TC06 | Recherche d'un produit inexistant, puis recherche vide | search_invalid.csv |
| TC07 | Page d'erreur propre (404) sans trace technique | - |
| TC08 | Affichage de la liste de resultats | keywords.csv (capteur) |
| TC09 | Ouverture d'une fiche produit (titre et prix visibles) | keywords.csv (moteur) |
| TC10 | Ouverture d'une page de categorie par son adresse | categorie_page.csv |
| TC11 | Ajout au panier | keywords.csv (relais) |
| TC12 | Le produit apparait dans le panier avec une devise coherente (TND) | keywords.csv (cable) |
| TC13 | Quantite : avertissement de stock sur la fiche, quantite modifiable dans le panier, valeur -1 refusee | keywords.csv (pompe) |
| TC14 | Suppression d'un produit du panier | keywords.csv (alimentation) |
| TC15 | Page de connexion affichee | - |
| TC16 | Connexion avec identifiants invalides | login_invalid.csv |
| TC17 | Inscription avec donnees invalides | register_invalid.csv |
| TC18 | Formulaire de contact invalide | contact_invalid.csv |
| TC19 | Pied de page et liens | - |
| TC20 | Tri ou filtre des produits | keywords.csv (ecran) |

Chaque test demarre sur un navigateur neuf (tests independants, pas de `dependsOnMethods`). Les tests pilotes par
CSV sont executes une fois par ligne de donnees.

## 8. Remarques

- Aucun vrai compte n'est cree et aucune commande n'est passee : les tests de compte utilisent uniquement des
  donnees invalides.
- Si le site affiche un captcha ou une protection anti-robot, lancer les tests en mode non-headless et
  resoudre la verification manuellement, ou le signaler dans le rapport final comme limite.
- Un produit a variantes (taille, couleur...) peut exiger de choisir une option avant l'ajout au panier :
  dans ce cas, choisissez un mot-cle (`search.keyword`) menant a un produit simple.

## 9. Test de demonstration (hors des 20 tests)

`DemoFailureTest` echoue **volontairement** pour montrer que la capture d'ecran, la trace et la page HTML sont bien
enregistrees. Il n'est pas dans `testng.xml` et ne s'execute que sur demande :

```bash
mvn test -Pdemo
```

Resultats : `reports/ExtentReport_demo.html`, `screenshots/demo_voluntaryFailure_*.png`,
`errors/demo_voluntaryFailure_*.txt`. Ce n'est **pas** une anomalie du site : a ne jamais presenter comme telle.
Les vraies anomalies du site se trouvent uniquement dans les echecs des 20 tests de la suite principale.

## 10. Outil d'exploration des anomalies (hors des 20 tests)

`AnomalyScanTest` parcourt les 10 premieres categories du menu et releve les produits a 0 TND, les images cassees,
les pages introuvables et les categories sans produit :

```bash
mvn test -Pscan        # ou mvnw.cmd test -Pscan
```

Resultat : `reports/anomalies_scan.csv` (a ouvrir avec Excel). Chaque ligne est un **indice** : verifiez-la a la main
sur le site avant de la presenter comme anomalie.


## Resultats attendus : melange de Passed et de Failed

Les tests ne sont pas truques : un test echoue seulement quand il detecte une vraie anomalie du site.
- **TC11** echoue tant que des produits sont affiches a 0,000 TND avec un bouton "Ajouter au panier" actif (AN-01).
- **TC12** echoue tant que le panier melange deux devises : montants en TND et message de livraison en DT (AN-03).
- **TC02** est volontairement **ignore** (Skipped) : aucun code promo valide n'est disponible. Mettre `promo.test.enabled=true` dans `config.properties` pour l'executer.
- Les 17 autres tests doivent passer. Si le site corrige les anomalies, les 20 tests passent.
