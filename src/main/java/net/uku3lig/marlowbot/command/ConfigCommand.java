package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.uku3lig.marlowbot.core.ICommand;
import net.uku3lig.marlowbot.util.Config;
import net.uku3lig.marlowbot.util.Database;

import java.util.Objects;

public class ConfigCommand implements ICommand {
    @Override
    public CommandData getCommandData() {
        SubcommandData formChannel = new SubcommandData("formchannel", "sets the channel in which the form results are sent")
                .addOption(OptionType.CHANNEL, "channel", "the channel");
        SubcommandData ticketCategory = new SubcommandData("ticketcategory", "sets the category in which tickets are created")
                .addOption(OptionType.CHANNEL, "category", "the category");

        return Commands.slash("config", "configures the bot to your liking")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .addSubcommands(formChannel, ticketCategory);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        Config config = Database.getById(Config.class, event.getGuild().getIdLong()).orElse(new Config(event.getGuild()));

        switch (subcommand) {
            case "formchannel" -> {
                OptionMapping option = Objects.requireNonNull(event.getOption("channel"));
                GuildChannelUnion channel = option.getAsChannel();

                if (channel instanceof TextChannel textChannel) {
                    config.setRequestsChannel(textChannel.getIdLong());
                    event.replyFormat("Set form channel to %s.", textChannel.getAsMention()).setEphemeral(true).queue();
                } else {
                    event.reply("Not a text channel.").setEphemeral(true).queue();
                }
            }
            case "ticketcategory" -> {
                OptionMapping option = Objects.requireNonNull(event.getOption("category"));
                GuildChannelUnion channel = option.getAsChannel();

                if (channel instanceof Category category) {
                    config.setTicketCategory(category.getIdLong());
                    event.replyFormat("Ticket category set to `%s`.", category.getName()).setEphemeral(true).queue();
                } else {
                    event.reply("Not a category.").setEphemeral(true).queue();
                }
            }

            default -> event.reply("Unknown setting.").setEphemeral(true).queue();
        }

        Database.saveOrUpdate(config);
    }
}
