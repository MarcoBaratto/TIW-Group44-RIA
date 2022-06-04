# Versione con JavaScript
Si realizzi un’applicazione client server web che modifica le specifiche precedenti come segue:
● La registrazione controlla la validità sintattica dell’indirizzo di email e l’uguaglianza tra
i campi “password” e “ripeti password”, anche a lato client.
● Dopo il login, l’intera applicazione è realizzata con un’unica pagina.
● Ogni interazione dell’utente è gestita senza ricaricare completamente la pagina, ma
produce l’invocazione asincrona del server e l’eventuale modifica del contenuto da
aggiornare a seguito dell’evento.
● I controlli di validità dei dati di input (ad esempio importo non nullo e maggiore di zero)
devono essere realizzati anche a lato client.
● L’avviso di fallimento è realizzato mediante un messaggio nella pagina che ospita
l’applicazione.
● L’applicazione chiede all’utente se vuole inserire nella propria rubrica i dati del
destinatario di un trasferimento andato a buon fine non ancora presente. Se l’utente
conferma, i dati sono memorizzati nella base di dati e usati per semplificare
l’inserimento. Quando l’utente crea un trasferimento, l’applicazione propone mediante
una funzione di auto-completamento i destinatari in rubrica il cui codice corrisponde
alle lettere inserite nel campo codice utente destinatario.
