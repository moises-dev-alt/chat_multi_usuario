package br.edu.unicesumar.chat.server;

import br.edu.unicesumar.chat.model.Mensagem;
import br.edu.unicesumar.chat.model.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/** Atende uma unica conexao de cliente em uma thread do servidor. */
public final class ClienteHandler implements Runnable {
    private final Socket socket;
    private final Servidor servidor;
    private PrintWriter saida;
    private Usuario usuario;
    private String grupo = "Geral";

    ClienteHandler(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            this.saida = writer;
            String nome = entrada.readLine();
            if (nome == null) return;
            usuario = new Usuario(nome);

            servidor.adicionarCliente(this);
            servidor.enviarHistoricoPara(this, grupo);
            enviar("Conectado como " + usuario.getNome() + ". Digite /grupo Nome para trocar de grupo ou /sair para encerrar.");

            String texto;
            while ((texto = entrada.readLine()) != null) {
                if ("/sair".equalsIgnoreCase(texto.trim())) break;
                if (texto.startsWith("/grupo ")) {
                    String novoGrupo = texto.substring(7).trim();
                    if (!novoGrupo.matches("[\\p{L}\\p{N} _-]{1,40}")) enviar("Erro: nome do grupo invalido.");
                    else servidor.mudarGrupo(this, novoGrupo);
                    continue;
                }
                if (texto.startsWith("/convidar ")) {
                    String convidado = texto.substring(10).trim();
                    if (convidado.isEmpty()) enviar("Erro: informe o nome da pessoa.");
                    else servidor.convidar(this, convidado);
                    continue;
                }
                try {
                    servidor.receber(new Mensagem(usuario, texto, LocalDateTime.now(), grupo));
                } catch (IllegalArgumentException e) {
                    enviar("Erro: " + e.getMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            enviar("Erro de conexao: " + e.getMessage());
        } catch (IOException e) {
            // A desconexao abrupta de um cliente e esperada e nao afeta os demais.
        } finally {
            servidor.removerCliente(this);
        }
    }

    public synchronized void enviar(String texto) {
        if (saida != null) saida.println(texto);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getGrupo() { return grupo; }
    void definirGrupo(String grupo) { this.grupo = grupo; }
}
