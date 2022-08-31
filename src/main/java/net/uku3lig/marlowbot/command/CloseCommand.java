package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.uku3lig.marlowbot.Main;
import net.uku3lig.marlowbot.core.IButton;
import net.uku3lig.marlowbot.core.ICommand;
import net.uku3lig.marlowbot.util.Database;
import net.uku3lig.marlowbot.util.entities.Config;

import javax.annotation.CheckReturnValue;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CloseCommand implements ICommand, IButton {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("close", "close a ticket")
                .addOption(OptionType.STRING, "reason", "the reason")
                .addOption(OptionType.BOOLEAN, "force", "false will send a request to the user")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (!(event.getChannel() instanceof TextChannel channel)) return;

        String reason = event.getOption("reason", "No reason was provided.", OptionMapping::getAsString);
        boolean force = event.getOption("force", false, OptionMapping::getAsBoolean);

        CompletableFuture<Message> future = channel.getIterableHistory().cache(false).reverse().takeAsync(1).thenApply(l -> l.get(0));

        // delete channel
        // send dm to user
        // update original embed
        // (case) request closure

        if (!force) {
            channel.getIterableHistory().cache(false).reverse().takeAsync(1)
                    .thenApply(l -> l.get(0))
                    .thenApply(m -> m.getMentions().getUsers().get(0))
                    .thenCompose(m -> {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setTitle("Close Request")
                                .setDescription(event.getUser().getAsMention() + " has requested to close this ticket.")
                                .addField("Reason", reason, false)
                                .setColor(Main.EMBED_COLOR);

                        return event.reply(m.getAsMention())
                                .addEmbeds(builder.build())
                                .addActionRow(getButton(), new KeepOpenButton().getButton())
                                .submit();
                    });
        } else {
            future.thenCompose(m -> event.reply("Closing ticket...").setEphemeral(true).flatMap(h -> closeTicket(m, channel, reason, true)).submit());
        }
    }

    @CheckReturnValue
    private RestAction<Void> closeTicket(Message msg, TextChannel channel, String reason, boolean force) {
        User user = msg.getMentions().getUsers().get(0);
        long originalId = Optional.ofNullable(msg.getEmbeds().get(0).getFooter()).map(MessageEmbed.Footer::getText).map(Long::parseLong).orElse(0L);

        // send dm to user
        return user.openPrivateChannel()
                .flatMap(c -> c.sendMessage("Your ticket has been closed. Reason: " + reason))
                // edit original embed
                .map(m -> {
                    Config config = Database.getById(Config.class, channel.getGuild().getId()).orElse(new Config(channel.getGuild()));
                    return channel.getGuild().getTextChannelById(config.getRequestsChannel());
                })
                .flatMap(c -> c.retrieveMessageById(originalId))
                .flatMap(m -> {
                    EmbedBuilder builder = new EmbedBuilder(m.getEmbeds().get(0))
                            .setTitle("TICKET CLOSED" + (force ? " (FORCED)" : ""))
                            .setColor(force ? 0xFF0D8486 : 0xFF36CFD3)
                            .addField("close reason", reason, false);
                    return m.editMessageEmbeds(builder.build());
                })
                // delete channel
                .flatMap(m -> channel.delete());
    }

    @Override
    public Button getButton() {
        return Button.success("accept_close", "Accept & Close");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (!(event.getChannel() instanceof TextChannel channel)) return;

        if (event.getUser().getIdLong() != event.getMessage().getMentions().getUsers().get(0).getIdLong()) {
            event.reply("you can't do that. :anger:").setEphemeral(true).queue();
            return;
        }

        String reason = event.getMessage().getEmbeds().get(0).getFields().get(0).getValue();

        channel.getIterableHistory().cache(false).reverse().takeAsync(1)
                .thenApply(l -> l.get(0))
                .thenCompose(m -> event.reply("Closing ticket...").setEphemeral(true).flatMap(h -> closeTicket(m, channel, reason, false)).submit());
    }
}
