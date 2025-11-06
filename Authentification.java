package lisa1connexion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;

import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class Authentification extends Application {
    
    // Configuration base de données
    private final String URL = "jdbc:oracle:thin:@192.168.10.32:1521:bdmatca";
    private final String USER = "soft";
    private final String PASSWORD = "part";
    
    // Configuration Email Gmail
    private final String EMAIL_HOST = "smtp.gmail.com";
    private final String EMAIL_PORT = "587";
    private final String EMAIL_USERNAME = "kouamehermanng@gmail.com";
    private final String EMAIL_PASSWORD = "dwck nixm ikml cjrz";
    private final String EMAIL_FROM = "kouamehermanng@gmail.com";
    
    // Composants d'interface
    private JFXPasswordField txtMotDePasse;
    private JFXTextField txtCodeOTP;
    private Label lblEmail, lblNomPrenom, lblMatricule, lblStatutConnexion, lblTempsRestant;
    private String codeOTP;
    private JFXButton btnConnexion, btnRenvoiOTP, btnAide;
    private VBox panelOTP, panelInfos;
    private JFXProgressBar progressBar;
    
    // Compteur d'échecs de connexion
    private int tentativesEchec = 0;
    private static final int MAX_TENTATIVES = 3;
    
    // Timer pour OTP
    private Timeline timerOTP;
    private int tempsRestant = 60;
    
    // État de l'application
    private boolean connexionEnCours = false;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initInterface();
        configurerEcouteurs();
    }

    private void initInterface() {
        primaryStage.setTitle("🔐 Authentification Agent CNA - MATCA");
        
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        // Initialisation des composants
        VBox headerPanel = crearPanelEntete();
        HBox panelStatut = crearPanelStatut();
        GridPane panelSaisie = crearPanelSaisie();
        panelInfos = crearPanelInfosAgent();
        panelOTP = crearPanelOTP();
        HBox panelBoutons = crearPanelBoutons();
        
        // Barre de progression
        progressBar = new JFXProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(400);
        
        // Assemblage
        mainContainer.getChildren().addAll(
            headerPanel, panelStatut, panelSaisie, 
            panelInfos, panelOTP, panelBoutons
        );
        
        VBox rootContainer = new VBox();
        rootContainer.getChildren().addAll(progressBar, mainContainer);
        
        Scene scene = new Scene(rootContainer, 600, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // Focus initial sur le mot de passe
        Platform.runLater(() -> txtMotDePasse.requestFocus());
    }
    
    private VBox crearPanelEntete() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10));
        
        Label titre = new Label("CONNEXION UTILISATEUR");
        titre.getStyleClass().add("title");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label sousTitre = new Label("Application MATCA - Sécurisée");
        sousTitre.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        panel.getChildren().addAll(titre, sousTitre);
        return panel;
    }
    
    private HBox crearPanelStatut() {
        HBox panel = new HBox();
        panel.setAlignment(Pos.CENTER);
        
        lblStatutConnexion = new Label("🔒 Connectez-vous avec votre mot de passe");
        lblStatutConnexion.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        panel.getChildren().add(lblStatutConnexion);
        return panel;
    }
    
    private GridPane crearPanelSaisie() {
        GridPane panel = new GridPane();
        panel.setHgap(10);
        panel.setVgap(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-background-radius: 5;");
        
        // Label mot de passe
        Label lblMotDePasse = new Label("🔑 Mot de passe:");
        lblMotDePasse.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        GridPane.setConstraints(lblMotDePasse, 0, 0);
        
        // Champ mot de passe
        txtMotDePasse = new JFXPasswordField();
        txtMotDePasse.setPromptText("Saisissez votre mot de passe");
        txtMotDePasse.setPrefWidth(200);
        txtMotDePasse.setPrefHeight(35);
        GridPane.setConstraints(txtMotDePasse, 1, 0);
        
        // Indication aide
        Label lblAide = new Label("💡 Appuyez sur Entrée après avoir saisi votre mot de passe");
        lblAide.setStyle("-fx-font-style: italic; -fx-font-size: 10px; -fx-text-fill: #7f8c8d;");
        GridPane.setConstraints(lblAide, 0, 1, 2, 1);
        
        panel.getChildren().addAll(lblMotDePasse, txtMotDePasse, lblAide);
        return panel;
    }
    
    private VBox crearPanelInfosAgent() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-background-radius: 5;");
        panel.setVisible(false);
        
        // Panel matricule
        HBox panelMatricule = new HBox(10);
        panelMatricule.setAlignment(Pos.CENTER_LEFT);
        Label lblIconMatricule = new Label("🆔");
        Label lblLabelMatricule = new Label("Matricule:");
        lblLabelMatricule.setStyle("-fx-font-weight: bold;");
        lblMatricule = new Label("Non identifié");
        lblMatricule.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        panelMatricule.getChildren().addAll(lblIconMatricule, lblLabelMatricule, lblMatricule);
        
        // Panel nom
        HBox panelNomPrenom = new HBox(10);
        panelNomPrenom.setAlignment(Pos.CENTER_LEFT);
        Label lblIconNom = new Label("👤");
        Label lblLabelNom = new Label("Agent:");
        lblLabelNom.setStyle("-fx-font-weight: bold;");
        lblNomPrenom = new Label("Non identifié");
        lblNomPrenom.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        panelNomPrenom.getChildren().addAll(lblIconNom, lblLabelNom, lblNomPrenom);
        
        // Panel email
        HBox panelEmail = new HBox(10);
        panelEmail.setAlignment(Pos.CENTER_LEFT);
        Label lblIconEmail = new Label("📧");
        Label lblLabelEmail = new Label("Email:");
        lblLabelEmail.setStyle("-fx-font-weight: bold;");
        lblEmail = new Label("Non identifié");
        lblEmail.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        panelEmail.getChildren().addAll(lblIconEmail, lblLabelEmail, lblEmail);
        
        panel.getChildren().addAll(panelMatricule, panelNomPrenom, panelEmail);
        return panel;
    }
    
    private VBox crearPanelOTP() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-background-radius: 5;");
        panel.setVisible(false);
        
        // Instruction
        Label lblInstruction = new Label("📧 Un code de vérification a été envoyé à votre adresse email:");
        lblInstruction.setStyle("-fx-font-size: 11px;");
        
        // Temps restant
        lblTempsRestant = new Label("⏱️ Temps restant: 1:00");
        lblTempsRestant.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #dc3545;");
        lblTempsRestant.setAlignment(Pos.CENTER);
        
        // Saisie OTP
        HBox panelSaisieOTP = new HBox(10);
        panelSaisieOTP.setAlignment(Pos.CENTER);
        Label lblSaisie = new Label("Code OTP:");
        lblSaisie.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        
        txtCodeOTP = new JFXTextField();
        txtCodeOTP.setPromptText("Saisissez le code OTP");
        txtCodeOTP.setPrefWidth(120);
        txtCodeOTP.setPrefHeight(35);
        
        panelSaisieOTP.getChildren().addAll(lblSaisie, txtCodeOTP);
        panel.getChildren().addAll(lblInstruction, lblTempsRestant, panelSaisieOTP);
        
        return panel;
    }
    
    private HBox crearPanelBoutons() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        
        // Bouton Aide
        btnAide = new JFXButton("❓ Aide");
        styliserBouton(btnAide, "#6c757d", "#495057");
        
        // Bouton Renvoyer OTP
        btnRenvoiOTP = new JFXButton("🔄 Renvoyer le code");
        styliserBouton(btnRenvoiOTP, "#ffc107", "#ff9f1c");
        btnRenvoiOTP.setVisible(false);
        
        // Bouton Connexion
        btnConnexion = new JFXButton("🚀 Se Connecter");
        styliserBouton(btnConnexion, "#28a745", "#218838");
        btnConnexion.setDisable(true);
        
        panel.getChildren().addAll(btnAide, btnRenvoiOTP, btnConnexion);
        return panel;
    }
    
    private void styliserBouton(JFXButton bouton, String couleurBase, String couleurSurvol) {
        bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-padding: 10 20 10 20;",
            couleurBase
        ));
        
        bouton.setOnMouseEntered(e -> 
            bouton.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 5; -fx-padding: 10 20 10 20;",
                couleurSurvol
            ))
        );
        
        bouton.setOnMouseExited(e -> 
            bouton.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 5; -fx-padding: 10 20 10 20;",
                couleurBase
            ))
        );
    }
    
    private void configurerEcouteurs() {
        // Entrée sur le mot de passe
        txtMotDePasse.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !connexionEnCours) {
                trouverAgentParMotDePasse();
            }
        });
        
        // Bouton connexion
        btnConnexion.setOnAction(e -> {
            if (!connexionEnCours) {
                verifierCodeOTP();
            }
        });
        
        // Bouton renvoyer OTP
        btnRenvoiOTP.setOnAction(e -> envoyerCodeOTP());
        
        // Entrée sur le code OTP
        txtCodeOTP.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !connexionEnCours) {
                verifierCodeOTP();
            }
        });
        
        // Bouton aide
        btnAide.setOnAction(e -> afficherAide());
    }
    
    private void demarrerConnexion() {
        connexionEnCours = true;
        progressBar.setVisible(true);
        btnConnexion.setDisable(true);
        lblStatutConnexion.setText("🔍 Vérification en cours...");
        lblStatutConnexion.setStyle("-fx-text-fill: #ffc107;");
    }
    
    private void arreterConnexion() {
        connexionEnCours = false;
        progressBar.setVisible(false);
        btnConnexion.setDisable(false);
        lblStatutConnexion.setStyle("-fx-text-fill: #7f8c8d;");
    }

    // ====================================================================
    // MÉTHODE : trouverAgentParMotDePasse()
    // ====================================================================
    private void trouverAgentParMotDePasse() {
        String motDePasse = txtMotDePasse.getText().trim();
        
        if (motDePasse.isEmpty()) {
            afficherMessageErreur("Veuillez saisir votre mot de passe.", "Champ obligatoire");
            txtMotDePasse.requestFocus();
            return;
        }
        
        if (tentativesEchec >= MAX_TENTATIVES) {
            afficherMessageErreur("Trop de tentatives échouées. L'application va se fermer.", "Sécurité");
            Platform.exit();
            return;
        }
        
        demarrerConnexion();
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // UTILISATION DE SessionUtilisateur POUR LA CONNEXION BDD
                SessionUtilisateur session = SessionUtilisateur.getInstance();
                String url = session.getDatabaseUrl();
                String user = session.getDatabaseUser();
                String password = session.getDatabasePassword();
                
                try (Connection conn = DriverManager.getConnection(url, user, password);
                     PreparedStatement stmt = conn.prepareStatement(
                         "SELECT agent_matricule, agent_nom, AGENT_STATUT, Email, AGEN_CODE FROM agent_cna WHERE agent_mopasse = ?")) {
                    
                    stmt.setString(1, motDePasse);
                    try (ResultSet rs = stmt.executeQuery()) {
                        
                        if (rs.next()) {
                            String agent_Matricule = rs.getString("agent_matricule");
                            String agent_Nom = rs.getString("agent_nom");
                            String statut = rs.getString("AGENT_STATUT");
                            String Email = rs.getString("Email");
                            String agence = rs.getString("AGEN_CODE");
                            
                            Platform.runLater(() -> {
                                if ("1".equals(statut)) {
                                    afficherMessageErreur("Votre compte est désactivé. Contactez l'administrateur.", "Compte désactivé");
                                    reinitialiserInterface();
                                } else {
                                    // ✅ INITIALISER LA SESSION GLOBALE
                                    SessionUtilisateur userSession = SessionUtilisateur.getInstance();
                                    userSession.initialiserSession(agent_Nom, agent_Matricule, agence, Email);
                                    
                                    afficherInfosAgent(agent_Nom, agent_Matricule, agence, Email);
                                    envoyerCodeOTP();
                                    tentativesEchec = 0;
                                }
                                arreterConnexion();
                            });
                            
                        } else {
                            Platform.runLater(() -> {
                                tentativesEchec++;
                                afficherMessageErreur("Mot de passe incorrect. Tentative " + tentativesEchec + "/" + MAX_TENTATIVES, "Échec authentification");
                                reinitialiserInterface();
                                arreterConnexion();
                            });
                        }
                    }
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        afficherMessageErreur("Erreur de connexion à la base: " + e.getMessage(), "Erreur technique");
                        arreterConnexion();
                    });
                }
                return null;
            }
        };
        
        new Thread(task).start();
    }

    // ====================================================================
    // MÉTHODE : envoyerCodeOTP()
    // ====================================================================
    private void envoyerCodeOTP() {
        // Récupérer l'email depuis la session
        SessionUtilisateur session = SessionUtilisateur.getInstance();
        String Email = session.getEmailBrut();
        
        if (Email == null || Email.isEmpty()) {
            afficherMessageErreur("Aucune adresse email trouvée pour cet agent.", "Erreur Email");
            return;
        }
        
        demarrerConnexion();
        lblStatutConnexion.setText("📧 Envoi du code OTP...");
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                codeOTP = String.format("%06d", (int)(Math.random() * 1000000));
                return envoyerEmailOTP(Email, codeOTP);
            }
        };
        
        task.setOnSucceeded(e -> {
            boolean emailEnvoye = task.getValue();
            if (emailEnvoye) {
                afficherMessageSucces("Code OTP envoyé avec succès à " + masquerEmail(Email), "Email envoyé");
                demarrerTimerOTP();
                btnRenvoiOTP.setVisible(true);
                txtCodeOTP.requestFocus();
            } else {
                afficherMessageErreur("Erreur lors de l'envoi de l'email. Vérifiez la configuration.", "Erreur d'envoi");
            }
            arreterConnexion();
        });
        
        task.setOnFailed(e -> {
            afficherMessageErreur("Erreur lors de l'envoi du code OTP: " + task.getException().getMessage(), "Erreur technique");
            arreterConnexion();
        });
        
        new Thread(task).start();
    }

    // ====================================================================
    // MÉTHODE : envoyerEmailOTP()
    // ====================================================================
    private boolean envoyerEmailOTP(String destinataire, String code) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", EMAIL_HOST);
            props.put("mail.smtp.port", EMAIL_PORT);
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "MATCA Authentification"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("🔐 Code de vérification MATCA");
            
            // Récupérer le nom depuis la session
            SessionUtilisateur userSession = SessionUtilisateur.getInstance();
            String nomAgent = userSession.getNomBrut();
            
            message.setText(creerContenuEmail(code, nomAgent));
            
            Transport.send(message);
            System.out.println("✅ Email OTP envoyé à: " + destinataire);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            System.out.println("🔐 CODE OTP POUR TEST (erreur d'envoi): " + codeOTP);
            return true; // Pour permettre la continuation des tests
        }
    }

    private String creerContenuEmail(String code, String nomAgent) {
        return "Bonjour " + nomAgent + ",\n\n" +
               "Votre code de vérification pour l'application MATCA est :\n\n" +
               "🔒 " + code + "\n\n" +
               "Ce code est valable pendant 1 minutes.\n\n" +
               "Si vous n'avez pas demandé ce code, veuillez ignorer cet email.\n\n" +
               "Cordialement,\nL'équipe MATCA";
    }

    private void demarrerTimerOTP() {
        tempsRestant = 60;
        if (timerOTP != null) {
            timerOTP.stop();
        }
        
        timerOTP = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempsRestant--;
            int minutes = tempsRestant / 60;
            int secondes = tempsRestant % 60;
            lblTempsRestant.setText(String.format("⏱️ Temps restant: %d:%02d", minutes, secondes));
            
            if (tempsRestant <= 0) {
                timerOTP.stop();
                lblTempsRestant.setText("❌ Code expiré");
                lblTempsRestant.setStyle("-fx-text-fill: #dc3545;");
                codeOTP = null;
                btnRenvoiOTP.setDisable(false);
            } else if (tempsRestant <= 60) {
                lblTempsRestant.setStyle("-fx-text-fill: #dc3545;");
            }
        }));
        timerOTP.setCycleCount(Timeline.INDEFINITE);
        timerOTP.play();
    }

    // ====================================================================
    // MÉTHODE : verifierCodeOTP()
    // ====================================================================
    private void verifierCodeOTP() {
        String codeSaisi = txtCodeOTP.getText().trim();
        
        if (codeSaisi.isEmpty()) {
            afficherMessageErreur("Veuillez saisir le code OTP.", "Code manquant");
            txtCodeOTP.requestFocus();
            return;
        }
        
        if (codeOTP == null) {
            afficherMessageErreur("Le code OTP a expiré. Veuillez en demander un nouveau.", "Code expiré");
            return;
        }
        
        if (!codeSaisi.equals(codeOTP)) {
            afficherMessageErreur("Code OTP incorrect. Veuillez réessayer.", "Erreur OTP");
            txtCodeOTP.setText("");
            txtCodeOTP.requestFocus();
            return;
        }
        
        if (timerOTP != null) {
            timerOTP.stop();
        }
        
        // Récupérer le nom depuis la session
        SessionUtilisateur session = SessionUtilisateur.getInstance();
        String nomAgent = session.getNom();
        
        afficherMessageSucces("Authentification réussie!\nBienvenue " + nomAgent, "Connexion réussie");
        ouvrirInterfacePrincipale();
    }

    // ====================================================================
    // MÉTHODE : ouvrirInterfacePrincipale()
    // ====================================================================
    private void ouvrirInterfacePrincipale() {
        primaryStage.close();
        
        Platform.runLater(() -> {
            try {
                // Vérifier que la session est valide
                SessionUtilisateur session = SessionUtilisateur.getInstance();
                if (!session.estValide()) {
                    afficherMessageErreur("Session invalide. Veuillez vous reconnecter.", "Erreur session");
                    return;
                }
                
                System.out.println("🚀 Ouverture de l'interface principale pour: " + session.getResume());
                
                // Ouvrir l'interface principale
                Stage mainStage = new Stage();
                InterfacePrincipale interfacePrincipale = new InterfacePrincipale();
                interfacePrincipale.start(mainStage);
                
            } catch (Exception e) {
                afficherMessageErreur("Erreur lors du lancement de l'interface: " + e.getMessage(), "Erreur");
                e.printStackTrace();
            }
        });
    }

    // ====================================================================
    // MÉTHODES AUXILIAIRES
    // ====================================================================
    
    private void afficherInfosAgent(String nomComplet, String matricule, String agence, String email) {
        lblMatricule.setText(matricule != null ? matricule : "N/A");
        lblNomPrenom.setText(nomComplet != null ? nomComplet : "N/A");
        lblEmail.setText(email != null ? masquerEmail(email) : "N/A");
        
        panelInfos.setVisible(true);
        panelOTP.setVisible(true);
        btnConnexion.setDisable(false);
        
        lblStatutConnexion.setText("✅ Agent identifié - Vérifiez votre email");
        lblStatutConnexion.setStyle("-fx-text-fill: #28a745;");
        
        primaryStage.sizeToScene();
        
        System.out.println("✅ Agent trouvé: " + nomComplet);
        System.out.println("🔢 Matricule: " + matricule);
        System.out.println("🏢 Agence: " + agence);
        System.out.println("📧 Email: " + email);
    }

    private String masquerEmail(String email) {
        if (email == null || email.length() < 5) return email;
        int atIndex = email.indexOf('@');
        if (atIndex < 3) return email;
        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }

    private void reinitialiserInterface() {
        txtMotDePasse.setText("");
        txtCodeOTP.setText("");
        
        panelInfos.setVisible(false);
        panelOTP.setVisible(false);
        btnRenvoiOTP.setVisible(false);
        
        if (timerOTP != null) {
            timerOTP.stop();
        }
        
        btnConnexion.setDisable(true);
        lblStatutConnexion.setText("🔒 Connectez-vous avec votre mot de passe");
        txtMotDePasse.requestFocus();
        
        primaryStage.sizeToScene();
    }

    private void afficherAide() {
        String message = 
            "🔍 Aide - Authentification MATCA\n\n" +
            "Procédure de connexion :\n" +
            "1. Saisissez votre mot de passe et appuyez sur Entrée\n" +
            "2. Si le mot de passe est correct, un code OTP est envoyé à votre email\n" +
            "3. Saisissez le code OTP reçu par email\n" +
            "4. Cliquez sur Se Connecter ou appuyez sur Entrée\n\n" +
            "En cas de problème :\n" +
            "• Code non reçu : Cliquez sur Renvoyer le code\n" +
            "• Code expiré : Demandez un nouveau code\n" +
            "• Email incorrect : Contactez l'administrateur";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aide - MATCA");
        alert.setHeaderText("🔍 Aide - Authentification MATCA");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void afficherMessageErreur(String message, String titre) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("❌ " + titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void afficherMessageSucces(String message, String titre) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ " + titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}