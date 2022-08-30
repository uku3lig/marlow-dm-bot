package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.uku3lig.marlowbot.core.IButton;
import net.uku3lig.marlowbot.core.IModal;
import net.uku3lig.marlowbot.util.Util;

import java.awt.*;

public class RejectButton implements IButton, IModal {
    @Override
    public Button getButton() {
        return Button.danger("reject", "Reject");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        MessageEmbed edited = Util.getEmbed(event)
                .setTitle("CONTACT REQUEST REJECTED")
                .setColor(Color.RED)
                .build();

        event.replyModal(getModal())
                .flatMap(v -> event.getHook().editOriginalEmbeds(edited).setActionRows())
                .queue();
    }

    @Override
    public Modal getModal() {
        TextInput reason = TextInput.create("reject_reason", "Rejection reason", TextInputStyle.SHORT)
                .setRequired(false)
                .build();

        return Modal.create("reject_modal", "Contact Request Rejection")
                .addActionRow(reason)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        String reasonText;
        ModalMapping modalReason = event.getValue("reject_reason");

        if (modalReason != null && !modalReason.getAsString().isEmpty() && !modalReason.getAsString().isBlank()) {
            reasonText = "Reason: " + modalReason.getAsString();
        } else {
            reasonText = "No reason was provided.";
        }

        event.deferReply(true).flatMap(v -> Util.sendRejectionToUser(event, reasonText)).queue();
    }

}
