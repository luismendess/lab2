import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Servidor {
    private static ServerSocket server;
    private final String ARQUIVO_FORTUNAS = "src/fortune-br.txt";
    private final String DELIMITADOR = "%";
    private List<String> fortunas = new ArrayList<>();
    private int porta = 1025;

    public void iniciar() {
        System.out.println("\nServidor iniciado na porta: " + porta);
        carregarFortunas();

        try {
            server = new ServerSocket(porta);

            while (true) {
                try (Socket socket = server.accept();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream()))) {

                    String mensagemJSON = reader.readLine();
                    System.out.println("Mensagem recebida: " + mensagemJSON);

                    String resposta = processarMensagem(mensagemJSON);
                    System.out.println("Resposta enviada: " + resposta);

                    writer.write(resposta);
                    writer.newLine();
                    writer.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    private void carregarFortunas() {
        fortunas.clear();
        try {
            // Ler o arquivo e dividir pelo delimitador
            if (!Files.exists(Paths.get(ARQUIVO_FORTUNAS))) {
                System.out.println("Arquivo de fortunas não encontrado.");
                return;
            }
            String conteudo = Files.readString(Paths.get(ARQUIVO_FORTUNAS));
            String[] partes = conteudo.split("\n" + DELIMITADOR + "\n");

            for (String parte : partes) {
                String fortuna = parte.trim();
                if (!fortuna.isEmpty()) {
                    fortunas.add(fortuna);
                }
            }
            System.out.println("\nFortunas carregadas: " + fortunas.size());
        } catch (IOException e) {
            System.out.println("Erro ao carregar fortunas: " + e.getMessage());
        }
    }

    private String processarMensagem(String json) {
        try {
            // Extrair método de forma mais precisa
            int methodStart = json.indexOf("\"method\":\"") + 10;
            int methodEnd = json.indexOf("\"", methodStart);
            String method = json.substring(methodStart, methodEnd);

            if ("read".equals(method)) {
                return processarLeitura();
            } else if ("write".equals(method)) {
                // Extrair argumentos
                int argsStart = json.indexOf("\"args\":[\"") + 9;
                int argsEnd = json.indexOf("\"]", argsStart);
                String args = json.substring(argsStart, argsEnd);
                return processarEscrita(args);
            }
        } catch (Exception e) {
            System.out.println("Erro no parsing: " + e.getMessage());
        }
        return "{\"result\":\"false\"}";
    }

    private String processarLeitura() {
        if (fortunas.isEmpty()) {
            return "{\"result\":\"Nenhuma fortuna disponível\"}";
        }

        Random rand = new Random();
        String fortuna = fortunas.get(rand.nextInt(fortunas.size()));

        // Formatar corretamente com escape
        return "{\"result\":\"" +
                fortuna.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                +
                "\"}";
    }

    private String processarEscrita(String novaFortuna) {
        try {
            novaFortuna = novaFortuna.replace("\\n", "\n");

            // Ler o conteúdo atual do arquivo
            Path caminhoArquivo = Paths.get(ARQUIVO_FORTUNAS);
            String conteudoAtual = Files.exists(caminhoArquivo) ? Files.readString(caminhoArquivo) : "";

            // Verificar as condições do final do arquivo
            if (conteudoAtual.endsWith("%\n")) {
                // Caso termine com "%\n", adicionar a nova fortuna diretamente
                conteudoAtual += novaFortuna + "\n%\n";
            } else if (conteudoAtual.endsWith("%")) {
                // Caso termine com "%", adicionar uma quebra de linha antes da nova fortuna
                conteudoAtual += "\n" + novaFortuna + "\n%\n";
            } else if (conteudoAtual.endsWith("\n") && conteudoAtual.trim().endsWith("%")) {
                // Caso a última linha esteja em branco e a penúltima seja "%", adicionar
                // diretamente
                conteudoAtual += novaFortuna + "\n";
            } else {
                // Caso contrário, adicionar um "%" antes da nova fortuna
                conteudoAtual += "\n%\n" + novaFortuna + "\n%\n";
            }

            // Escrever o conteúdo atualizado no arquivo
            Files.writeString(caminhoArquivo, conteudoAtual, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            // Atualizar a lista em memória
            fortunas.add(novaFortuna);

            return "{\"result\":\"Fortuna adicionada com sucesso!\"}";
        } catch (IOException e) {
            return "{\"result\":\"Erro ao salvar fortuna\"}";
        }
    }

    public static void main(String[] args) {
        new Servidor().iniciar();
    }
}