package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Topbar extends JPanel {

    private final Color TEXTO = new Color(32, 33, 36);
    private final Color GRIS = new Color(95, 99, 104);

    private JLabel titulo;
    private JTextField buscador;

    public Topbar() {

        setLayout(new BorderLayout(20, 0));
        setBackground(Color.WHITE);

        setBorder(new EmptyBorder(15, 30, 15, 30));
        setPreferredSize(new Dimension(0, 75));

        crearContenido();
    }

    private void crearContenido() {

        // TÍTULO DE LA SECCIÓN
        titulo = new JLabel("Mis archivos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 21));
        titulo.setForeground(TEXTO);

        add(titulo, BorderLayout.WEST);

        // BUSCADOR
        JPanel panelBuscador = new JPanel(new BorderLayout());
        panelBuscador.setBackground(new Color(248, 249, 250));
        panelBuscador.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(218, 220, 224)
                ),
                new EmptyBorder(5, 12, 5, 12)
        ));

        buscador = new JTextField();
        buscador.setFont(new Font("SansSerif", Font.PLAIN, 14));
        buscador.setBorder(null);
        buscador.setOpaque(false);

        buscador.setToolTipText("Buscar archivos...");

        JLabel iconoBuscar = new JLabel("⌕ ");
        iconoBuscar.setFont(new Font("SansSerif", Font.PLAIN, 23));
        iconoBuscar.setForeground(GRIS);

        panelBuscador.add(iconoBuscar, BorderLayout.WEST);
        panelBuscador.add(buscador, BorderLayout.CENTER);

        panelBuscador.setPreferredSize(new Dimension(350, 40));

        add(panelBuscador, BorderLayout.CENTER);

        // USUARIO
        JPanel panelUsuario = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 8, 0
        ));

        panelUsuario.setBackground(Color.WHITE);

        JLabel avatar = new JLabel("U");
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(35, 35));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(232, 240, 254));
        avatar.setForeground(new Color(26, 115, 232));
        avatar.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel nombreUsuario = new JLabel("Usuario");
        nombreUsuario.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nombreUsuario.setForeground(TEXTO);

        panelUsuario.add(avatar);
        panelUsuario.add(nombreUsuario);

        add(panelUsuario, BorderLayout.EAST);
    }

    public void cambiarTitulo(String nuevoTitulo) {
        titulo.setText(nuevoTitulo);
    }

    public String getTextoBusqueda() {
        return buscador.getText();
    }
}