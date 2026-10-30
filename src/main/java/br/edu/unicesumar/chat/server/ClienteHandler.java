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

            servidor.enviarHistoricoPara(this);
            servidor.adicionarCliente(this);
            enviar("Conectado como " + usuario.getNome() + ". Digite /sair para encerrar.");

            String texto;
            while ((texto = entrada.readLine()) != null) {
                if ("/sair".equalsIgnoreCase(texto.trim())) break;
                try {
                    servidor.receber(new Mensagem(usuario, texto, LocalDateTime.now()));
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
}
