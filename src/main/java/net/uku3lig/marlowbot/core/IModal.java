package net.uku3lig.marlowbot.core;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;

public interface IModal {
    Modal getModal();

    void onModal(ModalInteractionEvent event);
}
