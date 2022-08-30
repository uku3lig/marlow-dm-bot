package net.uku3lig.marlowbot.util.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Config {
    @Id
    @Setter(AccessLevel.NONE)
    private long id;

    private long requestsChannel;

    private long ticketCategory;

    public Config(@Nullable Guild g) {
        if (g == null) return;
        this.id = g.getIdLong();
    }
}
