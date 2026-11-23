package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterView extends JPanel {

    private JTextField campoUsuario;
    private JTextField campoCorreo;
    private JPasswordField campoContrasena;
    private JPasswordField campoRepetirContrasena;

    private JButton botonRegistrar;
    private JButton botonVolverLogin;

    private final Color AZUL = new Color(26, 115, 232);
    private final Color FONDO = new Color(248, 249, 250);
    private final Color TEXTO = new Color(32, 33, 36);
    private final Color GRIS = new Color(95, 99, 104);

    public RegisterView(Runnable alVolverLogin) {

        setLayout(new GridBagLayout());
        setBackground(FONDO);

        // TARJETA PRINCIPAL
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(new EmptyBorder(25, 45, 25, 45));
        tarjeta.setPreferredSize(new Dimension(400, 550));

        // LOGO
        JLabel logo = new JLabel("☁ MiCloud");
        logo.setFont(new Font("SansSerif", Font.BOLD, 27));
        logo.setForeground(AZUL);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // TÍTULO
        JLabel titulo = new JLabel("Crear una cuenta");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TEXTO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // SUBTÍTULO
        JLabel subtitulo = new JLabel("Empieza a guardar tus archivos en la nube");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(GRIS);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // USUARIO
        JLabel etiquetaUsuario = crearEtiqueta("Nombre de usuario");
        campoUsuario = crearCampo();

        // CORREO
        JLabel etiquetaCorreo = crearEtiqueta("Correo electrónico");
        campoCorreo = crearCampo();

        // CONTRASEÑA
        JLabel etiquetaContrasena = crearEtiqueta("Contraseña");
        campoContrasena = new JPasswordField();
        configurarCampo(campoContrasena);

        // REPETIR CONTRASEÑA
        JLabel etiquetaRepetir = crearEtiqueta("Repetir contraseña");
        campoRepetirContrasena = new JPasswordField();
        configurarCampo(campoRepetirContrasena);

        // BOTÓN REGISTRAR
        botonRegistrar = new JButton("Crear cuenta");
        botonRegistrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        botonRegistrar.setForeground(Color.WHITE);
        botonRegistrar.setBackground(AZUL);
        botonRegistrar.setFocusPainted(false);
        botonRegistrar.setBorderPainted(false);
        botonRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        botonRegistrar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // BOTÓN VOLVER
        botonVolverLogin = new JButton("¿Ya tienes cuenta? Inicia sesión");
        botonVolverLogin.setFont(new Font("SansSerif", Font.PLAIN, 12));
        botonVolverLogin.setForeground(AZUL);
        botonVolverLogin.setBackground(Color.WHITE);
        botonVolverLogin.setBorderPainted(false);
        botonVolverLogin.setFocusPainted(false);
        botonVolverLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonVolverLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // AÑADIR COMPONENTES
        tarjeta.add(logo);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(5));

        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(20));

        tarjeta.add(etiquetaUsuario);
        tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(campoUsuario);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(etiquetaCorreo);
        tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(campoCorreo);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(etiquetaContrasena);
        tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(campoContrasena);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(etiquetaRepetir);
        tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(campoRepetirContrasena);
        tarjeta.add(Box.createVerticalStrut(20));

        tarjeta.add(botonRegistrar);
        tarjeta.add(Box.createVerticalStrut(10));

        tarjeta.add(botonVolverLogin);

        add(tarjeta);

        // EVENTOS
        botonRegistrar.addActionListener(e -> {

            String usuario = campoUsuario.getText();
            String correo = campoCorreo.getText();

            String contrasena = new String(campoContrasena.getPassword());
            String repetir = new String(campoRepetirContrasena.getPassword());

            if (usuario.trim().isEmpty() ||
                correo.trim().isEmpty() ||
                contrasena.isEmpty() ||
                repetir.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Por favor, completa todos los campos.",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (!contrasena.equals(repetir)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Las contraseñas no coinciden.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            System.out.println("Registro visual:");
            System.out.println("Usuario: " + usuario);
            System.out.println("Correo: " + correo);

            JOptionPane.showMessageDialog(
                    this,
                    "Registro preparado correctamente.",
                    "MiCloud",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        botonVolverLogin.addActionListener(e -> alVolverLogin.run());
    }

    private JLabel crearEtiqueta(String texto) {

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiqueta.setForeground(TEXTO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);

        return etiqueta;
    }

    private JTextField crearCampo() {

        JTextField campo = new JTextField();
        configurarCampo(campo);

        return campo;
    }

    private void configurarCampo(JTextField campo) {

        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        campo.setPreferredSize(new Dimension(300, 38));

        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224)),
                new EmptyBorder(7, 12, 7, 12)
        ));

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}