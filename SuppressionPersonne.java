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
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class SuppressionPersonne extends Application {
    
    private JFXTextField matriculeField;
    private JFXButton rechercherButton, annulerButton;
    private boolean confirmed = false;
    private String matricule;
    private SessionUtilisateur session;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.session = SessionUtilisateur.getInstance();
        
        initInterface();
    }

    private void initInterface() {
        primaryStage.setTitle("Supprimer une Personne");
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainPanel = new VBox(20);
        mainPanel.setPadding(new Insets(30));
        mainPanel.setStyle("-fx-background-color: white;");
        mainPanel.setAlignment(Pos.CENTER);
        
        // Titre
        Label lblTitre = new Label("Supprimer une Personne");
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTitre.setTextFill(Color.web("#214080"));
        lblTitre.setAlignment(Pos.CENTER);
        
        // Instructions
        Label lblInstruction = new Label("Veuillez saisir le matricule de la personne à supprimer :");
        lblInstruction.setFont(Font.font("Segoe UI", 16));
        lblInstruction.setAlignment(Pos.CENTER);
        
        // Champ de saisie
        matriculeField = new JFXTextField();
        matriculeField.setPromptText("Saisissez le matricule (8 chiffres)");
        matriculeField.setPrefHeight(40);
        matriculeField.setMaxWidth(300);
        matriculeField.setStyle("-fx-font-size: 16px; -fx-background-radius: 5;");
        matriculeField.setLabelFloat(true);
        
        // Indication
        Label lblIndication = new Label("Appuyez sur Entrée ou cliquez sur Rechercher");
        lblIndication.setFont(Font.font("Segoe UI", Font.ITALIC, 14));
        lblIndication.setTextFill(Color.GRAY);
        lblIndication.setAlignment(Pos.CENTER);
        
        // Boutons
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);
        
        rechercherButton = new JFXButton("Rechercher");
        annulerButton = new JFXButton("Annuler");
        
        styliserBouton(rechercherButton, "#0078d7");
        styliserBouton(annulerButton, "#787878");
        
        rechercherButton.setPrefSize(130, 40);
        annulerButton.setPrefSize(130, 40);
        
        buttonPanel.getChildren().addAll(rechercherButton, annulerButton);
        
        // Assemblage
        mainPanel.getChildren().addAll(
            lblTitre, lblInstruction, matriculeField, 
            lblIndication, buttonPanel
        );
        
        Scene scene = new Scene(mainPanel, 500, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        
        configurerActions();
        
        // Focus initial
        primaryStage.setOnShown(e -> {
            matriculeField.requestFocus();
            matriculeField.selectAll();
        });
        
        primaryStage.show();
    }
    
    private void styliserBouton(JFXButton bouton, String couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 15px; -fx-background-radius: 5;",
            couleur
        ));
        bouton.setButtonType(JFXButton.ButtonType.RAISED);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 15px; -fx-background-radius: 5;",
            darkenColor(couleur)
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 15px; -fx-background-radius: 5;",
            couleur
        )));
    }
    
    private String darkenColor(String color) {
        return color.replace("0078d7", "0066b4")  // Bleu
                   .replace("787878", "656565");  // Gris
    }
    
    private void configurerActions() {
        rechercherButton.setOnAction(e -> rechercherPersonne());
        annulerButton.setOnAction(e -> primaryStage.close());
        matriculeField.setOnAction(e -> rechercherPersonne());
        
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
        matricule = matriculeField.getText().trim();
        
        if (matricule.isEmpty()) {
            showAlert("Champ obligatoire", "Veuillez saisir un matricule.", Alert.AlertType.WARNING);
            matriculeField.requestFocus();
            return;
        }
        
        if (!matricule.matches("\\d{8}")) {
            showAlert("Format invalide", "Le matricule doit contenir exactement 8 chiffres.", Alert.AlertType.WARNING);
            matriculeField.selectAll();
            matriculeField.requestFocus();
            return;
        }
        
        try {
            Map<String, String> donneesPersonne = rechercherPersonneParMatricule(matricule);
            
            if (donneesPersonne != null && !donneesPersonne.isEmpty()) {
                String topVigueur = donneesPersonne.get("PERS_TOPVIGUEUR");
                if ("1".equals(topVigueur)) {
                    String nomPersonne = donneesPersonne.get("NOM_PERSONNE");
                    showAlert("Personne déjà supprimée", 
                        "❌ Cette personne est déjà supprimée !\n\n" +
                        "Matricule: " + matricule + "\n" +
                        "Nom: " + nomPersonne + "\n\n" +
                        "Impossible de supprimer une personne déjà supprimée.",
                        Alert.AlertType.WARNING);
                    matriculeField.selectAll();
                    matriculeField.requestFocus();
                    return;
                }
                
                String typePersonne = donneesPersonne.get("TYPERS_CODE");
                String nomPersonne = donneesPersonne.get("NOM_PERSONNE");
                
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation de suppression");
                confirmation.setHeaderText("Confirmation de suppression");
                confirmation.setContentText("Voulez-vous vraiment supprimer cette personne ?\n\n" +
                    "Matricule: " + matricule + "\n" +
                    "Nom: " + nomPersonne + "\n" +
                    "Type: " + ("P".equals(typePersonne) ? "Personne Physique" : "Personne Morale"));
                
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        ouvrirFenetreDetaillee(matricule, typePersonne);
                    }
                });
                
            } else {
                showAlert("Recherche infructueuse", 
                    "Aucune personne trouvée avec le matricule : " + matricule,
                    Alert.AlertType.ERROR);
                matriculeField.selectAll();
                matriculeField.requestFocus();
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
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT " +
                        "p.PERS_NUMMAT, p.TYPERS_CODE, p.PERS_ADRPOST, p.PERS_TELEPHONE, " +
                        "p.PERS_FAX, p.PERS_EMAIL, p.PERS_ADRGEOGR, p.PERS_WHATSAPP, p.PERS_TOPVIGUEUR, " +
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
                donnees.put("PERS_TOPVIGUEUR", rs.getString("PERS_TOPVIGUEUR"));
                
                return donnees;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la recherche: " + e.getMessage());
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private void ouvrirFenetreDetaillee(String matricule, String typePersonne) {
        try {
            if ("P".equals(typePersonne)) {
                Stage detailStage = new Stage();
                FenetreDetailleeSuppressionPhysique fenetre = new FenetreDetailleeSuppressionPhysique();
                fenetre.initialiser(matricule, session);
                fenetre.start(detailStage);
                
            } else if ("M".equals(typePersonne)) {
                Stage detailStage = new Stage();
                FenetreDetailleeSuppressionMorale fenetre = new FenetreDetailleeSuppressionMorale();
                fenetre.initialiser(matricule, session);
                fenetre.start(detailStage);
            }
        } catch (Exception ex) {
            showAlert("Erreur",
                "Erreur lors de l'ouverture de la fenêtre de détail: " + ex.getMessage(),
                Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }

    public String getMatricule() {
        return matricule;
    }
    
    public static String showDialog(Stage parentStage) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        SuppressionPersonne dialog = new SuppressionPersonne();
        dialog.start(dialogStage);
        
        return dialog.isConfirmed() ? dialog.getMatricule() : null;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// Classe pour la fenêtre détaillée de suppression - Personne Physique
class FenetreDetailleeSuppressionPhysique extends Application {
    private boolean suppressionConfirmee = false;
    private String matricule;
    private SessionUtilisateur session;
    private Map<String, String> donneesCompletes;
    
    private JFXTextField txtMatricule, txtNom, txtPrenom, txtDateNaissance, txtLieuNaissance;
    private JFXTextField txtNNI, txtTelephone, txtEmail, txtAdresseGeographique;
    private JFXRadioButton radioMasculin, radioFeminin;
    private JFXButton btnConfirmerSuppression, btnAnnuler;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initInterface();
        peuplerChamps();
    }
    
    public void initialiser(String matricule, SessionUtilisateur session) {
        this.matricule = matricule;
        this.session = session;
        chargerDonneesCompletes();
        
        if (donneesCompletes != null) {
            String topVigueur = donneesCompletes.get("PERS_TOPVIGUEUR");
            if ("1".equals(topVigueur)) {
                showAlert("Personne déjà supprimée", 
                    "❌ Cette personne est déjà supprimée !\n\n" +
                    "Matricule: " + matricule + "\n" +
                    "Nom: " + donneesCompletes.get("PERSP_NOM") + " " + donneesCompletes.get("PERSP_PRENOM") + "\n\n" +
                    "Impossible de supprimer une personne déjà supprimée.",
                    Alert.AlertType.WARNING);
                return;
            }
        }
    }
    
    private void chargerDonneesCompletes() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT p.*, ph.* FROM PERSONNE p " +
                        "JOIN PHYSIQUE ph ON p.PERS_NUMMAT = ph.PERS_NUMMAT " +
                        "WHERE p.PERS_NUMMAT = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                donneesCompletes = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    donneesCompletes.put(columnName, value != null ? value : "");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement données PHYSIQUE: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private void initInterface() {
        primaryStage.setTitle("🗑️ Suppression Personne Physique");
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainPanel = new VBox(20);
        mainPanel.setPadding(new Insets(25));
        mainPanel.setStyle("-fx-background-color: white;");
        
        // Titre
        Label lblTitre = new Label("🗑️ Suppression Personne Physique");
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTitre.setTextFill(Color.RED);
        lblTitre.setAlignment(Pos.CENTER);
        
        // Panels
        VBox panelInfosPerso = creerPanelInfosPersonnelles();
        VBox panelCoordonnees = creerPanelCoordonnees();
        HBox panelBoutons = creerPanelBoutons();
        
        // ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        VBox contentPanel = new VBox(20);
        contentPanel.setPadding(new Insets(10));
        contentPanel.getChildren().addAll(panelInfosPerso, panelCoordonnees);
        scrollPane.setContent(contentPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        mainPanel.getChildren().addAll(lblTitre, scrollPane, panelBoutons);
        
        Scene scene = new Scene(mainPanel, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        
        configurerEcouteurs();
        primaryStage.show();
    }
    
    private VBox creerPanelInfosPersonnelles() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 2;");
        
        Label titleLabel = new Label("📋 Informations Personnelles");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.RED);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        
        // Matricule
        grid.add(creerLabel("🆔 Matricule:"), 0, 0);
        txtMatricule = creerTextField(true);
        grid.add(txtMatricule, 1, 0);
        
        // Nom
        grid.add(creerLabel("👤 Nom:"), 0, 1);
        txtNom = creerTextField(true);
        grid.add(txtNom, 1, 1);
        
        // Prénom
        grid.add(creerLabel("👤 Prénom:"), 0, 2);
        txtPrenom = creerTextField(true);
        grid.add(txtPrenom, 1, 2);
        
        // Date de naissance
        grid.add(creerLabel("📅 Date de naissance:"), 0, 3);
        txtDateNaissance = creerTextField(true);
        grid.add(txtDateNaissance, 1, 3);
        
        // Lieu de naissance
        grid.add(creerLabel("🏠 Lieu de naissance:"), 0, 4);
        txtLieuNaissance = creerTextField(true);
        grid.add(txtLieuNaissance, 1, 4);
        
        // Sexe
        grid.add(creerLabel("⚧ Sexe:"), 0, 5);
        HBox panelSexe = new HBox(10);
        panelSexe.setAlignment(Pos.CENTER_LEFT);
        radioMasculin = new JFXRadioButton("Masculin");
        radioFeminin = new JFXRadioButton("Féminin");
        ToggleGroup groupeSexe = new ToggleGroup();
        radioMasculin.setToggleGroup(groupeSexe);
        radioFeminin.setToggleGroup(groupeSexe);
        radioMasculin.setDisable(true);
        radioFeminin.setDisable(true);
        panelSexe.getChildren().addAll(radioMasculin, radioFeminin);
        grid.add(panelSexe, 1, 5);
        
        // NNI
        grid.add(creerLabel("🔢 NNI:"), 0, 6);
        txtNNI = creerTextField(true);
        grid.add(txtNNI, 1, 6);
        
        panel.getChildren().addAll(titleLabel, grid);
        return panel;
    }
    
    private VBox creerPanelCoordonnees() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 2;");
        
        Label titleLabel = new Label("📞 Coordonnées");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.RED);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        
        // Téléphone
        grid.add(creerLabel("📱 Téléphone:"), 0, 0);
        txtTelephone = creerTextField(true);
        grid.add(txtTelephone, 1, 0);
        
        // Email
        grid.add(creerLabel("📧 Email:"), 0, 1);
        txtEmail = creerTextField(true);
        grid.add(txtEmail, 1, 1);
        
        // Adresse
        grid.add(creerLabel("🏠 Adresse:"), 0, 2);
        txtAdresseGeographique = creerTextField(true);
        grid.add(txtAdresseGeographique, 1, 2);
        
        panel.getChildren().addAll(titleLabel, grid);
        return panel;
    }
    
    private HBox creerPanelBoutons() {
        HBox panel = new HBox(25);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 10, 0));
        
        btnAnnuler = new JFXButton("❌ Annuler");
        btnConfirmerSuppression = new JFXButton("🗑️ Confirmer la Suppression");
        
        styliserBouton(btnAnnuler, "#6c757d");
        styliserBouton(btnConfirmerSuppression, "#dc3545");
        
        panel.getChildren().addAll(btnAnnuler, btnConfirmerSuppression);
        return panel;
    }
    
    private Label creerLabel(String texte) {
        Label label = new Label(texte);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        return label;
    }
    
    private JFXTextField creerTextField(boolean desactive) {
        JFXTextField champ = new JFXTextField();
        champ.setPrefHeight(35);
        champ.setPrefWidth(300);
        champ.setStyle("-fx-font-size: 14px;");
        if (desactive) {
            champ.setDisable(true);
            champ.setStyle("-fx-font-size: 14px; -fx-background-color: #e9ecef;");
        }
        return champ;
    }
    
    private void styliserBouton(JFXButton bouton, String couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-padding: 12 30 12 30;",
            couleur
        ));
        bouton.setButtonType(JFXButton.ButtonType.RAISED);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-padding: 12 30 12 30;",
            darkenColor(couleur)
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-padding: 12 30 12 30;",
            couleur
        )));
    }
    
    private String darkenColor(String color) {
        return color.replace("6c757d", "495057")   // Gris
                   .replace("dc3545", "c82333");  // Rouge
    }
    
    private void configurerEcouteurs() {
        btnConfirmerSuppression.setOnAction(e -> confirmerSuppression());
        btnAnnuler.setOnAction(e -> primaryStage.close());
    }
    
    private void peuplerChamps() {
        if (donneesCompletes != null) {
            txtMatricule.setText(matricule);
            txtNom.setText(donneesCompletes.get("PERSP_NOM"));
            txtPrenom.setText(donneesCompletes.get("PERSP_PRENOM"));
            txtDateNaissance.setText(formatDate(donneesCompletes.get("PERSP_DATNAIS")));
            txtLieuNaissance.setText(donneesCompletes.get("PERSP_LIEUNAIS"));
            txtNNI.setText(donneesCompletes.get("PERSP_NNI"));
            txtTelephone.setText(donneesCompletes.get("PERS_TELEPHONE"));
            txtEmail.setText(donneesCompletes.get("PERS_EMAIL"));
            txtAdresseGeographique.setText(donneesCompletes.get("PERS_ADRGEOGR"));
            
            String sexe = donneesCompletes.get("PERSP_SEXE");
            if ("M".equals(sexe)) {
                radioMasculin.setSelected(true);
            } else if ("F".equals(sexe)) {
                radioFeminin.setSelected(true);
            }
        }
    }
    
    private String formatDate(String date) {
        if (date == null || date.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date d = sdf.parse(date);
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy");
            return sdfDisplay.format(d);
        } catch (Exception e) {
            return date;
        }
    }
    
    private void confirmerSuppression() {
        if (verifierDejaSupprime()) {
            return;
        }
        
        Alert confirmationFinale = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationFinale.setTitle("CONFIRMATION FINALE DE SUPPRESSION");
        confirmationFinale.setHeaderText("Confirmation finale");
        confirmationFinale.setContentText("Êtes-vous ABSOLUMENT sûr de vouloir supprimer définitivement cette personne ?\n\n" +
            "Matricule: " + matricule + "\n" +
            "Nom: " + txtNom.getText() + " " + txtPrenom.getText() + "\n\n" +
            "⚠️  Cette action est irréversible !");
        
        confirmationFinale.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (effectuerSuppression()) {
                    suppressionConfirmee = true;
                    showAlert("Suppression réussie", "✅ Personne physique supprimée avec succès!", Alert.AlertType.INFORMATION);
                    primaryStage.close();
                }
            }
        });
    }
    
    private boolean verifierDejaSupprime() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT PERS_TOPVIGUEUR FROM PERSONNE WHERE PERS_NUMMAT = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String topVigueur = rs.getString("PERS_TOPVIGUEUR");
                if ("1".equals(topVigueur)) {
                    showAlert("Personne déjà supprimée", 
                        "❌ Cette personne a été supprimée entre-temps !\n\n" +
                        "Matricule: " + matricule + "\n" +
                        "Nom: " + txtNom.getText() + " " + txtPrenom.getText() + "\n\n" +
                        "Impossible de supprimer une personne déjà supprimée.",
                        Alert.AlertType.WARNING);
                    primaryStage.close();
                    return true;
                }
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification TOPVIGUEUR: " + e.getMessage());
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private boolean effectuerSuppression() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "UPDATE PERSONNE SET PERS_TOPVIGUEUR = '1' WHERE PERS_NUMMAT = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Suppression logique effectuée pour le matricule: " + matricule);
                return true;
            } else {
                showAlert("Erreur", "Aucune personne trouvée avec ce matricule.", Alert.AlertType.ERROR);
                return false;
            }
            
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        } finally {
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
    
    public boolean isSuppressionConfirmee() {
        return suppressionConfirmee;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// Classe pour la fenêtre détaillée de suppression - Personne Morale
class FenetreDetailleeSuppressionMorale extends Application {
    private boolean suppressionConfirmee = false;
    private String matricule;
    private SessionUtilisateur session;
    private Map<String, String> donneesCompletes;
    
    private JFXTextField txtMatricule, txtRaisonSociale, txtSigle, txtDateCreation;
    private JFXTextField txtNomRepresentant, txtFonctionRepresentant, txtTelephone, txtEmail, txtAdresseGeographique;
    private JFXComboBox<String> comboFormeJuridique;
    private JFXButton btnConfirmerSuppression, btnAnnuler;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initInterface();
        peuplerChamps();
    }
    
    public void initialiser(String matricule, SessionUtilisateur session) {
        this.matricule = matricule;
        this.session = session;
        chargerDonneesCompletes();
        
        if (donneesCompletes != null) {
            String topVigueur = donneesCompletes.get("PERS_TOPVIGUEUR");
            if ("1".equals(topVigueur)) {
                showAlert("Personne déjà supprimée", 
                    "❌ Cette personne morale est déjà supprimée !\n\n" +
                    "Matricule: " + matricule + "\n" +
                    "Raison sociale: " + donneesCompletes.get("PERSM_RAISON") + "\n\n" +
                    "Impossible de supprimer une personne déjà supprimée.",
                    Alert.AlertType.WARNING);
                return;
            }
        }
    }
    
    // Les autres méthodes restent similaires à FenetreDetailleeSuppressionPhysique
    // avec adaptation pour les données des personnes morales
    
    private void chargerDonneesCompletes() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT p.*, m.* FROM PERSONNE p " +
                        "JOIN MORALE m ON p.PERS_NUMMAT = m.PERS_NUMMAT " +
                        "WHERE p.PERS_NUMMAT = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                donneesCompletes = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    donneesCompletes.put(columnName, value != null ? value : "");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement données MORALE: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    // Les méthodes d'interface et de logique restent similaires...
    // (creerPanelInformations, creerPanelRepresentant, etc.)
    
    public boolean isSuppressionConfirmee() {
        return suppressionConfirmee;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}