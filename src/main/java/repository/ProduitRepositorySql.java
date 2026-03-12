package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import Model.Produit;
import Model.ProduitAlimentaire;
import Model.ProduitElectronique;

public class ProduitRepositorySql implements ProduitRepository {

    private final Connection connection;

    public ProduitRepositorySql(Connection connection) {
        this.connection = connection;
    }

// methode pour Ajouter un produit à la base de données
    public void save(Produit produit) {
        String sql = "INSERT INTO produits (nom, prix_ht, quantite, type, date_peremption, duree_garantie) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produit.getNom());
            stmt.setDouble(2, produit.getPrixHT());
            stmt.setInt   (3, produit.getQuantiteStock());

            if (produit instanceof ProduitAlimentaire) {
                stmt.setString(4, "alimentaire");
                stmt.setString(5, ((ProduitAlimentaire) produit).getDatePeremption());
                stmt.setNull(6, Types.INTEGER);
            } else if (produit instanceof ProduitElectronique) {
                stmt.setString(4, "electronique");
                stmt.setNull(5, Types.VARCHAR);
                stmt.setInt(6, ((ProduitElectronique) produit).getDureeGarantie());
            }
            stmt.executeUpdate();
            System.out.println("Produit " + produit.getNom() + " enregistré en base de données.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

// methode pour Afficher la liste des produits de la base de données
    public List<Produit> getAllProduits() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits";
        try(PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next())  {
                Produit p = mapRow(rs);
                if (p != null) produits.add(p);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return produits; // Placeholder
    }
//methode pour Rechercher un produit par son nom dans la base de données
    public Produit getProduitByNom(String nom) {
        String sql = "SELECT * FROM produits WHERE nom = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }      
    
//Methode pour Modifier un produit dans la base de données
    public void update(Produit produit) {
        String sql = "UPDATE produits SET prix_ht = ?, quantite = ? , date_peremption = ?, duree_garantie = ? " +  "WHERE nom = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, produit.getPrixHT());
            stmt.setInt(2, produit.getQuantiteStock());
            stmt.setString(3, produit instanceof ProduitAlimentaire ?
                 ((ProduitAlimentaire) produit).getDatePeremption() : null);
            stmt.setObject(4, produit instanceof ProduitElectronique ? 
                ((ProduitElectronique) produit).getDureeGarantie() : null, Types.INTEGER);
            stmt.setString(5, produit.getNom());
            stmt.executeUpdate();
            System.out.println("Produit " + produit.getNom() + " mis à jour en base de données.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//Methode pour Supprimer un produit de la base de données
    public void deleteProduit(String nom) {
        String sql = "DELETE FROM produits WHERE nom = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nom);
            stmt.executeUpdate();
            System.out.println("Produit " + nom + " supprimé de la base de données.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Produit mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        if ("alimentaire".equals(type)) {
            return new ProduitAlimentaire(
                rs.getString("nom"),
                rs.getDouble("prix_ht"),
                rs.getInt("quantite"),
                rs.getString("date_peremption")
            );
        } else if ("electronique".equals(type)) {
            return new ProduitElectronique(
                rs.getString("nom"),
                rs.getDouble("prix_ht"),
                rs.getInt("quantite"),
                rs.getInt("duree_garantie")
            );
        }
        return null;
    }
}
