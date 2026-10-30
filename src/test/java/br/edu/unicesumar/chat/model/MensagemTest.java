package br.edu.unicesumar.chat.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MensagemTest {
    private final Usuario ana = new Usuario("Ana");
    private final LocalDateTime horario = LocalDateTime.of(2026, 10, 29, 14, 30, 5);

    @Test void deveFormatarMensagemComRemetenteEHorario() {
        Mensagem mensagem = new Mensagem(ana, "Ola!", horario);
        assertEquals("[29/10/2026 14:30:05] Ana: Ola!", mensagem.formatar());
    }

    @Test void deveRemoverEspacosDoConteudo() {
        assertEquals("teste", new Mensagem(ana, "  teste  ", horario).getConteudo());
    }

    @Test void naoDeveAceitarConteudoVazio() {
        assertThrows(IllegalArgumentException.class, () -> new Mensagem(ana, "   ", horario));
    }

    @Test void naoDeveAceitarConteudoMaiorQueMilCaracteres() {
        assertThrows(IllegalArgumentException.class, () -> new Mensagem(ana, "x".repeat(1001), horario));
    }

    @Test void deveManterHorarioOriginal() {
        assertEquals(horario, new Mensagem(ana, "Oi", horario).getHorario());
    }
}
