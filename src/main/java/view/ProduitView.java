package view;

import auth.AuthService;
import auth.User;
import controller.ProduitController;
import model.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static view.LoginView.*;

/**
 * Vue principale VENDEUR.
 * - Recherche avancée (nom, stock, prix, type, date limite) insensible à la casse.
 * - Formulaire de saisie avec images publicitaires à droite.
 * - Onglet CLIENT intégré pour passer des commandes.
 */
public class ProduitView extends JFrame {

    private final ProduitController ctrl;
    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tNom, tPrix, tQuantite, tSpecifique, tRecherche;
    private JComboBox<String> cType;
    private JLabel lblTotal, lblValeur, lblAlerte;
    private ClientView clientPanel;

    /**
     * @param ctrl contrôleur produit partagé
     * @param user utilisateur VENDEUR connecté
     */
    public ProduitView(ProduitController ctrl, User user) {
        this.ctrl        = ctrl;
        this.currentUser = user;
        setTitle("Gestion Stock — " + user.getUsername());
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Barre de déconnexion ──────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(C_DARK);
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JLabel lblUser = new JLabel("👤 " + user.getUsername() + "  [" + user.getRole() + "]");
        lblUser.setForeground(new Color(180, 200, 255));
        lblUser.setFont(new Font("Arial", Font.BOLD, 12));
        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.setBackground(new Color(180, 50, 50));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            AuthService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginView(ctrl));
        });
        topBar.add(lblUser,   BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        // ── TabbedPane ────────────────────────────────────────────────────────
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setBackground(new Color(220, 230, 245));
        tabbedPane.setForeground(Color.DARK_GRAY);

        // ── Panneau VENDEUR ───────────────────────────────────────────────────
        JPanel vendeurPanel = new JPanel(new BorderLayout(0, 0));

        JPanel header = new JPanel(new GridLayout(1, 3, 5, 0));
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.setBackground(new Color(227, 242, 253));
        lblTotal  = creerStatCard("Total",     header);
        lblValeur = creerStatCard("Valeur",    header);
        lblAlerte = creerStatCard("Stock < 5", header);

        lblAlerte.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblAlerte.getParent().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showStockAlert(ctrl.getAllProduits());
            }
        });
        lblValeur.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblValeur.getParent().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { showHistogramme(); }
        });

        // ── Formulaire (gauche) + Images pub (droite) ─────────────────────────
        JPanel centerArea = new JPanel(new BorderLayout(8, 0));
        centerArea.setBackground(new Color(227, 242, 253));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Saisie Produits"));
        formPanel.setBackground(new Color(227, 242, 253));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        tNom       = new JTextField(12);
        tPrix      = new JTextField(12);
        tQuantite  = new JTextField(12);
        tSpecifique = new JTextField(12);
        cType      = new JComboBox<>(new String[]{"Alimentaire", "Electronique"});

        ajouterChamp(formPanel, "Nom:",        tNom,        gbc, 0);
        ajouterChamp(formPanel, "Prix:",        tPrix,       gbc, 1);
        ajouterChamp(formPanel, "Qté:",         tQuantite,   gbc, 2);
        ajouterChamp(formPanel, "Type:",        cType,       gbc, 3);
        ajouterChamp(formPanel, "Date Limite / Garantie :", tSpecifique, gbc, 4);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(new Color(227, 242, 253));
        JButton bAjouter   = new JButton("Ajouter");
        JButton bModifier  = new JButton("Modifier");
        JButton bSupprimer = new JButton("Supprimer");
        bAjouter.setBackground(Color.GREEN);
        bModifier.setBackground(Color.ORANGE);
        bSupprimer.setBackground(Color.RED);
        btnPanel.add(bAjouter);
        btnPanel.add(bModifier);
        btnPanel.add(bSupprimer);
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        gbc.gridwidth = 1;

        // Panneau images publicitaires
        JPanel adsPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        adsPanel.setBorder(BorderFactory.createTitledBorder("Publicités"));
        adsPanel.setBackground(new Color(227, 242, 253));
        adsPanel.setPreferredSize(new Dimension(200, 0));
        String[] adLabels = {"PROMO -20%", "NOUVEAUTÉ !", "DÉSTOCKAGE", "OFFRE SPÉCIALE"};
        Color[]  adColors = {
                new Color(255, 80, 80), new Color(41, 130, 204),
                new Color(34, 160, 80), new Color(200, 130, 0)
        };
        String[] adIcons = {"🔥", "⭐", "📦", "🎁"};
        for (int i = 0; i < 4; i++) {
            JPanel ad = new JPanel(new BorderLayout(0, 2));
            ad.setBackground(adColors[i]);
            ad.setBorder(BorderFactory.createLineBorder(adColors[i].darker(), 1));
            JLabel icon = new JLabel(adIcons[i], SwingConstants.CENTER);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            icon.setForeground(Color.WHITE);
            JLabel text = new JLabel("<html><center>" + adLabels[i] + "</center></html>", SwingConstants.CENTER);
            text.setFont(new Font("Arial", Font.BOLD, 9));
            text.setForeground(Color.WHITE);
            ad.add(icon, BorderLayout.CENTER);
            ad.add(text, BorderLayout.SOUTH);
            adsPanel.add(ad);
        }

        centerArea.add(formPanel, BorderLayout.CENTER);
        centerArea.add(adsPanel,  BorderLayout.EAST);

        // ── Recherche avancée ─────────────────────────────────────────────────
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tRecherche = new JTextField(20);
        JButton bRechercher = new JButton("🔍 Rechercher");
        JButton bActualiser = new JButton("Vider Recherche");
        bRechercher.setBackground(Color.PINK);
        bActualiser.setBackground(new Color(255, 224, 130));

        JLabel hintLabel = new JLabel("<html><i style='color:gray;font-size:10px;'>"
                + "Recherche par : nom, type (alim/elec), prix (ex: &lt;500 ou &gt;100), stock (ex: &lt;5), date limite"
                + "</i></html>");

        searchPanel.add(new JLabel("Recherche :"));
        searchPanel.add(tRecherche);
        searchPanel.add(bRechercher);
        searchPanel.add(bActualiser);
        searchPanel.add(hintLabel);

        // Tableau — colonnes : Nom | Prix HT | Stock | Type | Date Limite / Garantie
        tableModel = new DefaultTableModel(
                new String[]{"Nom", "Prix HT", "Stock", "Type", "Date Limite / Garantie (mois)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(22);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(searchPanel,         BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(0, 260));

        vendeurPanel.add(header,      BorderLayout.NORTH);
        vendeurPanel.add(centerArea,  BorderLayout.CENTER);
        vendeurPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ── Panneau CLIENT (onglet intégré pour le vendeur) ───────────────────
        clientPanel = new ClientView(ctrl);
        JPanel clientPanelContent = (JPanel) clientPanel.getContentPane();

        tabbedPane.addTab(" VENDEUR ", vendeurPanel);
        tabbedPane.addTab(" CLIENT  ", clientPanelContent);

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            JLabel lbl = new JLabel(tabbedPane.getTitleAt(i));
            lbl.setPreferredSize(new Dimension(100, 50));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(C_DARK);
            tabbedPane.setTabComponentAt(i, lbl);
        }

        tabbedPane.addChangeListener(e -> updateTabColors(tabbedPane));
        updateTabColors(tabbedPane);

        getContentPane().setBackground(new Color(245, 245, 245));
        getContentPane().add(topBar,     BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // ── Actions boutons ───────────────────────────────────────────────────
        bAjouter.addActionListener(e -> {
            Produit p = recupererForm();
            if (p == null) return;
            if (ctrl.existsByNom(p.getNom())) {
                JOptionPane.showMessageDialog(this,
                        "❌ Un produit avec ce nom existe déjà !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                ctrl.creer_produit(p);
                JOptionPane.showMessageDialog(this, "✅ Produit ajouté.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                rafraichir(); viderChamps();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        bModifier.addActionListener(e -> {
            Produit p = recupererForm();
            if (p == null) return;
            try {
                ctrl.updateProduit(p);
                JOptionPane.showMessageDialog(this, "✅ Produit modifié.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                rafraichir(); viderChamps();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        bSupprimer.addActionListener(e -> {
            String nom = tNom.getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "❌ Sélectionnez un produit à supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Supprimer « " + nom + " » ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                ctrl.deleteProduit(nom);
                JOptionPane.showMessageDialog(this, "🗑 Produit supprimé.", "Info", JOptionPane.INFORMATION_MESSAGE);
                rafraichir(); viderChamps();
            }
        });

        bRechercher.addActionListener(e -> rechercherAvancee());
        tRecherche.addActionListener(e -> rechercherAvancee());
        bActualiser.addActionListener(e -> { tRecherche.setText(""); rafraichir(); });

        // Sélection d'une ligne → pré-remplir le formulaire
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                tNom.setText(tableModel.getValueAt(row, 0).toString());
                tPrix.setText(tableModel.getValueAt(row, 1).toString());
                tQuantite.setText(tableModel.getValueAt(row, 2).toString());
                cType.setSelectedItem(
                        tableModel.getValueAt(row, 3).toString().equals("Alim")
                                ? "Alimentaire" : "Electronique");
                tSpecifique.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        rafraichir();
        setVisible(true);
    }

    // ── Recherche avancée ─────────────────────────────────────────────────────
    private void rechercherAvancee() {
        String raw = tRecherche.getText().trim().toLowerCase();
        if (raw.isEmpty()) { rafraichir(); return; }

        List<Produit> resultats = ctrl.getAllProduits().stream().filter(p -> {
            if (raw.equals("alim") || raw.equals("alimentaire")) return p instanceof ProduitAlimentaire;
            if (raw.equals("elec") || raw.equals("electronique")) return p instanceof ProduitElectronique;

            if (raw.matches("<\\d+(\\.\\d+)?")) {
                double seuil = Double.parseDouble(raw.substring(1));
                return p.getPrixHT() < seuil;
            }
            if (raw.matches(">\\d+(\\.\\d+)?")) {
                double seuil = Double.parseDouble(raw.substring(1));
                return p.getPrixHT() > seuil;
            }
            if (raw.startsWith("stock<")) {
                try { double s = Double.parseDouble(raw.substring(6)); return p.getQuantiteStock() < s; }
                catch (NumberFormatException ignored) {}
            }
            if (raw.startsWith("stock>")) {
                try { double s = Double.parseDouble(raw.substring(6)); return p.getQuantiteStock() > s; }
                catch (NumberFormatException ignored) {}
            }

            String dateLimite = (p instanceof ProduitAlimentaire pa)
                    ? pa.getDatePeremption().toLowerCase() : "";
            return p.getNom().toLowerCase().contains(raw)
                    || (!dateLimite.isEmpty() && dateLimite.contains(raw))
                    || String.valueOf(p.getPrixHT()).contains(raw)
                    || String.valueOf(p.getQuantiteStock()).contains(raw);
        }).collect(Collectors.toList());

        tableModel.setRowCount(0);
        if (resultats.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucun produit trouvé pour : « " + tRecherche.getText().trim() + " »",
                    "Recherche", JOptionPane.INFORMATION_MESSAGE);
        } else {
            resultats.forEach(this::ajouterLigneTableau);
        }
    }

    // ── Rafraîchissement ─────────────────────────────────────────────────────
    private void rafraichir() {
        tableModel.setRowCount(0);
        List<Produit> list = ctrl.getAllProduits();
        double val = 0; int alt = 0;
        for (Produit p : list) {
            ajouterLigneTableau(p);
            val += p.getPrixHT() * p.getQuantiteStock();
            if (p.getQuantiteStock() < 5) alt++;
        }
        lblTotal.setText(String.valueOf(list.size()));
        lblValeur.setText(String.format("%.0f", val));
        lblAlerte.setText(String.valueOf(alt));
        if (clientPanel != null) clientPanel.chargerProduits();
    }

    private void showStockAlert(List<Produit> list) {
        long nb = list.stream().filter(p -> p.getQuantiteStock() < 5).count();
        if (nb == 0) {
            JOptionPane.showMessageDialog(this, "✅ Aucun produit en stock critique.",
                    "Stock", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("⚠️ Produits en stock critique (< 5 unités) :\n\n");
        list.stream().filter(p -> p.getQuantiteStock() < 5)
                .forEach(p -> sb.append(" • ").append(p.getNom())
                        .append(" : ").append(p.getQuantiteStock()).append(" unité(s)\n"));
        JOptionPane.showMessageDialog(this, sb.toString(), "Alerte Stock", JOptionPane.WARNING_MESSAGE);
    }

    private void showHistogramme() {
        JDialog dlg = new JDialog(this, "📊 Activité des Produits par Mois", true);
        dlg.setSize(700, 420);
        dlg.setLocationRelativeTo(this);
        dlg.add(new HistogrammePanel(ctrl.getAllProduits()));
        dlg.setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Ajoute une ligne au tableau avec les informations du produit.
     * Affiche la date de péremption pour les alimentaires, la durée de garantie pour les électroniques.
     */
    private void ajouterLigneTableau(Produit p) {
        String info = (p instanceof ProduitAlimentaire pa)
                ? pa.getDatePeremption()
                : String.valueOf(((ProduitElectronique) p).getDureeGarantie()) + " mois";
        tableModel.addRow(new Object[]{
                p.getNom(),
                String.format("%.0f", p.getPrixHT()),
                p.getQuantiteStock(),
                (p instanceof ProduitAlimentaire ? "Alim" : "Elec"),
                info
        });
    }

    private void ajouterChamp(JPanel p, String l, JComponent c, GridBagConstraints g, int y) {
        g.gridy = y; g.gridx = 0; p.add(new JLabel(l), g);
        g.gridx = 1;              p.add(c, g);
    }

    private JLabel creerStatCard(String t, JPanel p) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        c.add(new JLabel(t, SwingConstants.CENTER), BorderLayout.NORTH);
        JLabel v = new JLabel("0", SwingConstants.CENTER);
        v.setBackground(Color.WHITE);
        v.setOpaque(true);
        v.setFont(new Font("Arial", Font.BOLD, 14));
        c.add(v, BorderLayout.CENTER);
        p.add(c);
        return v;
    }

    /**
     * Lit le formulaire et construit un objet Produit.
     * Pour un alimentaire, le champ "Date Limite" doit être au format JJ-MM-AAAA.
     * Pour un électronique, le champ "Garantie" doit être un entier (nombre de mois).
     */
    private Produit recupererForm() {
        String nom      = tNom.getText().trim();
        String prixStr  = tPrix.getText().trim();
        String qteStr   = tQuantite.getText().trim();
        if (nom.isEmpty() || prixStr.isEmpty() || qteStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Les champs Nom, Prix et Quantité sont obligatoires.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            double prix = Double.parseDouble(prixStr);
            int    qte  = Integer.parseInt(qteStr);
            String spec = tSpecifique.getText().trim();
            if ("Alimentaire".equals(cType.getSelectedItem()))
                return new ProduitAlimentaire(nom, prix, qte, spec);
            return new ProduitElectronique(nom, prix, qte,
                    spec.isEmpty() ? 0 : Integer.parseInt(spec));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Valeurs numériques invalides.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void viderChamps() {
        tNom.setText(""); tPrix.setText(""); tQuantite.setText(""); tSpecifique.setText("");
    }

    private void updateTabColors(JTabbedPane tabs) {
        int selected = tabs.getSelectedIndex();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            java.awt.Component comp = tabs.getTabComponentAt(i);
            if (comp instanceof JLabel lbl) {
                if (i == selected) {
                    lbl.setForeground(C_DARK);
                    lbl.setFont(new Font("Arial", Font.BOLD, 12));
                } else {
                    lbl.setForeground(new Color(80, 100, 130));
                    lbl.setFont(new Font("Arial", Font.PLAIN, 12));
                }
            }
        }
    }
}