package lisa1connexion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSpinner;

import java.sql.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static final String[] URLS = {
        "jdbc:oracle:thin:@192.168.10.32:1521:bdmatca",
        "jdbc:oracle:thin:@localhost:1521/bdmatca"
    };
    
    private static final String USER = "soft";
    private static final String PASSWORD = "part";
    private static int tentativesConnexion = 0;
    private static final int MAX_TENTATIVES = 3;
    
    private Stage primaryStage;
    private StackPane rootStackPane;
    private VBox mainContainer;

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage Application MATCA");
        System.out.println("==============================");
        
        afficherConfiguration();
        
        // Lancer JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Application MATCA");
        
        initializeUI();
        showSplashScreen();
        
        // Test de connectivité réseau
        new Thread(this::testConnexion).start();
    }
    
    private void initializeUI() {
        rootStackPane = new StackPane();
        rootStackPane.setStyle("-fx-background-color: #f5f5f5;");
        
        mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        
        rootStackPane.getChildren().add(mainContainer);
        
        Scene scene = new Scene(rootStackPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    private void showSplashScreen() {
        mainContainer.getChildren().clear();
        
        VBox splashContainer = new VBox(30);
        splashContainer.setAlignment(Pos.CENTER);
        splashContainer.setPadding(new Insets(40));
        
        // Logo/Titre
        Label title = new Label("Application MATCA");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitle = new Label("Connexion à la base de données...");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Spinner de chargement
        JFXSpinner spinner = new JFXSpinner();
        spinner.setRadius(40);
        
        splashContainer.getChildren().addAll(title, subtitle, spinner);
        mainContainer.getChildren().add(splashContainer);
        
        primaryStage.show();
    }
    
    private void testConnexion() {
        if (testerConnectiviteReseau()) {
            Platform.runLater(() -> showStatus("Test de connexion à la base de données...", "INFO"));
            
            if (testerConnexionBaseAvecDetection()) {
                Platform.runLater(() -> {
                    showStatus("Connexion réussie!", "SUCCESS");
                    lancerInterfaceAuthentification();
                });
            } else {
                tentativesConnexion++;
                Platform.runLater(() -> {
                    if (tentativesConnexion < MAX_TENTATIVES) {
                        proposerNouvelleTentative();
                    } else {
                        proposerConfigurationManuelle();
                    }
                });
            }
        } else {
            Platform.runLater(this::afficherErreurReseau);
        }
    }
    
    private void showStatus(String message, String type) {
        Platform.runLater(() -> {
            Label statusLabel = new Label(message);
            
            switch (type) {
                case "SUCCESS":
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    break;
                case "ERROR":
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    break;
                default:
                    statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            }
            
            if (!mainContainer.getChildren().isEmpty()) {
                VBox currentContent = (VBox) mainContainer.getChildren().get(0);
                if (currentContent.getChildren().size() > 2) {
                    currentContent.getChildren().set(2, statusLabel);
                } else {
                    currentContent.getChildren().add(statusLabel);
                }
            }
        });
    }
    
    private static void afficherConfiguration() {
        System.out.println("📋 Configuration testée :");
        System.out.println("   Serveur principal : 192.168.10.32");
        System.out.println("   Port : 1521");
        System.out.println("   Base : bdmatca");
        System.out.println("   Utilisateur : soft");
        System.out.println("   Tentatives : " + MAX_TENTATIVES);
        System.out.println("-----------------------------------");
    }
    
    private boolean testerConnectiviteReseau() {
        System.out.println("🔍 Test de connectivité réseau...");
        
        String[] serveurs = {"192.168.10.32", "localhost"};
        int port = 1521;
        boolean serveurTrouve = false;
        
        for (String serveur : serveurs) {
            System.out.println("   Test de " + serveur + ":" + port + "...");
            
            long debut = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(serveur, port), 1000);
                long duree = System.currentTimeMillis() - debut;
                
                System.out.println("✅ Serveur accessible : " + serveur + ":" + port + " (" + duree + "ms)");
                serveurTrouve = true;
                break;
                
            } catch (Exception e) {
                long duree = System.currentTimeMillis() - debut;
                System.out.println("❌ " + serveur + ":" + port + " inaccessible (" + duree + "ms)");
            }
        }
        
        return serveurTrouve;
    }
    
    private boolean testerConnexionBaseAvecDetection() {
        System.out.println("🔍 Test de connexion avec détection automatique...");
        
        for (String url : URLS) {
            System.out.println("   Test URL : " + url);
            
            if (testerUrlConnexion(url)) {
                System.out.println("✅ URL valide : " + url);
                return true;
            }
        }
        
        System.out.println("❌ Aucune URL valide trouvée");
        return false;
    }
    
    private boolean testerUrlConnexion(String url) {
        Connection conn = null;
        
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("oracle.net.CONNECT_TIMEOUT", "1000");
            
            long debut = System.currentTimeMillis();
            conn = DriverManager.getConnection(url, props);
            long duree = System.currentTimeMillis() - debut;
            
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
                    if (rs.next()) {
                        System.out.println("      ✅ Connexion réussie en " + duree + "ms");
                        return true;
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("      ❌ Échec : " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
        
        return false;
    }
    
    private void afficherErreurReseau() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label("❌ Problème de Connexion Réseau"));
        
        VBox body = new VBox(10);
        body.setPadding(new Insets(10));
        
        Label message = new Label("Impossible de contacter le serveur Oracle.");
        message.setStyle("-fx-font-size: 14px;");
        
        VBox serveursBox = new VBox(5);
        serveursBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");
        Label serveursTitle = new Label("Serveurs testés :");
        serveursTitle.setStyle("-fx-font-weight: bold;");
        Label serveur1 = new Label("• 192.168.10.32:1521");
        Label serveur2 = new Label("• localhost:1521");
        serveursBox.getChildren().addAll(serveursTitle, serveur1, serveur2);
        
        Label solutionsTitle = new Label("Solutions possibles :");
        solutionsTitle.setStyle("-fx-font-weight: bold;");
        VBox solutionsList = new VBox(5);
        solutionsList.getChildren().addAll(
            new Label("• Vérifiez la connexion réseau"),
            new Label("• Contactez l'administrateur système"),
            new Label("• Vérifiez l'adresse du serveur Oracle")
        );
        
        body.getChildren().addAll(message, serveursBox, solutionsTitle, solutionsList);
        content.setBody(body);
        
        JFXButton okButton = new JFXButton("Compris");
        okButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        content.setActions(okButton);
        
        JFXDialog dialog = new JFXDialog(rootStackPane, content, JFXDialog.DialogTransition.CENTER);
        okButton.setOnAction(e -> dialog.close());
        
        dialog.show();
    }
    
    private void proposerConfigurationManuelle() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label("⚙️ Configuration Manuelle"));
        
        VBox body = new VBox(15);
        body.setPadding(new Insets(10));
        
        Label instruction = new Label("Veuillez saisir les paramètres de connexion :");
        
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.setPadding(new Insets(10));
        
        TextField txtServeur = new TextField("192.168.10.32");
        TextField txtPort = new TextField("1521");
        TextField txtBase = new TextField("bdmatca");
        
        configGrid.add(new Label("Serveur:"), 0, 0);
        configGrid.add(txtServeur, 1, 0);
        configGrid.add(new Label("Port:"), 0, 1);
        configGrid.add(txtPort, 1, 1);
        configGrid.add(new Label("Base de données:"), 0, 2);
        configGrid.add(txtBase, 1, 2);
        
        body.getChildren().addAll(instruction, configGrid);
        content.setBody(body);
        
        JFXButton validerButton = new JFXButton("Valider");
        validerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        JFXButton annulerButton = new JFXButton("Annuler");
        annulerButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        
        content.setActions(annulerButton, validerButton);
        
        JFXDialog dialog = new JFXDialog(rootStackPane, content, JFXDialog.DialogTransition.CENTER);
        
        validerButton.setOnAction(e -> {
            String url = "jdbc:oracle:thin:@" + txtServeur.getText() + ":" + 
                        txtPort.getText() + ":" + txtBase.getText();
            
            if (testerUrlConnexion(url)) {
                showDialog("Succès", "✅ Configuration validée!\nL'application va redémarrer.", "SUCCESS");
                dialog.close();
                // Redémarrer l'application
                Platform.runLater(() -> new Main().start(new Stage()));
                primaryStage.close();
            } else {
                showDialog("Échec", "❌ La configuration n'a pas fonctionné.\nVérifiez les paramètres.", "ERROR");
            }
        });
        
        annulerButton.setOnAction(e -> dialog.close());
        
        dialog.show();
    }
    
    private void proposerNouvelleTentative() {
        System.out.println("🔄 Tentative " + tentativesConnexion + "/" + MAX_TENTATIVES);
        
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label("Nouvelle tentative"));
        
        Label message = new Label("Tentative " + tentativesConnexion + "/" + MAX_TENTATIVES + " échouée.\n" +
            "Voulez-vous réessayer avec d'autres paramètres?");
        
        content.setBody(message);
        
        JFXButton reessayerButton = new JFXButton("Réessayer");
        reessayerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        JFXButton configButton = new JFXButton("Configurer");
        configButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        
        content.setActions(configButton, reessayerButton);
        
        JFXDialog dialog = new JFXDialog(rootStackPane, content, JFXDialog.DialogTransition.CENTER);
        
        reessayerButton.setOnAction(e -> {
            dialog.close();
            showSplashScreen();
            new Thread(this::testConnexion).start();
        });
        
        configButton.setOnAction(e -> {
            dialog.close();
            proposerConfigurationManuelle();
        });
        
        dialog.show();
    }
    
    private void lancerInterfaceAuthentification() {
        System.out.println("🎨 Lancement de l'interface d'authentification...");
        
        Platform.runLater(() -> {
            try {
                // Fermer la fenêtre de connexion
                primaryStage.close();
                
                // Ouvrir l'interface d'authentification
                Stage authStage = new Stage();
                Authentification auth = new Authentification();
                auth.start(authStage);
                
                System.out.println("✅ Interface d'authentification prête");
            } catch (Exception e) {
                System.err.println("❌ Erreur interface: " + e.getMessage());
                showDialog("Erreur Interface", 
                    "Erreur lors du lancement de l'interface:\n" + e.getMessage(), "ERROR");
            }
        });
    }
}