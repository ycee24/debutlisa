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
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;

public class RecherchePersonne extends Application {
    
    // Couleurs personnalisées
    private final Color ORANGE = Color.web("#ff8c00");
    private final Color FOND_CHAMP = Color.web("#f0f0f2");
    private final Color TEXTE_CHAMP = Color.web("#1c39bb");
    private final Color FOND_PRINCIPAL = Color.web("#f8f9fa");
    private final Color VERT = Color.web("#28a745");
    private final Color ROUGE = Color.web("#dc3545");
    
    private String typeSelectionne;
    private JFXTextField txtNom, txtPrenom, txtDateNaissance, txtParent, txtPiece, txtNNI, txtMatricule;
    private JFXTextField txtNomSocietaire, txtSigle, txtRegistreCommerce, txtCompteContribuable, txtMatriculeMoral;
    private TableView<Map<String, String>> tableResultats;
    private JFXButton btnRechercher, btnAnnuler, btnSelectionner;
    
    private SessionUtilisateur sessionUtilisateur;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionUtilisateur = SessionUtilisateur.getInstance();
        
        // Afficher d'abord la sélection du type
        afficherSelectionType();
    }

    private void afficherSelectionType() {
        Stage selectionStage = new Stage();
        selectionStage.initModality(Modality.APPLICATION_MODAL);
        selectionStage.setTitle("Sélection du Type de Personne");
        
        VBox mainPanel = new VBox(20);
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setPadding(new Insets(40));
        mainPanel.setStyle("-fx-background-color: #f8f9fa;");
        
        // Titre
        Label titleLabel = new Label("🔍 SÉLECTION DU TYPE DE PERSONNE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(ORANGE);
        
        // Instruction
        Label instructionLabel = new Label("Veuillez choisir le type de personne à rechercher:");
        instructionLabel.setFont(Font.font("Segoe UI", 20));
        instructionLabel.setTextFill(ORANGE);
        
        // Boutons de sélection
        HBox buttonPanel = new HBox(50);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(30, 100, 30, 100));
        
        JFXButton btnPhysique = new JFXButton("👤 PERSONNE PHYSIQUE");
        JFXButton btnMoral = new JFXButton("🏢 PERSONNE MORALE");
        
        styliserBoutonSelection(btnPhysique, VERT);
        styliserBoutonSelection(btnMoral, Color.web("#007bff"));
        
        btnPhysique.setPrefSize(350, 120);
        btnMoral.setPrefSize(350, 120);
        
        btnPhysique.setOnAction(e -> {
            typeSelectionne = "PHYSIQUE";
            selectionStage.close();
            initInterface();
        });
        
        btnMoral.setOnAction(e -> {
            typeSelectionne = "MORAL";
            selectionStage.close();
            initInterface();
        });
        
        buttonPanel.getChildren().addAll(btnPhysique, btnMoral);
        
        mainPanel.getChildren().addAll(titleLabel, instructionLabel, buttonPanel);
        
        Scene scene = new Scene(mainPanel, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        selectionStage.setScene(scene);
        selectionStage.showAndWait();
    }
    
    private void styliserBoutonSelection(JFXButton bouton, Color couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; " +
            "-fx-font-size: 20px; -fx-border-color: %s; -fx-border-width: 4;",
            couleur.toString().replace("0x", "#")
        ));
        bouton.setButtonType(JFXButton.ButtonType.RAISED);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 20px; -fx-border-color: %s; -fx-border-width: 4;",
            couleur.toString().replace("0x", "#"), couleur.toString().replace("0x", "#")
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; " +
            "-fx-font-size: 20px; -fx-border-color: %s; -fx-border-width: 4;",
            couleur.toString().replace("0x", "#")
        )));
    }
    
    private void initInterface() {
        primaryStage.setTitle("🔍 Recherche de Personne - " + typeSelectionne);
        
        VBox mainPanel = new VBox(20);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");
        
        // Titre
        Label titleLabel = new Label("🔍 RECHERCHE DE PERSONNE - " + typeSelectionne);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(ORANGE);
        titleLabel.setAlignment(Pos.CENTER);
        
        // Panel de recherche selon le type
        VBox searchPanel;
        if ("PHYSIQUE".equals(typeSelectionne)) {
            searchPanel = creerPanelPhysique();
        } else {
            searchPanel = creerPanelMoral();
        }
        
        // Panel des résultats
        VBox resultsPanel = creerPanelResultats();
        
        // Panel des boutons
        HBox buttonPanel = creerPanelBoutons();
        
        mainPanel.getChildren().addAll(titleLabel, searchPanel, resultsPanel, buttonPanel);
        
        Scene scene = new Scene(mainPanel, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox creerPanelPhysique() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #28a745; -fx-border-width: 2;");
        
        Label titleLabel = new Label("CRITÈRES DE RECHERCHE - PERSONNE PHYSIQUE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(VERT);
        
        GridPane criteriaPanel = new GridPane();
        criteriaPanel.setHgap(10);
        criteriaPanel.setVgap(8);
        criteriaPanel.setPadding(new Insets(12));
        
        Font labelFont = Font.font("Segoe UI", FontWeight.BOLD, 16);
        
        // Création des champs
        txtNom = creerChampPersonnalise();
        txtPrenom = creerChampPersonnalise();
        txtDateNaissance = creerChampPersonnalise();
        txtParent = creerChampPersonnalise();
        txtPiece = creerChampPersonnalise();
        txtNNI = creerChampPersonnalise();
        txtMatricule = creerChampPersonnalise();
        
        // Labels et champs
        int row = 0;
        criteriaPanel.add(creerLabel("Nom:", labelFont), 0, row);
        criteriaPanel.add(txtNom, 1, row++);
        
        criteriaPanel.add(creerLabel("Prénom:", labelFont), 0, row);
        criteriaPanel.add(txtPrenom, 1, row++);
        
        criteriaPanel.add(creerLabel("Date de naissance (JJ/MM/AAAA):", labelFont), 0, row);
        criteriaPanel.add(txtDateNaissance, 1, row++);
        
        criteriaPanel.add(creerLabel("Nom de la mère:", labelFont), 0, row);
        criteriaPanel.add(txtParent, 1, row++);
        
        criteriaPanel.add(creerLabel("Date délivrance pièce (JJ/MM/AAAA):", labelFont), 0, row);
        criteriaPanel.add(txtPiece, 1, row++);
        
        criteriaPanel.add(creerLabel("N° NNI:", labelFont), 0, row);
        criteriaPanel.add(txtNNI, 1, row++);
        
        criteriaPanel.add(creerLabel("Matricule:", labelFont), 0, row);
        criteriaPanel.add(txtMatricule, 1, row);
        
        panel.getChildren().addAll(titleLabel, criteriaPanel);
        return panel;
    }
    
    private VBox creerPanelMoral() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ff8c00; -fx-border-width: 2;");
        
        Label titleLabel = new Label("CRITÈRES DE RECHERCHE - PERSONNE MORALE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(ORANGE);
        
        GridPane criteriaPanel = new GridPane();
        criteriaPanel.setHgap(10);
        criteriaPanel.setVgap(8);
        criteriaPanel.setPadding(new Insets(12));
        
        Font labelFont = Font.font("Segoe UI", FontWeight.BOLD, 16);
        
        // Création des champs
        txtNomSocietaire = creerChampPersonnalise();
        txtSigle = creerChampPersonnalise();
        txtRegistreCommerce = creerChampPersonnalise();
        txtCompteContribuable = creerChampPersonnalise();
        txtMatriculeMoral = creerChampPersonnalise();
        
        // Labels et champs
        int row = 0;
        criteriaPanel.add(creerLabel("Nom sociétaire:", labelFont), 0, row);
        criteriaPanel.add(txtNomSocietaire, 1, row++);
        
        criteriaPanel.add(creerLabel("Raison sociale:", labelFont), 0, row);
        criteriaPanel.add(txtSigle, 1, row++);
        
        criteriaPanel.add(creerLabel("N° registre de commerce:", labelFont), 0, row);
        criteriaPanel.add(txtRegistreCommerce, 1, row++);
        
        criteriaPanel.add(creerLabel("N° compte contribuable:", labelFont), 0, row);
        criteriaPanel.add(txtCompteContribuable, 1, row++);
        
        criteriaPanel.add(creerLabel("Matricule:", labelFont), 0, row);
        criteriaPanel.add(txtMatriculeMoral, 1, row);
        
        panel.getChildren().addAll(titleLabel, criteriaPanel);
        return panel;
    }
    
    private Label creerLabel(String texte, Font font) {
        Label label = new Label(texte);
        label.setFont(font);
        label.setTextFill(ORANGE);
        return label;
    }
    
    private JFXTextField creerChampPersonnalise() {
        JFXTextField champ = new JFXTextField();
        champ.setStyle("-fx-background-color: #f0f0f2; -fx-text-fill: #1c39bb; -fx-font-weight: bold; -fx-font-size: 13px;");
        champ.setPrefSize(200, 28);
        return champ;
    }
    
    private VBox creerPanelResultats() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #28a745; -fx-border-width: 2;");
        panel.setPrefHeight(300);
        
        Label titleLabel = new Label("RÉSULTATS DE LA RECHERCHE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(VERT);
        
        // Création du TableView
        tableResultats = new TableView<>();
        
        // Colonnes selon le type
        if ("PHYSIQUE".equals(typeSelectionne)) {
            TableColumn<Map<String, String>, String> colMatricule = new TableColumn<>("Matricule");
            colMatricule.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("matricule")));
            
            TableColumn<Map<String, String>, String> colNom = new TableColumn<>("Nom");
            colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("nom")));
            
            TableColumn<Map<String, String>, String> colPrenom = new TableColumn<>("Prénom");
            colPrenom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("prenom")));
            
            TableColumn<Map<String, String>, String> colDateNaissance = new TableColumn<>("Date Naissance");
            colDateNaissance.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("date_naissance")));
            
            TableColumn<Map<String, String>, String> colNomMere = new TableColumn<>("Nom Mère");
            colNomMere.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("nom_mere")));
            
            TableColumn<Map<String, String>, String> colNumPiece = new TableColumn<>("Numéro Pièce");
            colNumPiece.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("numéro_piece")));
            
            TableColumn<Map<String, String>, String> colNNI = new TableColumn<>("NNI");
            colNNI.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("nni")));
            
            TableColumn<Map<String, String>, String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("type_personne")));
            
            tableResultats.getColumns().addAll(colMatricule, colNom, colPrenom, colDateNaissance, colNomMere, colNumPiece, colNNI, colType);
        } else {
            TableColumn<Map<String, String>, String> colMatricule = new TableColumn<>("Matricule");
            colMatricule.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("matricule")));
            
            TableColumn<Map<String, String>, String> colNomSocietaire = new TableColumn<>("Nom Sociétaire");
            colNomSocietaire.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("nom_societaire")));
            
            TableColumn<Map<String, String>, String> colRaisonSociale = new TableColumn<>("Raison Sociale");
            colRaisonSociale.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("raison_sociale")));
            
            TableColumn<Map<String, String>, String> colRegistre = new TableColumn<>("N° Registre");
            colRegistre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("registre")));
            
            TableColumn<Map<String, String>, String> colCompteContribuable = new TableColumn<>("N° Compte Contribuable");
            colCompteContribuable.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("compte_contribuable")));
            
            TableColumn<Map<String, String>, String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("type_personne")));
            
            tableResultats.getColumns().addAll(colMatricule, colNomSocietaire, colRaisonSociale, colRegistre, colCompteContribuable, colType);
        }
        
        // Style du tableau
        tableResultats.setStyle("-fx-font-size: 12px;");
        tableResultats.setRowFactory(tv -> {
            TableRow<Map<String, String>> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #ff8c001a;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });
        
        ScrollPane scrollPane = new ScrollPane(tableResultats);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        
        panel.getChildren().addAll(titleLabel, scrollPane);
        return panel;
    }
    
    private HBox creerPanelBoutons() {
        HBox panel = new HBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 10, 0));
        
        btnRechercher = new JFXButton("🔍 RECHERCHER");
        btnSelectionner = new JFXButton("✅ SÉLECTIONNER");
        btnAnnuler = new JFXButton("❌ ANNULER");
        
        Font buttonFont = Font.font("Segoe UI", FontWeight.BOLD, 14);
        btnRechercher.setFont(buttonFont);
        btnSelectionner.setFont(buttonFont);
        btnAnnuler.setFont(buttonFont);
        
        styliserBouton(btnRechercher, ORANGE);
        styliserBouton(btnSelectionner, VERT);
        styliserBouton(btnAnnuler, ROUGE);
        
        btnRechercher.setPrefSize(180, 40);
        btnSelectionner.setPrefSize(180, 40);
        btnAnnuler.setPrefSize(180, 40);
        
        btnRechercher.setOnAction(e -> executerRecherche());
        btnSelectionner.setOnAction(e -> selectionnerPersonne());
        btnAnnuler.setOnAction(e -> primaryStage.close());
        
        panel.getChildren().addAll(btnRechercher, btnSelectionner, btnAnnuler);
        return panel;
    }
    
    private void styliserBouton(JFXButton bouton, Color couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: %s; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 2;",
            couleur.toString().replace("0x", "#"), couleur.toString().replace("0x", "#")
        ));
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 2;",
            couleur.toString().replace("0x", "#"), couleur.toString().replace("0x", "#")
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: %s; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 2;",
            couleur.toString().replace("0x", "#"), couleur.toString().replace("0x", "#")
        )));
    }
    
    private void executerRecherche() {
        if ("PHYSIQUE".equals(typeSelectionne)) {
            rechercherPersonnePhysique();
        } else {
            rechercherPersonneMorale();
        }
    }
    
    private void rechercherPersonnePhysique() {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String dateNaissance = txtDateNaissance.getText().trim();
        String parent = txtParent.getText().trim();
        String piece = txtPiece.getText().trim();
        String nni = txtNNI.getText().trim();
        String matricule = txtMatricule.getText().trim();

        String url = sessionUtilisateur.getDatabaseUrl();
        String user = sessionUtilisateur.getDatabaseUser();
        String password = sessionUtilisateur.getDatabasePassword();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ph.PERS_NUMMAT as matricule, ph.PERSP_NOM as nom, ph.PERSP_PRENOM as prenom, ");
        sql.append("ph.PERSP_DATNAIS as date_naissance, ph.PERSP_NOMMERE as nom_mere, ");
        sql.append("ph.PERSP_NUMPIECE as numéro_piece, ph.PERSP_NNI as nni, vp.PERS_TYPE as type_personne ");
        sql.append("FROM physique ph, v_personne vp ");
        sql.append("WHERE ph.PERS_NUMMAT = vp.PERS_NUMMAT ");
        
        if (!nom.isEmpty()) sql.append("AND LOWER(ph.PERSP_NOM) LIKE LOWER(?) ");
        if (!prenom.isEmpty()) sql.append("AND LOWER(ph.PERSP_PRENOM) LIKE LOWER(?) ");
        if (!dateNaissance.isEmpty()) sql.append("AND ph.PERSP_DATNAIS = TO_DATE(?, 'DD/MM/YYYY') ");
        if (!parent.isEmpty()) sql.append("AND LOWER(ph.PERSP_NOMMERE) LIKE LOWER(?) ");
        if (!piece.isEmpty()) sql.append("AND LOWER(ph.PERSP_NUMPIECE) LIKE LOWER(?) ");
        if (!nni.isEmpty()) sql.append("AND ph.PERSP_NNI LIKE ? ");
        if (!matricule.isEmpty()) sql.append("AND ph.PERS_NUMMAT LIKE ? ");
        
        sql.append("ORDER BY ph.PERSP_NOM, ph.PERSP_PRENOM");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (!nom.isEmpty()) stmt.setString(paramIndex++, "%" + nom + "%");
            if (!prenom.isEmpty()) stmt.setString(paramIndex++, "%" + prenom + "%");
            if (!dateNaissance.isEmpty()) stmt.setString(paramIndex++, dateNaissance);
            if (!parent.isEmpty()) stmt.setString(paramIndex++, "%" + parent + "%");
            if (!piece.isEmpty()) stmt.setString(paramIndex++, "%" + piece + "%");
            if (!nni.isEmpty()) stmt.setString(paramIndex++, "%" + nni + "%");
            if (!matricule.isEmpty()) stmt.setString(paramIndex++, "%" + matricule + "%");

            ResultSet rs = stmt.executeQuery();
            ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

            int count = 0;
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("matricule", rs.getString("matricule"));
                row.put("nom", rs.getString("nom"));
                row.put("prenom", rs.getString("prenom"));
                row.put("date_naissance", rs.getString("date_naissance"));
                row.put("nom_mere", rs.getString("nom_mere"));
                row.put("numéro_piece", rs.getString("numéro_piece"));
                row.put("nni", rs.getString("nni"));
                row.put("type_personne", rs.getString("type_personne"));
                data.add(row);
                count++;
            }
            
            tableResultats.setItems(data);
            afficherResultatsRecherche(count);
            
        } catch (Exception e) {
            afficherErreur("Erreur lors de la recherche des personnes physiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void rechercherPersonneMorale() {
        String nomSocietaire = txtNomSocietaire.getText().trim();
        String raisonSociale = txtSigle.getText().trim();
        String registreCommerce = txtRegistreCommerce.getText().trim();
        String compteContribuable = txtCompteContribuable.getText().trim();
        String matricule = txtMatriculeMoral.getText().trim();

        String url = sessionUtilisateur.getDatabaseUrl();
        String user = sessionUtilisateur.getDatabaseUser();
        String password = sessionUtilisateur.getDatabasePassword();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT m.PERS_NUMMAT as matricule, m.PERSM_NOMSOC as nom_societaire, ");
        sql.append("m.PERSM_RAISON as raison_sociale, m.PERS_REGISTRE as registre, ");
        sql.append("m.PERS_CPTECONTRIB as compte_contribuable, vp.PERS_TYPE as type_personne ");
        sql.append("FROM morale m, v_personne vp ");
        sql.append("WHERE m.PERS_NUMMAT = vp.PERS_NUMMAT ");
        
        if (!nomSocietaire.isEmpty()) sql.append("AND LOWER(m.PERSM_NOMSOC) LIKE LOWER(?) ");
        if (!raisonSociale.isEmpty()) sql.append("AND LOWER(m.PERSM_RAISON) LIKE LOWER(?) ");
        if (!registreCommerce.isEmpty()) sql.append("AND m.PERS_REGISTRE LIKE ? ");
        if (!compteContribuable.isEmpty()) sql.append("AND m.PERS_CPTECONTRIB LIKE ? ");
        if (!matricule.isEmpty()) sql.append("AND m.PERS_NUMMAT LIKE ? ");
        
        sql.append("ORDER BY m.PERSM_NOMSOC");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (!nomSocietaire.isEmpty()) stmt.setString(paramIndex++, "%" + nomSocietaire + "%");
            if (!raisonSociale.isEmpty()) stmt.setString(paramIndex++, "%" + raisonSociale + "%");
            if (!registreCommerce.isEmpty()) stmt.setString(paramIndex++, "%" + registreCommerce + "%");
            if (!compteContribuable.isEmpty()) stmt.setString(paramIndex++, "%" + compteContribuable + "%");
            if (!matricule.isEmpty()) stmt.setString(paramIndex++, "%" + matricule + "%");

            ResultSet rs = stmt.executeQuery();
            ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

            int count = 0;
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("matricule", rs.getString("matricule"));
                row.put("nom_societaire", rs.getString("nom_societaire"));
                row.put("raison_sociale", rs.getString("raison_sociale"));
                row.put("registre", rs.getString("registre"));
                row.put("compte_contribuable", rs.getString("compte_contribuable"));
                row.put("type_personne", rs.getString("type_personne"));
                data.add(row);
                count++;
            }
            
            tableResultats.setItems(data);
            afficherResultatsRecherche(count);
            
        } catch (Exception e) {
            afficherErreur("Erreur lors de la recherche des personnes morales: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void afficherResultatsRecherche(int count) {
        if (count > 0) {
            showAlert("Résultats", count + " personne(s) trouvée(s)", Alert.AlertType.INFORMATION);
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Création de personne");
            alert.setHeaderText("Aucune personne trouvée");
            alert.setContentText("Voulez-vous la créer ?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    ouvrirEcranCreation();
                }
            });
        }
    }
    
    private void afficherErreur(String message) {
        showAlert("Erreur", message, Alert.AlertType.ERROR);
    }
    
    private void selectionnerPersonne() {
        Map<String, String> selectedItem = tableResultats.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une personne dans la liste", Alert.AlertType.WARNING);
            return;
        }
        
        String message = "Personne sélectionnée :\n";
        if ("PHYSIQUE".equals(typeSelectionne)) {
            message += "Matricule: " + selectedItem.get("matricule") + "\n" +
                      "Nom: " + selectedItem.get("nom") + "\n" +
                      "Prénom: " + selectedItem.get("prenom") + "\n" +
                      "Type: " + selectedItem.get("type_personne");
        } else {
            message += "Matricule: " + selectedItem.get("matricule") + "\n" +
                      "Nom Sociétaire: " + selectedItem.get("nom_societaire") + "\n" +
                      "Raison Sociale: " + selectedItem.get("raison_sociale") + "\n" +
                      "Type: " + selectedItem.get("type_personne");
        }
        
        showAlert("Sélection réussie", message, Alert.AlertType.INFORMATION);
        primaryStage.close();
    }
    
    private void ouvrirEcranCreation() {
        // Ces classes devront également être converties en JavaFX
        if ("PHYSIQUE".equals(typeSelectionne)) {
            Stage creationStage = new Stage();
            CreationPersonnePhysique creation = new CreationPersonnePhysique();
            creation.start(creationStage);
        } else {
            Stage creationStage = new Stage();
            CreationPersonneMorale creation = new CreationPersonneMorale();
            creation.start(creationStage);
        }
        primaryStage.close();
    }
    
    private Map<String, String> getDonneesRecherche() {
        Map<String, String> donnees = new HashMap<>();
        
        if ("PHYSIQUE".equals(typeSelectionne)) {
            donnees.put("nom", txtNom.getText().trim());
            donnees.put("prenom", txtPrenom.getText().trim());
            donnees.put("dateNaissance", txtDateNaissance.getText().trim());
            donnees.put("nomMere", txtParent.getText().trim());
        } else {
            donnees.put("nomSocietaire", txtNomSocietaire.getText().trim());
            donnees.put("raisonSociale", txtSigle.getText().trim());
            donnees.put("registreCommerce", txtRegistreCommerce.getText().trim());
            donnees.put("compteContribuable", txtCompteContribuable.getText().trim());
            donnees.put("matricule", txtMatriculeMoral.getText().trim());
        }
        
        return donnees;
    }
    
    public String[] getPersonneSelectionnee() {
        Map<String, String> selectedItem = tableResultats.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if ("PHYSIQUE".equals(typeSelectionne)) {
                return new String[] {
                    selectedItem.get("matricule"),
                    selectedItem.get("nom"),
                    selectedItem.get("prenom"),
                    typeSelectionne
                };
            } else {
                return new String[] {
                    selectedItem.get("matricule"),
                    selectedItem.get("nom_societaire"),
                    selectedItem.get("raison_sociale"),
                    typeSelectionne
                };
            }
        }
        return null;
    }
    
    public String getTypeSelectionne() {
        return typeSelectionne;
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}