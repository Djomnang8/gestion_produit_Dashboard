package repository;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import model.*;

/**
 * Implémentation SQL du dépôt de produits.
 * Utilise JDBC pour persister les données en base de données.
 * Le champ date_peremption est au format DATE (DD-MM-YYYY en affichage).
 */
public class ProduitRepositorySql implements ProduitRepository {
    private final Connection connection;

    /**
     * @param connection connexion JDBC active
     */
    public ProduitRepositorySql(Connection connection) {
        this.connection = connection;
    }

    /**
     * Enregistre un produit en base. Lève une exception si le nom existe déjà.
     * @param produit produit à insérer
     */
    @Override
    public void save(Produit produit) {
        String sql = "INSERT INTO produits (nom, prix_ht, quantite, type, date_peremption, duree_garantie) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produit.getNom());
            stmt.setDouble(2, produit.getPrixHT());
            stmt.setInt(3, produit.getQuantiteStock());
            if (produit instanceof ProduitAlimentaire pa) {
                stmt.setString(4, "alimentaire");
                // Stockage au format DATE SQL
                stmt.setDate(5, parseDateString(pa.getDatePeremption()));
                stmt.setNull(6, Types.INTEGER);
            } else if (produit instanceof ProduitElectronique pe) {
                stmt.setString(4, "electronique");
                stmt.setNull(5, Types.DATE);
                stmt.setInt(6, pe.getDureeGarantie());
            }
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Récupère tous les produits de la base.
     * @return liste des produits
     */
    @Override
    public List<Produit> getAllProduits() {
        List<Produit> produits = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM produits");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) { Produit p = mapRow(rs); if (p != null) produits.add(p); }
        } catch (Exception e) { e.printStackTrace(); }
        return produits;
    }

    /**
     * Recherche un produit par son nom exact.
     * @param nom nom du produit
     * @return le produit ou null
     */
    @Override
    public Produit getProduitByNom(String nom) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM produits WHERE nom = ?")) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Met à jour les champs modifiables d'un produit (prix, quantité, spécifique).
     * @param produit produit avec les nouvelles valeurs
     */
    @Override
    public void update(Produit produit) {
        String sql = "UPDATE produits SET prix_ht=?, quantite=?, date_peremption=?, duree_garantie=? WHERE nom=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, produit.getPrixHT());
            stmt.setInt(2, produit.getQuantiteStock());
            stmt.setDate(3, produit instanceof ProduitAlimentaire pa ? parseDateString(pa.getDatePeremption()) : null);
            stmt.setObject(4, produit instanceof ProduitElectronique pe ? pe.getDureeGarantie() : null, Types.INTEGER);
            stmt.setString(5, produit.getNom());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Supprime un produit par son nom.
     * @param nom nom du produit à supprimer
     */
    @Override
    public void deleteProduit(String nom) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM produits WHERE nom=?")) {
            stmt.setString(1, nom);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Convertit un ResultSet en objet Produit.
     * @param rs ligne de résultat SQL
     * @return Produit correspondant
     */
    private Produit mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        if ("alimentaire".equals(type)) {
            // Conversion DATE SQL -> format DD-MM-YYYY
            Date d = rs.getDate("date_peremption");
            String dateStr = d != null ? String.format("%02d-%02d-%04d",
                    d.toLocalDate().getDayOfMonth(), d.toLocalDate().getMonthValue(), d.toLocalDate().getYear()) : "";
            return new ProduitAlimentaire(rs.getString("nom"), rs.getDouble("prix_ht"), rs.getInt("quantite"), dateStr);
        } else if ("electronique".equals(type)) {
            return new ProduitElectronique(rs.getString("nom"), rs.getDouble("prix_ht"), rs.getInt("quantite"), rs.getInt("duree_garantie"));
        }
        return null;
    }

    /**
     * Parse une date au format DD-MM-YYYY vers java.sql.Date.
     * @param dateStr date sous forme de chaîne
     * @return java.sql.Date ou null si invalide
     */
    private Date parseDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return Date.valueOf(java.time.LocalDate.of(year, month, day));
            }
        } catch (Exception ignored) {}
        return null;
    }
}