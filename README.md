# Deny & Conquer

Deny & Conquer is a real-time multiplayer game written in Java.

Design details:
* Uses a multithreaded client-server based distributed system
* Concurrency is controlled by mutexes to lock critical sections to avoid race conditions
* Actions are coordinated by storing client messages in a priority queue and processing them by their timestamp
* Fault tolerance is supported by replicating the game state and electing a new server through consensus
* The GUI is written using JavaFX


How to run application:
* cd into the project repository and run the command "mvn compile exec:java"


![solarized palette](https://github.com/scc23/deny-and-conquer/blob/master/screenshots/menu1.png)
![solarized palette](https://github.com/scc23/deny-and-conquer/blob/master/screenshots/menu2.png)
![solarized palette](https://github.com/scc23/deny-and-conquer/blob/master/screenshots/gameplay.png)
