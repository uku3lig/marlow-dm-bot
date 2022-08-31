package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.marlowbot.core.IButton;

import java.awt.*;

public class KeepOpenButton implements IButton {
    @Override
    public Button getButton() {
        return Button.secondary("keep_open", "Deny & Keep Open");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getUser().getIdLong() != event.getMessage().getMentions().getUsers().get(0).getIdLong()) {
            event.reply("you can't do that. :anger:").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0))
                .setDescription(event.getUser().getAsMention() + " rejected the closing of the ticket.")
                .setColor(Color.RED);

        event.editMessageEmbeds(builder.build())
                .setActionRows()
                .queue();
    }
}
