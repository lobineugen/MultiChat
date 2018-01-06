package com.lobin.eugene;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

public class Client extends JFrame {
    private static final int PORT = 5364;

    private BufferedReader in;
    private PrintWriter out;
    private JTextField text = new JTextField();
    private JTextArea messageArea = new JTextArea();
    private JButton bSend = new JButton("Send");
    private String name;
    private JScrollPane message = new JScrollPane(messageArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    private Client() {
        super("Chatter");
        this.setSize(300, 300);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        text.setEditable(false);
        messageArea.setEditable(false);
        bSend.setEnabled(false);


        this.setLocationRelativeTo(null);
        this.setResizable(false);

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        panel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(message)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(text)
                        .addComponent(bSend)
                )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(message)
                .addGroup(layout.createParallelGroup()
                        .addComponent(text)
                        .addComponent(bSend)
                )
        );

        layout.linkSize(SwingConstants.VERTICAL, text, bSend);

        this.add(panel);
        this.setVisible(true);

        bSend.addActionListener(new SendMessage());
        text.addActionListener(new SendMessage());
    }

    class SendMessage implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!text.getText().equals("")) {
                out.println(text.getText());
                text.setText("");
                text.setBorder(BorderFactory.createLineBorder(Color.gray));
            } else {
                text.setBorder(BorderFactory.createLineBorder(Color.red));
            }
        }
    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                this,
                "Enter IP Address of the Server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    private String getUserName() {
        return JOptionPane.showInputDialog(
                this,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void nameUsed() {
        JOptionPane.showMessageDialog(this,
                "Please, input new name!",
                "This name used!",
                JOptionPane.ERROR_MESSAGE);
    }

    private void run() throws IOException {
        String serverAddress = getServerAddress();
        Socket socket = null;
        try {
            do {
                if ("".equals(serverAddress)) {
                    serverAddress = getServerAddress();
                } else if (serverAddress == null) {
                    messageArea.append("Connection failed");
                    break;
                } else {
                    socket = new Socket(serverAddress, PORT);
                    break;
                }
            } while (true);

            if (socket != null) {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    String line = in.readLine();
                    if (line.startsWith("SUBMITNAME")) {
                        name = getUserName();
                        out.println(name);
                    } else if (line.startsWith("NAMEUSED")) {
                        nameUsed();
                    } else if (line.startsWith("NAMEACCEPTED")) {
                        this.setTitle("Chatter - " + name);
                        text.setEditable(true);
                        bSend.setEnabled(true);
                    } else if (line.startsWith("MESSAGE")) {
                        messageArea.append(line.substring(8) + "\n");
                    }
                }
            }
        } catch (UnknownHostException ex) {
            messageArea.append("Connection failed");
        }

    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }
}