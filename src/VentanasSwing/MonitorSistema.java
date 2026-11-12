package VentanasSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MonitorSistema extends JFrame {

    // Paleta de colores
    private static final Color COLOR_SIDEBAR = new Color(24, 24, 27);
    private static final Color COLOR_FONDO = new Color(16, 16, 18);
    private static final Color COLOR_TARJETA = new Color(28, 28, 31);
    private static final Color COLOR_TARJETA_SECUNDARIA = new Color(36, 36, 40);
    private static final Color COLOR_BORDE = new Color(48, 48, 54);
    private static final Color COLOR_TEXTO = new Color(245, 245, 247);
    private static final Color COLOR_TEXTO_DIM = new Color(142, 142, 147);

    private static final Color COLOR_AZUL = new Color(10, 132, 255);
    private static final Color COLOR_MORADO = new Color(191, 90, 242);
    private static final Color COLOR_VERDE = new Color(48, 209, 88);
    private static final Color COLOR_NARANJA = new Color(255, 159, 10);
    private static final Color COLOR_ROJO = new Color(255, 69, 58);
    private static final Color COLOR_CIAN = new Color(100, 210, 255);

    private final com.sun.management.OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final RuntimeMXBean runtimeBean;

    private CardLayout cardLayout;
    private JPanel panelContenido;
    private final List<BotonSidebar> botonesNav = new ArrayList<>();

    private JLabel lblUptimeValor;
    private JLabel lblBateriaValor;
    private JLabel lblResumenCpu;
    private JLabel lblResumenRam;
    private JLabel lblResumenDiscos;

    private MedidorRadial gaugeCpu;
    private MedidorRadial gaugeRam;
    private GraficaHistorial graficaCpu;
    private GraficaHistorial graficaRam;
    private JLabel lblCpuDetalle;
    private JLabel lblRamDetalle;
    private JLabel lblSwapDetalle;
    private BarraProgresoMac barraSwap;
    private JLabel lblJvmHeapDetalle;
    private BarraProgresoMac barraJvmHeap;

    private JPanel contenedorDiscos;

    private JTextField txtHostPing;
    private JButton btnIniciarPing;
    private JLabel lblPingActual;
    private JLabel lblPingPromedio;
    private JLabel lblPingMinMax;
    private JLabel lblPingPerdida;
    private JLabel lblEstadoConexion;
    private GraficaHistorial graficaPing;
    private DefaultListModel<String> modeloLogPing;
    private Timer timerPing;
    private boolean pingCorriendo = false;
    private int pingsEnviados = 0;
    private int pingsPerdidos = 0;
    private long sumaLatencia = 0;
    private long minLatencia = Long.MAX_VALUE;
    private long maxLatencia = 0;

    public MonitorSistema() {
        osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        memoryBean = ManagementFactory.getMemoryMXBean();
        runtimeBean = ManagementFactory.getRuntimeMXBean();

        configurarVentana();
        inicializarUI();
        iniciarActualizacionEnVivo();
    }

    private void configurarVentana() {
        setTitle("Monitor del Sistema — macOS Edition");
        setSize(1020, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(940, 640));
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }

    private void inicializarUI() {
        add(crearSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(COLOR_FONDO);

        panelContenido.add(crearVistaResumen(), "RESUMEN");
        panelContenido.add(crearVistaRendimiento(), "RENDIMIENTO");
        panelContenido.add(crearVistaAlmacenamiento(), "DISCOS");
        panelContenido.add(crearVistaRed(), "RED");

        add(panelContenido, BorderLayout.CENTER);
    }

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 700));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDE),
                new EmptyBorder(24, 14, 20, 14)
        ));

        JLabel lblAppTitulo = new JLabel("Monitor del Sistema");
        lblAppTitulo.setForeground(COLOR_TEXTO);
        lblAppTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAppTitulo.setBorder(new EmptyBorder(0, 8, 4, 0));
        lblAppTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSeccion = new JLabel("DIAGNÓSTICO");
        lblSeccion.setForeground(COLOR_TEXTO_DIM);
        lblSeccion.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSeccion.setBorder(new EmptyBorder(18, 8, 8, 0));
        lblSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(lblAppTitulo);
        sidebar.add(lblSeccion);

        agregarItemNav(sidebar, "Acerca de este Equipo", "RESUMEN", COLOR_AZUL, true);
        agregarItemNav(sidebar, "CPU y Memoria", "RENDIMIENTO", COLOR_MORADO, false);
        agregarItemNav(sidebar, "Almacenamiento", "DISCOS", COLOR_NARANJA, false);
        agregarItemNav(sidebar, "Diagnóstico de Red", "RED", COLOR_CIAN, false);

        sidebar.add(Box.createVerticalGlue());


        JButton btnExportar = crearBotonPildora("Exportar Informe (.txt)", COLOR_AZUL, Color.WHITE);
        btnExportar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnExportar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExportar.addActionListener(e -> exportarInformeTxt());
        sidebar.add(btnExportar);
        sidebar.add(Box.createVerticalStrut(16));

        JLabel lblHostPie = new JLabel(obtenerNombreEquipo());
        lblHostPie.setForeground(COLOR_TEXTO);
        lblHostPie.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblHostPie.setBorder(new EmptyBorder(0, 8, 2, 0));
        lblHostPie.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblOsPie = new JLabel(System.getProperty("os.name") + " • " + System.getProperty("os.arch"));
        lblOsPie.setForeground(COLOR_TEXTO_DIM);
        lblOsPie.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblOsPie.setBorder(new EmptyBorder(0, 8, 0, 0));
        lblOsPie.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(lblHostPie);
        sidebar.add(lblOsPie);

        return sidebar;
    }

    private void agregarItemNav(JPanel sidebar, String titulo, String vista, Color acento, boolean activo) {
        BotonSidebar btn = new BotonSidebar(titulo, acento, activo);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            cardLayout.show(panelContenido, vista);
            for (BotonSidebar b : botonesNav) b.setActivo(b == btn);
            if (vista.equals("DISCOS")) refrescarDiscos();
        });
        botonesNav.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private JComponent crearVistaResumen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 28, 28));

        TarjetaMac heroCard = new TarjetaMac();
        heroCard.setLayout(new BorderLayout(20, 0));
        heroCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));

        heroCard.add(new IconoComputador(), BorderLayout.WEST);

        JPanel infoHero = new JPanel();
        infoHero.setLayout(new BoxLayout(infoHero, BoxLayout.Y_AXIS));
        infoHero.setOpaque(false);

        JLabel lblNombreHost = new JLabel(obtenerNombreEquipo());
        lblNombreHost.setForeground(COLOR_TEXTO);
        lblNombreHost.setFont(new Font("SansSerif", Font.BOLD, 26));

        String osTexto = System.getProperty("os.name") + " (Versión " + System.getProperty("os.version")
                + ") • Arquitectura " + System.getProperty("os.arch").toUpperCase();
        JLabel lblOsSub = new JLabel(osTexto);
        lblOsSub.setForeground(COLOR_TEXTO_DIM);
        lblOsSub.setFont(new Font("SansSerif", Font.PLAIN, 14));

        double ramTotalGb = osBean.getTotalMemorySize() / (1024.0 * 1024.0 * 1024.0);
        String chipTexto = Runtime.getRuntime().availableProcessors() + " Núcleos Lógicos  |  "
                + String.format("%.1f GB RAM Instalada", ramTotalGb);
        JLabel lblHardwareBadge = new JLabel(chipTexto);
        lblHardwareBadge.setForeground(COLOR_AZUL);
        lblHardwareBadge.setFont(new Font("SansSerif", Font.BOLD, 13));

        infoHero.add(Box.createVerticalGlue());
        infoHero.add(lblNombreHost);
        infoHero.add(Box.createVerticalStrut(4));
        infoHero.add(lblOsSub);
        infoHero.add(Box.createVerticalStrut(8));
        infoHero.add(lblHardwareBadge);
        infoHero.add(Box.createVerticalGlue());

        heroCard.add(infoHero, BorderLayout.CENTER);
        panel.add(heroCard);
        panel.add(Box.createVerticalStrut(16));

        JPanel filaEstado = new JPanel(new GridLayout(1, 3, 14, 0));
        filaEstado.setBackground(COLOR_FONDO);
        filaEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblResumenCpu = new JLabel("0%");
        lblResumenRam = new JLabel("0.0 GB");
        lblResumenDiscos = new JLabel(File.listRoots().length + " Unidades");

        filaEstado.add(crearMiniTarjetaMetrica("CARGA DE CPU", lblResumenCpu, COLOR_AZUL));
        filaEstado.add(crearMiniTarjetaMetrica("MEMORIA EN USO", lblResumenRam, COLOR_MORADO));
        filaEstado.add(crearMiniTarjetaMetrica("ALMACENAMIENTO", lblResumenDiscos, COLOR_NARANJA));

        panel.add(filaEstado);
        panel.add(Box.createVerticalStrut(18));

        TarjetaMac tarjetaHardware = new TarjetaMac();
        tarjetaHardware.setLayout(new BoxLayout(tarjetaHardware, BoxLayout.Y_AXIS));
        tarjetaHardware.add(crearTituloSeccion("Hardware y Energía"));

        double swapTotalGb = osBean.getTotalSwapSpaceSize() / (1024.0 * 1024.0 * 1024.0);
        lblBateriaValor = new JLabel(obtenerEstadoBateria());

        tarjetaHardware.add(crearFilaSpec("Nombre del Equipo (Host)", obtenerNombreEquipo(), null));
        tarjetaHardware.add(crearFilaSpec("Procesador / Identificador", obtenerModeloProcesador(), null));
        tarjetaHardware.add(crearFilaSpec("Núcleos Disponibles (Hilos)", Runtime.getRuntime().availableProcessors() + " núcleos lógicos", null));
        tarjetaHardware.add(crearFilaSpec("Memoria RAM Física Instalada", String.format("%.2f GB (%d MB)", ramTotalGb, osBean.getTotalMemorySize() / (1024 * 1024)), null));
        tarjetaHardware.add(crearFilaSpec("Memoria Virtual / Swap Total", String.format("%.2f GB", swapTotalGb), null));
        tarjetaHardware.add(crearFilaSpec("Estado de Batería y Energía", "", lblBateriaValor));

        panel.add(tarjetaHardware);
        panel.add(Box.createVerticalStrut(16));

        TarjetaMac tarjetaMonitores = new TarjetaMac();
        tarjetaMonitores.setLayout(new BoxLayout(tarjetaMonitores, BoxLayout.Y_AXIS));
        tarjetaMonitores.add(crearTituloSeccion("Monitores y Pantallas Conectadas"));

        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        tarjetaMonitores.add(crearFilaSpec("Densidad de Píxeles (DPI)", dpi + " PPP / DPI", null));

        List<String[]> monitores = obtenerMonitoresConectados();
        for (String[] mon : monitores) {
            tarjetaMonitores.add(crearFilaSpec(mon[0], mon[1], null));
        }

        panel.add(tarjetaMonitores);
        panel.add(Box.createVerticalStrut(16));

        TarjetaMac tarjetaRed = new TarjetaMac();
        tarjetaRed.setLayout(new BoxLayout(tarjetaRed, BoxLayout.Y_AXIS));
        tarjetaRed.add(crearTituloSeccion("Red e Interfaces Activas"));

        tarjetaRed.add(crearFilaSpec("Dirección IP Local Principal", obtenerIpLocal(), null));
        List<String[]> interfaces = obtenerInterfacesRed();
        if (interfaces.isEmpty()) {
            tarjetaRed.add(crearFilaSpec("Adaptadores de Red", "No se detectaron interfaces activas", null));
        } else {
            for (String[] iface : interfaces) {
                tarjetaRed.add(crearFilaSpec(iface[0], iface[1], null));
            }
        }

        panel.add(tarjetaRed);
        panel.add(Box.createVerticalStrut(16));

        TarjetaMac tarjetaSoftware = new TarjetaMac();
        tarjetaSoftware.setLayout(new BoxLayout(tarjetaSoftware, BoxLayout.Y_AXIS));
        tarjetaSoftware.add(crearTituloSeccion("Sistema Operativo y Entorno"));

        lblUptimeValor = new JLabel("Calculando...");
        Locale loc = Locale.getDefault();

        tarjetaSoftware.add(crearFilaSpec("Sistema Operativo", System.getProperty("os.name") + " " + System.getProperty("os.version"), null));
        tarjetaSoftware.add(crearFilaSpec("Arquitectura del SO", System.getProperty("os.arch"), null));
        tarjetaSoftware.add(crearFilaSpec("Usuario Activo", System.getProperty("user.name"), null));
        tarjetaSoftware.add(crearFilaSpec("Directorio de Usuario", System.getProperty("user.home"), null));
        tarjetaSoftware.add(crearFilaSpec("Zona Horaria del Sistema", ZoneId.systemDefault().toString(), null));
        tarjetaSoftware.add(crearFilaSpec("Idioma / Región", loc.getDisplayLanguage() + " (" + loc.toLanguageTag() + ")", null));
        tarjetaSoftware.add(crearFilaSpec("Codificación de Caracteres", Charset.defaultCharset().displayName(), null));
        tarjetaSoftware.add(crearFilaSpec("Máquina Virtual Java (JVM)", System.getProperty("java.vm.name") + " v" + System.getProperty("java.version"), null));
        tarjetaSoftware.add(crearFilaSpec("Proveedor de Java", System.getProperty("java.vendor"), null));
        tarjetaSoftware.add(crearFilaSpec("Ruta de Instalación Java", System.getProperty("java.home"), null));
        tarjetaSoftware.add(crearFilaSpec("Tiempo de Actividad (Sesión)", "", lblUptimeValor));

        panel.add(tarjetaSoftware);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JLabel crearTituloSeccion(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setForeground(COLOR_TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return titulo;
    }

    private JPanel crearMiniTarjetaMetrica(String titulo, JLabel lblValor, Color color) {
        TarjetaMac card = new TarjetaMac();
        card.setLayout(new BorderLayout(0, 6));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setForeground(COLOR_TEXTO_DIM);
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 11));

        lblValor.setForeground(color);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 24));

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearFilaSpec(String clave, String valorFijo, JLabel labelDinamico) {
        JPanel fila = new JPanel(new BorderLayout(16, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        fila.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
                new EmptyBorder(7, 2, 7, 2)
        ));

        JLabel lblClave = new JLabel(clave);
        lblClave.setForeground(COLOR_TEXTO_DIM);
        lblClave.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel lblVal = (labelDinamico != null) ? labelDinamico : new JLabel(valorFijo);
        lblVal.setForeground(COLOR_TEXTO);
        lblVal.setFont(new Font("SansSerif", Font.PLAIN, 13));

        fila.add(lblClave, BorderLayout.WEST);
        fila.add(lblVal, BorderLayout.EAST);
        return fila;
    }

    private JPanel crearVistaRendimiento() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Rendimiento en Tiempo Real");
        titulo.setForeground(COLOR_TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(16));

        JPanel gridPrincipal = new JPanel(new GridLayout(1, 2, 18, 0));
        gridPrincipal.setBackground(COLOR_FONDO);
        gridPrincipal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));
        gridPrincipal.setAlignmentX(Component.LEFT_ALIGNMENT);

        TarjetaMac cardCpu = new TarjetaMac();
        cardCpu.setLayout(new BorderLayout(0, 10));
        JPanel topCpu = new JPanel(new BorderLayout());
        topCpu.setOpaque(false);
        JLabel tCpu = new JLabel("PROCESADOR (CPU)");
        tCpu.setForeground(COLOR_TEXTO_DIM);
        tCpu.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblCpuDetalle = new JLabel("App JVM: 0.0%");
        lblCpuDetalle.setForeground(COLOR_AZUL);
        lblCpuDetalle.setFont(new Font("SansSerif", Font.BOLD, 12));
        topCpu.add(tCpu, BorderLayout.WEST);
        topCpu.add(lblCpuDetalle, BorderLayout.EAST);

        gaugeCpu = new MedidorRadial("Carga Global", COLOR_AZUL);
        graficaCpu = new GraficaHistorial(COLOR_AZUL, 50);
        cardCpu.add(topCpu, BorderLayout.NORTH);
        cardCpu.add(gaugeCpu, BorderLayout.CENTER);
        cardCpu.add(graficaCpu, BorderLayout.SOUTH);

        TarjetaMac cardRam = new TarjetaMac();
        cardRam.setLayout(new BorderLayout(0, 10));
        JPanel topRam = new JPanel(new BorderLayout());
        topRam.setOpaque(false);
        JLabel tRam = new JLabel("MEMORIA RAM FÍSICA");
        tRam.setForeground(COLOR_TEXTO_DIM);
        tRam.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblRamDetalle = new JLabel("0.0 / 0.0 GB");
        lblRamDetalle.setForeground(COLOR_MORADO);
        lblRamDetalle.setFont(new Font("SansSerif", Font.BOLD, 12));
        topRam.add(tRam, BorderLayout.WEST);
        topRam.add(lblRamDetalle, BorderLayout.EAST);

        gaugeRam = new MedidorRadial("RAM Ocupada", COLOR_MORADO);
        graficaRam = new GraficaHistorial(COLOR_MORADO, 50);
        cardRam.add(topRam, BorderLayout.NORTH);
        cardRam.add(gaugeRam, BorderLayout.CENTER);
        cardRam.add(graficaRam, BorderLayout.SOUTH);

        gridPrincipal.add(cardCpu);
        gridPrincipal.add(cardRam);
        panel.add(gridPrincipal);
        panel.add(Box.createVerticalStrut(14));

        TarjetaMac cardSwap = new TarjetaMac();
        cardSwap.setLayout(new BorderLayout(0, 8));
        cardSwap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        cardSwap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topSwap = new JPanel(new BorderLayout());
        topSwap.setOpaque(false);
        JLabel tSwap = new JLabel("MEMORIA VIRTUAL / SWAP DEL SISTEMA");
        tSwap.setForeground(COLOR_TEXTO_DIM);
        tSwap.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblSwapDetalle = new JLabel("0.0 / 0.0 GB");
        lblSwapDetalle.setForeground(COLOR_NARANJA);
        lblSwapDetalle.setFont(new Font("SansSerif", Font.BOLD, 12));
        topSwap.add(tSwap, BorderLayout.WEST);
        topSwap.add(lblSwapDetalle, BorderLayout.EAST);

        barraSwap = new BarraProgresoMac(COLOR_NARANJA);
        cardSwap.add(topSwap, BorderLayout.NORTH);
        cardSwap.add(barraSwap, BorderLayout.CENTER);
        panel.add(cardSwap);
        panel.add(Box.createVerticalStrut(14));

        TarjetaMac cardHeap = new TarjetaMac();
        cardHeap.setLayout(new BorderLayout(0, 8));
        cardHeap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        cardHeap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topHeap = new JPanel(new BorderLayout());
        topHeap.setOpaque(false);
        JLabel tHeap = new JLabel("MEMORIA ASIGNADA A JAVA (JVM HEAP)");
        tHeap.setForeground(COLOR_TEXTO_DIM);
        tHeap.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblJvmHeapDetalle = new JLabel("0 MB / 0 MB");
        lblJvmHeapDetalle.setForeground(COLOR_CIAN);
        lblJvmHeapDetalle.setFont(new Font("SansSerif", Font.BOLD, 12));
        topHeap.add(tHeap, BorderLayout.WEST);
        topHeap.add(lblJvmHeapDetalle, BorderLayout.EAST);

        barraJvmHeap = new BarraProgresoMac(COLOR_CIAN);

        JButton btnGc = crearBotonPildora("Liberar Memoria (GC)", COLOR_TARJETA_SECUNDARIA, COLOR_TEXTO);
        btnGc.addActionListener(e -> {
            System.gc();
            actualizarMetricas();
        });

        JPanel bottomHeap = new JPanel(new BorderLayout());
        bottomHeap.setOpaque(false);
        JLabel hintGc = new JLabel("Ejecuta el recolector de basura de Java para liberar memoria no utilizada.");
        hintGc.setForeground(COLOR_TEXTO_DIM);
        hintGc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bottomHeap.add(hintGc, BorderLayout.WEST);
        bottomHeap.add(btnGc, BorderLayout.EAST);

        cardHeap.add(topHeap, BorderLayout.NORTH);
        cardHeap.add(barraJvmHeap, BorderLayout.CENTER);
        cardHeap.add(bottomHeap, BorderLayout.SOUTH);

        panel.add(cardHeap);
        return panel;
    }

    private JPanel crearVistaAlmacenamiento() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO);

        JLabel titulo = new JLabel("Discos y Volúmenes Montados");
        titulo.setForeground(COLOR_TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));

        JButton btnRefrescar = crearBotonPildora("Escanear Discos", COLOR_AZUL, Color.WHITE);
        btnRefrescar.addActionListener(e -> refrescarDiscos());

        header.add(titulo, BorderLayout.WEST);
        header.add(btnRefrescar, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        contenedorDiscos = new JPanel();
        contenedorDiscos.setLayout(new BoxLayout(contenedorDiscos, BoxLayout.Y_AXIS));
        contenedorDiscos.setBackground(COLOR_FONDO);

        JScrollPane scroll = new JScrollPane(contenedorDiscos);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        refrescarDiscos();
        return panel;
    }

    private void refrescarDiscos() {
        contenedorDiscos.removeAll();
        File[] raices = File.listRoots();
        Color[] paletaDiscos = {COLOR_AZUL, COLOR_MORADO, COLOR_VERDE, COLOR_NARANJA, COLOR_CIAN};

        long sumaTotalGlobal = 0;
        long sumaUsadoGlobal = 0;

        int idx = 0;
        for (File disco : raices) {
            long total = disco.getTotalSpace();
            long libre = disco.getUsableSpace();
            long usado = total - libre;

            sumaTotalGlobal += total;
            sumaUsadoGlobal += usado;

            TarjetaMac card = new TarjetaMac();
            card.setLayout(new BorderLayout(0, 10));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

            JPanel filaSup = new JPanel(new BorderLayout());
            filaSup.setOpaque(false);

            String permisos = (disco.canRead() ? "Lectura" : "") + (disco.canWrite() ? " / Escritura" : "");
            JLabel lblUnidad = new JLabel("Unidad " + disco.getAbsolutePath() + "   (" + permisos + ")");
            lblUnidad.setForeground(COLOR_TEXTO);
            lblUnidad.setFont(new Font("SansSerif", Font.BOLD, 15));

            Color colorBarra = paletaDiscos[idx % paletaDiscos.length];
            double porcentaje = total > 0 ? (double) usado / total * 100.0 : 0;

            JLabel lblPorcentaje = new JLabel(total > 0 ? String.format("%.1f%% ocupado", porcentaje) : "Sin medio insertado");
            lblPorcentaje.setForeground(porcentaje > 85 ? COLOR_ROJO : colorBarra);
            lblPorcentaje.setFont(new Font("SansSerif", Font.BOLD, 13));

            filaSup.add(lblUnidad, BorderLayout.WEST);
            filaSup.add(lblPorcentaje, BorderLayout.EAST);

            BarraProgresoMac barra = new BarraProgresoMac(colorBarra);
            barra.setProgreso(total > 0 ? porcentaje / 100.0 : 0);

            JPanel filaInf = new JPanel(new BorderLayout());
            filaInf.setOpaque(false);

            String textoUsado = total > 0
                    ? String.format("Usado: %s  •  Disponible: %s", formatearBytes(usado), formatearBytes(libre))
                    : "Unidad extraíble vacía";
            String textoTotal = total > 0 ? "Capacidad Total: " + formatearBytes(total) : "";

            JLabel lblDetalles = new JLabel(textoUsado);
            lblDetalles.setForeground(COLOR_TEXTO_DIM);
            lblDetalles.setFont(new Font("SansSerif", Font.PLAIN, 13));

            JLabel lblCapacidad = new JLabel(textoTotal);
            lblCapacidad.setForeground(COLOR_TEXTO);
            lblCapacidad.setFont(new Font("SansSerif", Font.BOLD, 13));

            filaInf.add(lblDetalles, BorderLayout.WEST);
            filaInf.add(lblCapacidad, BorderLayout.EAST);

            card.add(filaSup, BorderLayout.NORTH);
            card.add(barra, BorderLayout.CENTER);
            card.add(filaInf, BorderLayout.SOUTH);

            contenedorDiscos.add(card);
            contenedorDiscos.add(Box.createVerticalStrut(14));
            idx++;
        }

        if (lblResumenDiscos != null && sumaTotalGlobal > 0) {
            lblResumenDiscos.setText(formatearBytes(sumaUsadoGlobal) + " / " + formatearBytes(sumaTotalGlobal));
        }

        contenedorDiscos.revalidate();
        contenedorDiscos.repaint();
    }

    private JPanel crearVistaRed() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Diagnóstico de Red y Latencia (Ping)");
        titulo.setForeground(COLOR_TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));

        lblEstadoConexion = new JLabel("Listo para probar");
        lblEstadoConexion.setForeground(COLOR_TEXTO_DIM);
        lblEstadoConexion.setFont(new Font("SansSerif", Font.BOLD, 13));

        header.add(titulo, BorderLayout.WEST);
        header.add(lblEstadoConexion, BorderLayout.EAST);
        panel.add(header);
        panel.add(Box.createVerticalStrut(16));

        TarjetaMac cardControl = new TarjetaMac();
        cardControl.setLayout(new BorderLayout(12, 0));
        cardControl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        cardControl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel izquierdaCtrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        izquierdaCtrl.setOpaque(false);

        JLabel lblDestino = new JLabel("Servidor / IP:");
        lblDestino.setForeground(COLOR_TEXTO_DIM);
        lblDestino.setFont(new Font("SansSerif", Font.BOLD, 13));

        txtHostPing = new JTextField("8.8.8.8", 16);
        txtHostPing.setBackground(COLOR_TARJETA_SECUNDARIA);
        txtHostPing.setForeground(COLOR_TEXTO);
        txtHostPing.setCaretColor(Color.WHITE);
        txtHostPing.setFont(new Font("Monospaced", Font.BOLD, 13));
        txtHostPing.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        izquierdaCtrl.add(lblDestino);
        izquierdaCtrl.add(txtHostPing);
        izquierdaCtrl.add(crearPresetPing("Google DNS", "8.8.8.8"));
        izquierdaCtrl.add(crearPresetPing("Cloudflare", "1.1.1.1"));
        izquierdaCtrl.add(crearPresetPing("Google Web", "google.com"));

        btnIniciarPing = crearBotonPildora("Iniciar Test", COLOR_VERDE, Color.BLACK);
        btnIniciarPing.addActionListener(e -> alternarTestPing());

        cardControl.add(izquierdaCtrl, BorderLayout.WEST);
        cardControl.add(btnIniciarPing, BorderLayout.EAST);
        panel.add(cardControl);
        panel.add(Box.createVerticalStrut(16));

        JPanel filaMetricasPing = new JPanel(new GridLayout(1, 4, 12, 0));
        filaMetricasPing.setBackground(COLOR_FONDO);
        filaMetricasPing.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        filaMetricasPing.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPingActual = new JLabel("-- ms");
        lblPingPromedio = new JLabel("-- ms");
        lblPingMinMax = new JLabel("-- / --");
        lblPingPerdida = new JLabel("0%");

        filaMetricasPing.add(crearMiniTarjetaMetrica("LATENCIA ACTUAL", lblPingActual, COLOR_CIAN));
        filaMetricasPing.add(crearMiniTarjetaMetrica("PROMEDIO", lblPingPromedio, COLOR_AZUL));
        filaMetricasPing.add(crearMiniTarjetaMetrica("MÍN / MÁX (ms)", lblPingMinMax, COLOR_MORADO));
        filaMetricasPing.add(crearMiniTarjetaMetrica("PÉRDIDA PAQUETES", lblPingPerdida, COLOR_VERDE));

        panel.add(filaMetricasPing);
        panel.add(Box.createVerticalStrut(16));

        TarjetaMac cardGraficaPing = new TarjetaMac();
        cardGraficaPing.setLayout(new BorderLayout(0, 8));
        cardGraficaPing.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));
        cardGraficaPing.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitGraf = new JLabel("HISTORIAL DE LATENCIA (ESCALA 0 - 200 ms)");
        lblTitGraf.setForeground(COLOR_TEXTO_DIM);
        lblTitGraf.setFont(new Font("SansSerif", Font.BOLD, 11));

        graficaPing = new GraficaHistorial(COLOR_CIAN, 45);
        graficaPing.setPreferredSize(new Dimension(500, 105));

        cardGraficaPing.add(lblTitGraf, BorderLayout.NORTH);
        cardGraficaPing.add(graficaPing, BorderLayout.CENTER);
        panel.add(cardGraficaPing);
        panel.add(Box.createVerticalStrut(16));

        TarjetaMac cardConsola = new TarjetaMac();
        cardConsola.setLayout(new BorderLayout(0, 8));
        cardConsola.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblConsolaTit = new JLabel("REGISTRO DE RESPUESTAS EN VIVO");
        lblConsolaTit.setForeground(COLOR_TEXTO_DIM);
        lblConsolaTit.setFont(new Font("SansSerif", Font.BOLD, 11));

        modeloLogPing = new DefaultListModel<>();
        JList<String> listaLogs = new JList<>(modeloLogPing);
        listaLogs.setBackground(new Color(20, 20, 23));
        listaLogs.setForeground(COLOR_TEXTO);
        listaLogs.setFont(new Font("Monospaced", Font.PLAIN, 12));
        listaLogs.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scrollLogs = new JScrollPane(listaLogs);
        scrollLogs.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));

        cardConsola.add(lblConsolaTit, BorderLayout.NORTH);
        cardConsola.add(scrollLogs, BorderLayout.CENTER);
        panel.add(cardConsola);

        timerPing = new Timer(1000, e -> ejecutarUnPing());
        return panel;
    }

    private JButton crearPresetPing(String etiqueta, String host) {
        JButton btn = crearBotonPildora(etiqueta, COLOR_TARJETA_SECUNDARIA, COLOR_TEXTO_DIM);
        btn.addActionListener(e -> {
            txtHostPing.setText(host);
            if (!pingCorriendo) alternarTestPing();
        });
        return btn;
    }

    private void alternarTestPing() {
        if (!pingCorriendo) {
            String host = txtHostPing.getText().trim();
            if (host.isEmpty()) return;

            pingsEnviados = 0;
            pingsPerdidos = 0;
            sumaLatencia = 0;
            minLatencia = Long.MAX_VALUE;
            maxLatencia = 0;
            modeloLogPing.clear();

            pingCorriendo = true;
            txtHostPing.setEnabled(false);
            btnIniciarPing.setText("Detener Test");
            btnIniciarPing.setBackground(COLOR_ROJO);
            btnIniciarPing.setForeground(Color.WHITE);
            lblEstadoConexion.setText("Probando conexión a " + host + "...");
            lblEstadoConexion.setForeground(COLOR_VERDE);

            ejecutarUnPing();
            timerPing.start();
        } else {
            pingCorriendo = false;
            timerPing.stop();
            txtHostPing.setEnabled(true);
            btnIniciarPing.setText("Iniciar Test");
            btnIniciarPing.setForeground(Color.BLACK);
            lblEstadoConexion.setText("Test detenido");
            lblEstadoConexion.setForeground(COLOR_TEXTO_DIM);
        }
    }

    private void ejecutarUnPing() {
        final String host = txtHostPing.getText().trim();
        new SwingWorker<Long, Void>() {
            private String ipResuelta = "";

            @Override
            protected Long doInBackground() {
                try {
                    InetAddress inet = InetAddress.getByName(host);
                    ipResuelta = inet.getHostAddress();
                    long inicio = System.nanoTime();
                    boolean alcanzable = inet.isReachable(2000);
                    long duracionMs = (System.nanoTime() - inicio) / 1_000_000L;
                    return alcanzable ? Math.max(1L, duracionMs) : -1L;
                } catch (Exception ex) {
                    return -1L;
                }
            }

            @Override
            protected void done() {
                if (!pingCorriendo) return;
                try {
                    long ms = get();
                    pingsEnviados++;
                    String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    if (ms >= 0) {
                        sumaLatencia += ms;
                        if (ms < minLatencia) minLatencia = ms;
                        if (ms > maxLatencia) maxLatencia = ms;

                        int exitosos = pingsEnviados - pingsPerdidos;
                        long promedio = exitosos > 0 ? sumaLatencia / exitosos : ms;

                        lblPingActual.setText(ms + " ms");
                        lblPingActual.setForeground(ms < 60 ? COLOR_VERDE : (ms < 130 ? COLOR_NARANJA : COLOR_ROJO));
                        lblPingPromedio.setText(promedio + " ms");
                        lblPingMinMax.setText(minLatencia + " / " + maxLatencia);

                        // Escala de gráfica sobre 200 ms máx
                        double valorGrafica = Math.min(100.0, (ms / 200.0) * 100.0);
                        graficaPing.agregarPunto(valorGrafica);

                        modeloLogPing.add(0, String.format("[%s] Respuesta desde %s (%s): tiempo = %d ms", hora, host, ipResuelta, ms));
                    } else {
                        pingsPerdidos++;
                        lblPingActual.setText("Timeout");
                        lblPingActual.setForeground(COLOR_ROJO);
                        graficaPing.agregarPunto(0);
                        modeloLogPing.add(0, String.format("[%s] Tiempo de espera agotado para %s", hora, host));
                    }

                    double pctPerdida = (double) pingsPerdidos / pingsEnviados * 100.0;
                    lblPingPerdida.setText(String.format("%.0f%%", pctPerdida));
                    lblPingPerdida.setForeground(pctPerdida == 0 ? COLOR_VERDE : COLOR_ROJO);

                    if (modeloLogPing.size() > 30) {
                        modeloLogPing.removeElementAt(modeloLogPing.size() - 1);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void exportarInformeTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Informe Técnico del Sistema");
        String fechaArchivo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        chooser.setSelectedFile(new File("Informe_Sistema_" + obtenerNombreEquipo() + "_" + fechaArchivo + ".txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de Texto (*.txt)", "txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(archivo))) {
                out.println(" INFORME TÉCNICO DEL SISTEMA");
                out.println(" Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                out.println("\n");

                out.println("[1. IDENTIFICACIÓN Y SISTEMA OPERATIVO]");
                out.println("  Nombre del Equipo (Host) : " + obtenerNombreEquipo());
                out.println("  Sistema Operativo        : " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
                out.println("  Arquitectura             : " + System.getProperty("os.arch"));
                out.println("  Usuario Activo           : " + System.getProperty("user.name"));
                out.println("  Directorio Personal      : " + System.getProperty("user.home"));
                out.println("  Zona Horaria             : " + ZoneId.systemDefault());
                out.println("  Idioma / Región          : " + Locale.getDefault().getDisplayName());
                out.println("  Tiempo Actividad Sesión  : " + formatearDuracion(runtimeBean.getUptime()) + "\n");

                long ramTotal = osBean.getTotalMemorySize();
                long ramLibre = osBean.getFreeMemorySize();
                long ramUsada = ramTotal - ramLibre;
                out.println("[2. PROCESADOR, MEMORIA Y ENERGÍA]");
                out.println("  Procesador               : " + obtenerModeloProcesador());
                out.println("  Núcleos Lógicos          : " + Runtime.getRuntime().availableProcessors());
                out.println("  Carga Actual CPU         : " + String.format("%.1f%%", Math.max(0, osBean.getCpuLoad() * 100.0)));
                out.println("  Memoria RAM Total        : " + formatearBytes(ramTotal));
                out.println("  Memoria RAM En Uso       : " + formatearBytes(ramUsada));
                out.println("  Memoria RAM Disponible   : " + formatearBytes(ramLibre));
                out.println("  Memoria Virtual (Swap)   : " + formatearBytes(osBean.getTotalSwapSpaceSize()));
                out.println("  Estado de Batería        : " + obtenerEstadoBateria() + "\n");

                out.println("[3. MONITORES Y PANTALLAS]");
                out.println("  Densidad de Píxeles      : " + Toolkit.getDefaultToolkit().getScreenResolution() + " DPI");
                for (String[] mon : obtenerMonitoresConectados()) {
                    out.println("  " + mon[0] + " -> " + mon[1]);
                }
                out.println();

                out.println("[4. ALMACENAMIENTO Y UNIDADES]");
                for (File disco : File.listRoots()) {
                    long total = disco.getTotalSpace();
                    long libre = disco.getUsableSpace();
                    long usado = total - libre;
                    if (total > 0) {
                        out.printf("  Unidad %-8s | Total: %-10s | Usado: %-10s | Libre: %-10s%n",
                                disco.getAbsolutePath(), formatearBytes(total), formatearBytes(usado), formatearBytes(libre));
                    } else {
                        out.printf("  Unidad %-8s | Sin medio insertado%n", disco.getAbsolutePath());
                    }
                }
                out.println();

                out.println("[5. RED E INTERFACES ACTIVAS]");
                out.println("  IP Local Principal       : " + obtenerIpLocal());
                for (String[] iface : obtenerInterfacesRed()) {
                    out.println("  " + iface[0] + " -> " + iface[1]);
                }
                out.println();

                out.println("[6. ENTORNO JAVA (JVM)]");
                out.println("  Versión Java             : " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
                out.println("  Proveedor                : " + System.getProperty("java.vendor"));
                out.println("  Ruta Instalación         : " + System.getProperty("java.home"));

                JOptionPane.showMessageDialog(
                        this,
                        "Informe guardado exitosamente en:\n" + archivo.getAbsolutePath(),
                        "Informe Exportado",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo guardar el informe: " + ex.getMessage(),
                        "Error al Exportar",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void iniciarActualizacionEnVivo() {
        Timer timer = new Timer(900, e -> actualizarMetricas());
        timer.setInitialDelay(150);
        timer.start();
    }

    private void actualizarMetricas() {
        double cpuSis = Math.max(0, osBean.getCpuLoad() * 100.0);
        double cpuJvm = Math.max(0, osBean.getProcessCpuLoad() * 100.0);

        gaugeCpu.setValor(cpuSis);
        graficaCpu.agregarPunto(cpuSis);
        lblCpuDetalle.setText(String.format("App JVM: %.1f%%", cpuJvm));
        lblResumenCpu.setText(String.format("%.0f%%", cpuSis));

        long ramTotal = osBean.getTotalMemorySize();
        long ramLibre = osBean.getFreeMemorySize();
        long ramUsada = ramTotal - ramLibre;
        double pctRam = ramTotal > 0 ? (double) ramUsada / ramTotal * 100.0 : 0;

        double gbUsada = ramUsada / (1024.0 * 1024.0 * 1024.0);
        double gbTotal = ramTotal / (1024.0 * 1024.0 * 1024.0);

        gaugeRam.setValor(pctRam);
        graficaRam.agregarPunto(pctRam);
        lblRamDetalle.setText(String.format("%.1f / %.1f GB", gbUsada, gbTotal));
        lblResumenRam.setText(String.format("%.1f / %.0f GB", gbUsada, gbTotal));

        long swapTotal = osBean.getTotalSwapSpaceSize();
        long swapLibre = osBean.getFreeSwapSpaceSize();
        long swapUsada = Math.max(0, swapTotal - swapLibre);
        double swapUsadaGb = swapUsada / (1024.0 * 1024.0 * 1024.0);
        double swapTotalGb = swapTotal / (1024.0 * 1024.0 * 1024.0);
        lblSwapDetalle.setText(String.format("%.2f / %.2f GB", swapUsadaGb, swapTotalGb));
        barraSwap.setProgreso(swapTotal > 0 ? (double) swapUsada / swapTotal : 0);

        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long heapUsadoMb = heap.getUsed() / (1024 * 1024);
        long heapMaxMb = heap.getMax() / (1024 * 1024);
        lblJvmHeapDetalle.setText(heapUsadoMb + " MB usados de " + heapMaxMb + " MB máx");
        barraJvmHeap.setProgreso(heapMaxMb > 0 ? (double) heapUsadoMb / heapMaxMb : 0);


        lblUptimeValor.setText(formatearDuracion(runtimeBean.getUptime()));
    }

    private List<String[]> obtenerMonitoresConectados() {
        List<String[]> lista = new ArrayList<>();
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] dispositivos = ge.getScreenDevices();
            GraphicsDevice principal = ge.getDefaultScreenDevice();

            for (int i = 0; i < dispositivos.length; i++) {
                GraphicsDevice gd = dispositivos[i];
                DisplayMode dm = gd.getDisplayMode();
                boolean esPrincipal = (gd == principal);

                String titulo = "Pantalla " + (i + 1) + (esPrincipal ? " (Principal)" : " (Externa)");
                String refresco = dm.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN ? "Auto" : dm.getRefreshRate() + " Hz";
                String colorDepth = dm.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI ? "32-bit" : dm.getBitDepth() + "-bit";
                String detalle = dm.getWidth() + " × " + dm.getHeight() + " px  •  " + refresco + "  •  Color " + colorDepth;

                lista.add(new String[]{titulo, detalle});
            }
        } catch (Exception e) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            lista.add(new String[]{"Pantalla Principal", d.width + " × " + d.height + " px"});
        }
        return lista;
    }

    private String obtenerEstadoBateria() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Process p = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                        "$b = Get-CimInstance Win32_Battery; if ($b) { \"$($b.EstimatedChargeRemaining)%|$($b.BatteryStatus)\" } else { 'AC' }")
                        .start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String linea = br.readLine();
                    if (linea != null && linea.contains("|")) {
                        String[] partes = linea.trim().split("\\|");
                        String carga = partes[0];
                        String estadoCod = partes[1];
                        String estadoTexto = switch (estadoCod) {
                            case "2", "6", "7", "8", "9" -> "Cargando (Conectado a AC)";
                            case "1" -> "En uso (Descargando)";
                            case "3" -> "Carga completa";
                            default -> "Conectado";
                        };
                        return carga + "%  •  " + estadoTexto;
                    }
                }
                return "Equipo de escritorio / Sin batería (Corriente AC)";
            } else if (os.contains("mac")) {
                Process p = new ProcessBuilder("pmset", "-g", "batt").start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        if (linea.contains("%")) {
                            return linea.replaceAll(".*\\t", "").trim();
                        }
                    }
                }
            } else if (os.contains("linux")) {
                File cap = new File("/sys/class/power_supply/BAT0/capacity");
                File stat = new File("/sys/class/power_supply/BAT0/status");
                if (cap.exists()) {
                    String pct = java.nio.file.Files.readString(cap.toPath()).trim();
                    String st = stat.exists() ? java.nio.file.Files.readString(stat.toPath()).trim() : "";
                    return pct + "%  •  " + st;
                }
            }
        } catch (Exception ignored) {
        }
        return "Corriente Directa (AC) / No detectada";
    }

    private String obtenerNombreEquipo() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            String envHost = System.getenv("COMPUTERNAME");
            return envHost != null ? envHost : "Estación de Trabajo";
        }
    }

    private String obtenerModeloProcesador() {
        String procId = System.getenv("PROCESSOR_IDENTIFIER");
        if (procId != null && !procId.isBlank()) {
            return procId;
        }
        String arch = System.getProperty("os.arch");
        return "Procesador " + arch.toUpperCase() + " (" + Runtime.getRuntime().availableProcessors() + " núcleos)";
    }

    private String obtenerIpLocal() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private List<String[]> obtenerInterfacesRed() {
        List<String[]> lista = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netInt : Collections.list(nets)) {
                if (!netInt.isUp() || netInt.isLoopback()) continue;

                String ipV4 = "";
                for (InetAddress addr : Collections.list(netInt.getInetAddresses())) {
                    if (addr instanceof Inet4Address) {
                        ipV4 = addr.getHostAddress();
                        break;
                    }
                }
                if (ipV4.isEmpty()) continue;

                byte[] macBytes = netInt.getHardwareAddress();
                String mac = "Sin MAC";
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < macBytes.length; i++) {
                        sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? ":" : ""));
                    }
                    mac = sb.toString();
                }

                lista.add(new String[]{
                        netInt.getDisplayName() + " (" + netInt.getName() + ")",
                        "IP: " + ipV4 + "  •  MAC: " + mac
                });
            }
        } catch (Exception ignored) {
        }
        return lista;
    }

    private String formatearBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        if (gb < 1024) return String.format("%.1f GB", gb);
        return String.format("%.2f TB", gb / 1024.0);
    }

    private String formatearDuracion(long millis) {
        long seg = millis / 1000;
        long horas = seg / 3600;
        long min = (seg % 3600) / 60;
        long s = seg % 60;
        if (horas > 0) return String.format("%dh %02dm %02ds", horas, min, s);
        return String.format("%02dm %02ds", min, s);
    }

    private JButton crearBotonPildora(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color actual = getBackground() != null && !getBackground().equals(UIManager.getColor("Button.background"))
                        ? getBackground() : bg;
                g2.setColor(getModel().isRollover() ? actual.brighter() : actual);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private static class BotonSidebar extends JButton {
        private final Color acento;
        private boolean activo;
        private boolean hover = false;

        public BotonSidebar(String texto, Color acento, boolean activo) {
            super(texto);
            this.acento = acento;
            this.activo = activo;

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(205, 38));
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setForeground(activo ? Color.WHITE : COLOR_TEXTO_DIM);
            setBorder(new EmptyBorder(0, 32, 0, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
            setForeground(activo ? Color.WHITE : COLOR_TEXTO_DIM);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (activo) {
                g2.setColor(new Color(44, 44, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hover) {
                g2.setColor(new Color(34, 34, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            g2.setColor(activo ? acento : new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 140));
            g2.fillOval(12, (getHeight() - 10) / 2, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class TarjetaMac extends JPanel {
        public TarjetaMac() {
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(COLOR_TARJETA);
            g2.fill(rect);

            g2.setColor(COLOR_BORDE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(rect);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class IconoComputador extends JPanel {
        public IconoComputador() {
            setPreferredSize(new Dimension(88, 88));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(58, 58, 64));
            g2.fillRoundRect(10, 12, 68, 46, 10, 10);

            GradientPaint pantallaGrad = new GradientPaint(14, 16, COLOR_AZUL, 74, 54, COLOR_MORADO);
            g2.setPaint(pantallaGrad);
            g2.fillRoundRect(14, 16, 60, 38, 6, 6);

            g2.setColor(new Color(142, 142, 147));
            g2.fillRoundRect(4, 60, 80, 6, 4, 4);

            g2.dispose();
        }
    }

    private static class MedidorRadial extends JPanel {
        private double porcentaje = 0;
        private final String subtitulo;
        private final Color colorAcento;

        public MedidorRadial(String subtitulo, Color colorAcento) {
            this.subtitulo = subtitulo;
            this.colorAcento = colorAcento;
            setOpaque(false);
            setPreferredSize(new Dimension(160, 150));
        }

        public void setValor(double porcentaje) {
            this.porcentaje = Math.max(0, Math.min(100, porcentaje));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int diametro = Math.min(getWidth(), getHeight()) - 18;
            int x = (getWidth() - diametro) / 2;
            int y = (getHeight() - diametro) / 2 + 6;
            float grosor = 11f;

            g2.setColor(new Color(44, 44, 50));
            g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Double(x, y, diametro, diametro, 225, -270, Arc2D.OPEN));

            Color actual = porcentaje > 85 ? COLOR_ROJO : colorAcento;
            g2.setColor(actual);
            g2.draw(new Arc2D.Double(x, y, diametro, diametro, 225, -270.0 * (porcentaje / 100.0), Arc2D.OPEN));

            String txt = String.format("%.0f%%", porcentaje);
            g2.setColor(COLOR_TEXTO);
            g2.setFont(new Font("SansSerif", Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, y + (diametro / 2) + 6);

            g2.setColor(COLOR_TEXTO_DIM);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics fmSub = g2.getFontMetrics();
            g2.drawString(subtitulo, (getWidth() - fmSub.stringWidth(subtitulo)) / 2, y + (diametro / 2) + 26);

            g2.dispose();
        }
    }

    private static class GraficaHistorial extends JPanel {
        private final LinkedList<Double> historial = new LinkedList<>();
        private final int maxPuntos;
        private final Color colorLinea;

        public GraficaHistorial(Color colorLinea, int maxPuntos) {
            this.colorLinea = colorLinea;
            this.maxPuntos = maxPuntos;
            setOpaque(false);
            setPreferredSize(new Dimension(280, 72));
            for (int i = 0; i < maxPuntos; i++) historial.add(0.0);
        }

        public void agregarPunto(double valor) {
            if (historial.size() >= maxPuntos) historial.removeFirst();
            historial.add(Math.max(0, Math.min(100, valor)));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(20, 20, 23));
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            g2.setColor(new Color(38, 38, 44));
            g2.drawLine(0, h / 2, w, h / 2);

            GeneralPath linea = new GeneralPath();
            GeneralPath area = new GeneralPath();
            double pasoX = (double) w / (maxPuntos - 1);
            area.moveTo(0, h);

            for (int i = 0; i < historial.size(); i++) {
                double px = i * pasoX;
                double py = h - ((historial.get(i) / 100.0) * (h - 10)) - 5;
                if (i == 0) linea.moveTo(px, py);
                else linea.lineTo(px, py);
                area.lineTo(px, py);
            }
            area.lineTo(w, h);
            area.closePath();

            g2.setPaint(new GradientPaint(
                    0, 0, new Color(colorLinea.getRed(), colorLinea.getGreen(), colorLinea.getBlue(), 85),
                    0, h, new Color(colorLinea.getRed(), colorLinea.getGreen(), colorLinea.getBlue(), 5)
            ));
            g2.fill(area);

            g2.setColor(colorLinea);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linea);
            g2.dispose();
        }
    }

    private static class BarraProgresoMac extends JPanel {
        private double progreso = 0;
        private final Color colorBarra;

        public BarraProgresoMac(Color colorBarra) {
            this.colorBarra = colorBarra;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 18));
        }

        public void setProgreso(double progreso) {
            this.progreso = Math.max(0.0, Math.min(1.0, progreso));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = 12;
            int y = (getHeight() - h) / 2;

            g2.setColor(new Color(44, 44, 50));
            g2.fillRoundRect(0, y, w, h, h, h);

            int ancho = (int) (w * progreso);
            if (ancho > 0) {
                g2.setColor(progreso > 0.85 ? COLOR_ROJO : colorBarra);
                g2.fillRoundRect(0, y, Math.max(h, ancho), h, h, h);
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MonitorSistema app = new MonitorSistema();
            app.setVisible(true);
        });
    }
}