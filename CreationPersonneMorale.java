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

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;

public class CreationPersonneMorale extends Application {
    
    private JFXTextField txtMatricule, txtRaisonSociale, txtSigle, txtDateCreation;
    private JFXTextField txtNumeroRCCM, txtNumeroIdentification, txtActivite;
    private JFXTextField txtTelephone, txtWhatsapp, txtFax, txtEmail, txtAdressePostale, txtAdresseGeographique;
    private JFXTextField txtNomRepresentant, txtPrenomRepresentant, txtFonctionRepresentant;
    private JFXComboBox<String> comboFormeJuridique, comboStatut, comboRegimeFiscal;
    private JFXComboBox<String> comboLieuGestion, comboNationalite, comboSecteurActivite;
    
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
            primaryStage.setTitle(isModification ? "✏️ Modifier Personne Morale" : "➕ Créer Personne Morale");
            if (isModification) {
                peuplerChampsAvecDonneesExistantes();
            }
        }
    }
    
    private void initInterface() {
        String titre = isModification ? "✏️ Modifier Personne Morale" : "➕ Créer Personne Morale";
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
        VBox panelInfos = creerPanelInformations();
        VBox panelRepresentant = creerPanelRepresentant();
        VBox panelCoordonnees = creerPanelCoordonnees();
        HBox panelBoutons = creerPanelBoutons();
        
        // ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        VBox contentPanel = new VBox(15);
        contentPanel.setPadding(new Insets(10));
        contentPanel.getChildren().addAll(panelInfos, panelRepresentant, panelCoordonnees);
        scrollPane.setContent(contentPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        
        mainPanel.getChildren().addAll(lblTitre, scrollPane, panelBoutons);
        
        Scene scene = new Scene(mainPanel, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        
        configurerEcouteurs();
    }
    
    private VBox creerPanelInformations() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ff8c00; -fx-border-width: 2;");
        
        Label titleLabel = new Label("📋 Informations de l'Entreprise");
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
        
        // Raison sociale
        grid.add(creerLabel("🏢 Raison sociale:"), 0, 1);
        txtRaisonSociale = creerTextField();
        grid.add(txtRaisonSociale, 1, 1);
        
        // Sigle
        grid.add(creerLabel("🔤 Sigle:"), 0, 2);
        txtSigle = creerTextField();
        grid.add(txtSigle, 1, 2);
        
        // Date création
        grid.add(creerLabel("📅 Date création:"), 0, 3);
        txtDateCreation = creerTextField();
        grid.add(txtDateCreation, 1, 3);
        
        // Forme juridique
        grid.add(creerLabel("⚖️ Forme juridique:"), 0, 4);
        comboFormeJuridique = new JFXComboBox<>();
        comboFormeJuridique.getItems().addAll("SARL", "SA", "SNC", "SCS", "GIE");
        comboFormeJuridique.setPrefWidth(300);
        comboFormeJuridique.setStyle("-fx-font-size: 14px;");
        grid.add(comboFormeJuridique, 1, 4);
        
        panel.getChildren().addAll(titleLabel, grid);
        return panel;
    }
    
    private VBox creerPanelRepresentant() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ff8c00; -fx-border-width: 2;");
        
        Label titleLabel = new Label("👤 Représentant Légal");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#ff8c00"));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        
        // Nom représentant
        grid.add(creerLabel("👤 Nom représentant:"), 0, 0);
        txtNomRepresentant = creerTextField();
        grid.add(txtNomRepresentant, 1, 0);
        
        // Prénom représentant
        grid.add(creerLabel("👤 Prénom représentant:"), 0, 1);
        txtPrenomRepresentant = creerTextField();
        grid.add(txtPrenomRepresentant, 1, 1);
        
        // Fonction
        grid.add(creerLabel("💼 Fonction:"), 0, 2);
        txtFonctionRepresentant = creerTextField();
        grid.add(txtFonctionRepresentant, 1, 2);
        
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
    }
    
    private void peuplerChampsAvecDonneesExistantes() {
        if (donneesExistantes != null) {
            txtMatricule.setText(donneesExistantes.get("PERS_NUMMAT"));
            txtRaisonSociale.setText(donneesExistantes.get("PERSM_RAISON"));
            txtSigle.setText(donneesExistantes.get("PERSM_NOMSOC"));
            txtDateCreation.setText(formatDate(donneesExistantes.get("PERSM_DATCREAT")));
            txtTelephone.setText(donneesExistantes.get("PERS_TELEPHONE"));
            txtEmail.setText(donneesExistantes.get("PERS_EMAIL"));
            txtAdresseGeographique.setText(donneesExistantes.get("PERS_ADRGEOGR"));
            txtNomRepresentant.setText(donneesExistantes.get("PERSM_NOMGERANT"));
            txtFonctionRepresentant.setText(donneesExistantes.get("PERSM_TITGERANT"));
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
    
    private void sauvegarder() {
        if (!validerFormulaire()) {
            return;
        }
        
        if (isModification) {
            if (mettreAJourPersonneMorale()) {
                modificationReussie = true;
                showAlert("Modification réussie", "✅ Personne morale modifiée avec succès!", Alert.AlertType.INFORMATION);
                primaryStage.close();
            }
        } else {
            if (enregistrerPersonneMorale()) {
                showAlert("Création réussie", "✅ Personne morale créée avec succès!", Alert.AlertType.INFORMATION);
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
        
        if (txtRaisonSociale.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez saisir la raison sociale.", Alert.AlertType.ERROR);
            txtRaisonSociale.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean enregistrerPersonneMorale() {
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
    
    private boolean mettreAJourPersonneMorale() {
        Connection conn = null;
        PreparedStatement stmtPersonne = null;
        PreparedStatement stmtMorale = null;
        PreparedStatement stmtVPersonne = null;
        PreparedStatement stmtHisto = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            
            String matricule = matriculeModification != null ? matriculeModification : txtMatricule.getText().trim();
            
            // 1. Sauvegarder dans HISTO_MOR
            int nouveauNumOrd = getProchainNumOrdHistoMor(matricule);
            
            String sqlHisto = "INSERT INTO HISTO_MOR (" +
                "PERS_NUMMAT, HPERS_NUMORD, HISTM_RAISON, HISTM_NOMGERANT, HISTM_TITGERANT, " +
                "HPERS_USER, HPERS_DATMOD, HPERS_TELEPHONE, HPERS_EMAIL, HPERS_ADRGEOGR) " +
                "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?)";
            
            stmtHisto = conn.prepareStatement(sqlHisto);
            stmtHisto.setString(1, matricule);
            stmtHisto.setInt(2, nouveauNumOrd);
            stmtHisto.setString(3, donneesExistantes.get("PERSM_RAISON"));
            stmtHisto.setString(4, donneesExistantes.get("PERSM_NOMGERANT"));
            stmtHisto.setString(5, donneesExistantes.get("PERSM_TITGERANT"));
            stmtHisto.setString(6, session.getMatricule());
            stmtHisto.setString(7, donneesExistantes.get("PERS_TELEPHONE"));
            stmtHisto.setString(8, donneesExistantes.get("PERS_EMAIL"));
            stmtHisto.setString(9, donneesExistantes.get("PERS_ADRGEOGR"));
            
            int rowsHisto = stmtHisto.executeUpdate();
            System.out.println("✅ Historique MORALE sauvegardé - NumOrd: " + nouveauNumOrd);
            
            // 2. Mettre à jour la table MORALE
            String sqlMorale = "UPDATE MORALE SET " +
                "PERSM_RAISON = ?, PERSM_NOMSOC = ?, PERSM_NOMGERANT = ?, PERSM_TITGERANT = ? " +
                "WHERE PERS_NUMMAT = ?";
            
            stmtMorale = conn.prepareStatement(sqlMorale);
            stmtMorale.setString(1, txtRaisonSociale.getText().trim());
            stmtMorale.setString(2, txtSigle.getText().trim());
            stmtMorale.setString(3, txtNomRepresentant.getText().trim());
            stmtMorale.setString(4, txtFonctionRepresentant.getText().trim());
            stmtMorale.setString(5, matricule);
            
            int rowsMorale = stmtMorale.executeUpdate();
            System.out.println("✅ Table MORALE mise à jour: " + rowsMorale + " ligne(s)");
            
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
            stmtVPersonne.setString(1, txtRaisonSociale.getText().trim());
            stmtVPersonne.setString(2, txtSigle.getText().trim());
            stmtVPersonne.setString(3, txtTelephone.getText().trim());
            stmtVPersonne.setString(4, matricule);
            
            int rowsVPersonne = stmtVPersonne.executeUpdate();
            System.out.println("✅ Table V_PERSONNE mise à jour: " + rowsVPersonne + " ligne(s)");
            
            conn.commit();
            System.out.println("✅ Modification personne morale réussie pour: " + matricule);
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
            try { if (stmtMorale != null) stmtMorale.close(); } catch (SQLException e) {}
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
    
    private int getProchainNumOrdHistoMor(String matricule) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String url = session.getDatabaseUrl();
            String user = session.getDatabaseUser();
            String password = session.getDatabasePassword();
            
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "SELECT NVL(MAX(HPERS_NUMORD), 0) + 1 as NOUVEAU_NUMORD " +
                        "FROM HISTO_MOR WHERE PERS_NUMMAT = ?";
            
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