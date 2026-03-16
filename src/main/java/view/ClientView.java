package view;

import auth.AuthService;
import auth.User;
import controller.ProduitController;
import model.*;

import service.file_attenteView;
import service.file_attenteView.PendingOrder;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static view.LoginView.*;

/**
 * Vue dédiée aux utilisateurs CLIENT.
 * Intègre la barre de navigation (titre + bouton Déconnexion) et le panneau de commande.
 * Peut aussi être utilisée comme sous-panneau dans ProduitView (onglet CLIENT du vendeur).
 *
 * Nouveauté : après soumission d'une commande, affiche un dialog d'attente.
 * La commande est validée (stock diminué) uniquement quand le vendeur accepte via ProduitView.
 */
public class ClientView extends JFrame {

    private final ProduitController ctrl;
    private JComboBox<String> comboProduits;
    private JTextField tQte;
    private JLabel lblTotal, lblStock;
    private DefaultTableModel tableModel;

    // Dialog d'attente affiché côté client (null si pas de commande en cours)
    private JDialog waitingDialog;
    // Commande en cours d'attente
    private PendingOrder pendingOrder;

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
        super();
        this.ctrl = ctrl;
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

        root.add(orderPanel,             BorderLayout.NORTH);
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

    /**
     * Valide une commande en la soumettant à la file d'attente (OrderQueue).
     * Le stock n'est PAS encore diminué : c'est le vendeur qui doit accepter.
     * Un dialog d'attente s'affiche jusqu'à décision du vendeur.
     */
    private void valider() {
        User currentUser = AuthService.getCurrentUser();

        // Si aucun utilisateur connecté (mode onglet vendeur) : comportement original
        if (currentUser == null) {
            validerDirectement();
            return;
        }

        if (!AuthService.isAllowed(User.Permission.PASSER_COMMANDE)) {
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

        int q;
        try {
            q = Integer.parseInt(tQte.getText().trim());
            if (q <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Quantité invalide.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (p.getQuantiteStock() < q) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Stock insuffisant !\nDisponible : " + p.getQuantiteStock(),
                    "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Soumettre la commande au vendeur via OrderQueue
        pendingOrder = file_attenteView.getInstance().submit(
                currentUser.getUsername(), nom, q, p.getPrixHT());

        // Afficher le dialog d'attente (non-bloquant : on utilise un thread séparé)
        showWaitingDialog(currentUser.getUsername(), nom, q, p.getPrixHT());
    }

    /**
     * Affiche la fenêtre d'attente côté client jusqu'à décision du vendeur.
     * Surveille l'état de la commande via un Timer Swing (polling toutes les 500 ms).
     */
    private void showWaitingDialog(String username, String nom, int qte, double prix) {
        // Trouver la fenêtre parente (this si visible, sinon null)
        Window parent = SwingUtilities.getWindowAncestor(getRootPane());

        waitingDialog = new JDialog(parent instanceof Frame ? (Frame) parent : null,
                "⏳ En attente de validation", false);
        waitingDialog.setSize(420, 220);
        waitingDialog.setLocationRelativeTo(parent);
        waitingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 247, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel icon = new JLabel("⏳", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel msg = new JLabel(
                "<html><center>"
                        + "<b>Commande soumise au vendeur</b><br><br>"
                        + "Produit : <b>" + nom + "</b><br>"
                        + "Quantité : <b>" + qte + "</b><br>"
                        + "Total estimé : <b>" + String.format("%.0f FCFA", prix * qte) + "</b><br><br>"
                        + "<i style='color:gray;'>Veuillez patienter pendant que le vendeur valide votre commande…</i>"
                        + "</center></html>",
                SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setForeground(new Color(41, 98, 204));

        panel.add(icon,     BorderLayout.NORTH);
        panel.add(msg,      BorderLayout.CENTER);
        panel.add(progress, BorderLayout.SOUTH);

        waitingDialog.add(panel);

        // Timer de polling toutes les 500 ms
        final PendingOrder orderRef = pendingOrder;
        Timer timer = new Timer(500, null);
        timer.addActionListener(e -> {
            if (orderRef.status == PendingOrder.Status.ACCEPTEE) {
                timer.stop();
                waitingDialog.dispose();
                waitingDialog = null;
                // Rafraîchir la liste (le stock a été diminué par le vendeur)
                chargerProduits();
                JOptionPane.showMessageDialog(ClientView.this,
                        "✅ Votre commande a été acceptée par le vendeur !\n"
                                + qte + " × " + nom
                                + "\nTotal : " + String.format("%.0f F", prix * qte),
                        "Commande acceptée", JOptionPane.INFORMATION_MESSAGE);
            } else if (orderRef.status == PendingOrder.Status.REFUSEE) {
                timer.stop();
                waitingDialog.dispose();
                waitingDialog = null;
                chargerProduits();
                JOptionPane.showMessageDialog(ClientView.this,
                        "❌ Votre commande a été refusée par le vendeur.",
                        "Commande refusée", JOptionPane.ERROR_MESSAGE);
            }
        });
        timer.start();

        waitingDialog.setVisible(true);
    }

    /**
     * Validation directe (mode onglet vendeur, sans utilisateur connecté).
     * Comportement original : diminue le stock immédiatement.
     */
    private void validerDirectement() {
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
            service.SalesTracker.recordSale(nom, q);
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