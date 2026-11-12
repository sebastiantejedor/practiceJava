package VentanasSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Clock extends JFrame {

    // Paleta de Colores
    private static final Color COLOR_FONDO = new Color(0, 0, 0);
    private static final Color COLOR_TARJETA = new Color(28, 28, 30);
    private static final Color COLOR_SEPARADOR = new Color(44, 44, 46);
    private static final Color COLOR_NARANJA = new Color(255, 159, 10);
    private static final Color COLOR_VERDE_TEXTO = new Color(48, 209, 88);
    private static final Color COLOR_VERDE_FONDO = new Color(10, 42, 20);
    private static final Color COLOR_ROJO_TEXTO = new Color(255, 69, 58);
    private static final Color COLOR_ROJO_FONDO = new Color(52, 16, 14);
    private static final Color COLOR_GRIS_BOTON = new Color(51, 51, 51);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(142, 142, 147);

    private CardLayout cardLayout;
    private JPanel contenedorVistas;
    private final List<BotonTab> botonesTab = new ArrayList<>();

    public Clock() {
        configurarVentana();
        inicializarUI();
    }

    private void configurarVentana() {
        setTitle("Reloj");
        setSize(380, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }

    private void inicializarUI() {
        cardLayout = new CardLayout();
        contenedorVistas = new JPanel(cardLayout);
        contenedorVistas.setBackground(COLOR_FONDO);

        contenedorVistas.add(new PanelReloj(), "RELOJ");
        contenedorVistas.add(new PanelCronometro(), "CRONOMETRO");
        contenedorVistas.add(new PanelTemporizador(), "TEMPORIZADOR");

        add(contenedorVistas, BorderLayout.CENTER);
        add(crearBarraNavegacion(), BorderLayout.SOUTH);
    }

    private JPanel crearBarraNavegacion() {
        JPanel nav = new JPanel(new GridLayout(1, 3));
        nav.setBackground(new Color(18, 18, 20));
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SEPARADOR));
        nav.setPreferredSize(new Dimension(380, 68));

        BotonTab tabReloj = new BotonTab("Reloj", "RELOJ", true);
        BotonTab tabCrono = new BotonTab("Cronómetro", "CRONOMETRO", false);
        BotonTab tabTemp = new BotonTab("Temporizador", "TEMPORIZADOR", false);

        botonesTab.add(tabReloj);
        botonesTab.add(tabCrono);
        botonesTab.add(tabTemp);

        for (BotonTab tab : botonesTab) {
            nav.add(tab);
        }

        return nav;
    }

    private void cambiarVista(String nombreVista, BotonTab seleccionado) {
        cardLayout.show(contenedorVistas, nombreVista);
        for (BotonTab b : botonesTab) {
            b.setActivo(b == seleccionado);
        }
    }

    private static class PanelReloj extends JPanel {
        private final JLabel lblHoraDigital;
        private final JLabel lblFecha;
        private final EsferaReloj esfera;

        public PanelReloj() {
            setBackground(COLOR_FONDO);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(24, 24, 24, 24));

            JLabel titulo = new JLabel("Reloj");
            titulo.setForeground(Color.WHITE);
            titulo.setFont(new Font("SansSerif", Font.BOLD, 32));
            add(titulo, BorderLayout.NORTH);

            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            centro.setBackground(COLOR_FONDO);

            esfera = new EsferaReloj();
            esfera.setAlignmentX(Component.CENTER_ALIGNMENT);

            lblHoraDigital = new JLabel("00:00:00");
            lblHoraDigital.setForeground(Color.WHITE);
            lblHoraDigital.setFont(new Font("SansSerif", Font.PLAIN, 48));
            lblHoraDigital.setAlignmentX(Component.CENTER_ALIGNMENT);

            lblFecha = new JLabel("");
            lblFecha.setForeground(COLOR_TEXTO_SECUNDARIO);
            lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 16));
            lblFecha.setAlignmentX(Component.CENTER_ALIGNMENT);

            centro.add(Box.createVerticalGlue());
            centro.add(esfera);
            centro.add(Box.createVerticalStrut(28));
            centro.add(lblHoraDigital);
            centro.add(Box.createVerticalStrut(6));
            centro.add(lblFecha);
            centro.add(Box.createVerticalGlue());

            add(centro, BorderLayout.CENTER);

            Timer timer = new Timer(50, e -> actualizarHora());
            timer.start();
            actualizarHora();
        }

        private void actualizarHora() {
            LocalDateTime ahora = LocalDateTime.now();
            lblHoraDigital.setText(ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            String fechaTexto = ahora.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "ES")));
            lblFecha.setText(fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1));
            esfera.repaint();
        }
    }

    private static class EsferaReloj extends JPanel {
        public EsferaReloj() {
            setPreferredSize(new Dimension(250, 250));
            setMaximumSize(new Dimension(250, 250));
            setBackground(COLOR_FONDO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int radio = 115;

            g2.setColor(COLOR_TARJETA);
            g2.fillOval(cx - radio, cy - radio, radio * 2, radio * 2);

            for (int i = 0; i < 60; i++) {
                double angulo = Math.toRadians(i * 6 - 90);
                boolean esHora = (i % 5 == 0);
                int rExt = radio - 10;
                int rInt = esHora ? radio - 22 : radio - 15;

                g2.setColor(esHora ? Color.WHITE : COLOR_TEXTO_SECUNDARIO);
                g2.setStroke(new BasicStroke(esHora ? 2.5f : 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(
                        cx + Math.cos(angulo) * rInt,
                        cy + Math.sin(angulo) * rInt,
                        cx + Math.cos(angulo) * rExt,
                        cy + Math.sin(angulo) * rExt
                ));
            }

            LocalTime t = LocalTime.now();
            double seg = t.getSecond() + t.getNano() / 1_000_000_000.0;
            double min = t.getMinute() + seg / 60.0;
            double hor = (t.getHour() % 12) + min / 60.0;

            // Manecilla Hora
            dibujarManecilla(g2, cx, cy, Math.toRadians(hor * 30 - 90), radio * 0.52, 5.5f, Color.WHITE);
            // Manecilla Minuto
            dibujarManecilla(g2, cx, cy, Math.toRadians(min * 6 - 90), radio * 0.78, 3.8f, Color.WHITE);
            // Manecilla Segundo
            dibujarManecilla(g2, cx, cy, Math.toRadians(seg * 6 - 90), radio * 0.85, 2.0f, COLOR_NARANJA);

            g2.setColor(COLOR_NARANJA);
            g2.fillOval(cx - 5, cy - 5, 10, 10);
            g2.setColor(COLOR_FONDO);
            g2.fillOval(cx - 2, cy - 2, 4, 4);

            g2.dispose();
        }

        private void dibujarManecilla(Graphics2D g2, int cx, int cy, double angulo, double largo, float grosor, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(cx, cy, cx + Math.cos(angulo) * largo, cy + Math.sin(angulo) * largo));
        }
    }

    private static class PanelCronometro extends JPanel {
        private final JLabel lblTiempo;
        private final BotonCircularIOS btnIzquierdo;
        private final BotonCircularIOS btnDerecho;
        private final JPanel listaVueltasPanel;

        private final Timer timer;
        private boolean corriendo = false;
        private long tiempoAcumulado = 0;
        private long inicioMarca = 0;
        private long ultimaVueltaMarca = 0;
        private final List<Long> tiemposVueltas = new ArrayList<>();

        public PanelCronometro() {
            setBackground(COLOR_FONDO);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(24, 24, 12, 24));

            lblTiempo = new JLabel("00:00.00", SwingConstants.CENTER);
            lblTiempo.setForeground(Color.WHITE);
            lblTiempo.setFont(new Font("Monospaced", Font.PLAIN, 54));
            lblTiempo.setBorder(new EmptyBorder(45, 0, 35, 0));
            add(lblTiempo, BorderLayout.NORTH);

            JPanel centro = new JPanel(new BorderLayout(0, 20));
            centro.setBackground(COLOR_FONDO);

            JPanel panelBotones = new JPanel(new BorderLayout());
            panelBotones.setBackground(COLOR_FONDO);
            panelBotones.setBorder(new EmptyBorder(0, 12, 0, 12));

            btnIzquierdo = new BotonCircularIOS("Vuelta", COLOR_GRIS_BOTON, COLOR_TEXTO_SECUNDARIO);
            btnIzquierdo.setEnabled(false);
            btnDerecho = new BotonCircularIOS("Iniciar", COLOR_VERDE_FONDO, COLOR_VERDE_TEXTO);

            panelBotones.add(btnIzquierdo, BorderLayout.WEST);
            panelBotones.add(btnDerecho, BorderLayout.EAST);
            centro.add(panelBotones, BorderLayout.NORTH);

            listaVueltasPanel = new JPanel();
            listaVueltasPanel.setLayout(new BoxLayout(listaVueltasPanel, BoxLayout.Y_AXIS));
            listaVueltasPanel.setBackground(COLOR_FONDO);

            JScrollPane scroll = new JScrollPane(listaVueltasPanel);
            scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SEPARADOR));
            scroll.getViewport().setBackground(COLOR_FONDO);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
            centro.add(scroll, BorderLayout.CENTER);

            add(centro, BorderLayout.CENTER);

            timer = new Timer(16, e -> actualizarDisplay());

            btnDerecho.addActionListener(e -> alternarInicioPausa());
            btnIzquierdo.addActionListener(e -> registrarVueltaOReiniciar());
        }

        private void alternarInicioPausa() {
            if (!corriendo) {
                inicioMarca = System.currentTimeMillis();
                corriendo = true;
                timer.start();

                btnDerecho.setEstilos("Detener", COLOR_ROJO_FONDO, COLOR_ROJO_TEXTO);
                btnIzquierdo.setEstilos("Vuelta", COLOR_GRIS_BOTON, Color.WHITE);
                btnIzquierdo.setEnabled(true);
            } else {
                tiempoAcumulado += System.currentTimeMillis() - inicioMarca;
                corriendo = false;
                timer.stop();

                btnDerecho.setEstilos("Iniciar", COLOR_VERDE_FONDO, COLOR_VERDE_TEXTO);
                btnIzquierdo.setEstilos("Reiniciar", COLOR_GRIS_BOTON, Color.WHITE);
            }
        }

        private void registrarVueltaOReiniciar() {
            if (corriendo) {
                long totalActual = obtenerTiempoTotal();
                long duracionVuelta = totalActual - ultimaVueltaMarca;
                ultimaVueltaMarca = totalActual;
                tiemposVueltas.add(duracionVuelta);
                renderizarVueltas();
            } else {
                tiempoAcumulado = 0;
                ultimaVueltaMarca = 0;
                tiemposVueltas.clear();
                lblTiempo.setText("00:00.00");
                listaVueltasPanel.removeAll();
                listaVueltasPanel.revalidate();
                listaVueltasPanel.repaint();

                btnIzquierdo.setEstilos("Vuelta", COLOR_GRIS_BOTON, COLOR_TEXTO_SECUNDARIO);
                btnIzquierdo.setEnabled(false);
            }
        }

        private long obtenerTiempoTotal() {
            return corriendo ? tiempoAcumulado + (System.currentTimeMillis() - inicioMarca) : tiempoAcumulado;
        }

        private void actualizarDisplay() {
            lblTiempo.setText(formatearMillis(obtenerTiempoTotal()));
        }

        private void renderizarVueltas() {
            listaVueltasPanel.removeAll();
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;

            if (tiemposVueltas.size() >= 2) {
                for (long t : tiemposVueltas) {
                    if (t < min) min = t;
                    if (t > max) max = t;
                }
            }

            for (int i = tiemposVueltas.size() - 1; i >= 0; i--) {
                long duracion = tiemposVueltas.get(i);
                Color colorFila = Color.WHITE;
                if (tiemposVueltas.size() >= 2) {
                    if (duracion == min) colorFila = COLOR_VERDE_TEXTO;
                    else if (duracion == max) colorFila = COLOR_ROJO_TEXTO;
                }

                JPanel fila = new JPanel(new BorderLayout());
                fila.setBackground(COLOR_FONDO);
                fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                fila.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SEPARADOR),
                        new EmptyBorder(8, 4, 8, 4)
                ));

                JLabel lblNombre = new JLabel("Vuelta " + (i + 1));
                lblNombre.setForeground(colorFila);
                lblNombre.setFont(new Font("SansSerif", Font.PLAIN, 15));

                JLabel lblValor = new JLabel(formatearMillis(duracion));
                lblValor.setForeground(colorFila);
                lblValor.setFont(new Font("Monospaced", Font.PLAIN, 15));

                fila.add(lblNombre, BorderLayout.WEST);
                fila.add(lblValor, BorderLayout.EAST);
                listaVueltasPanel.add(fila);
            }

            listaVueltasPanel.revalidate();
            listaVueltasPanel.repaint();
        }

        private String formatearMillis(long ms) {
            long minutos = (ms / 60000) % 60;
            long segundos = (ms / 1000) % 60;
            long centesimas = (ms % 1000) / 10;
            return String.format("%02d:%02d.%02d", minutos, segundos, centesimas);
        }
    }

    private static class PanelTemporizador extends JPanel {
        private final AnilloTemporizador anillo;
        private final BotonCircularIOS btnCancelar;
        private final BotonCircularIOS btnIniciar;

        private long duracionTotalMs = 60_000;
        private long restanteMs = 60_000;
        private long ultimaActualizacion = 0;
        private boolean activo = false;
        private final Timer timer;

        public PanelTemporizador() {
            setBackground(COLOR_FONDO);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(24, 24, 24, 24));

            JLabel titulo = new JLabel("Temporizador");
            titulo.setForeground(Color.WHITE);
            titulo.setFont(new Font("SansSerif", Font.BOLD, 32));
            add(titulo, BorderLayout.NORTH);

            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            centro.setBackground(COLOR_FONDO);

            anillo = new AnilloTemporizador();
            anillo.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel selectorTiempo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            selectorTiempo.setBackground(COLOR_FONDO);
            int[] presetsMinutos = {1, 5, 10, 25};
            for (int min : presetsMinutos) {
                JButton pill = crearPildoraPreset(min + " min", min * 60_000L);
                selectorTiempo.add(pill);
            }

            JPanel panelBotones = new JPanel(new BorderLayout());
            panelBotones.setBackground(COLOR_FONDO);
            panelBotones.setMaximumSize(new Dimension(310, 85));
            panelBotones.setBorder(new EmptyBorder(0, 10, 0, 10));

            btnCancelar = new BotonCircularIOS("Cancelar", COLOR_GRIS_BOTON, COLOR_TEXTO_SECUNDARIO);
            btnCancelar.setEnabled(false);
            btnIniciar = new BotonCircularIOS("Iniciar", COLOR_VERDE_FONDO, COLOR_VERDE_TEXTO);

            panelBotones.add(btnCancelar, BorderLayout.WEST);
            panelBotones.add(btnIniciar, BorderLayout.EAST);

            centro.add(Box.createVerticalGlue());
            centro.add(anillo);
            centro.add(Box.createVerticalStrut(18));
            centro.add(selectorTiempo);
            centro.add(Box.createVerticalStrut(24));
            centro.add(panelBotones);
            centro.add(Box.createVerticalGlue());

            add(centro, BorderLayout.CENTER);

            timer = new Timer(20, e -> tick());

            btnIniciar.addActionListener(e -> alternarTemporizador());
            btnCancelar.addActionListener(e -> cancelarTemporizador());
            actualizarVistaAnillo();
        }

        private JButton crearPildoraPreset(String texto, long ms) {
            JButton btn = new JButton(texto) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? new Color(58, 58, 60) : COLOR_TARJETA);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(68, 32));

            btn.addActionListener(e -> {
                if (!activo) {
                    duracionTotalMs = ms;
                    restanteMs = ms;
                    actualizarVistaAnillo();
                }
            });
            return btn;
        }

        private void alternarTemporizador() {
            if (!activo) {
                if (restanteMs <= 0) restanteMs = duracionTotalMs;
                ultimaActualizacion = System.currentTimeMillis();
                activo = true;
                timer.start();

                btnIniciar.setEstilos("Pausar", new Color(56, 36, 6), COLOR_NARANJA);
                btnCancelar.setEstilos("Cancelar", COLOR_GRIS_BOTON, Color.WHITE);
                btnCancelar.setEnabled(true);
            } else {
                activo = false;
                timer.stop();
                btnIniciar.setEstilos("Reanudar", COLOR_VERDE_FONDO, COLOR_VERDE_TEXTO);
            }
        }

        private void cancelarTemporizador() {
            activo = false;
            timer.stop();
            restanteMs = duracionTotalMs;
            actualizarVistaAnillo();

            btnIniciar.setEstilos("Iniciar", COLOR_VERDE_FONDO, COLOR_VERDE_TEXTO);
            btnCancelar.setEstilos("Cancelar", COLOR_GRIS_BOTON, COLOR_TEXTO_SECUNDARIO);
            btnCancelar.setEnabled(false);
        }

        private void tick() {
            long ahora = System.currentTimeMillis();
            restanteMs -= (ahora - ultimaActualizacion);
            ultimaActualizacion = ahora;

            if (restanteMs <= 0) {
                restanteMs = 0;
                actualizarVistaAnillo();
                Toolkit.getDefaultToolkit().beep();
                cancelarTemporizador();
                return;
            }
            actualizarVistaAnillo();
        }

        private void actualizarVistaAnillo() {
            double progreso = duracionTotalMs > 0 ? (double) restanteMs / duracionTotalMs : 0;
            long totalSeg = (long) Math.ceil(restanteMs / 1000.0);
            long min = totalSeg / 60;
            long seg = totalSeg % 60;
            anillo.actualizar(progreso, String.format("%02d:%02d", min, seg));
        }
    }

    private static class AnilloTemporizador extends JPanel {
        private double progreso = 1.0;
        private String textoTiempo = "01:00";

        public AnilloTemporizador() {
            setPreferredSize(new Dimension(260, 260));
            setMaximumSize(new Dimension(260, 260));
            setBackground(COLOR_FONDO);
        }

        public void actualizar(double progreso, String textoTiempo) {
            this.progreso = Math.max(0.0, Math.min(1.0, progreso));
            this.textoTiempo = textoTiempo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int grosor = 9;
            int margen = 14;
            int diametro = Math.min(getWidth(), getHeight()) - margen * 2;

            g2.setColor(COLOR_TARJETA);
            g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(margen, margen, diametro, diametro);

            g2.setColor(COLOR_NARANJA);
            double anguloBarrido = 360.0 * progreso;
            g2.draw(new Arc2D.Double(margen, margen, diametro, diametro, 90, anguloBarrido, Arc2D.OPEN));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 54));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(textoTiempo)) / 2;
            int ty = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(textoTiempo, tx, ty);

            g2.dispose();
        }
    }


    // Botón circular con doble anillo
    private static class BotonCircularIOS extends JButton {
        private Color colorFondo;
        private Color colorTexto;
        private boolean hover = false;

        public BotonCircularIOS(String texto, Color colorFondo, Color colorTexto) {
            super(texto);
            this.colorFondo = colorFondo;
            this.colorTexto = colorTexto;

            setPreferredSize(new Dimension(78, 78));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("SansSerif", Font.PLAIN, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public void setEstilos(String texto, Color fondo, Color textoColor) {
            setText(texto);
            this.colorFondo = fondo;
            this.colorTexto = textoColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int d = Math.min(getWidth(), getHeight()) - 2;
            Color bg = (hover && isEnabled()) ? colorFondo.brighter() : colorFondo;

            g2.setColor(bg);
            g2.fillOval(1, 1, d, d);

            g2.setColor(COLOR_FONDO);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(4, 4, d - 6, d - 6);

            g2.setColor(isEnabled() ? colorTexto : COLOR_TEXTO_SECUNDARIO);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    private class BotonTab extends JButton {
        private boolean activo;

        public BotonTab(String titulo, String vistaDestino, boolean activoInicial) {
            super(titulo);
            this.activo = activoInicial;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            actualizarColor();

            addActionListener(e -> cambiarVista(vistaDestino, this));
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
            actualizarColor();
        }

        private void actualizarColor() {
            setForeground(activo ? COLOR_NARANJA : COLOR_TEXTO_SECUNDARIO);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Clock app = new Clock();
            app.setVisible(true);
        });
    }
}
