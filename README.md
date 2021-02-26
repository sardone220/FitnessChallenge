fitnessChallenge
Documento riassuntivo:

Questa è un'applicazione rivolta ad un'utenza di sportivi professionisti piuttosto che amatoriali. L'obiettivo principale dell'applicazione è il monitoraggio degli allenamenti degli utenti sia che essi siano effettuati all'aperto sia all'interno di strutture sportive.
Abbiamo definito 3 categorie di utenti:
1-Trainer 
2-User
3-Trainer/User
1. L'User ha la possibilità di scegliere tra allenamento all'aperto o in una specifica struttura;
Scegliendo l'allenamento all'aperto l'utente può impostare la propria scheda di allenamento o modificarla, 
visionare schede precedenti e verificare le statistiche dei propri allenamenti.
Scegliendo invece l'allenamento presso una struttura sportiva l'utente è invitato ad identificarsi attraverso un sensore NFC, 
di cui la struttura dovrebbe essere dotata. In questo caso l'utente può solo prendere visione delle proprie schede 
di allenamento o di verificare le statistiche. 
Il compito di aggiungere/modificare/eliminare le schede di alllenamento spetta al Trainer.
In entrambi i casi, avviata la scheda di allenamento, l'utente può visionare, esercizio per esercizio, 
tutte le informazioni specifiche dell'esercizio che sta eseguendo mentre un timer monitora il tempo di recupero.

2. La figura del Trainer si identifica attraverso il login. Ad accesso effettuato, il Trainer può monitorare 
i progressi degli utenti oppure, come già detto, può gestire le schede di allenamento degli utenti
3. L'utente registratosi come Trainer/User è un particolare tipo di utente che può accedere sia alle funzionalità 
del punto 1 sia a quelle del punto 2.

Abbiamo usato le seguenti librerie esterne:
- https://github.com/Clans/FloatingActionButton -> alcune funzioni per implementare un FAB menu
- https://bintray.com/lopspower/maven/com.mikhaellopez:circularimageview -> per la gestione delle immagini circolari
- https://bintray.com/yshrsmz/maven/keyboardvisibilityevent/2.3.0/view/reviews -> per la rivelazione di apertura/chiusura tastiera
- https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.6 -> per la gestione dei file Json
- https://github.com/algolia/algoliasearch-client-android -> per le ricerche su Firebase
- https://github.com/PhilJay/MPAndroidChart -> per la gestione dei grafici statistici

