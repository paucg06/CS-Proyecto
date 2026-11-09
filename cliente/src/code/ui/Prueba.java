package ui;

import java.awt.CardLayout;
import javax.swing.*;

public class Prueba {
    public static void main(String[] args) {
        JFrame ventana = new JFrame("Prueba");

        CardLayout cartas = new CardLayout();
        JPanel contenedor = new JPanel(cartas);

        contenedor.add(new LoginView(
                () -> cartas.show(contenedor, "REGISTRO"),
                () -> cartas.show(contenedor, "MAIN")
        ), "LOGIN");

        contenedor.add(new RegistroView(
                () -> cartas.show(contenedor, "LOGIN")
        ), "REGISTRO");

        contenedor.add(new MainView(
                () -> cartas.show(contenedor, "LOGIN")
        ), "MAIN");

        ventana.add(contenedor);
        ventana.setSize(700, 450);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);
    }
}