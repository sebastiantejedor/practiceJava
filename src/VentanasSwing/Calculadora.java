package VentanasSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Calculadora extends JFrame {

    private JLabel pantalla;
    private double primerNumero = 0;
    private String operadorActual = "";
    private boolean iniciarNuevoNumero = true;
    private BotonRedondo botonOperadorActivo = null;

    // Paleta de colores
    private final Color COLOR_FONDO = new Color(0, 0, 0);
    private final Color COLOR_NUMEROS = new Color(51, 51, 51);
    private final Color COLOR_FUNCIONES = new Color(165, 165, 165);
    private final Color COLOR_OPERADORES = new Color(255, 159, 10);

    public Calculadora() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Calculadora");
        setSize(350, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {

        // Panel superior para el display
        JPanel panelPantalla = new JPanel(new BorderLayout());
        panelPantalla.setBackground(COLOR_FONDO);
        panelPantalla.setBorder(new EmptyBorder(40, 24, 15, 24));

        pantalla = new JLabel("0", SwingConstants.RIGHT);
        pantalla.setForeground(Color.WHITE);
        pantalla.setFont(new Font("SansSerif", Font.PLAIN, 64));
        panelPantalla.add(pantalla, BorderLayout.SOUTH);

        add(panelPantalla, BorderLayout.NORTH);

        // Panel inferior para los botones con GridBagLayout
        JPanel panelBotones = new JPanel(new GridBagLayout());
        panelBotones.setBackground(COLOR_FONDO);
        panelBotones.setBorder(new EmptyBorder(10, 16, 24, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[][] disposicion = {
                {"AC", "±", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "−"},
                {"1", "2", "3", "+"},
                {"0", ".", "="}
        };

        for (int fila = 0; fila < disposicion.length; fila++) {
            int colVisual = 0;
            for (int col = 0; col < disposicion[fila].length; col++) {
                String texto = disposicion[fila][col];
                gbc.gridy = fila;
                gbc.gridx = colVisual;

                if (texto.equals("0")) {
                    gbc.gridwidth = 2;
                    colVisual += 2;
                } else {
                    gbc.gridwidth = 1;
                    colVisual += 1;
                }

                BotonRedondo boton = crearBotonEstilizado(texto);
                panelBotones.add(boton, gbc);
            }
        }

        add(panelBotones, BorderLayout.CENTER);
        configurarTeclado();
    }

    private BotonRedondo crearBotonEstilizado(String texto) {
        Color bg;
        Color fg;
        boolean esOperador = false;

        if ("AC±%".contains(texto)) {
            bg = COLOR_FUNCIONES;
            fg = Color.BLACK;
        } else if ("÷×−+=".contains(texto)) {
            bg = COLOR_OPERADORES;
            fg = Color.WHITE;
            esOperador = !texto.equals("=");
        } else {
            bg = COLOR_NUMEROS;
            fg = Color.WHITE;
        }

        BotonRedondo boton = new BotonRedondo(texto, bg, fg, texto.equals("0"));
        final boolean opFlag = esOperador;

        boton.addActionListener(e -> procesarEntrada(texto, opFlag ? boton : null));
        return boton;
    }

    private void procesarEntrada(String comando, BotonRedondo botonFuente) {
        if ("0123456789".contains(comando)) {
            if (iniciarNuevoNumero) {
                pantalla.setText(comando);
                iniciarNuevoNumero = false;
            } else {
                if (pantalla.getText().replace(".", "").replace("-", "").length() < 9) {
                    pantalla.setText(pantalla.getText().equals("0") ? comando : pantalla.getText() + comando);
                }
            }
            desactivarEstadoOperador();
        } else if (comando.equals(".")) {
            if (iniciarNuevoNumero) {
                pantalla.setText("0.");
                iniciarNuevoNumero = false;
            } else if (!pantalla.getText().contains(".")) {
                pantalla.setText(pantalla.getText() + ".");
            }
        } else if (comando.equals("AC")) {
            pantalla.setText("0");
            primerNumero = 0;
            operadorActual = "";
            iniciarNuevoNumero = true;
            desactivarEstadoOperador();
        } else if (comando.equals("±")) {
            double valor = Double.parseDouble(pantalla.getText());
            if (valor != 0) {
                pantalla.setText(formatearNumero(valor * -1));
            }
        } else if (comando.equals("%")) {
            double valor = Double.parseDouble(pantalla.getText());
            pantalla.setText(formatearNumero(valor / 100.0));
            iniciarNuevoNumero = true;
        } else if ("÷×−+".contains(comando)) {
            if (!operadorActual.isEmpty() && !iniciarNuevoNumero) {
                calcularResultado();
            }
            primerNumero = Double.parseDouble(pantalla.getText());
            operadorActual = comando;
            iniciarNuevoNumero = true;

            desactivarEstadoOperador();
            if (botonFuente != null) {
                botonOperadorActivo = botonFuente;
                botonOperadorActivo.setActivo(true);
            }
        } else if (comando.equals("=")) {
            if (!operadorActual.isEmpty()) {
                calcularResultado();
                operadorActual = "";
                iniciarNuevoNumero = true;
                desactivarEstadoOperador();
            }
        }

        ajustarTamanoFuente();
    }

    private void calcularResultado() {
        double segundoNumero = Double.parseDouble(pantalla.getText());
        double resultado = 0;

        switch (operadorActual) {
            case "+" -> resultado = primerNumero + segundoNumero;
            case "−" -> resultado = primerNumero - segundoNumero;
            case "×" -> resultado = primerNumero * segundoNumero;
            case "÷" -> {
                if (segundoNumero == 0) {
                    pantalla.setText("Error");
                    return;
                }
                resultado = primerNumero / segundoNumero;
            }
        }
        pantalla.setText(formatearNumero(resultado));
    }

    private String formatearNumero(double num) {
        if (Double.isNaN(num) || Double.isInfinite(num)) return "Error";
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.########", simbolos);
        return df.format(num);
    }

    private void ajustarTamanoFuente() {
        int longitud = pantalla.getText().length();
        if (longitud <= 6) {
            pantalla.setFont(new Font("SansSerif", Font.PLAIN, 64));
        } else if (longitud <= 8) {
            pantalla.setFont(new Font("SansSerif", Font.PLAIN, 50));
        } else {
            pantalla.setFont(new Font("SansSerif", Font.PLAIN, 38));
        }
    }

    private void desactivarEstadoOperador() {
        if (botonOperadorActivo != null) {
            botonOperadorActivo.setActivo(false);
            botonOperadorActivo = null;
        }
    }

    private void configurarTeclado() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        String teclas = "0123456789.";
        for (char c : teclas.toCharArray()) {
            String s = String.valueOf(c);
            im.put(KeyStroke.getKeyStroke(c), s);
            am.put(s, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    procesarEntrada(s, null);
                }
            });
        }

        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "+");
        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "−");
        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), "×");
        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0), "÷");
        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "=");
        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "AC");
        registrarTeclaEspecial(im, am, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DEL");
    }

    private void registrarTeclaEspecial(InputMap im, ActionMap am, KeyStroke ks, String accion) {
        im.put(ks, accion);
        am.put(accion, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accion.equals("DEL")) {
                    String actual = pantalla.getText();
                    if (!iniciarNuevoNumero && actual.length() > 1) {
                        pantalla.setText(actual.substring(0, actual.length() - 1));
                    } else {
                        pantalla.setText("0");
                        iniciarNuevoNumero = true;
                    }
                    ajustarTamanoFuente();
                } else {
                    procesarEntrada(accion, null);
                }
            }
        });
    }

    private static class BotonRedondo extends JButton {
        private final Color colorBase;
        private final Color colorTextoBase;
        private final boolean esPildora;
        private boolean hover = false;
        private boolean presionado = false;
        private boolean activo = false;

        public BotonRedondo(String texto, Color colorBase, Color colorTexto, boolean esPildora) {
            super(texto);
            this.colorBase = colorBase;
            this.colorTextoBase = colorTexto;
            this.esPildora = esPildora;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("SansSerif", Font.PLAIN, 26));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    presionado = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    presionado = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    presionado = false;
                    repaint();
                }
            });
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int ancho = getWidth();
            int alto = getHeight();

            Color fondoActual = activo ? Color.WHITE : colorBase;
            Color textoActual = activo ? colorBase : colorTextoBase;

            if (presionado) {
                fondoActual = aclararColor(fondoActual, 55);
            } else if (hover) {
                fondoActual = aclararColor(fondoActual, 25);
            }

            g2.setColor(fondoActual);
            if (esPildora) {
                g2.fillRoundRect(0, 0, ancho, alto, alto, alto);
            } else {
                int diametro = Math.min(ancho, alto);
                int x = (ancho - diametro) / 2;
                int y = (alto - diametro) / 2;
                g2.fillOval(x, y, diametro, diametro);
            }

            g2.setColor(textoActual);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textoY = ((alto - fm.getHeight()) / 2) + fm.getAscent();
            int textoX = esPildora ? (alto / 2) - (fm.stringWidth(getText()) / 2) : (ancho - fm.stringWidth(getText())) / 2;

            g2.drawString(getText(), textoX, textoY);
            g2.dispose();
        }

        private Color aclararColor(Color c, int cantidad) {
            int r = Math.min(255, c.getRed() + cantidad);
            int g = Math.min(255, c.getGreen() + cantidad);
            int b = Math.min(255, c.getBlue() + cantidad);
            return new Color(r, g, b);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculadora calc = new Calculadora();
            calc.setVisible(true);
        });
    }
}