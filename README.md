# I. MazeGame
This is the repo of a p2p maze game.

# II. Project Structure
**Game** : The entry of the maze game. Every client/player is a "Game"

**GameHandler** : Interface, to modify/retrieve info from a game(client/player)

**GameHandlerImpl** : The implementation of GameHandler

**GameState** : The information of a running game. Include but not limit to player list, scoreboard......

**PingThread** : To ping other client periodically(in order to detect player crash)

**Player** : This is a bad design...... It contains the basic info of a player, like player score, player id......

**Tracker** : The tracker is the center registry centre.

**TrackerHandler** : Interface, to modify/retrieve info the tracker.

**TrackerHandlerImpl** : The implementation of TrackHandler.

# III. Game Main Thread Logic
**1.** Use the parameter to create a game object

**2.** Connect to the tracker, get the basic game info(N,K and a random player) and register yourself

**3.** Initialize the game state and set primary/backup server

**4.** Start Ping thread

**5.** Receive the input from command line and operate corresponding to the input

# IV. Ping thread Logic
**1.** Ping primary server and handle fail if needed

**2.** Ping backup server and handle fail if needed

**3.** ping some random player and handle fail if needed

# V. Crash/Fail handling Logic
To handle server/client fail, you firstly have to obtain local game's gamehandler's lock(using synchronized).
The reason is that if you do not do so, someone may modify your primary/backup server during your progress
of handling the fail. That will incur some chaos in your server info.

A example that I've met during debugging is like this : P1 is primary, P2 is backup, P3 is client.
I shut down P1, P3 is the first one that find it, P2 find it just a little slower that P3.
P3 now will contact P2(the backup) to handle this problem, and according to the logic below, the result will be:
P2 will become new primary, P3 will become new backup.

And P3 actually have entered the fail handling function when P2 updated this new server info to P3. Now the chaos occurs: 
P2 contact its backup server(should be itself, but now is P3 because P3 updated this information to P2 after P2 enter this function)
P2 now thinks that P3 should become the new primary and itself should become the new backup. So the outcome
of P2's fail handle will be : Primary : P3; Backup : P2.

To avoid the above chaos, you should obtain your game handler's lock before you handle fail. If the lock
is obtained by you, no one can update your info(to update a client's info, you must obtain its game handler lock) 
after you enter the fail handling function

## 1. Primary Server Crash
If the primary server crashed, we need to contact backup server.
(It's guaranteed that the backup server is not crashed at this time,
because we ping them every 1.5 seconds and the interval of two crashes is at least 2 seconds)

If you're the first one that contact the backup server to handle primary server crash, you should find that the backup server is its own backup server. Now you become the new backup,the original backup server becomes the new primary server.

If you're not the first one that contact the backup server, you should find that now the backup server has already become the primary
server of itself(and all other players), just set your primary and backup server according to the info you get from the original 
backup server(the current primary server).

## 2. Backup Server Crash
If the backup server crashed, we need to contact primary server.
(It's guaranteed that the primary server is not crashed at this time, same reason as mentioned in primary server crash)

If you're the first one that contact the primary server, you should find that the primary server's backup server is the
old crashed backup server. Now you become the new backup server.

If you're not the first one that contact the primary server, you should find that the primary server's backup server is
different from yours(specifically, its backup server is not the one that you found crashed). We reckon that
the new backup server is already regenerated. Just set your new backup according to primary server's backup.

## 3. Random Client Crash
Just delete it from your local game state, notify your primary and backup and the tracker(let them also delete it).




