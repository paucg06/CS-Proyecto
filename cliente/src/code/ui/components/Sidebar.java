package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    private final Color AZUL = new Color(26, 115, 232);
    private final Color AZUL_CLARO = new Color(232, 240, 254);
    private final Color TEXTO = new Color(60, 64, 67);

    private Consumer<String> alCambiarSeccion;
    private Runnable alCerrarSesion;

    public Sidebar(Consumer<String> alCambiarSeccion,
                   Runnable alCerrarSesion) {

        this.alCambiarSeccion = alCambiarSeccion;
        this.alCerrarSesion = alCerrarSesion;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 15, 20, 15));
        setPreferredSize(new Dimension(235, 0));

        crearContenido();
    }

    private void crearContenido() {

        // LOGO
        JLabel logo = new JLabel("☁ MiCloud");
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setForeground(AZUL);
        logo.setBorder(new EmptyBorder(5, 10, 30, 0));

        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(logo);

        // BOTÓN NUEVO
        JButton botonNuevo = new JButton("+ Nuevo");
        botonNuevo.setFont(new Font("SansSerif", Font.BOLD, 14));
        botonNuevo.setForeground(AZUL);
        botonNuevo.setBackground(AZUL_CLARO);
        botonNuevo.setFocusPainted(false);
        botonNuevo.setBorder(new EmptyBorder(12, 20, 12, 20));
        botonNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonNuevo.setAlignmentX(Component.LEFT_ALIGNMENT);

        botonNuevo.addActionListener(e -> mostrarOpcionesNuevo());

        add(botonNuevo);

        add(Box.createVerticalStrut(25));

        // OPCIONES DEL MENÚ
        add(crearBotonMenu("▣", "Mis archivos", "MIS_ARCHIVOS"));
        add(crearBotonMenu("♧", "Compartido conmigo", "COMPARTIDO"));
        add(crearBotonMenu("▤", "Papelera", "PAPELERA"));

        // ESPACIO FLEXIBLE
        add(Box.createVerticalGlue());

        // SEPARADOR
        JSeparator separador = new JSeparator();
        separador.setForeground(new Color(218, 220, 224));
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        add(separador);
        add(Box.createVerticalStrut(15));

        // CERRAR SESIÓN
        JButton cerrarSesion = crearBotonMenu("↪", "Cerrar sesión", null);

        cerrarSesion.addActionListener(e -> alCerrarSesion.run());

        add(cerrarSesion);
    }

    private JButton crearBotonMenu(String icono,
                                   String texto,
                                   String seccion) {

        JButton boton = new JButton(icono + "   " + texto);

        boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        boton.setForeground(TEXTO);
        boton.setBackground(Color.WHITE);

        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        boton.setPreferredSize(new Dimension(205, 45));

        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 15, 10, 10));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(AZUL_CLARO);
                boton.setForeground(AZUL);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(TEXTO);
            }
        });

        if (seccion != null) {
            boton.addActionListener(e ->
                    alCambiarSeccion.accept(seccion));
        }

        return boton;
    }

    private void mostrarOpcionesNuevo() {

        String[] opciones = {
                "Subir archivo",
                "Crear carpeta"
        };

        int seleccion = JOptionPane.showOptionDialog(
                this,
                "¿Qué quieres hacer?",
                "Nuevo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (seleccion == 0) {

            JFileChooser selector = new JFileChooser();

            selector.showOpenDialog(this);

        } else if (seleccion == 1) {

            String nombre = JOptionPane.showInputDialog(
                    this,
                    "Nombre de la carpeta:"
            );

            if (nombre != null && !nombre.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Carpeta creada visualmente: " + nombre
                );
            }
        }
    }
}