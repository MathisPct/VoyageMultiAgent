## Le projet

### Présentation générale du projet

![](/doc/0.png)

Ce projet comporte deux types d’agents : les fournisseurs et les acheteurs, chaque agent a des stratégies de négociation spécifiques et interagit avec les autres agents via un système de messagerie.

**Fournisseurs (Providers)**

* Les fournisseurs possèdent des billets de voyage qu'ils souhaitent vendre.  
* Chaque fournisseur a une liste de billets avec des informations telles que le prix, la destination, la compagnie aérienne, et la quantité disponible.  
* Les fournisseurs utilisent une stratégie de négociation pour déterminer les offres initiales et les contre-offres qu'ils feront aux acheteurs.


**Acheteurs (Buyers)**

* Les acheteurs cherchent à acheter des billets de voyage en fonction de leurs contraintes (budget maximum, compagnies aériennes autorisées, destinations souhaitées, etc.)  
* Chaque acheteur a une stratégie de négociation pour déterminer les offres initiales et les contre-offres qu'ils feront aux fournisseurs.  
* Les acheteurs peuvent former des coalitions pour acheter des billets en groupe, ce qui peut influencer leur pouvoir de négociation.

### Structure du projet   
Le projet est organisé en plusieurs sous-systèmes interconnectés :

#### Sous-systèmes

1. **Core System**  
     
   - Gère les agents principaux (Buyer, Provider)  
   - Définit les entités de base comme Ticket  
   - Permet l'interaction entre les agents

   

2. **Messaging System**  
     
   - Implémente la communication entre agents  
   - Gère l'historique des messages  
   - Garantit la transmission asynchrone des offres

   

3. **Coalition System**  
     
   - Gère la formation et l'organisation des coalitions  
   - Définit les contraintes des acheteurs  
   - Implémente les stratégies de formation des groupes

   

4. **Strategy System**  
     
   - Définit les différentes stratégies de négociation  
   - Permet l'extension facile avec de nouvelles stratégies  
   - Sépare la logique de négociation du reste du système

## La négociation

![](/doc/1.png)

La négociation entre les agents se déroule en deux phases principales :

**Phase 1 : Négociation Initiale**  
![](/doc/2.png)

* Offre Initiale : L'acheteur envoie une offre initiale au fournisseur pour un billet spécifique.  
* Contre-offre : Le fournisseur peut accepter l'offre, faire une contre-offre, ou rejeter l'offre.  
* Acceptation Préliminaire : Si les deux parties parviennent à un accord préliminaire, elles passent à la phase suivante.

**Phase 2 : Confirmation d'Achat**

* Demande de confirmation : L'acheteur demande la confirmation de l'achat du billet.  
* Réponse du Fournisseur : Le fournisseur peut accepter ou rejeter la demande de confirmation d'achat.  
* Finalisation : Si la demande est acceptée, la transaction est finalisée et le billet est vendu à l'acheteur.

![](/doc/3.png)
![](/doc/4.png)


## Stratégies

![](/doc/5.png)

### La stratégie naïve : 

**La stratégie naïve** est plutôt simple car pour l’acheteur on propose d’abord 60% du budget de l’acheteur en augmentant progressivement sans dépasser le budget maximum. Pour les fournisseurs, on baisse les prix de 10% à chaque contre-offre et en l’acceptant dès qu’elle atteint 90% du prix de base. 

### La stratégie basée sur l’intérêt : 

Nous avons mis en place un critère sur les agents qui est l’intérêt. Il représente l’envie de l’agent à acheter ou à vendre son ticket, plus son envie est grande, plus il va augmenter les prix pour les acheteurs, et diminuer le prix pour les fournisseurs. A l’inverse, si l’intérêt est bas,  le fournisseur va avoir tendance à ne pas trop baisser le prix et l’acheteur aura tendance à chercher une bonne affaire si son envie est basse, donc à ne pas augmenter le prix lors de la négociation. 

#### Fournisseur

Le prix proposé dans l’offre initiale suit la formule suivante : 

Offre Initiale=P×(1.20−0.015×(x−1))

où P est le prix de base du billet  
et x est l’intérêt de l’agent

Cela permet au fournisseur de fournir une offre initiale plus chère si son intérêt est faible.  
Ensuite, dans les contre offres, le prix augmente du % de l’intérêt. Si l’intérêt est de 10, alors le prix proposé par l’acheteur sera de 10% en plus.   
![](/doc/6.png)

Les contre-offre sont aussi basés sur l’intérêt

Contre offre=max(P × (1.0-0.01 × x), prixMin(billet))

où P est le prix de l’offre proposée par l’acheteur  
et x est l’intérêt de l’agent  
prixMin(billet) est le prix minimum du billet défini

![](/doc/7.png) 
En sachant que le fournisseur ne peut pas descendre en-dessous du prix minimum fixé pour le billet

#### Acheteur

Le prix proposé dans l’offre initiale suit la formule suivante : 

Offre Initiale=P×(0.25−0.055 × x)

où P est le prix de base du billet  
et x est l’intérêt de l’agent

![](/doc/8.png)

Les contre-offre sont aussi basés sur l’intérêt

Contre offre=P × (1.0+0.03 × x)

où P est le prix de l’offre proposée par le fournisseur  
et x est l’intérêt de l’agent

![](/doc/9.png)

### Mode Compétitif

En mode compétitif, chaque acheteur agit de manière indépendante dans le système :

#### Caractéristiques

- Négociation individuelle avec les fournisseurs  
- Pas de partage d'information entre acheteurs  
- Budget et contraintes individuels  
- Un acheteur \= une transaction \= un billet

#### Stratégie de Négociation

- Basée uniquement sur les contraintes personnelles  
- Prise en compte de l'intérêt individuel (1-10)  
- Augmentation progressive des offres en fonction du budget

### Mode Coopératif

La formation de coalitions permet aux acheteurs ayant des intérêts communs de se regrouper pour négocier ensemble.

#### Formation des Coalitions

- Les coalitions sont formées en fonction de critères communs :  
  - Destinations communes  
  - Compagnies aériennes autorisées communes  
  - Budgets compatibles  
- Taille maximale d'une coalition : on a décidé de fixer à 3 acheteurs  
- Un représentant est choisi pour chaque coalition (celui ayant le plus grand budget)

#### Stratégie de Formation des Coalitions

La stratégie de formation (`CoalitionFormationStrategy`) utilise plusieurs critères :

1. **Compatibilité** :  
     
   - Vérification des destinations communes  
   - Vérification des compagnies aériennes autorisées  
   - Analyse des budgets

   

2. **Valeur de Coalition** :  
     
   - Calculée en fonction du nombre de membres  
   - Prend en compte le budget commun  
   - Utilise une fonction logarithmique pour équilibrer la taille et le budget

   

3. **Processus de Négociation** :  
     
   - Le représentant négocie pour l'ensemble du groupe  
   - Achète le nombre exact de billets nécessaires pour la coalition  
   - Vérifie la disponibilité des billets pour tout le groupe, c'est-à-dire si le nombre de billets demandé est disponible

#### Architecture

##### Diagramme de séquence

![](/doc/10.png)

##### Implémentation Technique

Le système utilise plusieurs classes clés :

- `Coalition` : Gestion des membres et calcul du budget commun  
- `CoalitionFormationStrategy` : Algorithme de formation des coalitions  
- `Buyer` : Adaptation pour supporter les achats en groupe  
- `Provider` : Gestion des ventes multiples pour les coalitions

## Interface  

Nous avons mis en place une interface pour que cela soit beaucoup plus lisible et compréhensible pour l’utilisateur, voici quelques images :

### Gestion des agents

Pour gérer les agents: en créer, en supprimer, les modifer  
![](/doc/11.png)

### Interface de négociation

A gauche on voit les conversations qu’il y a eu entre tel acheteur et tel fournisseur. On a la possibilité de lancer la négociation en mode “Compétitif” ou “Coopératif”. On peut aussi relancer une négociation  
![](/doc/1.png)

### Visualisation des billets

On a un troisième onglet qui nous permet de voir l’ensemble des billets fournis par tous les agents  
![](/doc/12.png)