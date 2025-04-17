/*
 * Arquivo desenvolvido por: Iago Macarini e Luis Henrique Mendes
 * lab2 - Cliente e Servidor
 * Data: 21/04/2025
 */

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Cliente {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 1025;

    public void iniciar() {
        System.out.println("\nCliente iniciado na porta: " + PORT);
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (true) {
                System.out.println("\n|============= MENU =============|");
                System.out.println("| 1 - Ler uma fortuna aleatória  |");
                System.out.println("| 2 - Escrever uma nova fortuna  |");
                System.out.println("| 3 -          Sair              |");
                System.out.println("|================================|");
                System.out.print("Escolha uma opção: ");
                String opcao = consoleReader.readLine().trim();

                if (opcao.equals("3")) {
                    System.out.println("\nSaindo do cliente...");
                    Thread.sleep(1000);
                    System.out.println("Cliente encerrado.");
                    break;
                }
                String request;
                if (opcao.equals("1")) {
                    request = "{\"method\":\"read\"}\n";
                } else if (opcao.equals("2")) {
                    System.out.println("\nDigite a nova fortuna:");
                    String mensagem = consoleReader.readLine();
                    request = "{\"method\":\"write\",\"args\":[\"" +
                            mensagem.replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    .replace("\n", "\\n")
                            + "\"]}\n";
                } else {
                    System.out.println("Opção inválida!");
                    continue;
                }

                try (Socket socket = new Socket(SERVER_IP, PORT);
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                    // Envia requisição
                    writer.write(request);
                    writer.flush();

                    // Processa resposta
                    String response = reader.readLine();
                    if (opcao.equals("1")) {
                        System.out.println("\nFortuna recebida:\n");
                        Thread.sleep(1000);
                        System.out.println(parseResponse(response));
                    } else {
                        Thread.sleep(1000);
                        reader.readLine(); // Descarta a resposta
                        System.out.println("\nFortuna adicionada com sucesso!");
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String parseResponse(String response) {
        try {
            int start = response.indexOf("\"result\":\"") + 10;
            int end = response.lastIndexOf("\"");
            if (start < 10 || end <= start)
                return "Resposta inválida";

            return response.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        } catch (Exception e) {
            return "Erro na resposta: " + response;
        }
    }

    public static void main(String[] args) {
        new Cliente().iniciar();
    }
}