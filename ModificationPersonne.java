package lisa1connexion;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ModificationPersonne extends Application {
    
    private JFXTextField txtMatricule;
    private JFXButton btnRechercher, btnAnnuler;
    private Stage primaryStage;
    private SessionUtilisateur sessionUtilisateur;
    private boolean rechercheEffectuee = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionUtilisateur = SessionUtilisateur.getInstance();
        
        initInterface();
    }
    
    // Méthode pour initialiser avec des paramètres
    public void initialiser(SessionUtilisateur sessionUtilisateur) {
        this.sessionUtilisateur = sessionUtilisateur;
    }

    private void initInterface() {
        primaryStage.setTitle("Modifier une Personne");
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainPanel = new VBox(20);
        mainPanel.setPadding(new Insets(30));
        mainPanel.setStyle("-fx-background-color: white;");
        mainPanel.setAlignment(Pos.CENTER);
        
        // Titre
        Label lblTitre = new Label("Modifier une Personne");
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTitre.setTextFill(Color.web("#214080"));
        lblTitre.setAlignment(Pos.CENTER);
        
        // Instructions
        Label lblInstruction = new Label("Veuillez saisir le matricule de la personne à modifier :");
        lblInstruction.setFont(Font.font("Segoe UI", 14));
        lblInstruction.setAlignment(Pos.CENTER);
        
        // Champ de saisie
        txtMatricule = new JFXTextField();
        txtMatricule.setPromptText("Saisissez le matricule (8 chiffres)");
        txtMatricule.setPrefHeight(40);
        txtMatricule.setMaxWidth(300);
        txtMatricule.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
        txtMatricule.setLabelFloat(true);
        
        // Indication
        Label lblIndication = new Label("Appuyez sur Entrée ou cliquez sur Rechercher");
        lblIndication.setFont(Font.font("Segoe UI", Font.ITALIC, 12));
        lblIndication.setTextFill(Color.GRAY);
        lblIndication.setAlignment(Pos.CENTER);
        
        // Boutons
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);
        
        btnRechercher = new JFXButton("Rechercher");
        btnAnnuler = new JFXButton("Annuler");
        
        styliserBouton(btnRechercher, "#0078d7");
        styliserBouton(btnAnnuler, "#787878");
        
        btnRechercher.setPrefSize(120, 40);
        btnAnnuler.setPrefSize(120, 40);
        
        buttonPanel.getChildren().addAll(btnRechercher, btnAnnuler);
        
        // Assemblage
        mainPanel.getChildren().addAll(
            lblTitre, lblInstruction, txtMatricule, 
            lblIndication, buttonPanel
        );
        
        Scene scene = new Scene(mainPanel, 500, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        
        configurerActions();
        
        // Focus initial
        primaryStage.setOnShown(e -> {
            txtMatricule.requestFocus();
            txtMatricule.selectAll();
        });
        
        primaryStage.show();
    }
    
    private void styliserBouton(JFXButton bouton, String couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 5;",
            couleur
        ));
        bouton.setButtonType(JFXButton.ButtonType.RAISED);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 5;",
            darkenColor(couleur)
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 5;",
            couleur
        )));
    }
    
    private String darkenColor(String color) {
        return color.replace("0078d7", "0066b4")  // Bleu
                   .replace("787878", "656565");  // Gris
    }
    
    private void configurerActions() {
        // Action pour le bouton Rechercher
        btnRechercher.setOnAction(e -> rechercherPersonne());
        
        // Action pour le bouton Annuler
        btnAnnuler.setOnAction(e -> primaryStage.close());
        
        // Action pour la touche Entrée dans le champ matricule
        txtMatricule.setOnAction(e -> rechercherPersonne());
        
        // Gestion de la fermeture avec ESC
        primaryStage.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    primaryStage.close();
                    break;
                case ENTER:
                    rechercherPersonne();
                    break;
            }
        });
    }

    private void rechercherPersonne() {
        String matricule = txtMatricule.getText().trim();
        
        // Validation du matricule
        if (matricule.isEmpty()) {
            showAlert("Champ obligatoire", "Veuillez saisir un matricule.", Alert.AlertType.WARNING);
            txtMatricule.requestFocus();
            return;
        }
        
        // Validation du format du matricule (8 chiffres)
        if (!matricule.matches("\\d{8}")) {
            showAlert("Format invalide", "Le matricule doit contenir exactement 8 chiffres.", Alert.AlertType.WARNING);
            txtMatricule.selectAll();
            txtMatricule.requestFocus();
            return;
        }
        
        try {
            // Recherche de la personne dans la base de données
            Map<String, String> donneesPersonne = rechercherPersonneParMatricule(matricule);
            
            if (donneesPersonne != null && !donneesPersonne.isEmpty()) {
                String typePersonne = donneesPersonne.get("TYPERS_CODE");
                String nomPersonne = donneesPersonne.get("NOM_PERSONNE");
                
                rechercheEffectuee = true;
                
                showAlert("Recherche réussie", 
                    "Personne trouvée !\n" +
                    "Matricule: " + matricule + "\n" +
                    "Nom: " + nomPersonne + "\n" +
                    "Type: " + ("P".equals(typePersonne) ? "Personne Physique" : "Personne Morale"),
                    Alert.AlertType.INFORMATION);
                
                // Ouvrir le formulaire de modification approprié
                ouvrirFormulaireModification(matricule, typePersonne);
                
            } else {
                showAlert("Recherche infructueuse", 
                    "Aucune personne trouvée avec le matricule : " + matricule,
                    Alert.AlertType.ERROR);
                txtMatricule.selectAll();
                txtMatricule.requestFocus();
            }
        } catch (Exception ex) {
            showAlert("Erreur", 
                "Erreur lors de la recherche : " + ex.getMessage(),
                Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
    
    private Map<String, String> rechercherPersonneParMatricule(String matricule) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = sessionUtilisateur.getDatabaseUrl();
            String user = sessionUtilisateur.getDatabaseUser();
            String password = sessionUtilisateur.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            // Requête pour récupérer les données de base de la personne et déterminer son type
            String sql = "SELECT " +
                        "p.PERS_NUMMAT, p.TYPERS_CODE, p.PERS_ADRPOST, p.PERS_TELEPHONE, " +
                        "p.PERS_FAX, p.PERS_EMAIL, p.PERS_ADRGEOGR, p.PERS_WHATSAPP, " +
                        "CASE " +
                        "  WHEN p.TYPERS_CODE = 'P' THEN ph.PERSP_NOM || ' ' || ph.PERSP_PRENOM " +
                        "  WHEN p.TYPERS_CODE = 'M' THEN m.PERSM_RAISON " +
                        "  ELSE 'Inconnu' " +
                        "END as NOM_PERSONNE " +
                        "FROM PERSONNE p " +
                        "LEFT JOIN PHYSIQUE ph ON p.PERS_NUMMAT = ph.PERS_NUMMAT " +
                        "LEFT JOIN MORALE m ON p.PERS_NUMMAT = m.PERS_NUMMAT " +
                        "WHERE p.PERS_NUMMAT = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> donnees = new HashMap<>();
                donnees.put("PERS_NUMMAT", rs.getString("PERS_NUMMAT"));
                donnees.put("TYPERS_CODE", rs.getString("TYPERS_CODE"));
                donnees.put("NOM_PERSONNE", rs.getString("NOM_PERSONNE"));
                donnees.put("PERS_ADRPOST", rs.getString("PERS_ADRPOST"));
                donnees.put("PERS_TELEPHONE", rs.getString("PERS_TELEPHONE"));
                donnees.put("PERS_FAX", rs.getString("PERS_FAX"));
                donnees.put("PERS_EMAIL", rs.getString("PERS_EMAIL"));
                donnees.put("PERS_ADRGEOGR", rs.getString("PERS_ADRGEOGR"));
                donnees.put("PERS_WHATSAPP", rs.getString("PERS_WHATSAPP"));
                
                System.out.println("✅ Personne trouvée - Matricule: " + matricule + 
                                 ", Type: " + donnees.get("TYPERS_CODE") +
                                 ", Nom: " + donnees.get("NOM_PERSONNE"));
                
                return donnees;
            }
            
            System.out.println("❌ Aucune personne trouvée avec le matricule: " + matricule);
            return null;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private void ouvrirFormulaireModification(String matricule, String typePersonne) {
        try {
            if ("P".equals(typePersonne)) {
                // Ouvrir formulaire personne physique en mode modification
                System.out.println("🔧 Ouverture formulaire Modification Personne Physique pour: " + matricule);

                // Charger les données complètes de la personne physique
                Map<String, String> donneesCompletes = chargerDonneesPhysiqueCompletes(matricule);
                if (donneesCompletes != null) {
                    Stage modificationStage = new Stage();
                    CreationPersonnePhysique formulaire = new CreationPersonnePhysique();
                    formulaire.initialiser(sessionUtilisateur, donneesCompletes, true);
                    formulaire.start(modificationStage);
                    
                    // Fermer cette fenêtre après ouverture du formulaire
                    primaryStage.close();
                } else {
                    throw new Exception("Impossible de charger les données complètes de la personne physique");
                }

            } else if ("M".equals(typePersonne)) {
                // Ouvrir formulaire personne morale en mode modification
                System.out.println("🔧 Ouverture formulaire Modification Personne Morale pour: " + matricule);

                // Charger les données complètes de la personne morale
                Map<String, String> donneesCompletes = chargerDonneesMoraleCompletes(matricule);
                if (donneesCompletes != null) {
                    Stage modificationStage = new Stage();
                    CreationPersonneMorale formulaire = new CreationPersonneMorale();
                    formulaire.initialiser(sessionUtilisateur, donneesCompletes, true);
                    formulaire.start(modificationStage);
                    
                    // Fermer cette fenêtre après ouverture du formulaire
                    primaryStage.close();
                } else {
                    throw new Exception("Impossible de charger les données complètes de la personne morale");
                }

            } else {
                showAlert("Erreur", 
                    "Type de personne non reconnu: " + typePersonne,
                    Alert.AlertType.ERROR);
                return;
            }

        } catch (Exception ex) {
            showAlert("Erreur",
                "Erreur lors de l'ouverture du formulaire de modification: " + ex.getMessage(),
                Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
    
    private Map<String, String> chargerDonneesPhysiqueCompletes(String matricule) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = sessionUtilisateur.getDatabaseUrl();
            String user = sessionUtilisateur.getDatabaseUser();
            String password = sessionUtilisateur.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT p.*, ph.* FROM PERSONNE p " +
                        "JOIN PHYSIQUE ph ON p.PERS_NUMMAT = ph.PERS_NUMMAT " +
                        "WHERE p.PERS_NUMMAT = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> donnees = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    donnees.put(columnName, value != null ? value : "");
                }
                
                System.out.println("✅ Données PHYSIQUE chargées - " + donnees.size() + " colonnes");
                return donnees;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement données PHYSIQUE: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private Map<String, String> chargerDonneesMoraleCompletes(String matricule) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = sessionUtilisateur.getDatabaseUrl();
            String user = sessionUtilisateur.getDatabaseUser();
            String password = sessionUtilisateur.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT p.*, m.* FROM PERSONNE p " +
                        "JOIN MORALE m ON p.PERS_NUMMAT = m.PERS_NUMMAT " +
                        "WHERE p.PERS_NUMMAT = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> donnees = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    donnees.put(columnName, value != null ? value : "");
                }
                
                System.out.println("✅ Données MORALE chargées - " + donnees.size() + " colonnes");
                return donnees;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement données MORALE: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Méthode pour savoir si une recherche a été effectuée avec succès
    public boolean isRechercheEffectuee() {
        return rechercheEffectuee;
    }
    
    // Méthode pour obtenir le matricule saisi
    public String getMatriculeSaisi() {
        return txtMatricule.getText().trim();
    }

    public static void main(String[] args) {
        launch(args);
    }
}