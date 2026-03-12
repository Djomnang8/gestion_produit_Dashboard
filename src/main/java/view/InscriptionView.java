package view;

import auth.AuthService;
import javax.swing.*;
import java.awt.*;

import static view.LoginView.*;

/**
 * Fenêtre d'inscription réservée aux clients.
 * S'ouvre en dialog modal par-dessus la LoginView.
 * Palette identique à LoginView : bleu marine, orange, blanc cassé, vert confirmation.
 */
public class InscriptionView extends JDialog {

    private JTextField tUser;
    private JTextField tPass;
    private JTextField tPassConfirm;
    private JLabel     lblError;

    /**
     * @param parent fenêtre LoginView parente (reste visible derrière)
     */
    public InscriptionView(JFrame parent) {
        super(parent, "Créer un compte Client", true);
        setSize(400, 440);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    // ── Construction de l'interface ─────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LIGHT);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        add(root);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new GridLayout(2, 1, 0, 4));
        h.setBackground(new Color(22, 120, 80));
        h.setBorder(BorderFactory.createEmptyBorder(20, 30, 14, 30));
        JLabel title = new JLabel("🧑 Créer un Compte", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Espace réservé aux Clients", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(180, 240, 210));
        h.add(title); h.add(sub);
        return h;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(22, 46, 16, 46));

        tUser        = field();
        tPass        = field();
        tPassConfirm = field();

        JButton btnCreer   = mainBtn("✅  CRÉER MON COMPTE", new Color(22, 120, 80));
        JButton btnAnnuler = linkBtn("← Retour à la connexion");

        lblError = new JLabel(" ");
        lblError.setForeground(new Color(200, 50, 50));
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        form.add(label("Nom d'utilisateur"));           form.add(Box.createVerticalStrut(4));
        form.add(tUser);                                 form.add(Box.createVerticalStrut(12));
        form.add(label("Mot de passe"));                form.add(Box.createVerticalStrut(4));
        form.add(tPass);                                 form.add(Box.createVerticalStrut(12));
        form.add(label("Confirmer le mot de passe"));   form.add(Box.createVerticalStrut(4));
        form.add(tPassConfirm);                          form.add(Box.createVerticalStrut(18));
        form.add(btnCreer);                              form.add(Box.createVerticalStrut(6));
        form.add(lblError);                              form.add(Box.createVerticalStrut(10));
        form.add(btnAnnuler);

        btnCreer.addActionListener(e   -> doInscrire());
        btnAnnuler.addActionListener(e -> dispose());
        return form;
    }

    // ── Action ──────────────────────────────────────────────────────────────

    /**
     * Valide les champs et inscrit le client via AuthService.
     * En cas de succès, ferme le dialog et revient à la LoginView.
     */
    private void doInscrire() {
        String username = tUser.getText().trim();
        String password = tPass.getText().trim();
        String confirm  = tPassConfirm.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            lblError.setText("❌ Tous les champs sont obligatoires."); return;
        }
        if (username.length() < 3) {
            lblError.setText("❌ Nom d'utilisateur trop court (min. 3 car.)."); return;
        }
        if (password.length() < 4) {
            lblError.setText("❌ Mot de passe trop court (min. 4 car.)."); return;
        }
        if (!password.equals(confirm)) {
            lblError.setText("❌ Les mots de passe ne correspondent pas.");
            tPassConfirm.setText(""); return;
        }
        if (AuthService.usernameExists(username)) {
            lblError.setText("❌ Ce nom d'utilisateur est déjà pris."); return;
        }

        boolean ok = AuthService.inscrireClient(username, password);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "✅ Compte créé avec succès !\nVous pouvez maintenant vous connecter.",
                    "Inscription réussie", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            lblError.setText("❌ Erreur lors de la création du compte.");
        }
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
}