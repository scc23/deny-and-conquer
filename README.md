# Deny & Conquer

Deny & Conquer is a real-time multiplayer game written in Java.

## Game Objective
The objective is to deny your opponents filling the most number of boxes, by taking over as many boxes as you can. To take over a box, you must colour the box with your pen, and at least a certain percentage threshold of the area of the box must be coloured to be considered taken over by you. Otherwise, another player can try taking over the box. Once a box has been token over by a given player; i.e., that player has coloured at least the specified percentage threshold of the area of a box, then the box turns entirely to the colour of the player and can no longer be taken over by any other player. At the end of the game; i.e., when all boxes have been taken over, whoever has the most number of boxes will win the game. Having more than 1 winner is possible.

## Game Instructions
* The game is played by 4 players, each having a pen of different colour
* 1 player hosts the game, and 3 other players join the game by entering the host's IP address
* The host player can configure the game board size, the thickness of the pen, and the threshold needed to colour a box to own it
* To take over a white box, a player must click anywhere inside that box and hold the click button down, while scribbling inside the box
* While a player is scribbling in a box, that box is no longer available to other players

## Design Details
* Implemented a multithreaded client-server based distributed system using TCP and UDP sockets
* Concurrency is controlled by mutexes to lock critical sections to avoid race conditions
* Actions are coordinated by storing client messages in a priority queue and processing them by their timestamp
* Fault tolerance is supported by replicating the game state and electing a new server through consensus
* The GUI is written using JavaFX

### Concurrency
Once a player clicks in a box and while scribbling, that box is "locked" and made unavailable to other players, until the first player lets go of the click button.

### Coordination
The clocks of all players are synchronized, and the server checks the timestamp of each player's message. In case more than 1 player has clicked in a box at almost the same time, whoever has the smaller timestamp will be given access to that box.

### Fault Tolerance
The computer of the player who is running the server may go down. This could be because of network problems, that player's computer crashing, or the player getting mad that she or he is losing and just stopping the server. In such a case, the system will choose another player's computer to run the server for the remaining players, starting from the last known state of the game. This is handled with fault tolerance by replication. There will be a pause while the other serving is coming up. During this pause, the game will display a message to the remaining players so they know something is wrong and they need to wait.

## How to Run the Application:
Run the command "mvn compile exec:java" in the root project directory.

![solarized palette](https://github.com/scc23/deny-and-conquer/blob/master/screenshots/menu1.png)

![solarized palette](https://github.com/scc23/deny-and-conquer/blob/master/screenshots/menu2.png)

![solarized palette](https://github.com/scc23/deny-and-conquer/blob/master/screenshots/gameplay.png)
