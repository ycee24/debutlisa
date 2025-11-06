package lisa1connexion;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
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
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class CreationPersonnePhysique extends Application {
    
    private JFXTextField txtMatricule, txtNom, txtPrenom, txtDateNaissance, txtLieuNaissance;
    private JFXTextField txtNomJeuneFille, txtTypePiece, txtNumPiece, txtDateDelPiece, txtLieuDelPiece;
    private JFXTextField txtNomMere, txtNomPere, txtNumPC, txtCatPC, txtDateDelPC, txtLieuDelPC;
    private JFXTextField txtEmploye, txtFonction, txtDateDeces, txtNNI;
    private JFXTextField txtAdressePostale, txtTelephone, txtFax, txtEmail, txtAdresseGeographique, txtWhatsapp;
    private JFXComboBox<String> comboCivilite, comboProfession, comboTypePiece, comboCatPC, comboCodeVIP;
    private JFXComboBox<String> comboPays, comboLieuGestion;
    private JFXRadioButton radioMasculin, radioFeminin;
    private JFXButton btnSauvegarder, btnAnnuler, btnSupprimer;
    private SessionUtilisateur session;
    private Map<String, String> donneesExistantes;
    
    private boolean isModification = false;
    private boolean modificationReussie = false;
    private String matriculeModification;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // Ces paramètres devraient être passés via une méthode d'initialisation
        this.session = SessionUtilisateur.getInstance();
        this.isModification = false;
        
        initInterface();
        chargerDonneesInitiales();
    }
    
    // Méthode pour initialiser avec des paramètres
    public void initialiser(SessionUtilisateur session, Map<String, String> donneesExistantes, boolean isModification) {
        this.session = session;
        this.donneesExistantes = donneesExistantes;
        this.isModification = isModification;
        
        if (isModification && donneesExistantes != null) {
            this.matriculeModification = donneesExistantes.get("PERS_NUMMAT");
        }
        
        if (primaryStage != null) {
            primaryStage.setTitle(isModification ? "✏️ Modifier Personne Physique" : "➕ Créer Personne Physique");
            if (isModification) {
                peuplerChampsAvecDonneesExistantes();
            }
        }
    }
    
    private void initInterface() {
        String titre = isModification ? "✏️ Modifier Personne Physique" : "➕ Créer Personne Physique";
        primaryStage.setTitle(titre);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainPanel = new VBox(20);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");
        
        // Titre
        Label lblTitre = new Label(titre);
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitre.setTextFill(Color.web("#2c3e50"));
        lblTitre.setAlignment(Pos.CENTER);
        
        // Panels
        VBox panelInfosPerso = creerPanelInfosPersonnelles();
        VBox panelCoordonnees = creerPanelCoordonnees();
        HBox panelBoutons = creerPanelBoutons();
        
        // ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        VBox contentPanel = new VBox(15);
        contentPanel.setPadding(new Insets(10));
        contentPanel.getChildren().addAll(panelInfosPerso, panelCoordonnees);
        scrollPane.setContent(contentPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        
        mainPanel.getChildren().addAll(lblTitre, scrollPane, panelBoutons);
        
        Scene scene = new Scene(mainPanel, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        
        configurerEcouteurs();
    }
    
    private VBox creerPanelInfosPersonnelles() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ff8c00; -fx-border-width: 2;");
        
        Label titleLabel = new Label("📋 Informations Personnelles");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#ff8c00"));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        
        // Matricule
        grid.add(creerLabel("🆔 Matricule:"), 0, 0);
        txtMatricule = creerTextField();
        if (isModification) {
            txtMatricule.setDisable(true);
            txtMatricule.setStyle("-fx-background-color: #e9ecef;");
        }
        grid.add(txtMatricule, 1, 0);
        
        // Nom
        grid.add(creerLabel("👤 Nom:"), 0, 1);
        txtNom = creerTextField();
        grid.add(txtNom, 1, 1);
        
        // Prénom
        grid.add(creerLabel("👤 Prénom:"), 0, 2);
        txtPrenom = creerTextField();
        grid.add(txtPrenom, 1, 2);
        
        // Date de naissance
        grid.add(creerLabel("📅 Date de naissance:"), 0, 3);
        txtDateNaissance = creerTextField();
        grid.add(txtDateNaissance, 1, 3);
        
        // Lieu de naissance
        grid.add(creerLabel("🏠 Lieu de naissance:"), 0, 4);
        txtLieuNaissance = creerTextField();
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
        panelSexe.getChildren().addAll(radioMasculin, radioFeminin);
        grid.add(panelSexe, 1, 5);
        
        // NNI
        grid.add(creerLabel("🔢 NNI:"), 0, 6);
        txtNNI = creerTextField();
        grid.add(txtNNI, 1, 6);
        
        panel.getChildren().addAll(titleLabel, grid);
        return panel;
    }
    
    private VBox creerPanelCoordonnees() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ff8c00; -fx-border-width: 2;");
        
        Label titleLabel = new Label("📞 Coordonnées");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#ff8c00"));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        
        // Téléphone
        grid.add(creerLabel("📱 Téléphone:"), 0, 0);
        txtTelephone = creerTextField();
        grid.add(txtTelephone, 1, 0);
        
        // Email
        grid.add(creerLabel("📧 Email:"), 0, 1);
        txtEmail = creerTextField();
        grid.add(txtEmail, 1, 1);
        
        // Adresse
        grid.add(creerLabel("🏠 Adresse:"), 0, 2);
        txtAdresseGeographique = creerTextField();
        grid.add(txtAdresseGeographique, 1, 2);
        
        panel.getChildren().addAll(titleLabel, grid);
        return panel;
    }
    
    private HBox creerPanelBoutons() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 10, 0));
        
        btnAnnuler = new JFXButton("❌ Annuler");
        btnSauvegarder = new JFXButton(isModification ? "💾 Enregistrer modifications" : "✅ Sauvegarder");
        
        styliserBouton(btnAnnuler, "#6c757d");
        styliserBouton(btnSauvegarder, "#28a745");
        
        if (isModification) {
            btnSupprimer = new JFXButton("🗑️ Supprimer");
            styliserBouton(btnSupprimer, "#dc3545");
        }
        
        panel.getChildren().add(btnAnnuler);
        if (isModification) {
            panel.getChildren().add(btnSupprimer);
        }
        panel.getChildren().add(btnSauvegarder);
        
        return panel;
    }
    
    private Label creerLabel(String texte) {
        Label label = new Label(texte);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        return label;
    }
    
    private JFXTextField creerTextField() {
        JFXTextField champ = new JFXTextField();
        champ.setPrefHeight(35);
        champ.setPrefWidth(300);
        champ.setStyle("-fx-font-size: 14px;");
        return champ;
    }
    
    private void styliserBouton(JFXButton bouton, String couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-padding: 12 25 12 25;",
            couleur
        ));
        bouton.setButtonType(JFXButton.ButtonType.RAISED);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-padding: 12 25 12 25;",
            darkenColor(couleur)
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-padding: 12 25 12 25;",
            couleur
        )));
    }
    
    private String darkenColor(String color) {
        // Simplification pour assombrir la couleur
        return color.replace("28a745", "218838")  // Vert
                   .replace("6c757d", "495057")   // Gris
                   .replace("dc3545", "c82333");  // Rouge
    }
    
    private void configurerEcouteurs() {
        btnSauvegarder.setOnAction(e -> sauvegarder());
        btnAnnuler.setOnAction(e -> annuler());
        if (isModification && btnSupprimer != null) {
            btnSupprimer.setOnAction(e -> supprimerPersonne());
        }
    }
    
    private void chargerDonneesInitiales() {
        // Charger les données initiales pour les combobox
        // (civilites, professions, etc.)
    }
    
    private void peuplerChampsAvecDonneesExistantes() {
        if (donneesExistantes != null) {
            txtMatricule.setText(donneesExistantes.get("PERS_NUMMAT"));
            txtNom.setText(donneesExistantes.get("PERSP_NOM"));
            txtPrenom.setText(donneesExistantes.get("PERSP_PRENOM"));
            txtDateNaissance.setText(formatDate(donneesExistantes.get("PERSP_DATNAIS")));
            txtLieuNaissance.setText(donneesExistantes.get("PERSP_LIEUNAIS"));
            txtNNI.setText(donneesExistantes.get("PERSP_NNI"));
            txtTelephone.setText(donneesExistantes.get("PERS_TELEPHONE"));
            txtEmail.setText(donneesExistantes.get("PERS_EMAIL"));
            txtAdresseGeographique.setText(donneesExistantes.get("PERS_ADRGEOGR"));
            
            String sexe = donneesExistantes.get("PERSP_SEXE");
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
            Date d = sdf.parse(date);
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy");
            return sdfDisplay.format(d);
        } catch (Exception e) {
            return date;
        }
    }
    
    private void sauvegarder() {
        if (!validerFormulaire()) {
            return;
        }
        
        if (isModification) {
            if (mettreAJourPersonnePhysique()) {
                modificationReussie = true;
                showAlert("Modification réussie", "✅ Personne physique modifiée avec succès!", Alert.AlertType.INFORMATION);
                primaryStage.close();
            }
        } else {
            if (enregistrerPersonnePhysique()) {
                showAlert("Création réussie", "✅ Personne physique créée avec succès!", Alert.AlertType.INFORMATION);
                primaryStage.close();
            }
        }
    }
    
    private boolean validerFormulaire() {
        if (txtMatricule.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez saisir le matricule.", Alert.AlertType.ERROR);
            txtMatricule.requestFocus();
            return false;
        }
        
        if (txtNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez saisir le nom.", Alert.AlertType.ERROR);
            txtNom.requestFocus();
            return false;
        }
        
        if (txtPrenom.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez saisir le prénom.", Alert.AlertType.ERROR);
            txtPrenom.requestFocus();
            return false;
        }
        
        if (!radioMasculin.isSelected() && !radioFeminin.isSelected()) {
            showAlert("Erreur", "❌ Veuillez sélectionner le sexe.", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }
    
    private boolean enregistrerPersonnePhysique() {
        // Logique de création (si nécessaire)
        return true;
    }

    private void supprimerPersonne() {
        String matricule = matriculeModification != null ? matriculeModification : txtMatricule.getText().trim();
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirmation de suppression");
        alert.setContentText("Confirmer la suppression (TOPVIGUEUR=1) du matricule " + matricule + " ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    String url = session.getDatabaseUrl();
                    String user = session.getDatabaseUser();
                    String password = session.getDatabasePassword();
                    conn = DriverManager.getConnection(url, user, password);
                    String sql = "UPDATE PERSONNE SET PERS_TOPVIGUEUR='1' WHERE PERS_NUMMAT=?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, matricule);
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        showAlert("Succès", "Suppression logique effectuée.", Alert.AlertType.INFORMATION);
                        primaryStage.close();
                    } else {
                        showAlert("Info", "Aucune ligne mise à jour.", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException ex) {
                    showAlert("Erreur", "❌ Erreur lors de la suppression: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                    try { if (conn != null) conn.close(); } catch (Exception ignored) {}
                }
            }
        });
    }
    
    private boolean mettreAJourPersonnePhysique() {
        Connection conn = null;
        PreparedStatement stmtPersonne = null;
        PreparedStatement stmtPhysique = null;
        PreparedStatement stmtVPersonne = null;
        PreparedStatement stmtHisto = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            
            String matricule = matriculeModification != null ? matriculeModification : txtMatricule.getText().trim();
            
            // 1. Sauvegarder dans HISTO_PHYS
            int nouveauNumOrd = getProchainNumOrdHistoPhys(matricule);
            
            String sqlHisto = "INSERT INTO HISTO_PHYS (" +
                "PERS_NUMMAT, HPERS_NUMORD, HISTP_NOM, HISTP_PRENOM, HISTP_DATNAIS, " +
                "HISTP_LIEUNAIS, HISTP_SEXE, PERSP_NNI, HPERS_USER, HPERS_DATMOD, " +
                "HPERS_TELEPHONE, HPERS_EMAIL, HPERS_ADRGEOGR) " +
                "VALUES (?, ?, ?, ?, TO_DATE(?, 'DD/MM/YYYY'), ?, ?, ?, ?, SYSDATE, ?, ?, ?)";
            
            stmtHisto = conn.prepareStatement(sqlHisto);
            stmtHisto.setString(1, matricule);
            stmtHisto.setInt(2, nouveauNumOrd);
            stmtHisto.setString(3, donneesExistantes.get("PERSP_NOM"));
            stmtHisto.setString(4, donneesExistantes.get("PERSP_PRENOM"));
            stmtHisto.setString(5, formatDate(donneesExistantes.get("PERSP_DATNAIS")));
            stmtHisto.setString(6, donneesExistantes.get("PERSP_LIEUNAIS"));
            stmtHisto.setString(7, donneesExistantes.get("PERSP_SEXE"));
            stmtHisto.setString(8, donneesExistantes.get("PERSP_NNI"));
            stmtHisto.setString(9, session.getMatricule());
            stmtHisto.setString(10, donneesExistantes.get("PERS_TELEPHONE"));
            stmtHisto.setString(11, donneesExistantes.get("PERS_EMAIL"));
            stmtHisto.setString(12, donneesExistantes.get("PERS_ADRGEOGR"));
            
            int rowsHisto = stmtHisto.executeUpdate();
            System.out.println("✅ Historique PHYSIQUE sauvegardé - NumOrd: " + nouveauNumOrd);
            
            // 2. Mettre à jour la table PHYSIQUE
            String sqlPhysique = "UPDATE PHYSIQUE SET " +
                "PERSP_NOM = ?, PERSP_PRENOM = ?, PERSP_DATNAIS = TO_DATE(?, 'DD/MM/YYYY'), " +
                "PERSP_LIEUNAIS = ?, PERSP_SEXE = ?, PERSP_NNI = ? " +
                "WHERE PERS_NUMMAT = ?";
            
            stmtPhysique = conn.prepareStatement(sqlPhysique);
            stmtPhysique.setString(1, txtNom.getText().trim());
            stmtPhysique.setString(2, txtPrenom.getText().trim());
            stmtPhysique.setString(3, txtDateNaissance.getText().trim());
            stmtPhysique.setString(4, txtLieuNaissance.getText().trim());
            stmtPhysique.setString(5, getSexeSelectionne());
            stmtPhysique.setString(6, txtNNI.getText().trim());
            stmtPhysique.setString(7, matricule);
            
            int rowsPhysique = stmtPhysique.executeUpdate();
            System.out.println("✅ Table PHYSIQUE mise à jour: " + rowsPhysique + " ligne(s)");
            
            // 3. Mettre à jour la table PERSONNE
            String sqlPersonne = "UPDATE PERSONNE SET " +
                "PERS_TELEPHONE = ?, PERS_EMAIL = ?, PERS_ADRGEOGR = ? " +
                "WHERE PERS_NUMMAT = ?";
            
            stmtPersonne = conn.prepareStatement(sqlPersonne);
            stmtPersonne.setString(1, txtTelephone.getText().trim());
            stmtPersonne.setString(2, txtEmail.getText().trim());
            stmtPersonne.setString(3, txtAdresseGeographique.getText().trim());
            stmtPersonne.setString(4, matricule);
            
            int rowsPersonne = stmtPersonne.executeUpdate();
            System.out.println("✅ Table PERSONNE mise à jour: " + rowsPersonne + " ligne(s)");
            
            // 4. Mettre à jour V_PERSONNE
            String sqlVPersonne = "UPDATE V_PERSONNE SET " +
                "PERS_NOM1 = ?, PERS_NOM2 = ?, PERS_TELEPHONE = ? " +
                "WHERE PERS_NUMMAT = ?";
            
            stmtVPersonne = conn.prepareStatement(sqlVPersonne);
            stmtVPersonne.setString(1, txtNom.getText().trim());
            stmtVPersonne.setString(2, txtPrenom.getText().trim());
            stmtVPersonne.setString(3, txtTelephone.getText().trim());
            stmtVPersonne.setString(4, matricule);
            
            int rowsVPersonne = stmtVPersonne.executeUpdate();
            System.out.println("✅ Table V_PERSONNE mise à jour: " + rowsVPersonne + " ligne(s)");
            
            conn.commit();
            System.out.println("✅ Modification personne physique réussie pour: " + matricule);
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Erreur lors du rollback: " + ex.getMessage());
            }
            
            showAlert("Erreur", "❌ Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            return false;
            
        } finally {
            try { if (stmtHisto != null) stmtHisto.close(); } catch (SQLException e) {}
            try { if (stmtPhysique != null) stmtPhysique.close(); } catch (SQLException e) {}
            try { if (stmtPersonne != null) stmtPersonne.close(); } catch (SQLException e) {}
            try { if (stmtVPersonne != null) stmtVPersonne.close(); } catch (SQLException e) {}
            try { 
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close(); 
                }
            } catch (SQLException e) {}
        }
    }
    
    private int getProchainNumOrdHistoPhys(String matricule) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT NVL(MAX(HPERS_NUMORD), 0) + 1 as NOUVEAU_NUMORD " +
                        "FROM HISTO_PHYS WHERE PERS_NUMMAT = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("NOUVEAU_NUMORD");
            }
            
            return 1;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération numéro ordre historique: " + e.getMessage());
            return 1;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private String getSexeSelectionne() {
        if (radioMasculin.isSelected()) {
            return "M";
        } else if (radioFeminin.isSelected()) {
            return "F";
        }
        return "";
    }
    
    private void annuler() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirmation d'annulation");
        alert.setContentText("Voulez-vous vraiment annuler ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                primaryStage.close();
            }
        });
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public boolean isModificationReussie() {
        return modificationReussie;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}