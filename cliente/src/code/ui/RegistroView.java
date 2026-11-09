package ui;
import javax.swing.*;
import java.awt.*;

public class RegistroView extends JPanel {
    private JTextField campoUsuario;
    private JPasswordField campoContrasena;
    private JPasswordField campoRepetirContrasena;
    private JButton botonCrearCuenta = new JButton("Crear cuenta");
    private JButton botonIrAlInicio = new JButton("¿Ya estás registrado? Inicia sesión");

    public RegistroView(Runnable alIrAlInicio) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); // Margen alrededor de cada componente
        c.gridx = 0; // Siempre en la columna 0
        c.fill = GridBagConstraints.HORIZONTAL;

        campoUsuario = new JTextField(15);
        campoContrasena = new JPasswordField(15);
        campoRepetirContrasena = new JPasswordField(15);

        // ----- FILAS -----
        c.gridy = 0;
        JLabel titulo = new JLabel("Registro");
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
        add(new JLabel("Repetir contraseña: "), c);

        c.gridy = 6;
        add(campoRepetirContrasena, c);

        c.gridy = 7;
        add(botonCrearCuenta, c);

        c.gridy = 8;
        add(botonIrAlInicio, c);

        botonCrearCuenta.addActionListener(e -> {
            
            String usuario = campoUsuario.getText();
            String contrasena = new String(campoContrasena.getPassword());
            String repetida = new String(campoRepetirContrasena.getPassword());

            if (!contrasena.equals(repetida)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
                return;
            }

            System.out.println("Intento de registro: " + usuario);
        });

        botonIrAlInicio.addActionListener(e -> alIrAlInicio.run());
    }
}
