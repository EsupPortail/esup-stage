# ESUP Stage

ESUP STAGE est la refonte de l'application pStage. L'application a été complètement réécrite pour reprendre et améliorer les grandes et fonctionnalités : produire une convention de stage et ses avenants dans le cadre d'un processus de validation adapté. 

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

* ajouter un fichier src/main/resources/estage.properties basé sur le fichier src/main/resources/estage-example.properties 

* dans ce fichier, paramétrer la variable `appli.admin_technique` en ajoutant votre login cas (les logins sont séparés par des ;)
* lancer le serveur avec une commande maven `-Pdev clean package cargo:run` la première fois. Pour réduire le temps d'attente lors des développements, ajouter à cette commande l'option ` -Dskip.npm=true` qui permet de ne pas générer le build angular du frontend.
* pour lancer le frontend dev :
  * sans Docker : lancer la commande `ng serve --host localhost.dauphine.fr` au niveau du dossier frontend (node et npm devront être installés)
  * avec Docker :
    * se positionner au niveau du dossier `frontend`
    * lancer les commandes suivantes pour initier et installer les nodes modules :
      * `docker-compose build`
      * `docker-compose run --rm --entrypoint=npm frontend ci`
    * lancer la commande suivante pour lancer le fontend en dev : `docker-compose up -d`
* lancer chrome avec les options `--disable-web-security --user-data-dir=chemin d'un dossier` pour désactiver le CORS
* aller sur l'application à l'adresse http://localhost.dauphine.fr:8080/frontend/#/ et se connecter une première fois
* ouvrir un nouvel onglet sur http://localhost.dauphine.fr:8700 pour accéder à l'application en mode angular dev

## Procédure d'installation

https://github.com/EsupPortail/esup-stage/wiki

## Installation de ckeditor5

* aller sur le site https://ckeditor.com/ckeditor-5/online-builder/ pour générer ckeditor5 avec des plugins personnalisés
* choisir l'éditeur "Classic" (le plugin "Source code" ne fonctionne actuellement qu'avec l'éditeur Classic)
* enlever les plugins nécessitants une license PRO
* ci-dessous la liste des plugins actuelle de l'application (* : obligatoire pour le bon fonctionnement minimal) :
  * Alignement *
  * Autoformat
  * Base64 upload adapter *
  * Block quote *
  * Bold *
  * Find and replace *
  * Font background color *
  * Font color * 
  * Font family *
  * Font size *
  * Heading *
  * Highlight
  * Horizontal line
  * Image *
  * Image caption *
  * Image resize *
  * Image style *
  * Image toolbar *
  * Image upload *
  * Indent *
  * Indent block *
  * Italic *
  * Link
  * List *
  * List properties *
  * Media embed
  * Page break *
  * Paste front Office
  * Remove format *
  * Source editing *
  * Strikethrough *
  * Table *
  * Table cell properties *
  * Table column resize *
  * Table properties *
  * Table toolbar *
  * Text transformation *
  * To-do list
  * Underline *
* à l'étape suivante, disposer comme voulu les éléments de la barre d'outils de l'éditeur
* une fois terminé, choisir la langue française et télécharger la librairie
* supprimer tout le contenu de `src/frontend/src/custom-ck5` pour y mettre celui du dossier `build/` de la librairie téléchargée
* mettre à jour ou installer si nécessaire la librairie `@ckeditor/ckeditor5-angular` et les builds `@ckeditor/ckeditor5-build-classic` ou autre correspondant au type d'éditeur choisi à la 1ère étape
