package ui;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JPanel {
    private JTextField campoUsuario;
    private JPasswordField campoContrasena;
    private JButton botonEntrar;
    private JButton botonIrAlRegistro;

    public LoginView(Runnable alIrAlRegistro, Runnable alEntrar) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); // Margen alrededor de cada componente
        c.gridx = 0; // Siempre en la columna 0
        c.fill = GridBagConstraints.HORIZONTAL;

        campoUsuario = new JTextField(15);
        campoContrasena = new JPasswordField(15);
        botonEntrar = new JButton("Entrar");
        botonIrAlRegistro = new JButton("¿No tienes cuenta? Regístrate");

        // ----- FILAS -----
        c.gridy = 0;
        JLabel titulo = new JLabel("Iniciar sesión");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(titulo, c);

        c.gridy = 1;
        add(new JLabel("Usuario: "), c);

        c.gridy = 2;
        add(campoUsuario, c);

        c.gridy = 3;
        add(new JLabel("Contraseña: "), c);

        c.gridy = 4;
        add(campoContrasena, c);

        c.gridy = 5;
        add(botonEntrar, c);

        c.gridy = 6;
        add(botonIrAlRegistro, c);

        botonEntrar.addActionListener(e -> {
            String usuario = campoUsuario.getText();
            System.out.println("Intento de inicio: " + usuario);
            alEntrar.run();
        });

        botonIrAlRegistro.addActionListener(e -> alIrAlRegistro.run());
    }
}