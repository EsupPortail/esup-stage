# eStage

Produit de remplacant "pstage"

## Techno

* JAVA 11
* NodeJS 14


## Integration

* Build war

~~~shell
[user@pc ~/git/eStage]$ mvn clean package
...
[user@pc ~/git/eStage]$ ls target/*.war
target/ROOT.war
[user@pc ~/git/eStage]$ 
~~~

* Execution devel

Cette execution passe par le fichier de configuration "src/main/resources-filtered/application.properties"

~~~shell
[user@pc ~/git/eStage]$ mvn -Pdev clean package cargo:run
...
~~~

## Pré-requis

* la variable appli.data_dir dans estage.properties pointe sur le dossier d'upload du projet. Ce dossier doit contenir la structure suivante :
  * `centregestion`
    * `consigne-documents`
    * `logos`

Par exemple si `appli.data_dir=/etc/eStage/uploads` on aura :
```
/etc
|_/eStage
   |_/uploads
     |_/centregestion
       |_/consigne-documents
       |_/logos
```

## Procédure d'installation en environnement de dev

* ajouter un fichier src/main/resources/estage.properties basé sur le fichier src/main/resources/estage-example.properties : les mots de passe seront à demander au chef de projet
* dans ce fichier, paramétrer la variable `appli.admin_technique` en ajoutant votre login cas (les logins sont séparés par des ;)
* lancer le serveur avec une commande maven `-Pdev clean package cargo:run` la première fois. Pour réduire le temps d'attente lors des développements, ajouter à cette commande l'option ` -Dskip.npm=true` qui permet de ne pas générer le build angular du frontend.
* pour lancer le frontend dev, lancer la commande `ng serve --host localhost.dauphine.fr` au niveau du dossier frontend (node devra être installé)
* lancer chrome avec les options `--disable-web-security --user-data-dir=chemin d'un dossier` pour désactiver le CORS
* aller sur l'application à l'adresse http://localhost.dauphine.fr:8080/frontend/#/ et se connecter une première fois
* aller dans un nouvel onglet sur le frontend dev sur http://localhost.dauphine.fr:4200. L'application devrait être accessible.

De plus à chaque fois que le serveur java est redémarré, il faudra recharger la page sur http://localhost.dauphine.fr:8080/ pour recharger le JSESSIONID puis recharger sur localhost.dauphine.fr:4200/#/ pour mettre à jour le cookie.

