package br.edu.unicesumar.chat.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class UsuarioTest {
    @Test void deveNormalizarNome() {
        assertEquals("Maria", new Usuario("  Maria ").getNome());
    }

    @Test void naoDeveAceitarNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> new Usuario(" "));
    }

    @Test void naoDeveAceitarNomeComMaisDeCinquentaCaracteres() {
        assertThrows(IllegalArgumentException.class, () -> new Usuario("a".repeat(51)));
    }
}
