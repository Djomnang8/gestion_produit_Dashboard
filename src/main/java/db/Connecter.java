package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Connecter {

    public static java.sql.Connection connecter() {
        java.sql.Connection con = null;
        try{
            Properties properties = new Properties();
            InputStream inputStream = Connecter.class
                .getClassLoader()
                .getResourceAsStream("config.properties");

            if (inputStream == null) {
                throw new IOException("Fichier config.properties non trouvé dans le classpath.");
            }
            properties.load(inputStream);

            String url         = properties.getProperty("db.url");
            String utilisateur = properties.getProperty("db.utilisateur");
            String motDePasse  = properties.getProperty("db.motDePasse");

            con = DriverManager.getConnection(url, utilisateur, motDePasse);
            System.out.println("Connexion réussie à gestion_produit.");

        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur chargement config.properties : " + e.getMessage());
            throw new RuntimeException("Impossible de charger config.properties", e);
        }
        return con;
    }
        
    public static void fermerConnection(java.sql.Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

}
