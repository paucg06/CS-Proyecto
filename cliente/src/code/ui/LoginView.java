package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JPanel {

    private JTextField campoUsuario;
    private JPasswordField campoContrasena;
    private JButton botonEntrar;
    private JButton botonIrAlRegistro;

    private final Color AZUL = new Color(26, 115, 232);
    private final Color FONDO = new Color(248, 249, 250);
    private final Color TEXTO = new Color(32, 33, 36);
    private final Color GRIS = new Color(95, 99, 104);

    public LoginView(Runnable alIrAlRegistro, Runnable alEntrar) {

        setLayout(new GridBagLayout());
        setBackground(FONDO);

        // TARJETA PRINCIPAL
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(new EmptyBorder(35, 45, 35, 45));

        tarjeta.setPreferredSize(new Dimension(380, 450));

        // LOGO
        JLabel logo = new JLabel("☁ MiCloud");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(AZUL);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // TÍTULO
        JLabel titulo = new JLabel("Iniciar sesión");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TEXTO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // SUBTÍTULO
        JLabel subtitulo = new JLabel("Accede a tus archivos desde cualquier lugar");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(GRIS);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // CAMPO USUARIO
        JLabel etiquetaUsuario = new JLabel("Usuario");
        configurarEtiqueta(etiquetaUsuario);

        campoUsuario = new JTextField();
        configurarCampo(campoUsuario);

        // CAMPO CONTRASEÑA
        JLabel etiquetaContrasena = new JLabel("Contraseña");
        configurarEtiqueta(etiquetaContrasena);

        campoContrasena = new JPasswordField();
        configurarCampo(campoContrasena);

        // BOTÓN ENTRAR
        botonEntrar = new JButton("Entrar");
        botonEntrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        botonEntrar.setForeground(Color.WHITE);
        botonEntrar.setBackground(AZUL);
        botonEntrar.setFocusPainted(false);
        botonEntrar.setBorderPainted(false);
        botonEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        botonEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // BOTÓN REGISTRO
        botonIrAlRegistro = new JButton("¿No tienes cuenta? Regístrate");
        botonIrAlRegistro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        botonIrAlRegistro.setForeground(AZUL);
        botonIrAlRegistro.setBackground(Color.WHITE);
        botonIrAlRegistro.setBorderPainted(false);
        botonIrAlRegistro.setFocusPainted(false);
        botonIrAlRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonIrAlRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);

        // AÑADIR COMPONENTES
        tarjeta.add(logo);
        tarjeta.add(Box.createVerticalStrut(20));

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(8));

        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(30));

        tarjeta.add(etiquetaUsuario);
        tarjeta.add(Box.createVerticalStrut(7));

        tarjeta.add(campoUsuario);
        tarjeta.add(Box.createVerticalStrut(18));

        tarjeta.add(etiquetaContrasena);
        tarjeta.add(Box.createVerticalStrut(7));

        tarjeta.add(campoContrasena);
        tarjeta.add(Box.createVerticalStrut(25));

        tarjeta.add(botonEntrar);
        tarjeta.add(Box.createVerticalStrut(15));

        tarjeta.add(botonIrAlRegistro);

        add(tarjeta);

        // EVENTOS
        botonEntrar.addActionListener(e -> {

            String usuario = campoUsuario.getText();

            System.out.println("Intento de inicio: " + usuario);

            alEntrar.run();
        });

        botonIrAlRegistro.addActionListener(e -> alIrAlRegistro.run());
    }

    private void configurarEtiqueta(JLabel etiqueta) {

        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiqueta.setForeground(TEXTO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configurarCampo(JTextField campo) {

        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        campo.setPreferredSize(new Dimension(290, 42));

        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}