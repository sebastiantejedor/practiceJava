package VentanasSwing;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicPlayer extends JFrame {

    private static final Color COLOR_ROSA_APPLE = new Color(250, 45, 72);

    private boolean modoOscuro = true;
    private Color colorFondoCentral;
    private Color colorSidebar;
    private Color colorTarjeta;
    private Color colorTarjetaHover;
    private Color colorBorde;
    private Color colorTexto;
    private Color colorTextoDim;
    private Color colorPistaBarra;

    // Biblioteca local
    private final List<CancionLocal> biblioteca = new ArrayList<>();
    private final List<CancionLocal> listaFiltrada = new ArrayList<>();
    private int indiceActual = -1;

    // Motor de reproducción MP3 (JLayer)
    private AdvancedPlayer reproductorMp3;
    private Thread hiloAudio;
    private boolean reproduciendo = false;
    private boolean pausado = false;
    private boolean modoAleatorio = false;
    private boolean modoRepetir = false;
    private int framePausado = 0;
    private long inicioReproduccionMs = 0;
    private long acumuladoMs = 0;

    // Componentes UI
    private JPanel panelSidebar;
    private JPanel panelCentral;
    private JLabel lblTituloBiblio;
    private JTextField txtFiltro;
    private JPanel panelListaCanciones;
    private JLabel lblContadorCanciones;
    private JButton btnTema;
    private JButton btnAgregarArchivos;
    private JButton btnAgregarCarpeta;

    private CaratulaPanel panelCaratula;
    private EcualizadorPanel ecualizador;
    private JLabel lblBadgeEstado;
    private JLabel lblTituloActual;
    private JLabel lblArtistaActual;
    private JLabel lblTiempoTranscurrido;
    private JLabel lblDuracionEstimada;
    private BarraProgresoAudio barraProgreso;
    private JButton btnPlayPause;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JButton btnShuffle;
    private JButton btnRepeat;
    private Timer timerUI;

    public MusicPlayer() {
        aplicarPaletaTema();
        configurarVentana();
        inicializarUI();
        iniciarTimerUI();
        cargarCarpetaMusicaPorDefecto();
    }

    private void aplicarPaletaTema() {
        if (modoOscuro) {
            colorFondoCentral = new Color(16, 16, 18);
            colorSidebar = new Color(24, 24, 27);
            colorTarjeta = new Color(34, 34, 38);
            colorTarjetaHover = new Color(46, 46, 52);
            colorBorde = new Color(48, 48, 54);
            colorTexto = new Color(245, 245, 247);
            colorTextoDim = new Color(152, 152, 157);
            colorPistaBarra = new Color(54, 54, 60);
        } else {
            colorFondoCentral = new Color(255, 255, 255);
            colorSidebar = new Color(245, 245, 247);
            colorTarjeta = new Color(232, 232, 237);
            colorTarjetaHover = new Color(222, 222, 228);
            colorBorde = new Color(216, 216, 222);
            colorTexto = new Color(28, 28, 30);
            colorTextoDim = new Color(110, 110, 115);
            colorPistaBarra = new Color(220, 220, 226);
        }
    }

    private void alternarTema() {
        modoOscuro = !modoOscuro;
        aplicarPaletaTema();

        getContentPane().setBackground(colorFondoCentral);
        panelSidebar.setBackground(colorSidebar);
        panelSidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, colorBorde),
                new EmptyBorder(20, 16, 16, 16)
        ));
        panelCentral.setBackground(colorFondoCentral);

        lblTituloBiblio.setForeground(colorTexto);
        txtFiltro.setForeground(colorTexto);
        lblContadorCanciones.setForeground(colorTextoDim);

        lblTituloActual.setForeground(colorTexto);
        lblTiempoTranscurrido.setForeground(colorTextoDim);
        lblDuracionEstimada.setForeground(colorTextoDim);

        btnAnterior.setForeground(colorTexto);
        btnSiguiente.setForeground(colorTexto);
        btnShuffle.setForeground(modoAleatorio ? COLOR_ROSA_APPLE : colorTextoDim);
        btnRepeat.setForeground(modoRepetir ? COLOR_ROSA_APPLE : colorTextoDim);

        btnTema.setText(modoOscuro ? "☀ Modo Claro" : "🌙 Modo Oscuro");
        btnTema.setBackground(colorTarjeta);
        btnTema.setForeground(colorTexto);

        btnAgregarArchivos.setBackground(colorTarjeta);
        btnAgregarArchivos.setForeground(colorTexto);

        panelListaCanciones.setBackground(colorSidebar);
        renderizarLista();
        repaint();
    }

    private void configurarVentana() {
        setTitle("Music Player");
        setSize(1060, 680);
        setMinimumSize(new Dimension(920, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(colorFondoCentral);
        setLayout(new BorderLayout());
    }

    private void inicializarUI() {
        add(crearSidebarBiblioteca(), BorderLayout.WEST);
        add(crearEscenarioCentral(), BorderLayout.CENTER);
    }

    // =========================================================================
    // PANEL IZQUIERDO: SIDEBAR COMPACTO CON BIBLIOTECA MP3
    // =========================================================================
    private JPanel crearSidebarBiblioteca() {
        panelSidebar = new JPanel(new BorderLayout(0, 12));
        panelSidebar.setBackground(colorSidebar);
        panelSidebar.setPreferredSize(new Dimension(320, 680));
        panelSidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, colorBorde),
                new EmptyBorder(20, 16, 16, 16)
        ));

        // Encabezado del Sidebar
        JPanel topSidebar = new JPanel();
        topSidebar.setLayout(new BoxLayout(topSidebar, BoxLayout.Y_AXIS));
        topSidebar.setOpaque(false);

        JPanel filaTit = new JPanel(new BorderLayout());
        filaTit.setOpaque(false);

        lblTituloBiblio = new JLabel("Biblioteca");
        lblTituloBiblio.setForeground(colorTexto);
        lblTituloBiblio.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filaBotones.setOpaque(false);

        btnAgregarArchivos = crearBotonPildora("+ MP3", colorTarjeta, colorTexto);
        btnAgregarArchivos.setToolTipText("Agregar archivos MP3");
        btnAgregarArchivos.addActionListener(e -> seleccionarArchivosMp3());

        btnAgregarCarpeta = crearBotonPildora("+ Carpeta", COLOR_ROSA_APPLE, Color.WHITE);
        btnAgregarCarpeta.setToolTipText("Escanear carpeta completa");
        btnAgregarCarpeta.addActionListener(e -> seleccionarCarpetaMp3());

        filaBotones.add(btnAgregarArchivos);
        filaBotones.add(btnAgregarCarpeta);

        filaTit.add(lblTituloBiblio, BorderLayout.WEST);
        filaTit.add(filaBotones, BorderLayout.EAST);

        // Buscador estilo macOS
        JPanel cajaBusqueda = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorTarjeta);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        cajaBusqueda.setOpaque(false);
        cajaBusqueda.setBorder(new EmptyBorder(7, 12, 7, 12));

        JLabel iconoLupa = new JLabel("🔍");
        iconoLupa.setForeground(colorTextoDim);

        txtFiltro = new JTextField();
        txtFiltro.setOpaque(false);
        txtFiltro.setBorder(null);
        txtFiltro.setForeground(colorTexto);
        txtFiltro.setCaretColor(COLOR_ROSA_APPLE);
        txtFiltro.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtFiltro.setToolTipText("Buscar canción o artista...");
        txtFiltro.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarLista();
            }
        });

        cajaBusqueda.add(iconoLupa, BorderLayout.WEST);
        cajaBusqueda.add(txtFiltro, BorderLayout.CENTER);

        topSidebar.add(filaTit);
        topSidebar.add(Box.createVerticalStrut(12));
        topSidebar.add(cajaBusqueda);

        panelSidebar.add(topSidebar, BorderLayout.NORTH);

        // Lista de pistas MP3
        panelListaCanciones = new JPanel();
        panelListaCanciones.setLayout(new BoxLayout(panelListaCanciones, BoxLayout.Y_AXIS));
        panelListaCanciones.setBackground(colorSidebar);

        JScrollPane scroll = new JScrollPane(panelListaCanciones);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        panelSidebar.add(scroll, BorderLayout.CENTER);

        lblContadorCanciones = new JLabel("0 pistas MP3");
        lblContadorCanciones.setForeground(colorTextoDim);
        lblContadorCanciones.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panelSidebar.add(lblContadorCanciones, BorderLayout.SOUTH);

        return panelSidebar;
    }

    // =========================================================================
    // ESCENARIO CENTRAL: REPRODUCTOR PROTAGONISTA CENTRADO
    // =========================================================================
    private JPanel crearEscenarioCentral() {
        panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(colorFondoCentral);
        panelCentral.setBorder(new EmptyBorder(20, 32, 28, 32));

        // Barra superior con indicador "Reproduciendo ahora" y botón de Tema Claro/Oscuro
        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setOpaque(false);

        lblBadgeEstado = new JLabel("● EN REPRODUCCIÓN");
        lblBadgeEstado.setForeground(COLOR_ROSA_APPLE);
        lblBadgeEstado.setFont(new Font("SansSerif", Font.BOLD, 11));

        btnTema = crearBotonPildora("☀ Modo Claro", colorTarjeta, colorTexto);
        btnTema.addActionListener(e -> alternarTema());

        barraSuperior.add(lblBadgeEstado, BorderLayout.WEST);
        barraSuperior.add(btnTema, BorderLayout.EAST);
        panelCentral.add(barraSuperior, BorderLayout.NORTH);

        // Contenedor perfectamente centrado para Carátula + Info + Controles
        JPanel centroHero = new JPanel();
        centroHero.setLayout(new BoxLayout(centroHero, BoxLayout.Y_AXIS));
        centroHero.setOpaque(false);

        centroHero.add(Box.createVerticalGlue());

        // 1. Carátula grande protagonista con sombra realista
        panelCaratula = new CaratulaPanel();
        panelCaratula.setAlignmentX(Component.CENTER_ALIGNMENT);
        centroHero.add(panelCaratula);
        centroHero.add(Box.createVerticalStrut(22));

        // 2. Ecualizador espectral centrado
        ecualizador = new EcualizadorPanel(32);
        ecualizador.setAlignmentX(Component.CENTER_ALIGNMENT);
        centroHero.add(ecualizador);
        centroHero.add(Box.createVerticalStrut(18));

        // 3. Título y Artista centrados
        lblTituloActual = new JLabel("Selecciona una canción");
        lblTituloActual.setForeground(colorTexto);
        lblTituloActual.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTituloActual.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblArtistaActual = new JLabel("Tus archivos locales MP3");
        lblArtistaActual.setForeground(COLOR_ROSA_APPLE);
        lblArtistaActual.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lblArtistaActual.setAlignmentX(Component.CENTER_ALIGNMENT);

        centroHero.add(lblTituloActual);
        centroHero.add(Box.createVerticalStrut(4));
        centroHero.add(lblArtistaActual);
        centroHero.add(Box.createVerticalStrut(22));

        // 4. Barra de progreso ancha y tiempos
        barraProgreso = new BarraProgresoAudio();
        barraProgreso.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel filaTiempos = new JPanel(new BorderLayout());
        filaTiempos.setOpaque(false);
        filaTiempos.setMaximumSize(new Dimension(420, 20));
        lblTiempoTranscurrido = new JLabel("0:00");
        lblTiempoTranscurrido.setForeground(colorTextoDim);
        lblTiempoTranscurrido.setFont(new Font("SansSerif", Font.PLAIN, 12));

        lblDuracionEstimada = new JLabel("0:00");
        lblDuracionEstimada.setForeground(colorTextoDim);
        lblDuracionEstimada.setFont(new Font("SansSerif", Font.PLAIN, 12));

        filaTiempos.add(lblTiempoTranscurrido, BorderLayout.WEST);
        filaTiempos.add(lblDuracionEstimada, BorderLayout.EAST);

        centroHero.add(barraProgreso);
        centroHero.add(Box.createVerticalStrut(4));
        centroHero.add(filaTiempos);
        centroHero.add(Box.createVerticalStrut(20));

        // 5. Botonera de control centrada
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        controles.setOpaque(false);
        controles.setMaximumSize(new Dimension(440, 74));

        btnShuffle = crearBotonControl("⇄", 18, colorTextoDim);
        btnShuffle.setToolTipText("Modo aleatorio");
        btnShuffle.addActionListener(e -> {
            modoAleatorio = !modoAleatorio;
            btnShuffle.setForeground(modoAleatorio ? COLOR_ROSA_APPLE : colorTextoDim);
        });

        btnAnterior = crearBotonControl("⏮", 26, colorTexto);
        btnAnterior.addActionListener(e -> reproducirAnterior());

        btnPlayPause = new JButton("▶") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? COLOR_ROSA_APPLE.brighter() : COLOR_ROSA_APPLE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnPlayPause.setPreferredSize(new Dimension(68, 68));
        btnPlayPause.setForeground(Color.WHITE);
        btnPlayPause.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnPlayPause.setContentAreaFilled(false);
        btnPlayPause.setBorderPainted(false);
        btnPlayPause.setFocusPainted(false);
        btnPlayPause.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPlayPause.addActionListener(e -> alternarPlayPause());

        btnSiguiente = crearBotonControl("⏭", 26, colorTexto);
        btnSiguiente.addActionListener(e -> reproducirSiguiente());

        btnRepeat = crearBotonControl("↻", 20, colorTextoDim);
        btnRepeat.setToolTipText("Repetir canción");
        btnRepeat.addActionListener(e -> {
            modoRepetir = !modoRepetir;
            btnRepeat.setForeground(modoRepetir ? COLOR_ROSA_APPLE : colorTextoDim);
        });

        controles.add(btnShuffle);
        controles.add(btnAnterior);
        controles.add(btnPlayPause);
        controles.add(btnSiguiente);
        controles.add(btnRepeat);

        centroHero.add(controles);
        centroHero.add(Box.createVerticalGlue());

        panelCentral.add(centroHero, BorderLayout.CENTER);
        return panelCentral;
    }

    // =========================================================================
    // GESTIÓN DE ARCHIVOS LOCALES MP3
    // =========================================================================
    private void cargarCarpetaMusicaPorDefecto() {
        File carpetaMusica = new File(System.getProperty("user.home"), "Music");
        if (!carpetaMusica.exists()) {
            carpetaMusica = new File(System.getProperty("user.home"), "Música");
        }
        if (carpetaMusica.exists() && carpetaMusica.isDirectory()) {
            escanearDirectorio(carpetaMusica);
        }
        filtrarLista();
    }

    private void seleccionarArchivosMp3() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivos MP3");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de Audio MP3 (*.mp3)", "mp3"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) {
                agregarArchivoSiEsMp3(f);
            }
            filtrarLista();
            if (indiceActual == -1 && !listaFiltrada.isEmpty()) {
                reproducirCancion(listaFiltrada.get(0));
            }
        }
    }

    private void seleccionarCarpetaMp3() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar carpeta con música MP3");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            escanearDirectorio(chooser.getSelectedFile());
            filtrarLista();
            if (indiceActual == -1 && !listaFiltrada.isEmpty()) {
                reproducirCancion(listaFiltrada.get(0));
            }
        }
    }

    private void escanearDirectorio(File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) return;
        for (File f : archivos) {
            if (f.isDirectory()) {
                escanearDirectorio(f);
            } else {
                agregarArchivoSiEsMp3(f);
            }
        }
    }

    private void agregarArchivoSiEsMp3(File f) {
        if (!f.getName().toLowerCase().endsWith(".mp3")) return;
        for (CancionLocal c : biblioteca) {
            if (c.archivo.getAbsolutePath().equals(f.getAbsolutePath())) return;
        }
        biblioteca.add(new CancionLocal(f));
    }

    private void filtrarLista() {
        String q = txtFiltro.getText().trim().toLowerCase();
        listaFiltrada.clear();
        for (CancionLocal c : biblioteca) {
            if (q.isEmpty() || c.titulo.toLowerCase().contains(q) || c.artista.toLowerCase().contains(q)) {
                listaFiltrada.add(c);
            }
        }
        renderizarLista();
        lblContadorCanciones.setText(listaFiltrada.size() + " pistas MP3");
    }

    private void renderizarLista() {
        panelListaCanciones.removeAll();

        if (listaFiltrada.isEmpty()) {
            JLabel vacio = new JLabel("<html><center>Sin archivos MP3.<br>Usa '+ MP3' o '+ Carpeta'</center></html>", SwingConstants.CENTER);
            vacio.setForeground(colorTextoDim);
            vacio.setFont(new Font("SansSerif", Font.PLAIN, 13));
            vacio.setBorder(new EmptyBorder(40, 10, 0, 10));
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelListaCanciones.add(vacio);
        }

        CancionLocal actual = (indiceActual >= 0 && indiceActual < biblioteca.size()) ? biblioteca.get(indiceActual) : null;

        for (CancionLocal c : listaFiltrada) {
            boolean esActual = (c == actual);

            JPanel fila = new JPanel(new BorderLayout(10, 0)) {
                private boolean hover = false;
                {
                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                        @Override
                        public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                        @Override
                        public void mouseClicked(MouseEvent e) { reproducirCancion(c); }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (esActual) {
                        g2.setColor(new Color(250, 45, 72, modoOscuro ? 45 : 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    } else if (hover) {
                        g2.setColor(colorTarjetaHover);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    }
                    g2.dispose();
                }
            };
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            fila.setBorder(new EmptyBorder(6, 8, 6, 10));
            fila.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Miniatura redondeada
            JPanel badge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, c.colorPrimario, 36, 36, c.colorSecundario));
                    g2.fillRoundRect(0, 0, 36, 36, 9, 9);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                    g2.drawString(esActual ? "🔊" : "♪", 11, 23);
                    g2.dispose();
                }
            };
            badge.setPreferredSize(new Dimension(36, 36));
            badge.setOpaque(false);
            fila.add(badge, BorderLayout.WEST);

            // Título + Artista
            JPanel textos = new JPanel();
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.setOpaque(false);

            JLabel lblTit = new JLabel(c.titulo);
            lblTit.setForeground(esActual ? COLOR_ROSA_APPLE : colorTexto);
            lblTit.setFont(new Font("SansSerif", Font.BOLD, 13));

            JLabel lblSub = new JLabel(c.artista);
            lblSub.setForeground(colorTextoDim);
            lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));

            textos.add(Box.createVerticalGlue());
            textos.add(lblTit);
            textos.add(Box.createVerticalStrut(2));
            textos.add(lblSub);
            textos.add(Box.createVerticalGlue());
            fila.add(textos, BorderLayout.CENTER);

            JLabel lblDur = new JLabel(formatearSegundos(c.duracionSegundosEstimada));
            lblDur.setForeground(esActual ? COLOR_ROSA_APPLE : colorTextoDim);
            lblDur.setFont(new Font("SansSerif", Font.PLAIN, 11));
            fila.add(lblDur, BorderLayout.EAST);

            panelListaCanciones.add(fila);
            panelListaCanciones.add(Box.createVerticalStrut(3));
        }

        panelListaCanciones.revalidate();
        panelListaCanciones.repaint();
    }

    // =========================================================================
    // MOTOR DE REPRODUCCIÓN MP3 (JLayer)
    // =========================================================================
    private void reproducirCancion(CancionLocal cancion) {
        detenerReproduccionTotal();

        indiceActual = biblioteca.indexOf(cancion);
        framePausado = 0;
        acumuladoMs = 0;
        pausado = false;

        lblTituloActual.setText(cancion.titulo);
        lblArtistaActual.setText(cancion.artista);
        lblDuracionEstimada.setText(formatearSegundos(cancion.duracionSegundosEstimada));
        panelCaratula.setColores(cancion.colorPrimario, cancion.colorSecundario);
        renderizarLista();

        iniciarStreamDesdeFrame(cancion, 0);
    }

    private void iniciarStreamDesdeFrame(CancionLocal cancion, int frameInicial) {
        reproduciendo = true;
        btnPlayPause.setText("⏸");
        ecualizador.setActivo(true);
        inicioReproduccionMs = System.currentTimeMillis();

        hiloAudio = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(cancion.archivo);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                reproductorMp3 = new AdvancedPlayer(bis);
                reproductorMp3.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        if (pausado) {
                            framePausado += (int) ((System.currentTimeMillis() - inicioReproduccionMs) / 26.12);
                        } else if (reproduciendo) {
                            reproduciendo = false;
                            SwingUtilities.invokeLater(() -> {
                                ecualizador.setActivo(false);
                                btnPlayPause.setText("▶");
                                if (modoRepetir) {
                                    reproducirCancion(cancion);
                                } else {
                                    reproducirSiguiente();
                                }
                            });
                        }
                    }
                });
                reproductorMp3.play(frameInicial, Integer.MAX_VALUE);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    reproduciendo = false;
                    ecualizador.setActivo(false);
                    btnPlayPause.setText("▶");
                    lblArtistaActual.setText("Error leyendo MP3: " + ex.getMessage());
                });
            }
        });
        hiloAudio.setDaemon(true);
        hiloAudio.start();
    }

    private void alternarPlayPause() {
        if (biblioteca.isEmpty()) return;
        if (indiceActual == -1) {
            reproducirCancion(listaFiltrada.isEmpty() ? biblioteca.get(0) : listaFiltrada.get(0));
            return;
        }

        CancionLocal actual = biblioteca.get(indiceActual);
        if (reproduciendo) {
            pausado = true;
            reproduciendo = false;
            acumuladoMs += System.currentTimeMillis() - inicioReproduccionMs;
            if (reproductorMp3 != null) {
                reproductorMp3.stop();
            }
            btnPlayPause.setText("▶");
            ecualizador.setActivo(false);
        } else {
            pausado = false;
            iniciarStreamDesdeFrame(actual, framePausado);
        }
    }

    private void reproducirSiguiente() {
        if (listaFiltrada.isEmpty()) return;
        CancionLocal actual = (indiceActual >= 0 && indiceActual < biblioteca.size()) ? biblioteca.get(indiceActual) : null;
        int posEnFiltrada = listaFiltrada.indexOf(actual);

        int siguienteIdx;
        if (modoAleatorio) {
            siguienteIdx = new Random().nextInt(listaFiltrada.size());
        } else {
            siguienteIdx = (posEnFiltrada + 1) % listaFiltrada.size();
        }
        reproducirCancion(listaFiltrada.get(siguienteIdx));
    }

    private void reproducirAnterior() {
        if (listaFiltrada.isEmpty()) return;
        CancionLocal actual = (indiceActual >= 0 && indiceActual < biblioteca.size()) ? biblioteca.get(indiceActual) : null;
        int posEnFiltrada = listaFiltrada.indexOf(actual);
        int anteriorIdx = (posEnFiltrada - 1 + listaFiltrada.size()) % listaFiltrada.size();
        reproducirCancion(listaFiltrada.get(anteriorIdx));
    }

    private void detenerReproduccionTotal() {
        reproduciendo = false;
        pausado = false;
        if (reproductorMp3 != null) {
            try {
                reproductorMp3.close();
            } catch (Exception ignored) {
            }
            reproductorMp3 = null;
        }
    }

    private void iniciarTimerUI() {
        timerUI = new Timer(50, e -> {
            ecualizador.animarPaso();
            if (indiceActual >= 0 && indiceActual < biblioteca.size()) {
                CancionLocal c = biblioteca.get(indiceActual);
                long transcurridoMs = acumuladoMs + (reproduciendo ? (System.currentTimeMillis() - inicioReproduccionMs) : 0);
                long seg = transcurridoMs / 1000;
                lblTiempoTranscurrido.setText(formatearSegundos(seg));

                if (c.duracionSegundosEstimada > 0) {
                    barraProgreso.setProgreso((double) seg / c.duracionSegundosEstimada);
                }
            }
        });
        timerUI.start();
    }

    private String formatearSegundos(long totalSeg) {
        long min = totalSeg / 60;
        long seg = totalSeg % 60;
        return String.format("%d:%02d", min, seg);
    }

    private JButton crearBotonPildora(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getBackground() != null ? getBackground() : bg;
                g2.setColor(getModel().isRollover() ? (modoOscuro ? base.brighter() : base.darker()) : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private JButton crearBotonControl(String simbolo, int tamano, Color color) {
        JButton btn = new JButton(simbolo);
        btn.setForeground(color);
        btn.setFont(new Font("SansSerif", Font.PLAIN, tamano));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =========================================================================
    // MODELO DE CANCIÓN LOCAL MP3
    // =========================================================================
    private static class CancionLocal {
        final File archivo;
        final String titulo;
        final String artista;
        final long duracionSegundosEstimada;
        final Color colorPrimario;
        final Color colorSecundario;

        CancionLocal(File archivo) {
            this.archivo = archivo;
            String nombreSinExt = archivo.getName().replaceFirst("(?i)\\.mp3$", "");

            if (nombreSinExt.contains(" - ")) {
                String[] partes = nombreSinExt.split(" - ", 2);
                this.artista = partes[0].trim();
                this.titulo = partes[1].trim();
            } else {
                this.titulo = nombreSinExt;
                this.artista = archivo.getParentFile() != null ? archivo.getParentFile().getName() : "Archivo Local";
            }

            this.duracionSegundosEstimada = Math.max(1, archivo.length() / 24000L);

            Color[][] paletas = {
                    {new Color(250, 45, 72), new Color(140, 25, 95)},
                    {new Color(10, 132, 255), new Color(94, 92, 230)},
                    {new Color(191, 90, 242), new Color(255, 55, 95)},
                    {new Color(255, 159, 10), new Color(255, 69, 58)},
                    {new Color(48, 209, 88), new Color(10, 132, 255)}
            };
            int idx = Math.abs(titulo.hashCode()) % paletas.length;
            this.colorPrimario = paletas[idx][0];
            this.colorSecundario = paletas[idx][1];
        }
    }

    // =========================================================================
    // COMPONENTES VISUALES PERSONALIZADOS (CARÁTULA CON SOMBRA, ECUALIZADOR, BARRA)
    // =========================================================================
    private class CaratulaPanel extends JPanel {
        private Color c1 = COLOR_ROSA_APPLE;
        private Color c2 = new Color(88, 86, 214);

        public CaratulaPanel() {
            setPreferredSize(new Dimension(260, 260));
            setMaximumSize(new Dimension(260, 260));
            setOpaque(false);
        }

        public void setColores(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 12;
            int w = getWidth() - pad * 2;
            int h = getHeight() - pad * 2;

            // Sombra ambiental suave estilo Apple Music
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), modoOscuro ? 16 : 22));
                g2.fillRoundRect(pad - i, pad + 6 + i, w + i * 2, h + i * 2, 32, 32);
            }

            g2.setClip(new RoundRectangle2D.Double(pad, pad, w, h, 28, 28));
            g2.setPaint(new GradientPaint(pad, pad, c1, pad + w, pad + h, c2));
            g2.fillRect(pad, pad, w, h);

            // Brillo sutil superior tipo cristal
            g2.setPaint(new GradientPaint(pad, pad, new Color(255, 255, 255, 55), pad, pad + h / 2, new Color(255, 255, 255, 0)));
            g2.fillRect(pad, pad, w, h / 2);

            g2.setColor(new Color(255, 255, 255, 235));
            g2.setFont(new Font("SansSerif", Font.BOLD, 84));
            FontMetrics fm = g2.getFontMetrics();
            String nota = "♫";
            g2.drawString(nota, pad + (w - fm.stringWidth(nota)) / 2, pad + ((h - fm.getHeight()) / 2) + fm.getAscent());

            g2.setClip(null);
            g2.dispose();
        }
    }

    private static class EcualizadorPanel extends JPanel {
        private final double[] alturas;
        private final double[] objetivos;
        private boolean activo = false;
        private final Random random = new Random();

        public EcualizadorPanel(int numBarras) {
            this.alturas = new double[numBarras];
            this.objetivos = new double[numBarras];
            for (int i = 0; i < numBarras; i++) {
                alturas[i] = 0.12;
                objetivos[i] = 0.12;
            }
            setPreferredSize(new Dimension(360, 44));
            setMaximumSize(new Dimension(360, 44));
            setOpaque(false);
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
        }

        public void animarPaso() {
            for (int i = 0; i < alturas.length; i++) {
                if (activo) {
                    if (Math.abs(alturas[i] - objetivos[i]) < 0.08) {
                        double factorCentro = 1.0 - Math.abs((i - alturas.length / 2.0) / (alturas.length / 1.4));
                        objetivos[i] = 0.15 + random.nextDouble() * (0.55 + 0.35 * factorCentro);
                    }
                    alturas[i] += (objetivos[i] - alturas[i]) * 0.32;
                } else {
                    alturas[i] += (0.10 - alturas[i]) * 0.2;
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int n = alturas.length;
            int gap = 4;
            int anchoBarra = Math.max(4, (getWidth() - (n - 1) * gap) / n);
            int maxAlto = getHeight() - 4;

            for (int i = 0; i < n; i++) {
                int h = Math.max(5, (int) (alturas[i] * maxAlto));
                int x = i * (anchoBarra + gap);
                int y = (getHeight() - h) / 2;

                g2.setPaint(new GradientPaint(x, y, COLOR_ROSA_APPLE, x, y + h, new Color(255, 110, 130)));
                g2.fillRoundRect(x, y, anchoBarra, h, anchoBarra, anchoBarra);
            }
            g2.dispose();
        }
    }

    private class BarraProgresoAudio extends JPanel {
        private double progreso = 0;

        public BarraProgresoAudio() {
            setPreferredSize(new Dimension(420, 16));
            setMaximumSize(new Dimension(420, 16));
            setOpaque(false);
        }

        public void setProgreso(double p) {
            this.progreso = Math.max(0.0, Math.min(1.0, p));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = 6;
            int y = (getHeight() - h) / 2;

            g2.setColor(colorPistaBarra);
            g2.fillRoundRect(0, y, w, h, h, h);

            int ancho = (int) (w * progreso);
            if (ancho > 0) {
                g2.setColor(COLOR_ROSA_APPLE);
                g2.fillRoundRect(0, y, ancho, h, h, h);
            }

            int knobX = Math.max(0, Math.min(w - 14, ancho - 7));
            g2.setColor(modoOscuro ? Color.WHITE : COLOR_ROSA_APPLE);
            g2.fillOval(knobX, (getHeight() - 14) / 2, 14, 14);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MusicPlayer player = new MusicPlayer();
            player.setVisible(true);
        });
    }
}