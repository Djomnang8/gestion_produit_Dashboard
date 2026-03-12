package View;

import Controller.ProduitController;
import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProduitView extends JFrame {
    private ProduitController controller;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tNom, tPrix, tQuantite, tSpecifique, tRecherche;
    private JComboBox<String> cType;
    private JLabel lblTotal, lblValeur, lblAlerte;
    private ClientView clientPanel;

    public ProduitView(ProduitController controller) {
        this.controller = controller;
        this.setTitle("Gestion Stock");
        this.setSize(950, 650);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        // --- fenetre du VENDEUR ---
        JPanel vendeurPanel = new JPanel(new BorderLayout(0, 0));

        JPanel header = new JPanel(new GridLayout(1, 3, 5, 0));
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.setBackground(new Color(227, 242, 253));// fond en-tête

        lblTotal = creerStatCard("Total", header);
        lblValeur = creerStatCard("Valeur", header);
        lblAlerte = creerStatCard("Stock < 5", header);

        // Formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Saisie Produits "));
        formPanel.setBackground(new Color(227, 242, 253));     // fond formulaire
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        tNom = new JTextField(15); tPrix = new JTextField(15);
        tQuantite = new JTextField(15); tSpecifique = new JTextField(15);
        cType = new JComboBox<>(new String[]{"Alimentaire", "Electronique"});

        ajouterChamp(formPanel, "Nom:", tNom, gbc, 0);
        ajouterChamp(formPanel, "Prix:", tPrix, gbc, 1);
        ajouterChamp(formPanel, "Qté:", tQuantite, gbc, 2);
        ajouterChamp(formPanel, "Type:", cType, gbc, 3);
        ajouterChamp(formPanel, "Date Limite:", tSpecifique, gbc, 4);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton bAjouter = new JButton("Ajouter");
        JButton bModifier = new JButton("Modifier");
        JButton bSupprimer = new JButton("Supprimer");

        // === COULEURS DES BOUTONS ===
        bAjouter.setBackground(Color.green);
        bModifier.setBackground(Color.orange);
        bSupprimer.setBackground(Color.red);
        btnPanel.add(bAjouter); btnPanel.add(bModifier); btnPanel.add(bSupprimer);
        gbc.gridy = 5; gbc.gridx = 1; formPanel.add(btnPanel, gbc);

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tRecherche = new JTextField(15);
        JButton bRechercher = new JButton("Rechercher");
        JButton bActualiser = new JButton("Vider Recherche");

        // === COULEURS BOUTONS RECHERCHE ===
        bRechercher.setBackground(Color.pink);
        bActualiser.setBackground(new Color(255, 224, 130));
        searchPanel.add(new JLabel("Recherche Nom:")); searchPanel.add(tRecherche);
        searchPanel.add(bRechercher); searchPanel.add(bActualiser);

        tableModel = new DefaultTableModel(new String[]{"Nom", "Prix", "Stock", "Type", "Date Limite"}, 0);
        table = new JTable(tableModel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(0, 250));

        vendeurPanel.add(header, BorderLayout.NORTH);
        vendeurPanel.add(formPanel, BorderLayout.CENTER);
        vendeurPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- ONGLET CLIENT ---
        clientPanel = new ClientView(controller);
        tabbedPane.addTab(" VENDEUR ", vendeurPanel);
        tabbedPane.addTab(" CLIENT  ", clientPanel);

        // Taille des onglets
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            JLabel lbl = new JLabel(tabbedPane.getTitleAt(i));
            lbl.setPreferredSize(new Dimension(100, 50));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            tabbedPane.setTabComponentAt(i, lbl);
        }

        // === COULEUR FOND PRINCIPAL ===
        this.getContentPane().setBackground(new Color(245, 245, 245));

        this.add(tabbedPane);

        // --- LOGIQUE DES BOUTONS ---
        bAjouter.addActionListener(e -> {
            controller.creer_produit(recupererForm());
            rafraichir();
            viderChamps();
        });

        bModifier.addActionListener(e -> {
            controller.updateProduit(recupererForm());
            rafraichir();
            viderChamps();
        });

        bSupprimer.addActionListener(e -> {
            controller.deleteProduit(tNom.getText());
            rafraichir();
            viderChamps();
        });

        bRechercher.addActionListener(e -> {
            Produit p = controller.getProduitByNom(tRecherche.getText());
            if (p != null) {
                tableModel.setRowCount(0);
                ajouterLigneTableau(p);
            } else {
                JOptionPane.showMessageDialog(this, "Produit non trouvé");
            }
        });

        bActualiser.addActionListener(e -> {
            tRecherche.setText("");
            rafraichir();
        });

        // --- SELECTION LIGNE  ---
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                tNom.setText(tableModel.getValueAt(row, 0).toString());
                tPrix.setText(tableModel.getValueAt(row, 1).toString());
                tQuantite.setText(tableModel.getValueAt(row, 2).toString());
                cType.setSelectedItem(tableModel.getValueAt(row, 3).toString().equals("Alim") ? "Alimentaire" : "Electronique");
                tSpecifique.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        rafraichir();
        this.setVisible(true);
    }

    private void viderChamps() {
        tNom.setText("");
        tPrix.setText("");
        tQuantite.setText("");
        tSpecifique.setText("");
    }

    private void ajouterLigneTableau(Produit p) {
        String info = (p instanceof ProduitAlimentaire) ? ((ProduitAlimentaire)p).getDatePeremption() : String.valueOf(((ProduitElectronique)p).getDureeGarantie());
        tableModel.addRow(new Object[]{p.getNom(), p.getPrixHT(), p.getQuantiteStock(), (p instanceof ProduitAlimentaire ? "Alim" : "Elec"), info});
    }

    private void rafraichir() {
        tableModel.setRowCount(0);
        List<Produit> list = controller.getAllProduits();
        double val = 0; int alt = 0;
        for (Produit p : list) {
            ajouterLigneTableau(p);
            val += (p.getPrixHT() * p.getQuantiteStock());
            if (p.getQuantiteStock() < 5) alt++;
        }
        lblTotal.setText(String.valueOf(list.size()));
        lblValeur.setText(String.format("%.0f", val));
        lblAlerte.setText(String.valueOf(alt));
        clientPanel.chargerProduits();
    }


    private void ajouterChamp(JPanel p, String l, JComponent c, GridBagConstraints g, int y) {
        g.gridy = y; g.gridx = 0; p.add(new JLabel(l), g);
        g.gridx = 1; p.add(c, g);
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
        p.add(c); return v;
    }

    private Produit recupererForm() {
        try {
            if ("Alimentaire".equals(cType.getSelectedItem()))
                return new ProduitAlimentaire(tNom.getText(), Double.parseDouble(tPrix.getText()), Integer.parseInt(tQuantite.getText()), tSpecifique.getText());
            return new ProduitElectronique(tNom.getText(), Double.parseDouble(tPrix.getText()), Integer.parseInt(tQuantite.getText()), Integer.parseInt(tSpecifique.getText()));
        } catch (Exception e) { return null; }
    }
}