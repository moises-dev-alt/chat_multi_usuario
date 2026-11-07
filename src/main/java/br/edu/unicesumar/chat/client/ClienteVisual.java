package br.edu.unicesumar.chat.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListModel;

/** Interface Swing do chat: grupos TCP e uma conversa individual com IA. */
public final class ClienteVisual extends JFrame {
    private final String nome;
    private final JPanel mensagens = new JPanel();
    private final JScrollPane rolagem = new JScrollPane(mensagens);
    private final JTextField texto = new JTextField();
    private final DefaultListModel<String> grupos = new DefaultListModel<>();
    private final JList<String> listaGrupos = new JList<>(grupos);
    private final JTextArea conversaIa = new JTextArea();
    private final JTextField perguntaIa = new JTextField();
    private final ServicoIa ia = new ServicoIa();
    private ServicoIa.Configuracao configIa;
    private PrintWriter saida;
    private Socket socket;
    private String grupoAtual = "Geral";
    private boolean trocandoGrupo;

    private ClienteVisual(String host, int porta, String nome) throws IOException {
        super("Chat Multiusuario");
        this.nome = nome;
        conectar(host, porta);
        construirTela();
    }

    private void conectar(String host, int porta) throws IOException {
        socket = new Socket(host, porta);
        saida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        saida.println(nome);
        Thread leitor = new Thread(() -> {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = entrada.readLine()) != null) {
                    String recebida = linha;
                    SwingUtilities.invokeLater(() -> receber(recebida));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> adicionarMensagem("Sistema", "Conexao encerrada."));
            }
        }, "leitor-chat");
        leitor.setDaemon(true);
        leitor.start();
    }

    private void construirTela() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 560));
        mensagens.setLayout(new BoxLayout(mensagens, BoxLayout.Y_AXIS));
        mensagens.setBackground(new Color(245, 247, 250));
        rolagem.setBorder(BorderFactory.createEmptyBorder());

        grupos.addElement("Geral");
        listaGrupos.setSelectedValue("Geral", true);
        listaGrupos.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) trocarGrupo(listaGrupos.getSelectedValue()); });
        JPanel lateral = new JPanel(new BorderLayout(6, 6));
        lateral.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 8));
        lateral.add(new JLabel("GRUPOS"), BorderLayout.NORTH);
        lateral.add(new JScrollPane(listaGrupos), BorderLayout.CENTER);
        JButton criar = new JButton("+ Criar grupo");
        criar.addActionListener(e -> criarGrupo());
        JButton convidar = new JButton("Convidar pessoa");
        convidar.addActionListener(e -> convidarPessoa());
        JPanel acoesGrupo = new JPanel(new BorderLayout(4, 4));
        acoesGrupo.add(criar, BorderLayout.NORTH); acoesGrupo.add(convidar, BorderLayout.SOUTH);
        lateral.add(acoesGrupo, BorderLayout.SOUTH);

        JPanel conversa = new JPanel(new BorderLayout(8, 8));
        conversa.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 14));
        conversa.add(rolagem, BorderLayout.CENTER);
        JPanel envio = new JPanel(new BorderLayout(8, 0));
        JButton enviar = new JButton("Enviar");
        enviar.addActionListener(e -> enviarMensagem());
        texto.addActionListener(e -> enviarMensagem());
        envio.add(texto, BorderLayout.CENTER); envio.add(enviar, BorderLayout.EAST);
        conversa.add(envio, BorderLayout.SOUTH);
        JPanel chatGrupos = new JPanel(new BorderLayout());
        chatGrupos.add(lateral, BorderLayout.WEST); chatGrupos.add(conversa, BorderLayout.CENTER);

        conversaIa.setEditable(false); conversaIa.setLineWrap(true); conversaIa.setWrapStyleWord(true);
        JPanel iaPainel = new JPanel(new BorderLayout(8, 8)); iaPainel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        iaPainel.add(new JScrollPane(conversaIa), BorderLayout.CENTER);
        JPanel iaEnvio = new JPanel(new BorderLayout(8, 0));
        JButton perguntar = new JButton("Perguntar");
        perguntar.addActionListener(e -> perguntarIa()); perguntaIa.addActionListener(e -> perguntarIa());
        iaEnvio.add(perguntaIa, BorderLayout.CENTER); iaEnvio.add(perguntar, BorderLayout.EAST);
        JButton configurar = new JButton("Configurar IA"); configurar.addActionListener(e -> configurarIa());
        JPanel iaRodape = new JPanel(new BorderLayout(8, 0)); iaRodape.add(configurar, BorderLayout.WEST); iaRodape.add(iaEnvio, BorderLayout.CENTER);
        iaPainel.add(iaRodape, BorderLayout.SOUTH);

        JTabbedPane abas = new JTabbedPane(); abas.addTab("Conversas", chatGrupos); abas.addTab("Assistente IA", iaPainel);
        add(abas); pack(); setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { fechar(); } });
    }

    private void receber(String linha) {
        if ("__LIMPAR_HISTORICO__".equals(linha)) { mensagens.removeAll(); mensagens.revalidate(); mensagens.repaint(); return; }
        if (linha.startsWith("__CONVITE_GRUPO__")) { receberConvite(linha.substring(17)); return; }
        int fimNome = linha.indexOf(": ");
        String remetente = fimNome > 0 ? linha.substring(0, fimNome).replaceFirst("^\\[[^]]+\\] ", "") : "Sistema";
        String conteudo = fimNome > 0 ? linha.substring(fimNome + 2) : linha;
        adicionarMensagem(remetente, conteudo);
    }

    private void adicionarMensagem(String remetente, String conteudo) {
        JLabel bolha = new JLabel("<html><b>" + html(remetente) + "</b><br>" + html(conteudo) + "</html>");
        bolha.setOpaque(true); bolha.setBackground(remetente.equals(nome) ? new Color(212, 241, 220) : Color.WHITE);
        bolha.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JPanel linha = new JPanel(new FlowLayout(remetente.equals(nome) ? FlowLayout.RIGHT : FlowLayout.LEFT));
        linha.setOpaque(false); linha.add(bolha); mensagens.add(linha); mensagens.revalidate();
        SwingUtilities.invokeLater(() -> rolagem.getVerticalScrollBar().setValue(rolagem.getVerticalScrollBar().getMaximum()));
    }

    private void criarGrupo() {
        String grupo = JOptionPane.showInputDialog(this, "Nome do novo grupo:");
        if (grupo == null || grupo.isBlank()) return;
        grupo = grupo.trim();
        if (!grupo.matches("[\\p{L}\\p{N} _-]{1,40}")) { JOptionPane.showMessageDialog(this, "Use ate 40 letras, numeros, espacos, _ ou -."); return; }
        if (!contemGrupo(grupo)) grupos.addElement(grupo);
        listaGrupos.setSelectedValue(grupo, true);
    }

    private void convidarPessoa() {
        String pessoa = JOptionPane.showInputDialog(this, "Nome da pessoa conectada para convidar a " + grupoAtual + ":");
        if (pessoa != null && !pessoa.isBlank()) saida.println("/convidar " + pessoa.trim());
    }

    private void receberConvite(String grupo) {
        if (!contemGrupo(grupo)) grupos.addElement(grupo);
        int escolha = JOptionPane.showConfirmDialog(this, "Voce foi convidado para o grupo '" + grupo + "'. Entrar agora?", "Convite para grupo", JOptionPane.YES_NO_OPTION);
        if (escolha == JOptionPane.YES_OPTION) listaGrupos.setSelectedValue(grupo, true);
    }

    private boolean contemGrupo(String grupo) { for (int i = 0; i < grupos.size(); i++) if (grupos.get(i).equals(grupo)) return true; return false; }
    private void trocarGrupo(String grupo) {
        if (grupo == null || grupo.equals(grupoAtual) || trocandoGrupo) return;
        grupoAtual = grupo; trocandoGrupo = true; mensagens.removeAll(); mensagens.revalidate(); mensagens.repaint();
        saida.println("/grupo " + grupo); trocandoGrupo = false;
    }
    private void enviarMensagem() { String valor = texto.getText().trim(); if (!valor.isEmpty()) { saida.println(valor); texto.setText(""); } }

    private void configurarIa() {
        String endpoint = JOptionPane.showInputDialog(this, "Endpoint (OpenAI ou Ollama):", configIa == null ? "https://api.openai.com/v1/chat/completions" : configIa.endpoint());
        if (endpoint == null) return;
        String chave = JOptionPane.showInputDialog(this, "Chave da API (vazio para Ollama):", configIa == null ? System.getenv().getOrDefault("OPENAI_API_KEY", "") : configIa.chave());
        if (chave == null) return;
        String modelo = JOptionPane.showInputDialog(this, "Modelo:", configIa == null ? "gpt-4o-mini" : configIa.modelo());
        if (modelo != null && !modelo.isBlank()) { configIa = new ServicoIa.Configuracao(endpoint.trim(), chave.trim(), modelo.trim()); conversaIa.append("IA configurada: " + modelo.trim() + "\n\n"); }
    }
    private void perguntarIa() {
        String pergunta = perguntaIa.getText().trim(); if (pergunta.isEmpty()) return;
        if (configIa == null) { configurarIa(); if (configIa == null) return; }
        perguntaIa.setText(""); conversaIa.append("Voce: " + pergunta + "\nIA esta respondendo...\n");
        new Thread(() -> { try { String resposta = ia.responder(pergunta, configIa); SwingUtilities.invokeLater(() -> conversaIa.append("IA: " + resposta + "\n\n")); }
            catch (Exception e) { SwingUtilities.invokeLater(() -> conversaIa.append("Erro ao consultar a IA: " + e.getMessage() + "\n\n")); } }, "consulta-ia").start();
    }
    private void fechar() { try { if (saida != null) saida.println("/sair"); if (socket != null) socket.close(); } catch (IOException ignored) { } }
    private static String html(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>"); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String nome = JOptionPane.showInputDialog(null, "Seu nome:"); if (nome == null || nome.isBlank()) return;
            try { ClienteVisual cliente = new ClienteVisual(args.length > 0 ? args[0] : "localhost", args.length > 1 ? Integer.parseInt(args[1]) : 5000, nome.trim()); cliente.setVisible(true); }
            catch (Exception e) { JOptionPane.showMessageDialog(null, "Nao foi possivel conectar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        });
    }
}
