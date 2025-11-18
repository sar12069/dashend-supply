import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ControlRiegoGUI - simulador con ventana (Swing)
 */
public class ControlRiegoGUI extends JFrame {

    static class Zona {
        String nombre;
        double humedad;
        double humedadMin;
        double humedadObjetivo;
        boolean valvulaAbierta;

        public Zona(String nombre, double humedadInicial, double min, double objetivo) {
            this.nombre = nombre;
            this.humedad = humedadInicial;
            this.humedadMin = min;
            this.humedadObjetivo = objetivo;
            this.valvulaAbierta = false;
        }
    }

    // Lista de zonas (no static para facil manejo con la instancia)
    private final List<Zona> zonas = new ArrayList<>();

    // Componentes gráficos
    private final JTextArea logArea = new JTextArea(8, 40);
    private final JPanel panelZonas = new JPanel();

    public ControlRiegoGUI() {
        super("🌿 Sistema de Control de Riego Automático");

        setLayout(new BorderLayout(10, 10));
        panelZonas.setLayout(new GridLayout(0, 1, 5, 5));
        add(new JScrollPane(panelZonas), BorderLayout.CENTER);

        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar ventana
        setVisible(true);

        // Iniciar simulación con javax.swing.Timer (hilo del evento EDT)
        Timer timer = new Timer(2000, (ActionEvent e) -> {
            actualizarSensores();
            controlarRiego();
            actualizarGUI();
        });
        timer.start();
    }

    // Actualiza la interfaz mostrando las zonas
    private void actualizarGUI() {
        panelZonas.removeAll();

        for (Zona z : zonas) {
            JPanel p = new JPanel(new GridLayout(3, 1));
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)
            ));

            JLabel lNombre = new JLabel("Zona: " + z.nombre);
            JLabel lHumedad = new JLabel("Humedad: " + (int) (z.humedad * 100) + "%");
            JLabel lValvula = new JLabel("Válvula: " + (z.valvulaAbierta ? "ABIERTA 🟢" : "CERRADA 🔴"));

            p.add(lNombre);
            p.add(lHumedad);
            p.add(lValvula);

            panelZonas.add(p);
        }

        panelZonas.revalidate();
        panelZonas.repaint();
    }

    // Agrega una línea al log en la zona inferior
    private void log(String msg) {
        String hora = LocalTime.now().withNano(0).toString();
        logArea.append("[" + hora + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // Simulación de cambio de humedad
    private void actualizarSensores() {
        Random rand = new Random();
        for (Zona z : zonas) {
            if (!z.valvulaAbierta) {
                z.humedad -= 0.01 + rand.nextDouble() * 0.01;
            } else {
                z.humedad += 0.02 + rand.nextDouble() * 0.02;
            }

            if (z.humedad < 0.05) z.humedad = 0.05;
            if (z.humedad > 1.0) z.humedad = 1.0;
        }
    }

    // Control automático de válvulas (y escribir eventos en el log)
    private void controlarRiego() {
        for (Zona z : zonas) {
            if (z.humedad < z.humedadMin && !z.valvulaAbierta) {
                z.valvulaAbierta = true;
                log("🚰 Abriendo válvula en " + z.nombre);
            } else if (z.humedad >= z.humedadObjetivo && z.valvulaAbierta) {
                z.valvulaAbierta = false;
                log("✅ Cerrando válvula en " + z.nombre);
            }
        }
    }

    public static void main(String[] args) {
        // Inicializar zonas y lanzar GUI en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            ControlRiegoGUI app = new ControlRiegoGUI();
            app.zonas.add(new Zona("Ornamentales", 0.35, 0.25, 0.45));
            app.zonas.add(new Zona("Tomates", 0.40, 0.30, 0.50));
            app.zonas.add(new Zona("Aromáticas", 0.28, 0.22, 0.40));
            app.actualizarGUI();
        });
    }
}
