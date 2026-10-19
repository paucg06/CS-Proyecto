//Librerias
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

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

        //Zona de texto (centro): muestra los resultados y acepta arrastrar y soltar archivos
        JTextArea zonaTexto = new JTextArea("Arrastra y suelta un archivo aquí o pulsa 'Subir Archivo'...");
        zonaTexto.setEditable(false); //El usuario no puede escribir en ella
        JScrollPane scroll = new JScrollPane(zonaTexto);

        // Configurar funcionalidad Drag and Drop en la zona de texto
        configurarDragAndDrop(zonaTexto);

        //Botones: Subir y Bajar archivos
        JButton botonSubir = new JButton("Subir Archivo");
        JButton botonBajar = new JButton("Bajar Archivo");

        //Que hace cada boton al pulsarlo
        botonSubir.addActionListener(e -> abrirExploradorYSubir(ventana, zonaTexto));
        botonBajar.addActionListener(e -> descifrarValoresAES(zonaTexto));

        //Panel con los botones dispuestos horizontalmente
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10)); //Margen alrededor
        panelBotones.add(botonSubir);
        panelBotones.add(botonBajar);

        //Panel superior que agrupa titulo y botones
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(titulo, BorderLayout.NORTH);
        panelNorte.add(panelBotones, BorderLayout.CENTER);

        //Colocar todo en la ventana
        ventana.setLayout(new BorderLayout());
        ventana.add(panelNorte, BorderLayout.NORTH); // Titulo y botones arriba
        ventana.add(scroll, BorderLayout.CENTER);     // Zona de texto en el centro

        ventana.setVisible(true);
    }

    // Abre el explorador del sistema (JFileChooser) para seleccionar y subir un archivo
    private void abrirExploradorYSubir(JFrame ventana, JTextArea zonaTexto)
    {
        JFileChooser selectorArchivos = new JFileChooser();
        int resultado = selectorArchivos.showOpenDialog(ventana);

        if (resultado == JFileChooser.APPROVE_OPTION)
        {
            File archivoSeleccionado = selectorArchivos.getSelectedFile();
            Main.CifrarValoresAES(0); //Reutilizamos el metodo de Main
            zonaTexto.setText("Archivo seleccionado: " + archivoSeleccionado.getAbsolutePath() + "\n" +
                              "Estado de cifrado/subida: Archivo " + Main.testImgEnc + " procesado con éxito.");
        }
    }

    // Permite arrastrar y soltar archivos directamente sobre la zona de texto
    private void configurarDragAndDrop(JTextArea zonaTexto)
    {
        zonaTexto.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    // Corrección: llamada a getTransferable() antes de getTransferData()
                    List<File> archivos = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!archivos.isEmpty()) {
                        File archivoArrastrado = archivos.get(0);
                        Main.CifrarValoresAES(0); //Reutilizamos el metodo de Main
                        zonaTexto.setText("Archivo arrastrado: " + archivoArrastrado.getAbsolutePath() + "\n" +
                                          "Estado de cifrado/subida: Archivo " + Main.testImgEnc + " procesado con éxito.");
                        return true;
                    }
                } catch (Exception e) {
                    zonaTexto.setText("Error al procesar el archivo arrastrado: " + e.getMessage());
                }
                return false;
            }
        });
    }

    // Descifra y simula la descarga del archivo
    private void descifrarValoresAES(JTextArea zonaTexto)
    {
        zonaTexto.setText("Archivo descifrado estado: " + Boolean.toString(Main.DescifrarValoresAES(0)));
    }
}