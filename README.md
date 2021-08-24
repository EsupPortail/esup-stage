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

~~~shell
[user@pc ~/git/eStage]$ mvn spring-boot:run
...
~~~
