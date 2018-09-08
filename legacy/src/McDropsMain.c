/* James Moretti
 * Apr 5, 2014
 *
 * McDropsMain.c - Main / driving file for Minecraft Drops
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

#define NAME_LENGTH 16
#define MAX_LC_DIGITS 3
#define MINUTES_BETWEEN_DROPS 15
#define DEFAULT_ITEM 19	//Sponge
#define MAX_PLAYERS 20

typedef struct {
	int itemId;
	float dropChance;
	float lowerThresh;
	int bundleNum;
} DropItem;

typedef struct {
	DropItem* item;
	int length;
} DropTable;

/*
 * Gets the number of items in DropTable.txt.
 * Number of lines = number of items.
 */
int getDropTableLength() {
    char lcbuff[MAX_LC_DIGITS + 1];
    FILE* lcp = popen("wc -l < DropTable.txt", "r");
    fgets(lcbuff, MAX_LC_DIGITS + 1, lcp);
    int estat = pclose(lcp);
    int lineCount = atoi( lcbuff );

	// Error checking
	if(estat == -1) {
        printf("Failed to get drop table length!\n");
        exit(1);
	}

	return lineCount;
}

/*
 * Populate interal drop table from drop table file
 */
void populateDropTable(DropTable* table) {
	FILE* fp = fopen("DropTable.txt", "r");
	int i;
	for (i = 0; i < table->length; i++) {
		// %*[^\n] reads until the new line char, %*c reads the new line char
		fscanf(fp, "%d,%f,%d%*[^\n]%*c",
			&(table->item[i].itemId), &(table->item[i].dropChance),
			&(table->item[i].bundleNum));
	}
	int estat = fclose(fp);

	// Error checking
	if(estat == -1) {
		printf("Failed to read in drop table!\n");
		exit(2);
	}
}

/*
 * Calculates drop chance percentage ranges for each item in the table.
 *
 * Items are initially read in with an arbirary integer weight for their
 * chance of being dropped. This function sums up all of those weights
 * then replaces each individual item's weight with a percentage and
 * sum of the percentages before it (the lower percent threshold).
 *
 * For example, if an item has a drop chance of 10% and a lower
 * threshold of 30%, then the item would drop if the random number generator
 * hits between 30% and 40% (30% + 10%). The item directly after this one
 * would have a lower threshold of 40%.
 */
void calcDropValues(DropTable* table) {
	// Calcluate total weight
	int i;
	int totalWeight = 0;
    for (i = 0; i < table->length; i++)
        totalWeight += table->item[i].dropChance;

	// Calculate item drop thresholds
	float currentTotal = 0;
	for (i = 0; i < table->length; i++) {
		table->item[i].lowerThresh = currentTotal;
		table->item[i].dropChance /= totalWeight;
		currentTotal += table->item[i].dropChance;
	}
}

/*
 * Creates, populates, and returns a pointer to the drop table
 */
DropTable* genDropTable() {
	printf("Populating drop table...\n");

	DropTable* table = (DropTable*) malloc(sizeof(DropTable));
	table->length = getDropTableLength();
	table->item = (DropItem*) malloc(table->length * sizeof(DropItem));
	populateDropTable(table);

	printf("Calculating drop values...\n");
	calcDropValues(table);

	return table;
}

/*
 * Populate list of players and return how many were read in
 */
int getPlayerList(char players[][NAME_LENGTH + 1]) {
	FILE* plp = popen("./ListPlayers.sh", "r");

	// Read player number from first line
	int count = 0;
	fscanf(plp, "%*s %*s %d %*s %*s %*s %*[\n]", &count);

	// Read each player into the array. Ignore commas and newlines
	int i;
	for (i = 0; i < count; i++) {
		fscanf(plp, "%[^,\n]%*2c", players[i]);
	}
	int estat = pclose(plp);

	// Error checking
	if(estat == -1) {
		printf("Failed to read in player list!\n");
		exit(3);
	}

	return count;
}

/*
 * Generate a list of drops. One for each player.
 */
void genDropList(DropTable* table, DropItem* playerDrops, int numPlayers) {
	float roll;
	int i, j;
	for (i = 0; i < numPlayers; i++) {
		roll = (float)rand() / RAND_MAX; // Generate a percentage

		// Match our roll to one of the drop items
		for (j = 0; j < table->length; j++) {
			if(table->item[j].lowerThresh <= roll &&
				roll < table->item[j].lowerThresh + table->item[j].dropChance) {
				playerDrops[i] = table->item[j];
			}
		}

		// Catch the extremely rare situation where rand() generates RAND_MAX
		if (roll == RAND_MAX) {
			playerDrops[j].itemId = DEFAULT_ITEM;
			playerDrops[j].bundleNum = 1;
		}
	}
}

/*
 * Give a player his drop (and possibly alert him)
 */
void payout(char* player, DropItem drop) {
	char cmd[50];
	sprintf(cmd, "mcgive %s %d %d", player, drop.itemId, drop.bundleNum);
	system(cmd);
}

/*
 * Generates a list of drops for each player then gives them out
 */
void genAndGiveDrops(DropTable* table, char playerList[][NAME_LENGTH + 1]) {
    // Populate player list
    int numPlayers = getPlayerList(playerList);

    // Get a drop for each player
    DropItem playerDrops[numPlayers];
    genDropList(table, playerDrops, numPlayers);

    // Give each player their drop
    int i;
    for (i = 0; i < numPlayers; i++)
		printf("Dropped %d %d's for %s\n", 
			playerDrops[i].bundleNum, playerDrops[i].itemId, playerList[i]);
        //payout(playerList[i], playerDrops[i]);

    printf("Items dropped for %d players.\n", numPlayers);

}

/*
 * Program driver
 */
int main() {
	printf("Starting MC Drops v2.0\n");
	srand(time(NULL));

	/* The player list probably doesn't need to be kept in main,
		but this does avoid needing to re-allocate the player array
		on every drop. I might move this in the future*/
	char playerHolder[MAX_PLAYERS][NAME_LENGTH + 1];

	DropTable* pdropTable = genDropTable();

	printf("MC Drops successfully initialized. Use Ctrl+C to quit.\n\n");

	// Run once every few minutes
	while (1) {
		genAndGiveDrops(pdropTable, playerHolder);

		// Wait for next run
		sleep(60 * MINUTES_BETWEEN_DROPS);
	}

	// This never gets called, but it's here for completeness' sake
	free(pdropTable->item);
	free(pdropTable);

	return 0;
}
