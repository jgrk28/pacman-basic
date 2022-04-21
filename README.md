## Modified Pac-Man game to explore AI in games
I forked this repo from a wonderful Pac-Man java clone to extend what the AI 
(the ghosts) could do and explore some game AI concepts.

### Building
To build the game on a mac you can run `./build_game`.
On a Windows computer you can follow the instructions in the OLD_README
and run `mvn clean install` in both pacman-core/ and pacman-ui-swing/.
Once you build with mvn, run the jar at pacman-ui-swing/target/
pacman-ui-swing-1.0-jar-with-dependencies.jar.

### Playing
Once the game is running you can press "v" to toggle between game modes
and press "space" to start the game. The PacMan and MsPacMan implementations
were preserved from the github repo I forked while the "Occupancy Map"
and "Individuals" versions implement my own code. Once playing you can simply
control the game with the arrow keys. Other commands for debugging and
manipulating the game can be found in the OLD_README.

### Ideas
The first idea I was exploring here was using an occupancy map to have the
ghosts try to guess where Pac-Man went when they cannot see him. The ghosts
all share the occupancy map knowledge which is displayed as a red grid as you
play the game.\
\
The second idea I wanted to explore was to build back in the individual ghost
personalities that exist in the original Pac-Man on top of the occupancy map
mode. In addition to different movement patterns I also added different ways
for the ghosts to "detect" Pac-Man which will update the occupancy map.
