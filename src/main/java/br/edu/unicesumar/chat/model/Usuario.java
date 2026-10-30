package br.edu.unicesumar.chat.model;

import java.util.Objects;

/** Identifica um participante do chat. */
public final class Usuario {
    private final String nome;

    public Usuario(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do usuario e obrigatorio.");
        }
        String nomeNormalizado = nome.trim();
        if (nomeNormalizado.length() > 50) {
            throw new IllegalArgumentException("O nome pode ter no maximo 50 caracteres.");
        }
        this.nome = nomeNormalizado;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Usuario && nome.equals(((Usuario) outro).nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}
