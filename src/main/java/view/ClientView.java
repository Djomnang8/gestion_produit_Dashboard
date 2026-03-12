package View;

import Controller.ProduitController;
import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientView extends JPanel {
    private ProduitController controller;
    private JComboBox<String> comboProduits;
    private JTextField tQte;
    private JLabel lblTotal;
    private DefaultTableModel tableModel;

    public ClientView(ProduitController controller) {
        this.controller = controller;
        this.setLayout(new BorderLayout(5, 5));

        // === AJOUT COULEURS ===
        this.setBackground(new Color(245, 245, 245));          // fond principal

        JPanel orderPanel = new JPanel(new GridBagLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Passer commande"));
        orderPanel.setPreferredSize(new Dimension(0, 220));
        orderPanel.setBackground(new Color(227, 242, 253));    // fond panneau

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        comboProduits = new JComboBox<>();
        tQte = new JTextField("1", 10);
        lblTotal = new JLabel("Total : 0 FCFA");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal.setForeground(new Color(25, 118, 210));
        JButton btnValider = new JButton("Passer une Commande");
        btnValider.setBackground(Color.green);
        btnValider.setForeground(Color.BLACK);

        gbc.gridx = 0; gbc.gridy = 0; orderPanel.add(new JLabel("Produit:"), gbc);
        gbc.gridx = 1; orderPanel.add(comboProduits, gbc);
        gbc.gridx = 0; gbc.gridy = 1; orderPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1; orderPanel.add(tQte, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; orderPanel.add(lblTotal, gbc);
        gbc.gridy = 3; orderPanel.add(btnValider, gbc);

        // --- TABLEAU ---
        tableModel = new DefaultTableModel(new String[]{"Nom", "Prix Unitaire.", "Stock"}, 0);
        JTable table = new JTable(tableModel);

        this.add(orderPanel, BorderLayout.NORTH);
        this.add(new JScrollPane(table), BorderLayout.CENTER);

        comboProduits.addActionListener(e -> calculer());
        tQte.addKeyListener(new java.awt.event.KeyAdapter() { public void keyReleased(java.awt.event.KeyEvent e) { calculer(); }});
        btnValider.addActionListener(e -> valider());

        chargerProduits();
    }

    public void chargerProduits() {
        comboProduits.removeAllItems();
        tableModel.setRowCount(0);
        List<Produit> list = controller.getAllProduits();
        for (Produit p : list) {
            comboProduits.addItem(p.getNom());
            tableModel.addRow(new Object[]{p.getNom(), p.getPrixHT(), p.getQuantiteStock()});
        }
    }

    private void calculer() {
        String nom = (String) comboProduits.getSelectedItem();
        if (nom != null && !tQte.getText().isEmpty()) {
            try {
                Produit p = controller.getProduitByNom(nom);
                int q = Integer.parseInt(tQte.getText());
                lblTotal.setText(String.format("Total : %.0f FCFA", p.getPrixHT() * q));
            } catch (Exception e) { lblTotal.setText("Total : 0 FCFA"); }
        }
    }

    private void valider() {
        String nom = (String) comboProduits.getSelectedItem();
        if (nom == null) return;
        Produit p = controller.getProduitByNom(nom);
        try {
            int q = Integer.parseInt(tQte.getText());
            if (p.getQuantiteStock() >= q) {
                p.retirerStock(q);
                controller.updateProduit(p);
                JOptionPane.showMessageDialog(this, "Validé !");
                chargerProduits();
            } else {
                JOptionPane.showMessageDialog(this, "Stock insuffisant");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Qté invalide"); }
    }
}