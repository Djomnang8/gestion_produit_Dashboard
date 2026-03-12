package view;

import auth.AuthService;
import auth.User;
import controller.ProduitController;
import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre de connexion principale.
 * Reçoit le contrôleur depuis Main et le transmet aux vues suivantes.
 * – Vendeur → ProduitView
 * – Client  → ClientView (fenêtre autonome)
 * – Nouveau client → InscriptionView (dialog modal)
 */
public class LoginView extends JFrame {

    // ── Charte graphique partagée avec toutes les vues ───────────────────────
    public static final Color C_DARK    = new Color(18, 34, 68);
    public static final Color C_PRIMARY = new Color(41, 98, 204);
    public static final Color C_ACCENT  = new Color(255, 140, 0);
    public static final Color C_LIGHT   = new Color(245, 247, 252);
    public static final Color C_TEXT    = new Color(30, 30, 50);

    private final ProduitController ctrl;
    private JTextField tUser;
    private JTextField tPass;
    private JLabel     lblError;

    /**
     * @param ctrl contrôleur construit dans Main, transmis à toutes les vues
     */
    public LoginView(ProduitController ctrl) {
        this.ctrl = ctrl;
        setTitle("Connexion — StockManager Pro");
        setSize(430, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    // ── Construction de l'interface ──────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LIGHT);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        add(root);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new GridLayout(2, 1, 0, 4));
        h.setBackground(C_DARK);
        h.setBorder(BorderFactory.createEmptyBorder(28, 30, 18, 30));
        JLabel title = new JLabel("📦 StockManager Pro", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Gestion de Stock & Ventes", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(180, 200, 255));
        h.add(title); h.add(sub);
        return h;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(28, 50, 16, 50));

        tUser = field();
        tPass = field();

        JButton btnLogin = mainBtn("🔐  SE CONNECTER", C_ACCENT);
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(200, 50, 50));
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // Séparateur visuel
        JPanel sep = new JPanel(new BorderLayout(6, 0));
        sep.setBackground(C_LIGHT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        sep.add(new JSeparator(), BorderLayout.WEST);
        JLabel ou = new JLabel("  ou  ");
        ou.setForeground(new Color(160, 160, 180));
        sep.add(ou, BorderLayout.CENTER);
        sep.add(new JSeparator(), BorderLayout.EAST);

        JButton btnInscrire = linkBtn("Pas encore de compte ? S'inscrire en tant que Client");

        form.add(label("Nom d'utilisateur")); form.add(Box.createVerticalStrut(4));
        form.add(tUser);                      form.add(Box.createVerticalStrut(12));
        form.add(label("Mot de passe"));      form.add(Box.createVerticalStrut(4));
        form.add(tPass);                      form.add(Box.createVerticalStrut(18));
        form.add(btnLogin);                   form.add(Box.createVerticalStrut(6));
        form.add(lblError);                   form.add(Box.createVerticalStrut(14));
        form.add(sep);                        form.add(Box.createVerticalStrut(8));
        form.add(btnInscrire);

        btnLogin.addActionListener(e -> doLogin());
        tPass.addActionListener(e -> doLogin());
        btnInscrire.addActionListener(e -> new InscriptionView(this));
        return form;
    }

    // ── Action de connexion ───────────────────────────────────────────────────

    /**
     * Authentifie l'utilisateur et ouvre la vue correspondant à son rôle.
     * VENDEUR → ProduitView | CLIENT → ClientView
     */
    private void doLogin() {
        String username = tUser.getText().trim();
        String password = tPass.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("❌ Remplissez tous les champs."); return;
        }
        User u = AuthService.login(username, password);
        if (u == null) {
            lblError.setText("❌ Identifiants incorrects ou compte désactivé.");
            tPass.setText(""); return;
        }
        dispose();
        SwingUtilities.invokeLater(() -> {
            switch (u.getRole()) {
                case VENDEUR -> new ProduitView(ctrl, u);
                case CLIENT  -> new ClientView(ctrl, u);
            }
        });
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(CENTER_ALIGNMENT);
        return f;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(C_TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton mainBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton linkBtn(String text) {
        JButton b = new JButton("<html><u>" + text + "</u></html>");
        b.setForeground(C_PRIMARY); b.setBackground(C_LIGHT);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** @return le contrôleur partagé */
    public ProduitController getCtrl() { return ctrl; }
}