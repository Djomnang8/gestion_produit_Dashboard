import Controller.ProduitController;
import Repository.ProduitRepository;
import Repository.ProduitRepositorySql;
import Service.ProduitServiceImpl;
import View.ClientView;
import View.ProduitView;
import db.Connecter;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        // Connexion à la base de données
        Connection connection = Connecter.connecter();
        if (connection == null) {
            System.out.println("Impossible de se connecter à la base.");
            return;
        }

        ProduitRepository produitRepository = new ProduitRepositorySql(connection);
        ProduitServiceImpl produitService = new ProduitServiceImpl(produitRepository);
        ProduitController produitController = new ProduitController(produitService);


        new ProduitView(produitController);

        //new ClientView(produitController);
    }
}