package net.uku3lig.marlowbot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.marlowbot.core.IButton;
import net.uku3lig.marlowbot.util.Database;
import net.uku3lig.marlowbot.util.Util;
import net.uku3lig.marlowbot.util.entities.Config;

import java.awt.*;
import java.util.EnumSet;
import java.util.Optional;

public class AcceptButton implements IButton {
    @Override
    public Button getButton() {
        return Button.success("mod_accept", "Accept Mod");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        Config config = Database.getById(Config.class, event.getGuild().getIdLong()).orElse(new Config(event.getGuild()));
        Category category = event.getGuild().getCategoryById(config.getTicketCategory());

        if (category == null) {
            event.reply("Please set a ticket category with /config.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = Util.getEmbed(event)
                .setTitle("CONTACT REQUEST ACCEPTED")
                .setColor(Color.GREEN);

        long id = Optional.ofNullable(builder.build().getFooter()).map(MessageEmbed.Footer::getText).map(Long::parseLong).orElse(0L);
        String tag = Optional.ofNullable(builder.build().getAuthor()).map(MessageEmbed.AuthorInfo::getName).orElse("unknown").replace('#', '-');

        category.createTextChannel(tag).addMemberPermissionOverride(id, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                .flatMap(c -> c.sendMessage(Util.mention(id) + " " + Util.mention(event.getGuild().getOwnerIdLong())).setEmbeds(builder.build()))
                .flatMap(m -> event.editMessageEmbeds(builder.setDescription(m.getChannel().getAsMention()).build()))
                .queue();
    }
}
