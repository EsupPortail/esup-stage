# eStage

Produit de remplacant "pstage" mais je ne sais pas ce que ça fait exactement

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

## Procédure d'installation en environnement de dev

* ajouter un fichier src/main/resources/estage.properties basé sur le fichier src/main/resources/estage-example.properties : les mots de passe seront à demander au chef de projet
* dans ce fichier, paramétrer la variable `appli.admin_technique` en ajoutant votre login cas (les logins sont séparés par des ;)
* lancer le serveur avec une commande maven `-Pdev clean package cargo:run` la première fois. Pour réduire le temps d'attente lors des développements, ajouter à cette commande l'option ` -Dskip.npm=true` qui permet de ne pas générer le build angular du frontend.
* pour lancer le frontend dev, lancer la commande `ng serve` au niveau du dossier frontend (node devra être installé)
* lancer chrome avec les options `--disable-web-security --user-data-dir=chemin d'un dossier` pour désactiver le CORS
* aller sur l'application à l'adresse http://localhost.dauphine.fr:8080/frontend/#/ et copier le cookie `idsToken`
* aller sur le frontend dev sur http://localhost:4200 et y recopier le cookie. L'application devrait être accessible après rafraichissement de la page

