package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.uku3lig.marlowbot.Main;
import net.uku3lig.marlowbot.core.IButton;
import net.uku3lig.marlowbot.core.ICommand;
import net.uku3lig.marlowbot.core.IModal;
import net.uku3lig.marlowbot.util.entities.Config;
import net.uku3lig.marlowbot.util.Database;

import java.time.Instant;

public class OpenRequestsCommand implements ICommand, IButton, IModal {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("openrequests", "opens requests in the current channel")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (!(event.getChannel() instanceof MessageChannel channel)) return;
        if (event.getGuild() == null) return;

        Config config = Database.getById(Config.class, event.getGuild().getId()).orElse(new Config(event.getGuild()));
        TextChannel reqChannel = Main.getJda().getTextChannelById(config.getRequestsChannel());
        if (reqChannel == null) {
            event.reply("Please set a request channel using the /config command.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Click the button below to contact Marlow")
                .setColor(0xFFfe88ff)
                .setFooter("marlowbot by uku#1880");

        channel.sendMessageEmbeds(builder.build())
                .setActionRow(getButton())
                .flatMap(m -> event.reply("Created.").setEphemeral(true))
                .queue();
    }

    @Override
    public Button getButton() {
        return Button.primary("open_inquiry", "Open Inquiry").withEmoji(Emoji.fromUnicode("\uD83D\uDCD1"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        event.replyModal(getModal()).queue();
    }

    @Override
    public Modal getModal() {
        TextInput reason = TextInput.create("reason", "Why do you want to contact Marlow?", TextInputStyle.PARAGRAPH)
                .setPlaceholder("I would like to contact Marlow because...")
                .build();

        return Modal.create("contact_modal", "Inquiry details")
                .addActionRow(reason)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        if (event.getGuild() == null) return;

        Config config = Database.getById(Config.class, event.getGuild().getId()).orElse(new Config(event.getGuild()));
        TextChannel channel = Main.getJda().getTextChannelById(config.getRequestsChannel());
        if (channel == null) {
            event.reply("The owner didn't set a form result channel.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl())
                .setFooter(event.getUser().getId())
                .setTimestamp(Instant.now());

        event.getValues().stream()
                .filter(m -> !m.getAsString().isEmpty() && !m.getAsString().isBlank())
                .forEach(m -> builder.addField(m.getId(), m.getAsString(), false));

        channel.sendMessageEmbeds(builder.build())
                //.setActionRow(new AcceptButton().getButton(), new RejectButton().getButton()) TODO
                .flatMap(m -> event.reply("Thanks for your submission. You will receive a reply shortly.").setEphemeral(true))
                .queue();

    }
}
