package net.uku3lig.marlowbot.core;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {
    CommandData getCommandData();

    void onCommand(GenericCommandInteractionEvent event);
}
