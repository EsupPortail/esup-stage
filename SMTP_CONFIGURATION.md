# ESUP-Stage - Configuration SMTP

## Choix entre SSL/TLS et STARTTLS

L'application supporte deux modes de sécurisation des emails :

### Mode 1 : STARTTLS
- **Port standard** : 587
- **Protocole** : smtp
- **Usage** : Standard moderne, largement supporté

**Configuration :**
```properties
appli.mailer.protocol=smtp
appli.mailer.host=smtp.votre-universite.fr
appli.mailer.port=587
appli.mailer.auth=true
appli.mailer.username=votre-compte
appli.mailer.password=votre-mot-de-passe
appli.mailer.from=nepasrepondre@votre-universite.fr
appli.mailer.starttls.enable=true
appli.mailer.starttls.required=true
```

### Mode 2 : SSL/TLS Direct
- **Port standard** : 465
- **Protocole** : smtps
- **Usage** : Méthode traditionnelle

**Configuration :**
```properties
appli.mailer.protocol=smtps
appli.mailer.host=smtp.votre-universite.fr
appli.mailer.port=465
appli.mailer.auth=true
appli.mailer.username=votre-compte
appli.mailer.password=votre-mot-de-passe
appli.mailer.from=nepasrepondre@votre-universite.fr
appli.mailer.ssl.enable=true
```

### Mode 3 : Sans chiffrement (Dev/Test uniquement)
- **Port standard** : 25 ou 1025
- **Protocole** : smtp
- **Usage** : **UNIQUEMENT pour environnements de développement/test locaux**

**Configuration :**
```properties
appli.mailer.protocol=smtp
appli.mailer.host=localhost
appli.mailer.port=1025
appli.mailer.auth=false
appli.mailer.from=nepasrepondre@test.local
# Pas de ssl.enable ni starttls.enable
```

**ATTENTION :**
- **Ne JAMAIS utiliser en production** - les emails transitent en clair
- Utile avec des outils comme [MailHog](https://github.com/mailhog/MailHog) ou [MailCatcher](https://mailcatcher.me/) pour intercepter les emails en développement
- Aucune sécurité - données non chiffrées

## Erreurs courantes

### Erreur : "Must issue a STARTTLS command first"
**Cause** : Configuration mixte SSL + port 587
**Solution** : Utilisez STARTTLS sur port 587 (mode 1) OU SSL sur port 465 (mode 2), mais pas les deux

### Erreur : "Connection refused"
**Cause** : Port bloqué par le pare-feu
**Solution** : Vérifiez que le port est ouvert (587 ou 465 selon votre config)

### Erreur : "Authentication failed"
**Cause** : Identifiants incorrects
**Solution** : Vérifiez username et password

## Test de configuration

Après modification, testez l'envoi d'email via l'interface d'administration.
