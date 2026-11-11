package com.celectronix.tests;

import org.testng.annotations.DataProvider;

import com.celectronix.utils.CsvReader;

/** Livrable 7 : les jeux de donnees sont dans src/test/resources/testdata/*.csv */
public class DataProviders {

    @DataProvider(name = "searchInvalid")
    public static Object[][] searchInvalid() {
        return CsvReader.read("testdata/search_invalid.csv");
    }

    @DataProvider(name = "loginInvalid")
    public static Object[][] loginInvalid() {
        return CsvReader.read("testdata/login_invalid.csv");
    }

    @DataProvider(name = "registerInvalid")
    public static Object[][] registerInvalid() {
        return CsvReader.read("testdata/register_invalid.csv");
    }

    @DataProvider(name = "contactInvalid")
    public static Object[][] contactInvalid() {
        return CsvReader.read("testdata/contact_invalid.csv");
    }

    @DataProvider(name = "promoInvalid")
    public static Object[][] promoInvalid() {
        return CsvReader.read("testdata/promo_invalid.csv");
    }

    @DataProvider(name = "categoryPage")
    public static Object[][] categoryPage() {
        return CsvReader.read("testdata/categorie_page.csv");
    }
}
