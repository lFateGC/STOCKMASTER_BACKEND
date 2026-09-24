package com.stockmaster.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptorTest {

    private AesEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new AesEncryptor();
        encryptor.setSecretKey("StockMasterEncryptionSecretKey2026!");
    }

    @Test
    void testEncryptAndDecrypt_ReturnsOriginalString() {
        String original = "Dirección confidencial 123 - Teléfono: 999888777";

        String encrypted = AesEncryptor.encrypt(original);
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("ENC:"));
        assertNotEquals(original, encrypted);

        String decrypted = AesEncryptor.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testEncryptNullOrBlank_ReturnsSame() {
        assertNull(AesEncryptor.encrypt(null));
        assertEquals("", AesEncryptor.encrypt(""));
        assertEquals("   ", AesEncryptor.encrypt("   "));
    }

    @Test
    void testDecryptUnencryptedString_ReturnsOriginal() {
        String plain = "Texto sin encriptar";
        assertEquals(plain, AesEncryptor.decrypt(plain));
    }
}
