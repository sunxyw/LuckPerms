package me.lucko.luckperms.commands.group;

import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.commands.MainCommand;
import me.lucko.luckperms.commands.Sender;
import me.lucko.luckperms.commands.SubCommand;
import me.lucko.luckperms.commands.Util;
import me.lucko.luckperms.groups.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupMainCommand extends MainCommand {

    private final List<GroupSubCommand> subCommands = new ArrayList<>();

    public GroupMainCommand() {
        super("Group", "/perms group <group>", 2);
    }

    @Override
    protected void execute(LuckPermsPlugin plugin, Sender sender, List<String> args) {
        if (args.size() < 2) {
            sendUsage(sender);
            return;
        }

        Optional<GroupSubCommand> o = subCommands.stream().filter(s -> s.getName().equalsIgnoreCase(args.get(1))).limit(1).findAny();

        if (!o.isPresent()) {
            Util.sendPluginMessage(sender, "Command not recognised.");
            return;
        }

        final GroupSubCommand sub = o.get();
        if (!sub.isAuthorized(sender)) {
            Util.sendPluginMessage(sender, "You do not have permission to use this command!");
            return;
        }

        List<String> strippedArgs = new ArrayList<>();
        if (args.size() > 2) {
            strippedArgs.addAll(args.subList(2, args.size()));
        }

        final String groupName = args.get(0).toLowerCase();
        plugin.getDatastore().loadGroup(groupName, success -> {
            if (!success) {
                Util.sendPluginMessage(sender, "&eGroup could not be found.");
                return;
            }

            Group group = plugin.getGroupManager().getGroup(groupName);
            if (group == null) {
                Util.sendPluginMessage(sender, "&eGroup could not be found.");
                return;
            }

            if (sub.isArgLengthInvalid(strippedArgs.size())) {
                sub.sendUsage(sender);
                return;
            }

            sub.execute(plugin, sender, group, strippedArgs);
        });
    }

    @Override
    public List<? extends SubCommand> getSubCommands() {
        return subCommands;
    }

    public void registerSubCommand(GroupSubCommand subCommand) {
        subCommands.add(subCommand);
    }

}
