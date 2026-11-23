package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import ui.components.Sidebar;
import ui.components.Topbar;

public class MainView extends JPanel {

    private CardLayout cartas;
    private JPanel contenido;

    private Topbar barraSuperior;

    private final Color FONDO = new Color(248, 249, 250);

    public MainView(Runnable alCerrarSesion) {

        setLayout(new BorderLayout());
        setBackground(FONDO);

        // BARRA SUPERIOR
        barraSuperior = new Topbar();

        add(barraSuperior, BorderLayout.NORTH);

        // MENÚ LATERAL
        Sidebar menu = new Sidebar(
                this::cambiarSeccion,
                alCerrarSesion
        );

        add(menu, BorderLayout.WEST);

        // CONTENIDO CENTRAL
        cartas = new CardLayout();

        contenido = new JPanel(cartas);
        contenido.setBackground(FONDO);

        contenido.add(
                crearPanelTemporal("Mis archivos"),
                "MIS_ARCHIVOS"
        );

        contenido.add(
                crearPanelTemporal("Compartido conmigo"),
                "COMPARTIDO"
        );

        contenido.add(
                crearPanelTemporal("Papelera"),
                "PAPELERA"
        );

        add(contenido, BorderLayout.CENTER);

        // SECCIÓN INICIAL
        cambiarSeccion("MIS_ARCHIVOS");
    }

    private void cambiarSeccion(String seccion) {

        cartas.show(contenido, seccion);

        switch (seccion) {

            case "MIS_ARCHIVOS":
                barraSuperior.cambiarTitulo("Mis archivos");
                break;

            case "COMPARTIDO":
                barraSuperior.cambiarTitulo("Compartido conmigo");
                break;

            case "PAPELERA":
                barraSuperior.cambiarTitulo("Papelera");
                break;
        }
    }

    private JPanel crearPanelTemporal(String titulo) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel mensaje = new JLabel(titulo);

        mensaje.setFont(new Font("SansSerif", Font.PLAIN, 16));
        mensaje.setForeground(new Color(95, 99, 104));

        panel.add(mensaje, BorderLayout.NORTH);

        return panel;
    }
}