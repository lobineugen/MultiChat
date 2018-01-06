package com.lobin.eugene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;

public class Server {
    private static final int PORT = 5364;
    private static HashSet<String> names = new HashSet<>();
    private static HashSet<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new Handler(listener.accept()).start();
            }
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                System.out.println("User: " + socket.getInetAddress()
                        .toString().replace("/", "")
                        + " connected;");
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        } else {
                            out.println("NAMEUSED");
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.add(out);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " connected to chat!");
                }


                while (true) {
                    try {
                        String input = in.readLine();
                        if (input == null) {
                            return;
                        }
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    } catch (SocketException ex) {
                        System.out.println("User " + socket.getInetAddress()
                                .toString().replace("/", "")
                                + " disconnected;");
                        break;
                    }
                }
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " disconnected from chat!");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}