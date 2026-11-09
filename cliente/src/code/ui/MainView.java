package ui;

import javax.swing.*;
import java.awt.*;

public class MainView extends JPanel {

    public MainView(Runnable alCerrarSesion) {
        setLayout(new BorderLayout());

        // --- Contenido central (varias secciones) ---
        CardLayout cartas = new CardLayout();
        JPanel contenido = new JPanel(cartas);
        contenido.add(new JLabel("Aquí irán Mis Archivos"), "MIS_ARCHIVOS");
        contenido.add(new JLabel("Aquí irá Compartido conmigo"), "COMPARTIDO");
        contenido.add(new JLabel("Aquí irá la Papelera"), "PAPELERA");

        // --- Menú lateral ---
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(180, 0));

        JButton botonMisArchivos = new JButton("Mis Archivos");
        JButton botonCompartido = new JButton("Compartido conmigo");
        JButton botonPapelera = new JButton("Papelera");
        JButton botonCerrarSesion = new JButton("Cerrar sesión");

        botonMisArchivos.addActionListener(e -> cartas.show(contenido, "MIS_ARCHIVOS"));
        botonCompartido.addActionListener(e -> cartas.show(contenido, "COMPARTIDO"));
        botonPapelera.addActionListener(e -> cartas.show(contenido, "PAPELERA"));
        botonCerrarSesion.addActionListener(e -> alCerrarSesion.run());

        menu.add(botonMisArchivos);
        menu.add(botonCompartido);
        menu.add(botonPapelera);
        menu.add(botonCerrarSesion);

        // --- Colocar todo en este panel ---
        add(menu, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
    }
}