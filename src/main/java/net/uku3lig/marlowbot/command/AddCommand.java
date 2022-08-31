package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.uku3lig.marlowbot.core.ICommand;

import java.util.EnumSet;

public class AddCommand implements ICommand {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("add", "adds an user to the ticket")
                .addOption(OptionType.USER, "user", "the user to add", true);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (!(event.getChannel() instanceof TextChannel channel)) return;

        long id = event.getOption("user", 0L, o -> o.getAsUser().getIdLong());
        channel.getManager().putMemberPermissionOverride(id, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                .flatMap(v -> event.reply("Successfully added <@" + id + "> to the ticket.").setEphemeral(true))
                .onErrorFlatMap(t -> event.reply("Unknown user.").setEphemeral(true))
                .queue();
    }
}
