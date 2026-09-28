//Librerias
import javax.swing.*;
import java.awt.*;

public class UI
{
    public void HelloUI()
    {
        System.out.println("\n¡Hola, soy UI.\n");
    }

    //Abre la ventana principal de la aplicacion
    public void AbrirVentana()
    {
        //Ventana
        JFrame ventana = new JFrame("Servicio seguro de contenidos multimedia");
        ventana.setSize(700, 400);
        ventana.setLocationRelativeTo(null); //Centrar en pantalla
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Al cerrar la ventana termina el programa

        //Titulo (parte de arriba)
        JLabel titulo = new JLabel("Servicio seguro de contenidos multimedia", SwingConstants.CENTER);

        //Zona de texto (centro): aqui se muestran los resultados, sustituye a la consola
        JTextArea zonaTexto = new JTextArea();
        zonaTexto.setEditable(false); //El usuario no puede escribir en ella
        JScrollPane scroll = new JScrollPane(zonaTexto);

        //Botones (las mismas opciones del menu anterior)
        JButton botonActualizar = new JButton("Actualizar");
        JButton botonMostrar = new JButton("Mostrar Archivos");
        JButton botonAes = new JButton("AES");
        JButton botonDb = new JButton("DB");
        JButton botonSalir = new JButton("Salir");

        //Que hace cada boton al pulsarlo
        botonActualizar.addActionListener(e -> opcionActualizar(zonaTexto));
        botonMostrar.addActionListener(e -> opcionMostrarArchivos(zonaTexto));
        botonAes.addActionListener(e -> zonaTexto.setText("¡Hola, soy AES."));
        botonDb.addActionListener(e -> zonaTexto.setText("¡Hola, soy DbManager."));
        botonSalir.addActionListener(e -> System.exit(0));

        //Panel con los botones uno debajo de otro (izquierda)
        JPanel panelBotones = new JPanel(new GridLayout(0, 1, 5, 5)); //Cualquier numero de filas, 1 columna
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //Margen alrededor
        panelBotones.add(botonActualizar);
        panelBotones.add(botonMostrar);
        panelBotones.add(botonAes);
        panelBotones.add(botonDb);
        panelBotones.add(botonSalir);

        //Colocar todo en la ventana
        ventana.setLayout(new BorderLayout());
        ventana.add(titulo, BorderLayout.NORTH);
        ventana.add(panelBotones, BorderLayout.WEST);
        ventana.add(scroll, BorderLayout.CENTER);

        ventana.setVisible(true);
    }

    //Opcion 1: vuelve a leer los archivos de la carpeta
    private void opcionActualizar(JTextArea zonaTexto)
    {
        Main.cargarArchivos(); //Reutilizamos el metodo de Main
        zonaTexto.setText("Archivos actualizados: " + Main.matrizArchivos.length);
    }

    //Opcion 2: actualiza y muestra el nombre y tamanyo de cada archivo
    private void opcionMostrarArchivos(JTextArea zonaTexto)
    {
        Main.cargarArchivos();
        zonaTexto.setText(""); //Borrar lo que hubiera

        for (int i = 0; i < Main.matrizArchivos.length; i++)
        {
            String nombre = (String) Main.matrizArchivos[i][0];
            byte[] contenido = (byte[]) Main.matrizArchivos[i][1];

            zonaTexto.append(nombre + "\n");
            zonaTexto.append(contenido.length + " bytes\n");
        }
    }
}