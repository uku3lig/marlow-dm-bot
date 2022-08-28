package net.uku3lig.marlowbot.core;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public interface IButton {
    Button getButton();

    void onButtonClick(ButtonInteractionEvent event);
}
