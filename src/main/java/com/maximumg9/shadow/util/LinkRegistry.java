package com.maximumg9.shadow.util;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.AddHealthLink;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Objects;
import java.util.WeakHashMap;

public class LinkRegistry {
    private final WeakHashMap<AddHealthLink.Link, AddHealthLink.Link> linkRegistry = new WeakHashMap<>();

    private final Shadow shadow;

    public LinkRegistry(Shadow shadow) {
        this.shadow = shadow;
    }

    public void addLink(AddHealthLink.Link link) {
        linkRegistry.put(link,link);
    }

    public void removeLink(AddHealthLink.Link link) {
        linkRegistry.remove(link);
    }

    public NbtCompound writeNBT(NbtCompound nbt) {
        NbtList list = new NbtList();
        linkRegistry.keySet()
            .stream()
            .map((link) -> link.writeNBT(new NbtCompound()))
            .forEach(list::add);

        nbt.put("links",list);

        return nbt;
    }

    public void readNBT(NbtCompound nbt) {
        NbtElement possibleLinks = nbt.get("links");
        if((possibleLinks instanceof NbtList links)) {
            clearLinks();
            links.stream()
                .map(
                    (linkData) -> {
                        if(linkData instanceof NbtCompound compound) {
                            AddHealthLink.Link link = new AddHealthLink.Link(this.shadow);
                            link.readNBT(compound);
                            return link;
                        }
                        return null;
                    })
                .filter(Objects::nonNull)
                .forEach(this::assignToPlayers);
        } else {
            throw new IllegalSaveException("Links are not a list");
        }
    }

    public void clearLinks() {
        for(AddHealthLink.Link link : linkRegistry.keySet()) {
            AddHealthLink.Link.destroyLink(link);
        }
    }

    private void assignToPlayers(AddHealthLink.Link link) {
        for(IndirectPlayer p : link.players) {
            if(p.link == null) {
                p.link = link;
            } else {
                throw new IllegalSaveException("link linked to multiple players");
            }
        }

        link.syncHealths(
            new DamageSource(
                MiscUtil.getDamageType(
                    shadow.getServer(),
                    DamageTypes.GENERIC
                )
            )
        );
    }

    public void save() {

    }
}
