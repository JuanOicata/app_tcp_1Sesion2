package org.vinni.servidor.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class PrincipalCli extends JFrame {

    private final int PORT = 12345;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;

    private JTextArea mensajesTxt;
    private JTextField mensajeTxt;
    private JButton bConectar, btEnviar, btArchivo;

    private int miID;

    public PrincipalCli() {

        setTitle("CLIENTE TCP");
        setSize(500,420);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bConectar = new JButton("CONECTAR");
        bConectar.setBounds(150,20,200,40);
        add(bConectar);

        mensajeTxt = new JTextField();
        mensajeTxt.setBounds(50,80,300,30);
        add(mensajeTxt);

        btEnviar = new JButton("Enviar Mensaje");
        btEnviar.setBounds(360,80,120,30);
        add(btEnviar);

        btArchivo = new JButton("Enviar Archivo");
        btArchivo.setBounds(150,120,200,30);
        add(btArchivo);

        mensajesTxt = new JTextArea();
        JScrollPane scroll = new JScrollPane(mensajesTxt);
        scroll.setBounds(30,170,430,180);
        add(scroll);

        bConectar.addActionListener(e -> conectar());
        btEnviar.addActionListener(e -> enviarMensaje());
        btArchivo.addActionListener(e -> enviarArchivo());
    }

    private void conectar() {
        try {
            socket = new Socket("localhost", PORT);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            new Thread(this::recibir).start();

        } catch (Exception e) {
            mensajesTxt.append("Error conexión\n");
        }
    }

    private void enviarMensaje() {
        try {
            dos.writeUTF("MSG");
            dos.writeUTF(mensajeTxt.getText());

            String destinoStr = JOptionPane.showInputDialog(
                    "0 = todos | ID específico:");

            int destino = Integer.parseInt(destinoStr);

            dos.writeInt(destino);

            mensajeTxt.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviarArchivo() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {

                File archivo = fileChooser.getSelectedFile();

                dos.writeUTF("FILE");
                dos.writeUTF(archivo.getName());
                dos.writeLong(archivo.length());

                String destinoStr = JOptionPane.showInputDialog(
                        "0 = todos | ID específico:");

                int destino = Integer.parseInt(destinoStr);

                dos.writeInt(destino);

                FileInputStream fis = new FileInputStream(archivo);
                byte[] buffer = new byte[4096];
                int bytes;

                while ((bytes = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, bytes);
                }

                fis.close();

                mensajesTxt.append("Archivo enviado\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recibir() {
        try {
            while (true) {

                String tipo = dis.readUTF();

                if (tipo.equals("ID")) {
                    miID = dis.readInt();
                    mensajesTxt.append("Mi ID es: " + miID + "\n");
                }

                else if (tipo.equals("MSG")) {
                    String mensaje = dis.readUTF();
                    mensajesTxt.append(mensaje + "\n");
                }

                else if (tipo.equals("FILE")) {

                    String nombre = dis.readUTF();
                    long tamaño = dis.readLong();

                    byte[] archivo = new byte[(int) tamaño];
                    dis.readFully(archivo);

                    guardarEnEscritorio("ARCHIVOS_RECIBIDOS", nombre, archivo);

                    mensajesTxt.append("Archivo recibido: " + nombre + "\n");

                    Desktop.getDesktop().open(
                            new File(System.getProperty("user.home")
                                    + "/Desktop/ARCHIVOS_RECIBIDOS"));
                }
            }
        } catch (Exception e) {
            mensajesTxt.append("Desconectado\n");
        }
    }

    private void guardarEnEscritorio(String carpetaNombre,
                                     String nombre,
                                     byte[] archivo) throws IOException {

        File carpeta = new File(System.getProperty("user.home")
                + "/Desktop/" + carpetaNombre);

        if (!carpeta.exists())
            carpeta.mkdir();

        File archivoGuardado = new File(carpeta, nombre);

        FileOutputStream fos = new FileOutputStream(archivoGuardado);
        fos.write(archivo);
        fos.close();
    }

    public static void main(String[] args) {
        new PrincipalCli().setVisible(true);
    }
}
