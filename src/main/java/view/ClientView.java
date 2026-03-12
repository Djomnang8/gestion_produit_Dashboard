package view;

import auth.AuthService;
import auth.User;
import controller.ProduitController;
import model.*;
import service.SalesTracker;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


import static view.LoginView.*;

/**
 * Vue dédiée aux utilisateurs CLIENT.
 * Intègre la barre de navigation (titre + bouton Déconnexion) et le panneau de commande.
 * Peut aussi être utilisée comme sous-panneau dans ProduitView (onglet CLIENT du vendeur).
 *
 */
public class ClientView extends JFrame {

    private final ProduitController ctrl;
    private JComboBox<String> comboProduits;
    private JTextField tQte;
    private JLabel lblTotal, lblStock;
    private DefaultTableModel tableModel;

    // ── Constructeur complet ───────
    /**
     * @param ctrl contrôleur produit partagé
     * @param user utilisateur CLIENT connecté
     */
    public ClientView(ProduitController ctrl, User user) {
        super("StockManager Pro — CLIENT : " + user.getUsername());
        this.ctrl = ctrl;
        setSize(650, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_LIGHT);

        // Barre supérieure
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("🛒 Espace Client — " + user.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnLogout = new JButton("⏻  Déconnexion");
        btnLogout.setBackground(new Color(180, 50, 50));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            AuthService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginView(ctrl));
        });

        header.add(title,     BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);

        add(header,              BorderLayout.NORTH);
        add(buildOrderPanel(),   BorderLayout.CENTER);

        chargerProduits();
        setVisible(true);
    }

    // ── Constructeur panneau seul (utilisé comme onglet dans ProduitView) ─────
    /**
     * Crée uniquement le panneau de commande, sans fenêtre ni barre de déconnexion.
     * Utilisé comme composant dans l'onglet CLIENT de ProduitView.
     * @param ctrl contrôleur produit partagé
     */
    public ClientView(ProduitController ctrl) {
        super();                    // JFrame non affichée — on utilise getContentPane()
        this.ctrl = ctrl;
        // Dans ce mode, on expose le contenu via un panneau récupérable
        JPanel pane = buildOrderPanel();
        setContentPane(pane);
        chargerProduits();
    }

    // ── Construction du panneau de commande ───────────────────────────────────
    private JPanel buildOrderPanel() {
        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBackground(C_LIGHT);

        // Panneau commande
        JPanel orderPanel = new JPanel(new GridBagLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(C_PRIMARY, 1, true),
                "🛒 Passer une Commande",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), C_PRIMARY));
        orderPanel.setPreferredSize(new Dimension(0, 185));
        orderPanel.setBackground(new Color(232, 244, 253));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10);
        g.fill   = GridBagConstraints.HORIZONTAL;

        comboProduits = new JComboBox<>();
        comboProduits.setBackground(Color.WHITE);
        tQte      = new JTextField("1", 8);
        lblTotal  = new JLabel("Total : 0 FCFA");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(C_PRIMARY);
        lblStock  = new JLabel("Stock disponible : —");
        lblStock.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStock.setForeground(new Color(80, 120, 80));

        JButton btnValider = new JButton("✅ Confirmer la Commande");
        btnValider.setBackground(new Color(34, 170, 100));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFocusPainted(false);
        btnValider.setBorderPainted(false);
        btnValider.setFont(new Font("Segoe UI", Font.BOLD, 12));

        g.gridx = 0; g.gridy = 0; orderPanel.add(new JLabel("Produit :"), g);
        g.gridx = 1;              orderPanel.add(comboProduits, g);
        g.gridx = 0; g.gridy = 1; orderPanel.add(new JLabel("Quantité :"), g);
        g.gridx = 1;              orderPanel.add(tQte, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; orderPanel.add(lblStock, g);
        g.gridy = 3;              orderPanel.add(lblTotal, g);
        g.gridy = 4;              orderPanel.add(btnValider, g);

        // Tableau produits disponibles
        tableModel = new DefaultTableModel(
                new String[]{"Produit", "Prix Unitaire", "Stock Dispo"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setBackground(C_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionBackground(new Color(200, 220, 255));

        root.add(orderPanel,          BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        comboProduits.addActionListener(e -> calculer());
        tQte.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { calculer(); }
        });
        btnValider.addActionListener(e -> valider());

        return root;
    }

    // ── Méthodes métier ───────────────────────────────────────────────────────

    /** Recharge la liste des produits depuis la base. */
    public void chargerProduits() {
        if (comboProduits == null) return;
        comboProduits.removeAllItems();
        tableModel.setRowCount(0);
        ctrl.getAllProduits().forEach(p -> {
            if (p.getQuantiteStock() > 0) comboProduits.addItem(p.getNom());
            tableModel.addRow(new Object[]{
                    p.getNom(),
                    String.format("%.0f F", p.getPrixHT()),
                    p.getQuantiteStock()
            });
        });
        calculer();
    }

    private void calculer() {
        String nom = (String) comboProduits.getSelectedItem();
        if (nom == null) {
            lblTotal.setText("Total : 0 FCFA");
            lblStock.setText("Stock disponible : —");
            return;
        }
        Produit p = ctrl.getProduitByNom(nom);
        if (p == null) return;
        lblStock.setText("Stock disponible : " + p.getQuantiteStock() + " unité(s)");
        try {
            int q = Integer.parseInt(tQte.getText().trim());
            lblTotal.setText(String.format("Total : %.0f FCFA", p.getPrixHT() * q));
        } catch (Exception e) {
            lblTotal.setText("Total : 0 FCFA");
        }
    }

    private void valider() {
        if (!AuthService.isAllowed(User.Permission.PASSER_COMMANDE)
                && AuthService.getCurrentUser() != null) {
            JOptionPane.showMessageDialog(this,
                    "❌ Vous n'avez pas la permission de passer commande.",
                    "Accès refusé", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nom = (String) comboProduits.getSelectedItem();
        if (nom == null) {
            JOptionPane.showMessageDialog(this, "❌ Sélectionnez un produit.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Produit p = ctrl.getProduitByNom(nom);
        if (p == null) return;
        try {
            int q = Integer.parseInt(tQte.getText().trim());
            if (q <= 0) throw new NumberFormatException();
            if (p.getQuantiteStock() < q) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Stock insuffisant !\nDisponible : " + p.getQuantiteStock(),
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
                return;
            }
            p.retirerStock(q);
            ctrl.updateProduit(p);
            SalesTracker.recordSale(nom, q);
            JOptionPane.showMessageDialog(this,
                    "✅ Commande validée !\n" + q + " × " + nom
                            + "\nTotal : " + String.format("%.0f F", p.getPrixHT() * q),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            chargerProduits();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Quantité invalide.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}