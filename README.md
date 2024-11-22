# ESUP Stage

ESUP STAGE est la refonte de l'application pStage. L'application a été complètement réécrite pour reprendre et améliorer les grandes et fonctionnalités : produire une convention de stage et ses avenants dans le cadre d'un processus de validation adapté. 

## Techno

* JAVA 21
* NodeJS 20


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
  * `images`
  * `signatures`

Par exemple si `appli.data_dir=/etc/eStage/uploads` on aura :
```
/etc
|_/eStage
   |_/uploads
     |_/centregestion
       |_/consigne-documents
       |_/logos
     |_/images
     |_/signatures
```

## CAS

Par défaut, l'application attend du serveur CAS une réponse au format JSON. Si le serveur CAS répond au format XML,
il faut ajouter la ligne suivante au fichier `estage.properties` :
```properties
cas.response_type=xml
```

## Signature électronique (optionnel)

La signature électronique est activée si au moins une des configuration ci-dessous est paramétrée. Si plusieurs solutions configurées, Docaposte prendra le dessus.

### Docaposte

> **ATTENTION :** Les paramètres ne doivent pas être ajoutés s'ils ne sont pas utilisés

Les paramètres Docaposte se trouve dans le fichier `estage.properties` :
```properties
# docaposte
docaposte.uri=https://demo-parapheur.dfast.fr/parapheur-soap/soap/v1/Documents
docaposte.siren=xxx
docaposte.keystore.path=<chemin du fichier .p12>
docaposte.keystore.password=xxx
docaposte.truststore.path=<chemin du fichier .jks>
docaposte.truststore.password=xxx
```
Les certificats pour Docaposte peuvent être déposés où vous le souhaitez en dehors du projet.

### ESUP-Signature (mode solution externe)

> **ATTENTION :** Les paramètres ne doivent pas être ajoutés s'ils ne sont pas utilisés

ESUP-Stage met à disposition des api "public" accessible avec un des tokens paramétrés dans `appli.public.tokens`. La liste des api se trouve dans `/public/swagger-ui.html`.

Le paramétrage "webhook" correspondent à l'appel vers une api externe pour la signature électronique (cette solution est à mettre en place par chaque établissement). Les api vers ESUP-Signature sont intégrés dans ESUP-Stage mais la configuration est la même pour une solution externe.

`webhook.signature.uri` : uri de l'api externe\
`webhook.signature.token` : token d'accès

```properties
# tokens d'accès d'esup-stage permettant d'autoriser les webhook à accéder aux api /public/api d'esup-stage séparés par des ; (exemple : token1;token2;token3)
appli.public.tokens=xxxx

### Paramétrage webhooks ###
# uri du webhook de signature
webhook.signature.uri=http://localhost:8080/webhook/esup-signature
# token permettant d'accéder au webhook signature
webhook.signature.token=yyyyy
### -------------------- ###

### Paramétrage esup-signature ###
# numero du circuit
esupsignature.uri=http://localhost:8880/ws
esupsignature.circuit=123
### -------------------- ###
```

## Procédure d'installation en environnement de dev

* ajouter un fichier src/main/resources/estage.properties basé sur le fichier src/main/resources/estage-example.properties 

* dans ce fichier, paramétrer la variable `appli.admin_technique` en ajoutant votre login cas (les logins sont séparés par des ;)
* lancer la génération des classes java pour le client Docaposte avec la commande maven `-Pdev jaxb2:generate` : les classes sont générées dans `org/esup_portail/esup_stage/docaposte/gen`
* lancer le serveur avec une commande maven `-Pdev clean package cargo:run`. Le profil `dev` permet de désactiver l'installation de node, npm et du build angular. Si besoin, il suffit de commenter la partie `execution` se trouvant au niveau du profil `dev`.
* pour lancer le frontend dev :
  * sans Docker : lancer la commande `ng serve --host localhost.dauphine.fr --proxy-config src/proxy.conf.json` au niveau du dossier frontend (node et npm devront être installés)
  * avec Docker :
    * se positionner au niveau du dossier `frontend`
    * lancer les commandes suivantes pour initier et installer les nodes modules :
      * `docker-compose build`
      * `docker-compose run --rm --entrypoint=npm frontend ci`
    * lancer la commande suivante pour lancer le fontend en dev : `docker-compose up -d`
* aller sur l'application à l'adresse http://localhost.dauphine.fr:8080/frontend/#/ et se connecter une première fois
* ouvrir un nouvel onglet sur http://localhost.dauphine.fr:8700 (ou http://localhost.dauphine.fr:4200) pour accéder à l'application en mode angular dev
* pour se déconnecter, aller sur http://localhost.dauphine.fr:4200/logout

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
