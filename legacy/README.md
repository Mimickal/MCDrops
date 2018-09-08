MC Drops
========

Breif:
------
    Get rewarded for your patronage! MC Drops is a script that periodically
    drops controlled random items on players in the minecraft server.

About:
------
    We thought it would be cool if the server dropped random items on people
    every once in a while, similar to Team Fortress 2 drops. Many of our
    players are frustrated with the changes made to IndustrialCraft2, and
    rightfully so. It can be difficult to get started, especially if you're
    working alone and suffer an unfortunate death. MC Drops helps starting
    and established players alike by periodically dropping useful loot.

    Every x minutes, MC Drops sends a command to retrieve a list of players.
    It then performs a dice roll to pick a random item from a drop table for
    each of those players. Finally, each player is privately alerted that
    about the item they've recieved.

    Drops are controlled in a table, so people won't just have quantum armor
    falling out of the sky on them. Drops include things like iron ingots,
    lower tier armors and weapons, food, and compasses; Basic goods that are a
    boon for starting players, and convenient for established players.

Technical:
----------
	The file DropTable.txt must exist in the same directory as the MC Drops
	executable. The drop table contains the itemId and weight (chance of 
	being dropped) in CSV format, with only one item per line. An example
	drop item looks like this: id, weight

Versions:
---------
	1.0: Supports reading a drop table from file and dispensing a single item
		 at a time at a fixed interval.

	2.0: Has a config file to change interval of drops. Supports bundles of
		 items and item sub-ids (like with colored wool). Supports random
		 intervals (like drop every 10 to 20 minutes rather than 15).
