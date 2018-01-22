package mimickal.mc.mcdrops;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements ICommand {

    private static final String COMMAND_NAME = "reloaddrops";
    private static final String ALIAS_1 = "rd";

    private final List<String> aliases;

    public ReloadCommand() {
        aliases = new ArrayList<>();
        aliases.add(COMMAND_NAME);
        aliases.add(ALIAS_1);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + COMMAND_NAME;
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        DropTable.loadDropTable();
        Config.load();
        DropsMod.LOGGER.info("Reloaded config and drop table");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canCommandSenderUseCommand(server.getOpPermissionLevel(), COMMAND_NAME);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
