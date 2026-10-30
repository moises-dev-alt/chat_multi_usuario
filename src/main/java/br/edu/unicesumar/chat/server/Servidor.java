package br.edu.unicesumar.chat.server;

import br.edu.unicesumar.chat.model.Mensagem;
import br.edu.unicesumar.chat.persistence.MensagemRepository;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Servidor TCP: aceita clientes, mantem a lista sincronizada e faz broadcast. */
public final class Servidor implements AutoCloseable {
    public static final int PORTA_PADRAO = 5000;
    private static final int LIMITE_HISTORICO = 20;

    private final int porta;
    private final MensagemRepository repository;
    private final List<ClienteHandler> clientes = new CopyOnWriteArrayList<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean executando;
    private ServerSocket serverSocket;

    public Servidor(int porta, MensagemRepository repository) {
        this.porta = porta;
        this.repository = repository;
    }

    public void iniciar() throws IOException {
        executando = true;
        try (ServerSocket socket = new ServerSocket(porta)) {
            this.serverSocket = socket;
            System.out.println("Servidor iniciado na porta " + porta + ".");
            while (executando) {
                try {
                    Socket cliente = socket.accept();
                    pool.submit(new ClienteHandler(cliente, this));
                } catch (IOException e) {
                    if (executando) throw e;
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    void adicionarCliente(ClienteHandler cliente) {
        clientes.add(cliente);
        System.out.println(cliente.getUsuario().getNome() + " conectou.");
    }

    void removerCliente(ClienteHandler cliente) {
        if (clientes.remove(cliente) && cliente.getUsuario() != null) {
            System.out.println(cliente.getUsuario().getNome() + " desconectou.");
        }
    }

    void enviarHistoricoPara(ClienteHandler cliente) {
        cliente.enviar("--- Ultimas mensagens ---");
        for (Mensagem mensagem : repository.buscarUltimas(LIMITE_HISTORICO)) {
            cliente.enviar(mensagem.formatar());
        }
        cliente.enviar("--- Fim do historico ---");
    }

    /** Salva antes de distribuir para que o historico nao perca mensagens transmitidas. */
    void receber(Mensagem mensagem) {
        repository.salvar(mensagem);
        broadcast(mensagem.formatar());
    }

    void broadcast(String texto) {
        for (ClienteHandler cliente : clientes) {
            cliente.enviar(texto);
        }
    }

    @Override
    public void close() throws IOException {
        executando = false;
        if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        pool.shutdownNow();
    }

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : PORTA_PADRAO;
        String urlBanco = args.length > 1 ? args[1] : "jdbc:h2:./data/chatdb";
        MensagemRepository repository = new MensagemRepository(urlBanco);
        try (Servidor servidor = new Servidor(porta, repository)) {
            servidor.iniciar();
        }
    }
}
