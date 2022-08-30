package net.uku3lig.marlowbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.requests.RestAction;
import net.uku3lig.marlowbot.Main;

import javax.annotation.CheckReturnValue;

public class Util {
    public static EmbedBuilder getEmbed(ComponentInteraction event) {
        return new EmbedBuilder(event.getMessage().getEmbeds().stream().findFirst().orElse(null));
    }

    @CheckReturnValue
    public static RestAction<Message> sendRejectionToUser(ModalInteractionEvent event, final String reason) {
        return getUser(event.getHook())
                .flatMap(User::openPrivateChannel)
                .flatMap(c -> c.sendMessageFormat("Your contact request was rejected. %s", reason))
                .flatMap(m -> event.getHook().sendMessageFormat("Contact request rejected.").setEphemeral(true));
    }

    public static RestAction<User> getUser(InteractionHook hook) {
        return hook.retrieveOriginal()
                .map(Message::getMessageReference)
                .flatMap(MessageReference::resolve)
                .map(m -> m.getEmbeds().get(0).getFooter())
                .map(MessageEmbed.Footer::getText)
                .flatMap(id -> Main.getJda().retrieveUserById(id));
    }

    public static String mention(long id) {
        return "<@" + id + ">";
    }

    private Util() {}
}
