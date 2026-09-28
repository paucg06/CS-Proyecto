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
        ventana.setSize(600, 400);
        ventana.setLocationRelativeTo(null); //Centrar en pantalla
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //Cierra solo la ventana, no el programa

        //Titulo (parte de arriba)
        JLabel titulo = new JLabel("Archivos disponibles", SwingConstants.CENTER);

        //Lista de archivos (centro)
        DefaultListModel<String> modeloLista = new DefaultListModel<>(); //Aqui se guardan los textos de la lista
        JList<String> lista = new JList<>(modeloLista);
        JScrollPane scroll = new JScrollPane(lista); //Barra de desplazamiento

        //Boton (parte de abajo)
        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> actualizarLista(modeloLista)); //Al pulsar, recarga la lista

        JPanel panelBotones = new JPanel();
        panelBotones.add(botonActualizar);

        //Colocar todo en la ventana
        ventana.setLayout(new BorderLayout());
        ventana.add(titulo, BorderLayout.NORTH);
        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);

        actualizarLista(modeloLista); //Cargar la lista nada mas abrir
        ventana.setVisible(true);
    }

    //Vuelve a leer los archivos y rellena la lista
    private void actualizarLista(DefaultListModel<String> modeloLista)
    {
        Main.cargarArchivos(); //Reutilizamos el metodo de Main
        modeloLista.clear();

        for (int i = 0; i < Main.matrizArchivos.length; i++)
        {
            String nombre = (String) Main.matrizArchivos[i][0];
            byte[] contenido = (byte[]) Main.matrizArchivos[i][1];

            modeloLista.addElement(nombre + " (" + contenido.length + " bytes)");
        }
    }
}