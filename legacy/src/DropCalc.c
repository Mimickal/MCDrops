/* James Moretti
 * Apr 22, 2014
 *
 * This program takes in a drop table and outputs the
 * percent drop chances on all the items.
 *
 * This will probably eventually get integrated into McDrops,
 * which is why drop tables are read using command line args
 */

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define MAX_LC_DIGITS 3
#define DEFAULT_ITEM 19 // Sponge
#define CMD_LEN 50

typedef struct {
    int itemId;
    float dropChance;
} DropItem;

/*
 * Gets the number of items in DropTable.txt.
 * Number of lines = number of items.
 */
int getDropTableLength(char* fname) {
	char cmd[CMD_LEN];
	sprintf(cmd, "wc -l < %s", fname);

    char lcbuff[MAX_LC_DIGITS + 1];
    FILE* lcp = popen(cmd, "r");
    fgets(lcbuff, MAX_LC_DIGITS + 1, lcp);
    pclose(lcp);
    int lineCount = atoi( lcbuff );

    return lineCount;
}

/*
 * Populate interal drop table from drop table file
 */
void getDropTable(char* fname, DropItem* table, int length) {
    FILE* fp = fopen(fname, "r");
    int i;
    for (i = 0; i < length; i++) {
        // %[^\n] reads until the new line char, %*c reads the new line
        fscanf(fp, "%d,%f%*[^\n]%*c", &table[i].itemId, &table[i].dropChance);
    }
    fclose(fp);
}

/*
 * Calculates percent chance of dropping each item
 * and outputs them to a file
 */
void outputDropPercentages(DropItem* dropTable, int length) {
	// Calcluate total weight
    int i;
    int totalWeight = 0;
    for (i = 0; i < length; i++)
        totalWeight += dropTable[i].dropChance;

    // Calculate item drop thresholds
	float totalPercent;
    for (i = 0; i < length; i++) {
        dropTable[i].dropChance /= totalWeight;
		totalPercent += dropTable[i].dropChance * 100;
	}

	// Output all percentages to a file
	FILE* of = fopen("DropCalc.txt", "w");
	for (i = 0; i < length; i++) {
		fprintf(of, "Item %d: %f%%\n",
			dropTable[i].itemId, dropTable[i].dropChance * 100);
	}
	fprintf(of, "Total percent: %f%%\n", totalPercent);
	fclose(of);
}

/*
 * argv[0] - program name
 * argv[1] - flag
 * argv[2] - drop table file
 */
int main(int argc, char** argv) {
	// Make sure our command line arguments are right
	if (argc != 3) {
		printf("%s usage: %s -c [drop table txt]\n");
	}
	if (strcmp(argv[1], "-c" ) != 0) {
		printf("Invalid flag %s\n", argv[1]);
	}

    srand(time(NULL));

    // Populate drop table
	int numDrops;
    numDrops = getDropTableLength(argv[2]);
    DropItem dropTable[numDrops];
    getDropTable(argv[2], dropTable, numDrops);

	// Calculate and output drop chances to file
    outputDropPercentages(dropTable, numDrops);
	printf("Drops output to DropCalc.txt\n");

	return 0;
}
