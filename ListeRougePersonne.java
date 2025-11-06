package lisa1connexion;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ListeRougePersonne extends Stage {
    private JFXTextField matriculeField;
    private JFXButton rechercherButton;
    private JFXButton annulerButton;
    private boolean confirmed = false;
    private String matricule;
    private SessionUtilisateur session;

    public ListeRougePersonne(Stage parent) {
        initOwner(parent);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);
        
        this.session = SessionUtilisateur.getInstance();
        initializeComponents();
        setupLayout();
        setupListeners();
        
        setTitle("Mettre en Liste Rouge");
        sizeToScene();
        centerOnScreen();
        setResizable(false);
    }

    private void initializeComponents() {
        matriculeField = new JFXTextField();
        matriculeField.setPromptText("Saisir le matricule");
        matriculeField.setMaxWidth(300);
        matriculeField.setStyle("-jfx-focus-color: #b22222; -jfx-unfocus-color: #b4b4b4;");
        
        rechercherButton = new JFXButton("Rechercher");
        annulerButton = new JFXButton("Annuler");
        
        rechercherButton.setPrefSize(130, 40);
        annulerButton.setPrefSize(130, 40);
        
        styleButton(rechercherButton, "#b22222");
        styleButton(annulerButton, "#787878");
    }

    private void setupLayout() {
        // Titre
        Label lblTitre = new Label("Mettre en Liste Rouge");
        lblTitre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #b22222;");
        
        // Instructions
        Label lblInstruction = new Label("Veuillez saisir le matricule de la personne à mettre en liste rouge :");
        lblInstruction.setStyle("-fx-font-size: 16px;");
        
        // Indication
        Label lblIndication = new Label("Appuyez sur Entrée ou cliquez sur Rechercher");
        lblIndication.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: gray;");
        
        // Boutons
        HBox buttonBox = new HBox(15, rechercherButton, annulerButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Layout principal
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: white;");
        
        mainLayout.getChildren().addAll(
            lblTitre, lblInstruction, matriculeField, 
            lblIndication, buttonBox
        );
        
        Scene scene = new Scene(mainLayout, 500, 250);
        setScene(scene);
        
        // Focus sur le champ matricule
        Platform.runLater(() -> {
            matriculeField.requestFocus();
            matriculeField.selectAll();
        });
    }

    private void styleButton(JFXButton button, String color) {
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;",
            color
        ));
        button.setRipplerFill(javafx.scene.paint.Color.WHITE);
        
        button.setOnMouseEntered(e -> button.setStyle(
            String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;",
                darkenColor(color))
        ));
        button.setOnMouseExited(e -> button.setStyle(
            String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;",
                color)
        ));
    }
    
    private String darkenColor(String hexColor) {
        return hexColor.replaceAll("([0-9a-fA-F]{2})", "80");
    }

    private void setupListeners() {
        rechercherButton.setOnAction(e -> rechercherPersonne());
        annulerButton.setOnAction(e -> close());
        matriculeField.setOnAction(e -> rechercherPersonne());
    }

    private void rechercherPersonne() {
        matricule = matriculeField.getText().trim();
        
        if (matricule.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ obligatoire", 
                     "Veuillez saisir un matricule.");
            matriculeField.requestFocus();
            return;
        }
        
        if (!matricule.matches("\\d{8}")) {
            showAlert(Alert.AlertType.WARNING, "Format invalide",
                     "Le matricule doit contenir exactement 8 chiffres.");
            matriculeField.selectAll();
            matriculeField.requestFocus();
            return;
        }
        
        try {
            Map<String, String> donneesPersonne = rechercherPersonneParMatricule(matricule);
            
            if (donneesPersonne != null && !donneesPersonne.isEmpty()) {
                String topVigueur = donneesPersonne.get("PERS_TOPVIGUEUR");
                if ("2".equals(topVigueur)) {
                    String nomPersonne = donneesPersonne.get("NOM_PERSONNE");
                    showAlert(Alert.AlertType.WARNING, "Personne déjà en liste rouge",
                             "⚠️ Cette personne est déjà en liste rouge !\n\n" +
                             "Matricule: " + matricule + "\n" +
                             "Nom: " + nomPersonne + "\n\n" +
                             "Impossible de mettre en liste rouge une personne déjà en liste rouge.");
                    matriculeField.selectAll();
                    matriculeField.requestFocus();
                    return;
                }
                
                if ("1".equals(topVigueur)) {
                    String nomPersonne = donneesPersonne.get("NOM_PERSONNE");
                    showAlert(Alert.AlertType.WARNING, "Personne supprimée",
                             "❌ Cette personne est supprimée !\n\n" +
                             "Matricule: " + matricule + "\n" +
                             "Nom: " + nomPersonne + "\n\n" +
                             "Impossible de mettre en liste rouge une personne supprimée.");
                    matriculeField.selectAll();
                    matriculeField.requestFocus();
                    return;
                }
                
                String typePersonne = donneesPersonne.get("TYPERS_CODE");
                String nomPersonne = donneesPersonne.get("NOM_PERSONNE");
                
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation de mise en liste rouge");
                confirmation.setHeaderText("Voulez-vous vraiment mettre cette personne en liste rouge ?");
                confirmation.setContentText(
                    "Matricule: " + matricule + "\n" +
                    "Nom: " + nomPersonne + "\n" +
                    "Type: " + ("P".equals(typePersonne) ? "Personne Physique" : "Personne Morale") + "\n\n" +
                    "🔴 La personne sera marquée comme étant en liste rouge."
                );
                
                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    ouvrirFenetreDetaillee(matricule, typePersonne);
                }
                
            } else {
                showAlert(Alert.AlertType.ERROR, "Recherche infructueuse",
                         "Aucune personne trouvée avec le matricule : " + matricule);
                matriculeField.selectAll();
                matriculeField.requestFocus();
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                     "Erreur lors de la recherche : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
                FenetreDetailleePhysique fenetre = 
                    new FenetreDetailleePhysique((Stage) getOwner(), matricule, session);
                fenetre.showAndWait();
                
                if (fenetre.isListeRougeConfirmee()) {
                    confirmed = true;
                    showAlert(Alert.AlertType.INFORMATION, "Liste rouge réussie",
                             "✅ Personne physique mise en liste rouge avec succès!");
                    close();
                }
            } else if ("M".equals(typePersonne)) {
                FenetreDetailleeMorale fenetre = 
                    new FenetreDetailleeMorale((Stage) getOwner(), matricule, session);
                fenetre.showAndWait();
                
                if (fenetre.isListeRougeConfirmee()) {
                    confirmed = true;
                    showAlert(Alert.AlertType.INFORMATION, "Liste rouge réussie",
                             "✅ Personne morale mise en liste rouge avec succès!");
                    close();
                }
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                     "Erreur lors de l'ouverture de la fenêtre de détail: " + ex.getMessage());
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getMatricule() {
        return matricule;
    }
    
    public static String showDialog(Stage parent) {
        ListeRougePersonne dialog = new ListeRougePersonne(parent);
        dialog.showAndWait();
        return dialog.isConfirmed() ? dialog.getMatricule() : null;
    }

    // Classe interne pour Personne Physique
    private class FenetreDetailleePhysique extends Stage {
        private boolean listeRougeConfirmee = false;
        private String matricule;
        private SessionUtilisateur session;
        private Map<String, String> donneesCompletes;
        
        private JFXTextField txtMatricule, txtNom, txtPrenom, txtDateNaissance, txtLieuNaissance;
        private JFXTextField txtNNI, txtTelephone, txtEmail, txtAdresseGeographique;
        private ToggleGroup groupeSexe;
        private JFXRadioButton radioMasculin, radioFeminin;
        private JFXButton btnConfirmerListeRouge, btnAnnuler;

        public FenetreDetailleePhysique(Stage parent, String matricule, SessionUtilisateur session) {
            initOwner(parent);
            initModality(Modality.WINDOW_MODAL);
            initStyle(StageStyle.UTILITY);
            
            this.matricule = matricule;
            this.session = session;
            chargerDonneesCompletes();
            
            if (donneesCompletes != null) {
                String topVigueur = donneesCompletes.get("PERS_TOPVIGUEUR");
                if ("2".equals(topVigueur)) {
                    showAlert("Personne déjà en liste rouge",
                             "⚠️ Cette personne est déjà en liste rouge !\n\n" +
                             "Matricule: " + matricule + "\n" +
                             "Nom: " + donneesCompletes.get("PERSP_NOM") + " " + donneesCompletes.get("PERSP_PRENOM") + "\n\n" +
                             "Impossible de mettre en liste rouge une personne déjà en liste rouge.");
                    return;
                }
                if ("1".equals(topVigueur)) {
                    showAlert("Personne supprimée",
                             "❌ Cette personne est supprimée !\n\n" +
                             "Matricule: " + matricule + "\n" +
                             "Nom: " + donneesCompletes.get("PERSP_NOM") + " " + donneesCompletes.get("PERSP_PRENOM") + "\n\n" +
                             "Impossible de mettre en liste rouge une personne supprimée.");
                    return;
                }
            }
            
            initInterface();
            peuplerChamps();
            
            setTitle("Détails Personne Physique - Liste Rouge");
            show();
        }
        
        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
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
            VBox mainLayout = new VBox(20);
            mainLayout.setPadding(new Insets(25));
            mainLayout.setStyle("-fx-background-color: white;");
            
            // Titre
            Label lblTitre = new Label("🔴 Mise en Liste Rouge - Personne Physique");
            lblTitre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: red;");
            lblTitre.setAlignment(Pos.CENTER);
            
            VBox.setMargin(lblTitre, new Insets(0, 0, 25, 0));
            
            // Panels
            TitledPane panelInfosPerso = creerPanelInfosPersonnelles();
            TitledPane panelCoordonnees = creerPanelCoordonnees();
            HBox panelBoutons = creerPanelBoutons();
            
            mainLayout.getChildren().addAll(lblTitre, panelInfosPerso, panelCoordonnees, panelBoutons);
            
            ScrollPane scrollPane = new ScrollPane(mainLayout);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: white; -fx-border-color: white;");
            
            Scene scene = new Scene(scrollPane, 1100, 700);
            setScene(scene);
            
            configurerEcouteurs();
        }
        
        private TitledPane creerPanelInfosPersonnelles() {
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));
            
            // Colonne 0
            int row = 0;
            grid.add(createLabel("🆔 Matricule:"), 0, row);
            txtMatricule = createDisabledTextField();
            grid.add(txtMatricule, 1, row++);
            
            grid.add(createLabel("👤 Nom:"), 0, row);
            txtNom = createDisabledTextField();
            grid.add(txtNom, 1, row++);
            
            grid.add(createLabel("👤 Prénom:"), 0, row);
            txtPrenom = createDisabledTextField();
            grid.add(txtPrenom, 1, row++);
            
            grid.add(createLabel("📅 Date de naissance:"), 0, row);
            txtDateNaissance = createDisabledTextField();
            grid.add(txtDateNaissance, 1, row++);
            
            grid.add(createLabel("🏠 Lieu de naissance:"), 0, row);
            txtLieuNaissance = createDisabledTextField();
            grid.add(txtLieuNaissance, 1, row++);
            
            grid.add(createLabel("⚧ Sexe:"), 0, row);
            HBox sexeBox = new HBox(20);
            sexeBox.setAlignment(Pos.CENTER_LEFT);
            groupeSexe = new ToggleGroup();
            radioMasculin = new JFXRadioButton("Masculin");
            radioFeminin = new JFXRadioButton("Féminin");
            radioMasculin.setToggleGroup(groupeSexe);
            radioFeminin.setToggleGroup(groupeSexe);
            radioMasculin.setDisable(true);
            radioFeminin.setDisable(true);
            sexeBox.getChildren().addAll(radioMasculin, radioFeminin);
            grid.add(sexeBox, 1, row++);
            
            grid.add(createLabel("🔢 NNI:"), 0, row);
            txtNNI = createDisabledTextField();
            grid.add(txtNNI, 1, row++);
            
            TitledPane panel = new TitledPane("📋 Informations Personnelles", grid);
            panel.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            panel.setExpanded(true);
            return panel;
        }
        
        private TitledPane creerPanelCoordonnees() {
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));
            
            int row = 0;
            grid.add(createLabel("📱 Téléphone:"), 0, row);
            txtTelephone = createDisabledTextField();
            grid.add(txtTelephone, 1, row++);
            
            grid.add(createLabel("📧 Email:"), 0, row);
            txtEmail = createDisabledTextField();
            grid.add(txtEmail, 1, row++);
            
            grid.add(createLabel("🏠 Adresse:"), 0, row);
            txtAdresseGeographique = createDisabledTextField();
            grid.add(txtAdresseGeographique, 1, row++);
            
            TitledPane panel = new TitledPane("📞 Coordonnées", grid);
            panel.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            panel.setExpanded(true);
            return panel;
        }
        
        private HBox creerPanelBoutons() {
            btnAnnuler = new JFXButton("❌ Annuler");
            btnConfirmerListeRouge = new JFXButton("🔴 Confirmer la Liste Rouge");
            
            styleButton(btnAnnuler, "#6c757d");
            styleButton(btnConfirmerListeRouge, "#b22222");
            
            HBox buttonBox = new HBox(25, btnAnnuler, btnConfirmerListeRouge);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));
            
            return buttonBox;
        }
        
        private Label createLabel(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            return label;
        }
        
        private JFXTextField createDisabledTextField() {
            JFXTextField field = new JFXTextField();
            field.setDisable(true);
            field.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #666666;");
            return field;
        }
        
        private void styleButton(JFXButton button, String color) {
            button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;",
                color
            ));
            button.setPrefSize(200, 45);
            button.setRipplerFill(javafx.scene.paint.Color.WHITE);
        }
        
        private void configurerEcouteurs() {
            btnConfirmerListeRouge.setOnAction(e -> confirmerListeRouge());
            btnAnnuler.setOnAction(e -> close());
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
        
        private void confirmerListeRouge() {
            if (verifierDejaEnListeRouge()) {
                return;
            }
            
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("CONFIRMATION FINALE DE LISTE ROUGE");
            confirmation.setHeaderText("Êtes-vous ABSOLUMENT sûr de vouloir mettre cette personne en liste rouge ?");
            confirmation.setContentText(
                "Matricule: " + matricule + "\n" +
                "Nom: " + txtNom.getText() + " " + txtPrenom.getText() + "\n\n" +
                "🔴 La personne sera marquée comme étant en liste rouge."
            );
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (effectuerMiseEnListeRouge()) {
                    listeRougeConfirmee = true;
                    showSuccessAlert("Liste rouge réussie",
                                   "✅ Personne physique mise en liste rouge avec succès!");
                    close();
                }
            }
        }
        
        private void showSuccessAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
        
        private boolean verifierDejaEnListeRouge() {
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
                    if ("2".equals(topVigueur)) {
                        showAlert("Personne déjà en liste rouge",
                                 "⚠️ Cette personne a été mise en liste rouge entre-temps !\n\n" +
                                 "Matricule: " + matricule + "\n" +
                                 "Nom: " + txtNom.getText() + " " + txtPrenom.getText() + "\n\n" +
                                 "Impossible de mettre en liste rouge une personne déjà en liste rouge.");
                        close();
                        return true;
                    }
                    if ("1".equals(topVigueur)) {
                        showAlert("Personne supprimée",
                                 "❌ Cette personne a été supprimée entre-temps !\n\n" +
                                 "Matricule: " + matricule + "\n" +
                                 "Nom: " + txtNom.getText() + " " + txtPrenom.getText() + "\n\n" +
                                 "Impossible de mettre en liste rouge une personne supprimée.");
                        close();
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
        
        private boolean effectuerMiseEnListeRouge() {
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                String url = session.getDatabaseUrl();
                String user = session.getDatabaseUser();
                String password = session.getDatabasePassword();
                
                conn = DriverManager.getConnection(url, user, password);
                
                String sql = "UPDATE PERSONNE SET PERS_TOPVIGUEUR = '2' WHERE PERS_NUMMAT = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, matricule);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("✅ Mise en liste rouge effectuée pour le matricule: " + matricule);
                    return true;
                } else {
                    showAlert("Erreur", "Aucune personne trouvée avec ce matricule.");
                    return false;
                }
                
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la mise en liste rouge: " + e.getMessage());
                return false;
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (conn != null) conn.close(); } catch (SQLException e) {}
            }
        }
        
        public boolean isListeRougeConfirmee() {
            return listeRougeConfirmee;
        }
    }

    // Classe interne pour Personne Morale
    private class FenetreDetailleeMorale extends Stage {
        private boolean listeRougeConfirmee = false;
        private String matricule;
        private SessionUtilisateur session;
        private Map<String, String> donneesCompletes;
        
        private JFXTextField txtMatricule, txtRaisonSociale, txtSigle, txtDateCreation;
        private JFXTextField txtNomRepresentant, txtFonctionRepresentant, txtTelephone, txtEmail, txtAdresseGeographique;
        private ComboBox<String> comboFormeJuridique;
        private JFXButton btnConfirmerListeRouge, btnAnnuler;

        public FenetreDetailleeMorale(Stage parent, String matricule, SessionUtilisateur session) {
            initOwner(parent);
            initModality(Modality.WINDOW_MODAL);
            initStyle(StageStyle.UTILITY);
            
            this.matricule = matricule;
            this.session = session;
            chargerDonneesCompletes();
            
            if (donneesCompletes != null) {
                String topVigueur = donneesCompletes.get("PERS_TOPVIGUEUR");
                if ("2".equals(topVigueur)) {
                    showAlert("Personne déjà en liste rouge",
                             "⚠️ Cette personne morale est déjà en liste rouge !\n\n" +
                             "Matricule: " + matricule + "\n" +
                             "Raison sociale: " + donneesCompletes.get("PERSM_RAISON") + "\n\n" +
                             "Impossible de mettre en liste rouge une personne déjà en liste rouge.");
                    return;
                }
                if ("1".equals(topVigueur)) {
                    showAlert("Personne supprimée",
                             "❌ Cette personne morale est supprimée !\n\n" +
                             "Matricule: " + matricule + "\n" +
                             "Raison sociale: " + donneesCompletes.get("PERSM_RAISON") + "\n\n" +
                             "Impossible de mettre en liste rouge une personne supprimée.");
                    return;
                }
            }
            
            initInterface();
            peuplerChamps();
            
            setTitle("Détails Personne Morale - Liste Rouge");
            show();
        }
        
        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
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
        
        private void initInterface() {
            VBox mainLayout = new VBox(20);
            mainLayout.setPadding(new Insets(25));
            mainLayout.setStyle("-fx-background-color: white;");
            
            // Titre
            Label lblTitre = new Label("🏢 Mise en Liste Rouge - Personne Morale");
            lblTitre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: red;");
            lblTitre.setAlignment(Pos.CENTER);
            
            VBox.setMargin(lblTitre, new Insets(0, 0, 25, 0));
            
            // Panels
            TitledPane panelInfos = creerPanelInformations();
            TitledPane panelRepresentant = creerPanelRepresentant();
            TitledPane panelCoordonnees = creerPanelCoordonnees();
            HBox panelBoutons = creerPanelBoutons();
            
            mainLayout.getChildren().addAll(lblTitre, panelInfos, panelRepresentant, panelCoordonnees, panelBoutons);
            
            ScrollPane scrollPane = new ScrollPane(mainLayout);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: white; -fx-border-color: white;");
            
            Scene scene = new Scene(scrollPane, 1100, 700);
            setScene(scene);
            
            configurerEcouteurs();
        }
        
        private TitledPane creerPanelInformations() {
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));
            
            int row = 0;
            grid.add(createLabel("🆔 Matricule:"), 0, row);
            txtMatricule = createDisabledTextField();
            grid.add(txtMatricule, 1, row++);
            
            grid.add(createLabel("🏢 Raison sociale:"), 0, row);
            txtRaisonSociale = createDisabledTextField();
            grid.add(txtRaisonSociale, 1, row++);
            
            grid.add(createLabel("🔤 Sigle:"), 0, row);
            txtSigle = createDisabledTextField();
            grid.add(txtSigle, 1, row++);
            
            grid.add(createLabel("📅 Date création:"), 0, row);
            txtDateCreation = createDisabledTextField();
            grid.add(txtDateCreation, 1, row++);
            
            grid.add(createLabel("⚖️ Forme juridique:"), 0, row);
            comboFormeJuridique = new ComboBox<>();
            comboFormeJuridique.getItems().addAll("SARL", "SA", "SNC", "SCS", "GIE");
            comboFormeJuridique.setDisable(true);
            comboFormeJuridique.setStyle("-fx-background-color: #f0f0f0;");
            grid.add(comboFormeJuridique, 1, row++);
            
            TitledPane panel = new TitledPane("📋 Informations de l'Entreprise", grid);
            panel.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            panel.setExpanded(true);
            return panel;
        }
        
        private TitledPane creerPanelRepresentant() {
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));
            
            int row = 0;
            grid.add(createLabel("👤 Nom représentant:"), 0, row);
            txtNomRepresentant = createDisabledTextField();
            grid.add(txtNomRepresentant, 1, row++);
            
            grid.add(createLabel("💼 Fonction:"), 0, row);
            txtFonctionRepresentant = createDisabledTextField();
            grid.add(txtFonctionRepresentant, 1, row++);
            
            TitledPane panel = new TitledPane("👤 Représentant Légal", grid);
            panel.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            panel.setExpanded(true);
            return panel;
        }
        
        private TitledPane creerPanelCoordonnees() {
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));
            
            int row = 0;
            grid.add(createLabel("📱 Téléphone:"), 0, row);
            txtTelephone = createDisabledTextField();
            grid.add(txtTelephone, 1, row++);
            
            grid.add(createLabel("📧 Email:"), 0, row);
            txtEmail = createDisabledTextField();
            grid.add(txtEmail, 1, row++);
            
            grid.add(createLabel("🏠 Adresse:"), 0, row);
            txtAdresseGeographique = createDisabledTextField();
            grid.add(txtAdresseGeographique, 1, row++);
            
            TitledPane panel = new TitledPane("📞 Coordonnées", grid);
            panel.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            panel.setExpanded(true);
            return panel;
        }
        
        private HBox creerPanelBoutons() {
            btnAnnuler = new JFXButton("❌ Annuler");
            btnConfirmerListeRouge = new JFXButton("🔴 Confirmer la Liste Rouge");
            
            styleButton(btnAnnuler, "#6c757d");
            styleButton(btnConfirmerListeRouge, "#b22222");
            
            HBox buttonBox = new HBox(25, btnAnnuler, btnConfirmerListeRouge);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));
            
            return buttonBox;
        }
        
        private Label createLabel(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            return label;
        }
        
        private JFXTextField createDisabledTextField() {
            JFXTextField field = new JFXTextField();
            field.setDisable(true);
            field.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #666666;");
            return field;
        }
        
        private void styleButton(JFXButton button, String color) {
            button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;",
                color
            ));
            button.setPrefSize(200, 45);
            button.setRipplerFill(javafx.scene.paint.Color.WHITE);
        }
        
        private void configurerEcouteurs() {
            btnConfirmerListeRouge.setOnAction(e -> confirmerListeRouge());
            btnAnnuler.setOnAction(e -> close());
        }
        
        private void peuplerChamps() {
            if (donneesCompletes != null) {
                txtMatricule.setText(matricule);
                txtRaisonSociale.setText(donneesCompletes.get("PERSM_RAISON"));
                txtSigle.setText(donneesCompletes.get("PERSM_NOMSOC"));
                txtDateCreation.setText(formatDate(donneesCompletes.get("PERSM_DATCREAT")));
                txtNomRepresentant.setText(donneesCompletes.get("PERSM_NOMGERANT"));
                txtFonctionRepresentant.setText(donneesCompletes.get("PERSM_TITGERANT"));
                txtTelephone.setText(donneesCompletes.get("PERS_TELEPHONE"));
                txtEmail.setText(donneesCompletes.get("PERS_EMAIL"));
                txtAdresseGeographique.setText(donneesCompletes.get("PERS_ADRGEOGR"));
                
                String formeJuridique = donneesCompletes.get("PERSM_FORMEJURID");
                if (formeJuridique != null && !formeJuridique.isEmpty()) {
                    comboFormeJuridique.setValue(formeJuridique);
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
        
        private void confirmerListeRouge() {
            if (verifierDejaEnListeRouge()) {
                return;
            }
            
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("CONFIRMATION FINALE DE LISTE ROUGE");
            confirmation.setHeaderText("Êtes-vous ABSOLUMENT sûr de vouloir mettre cette personne morale en liste rouge ?");
            confirmation.setContentText(
                "Matricule: " + matricule + "\n" +
                "Raison sociale: " + txtRaisonSociale.getText() + "\n\n" +
                "🔴 La personne sera marquée comme étant en liste rouge."
            );
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (effectuerMiseEnListeRouge()) {
                    listeRougeConfirmee = true;
                    showSuccessAlert("Liste rouge réussie",
                                   "✅ Personne morale mise en liste rouge avec succès!");
                    close();
                }
            }
        }
        
        private void showSuccessAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
        
        private boolean verifierDejaEnListeRouge() {
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
                    if ("2".equals(topVigueur)) {
                        showAlert("Personne déjà en liste rouge",
                                 "⚠️ Cette personne morale a été mise en liste rouge entre-temps !\n\n" +
                                 "Matricule: " + matricule + "\n" +
                                 "Raison sociale: " + txtRaisonSociale.getText() + "\n\n" +
                                 "Impossible de mettre en liste rouge une personne déjà en liste rouge.");
                        close();
                        return true;
                    }
                    if ("1".equals(topVigueur)) {
                        showAlert("Personne supprimée",
                                 "❌ Cette personne morale a été supprimée entre-temps !\n\n" +
                                 "Matricule: " + matricule + "\n" +
                                 "Raison sociale: " + txtRaisonSociale.getText() + "\n\n" +
                                 "Impossible de mettre en liste rouge une personne supprimée.");
                        close();
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
        
        private boolean effectuerMiseEnListeRouge() {
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                String url = session.getDatabaseUrl();
                String user = session.getDatabaseUser();
                String password = session.getDatabasePassword();
                
                conn = DriverManager.getConnection(url, user, password);
                
                String sql = "UPDATE PERSONNE SET PERS_TOPVIGUEUR = '2' WHERE PERS_NUMMAT = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, matricule);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("✅ Mise en liste rouge effectuée pour le matricule: " + matricule);
                    return true;
                } else {
                    showAlert("Erreur", "Aucune personne trouvée avec ce matricule.");
                    return false;
                }
                
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la mise en liste rouge: " + e.getMessage());
                return false;
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                try { if (conn != null) conn.close(); } catch (SQLException e) {}
            }
        }
        
        public boolean isListeRougeConfirmee() {
            return listeRougeConfirmee;
        }
    }
}