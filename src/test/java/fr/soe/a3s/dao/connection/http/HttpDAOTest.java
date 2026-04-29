package fr.soe.a3s.dao.connection.http;

import fr.soe.a3s.constant.ProtocolType;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpDAOTest {

    // Built from a numeric char literal so the test source stays pure ASCII;
    // otherwise the test's correctness would depend on the compiler interpreting
    // this file's bytes with the right charset (Cp1252 on Windows JVMs is the
    // very thing the bug is about).
    private static final String O_ACUTE = String.valueOf((char) 0x00F3);

    @Test
    void buildUrlPercentEncodesNonAsciiPathAsUtf8() throws Exception {
        String relativePath = "/arma3sync/@Arma 3 Revoluci" + O_ACUTE + "n/mod.cpp";

        URL url = HttpDAO.buildUrl(ProtocolType.HTTP, "arma3.kellerkompanie.com", 80, relativePath);

        String externalForm = url.toExternalForm();

        assertTrue(
                externalForm.contains("%C3%B3"),
                "URL must UTF-8 percent-encode the 'o-acute' character as %C3%B3, got: " + externalForm);
        assertFalse(
                externalForm.contains(O_ACUTE),
                "URL must not contain the literal non-ASCII character, got: " + externalForm);
        assertFalse(
                externalForm.contains("%F3"),
                "URL must not contain CP-1252 encoding (%F3) of the 'o-acute' character, got: " + externalForm);
    }

    @Test
    void buildUrlLeavesPureAsciiPathUntouched() throws Exception {
        String relativePath = "/arma3sync/@CBA_A3/mod.cpp";

        URL url = HttpDAO.buildUrl(ProtocolType.HTTP, "arma3.kellerkompanie.com", 80, relativePath);

        assertEquals(
                "http://arma3.kellerkompanie.com:80/arma3sync/@CBA_A3/mod.cpp",
                url.toExternalForm());
    }

    @Test
    void buildUrlEncodesSpacesAsPercent20() throws Exception {
        String relativePath = "/arma3sync/@Some Mod/mod.cpp";

        URL url = HttpDAO.buildUrl(ProtocolType.HTTPS, "example.com", 443, relativePath);

        String externalForm = url.toExternalForm();
        assertTrue(
                externalForm.contains("%20"),
                "URL must percent-encode spaces, got: " + externalForm);
        assertTrue(
                externalForm.startsWith("https://example.com:443/"),
                "URL must use https scheme and port, got: " + externalForm);
    }
}
