package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ui.components.Sidebar;
import ui.components.Topbar;

public class MainView extends JPanel {

    private CardLayout cartas;
    private JPanel contenido;
    private Topbar barraSuperior;

    private final Color FONDO = new Color(248, 249, 250);
    private final Color TEXTO = new Color(32, 33, 36);
    private final Color GRIS = new Color(95, 99, 104);
    private final Color BORDE = new Color(218, 220, 224);

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

        contenido.add(crearPanelMisArchivos(), "MIS_ARCHIVOS");
        contenido.add(crearPanelTemporal("Compartido conmigo"), "COMPARTIDO");
        contenido.add(crearPanelTemporal("Papelera"), "PAPELERA");

        add(contenido, BorderLayout.CENTER);

        cambiarSeccion("MIS_ARCHIVOS");
    }

    // CAMBIAR SECCIÓN
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

    // PANEL PRINCIPAL DE ARCHIVOS
    private JPanel crearPanelMisArchivos() {

        JPanel panel = new JPanel(new BorderLayout(0, 25));
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(30, 35, 30, 35));

        JPanel zonaSuperior = new JPanel(new BorderLayout());
        zonaSuperior.setBackground(FONDO);

        JLabel titulo = new JLabel("Mi unidad");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        titulo.setForeground(TEXTO);

        JLabel descripcion = new JLabel("Gestiona todos tus archivos y carpetas");
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descripcion.setForeground(GRIS);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(FONDO);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(descripcion);

        // BOTÓN ORDENAR
        JButton ordenar = new JButton("Ordenar por nombre");
        ordenar.setBackground(Color.WHITE);
        ordenar.setForeground(GRIS);
        ordenar.setFocusPainted(false);
        ordenar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ordenar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(8, 12, 8, 12)
        ));

        zonaSuperior.add(textos, BorderLayout.WEST);
        zonaSuperior.add(ordenar, BorderLayout.EAST);

        // CONTENIDO DESPLAZABLE
        JPanel contenidoArchivos = new JPanel();
        contenidoArchivos.setLayout(new BoxLayout(
                contenidoArchivos,
                BoxLayout.Y_AXIS
        ));
        contenidoArchivos.setBackground(FONDO);

        // CARPETAS
        JLabel tituloCarpetas = crearTituloApartado("Carpetas");

        contenidoArchivos.add(tituloCarpetas);
        contenidoArchivos.add(Box.createVerticalStrut(15));

        JPanel gridCarpetas = new JPanel(new GridLayout(0, 3, 15, 15));
        gridCarpetas.setBackground(FONDO);

        gridCarpetas.add(crearTarjetaCarpeta("Documentos", "12 elementos"));
        gridCarpetas.add(crearTarjetaCarpeta("Imágenes", "28 elementos"));
        gridCarpetas.add(crearTarjetaCarpeta("Proyectos", "5 elementos"));
        gridCarpetas.add(crearTarjetaCarpeta("Universidad", "16 elementos"));

        contenidoArchivos.add(gridCarpetas);
        contenidoArchivos.add(Box.createVerticalStrut(35));

        // ARCHIVOS
        JLabel tituloArchivos = crearTituloApartado("Archivos recientes");

        contenidoArchivos.add(tituloArchivos);
        contenidoArchivos.add(Box.createVerticalStrut(15));

        contenidoArchivos.add(crearListaArchivos());

        JScrollPane scroll = new JScrollPane(contenidoArchivos);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(zonaSuperior, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // TÍTULOS DE APARTADOS
    private JLabel crearTituloApartado(String texto) {

        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(TEXTO);

        return titulo;
    }

    // TARJETAS DE CARPETAS
    private JPanel crearTarjetaCarpeta(String nombre, String elementos) {

        JPanel tarjeta = new JPanel(new BorderLayout(10, 5));
        tarjeta.setBackground(Color.WHITE);

        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel icono = new JLabel("📁");
        icono.setFont(new Font("SansSerif", Font.PLAIN, 27));

        JLabel nombreCarpeta = new JLabel(nombre);
        nombreCarpeta.setFont(new Font("SansSerif", Font.BOLD, 13));
        nombreCarpeta.setForeground(TEXTO);

        JLabel cantidad = new JLabel(elementos);
        cantidad.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cantidad.setForeground(GRIS);

        JPanel informacion = new JPanel();
        informacion.setLayout(new BoxLayout(informacion, BoxLayout.Y_AXIS));
        informacion.setBackground(Color.WHITE);

        informacion.add(nombreCarpeta);
        informacion.add(Box.createVerticalStrut(5));
        informacion.add(cantidad);

        tarjeta.add(icono, BorderLayout.WEST);
        tarjeta.add(informacion, BorderLayout.CENTER);

        tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tarjeta.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(new Color(245, 248, 252));
                informacion.setBackground(new Color(245, 248, 252));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
                informacion.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Abrir carpeta: " + nombre);
            }
        });

        return tarjeta;
    }

    // LISTA DE ARCHIVOS
    private JPanel crearListaArchivos() {

        JPanel lista = new JPanel(new GridLayout(0, 1, 0, 0));
        lista.setBackground(Color.WHITE);

        lista.setBorder(BorderFactory.createLineBorder(BORDE));

        // CABECERA
        JPanel cabecera = crearFilaArchivo(
                "Nombre", "Propietario", "Modificado", "Tamaño", true
        );

        lista.add(cabecera);

        // ARCHIVOS DE EJEMPLO
        lista.add(crearFilaArchivo(
                "📄  Trabajo.pdf", "Yo", "Hoy", "2,4 MB", false
        ));

        lista.add(crearFilaArchivo(
                "🖼  Fotografía.png", "Yo", "Ayer", "1,8 MB", false
        ));

        lista.add(crearFilaArchivo(
                "📊  Presentación.pptx", "Yo", "12 nov 2026", "4,2 MB", false
        ));

        lista.add(crearFilaArchivo(
                "📝  Apuntes.docx", "Yo", "10 nov 2026", "850 KB", false
        ));

        return lista;
    }

    // FILAS DE ARCHIVOS
    private JPanel crearFilaArchivo(
            String nombre,
            String propietario,
            String fecha,
            String tamano,
            boolean cabecera
    ) {

        JPanel fila = new JPanel(new GridLayout(1, 4));
        fila.setBackground(cabecera
                ? new Color(248, 249, 250)
                : Color.WHITE);

        fila.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, BORDE
        ));

        JLabel archivo = new JLabel(nombre);
        JLabel dueno = new JLabel(propietario);
        JLabel modificado = new JLabel(fecha);
        JLabel peso = new JLabel(tamano);

        JLabel[] etiquetas = {
                archivo, dueno, modificado, peso
        };

        for (JLabel etiqueta : etiquetas) {

            etiqueta.setFont(new Font(
                    "SansSerif",
                    cabecera ? Font.BOLD : Font.PLAIN,
                    12
            ));

            etiqueta.setForeground(cabecera ? GRIS : TEXTO);

            etiqueta.setBorder(new EmptyBorder(13, 12, 13, 5));

            fila.add(etiqueta);
        }

        if (!cabecera) {

            fila.setCursor(new Cursor(Cursor.HAND_CURSOR));

            fila.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    if (SwingUtilities.isRightMouseButton(e)) {

                        JPopupMenu menu = new JPopupMenu();

                        JMenuItem abrir = new JMenuItem("Abrir");
                        JMenuItem descargar = new JMenuItem("Descargar");
                        JMenuItem compartir = new JMenuItem("Compartir");
                        JMenuItem eliminar = new JMenuItem("Eliminar");

                        abrir.addActionListener(ev ->
                                System.out.println("Abrir: " + nombre));

                        descargar.addActionListener(ev ->
                                System.out.println("Descargar: " + nombre));

                        compartir.addActionListener(ev ->
                                System.out.println("Compartir: " + nombre));

                        eliminar.addActionListener(ev ->
                                System.out.println("Eliminar: " + nombre));

                        menu.add(abrir);
                        menu.add(descargar);
                        menu.add(compartir);
                        menu.addSeparator();
                        menu.add(eliminar);

                        menu.show(fila, e.getX(), e.getY());
                    }
                }
            });
        }

        fila.setPreferredSize(new Dimension(0, 45));

        return fila;
    }

    // PANELES TEMPORALES
    private JPanel crearPanelTemporal(String titulo) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel mensaje = new JLabel(titulo);
        mensaje.setFont(new Font("SansSerif", Font.PLAIN, 16));
        mensaje.setForeground(GRIS);

        panel.add(mensaje, BorderLayout.NORTH);

        return panel;
    }
}