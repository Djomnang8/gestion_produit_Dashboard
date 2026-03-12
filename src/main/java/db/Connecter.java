package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utilitaire de connexion à la base de données.
 * La configuration est lue depuis le fichier {@code config.properties}
 * placé à la racine du classpath (src/resources/).
 *
 * <p>Propriétés attendues :</p>
 * <ul>
 *   <li>{@code db.url}        — URL JDBC (ex: jdbc:mysql://localhost:3306/gestion_stock)</li>
 *   <li>{@code db.utilisateur} — nom d'utilisateur MySQL</li>
 *   <li>{@code db.motDePasse}  — mot de passe MySQL</li>
 * </ul>
 */
public class Connecter {

    /**
     * Ouvre et retourne une connexion à la base de données.
     * Les paramètres sont lus depuis {@code config.properties}.
     *
     * @return connexion JDBC active, ou {@code null} en cas d'erreur SQL
     * @throws RuntimeException si {@code config.properties} est introuvable
     */
    public static Connection connecter() {
        Connection con = null;
        try {
            Properties properties = new Properties();
            InputStream inputStream = Connecter.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (inputStream == null) {
                throw new IOException("Fichier config.properties non trouvé dans le classpath.");
            }
            properties.load(inputStream);

            String url          = properties.getProperty("db.url");
            String utilisateur  = properties.getProperty("db.utilisateur");
            String motDePasse   = properties.getProperty("db.motDePasse");

            con = DriverManager.getConnection(url, utilisateur, motDePasse);
            System.out.println("Connexion réussie à gestion_produits.");

        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur chargement config.properties : " + e.getMessage());
            throw new RuntimeException("Impossible de charger config.properties", e);
        }
        return con;
    }

    /**
     * Ferme proprement une connexion JDBC.
     *
     * @param connection connexion à fermer (peut être {@code null})
     */
    public static void fermerConnection(Connection connection) {
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