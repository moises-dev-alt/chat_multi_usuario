package br.edu.unicesumar.chat.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Mensagem de dominio que sera persistida e distribuida aos clientes. */
public final class Mensagem {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Usuario remetente;
    private final String conteudo;
    private final LocalDateTime horario;
    private final String grupo;

    public Mensagem(Usuario remetente, String conteudo, LocalDateTime horario) {
        this(remetente, conteudo, horario, "Geral");
    }

    public Mensagem(Usuario remetente, String conteudo, LocalDateTime horario, String grupo) {
        this.remetente = Objects.requireNonNull(remetente, "Remetente e obrigatorio.");
        if (conteudo == null || conteudo.trim().isEmpty()) {
            throw new IllegalArgumentException("A mensagem nao pode ser vazia.");
        }
        String texto = conteudo.trim();
        if (texto.length() > 1000) {
            throw new IllegalArgumentException("A mensagem pode ter no maximo 1000 caracteres.");
        }
        this.conteudo = texto;
        this.horario = Objects.requireNonNull(horario, "Horario e obrigatorio.");
        if (grupo == null || grupo.trim().isEmpty()) throw new IllegalArgumentException("Grupo e obrigatorio.");
        this.grupo = grupo.trim();
    }

    public Usuario getRemetente() { return remetente; }
    public String getConteudo() { return conteudo; }
    public LocalDateTime getHorario() { return horario; }
    public String getGrupo() { return grupo; }

    public String formatar() {
        return "[" + FORMATO.format(horario) + "] " + remetente.getNome() + ": " + conteudo;
    }
}
