# TreasureHunt
Basic Treasure Hunt module, massive improvements coming

This module aims to create a treasure hunt for the player inside Terasology. As of now it is pretty small and has room for major improvements:

- The OnPlayerSpawnedEvent method can and will be replaced by a more appropriate one
- Code reformating will be done in order to access player & chest locations more easily
- General code optimization and improvement, because as of now I use a "trick" to achieve what I want to do (it works, but is ugly)
- Will add a thread/scheduled task to refresh player location because ATM it's only loaded once
- Will add riddles instead of simple Player location and Chest Location
