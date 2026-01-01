package com.maximumg9.shadow.abilities.filters;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityFilterResult;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.TimeUtil;

public abstract class Filters {

    public static class NotGracePeriod extends Filter {
        private static final String DEFAULT_MESSAGE = "You cannot use this ability during the grace period";
        public NotGracePeriod() {
            super(DEFAULT_MESSAGE);
        }

        public NotGracePeriod(String message) {
            super(message);
        }

        @Override
        public boolean filter(Ability ability) {
            return !ability.getPlayer().getShadow().isGracePeriod();
        }
    }

    public static class Night extends Filter {
        private static final String DEFAULT_MESSAGE = "You can only use this ability during night";

        public Night() {
            super(DEFAULT_MESSAGE);
        }

        public Night(String message) {
            super(message);
        }

        @Override
        public boolean filter(Ability ability) {
            return ability.getShadow().isNight();
        }
    }

    public static class RequiredMaxHealth extends Filter {
        private static final String DEFAULT_MESSAGE = "You do not have enough health";

        private final float requiredHealth;

        public RequiredMaxHealth(float requiredHealth) {
            this(requiredHealth, DEFAULT_MESSAGE);
        }

        public RequiredMaxHealth(float requiredHealth, String message) {
            super(message);
            this.requiredHealth = requiredHealth;
        }

        @Override
        public boolean filter(Ability ability) {
            return ability.getPlayer().getPlayerOrThrow().getMaxHealth() > requiredHealth;
        }
    }


    public static class Cooldown extends Filter {
        private static final String DEFAULT_MESSAGE = "This ability is on cooldown for %s";

        private final long cooldown;

        public Cooldown(long cooldown) {
            this(cooldown,DEFAULT_MESSAGE);
        }

        public Cooldown(long cooldown, String message) {
            super(message);
            this.cooldown = cooldown;
        }

        @Override
        public boolean filter(Ability ability) {
            return ability.getCooldownTimeLeft(this.cooldown) > 0;
        }

        @Override
        public AbilityFilterResult test(Ability ability) {
            long timeLeft = ability.getCooldownTimeLeft(this.cooldown);
            return ability.getCooldownTimeLeft(this.cooldown) > 0 ?
                AbilityFilterResult.FAIL(
                    String.format(
                        this.message,
                        TimeUtil.ticksToText(timeLeft,false)
                    )
                ) : AbilityFilterResult.PASS();
        }
    }

    public static class NonShadowsGreaterThanShadows extends Filter {

        private static final String DEFAULT_MESSAGE = "You cannot use this when there are more non shadows than shadows.";

        public NonShadowsGreaterThanShadows() {
            super(DEFAULT_MESSAGE);
        }

        public NonShadowsGreaterThanShadows(String message) {
            super(message);
        }

        @Override
        public boolean filter(Ability ability) {
            Shadow shadow = ability.getShadow();

            long shadows = shadow.indirectPlayerManager
                .getRecentlyOnlinePlayers(shadow.config.disconnectTime)
                .stream()
                .filter(
                    (player) ->
                        player.role != null &&
                            player.role.getFaction() == Faction.SHADOW
                ).count();
            long nonShadows = shadow.indirectPlayerManager
                .getRecentlyOnlinePlayers(shadow.config.disconnectTime)
                .stream().filter(
                    (player) -> player.role != null &&
                        player.role.getFaction() != Faction.SPECTATOR
                )
                .count() - shadows;

            return nonShadows > shadows;
        }
    }

    public static class OneTimeUse extends LimitedUses {
        private static final String DEFAULT_MESSAGE = "You have already used this ability";
        public OneTimeUse(String message) {
            super(1, message);
        }
        public OneTimeUse() {
            this(DEFAULT_MESSAGE);
        }
    }

    public static class LimitedUses extends Filter {
        private static final String DEFAULT_MESSAGE = "You cannot use this ability more than %s times";

        private final int maxUses;
        private int uses = 0;

        public LimitedUses(int maxUses, String message) {
            super(message);
            this.maxUses = maxUses;
        }

        @Override
        public boolean filter(Ability ability) {
            return uses <= maxUses;
        }

        public AbilityFilterResult test(Ability ability) {
            return this.filter(ability) ? AbilityFilterResult.PASS() :
                AbilityFilterResult.FAIL(String.format(this.message,maxUses));
        }

        @Override
        public void postApply(Ability ability) {
            this.uses++;
            super.postApply(ability);
        }
    }
}
