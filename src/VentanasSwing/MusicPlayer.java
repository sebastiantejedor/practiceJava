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

    // Paleta oficial
    private static final Color COLOR_FONDO = new Color(18, 18, 20);
    private static final Color COLOR_SIDEBAR = new Color(24, 24, 27);
    private static final Color COLOR_TARJETA = new Color(30, 30, 34);
    private static final Color COLOR_TARJETA_HOVER = new Color(42, 42, 48);
    private static final Color COLOR_BORDE = new Color(50, 50, 56);
    private static final Color COLOR_ROSA_APPLE = new Color(250, 45, 72);
    private static final Color COLOR_TEXTO = new Color(245, 245, 247);
    private static final Color COLOR_TEXTO_DIM = new Color(152, 152, 157);

    private final List<CancionLocal> biblioteca = new ArrayList<>();
    private final List<CancionLocal> listaFiltrada = new ArrayList<>();
    private int indiceActual = -1;

    private AdvancedPlayer reproductorMp3;
    private Thread hiloAudio;
    private boolean reproduciendo = false;
    private boolean pausado = false;
    private boolean modoAleatorio = false;
    private boolean modoRepetir = false;
    private int framePausado = 0;
    private long inicioReproduccionMs = 0;
    private long acumuladoMs = 0;

    private JTextField txtFiltro;
    private JPanel panelListaCanciones;
    private JLabel lblContadorCanciones;

    private CaratulaPanel panelCaratula;
    private EcualizadorPanel ecualizador;
    private JLabel lblTituloActual;
    private JLabel lblArtistaActual;
    private JLabel lblTiempoTranscurrido;
    private JLabel lblDuracionEstimada;
    private BarraProgresoAudio barraProgreso;
    private JButton btnPlayPause;
    private JButton btnShuffle;
    private JButton btnRepeat;
    private Timer timerUI;

    public MusicPlayer() {
        configurarVentana();
        inicializarUI();
        iniciarTimerUI();
        cargarCarpetaMusicaPorDefecto();
    }

    private void configurarVentana() {
        setTitle("Music Player");
        setSize(1000, 650);
        setMinimumSize(new Dimension(900, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }

    private void inicializarUI() {
        add(crearPanelBiblioteca(), BorderLayout.CENTER);
        add(crearPanelReproductor(), BorderLayout.EAST);
    }

    private JPanel crearPanelBiblioteca() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 26, 24, 20));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(COLOR_FONDO);

        JLabel titulo = new JLabel("Tu Biblioteca MP3");
        titulo.setForeground(COLOR_TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JPanel botonesImportar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonesImportar.setBackground(COLOR_FONDO);

        JButton btnAgregarArchivos = crearBotonPildora("+ Archivos MP3", COLOR_TARJETA, COLOR_TEXTO);
        btnAgregarArchivos.addActionListener(e -> seleccionarArchivosMp3());

        JButton btnAgregarCarpeta = crearBotonPildora("+ Carpeta", COLOR_ROSA_APPLE, Color.WHITE);
        btnAgregarCarpeta.addActionListener(e -> seleccionarCarpetaMp3());

        botonesImportar.add(btnAgregarArchivos);
        botonesImportar.add(btnAgregarCarpeta);

        filaTitulo.add(titulo, BorderLayout.WEST);
        filaTitulo.add(botonesImportar, BorderLayout.EAST);

        JPanel cajaBusqueda = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_TARJETA);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(COLOR_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        cajaBusqueda.setOpaque(false);
        cajaBusqueda.setBorder(new EmptyBorder(9, 14, 9, 14));

        JLabel iconoLupa = new JLabel("🔍");
        iconoLupa.setForeground(COLOR_TEXTO_DIM);

        txtFiltro = new JTextField();
        txtFiltro.setOpaque(false);
        txtFiltro.setBorder(null);
        txtFiltro.setForeground(COLOR_TEXTO);
        txtFiltro.setCaretColor(COLOR_ROSA_APPLE);
        txtFiltro.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtFiltro.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarLista();
            }
        });

        cajaBusqueda.add(iconoLupa, BorderLayout.WEST);
        cajaBusqueda.add(txtFiltro, BorderLayout.CENTER);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(COLOR_FONDO);
        header.add(filaTitulo);
        header.add(Box.createVerticalStrut(14));
        header.add(cajaBusqueda);

        panel.add(header, BorderLayout.NORTH);

        panelListaCanciones = new JPanel();
        panelListaCanciones.setLayout(new BoxLayout(panelListaCanciones, BoxLayout.Y_AXIS));
        panelListaCanciones.setBackground(COLOR_FONDO);

        JScrollPane scroll = new JScrollPane(panelListaCanciones);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        panel.add(scroll, BorderLayout.CENTER);

        lblContadorCanciones = new JLabel("0 canciones cargadas");
        lblContadorCanciones.setForeground(COLOR_TEXTO_DIM);
        lblContadorCanciones.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lblContadorCanciones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelReproductor() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(370, 650));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDE),
                new EmptyBorder(28, 28, 28, 28)
        ));

        panelCaratula = new CaratulaPanel();
        panelCaratula.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(panelCaratula);
        sidebar.add(Box.createVerticalStrut(22));

        ecualizador = new EcualizadorPanel(26);
        ecualizador.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(ecualizador);
        sidebar.add(Box.createVerticalStrut(20));

        lblTituloActual = new JLabel("Sin reproducción");
        lblTituloActual.setForeground(COLOR_TEXTO);
        lblTituloActual.setFont(new Font("SansSerif", Font.BOLD, 19));
        lblTituloActual.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblArtistaActual = new JLabel("Selecciona un MP3 de tu equipo");
        lblArtistaActual.setForeground(COLOR_ROSA_APPLE);
        lblArtistaActual.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblArtistaActual.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(lblTituloActual);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(lblArtistaActual);
        sidebar.add(Box.createVerticalStrut(22));

        barraProgreso = new BarraProgresoAudio();
        barraProgreso.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel filaTiempos = new JPanel(new BorderLayout());
        filaTiempos.setOpaque(false);
        filaTiempos.setMaximumSize(new Dimension(310, 20));
        lblTiempoTranscurrido = new JLabel("0:00");
        lblTiempoTranscurrido.setForeground(COLOR_TEXTO_DIM);
        lblTiempoTranscurrido.setFont(new Font("SansSerif", Font.PLAIN, 11));

        lblDuracionEstimada = new JLabel("0:00");
        lblDuracionEstimada.setForeground(COLOR_TEXTO_DIM);
        lblDuracionEstimada.setFont(new Font("SansSerif", Font.PLAIN, 11));

        filaTiempos.add(lblTiempoTranscurrido, BorderLayout.WEST);
        filaTiempos.add(lblDuracionEstimada, BorderLayout.EAST);

        sidebar.add(barraProgreso);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(filaTiempos);
        sidebar.add(Box.createVerticalStrut(22));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        controles.setOpaque(false);
        controles.setMaximumSize(new Dimension(330, 68));

        btnShuffle = crearBotonControl("⇄", 16, COLOR_TEXTO_DIM);
        btnShuffle.setToolTipText("Aleatorio");
        btnShuffle.addActionListener(e -> {
            modoAleatorio = !modoAleatorio;
            btnShuffle.setForeground(modoAleatorio ? COLOR_ROSA_APPLE : COLOR_TEXTO_DIM);
        });

        JButton btnAnterior = crearBotonControl("⏮", 22, COLOR_TEXTO);
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
        btnPlayPause.setPreferredSize(new Dimension(62, 62));
        btnPlayPause.setForeground(Color.WHITE);
        btnPlayPause.setFont(new Font("SansSerif", Font.BOLD, 22));
        btnPlayPause.setContentAreaFilled(false);
        btnPlayPause.setBorderPainted(false);
        btnPlayPause.setFocusPainted(false);
        btnPlayPause.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPlayPause.addActionListener(e -> alternarPlayPause());

        JButton btnSiguiente = crearBotonControl("⏭", 22, COLOR_TEXTO);
        btnSiguiente.addActionListener(e -> reproducirSiguiente());

        btnRepeat = crearBotonControl("↻", 18, COLOR_TEXTO_DIM);
        btnRepeat.setToolTipText("Repetir pista");
        btnRepeat.addActionListener(e -> {
            modoRepetir = !modoRepetir;
            btnRepeat.setForeground(modoRepetir ? COLOR_ROSA_APPLE : COLOR_TEXTO_DIM);
        });

        controles.add(btnShuffle);
        controles.add(btnAnterior);
        controles.add(btnPlayPause);
        controles.add(btnSiguiente);
        controles.add(btnRepeat);

        sidebar.add(controles);
        sidebar.add(Box.createVerticalGlue());

        JLabel hintFormatos = new JLabel("Formato soportado: MP3 Local (320 kbps / VBR)");
        hintFormatos.setForeground(COLOR_TEXTO_DIM);
        hintFormatos.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hintFormatos.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(hintFormatos);

        return sidebar;
    }


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
        lblContadorCanciones.setText(listaFiltrada.size() + " canciones MP3 en biblioteca");
    }

    private void renderizarLista() {
        panelListaCanciones.removeAll();

        if (listaFiltrada.isEmpty()) {
            JLabel vacio = new JLabel("No hay archivos MP3 cargados. Usa '+ Archivos MP3' o '+ Carpeta' arriba.");
            vacio.setForeground(COLOR_TEXTO_DIM);
            vacio.setFont(new Font("SansSerif", Font.PLAIN, 14));
            vacio.setBorder(new EmptyBorder(40, 10, 0, 0));
            panelListaCanciones.add(vacio);
        }

        CancionLocal actual = (indiceActual >= 0 && indiceActual < biblioteca.size()) ? biblioteca.get(indiceActual) : null;

        for (CancionLocal c : listaFiltrada) {
            boolean esActual = (c == actual);

            JPanel fila = new JPanel(new BorderLayout(14, 0)) {
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
                        g2.setColor(new Color(250, 45, 72, 42));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    } else if (hover) {
                        g2.setColor(COLOR_TARJETA_HOVER);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    }
                    g2.dispose();
                }
            };
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            fila.setBorder(new EmptyBorder(8, 12, 8, 14));
            fila.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Miniatura de color único por pista
            JPanel badge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, c.colorPrimario, 42, 42, c.colorSecundario));
                    g2.fillRoundRect(0, 0, 42, 42, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    g2.drawString(esActual ? "🔊" : "♪", 13, 27);
                    g2.dispose();
                }
            };
            badge.setPreferredSize(new Dimension(42, 42));
            badge.setOpaque(false);
            fila.add(badge, BorderLayout.WEST);

            JPanel textos = new JPanel();
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.setOpaque(false);

            JLabel lblTit = new JLabel(c.titulo);
            lblTit.setForeground(esActual ? COLOR_ROSA_APPLE : COLOR_TEXTO);
            lblTit.setFont(new Font("SansSerif", Font.BOLD, 14));

            JLabel lblSub = new JLabel(c.artista + "  •  " + c.tamanoMb);
            lblSub.setForeground(COLOR_TEXTO_DIM);
            lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));

            textos.add(Box.createVerticalGlue());
            textos.add(lblTit);
            textos.add(Box.createVerticalStrut(3));
            textos.add(lblSub);
            textos.add(Box.createVerticalGlue());
            fila.add(textos, BorderLayout.CENTER);

            JLabel lblDur = new JLabel(formatearSegundos(c.duracionSegundosEstimada));
            lblDur.setForeground(esActual ? COLOR_ROSA_APPLE : COLOR_TEXTO_DIM);
            lblDur.setFont(new Font("SansSerif", Font.PLAIN, 12));
            fila.add(lblDur, BorderLayout.EAST);

            panelListaCanciones.add(fila);
            panelListaCanciones.add(Box.createVerticalStrut(4));
        }

        panelListaCanciones.revalidate();
        panelListaCanciones.repaint();
    }

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
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
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

    private static class CancionLocal {
        final File archivo;
        final String titulo;
        final String artista;
        final String tamanoMb;
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

            double mb = archivo.length() / (1024.0 * 1024.0);
            this.tamanoMb = String.format("%.1f MB", mb);
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

    private static class CaratulaPanel extends JPanel {
        private Color c1 = COLOR_ROSA_APPLE;
        private Color c2 = new Color(88, 86, 214);

        public CaratulaPanel() {
            setPreferredSize(new Dimension(240, 240));
            setMaximumSize(new Dimension(240, 240));
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

            int w = getWidth();
            int h = getHeight();
            g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, 26, 26));

            g2.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 220));
            g2.setFont(new Font("SansSerif", Font.BOLD, 76));
            FontMetrics fm = g2.getFontMetrics();
            String nota = "♫";
            g2.drawString(nota, (w - fm.stringWidth(nota)) / 2, ((h - fm.getHeight()) / 2) + fm.getAscent());

            g2.setClip(null);
            g2.setColor(new Color(255, 255, 255, 35));
            g2.draw(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 26, 26));
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
            setPreferredSize(new Dimension(300, 42));
            setMaximumSize(new Dimension(300, 42));
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

    private static class BarraProgresoAudio extends JPanel {
        private double progreso = 0;

        public BarraProgresoAudio() {
            setPreferredSize(new Dimension(310, 14));
            setMaximumSize(new Dimension(310, 14));
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

            g2.setColor(new Color(55, 55, 62));
            g2.fillRoundRect(0, y, w, h, h, h);

            int ancho = (int) (w * progreso);
            if (ancho > 0) {
                g2.setColor(COLOR_ROSA_APPLE);
                g2.fillRoundRect(0, y, ancho, h, h, h);
            }

            int knobX = Math.max(0, Math.min(w - 12, ancho - 6));
            g2.setColor(Color.WHITE);
            g2.fillOval(knobX, (getHeight() - 12) / 2, 12, 12);

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