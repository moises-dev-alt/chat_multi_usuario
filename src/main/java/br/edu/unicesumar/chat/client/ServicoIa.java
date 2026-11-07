package br.edu.unicesumar.chat.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Cliente HTTP simples para APIs compativeis com OpenAI (OpenAI ou Ollama). */
final class ServicoIa {
    record Configuracao(String endpoint, String chave, String modelo) { }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    String responder(String pergunta, Configuracao config) throws IOException, InterruptedException {
        String corpo = "{\"model\":\"" + escapar(config.modelo()) + "\",\"messages\":["
                + "{\"role\":\"system\",\"content\":\"Voce e um assistente util do chat. Responda em portugues.\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapar(pergunta) + "\"}],\"temperature\":0.7}";
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create(config.endpoint()))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        if (!config.chave().isBlank()) pedido.header("Authorization", "Bearer " + config.chave());
        HttpResponse<String> resposta = http.send(pedido.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
            throw new IOException("A IA respondeu HTTP " + resposta.statusCode() + ": " + resposta.body());
        }
        return extrairConteudo(resposta.body());
    }

    private static String extrairConteudo(String json) throws IOException {
        int choices = json.indexOf("\"choices\"");
        int content = json.indexOf("\"content\"", Math.max(0, choices));
        if (content < 0) throw new IOException("Resposta da IA sem texto: " + json);
        int inicio = json.indexOf('"', json.indexOf(':', content) + 1);
        if (inicio < 0) throw new IOException("Resposta da IA invalida.");
        StringBuilder texto = new StringBuilder();
        boolean escape = false;
        for (int i = inicio + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                texto.append(switch (c) { case 'n' -> '\n'; case 'r' -> '\r'; case 't' -> '\t'; case '"' -> '"'; case '\\' -> '\\'; default -> c; });
                escape = false;
            } else if (c == '\\') escape = true;
            else if (c == '"') return texto.toString();
            else texto.append(c);
        }
        throw new IOException("Resposta da IA invalida.");
    }

    private static String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
