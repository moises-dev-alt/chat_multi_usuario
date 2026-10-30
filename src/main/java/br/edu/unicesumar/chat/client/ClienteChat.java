package br.edu.unicesumar.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Cliente de console para demonstrar o chat. */
public final class ClienteChat {
    private ClienteChat() { }

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
             Socket socket = new Socket(host, porta);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter saida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            System.out.print("Seu nome: ");
            String nome = console.readLine();
            saida.println(nome);

            Thread leitor = new Thread(() -> {
                try {
                    String linha;
                    while ((linha = entrada.readLine()) != null) System.out.println(linha);
                } catch (IOException e) {
                    System.out.println("Conexao com o servidor encerrada.");
                }
            }, "leitor-do-servidor");
            leitor.setDaemon(true);
            leitor.start();

            String texto;
            while ((texto = console.readLine()) != null) {
                saida.println(texto);
                if ("/sair".equalsIgnoreCase(texto.trim())) break;
            }
        }
    }
}
