package org.vinni.servidor.gui;


import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Author: Vinni
 */
public class PrincipalSrv extends javax.swing.JFrame {
    private final int PORT = 12345;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private int contadorClientes = 0;

    private Map<Integer, PrintWriter> clientes =
            java.util.Collections.synchronizedMap(new HashMap<>());



    /**
     * Creates new form Principal1
     */
    public PrincipalSrv() {
        initComponents();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        this.setTitle("Servidor ...");

        bIniciar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        mensajesTxt = new JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        bIniciar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bIniciar.setText("INICIAR SERVIDOR");
        bIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bIniciarActionPerformed(evt);
            }
        });
        getContentPane().add(bIniciar);
        bIniciar.setBounds(100, 90, 250, 40);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));
        jLabel1.setText("SERVIDOR TCP : HOEL");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(150, 10, 160, 17);

        mensajesTxt.setColumns(25);
        mensajesTxt.setRows(5);

        jScrollPane1.setViewportView(mensajesTxt);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(20, 160, 410, 70);

        setSize(new java.awt.Dimension(491, 290));
        setLocationRelativeTo(null);
    }// </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrincipalSrv().setVisible(true);
            }
        });

    }
    private void bIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        iniciarServidor();
    }

    private void iniciarServidor() {
        JOptionPane.showMessageDialog(this, "Iniciando servidor");
        new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress addr = InetAddress.getLocalHost();
                    serverSocket = new ServerSocket( PORT);
                    mensajesTxt.append("Servidor TCP en ejecución: "+ addr + " ,Puerto " + serverSocket.getLocalPort()+ "\n");
                    while (true) {
                        Socket cliente = serverSocket.accept();

                        contadorClientes++;
                        int numeroCliente = contadorClientes;

                        PrintWriter out = new PrintWriter(
                                cliente.getOutputStream(), true);

                        clientes.put(numeroCliente, out);

                        mensajesTxt.append("Cliente " + numeroCliente + " conectado\n");

                        out.println("Conectado como Cliente " + numeroCliente);

                        new Thread(() -> manejarCliente(cliente, numeroCliente)).start();
                    }




                } catch (IOException ex) {
                    ex.printStackTrace();
                    mensajesTxt.append("Error en el servidor: " + ex.getMessage() + "\n");
                }
            }
        }).start();
    }

    private void manejarCliente(Socket cliente, int numeroCliente) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(cliente.getInputStream()));

            String linea;

            while ((linea = in.readLine()) != null) {

                // Formato esperado: destino:mensaje
                String[] partes = linea.split(":", 2);

                if (partes.length == 2) {
                    int destino = Integer.parseInt(partes[0]);
                    String mensaje = partes[1];

                    enviarACliente(destino,
                            "Cliente " + numeroCliente + " dice: " + mensaje);

                    mensajesTxt.append(
                            "Cliente " + numeroCliente +
                                    " → Cliente " + destino + ": " + mensaje + "\n"
                    );
                } else {
                    mensajesTxt.append("Formato inválido de Cliente " + numeroCliente + "\n");
                }
            }

        } catch (Exception e) {
            mensajesTxt.append("Cliente " + numeroCliente + " desconectado\n");
        } finally {
            clientes.remove(numeroCliente);
        }
    }

    private void enviarACliente(int destino, String mensaje) {

        synchronized (clientes) {
            PrintWriter clienteDestino = clientes.get(destino);

            if (clienteDestino != null) {
                clienteDestino.println(mensaje);
            } else {
                mensajesTxt.append("Cliente destino no existe\n");
            }
        }
    }


    /*private void enviarATodos(String mensaje) {
        synchronized (clientes) {
            for (PrintWriter cliente : clientes) {
                cliente.println(mensaje);
            }
        }
    }*/



    // Variables declaration - do not modify
    private javax.swing.JButton bIniciar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextArea mensajesTxt;
    private javax.swing.JScrollPane jScrollPane1;
}
//*intento commit//