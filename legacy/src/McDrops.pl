'use strict';
'use warnings';

use JSON::Parse;

use constant PLAYER_FILE => '/home/minecraft/server/hidden.txt';
use constant DEFAULT_ITEM => 'sponge';
use constant MINUTES_BETWEEN_DROPS => 15;

# Read in drop data
my $dropdata = JSON::Parse::json_file_to_perl('droptable.json');


my $droplist = [];	# List of weighted drop hashes
my $totalweight = 0;	# Sum of all weights

# Make weighted drop list
for my $key (keys %$dropdata) {
	my $drop = {};

	$drop->{'name'} = $key;

	# Amount of item to drop can be specified a few ways.
	# 1. Always drop 'count' items
	# 2. Specify a min and max amount for the item
	# 3. Specify a max, and min will default to 1
	# 4. Omit max and min, and count will default to 1
	if (defined $dropdata->{$key}->{'count'}) {
		$drop->{'minamount'} = $dropdata->{$key}->{'count'};
		$drop->{'maxamount'} = $dropdata->{$key}->{'count'};
	}
	elsif (defined $dropdata->{$key}->{'min'} && defined $dropdata->{$key}->{'max'}) {
		$drop->{'minamount'} = $dropdata->{$key}->{'min'};
		$drop->{'maxamount'} = $dropdata->{$key}->{'max'};
	}
	elsif (!defined $dropdata->{$key}->{'min'} && defined $dropdata->{$key}->{'max'}) {
		$drop->{'minamount'} = 1;
		$drop->{'maxamount'} = $dropdata->{$key}->{'max'};
	}
	else {
		print "Warning: min specified without max for $key\n" if (defined $dropdata->{$key}->{'min'});
		$drop->{'minamount'} = 1;
		$drop->{'maxamount'} = 1;
	}

	# Calculate range for selecting this item
	die "Error: No weight specified for '$key'!\n" if (!defined $dropdata->{$key}->{'weight'});

	$drop->{'minweight'} = $totalweight;
	$totalweight += $dropdata->{$key}->{'weight'};
	$drop->{'maxweight'} = $totalweight;

	push(@$droplist, $drop);
}

sub getWeightedRandomItem {
	my $roll = int(rand($totalweight));

	# Find the item in range of this roll
	for my $item (@$droplist) {
		if ($item->{'minweight'} <= $roll && $roll < $item->{'maxweight'}) {
			my $amount = int(rand($item->{'maxamount'} - $item->{'minamount'})) + $item->{'minamount'};
			return {
				name   => $item->{'name'},
				amount => $amount,
			};
		}
	}

	# If we somehow don't find the item
	return DEFAULT_ITEM;
}

print "Drops table loaded\n";
print "Spawning drops every @{[MINUTES_BETWEEN_DROPS]} minutes...\n\n";

# Run continually
while (1) {
	# Get list of currently online players.
	#
	# This works off of the player file the HideNames mod maintains.
	# The HideNames config setting for removing players from that
	# file when they logout needs to be enabled.
	#
	# The format for this file is:
	# playername:true
	# playername:false
	my $playerlist = [];
	open(my $playerfile, '<', PLAYER_FILE);
	while (my $line = <$playerfile>) {
		my ($playername) = $line =~ /^([\w]+):(?:true|false)$/;
		if ($playername) {
			push(@$playerlist, $playername);
		}
	}

	# Randomly select and drop an item for each online player
	for $player (@$playerlist) {
		my $item = getWeightedRandomItem();
		my $command = "/give $player $item->{'name'} $item->{'amount'}";
		system("tmux send-keys -t 0:0 \"$command\" C-m");
	}

	print "Items dropped for @{[scalar @$playerlist]} player(s)\n";

	sleep(MINUTES_BETWEEN_DROPS * 60);
}

