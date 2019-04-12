# Deny & Conquer

Deny & Conquer is a real-time multiplayer game written in Java.

Design details:
* It uses a multithreaded client-server based distributed system
* Concurrency is controlled by mutexes to lock critical sections to avoid race conditions
* Actions are coordinated by storing client messages in a priority queue and processing them by their timestamp
* Fault tolerance is supported by replicating the game state and electing a new server through consensus
