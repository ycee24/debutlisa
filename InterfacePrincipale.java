package lisa1connexion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;

public class InterfacePrincipale extends Application {
    
    // Composants d'interface
    private Label lblWelcome, lblUserInfo;
    private JFXTabPane tabbedPane;
    private VBox panelDashboard, panelGestionPersonnes, panelProductionAutomobile, 
                panelSinistre, panelIndemnisation, panelAdministrateur, panelStatistique;
    
    // Données de la personne sélectionnée
    private String personneSelectionnee_Matricule;
    private String personneSelectionnee_Nom;
    private String personneSelectionnee_Prenom;
    
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initInterface();
        configurerComposants();
    }

    private void initInterface() {
        // Récupérer les infos depuis la session globale
        SessionUtilisateur session = SessionUtilisateur.getInstance();
        
        primaryStage.setTitle("🏢 MATCA - Connecté: " + session.getNom());
        
        // Création de l'interface principale
        BorderPane mainPanel = creerLayoutPrincipal();
        
        Scene scene = new Scene(mainPanel, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    
    private BorderPane creerLayoutPrincipal() {
        BorderPane mainPanel = new BorderPane();
        mainPanel.setPadding(new Insets(5));
        
        // En-tête
        HBox headerPanel = creerHeaderPanel();
        
        // Contenu principal avec onglets
        JFXTabPane tabbedPane = creerOnglets();
        
        // Pied de page
        HBox footerPanel = creerFooterPanel();
        
        // Assemblage
        mainPanel.setTop(headerPanel);
        mainPanel.setCenter(tabbedPane);
        mainPanel.setBottom(footerPanel);
        
        return mainPanel;
    }
    
    private HBox creerHeaderPanel() {
        HBox headerPanel = new HBox();
        headerPanel.setStyle("-fx-background-color: #ff8c00;"); // Orange
        headerPanel.setPadding(new Insets(10, 20, 10, 20));
        headerPanel.setPrefHeight(80);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        
        // Titre principal
        Label titleLabel = new Label("🏢 MATCA - LISA Logiciel Intégré pour les Sociétés d'Assurances");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        // Informations utilisateur depuis la session globale
        HBox userPanel = new HBox(20);
        userPanel.setAlignment(Pos.CENTER_RIGHT);
        userPanel.setStyle("-fx-background-color: transparent;");
        
        SessionUtilisateur session = SessionUtilisateur.getInstance();
        lblUserInfo = new Label(session.getResume());
        lblUserInfo.setFont(Font.font("Segoe UI", 14));
        lblUserInfo.setTextFill(Color.WHITE);
        
        JFXButton btnDeconnexion = new JFXButton("🚪 Déconnexion");
        styliserBoutonHeader(btnDeconnexion);
        
        btnDeconnexion.setOnAction(e -> deconnecter());
        
        userPanel.getChildren().addAll(lblUserInfo, btnDeconnexion);
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerPanel.getChildren().addAll(titleLabel, userPanel);
        
        return headerPanel;
    }
    
    private JFXTabPane creerOnglets() {
        JFXTabPane tabbedPane = new JFXTabPane();
        
        // Création des différents onglets dans l'ordre spécifié
        Tab tabDashboard = new Tab("📊 Tableau de Bord");
        tabDashboard.setContent(creerOngletDashboard());
        tabDashboard.setClosable(false);
        
        Tab tabGestionPersonnes = new Tab("👥 Gestion Personnes");
        tabGestionPersonnes.setContent(creerOngletGestionPersonnes());
        tabGestionPersonnes.setClosable(false);
        
        Tab tabProductionAuto = new Tab("🚗 Production Auto");
        tabProductionAuto.setContent(creerOngletProductionAutomobile());
        tabProductionAuto.setClosable(false);
        
        Tab tabSinistre = new Tab("🚨 Sinistres");
        tabSinistre.setContent(creerOngletSinistre());
        tabSinistre.setClosable(false);
        
        Tab tabIndemnisation = new Tab("💰 Indemnisation");
        tabIndemnisation.setContent(creerOngletIndemnisation());
        tabIndemnisation.setClosable(false);
        
        Tab tabAdministrateur = new Tab("⚙️ Administrateur");
        tabAdministrateur.setContent(creerOngletAdministrateur());
        tabAdministrateur.setClosable(false);
        
        Tab tabStatistique = new Tab("📈 Statistiques");
        tabStatistique.setContent(creerOngletStatistique());
        tabStatistique.setClosable(false);
        
        tabbedPane.getTabs().addAll(
            tabDashboard, tabGestionPersonnes, tabProductionAuto, 
            tabSinistre, tabIndemnisation, tabAdministrateur, tabStatistique
        );
        
        return tabbedPane;
    }
    
    private VBox creerOngletDashboard() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        // Titre
        Label titleLabel = new Label("🎯 Tableau de Bord");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setAlignment(Pos.CENTER);
        
        // Cartes de statistiques
        GridPane cardsPanel = creerCartesDashboard();
        
        // Actions rapides
        HBox quickActionsPanel = creerActionsRapides();
        
        panel.getChildren().addAll(titleLabel, cardsPanel, quickActionsPanel);
        return panel;
    }
    
    private GridPane creerCartesDashboard() {
        GridPane panel = new GridPane();
        panel.setHgap(15);
        panel.setVgap(15);
        panel.setPadding(new Insets(0, 0, 30, 0));
        
        // Carte 1: Personnes enregistrées
        VBox card1 = creerCarte("👥 Personnes enregistrées", "156", Color.web("#ff8c00"));
        // Carte 2: Sinistres déclarés
        VBox card2 = creerCarte("🚨 Sinistres déclarés", "42", Color.web("#ff4500"));
        // Carte 3: Sinistres en traitement
        VBox card3 = creerCarte("⏳ Sinistres en traitement", "18", Color.web("#ffa500"));
        // Carte 4: Sinistres urgents
        VBox card4 = creerCarte("🚨 Sinistres urgents", "5", Color.web("#dc3545"));
        // Carte 5: Indemnisations versées
        VBox card5 = creerCarte("💰 Indemnisations versées", "28", Color.web("#ff8c00"));
        // Carte 6: Taux de traitement
        VBox card6 = creerCarte("📈 Taux de traitement", "85%", Color.web("#2e7d32"));
        
        // Ajout au grid
        panel.add(card1, 0, 0);
        panel.add(card2, 1, 0);
        panel.add(card3, 2, 0);
        panel.add(card4, 0, 1);
        panel.add(card5, 1, 1);
        panel.add(card6, 2, 1);
        
        return panel;
    }
    
    private VBox creerCarte(String titre, String valeur, Color couleur) {
        VBox carte = new VBox(10);
        carte.setPadding(new Insets(20));
        carte.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        carte.setPrefSize(200, 120);
        
        Label lblTitre = new Label(titre);
        lblTitre.setFont(Font.font("Segoe UI", 14));
        lblTitre.setTextFill(Color.web("#6c757d"));
        
        Label lblValeur = new Label(valeur);
        lblValeur.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        lblValeur.setTextFill(couleur);
        
        carte.getChildren().addAll(lblTitre, lblValeur);
        return carte;
    }
    
    private HBox creerActionsRapides() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ff8c00; -fx-border-width: 2;");
        
        String[] actions = {
            "➕ Nouvelle Personne", "🚨 Déclarer Sinistre", "💰 Indemniser", "📤 Exporter", "🔄 Actualiser"
        };
        
        // Couleurs orange pour tous les boutons
        String[] couleurs = {
            "#ff8c00", "#ffa500", "#ff7800", "#ffb400", "#ff6400"
        };
        
        for (int i = 0; i < actions.length; i++) {
            JFXButton btn = new JFXButton(actions[i]);
            styliserBoutonActionRapide(btn, couleurs[i]);
            panel.getChildren().add(btn);
        }
        
        return panel;
    }
    
    private VBox creerOngletGestionPersonnes() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        // En-tête avec titre et boutons
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("👥 Gestion des Personnes");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Boutons d'action
        HBox buttonsPanel = new HBox(15);
        buttonsPanel.setAlignment(Pos.CENTER_RIGHT);
        
        JFXButton btnModifier = new JFXButton("✏️ Modifier");
        JFXButton btnSupprimer = new JFXButton("🗑️ Supprimer");
        JFXButton btnRechercher = new JFXButton("🔍 Rechercher");
        JFXButton btnLrouge = new JFXButton("🔍 Mettre en liste Rouge");
        
        styliserBoutonAction(btnModifier, "#ffa500");
        styliserBoutonAction(btnSupprimer, "#dc3545");
        styliserBoutonAction(btnRechercher, "#ff7800");
        styliserBoutonAction(btnLrouge, "#ff7800");
        
        // Actions
        btnModifier.setOnAction(e -> {
            // Ouvre la fenêtre de modification
            Platform.runLater(() -> {
                Stage modificationStage = new Stage();
                ModificationPersonne modificationDialog = new ModificationPersonne();
                modificationDialog.start(modificationStage);
            });
        });
        
        btnSupprimer.setOnAction(e -> {
            String matricule = SuppressionPersonne.showDialog(primaryStage);
            if (matricule != null) {
                showAlert("Suppression réussie", "✅ Suppression effectuée pour le matricule: " + matricule, Alert.AlertType.INFORMATION);
            }
        });
        
        btnRechercher.setOnAction(e -> {
            Platform.runLater(() -> {
                Stage rechercheStage = new Stage();
                RecherchePersonne rechercheDialog = new RecherchePersonne();
                rechercheDialog.start(rechercheStage);
            });
        });
        
        buttonsPanel.getChildren().addAll(btnRechercher, btnModifier, btnSupprimer, btnLrouge);
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerPanel.getChildren().addAll(titleLabel, buttonsPanel);
        
        // Tableau des personnes (simulé)
        VBox tablePanel = new VBox();
        tablePanel.setPadding(new Insets(20));
        tablePanel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label tableLabel = new Label("📋 Tableau des personnes - Fonctionnalité en développement");
        tableLabel.setFont(Font.font("Segoe UI", 16));
        tableLabel.setTextFill(Color.web("#6c757d"));
        tableLabel.setAlignment(Pos.CENTER);
        
        tablePanel.getChildren().add(tableLabel);
        VBox.setVgrow(tablePanel, Priority.ALWAYS);
        
        panel.getChildren().addAll(headerPanel, tablePanel);
        return panel;
    }
    
    private VBox creerOngletProductionAutomobile() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        // En-tête avec titre et boutons
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🚗 Production Automobile");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Boutons d'action
        HBox buttonsPanel = new HBox(15);
        buttonsPanel.setAlignment(Pos.CENTER_RIGHT);
        
        JFXButton btnNouveauVehicule = new JFXButton("➕ Nouveau Véhicule");
        JFXButton btnPlanProduction = new JFXButton("📅 Plan Production");
        JFXButton btnSuiviChain = new JFXButton("🏭 Suivi Chaîne");
        JFXButton btnRapportProd = new JFXButton("📊 Rapport Production");
        
        styliserBoutonAction(btnNouveauVehicule, "#ff8c00");
        styliserBoutonAction(btnPlanProduction, "#ffa500");
        styliserBoutonAction(btnSuiviChain, "#ff7800");
        styliserBoutonAction(btnRapportProd, "#ff6400");
        
        buttonsPanel.getChildren().addAll(btnRapportProd, btnNouveauVehicule, btnPlanProduction, btnSuiviChain);
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerPanel.getChildren().addAll(titleLabel, buttonsPanel);
        
        // Contenu principal
        HBox contentPanel = new HBox(20);
        contentPanel.setPadding(new Insets(20, 0, 0, 0));
        
        // Statistiques production
        VBox statsPanel = new VBox();
        statsPanel.setPadding(new Insets(15));
        statsPanel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(15));
        
        statsGrid.add(creerMiniCarte("Véhicules produits", "1,248", Color.web("#ff8c00")), 0, 0);
        statsGrid.add(creerMiniCarte("En production", "156", Color.web("#ffa500")), 1, 0);
        statsGrid.add(creerMiniCarte("Taux de défaut", "2.3%", Color.web("#ff4500")), 0, 1);
        statsGrid.add(creerMiniCarte("Efficacité", "94.5%", Color.web("#2e7d32")), 1, 1);
        statsGrid.add(creerMiniCarte("Objectif mensuel", "1,500", Color.web("#ff7800")), 0, 2);
        statsGrid.add(creerMiniCarte("Retard", "3.2%", Color.web("#dc3545")), 1, 2);
        
        statsPanel.getChildren().add(statsGrid);
        
        // Contrôles qualité
        VBox qualitePanel = new VBox();
        qualitePanel.setPadding(new Insets(15));
        qualitePanel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        TextArea txtQualite = new TextArea(
            "🔧 Contrôles en cours:\n" +
            "• Peinture: 98% ✓\n" +
            "• Moteur: 96% ✓\n" +
            "• Électronique: 94% ⚠️\n" +
            "• Carrosserie: 99% ✓\n\n" +
            "📋 Inspections aujourd'hui: 42\n" +
            "✅ Conformité: 97.2%"
        );
        txtQualite.setFont(Font.font("Segoe UI", 12));
        txtQualite.setEditable(false);
        
        qualitePanel.getChildren().add(txtQualite);
        
        contentPanel.getChildren().addAll(statsPanel, qualitePanel);
        HBox.setHgrow(statsPanel, Priority.ALWAYS);
        HBox.setHgrow(qualitePanel, Priority.ALWAYS);
        
        panel.getChildren().addAll(headerPanel, contentPanel);
        return panel;
    }
    
    private VBox creerMiniCarte(String titre, String valeur, Color couleur) {
        VBox carte = new VBox(5);
        carte.setPadding(new Insets(8));
        carte.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label lblTitre = new Label(titre);
        lblTitre.setFont(Font.font("Segoe UI", 10));
        lblTitre.setTextFill(Color.web("#6c757d"));
        
        Label lblValeur = new Label(valeur);
        lblValeur.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblValeur.setTextFill(couleur);
        
        carte.getChildren().addAll(lblTitre, lblValeur);
        return carte;
    }
    
    private VBox creerOngletSinistre() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        // En-tête avec titre et boutons
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🚨 Gestion des Sinistres");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Boutons d'action
        HBox buttonsPanel = new HBox(15);
        buttonsPanel.setAlignment(Pos.CENTER_RIGHT);
        
        JFXButton btnNouveauSinistre = new JFXButton("➕ Nouveau Sinistre");
        JFXButton btnTraiter = new JFXButton("⚡ Traiter");
        JFXButton btnUrgent = new JFXButton("🚨 Marquer Urgent");
        JFXButton btnRapport = new JFXButton("📊 Rapport");
        
        styliserBoutonAction(btnNouveauSinistre, "#ff4500");
        styliserBoutonAction(btnTraiter, "#ff8c00");
        styliserBoutonAction(btnUrgent, "#ffa500");
        styliserBoutonAction(btnRapport, "#ff7800");
        
        buttonsPanel.getChildren().addAll(btnRapport, btnNouveauSinistre, btnTraiter, btnUrgent);
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerPanel.getChildren().addAll(titleLabel, buttonsPanel);
        
        // Contenu principal
        HBox contentPanel = new HBox(20);
        
        // Liste des sinistres
        VBox sinistresPanel = new VBox();
        sinistresPanel.setPadding(new Insets(15));
        sinistresPanel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label sinistresLabel = new Label("Liste des sinistres en cours...");
        sinistresLabel.setFont(Font.font("Segoe UI", 14));
        sinistresLabel.setTextFill(Color.web("#6c757d"));
        sinistresLabel.setAlignment(Pos.CENTER);
        
        sinistresPanel.getChildren().add(sinistresLabel);
        
        // Détails du sinistre
        VBox detailsPanel = new VBox();
        detailsPanel.setPadding(new Insets(15));
        detailsPanel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label detailsLabel = new Label("Sélectionnez un sinistre pour voir les détails...");
        detailsLabel.setFont(Font.font("Segoe UI", 14));
        detailsLabel.setTextFill(Color.web("#6c757d"));
        detailsLabel.setAlignment(Pos.CENTER);
        
        detailsPanel.getChildren().add(detailsLabel);
        
        contentPanel.getChildren().addAll(sinistresPanel, detailsPanel);
        HBox.setHgrow(sinistresPanel, Priority.ALWAYS);
        HBox.setHgrow(detailsPanel, Priority.ALWAYS);
        
        panel.getChildren().addAll(headerPanel, contentPanel);
        return panel;
    }
    
    private VBox creerOngletIndemnisation() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label("💰 Gestion des Indemnisations");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setAlignment(Pos.CENTER);
        
        // Cartes d'indemnisation
        GridPane cardsPanel = new GridPane();
        cardsPanel.setHgap(15);
        cardsPanel.setVgap(15);
        cardsPanel.setPadding(new Insets(0, 50, 0, 50));
        
        VBox card1 = creerCarte("💰 Montant total indemnisé", "450,000 €", Color.web("#ff8c00"));
        VBox card2 = creerCarte("📋 Dossiers indemnisés", "28", Color.web("#ffa500"));
        VBox card3 = creerCarte("⏳ En attente paiement", "12", Color.web("#ff7800"));
        VBox card4 = creerCarte("📈 Moyenne par dossier", "16,071 €", Color.web("#ff6400"));
        
        cardsPanel.add(card1, 0, 0);
        cardsPanel.add(card2, 1, 0);
        cardsPanel.add(card3, 0, 1);
        cardsPanel.add(card4, 1, 1);
        
        // Boutons d'action
        HBox actionsPanel = new HBox(15);
        actionsPanel.setAlignment(Pos.CENTER);
        actionsPanel.setPadding(new Insets(30, 0, 0, 0));
        
        JFXButton btnCalculer = new JFXButton("🧮 Calculer Indemnité");
        JFXButton btnPayer = new JFXButton("💳 Payer Indemnité");
        JFXButton btnHistorique = new JFXButton("📋 Historique Paiements");
        
        styliserBoutonAction(btnCalculer, "#ff8c00");
        styliserBoutonAction(btnPayer, "#ffa500");
        styliserBoutonAction(btnHistorique, "#ff7800");
        
        actionsPanel.getChildren().addAll(btnCalculer, btnPayer, btnHistorique);
        
        panel.getChildren().addAll(titleLabel, cardsPanel, actionsPanel);
        return panel;
    }
    
    private VBox creerOngletAdministrateur() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label("⚙️ Panel Administrateur");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setAlignment(Pos.CENTER);
        
        // Options administratives
        GridPane optionsPanel = new GridPane();
        optionsPanel.setHgap(15);
        optionsPanel.setVgap(15);
        optionsPanel.setPadding(new Insets(0, 50, 0, 50));
        
        // Création des options administratives
        String[] options = {
            "👥 Gestion Utilisateurs", "📊 Statistiques Avancées", 
            "🔧 Configuration Système", "📁 Sauvegarde Données",
            "📋 Logs et Audit", "🛡️ Sécurité"
        };
        
        String[] couleursOptions = {
            "#ff8c00", "#ffa500", "#ff7800", "#ff6400", "#ffb400", "#ff4500"
        };
        
        for (int i = 0; i < options.length; i++) {
            VBox optionCard = creerCarteOption(options[i], couleursOptions[i]);
            optionsPanel.add(optionCard, i % 2, i / 2);
        }
        
        panel.getChildren().addAll(titleLabel, optionsPanel);
        return panel;
    }
    
    private VBox creerOngletStatistique() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label("📈 Statistiques Détaillées");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setAlignment(Pos.CENTER);
        
        // Conteneur principal pour les graphiques
        GridPane statsContainer = new GridPane();
        statsContainer.setHgap(20);
        statsContainer.setVgap(20);
        statsContainer.setPadding(new Insets(0, 20, 0, 20));
        
        // Graphiques
        VBox chart1 = creerPanelGraphique("📊 Répartition des Sinistres", 
            "• Accident: 45%\n• Vol: 25%\n• Incendie: 15%\n• Autre: 15%", 
            "#ff8c00");
        
        VBox chart2 = creerPanelGraphique("📈 Évolution Mensuelle", 
            "• Jan: 12\n• Fév: 18\n• Mar: 15\n• Avr: 22\n• Mai: 25", 
            "#ffa500");
        
        VBox chart3 = creerPanelGraphique("⚡ Taux de Traitement", 
            "• Traités: 78%\n• En cours: 15%\n• En attente: 7%", 
            "#ff7800");
        
        VBox chart4 = creerPanelGraphique("💰 Indemnisations par Type", 
            "• Accident: 60%\n• Vol: 25%\n• Incendie: 10%\n• Autre: 5%", 
            "#ff6400");
        
        statsContainer.add(chart1, 0, 0);
        statsContainer.add(chart2, 1, 0);
        statsContainer.add(chart3, 0, 1);
        statsContainer.add(chart4, 1, 1);
        
        // Filtres et export
        HBox controlsPanel = new HBox(15);
        controlsPanel.setAlignment(Pos.CENTER);
        controlsPanel.setPadding(new Insets(20, 0, 0, 0));
        
        ComboBox<String> comboPeriode = new ComboBox<>();
        comboPeriode.getItems().addAll("7 derniers jours", "30 derniers jours", "3 derniers mois", "Année en cours");
        comboPeriode.setValue("30 derniers jours");
        
        JFXButton btnGenererRapport = new JFXButton("📄 Générer Rapport");
        JFXButton btnExporterExcel = new JFXButton("📤 Exporter Excel");
        
        styliserBoutonAction(btnGenererRapport, "#ff8c00");
        styliserBoutonAction(btnExporterExcel, "#ffa500");
        
        controlsPanel.getChildren().addAll(
            new Label("Période:"), comboPeriode, btnGenererRapport, btnExporterExcel
        );
        
        panel.getChildren().addAll(titleLabel, statsContainer, controlsPanel);
        return panel;
    }
    
    private VBox creerPanelGraphique(String titre, String donnees, String couleur) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label lblTitre = new Label(titre);
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitre.setTextFill(Color.web(couleur));
        lblTitre.setAlignment(Pos.CENTER);
        
        TextArea txtDonnees = new TextArea(donnees);
        txtDonnees.setFont(Font.font("Segoe UI", 12));
        txtDonnees.setEditable(false);
        txtDonnees.setStyle("-fx-background-color: #f8f9fa;");
        
        panel.getChildren().addAll(lblTitre, txtDonnees);
        return panel;
    }
    
    private VBox creerCarteOption(String titre, String couleur) {
        VBox carte = new VBox();
        carte.setPadding(new Insets(20));
        carte.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        carte.setCursor(javafx.scene.Cursor.HAND);
        
        Label lblTitre = new Label(titre);
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitre.setTextFill(Color.web(couleur));
        lblTitre.setAlignment(Pos.CENTER);
        
        // Effet de survol
        carte.setOnMouseEntered(e -> carte.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;"));
        carte.setOnMouseExited(e -> carte.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;"));
        
        carte.getChildren().add(lblTitre);
        return carte;
    }
    
    private HBox creerFooterPanel() {
        HBox footerPanel = new HBox();
        footerPanel.setStyle("-fx-background-color: #ff8c00;");
        footerPanel.setPadding(new Insets(10));
        footerPanel.setAlignment(Pos.CENTER);
        
        Label footerLabel = new Label("🔒 MATCA-LISA v2.0 - © 2026");
        footerLabel.setFont(Font.font("Segoe UI", 12));
        footerLabel.setTextFill(Color.WHITE);
        
        footerPanel.getChildren().add(footerLabel);
        return footerPanel;
    }
    
    // Méthodes de style
    private void styliserBoutonHeader(JFXButton bouton) {
        bouton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        bouton.setPrefSize(120, 40);
    }
    
    private void styliserBoutonAction(JFXButton bouton, String couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 3;",
            couleur
        ));
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 3;",
            couleur, couleur
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 3;",
            couleur
        )));
        
        // Action par défaut
        bouton.setOnAction(e -> showAlert("Information", 
            "Fonctionnalité '" + bouton.getText() + "' en développement.\n" +
            "Cette option sera disponible dans la prochaine version.", 
            Alert.AlertType.INFORMATION));
    }
    
    private void styliserBoutonActionRapide(JFXButton bouton, String couleur) {
        bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 3; -fx-font-size: 14px;",
            couleur
        ));
        bouton.setPrefSize(180, 50);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 3; -fx-font-size: 14px;",
            couleur, couleur
        )));
        
        bouton.setOnMouseExited(e -> bouton.setStyle(String.format(
            "-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; " +
            "-fx-border-color: %s; -fx-border-width: 3; -fx-font-size: 14px;",
            couleur
        )));
        
        bouton.setOnAction(e -> showAlert("Information", 
            "Fonctionnalité '" + bouton.getText() + "' en développement.\n" +
            "Cette option sera disponible dans la prochaine version.", 
            Alert.AlertType.INFORMATION));
    }
    
    private void configurerComposants() {
        SessionUtilisateur session = SessionUtilisateur.getInstance();
        System.out.println("🚀 Interface principale initialisée pour: " + session.getResume());
    }
    
    private void deconnecter() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionUtilisateur.getInstance().deconnecter();
                primaryStage.close();
                
                // Retour à l'authentification
                Platform.runLater(() -> {
                    Stage authStage = new Stage();
                    Authentification auth = new Authentification();
                    auth.start(authStage);
                });
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
    
    // Méthodes pour la gestion des personnes sélectionnées
    public void setPersonneSelectionnee(String matricule, String nom, String prenom) {
        this.personneSelectionnee_Matricule = matricule;
        this.personneSelectionnee_Nom = nom;
        this.personneSelectionnee_Prenom = prenom;
        
        showAlert("Personne sélectionnée", 
            "✅ Personne sélectionnée avec succès !\n\n" +
            "📋 Informations reçues :\n" +
            "• Matricule: " + matricule + "\n" +
            "• Nom: " + nom + "\n" +
            "• Prénom: " + prenom + "\n\n" +
            "Ces informations peuvent maintenant être utilisées pour les opérations suivantes.",
            Alert.AlertType.INFORMATION);
        
        System.out.println("Personne sélectionnée - Matricule: " + matricule + ", Nom: " + nom + ", Prénom: " + prenom);
    }
    
    public String[] getPersonneSelectionnee() {
        if (personneSelectionnee_Matricule != null) {
            return new String[] {
                personneSelectionnee_Matricule,
                personneSelectionnee_Nom,
                personneSelectionnee_Prenom
            };
        }
        return null;
    }
    
    public boolean isPersonneSelectionnee() {
        return personneSelectionnee_Matricule != null;
    }
    
    public void clearPersonneSelectionnee() {
        this.personneSelectionnee_Matricule = null;
        this.personneSelectionnee_Nom = null;
        this.personneSelectionnee_Prenom = null;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}