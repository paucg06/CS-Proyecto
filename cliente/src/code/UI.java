//Librerias
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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

        //Antes de mostrar nada, el usuario debe iniciar sesion (o registrarse). Si cancela, se cierra el programa
        if (!iniciarSesion(ventana))
        {
            System.exit(0);
        }

        //Titulo (parte de arriba)
        JLabel titulo = new JLabel("Servicio seguro de contenidos multimedia", SwingConstants.CENTER);

        //Zona de texto (centro): muestra los resultados y acepta arrastrar y soltar archivos
        JTextArea zonaTexto = new JTextArea("Arrastra y suelta archivos aquí o pulsa 'Cifrar'...");
        zonaTexto.setEditable(false); //El usuario no puede escribir en ella
        JScrollPane scroll = new JScrollPane(zonaTexto);

        // Configurar funcionalidad Drag and Drop en la zona de texto
        configurarDragAndDrop(zonaTexto);

        //Botones: Subir y Bajar archivos
        JButton botonSubir = new JButton("Cifrar");
        JButton botonBajar = new JButton("Descifrar");

        //Que hace cada boton al pulsarlo
        botonSubir.addActionListener(e -> abrirExploradorYSubir(ventana, zonaTexto));
        botonBajar.addActionListener(e -> descifrarYGuardar(ventana, zonaTexto));

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

    // Fase I: un unico usuario. Solo se pide la contrasenya (la primera vez sirve para crearla)
    private boolean iniciarSesion(JFrame ventana)
    {
        boolean primeraVez = !Main.ExisteUsuarioUnico();
        String mensaje = primeraVez
                ? "Primer uso: crea la contraseña que protegerá tu clave privada RSA."
                : "Introduce tu contraseña:";

        while (true)
        {
            JPasswordField campoPassword = new JPasswordField(15);
            int eleccion = JOptionPane.showConfirmDialog(ventana, new Object[]{mensaje, campoPassword},
                    "Acceso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (eleccion != JOptionPane.OK_OPTION)
            {
                return false; //Cancelar o cerrar el dialogo
            }

            String password = new String(campoPassword.getPassword());
            if (password.isEmpty())
            {
                JOptionPane.showMessageDialog(ventana, "La contraseña no puede estar vacía.", "Aviso", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            if (Main.AccederUsuarioUnico(password))
            {
                return true;
            }
            JOptionPane.showMessageDialog(ventana, "Contraseña incorrecta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Abre el explorador del sistema (JFileChooser) para seleccionar y subir uno o varios archivos
    private void abrirExploradorYSubir(JFrame ventana, JTextArea zonaTexto)
    {
        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setMultiSelectionEnabled(true);
        int resultado = selectorArchivos.showOpenDialog(ventana);

        if (resultado == JFileChooser.APPROVE_OPTION)
        {
            cifrarArchivos(List.of(selectorArchivos.getSelectedFiles()), zonaTexto);
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
                    List<File> archivos = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!archivos.isEmpty()) {
                        cifrarArchivos(archivos, zonaTexto);
                        return true;
                    }
                } catch (Exception e) {
                    zonaTexto.setText("Error al procesar el archivo arrastrado: " + e.getMessage());
                }
                return false;
            }
        });
    }

    // Cifra y guarda en la base de datos cada archivo de la lista (comun al explorador y al drag and drop)
    private void cifrarArchivos(List<File> archivos, JTextArea zonaTexto)
    {
        zonaTexto.setText("Cifrando " + archivos.size() + " archivo(s)...\n");

        //En segundo plano para no congelar la ventana con archivos grandes
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder informe = new StringBuilder();
                for (File archivo : archivos)
                {
                    informe.append(archivo.getName()).append(": ");
                    try
                    {
                        if (!archivo.isFile())
                        {
                            informe.append("ERROR, no es un archivo (las carpetas no se pueden subir).\n");
                            continue;
                        }
                        byte[] contenido = Files.readAllBytes(archivo.toPath());
                        if (Main.GuardarMiArchivo(contenido, archivo.getName()))
                        {
                            informe.append("cifrado con AES-128 y guardado en la carpeta '" + Main.DIREC_CIFRADOS + "' (archivo .enc).\n");
                        }
                        else
                        {
                            informe.append("NO guardado: ya existe un archivo con ese nombre (o el nombre contiene ';').\n");
                        }
                    }
                    catch (Exception e)
                    {
                        informe.append("ERROR, ").append(e.getMessage()).append("\n");
                    }
                }
                return informe.toString();
            }

            @Override
            protected void done() {
                try {
                    zonaTexto.setText(get());
                } catch (Exception e) {
                    zonaTexto.setText("Error inesperado: " + e.getMessage());
                }
            }
        }.execute();
    }

    // Muestra los archivos cifrados del usuario, descifra los elegidos y los guarda en la carpeta local
    private void descifrarYGuardar(JFrame ventana, JTextArea zonaTexto)
    {
        String[] nombres = Main.ListarMisArchivos();
        if (nombres.length == 0)
        {
            zonaTexto.setText("No tienes archivos cifrados todavía. Cifra alguno primero.");
            return;
        }

        JList<String> lista = new JList<>(nombres);
        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane panelLista = new JScrollPane(lista);
        panelLista.setPreferredSize(new Dimension(350, 200));

        int resultado = JOptionPane.showConfirmDialog(ventana, panelLista,
                "Selecciona los archivos a descifrar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION || lista.isSelectionEmpty())
        {
            return;
        }

        List<String> elegidos = lista.getSelectedValuesList();
        zonaTexto.setText("Descifrando " + elegidos.size() + " archivo(s)...\n");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder informe = new StringBuilder();
                for (String nombre : elegidos)
                {
                    informe.append(nombre).append(": ");
                    try
                    {
                        Path destino = Main.GuardarArchivoDescifrado(nombre);
                        informe.append("descifrado y guardado en ").append(destino.toAbsolutePath()).append("\n");
                        abrirArchivo(destino, informe);
                    }
                    catch (Exception e)
                    {
                        informe.append("ERROR al descifrar, ").append(e.getMessage()).append("\n");
                    }
                }
                return informe.toString();
            }

            @Override
            protected void done() {
                try {
                    zonaTexto.setText(get());
                } catch (Exception e) {
                    zonaTexto.setText("Error inesperado: " + e.getMessage());
                }
            }
        }.execute();
    }

    // Abre el archivo con el programa predeterminado del sistema (si falla, solo se avisa en el informe)
    private void abrirArchivo(Path ruta, StringBuilder informe)
    {
        try
        {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
            {
                Desktop.getDesktop().open(ruta.toFile());
                informe.append("   (abierto automáticamente)\n");
            }
            else
            {
                informe.append("   (no se pudo abrir automáticamente en este sistema)\n");
            }
        }
        catch (Exception e)
        {
            informe.append("   (no se pudo abrir automáticamente: ").append(e.getMessage()).append(")\n");
        }
    }
}
