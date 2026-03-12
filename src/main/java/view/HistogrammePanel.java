package view;

import model.*;
import service.SalesTracker;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static view.LoginView.*;

/**
 * Panneau d'histogramme affichant les ventes mensuelles par produit.
 * Inclut une sélection du produit et de l'année (2020 à aujourd'hui).
 */
public class HistogrammePanel extends JPanel {

    private final List<Produit> produits;
    private JComboBox<String> cboProduit, cboAnnee;
    private BarChart chart;

    public HistogrammePanel(List<Produit> produits) {
        this.produits = produits;
        setLayout(new BorderLayout(6, 6));
        setBackground(C_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.setBackground(C_LIGHT);

        cboProduit = new JComboBox<>();
        produits.forEach(p -> cboProduit.addItem(p.getNom()));
        // Ajouter les produits vendus même s'ils ont été supprimés
        SalesTracker.getProducts().forEach(n -> {
            boolean found = produits.stream().anyMatch(p -> p.getNom().equals(n));
            if (!found) cboProduit.addItem(n);
        });

        cboAnnee = new JComboBox<>();
        int curYear = LocalDate.now().getYear();
        for (int y = 2020; y <= curYear; y++) cboAnnee.addItem(String.valueOf(y));
        cboAnnee.setSelectedItem(String.valueOf(curYear));

        controls.add(new JLabel("Produit :")); controls.add(cboProduit);
        controls.add(new JLabel("Année :"));   controls.add(cboAnnee);

        chart = new BarChart();
        add(controls, BorderLayout.NORTH);
        add(chart,    BorderLayout.CENTER);

        cboProduit.addActionListener(e -> updateChart());
        cboAnnee.addActionListener(e   -> updateChart());
        updateChart();
    }

    private void updateChart() {
        String produit = (String) cboProduit.getSelectedItem();
        int year = Integer.parseInt((String) cboAnnee.getSelectedItem());
        int[] data = SalesTracker.getMonthlySales(produit, year);
        chart.setData(produit, year, data);
    }

    // ── Composant de dessin ───────────────────────────────────────────────────
    static class BarChart extends JPanel {
        private String produit = "";
        private int year;
        private int[] data = new int[12];
        private static final String[] MONTHS =
                {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
        private static final Color[] BAR_COLORS = {
                new Color(41,98,204), new Color(255,140,0), new Color(34,170,100),
                new Color(220,50,50), new Color(120,80,200), new Color(0,170,200)
        };

        void setData(String p, int y, int[] d) { this.produit = p; this.year = y; this.data = d; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245,247,252)); g2.fillRect(0, 0, getWidth(), getHeight());

            int W = getWidth(), H = getHeight(), pad = 50, top = 30;
            int chartW = W - pad * 2, chartH = H - pad - top;
            int maxVal = Math.max(1, Arrays.stream(data).max().orElse(1));

            // Titre
            g2.setColor(C_DARK); g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(produit + " — Ventes " + year, pad, 20);

            // Lignes de guide
            g2.setColor(new Color(210, 215, 230));
            for (int i = 0; i <= 5; i++) {
                int y = top + chartH - (i * chartH / 5);
                g2.drawLine(pad, y, W - pad, y);
                g2.setColor(new Color(100,120,150));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(String.valueOf(i * maxVal / 5), 5, y + 4);
                g2.setColor(new Color(210, 215, 230));
            }

            // Barres
            int barW = chartW / 12 - 6;
            for (int i = 0; i < 12; i++) {
                int barH = data[i] == 0 ? 2 : (int)((double) data[i] / maxVal * chartH);
                int x    = pad + i * (chartW / 12) + 3;
                int y    = top + chartH - barH;
                g2.setColor(BAR_COLORS[i % BAR_COLORS.length]);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);
                if (data[i] > 0) {
                    g2.setColor(C_DARK);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.drawString(String.valueOf(data[i]), x + barW/2 - 4, y - 3);
                }
                g2.setColor(new Color(80,100,130));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(MONTHS[i], x, H - 8);
            }
        }
    }
}