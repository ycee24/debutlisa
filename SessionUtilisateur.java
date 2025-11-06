package lisa1connexion;

public class SessionUtilisateur {
    private static SessionUtilisateur instance;
    
    private String nom;
    private String matricule;
    private String agence;
    private String email;
    
    // Paramètres de connexion à la base de données
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    
    // Constructeur privé pour le pattern Singleton
    private SessionUtilisateur() {
        // Valeurs par défaut pour la connexion à la base de données
        this.databaseUrl = "jdbc:oracle:thin:@192.168.10.32:1521:bdmatca";
        this.databaseUser = "soft";
        this.databasePassword = "part";
    }
    
    // Méthode pour obtenir l'instance unique (version thread-safe)
    public static SessionUtilisateur getInstance() {
        if (instance == null) {
            synchronized (SessionUtilisateur.class) {
                if (instance == null) {
                    instance = new SessionUtilisateur();
                }
            }
        }
        return instance;
    }
    
    // Méthode pour initialiser la session
    public void initialiserSession(String nom, String matricule, String agence, String email) {
        this.nom = nom;
        this.matricule = matricule;
        this.agence = agence;
        this.email = email;
        
        System.out.println("✅ Session initialisée pour: " + nom + " (" + matricule + ")");
        System.out.println("🏢 Agence: " + agence);
        System.out.println("📧 Email: " + email);
    }
    
    // Méthode pour vider la session (déconnexion)
    public void deconnecter() {
        this.nom = null;
        this.matricule = null;
        this.agence = null;
        this.email = null;
        System.out.println("🚪 Session déconnectée");
    }
    
    // Getters pour les informations utilisateur
    public String getNom() {
        return nom != null ? nom : "Non connecté";
    }
    
    public String getMatricule() {
        return matricule != null ? matricule : "N/A";
    }
    
    public String getAgence() {
        return agence != null ? agence : "N/A";
    }
    
    public String getEmail() {
        return email != null ? email : "N/A";
    }
    
    // Getters pour les valeurs brutes (pour usage interne)
    public String getNomBrut() {
        return nom;
    }
    
    public String getMatriculeBrut() {
        return matricule;
    }
    
    public String getAgenceBrut() {
        return agence;
    }
    
    public String getEmailBrut() {
        return email;
    }
    
    // Getters pour la connexion à la base de données
    public String getDatabaseUrl() {
        return databaseUrl;
    }
    
    public String getDatabaseUser() {
        return databaseUser;
    }
    
    public String getDatabasePassword() {
        return databasePassword;
    }
    
    // Setters pour la connexion à la base de données (optionnels)
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    
    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }
    
    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }
    
    // Méthode pour configurer la connexion à la base de données
    public void configurerConnexionBDD(String url, String user, String password) {
        this.databaseUrl = url;
        this.databaseUser = user;
        this.databasePassword = password;
        System.out.println("✅ Configuration BDD mise à jour");
    }
    
    // Vérifier si un utilisateur est connecté
    public boolean estConnecte() {
        return nom != null && matricule != null;
    }
    
    // Obtenir un résumé des informations
    public String getResume() {
        return String.format("👤 %s | 🆔 %s | 🏢 %s", getNom(), getMatricule(), getAgence());
    }
    
    // Obtenir un résumé de la configuration BDD (pour debug)
    public String getResumeBDD() {
        return String.format("🗄️ URL: %s | 👤 User: %s", databaseUrl, databaseUser);
    }
    
    // Méthode pour valider que la session est correctement initialisée
    public boolean estValide() {
        return estConnecte() && 
               nom != null && !nom.trim().isEmpty() &&
               matricule != null && !matricule.trim().isEmpty() &&
               agence != null && !agence.trim().isEmpty();
    }
    
    // Méthode pour obtenir les informations de débogage
    public String getDebugInfo() {
        return String.format(
            "SessionUtilisateur [nom=%s, matricule=%s, agence=%s, email=%s, connecté=%s]",
            nom, matricule, agence, email, estConnecte()
        );
    }
}