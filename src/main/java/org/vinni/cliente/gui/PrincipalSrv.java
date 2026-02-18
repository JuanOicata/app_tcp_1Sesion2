package org.vinni.cliente.gui;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrincipalSrv extends JFrame {

    private final int PORT = 12345;
    private ServerSocket serverSocket;
    private JTextArea mensajesTxt;
    private JButton bIniciar;

    private int contadorClientes = 0;
    private Map<Integer, Socket> clientes = new ConcurrentHashMap<>();

    public PrincipalSrv() {

        setTitle("SERVIDOR INCLIENTES");
        setSize(500,300);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bIniciar = new JButton("INICIAR SERVIDOR");
        bIniciar.setBounds(120, 40, 250, 40);
        add(bIniciar);

        mensajesTxt = new JTextArea();
        JScrollPane scroll = new JScrollPane(mensajesTxt);
        scroll.setBounds(20, 100, 440, 150);
        add(scroll);

        bIniciar.addActionListener(e -> iniciarServidor());
    }

    private void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                mensajesTxt.append("Servidor iniciado en puerto " + PORT + "\n");

                while (true) {
                    Socket cliente = serverSocket.accept();
                    contadorClientes++;
                    int id = contadorClientes;

                    clientes.put(id, cliente);
                    mensajesTxt.append("Cliente " + id + " conectado\n");

                    new Thread(() -> manejarCliente(cliente, id)).start();
                }

            } catch (Exception e) {
                mensajesTxt.append("Error servidor\n");
            }
        }).start();
    }

    private boolean validarArchivo(String nombre, long tamaño) {

        if (tamaño < 1024 || tamaño > (5 * 1024 * 1024))
            return false;

        String n = nombre.toLowerCase();

        if (n.endsWith(".exe") || n.endsWith(".wat")
                || n.endsWith(".bat") || n.endsWith(".bar"))
            return false;

        return true;
    }

    private void manejarCliente(Socket cliente, int id) {

        try {
            DataInputStream dis = new DataInputStream(cliente.getInputStream());

            // Enviar ID al cliente cuando conecta
            DataOutputStream dosInit = new DataOutputStream(cliente.getOutputStream());
            dosInit.writeUTF("ID");
            dosInit.writeInt(id);

            while (true) {

                String tipo = dis.readUTF();

                if (tipo.equals("MSG")) {

                    String mensaje = dis.readUTF();
                    int destino = dis.readInt();

                    mensajesTxt.append("Cliente " + id + ": " + mensaje + "\n");

                    if (destino == 0) {
                        for (Map.Entry<Integer, Socket> entry : clientes.entrySet()) {
                            if (entry.getKey() != id) {
                                enviarMensaje(entry.getValue(), id, mensaje);
                            }
                        }
                    } else {
                        Socket s = clientes.get(destino);
                        if (s != null)
                            enviarMensaje(s, id, mensaje);
                    }
                }

                else if (tipo.equals("FILE")) {

                    String nombre = dis.readUTF();
                    long tamaño = dis.readLong();
                    int destino = dis.readInt();

                    if (!validarArchivo(nombre, tamaño)) {
                        mensajesTxt.append("Archivo rechazado\n");
                        continue;
                    }

                    byte[] archivo = new byte[(int) tamaño];
                    dis.readFully(archivo);

                    // Guardar en escritorio servidor
                    guardarEnEscritorio("ARCHIVOS_SERVIDOR", nombre, archivo);

                    mensajesTxt.append("Archivo recibido: " + nombre + "\n");

                    if (destino == 0) {
                        for (Map.Entry<Integer, Socket> entry : clientes.entrySet()) {
                            if (entry.getKey() != id) {
                                reenviarArchivo(entry.getValue(), nombre, archivo);
                            }
                        }
                    } else {
                        Socket s = clientes.get(destino);
                        if (s != null)
                            reenviarArchivo(s, nombre, archivo);
                    }
                }
            }

        } catch (Exception e) {
            mensajesTxt.append("Cliente " + id + " desconectado\n");
            clientes.remove(id);
        }
    }

    private void enviarMensaje(Socket socket, int idOrigen, String mensaje) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("MSG");
            dos.writeUTF("Cliente " + idOrigen + ": " + mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reenviarArchivo(Socket socket, String nombre, byte[] archivo) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("FILE");
            dos.writeUTF(nombre);
            dos.writeLong(archivo.length);
            dos.write(archivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarEnEscritorio(String carpetaNombre, String nombre, byte[] archivo) throws IOException {

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
        new PrincipalSrv().setVisible(true);
    }
}
