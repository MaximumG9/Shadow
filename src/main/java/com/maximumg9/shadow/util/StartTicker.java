package com.maximumg9.shadow.util;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.items.Eye;
import com.maximumg9.shadow.modifiers.Modifier;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;

public class StartTicker implements Tickable {
    private static final int NETHER_LAVA_HEIGHT = 32;
    final Shadow shadow;

    // We give ~3s for the server to catch up
    int ticksLeft = 13 * 20;

    public StartTicker(Shadow shadow) {
        this.shadow = shadow;

        for (IndirectPlayer player : shadow.getOnlinePlayers()) {
            player.setTitleTimesNow(10, 20, 10);
            player.sendTitleNow(
                Text.literal("Spawning Ender Eyes")
                    .styled(style -> style.withColor(Formatting.DARK_GREEN))
            );
        }

        spawnEnderEyes();
    }

    @Override
    public void tick() {
        if (ticksLeft % 20 == 0 && ticksLeft <= 200) {
            for (IndirectPlayer player : this.shadow.getOnlinePlayers()) {
                player.setTitleTimesNow(5, 10, 5);
                player.sendTitleNow(Text.literal(TimeUtil.ticksToText(this.ticksLeft, false)));
            }
        }

        ticksLeft--;
    }

    @Override
    public void onEnd() {
        shadow.state.phase = GamePhase.PLAYING;

        if (!shadow.config.roleManager.pickRoles()) return;
        if (!shadow.config.modifierManager.pickModifiers()) return;

        for (IndirectPlayer player : shadow.getOnlinePlayers()) {
            if (player.role.getFaction() == Faction.SPECTATOR) {
                player.getPlayerOrThrow().changeGameMode(GameMode.SPECTATOR);
            } else {
                player.getPlayerOrThrow().changeGameMode(GameMode.SURVIVAL);
            }

            player.clearPlayerData(CancelPredicates.NEVER_CANCEL);

            player.role.init();
            player.modifiers.forEach(Modifier::init);
            player.sendMessage(TextUtil.gray("Your modifiers: ").append(
                player.modifiers.isEmpty() ? TextUtil.red("None") : Texts.join(
                    player.modifiers.stream().map(modifier -> modifier.getName().copy())
                        .toList(),
                    Text.literal(", ").styled(style -> style.withColor(Formatting.GRAY))
                )
            ), CancelPredicates.NEVER_CANCEL);

            player.frozen = false;

            player.setTitleTimesNow(10, 40, 10);
            player.sendTitleNow(player.role.getName());
            player.sendSubtitleNow(player.role.getSubFaction().name);
        }

        for(ServerWorld world : shadow.getServer().getWorlds()) {
            world.setTimeOfDay(0L);
        }

        shadow.init();

        shadow.saveAsync();

        shadow.addTickable(new GracePeriodTicker(shadow));
    }

    private void spawnEnderEyes() {
        BlockPos center = shadow.state.currentLocation;

        ServerWorld overworld = this.shadow.getServer().getOverworld();

        for (int i = 0; i < this.shadow.config.overworldEyes; i++) {
            int radius = this.shadow.config.worldBorderSize / 2;

            int x = center.getX() + this.shadow.random.nextBetween(-radius, radius);
            int z = center.getZ() + this.shadow.random.nextBetween(-radius, radius);
            int y = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);

            spawnEye(overworld, new BlockPos(x, y, z));
        }

        ServerWorld nether = this.shadow.getServer().getWorld(ServerWorld.NETHER);

        if (nether == null) {
            shadow.ERROR("The nether does not exist");
            return;
        }

        double netherScaleFactor = DimensionType.getCoordinateScaleFactor(overworld.getDimension(), nether.getDimension());

        for (int i = 0; i < this.shadow.config.netherRoofEyes; i++) {
            int radius = this.shadow.config.worldBorderSize / 2;

            int x = (int) (center.getX() * netherScaleFactor) + this.shadow.random.nextBetween(-radius, radius);
            int z = (int) (center.getZ() * netherScaleFactor) + this.shadow.random.nextBetween(-radius, radius);

            spawnEye(nether, new BlockPos(x, nether.getLogicalHeight() + 1, z));
        }

        for (int i = 0; i < this.shadow.config.netherEyes; i++) {
            int radius = this.shadow.config.worldBorderSize / 2;

            BlockPos.Mutable pos = new BlockPos.Mutable();

            while (true) {
                int x = (int) (center.getX() * netherScaleFactor) + this.shadow.random.nextBetween(-radius, radius);
                int y = this.shadow.random.nextBetween(NETHER_LAVA_HEIGHT, nether.getLogicalHeight());
                int z = (int) (center.getZ() * netherScaleFactor) + this.shadow.random.nextBetween(-radius, radius);

                pos.set(x, y, z);

                BlockState state;

                while ((state = nether.getBlockState(pos)).isAir()) {
                    pos.move(Direction.DOWN);
                }

                pos.move(Direction.UP);

                BlockState currentState = nether.getBlockState(pos);

                if (
                    state.getFluidState().getBlockState().getBlock() != Blocks.LAVA &&
                        state.getBlock() != Blocks.FIRE &&
                        currentState.isAir()
                ) {
                    break;
                }
            }

            spawnEye(nether, pos);
        }
    }

    private void spawnEye(ServerWorld world, BlockPos pos) {
        pos.add(0, 1, 0);

        Vec3d blockCenter = pos.toCenterPos();
        ItemEntity item = new ItemEntity(world, blockCenter.x, blockCenter.y, blockCenter.z, new ItemStack(Items.ENDER_EYE), 0, 0.2, 0);
        item.setInvulnerable(true);
        item.setNeverDespawn();

        DisplayEntity.ItemDisplayEntity display = EntityType.ITEM_DISPLAY.create(world);
        assert display != null;
        display.getStackReference(0).set(new ItemStack(Items.ENDER_EYE));
        display.updatePosition(blockCenter.x, blockCenter.y, blockCenter.z);

        world.spawnEntity(item);
        world.spawnEntity(display);

        Eye eye = new Eye(world.getRegistryKey(), item.getUuid(), display.getUuid(), pos);

        shadow.state.eyes.add(eye);
    }

    @Override
    public boolean shouldEnd() {
        return ticksLeft <= 0;
    }

    public static class GracePeriodTicker implements Tickable {
        final Shadow shadow;

        int ticksLeft = 0;

        public GracePeriodTicker(Shadow shadow) {
            this.shadow = shadow;
            if (shadow.config.gracePeriodTicks >= 0) {
                ticksLeft = shadow.config.gracePeriodTicks;
                shadow.getServer().setPvpEnabled(false);
                for (IndirectPlayer player : this.shadow.getOnlinePlayers()) {
                    player.sendMessageNow(
                        Text.literal("There is a " + TimeUtil.ticksToText(shadow.config.gracePeriodTicks, true) + " grace period. All PVP and any killing-related abilities are disabled during this time!").styled(style -> style.withColor(Formatting.GREEN))
                    );
                }
            }
        }
        @Override
        public void tick() {
            ticksLeft--;
        }

        @Override
        public void onEnd() {
            if(shadow.state.phase != GamePhase.PLAYING) return;
            shadow.getServer().setPvpEnabled(true);
            for (IndirectPlayer player : this.shadow.getOnlinePlayers()) {
                player.sendMessageNow(
                    Text.literal("The grace period has ended!").styled(style -> style.withColor(Formatting.GOLD))
                );
            }
        }

        @Override
        public boolean shouldEnd() {
            return ticksLeft <= 0 || shadow.state.phase != GamePhase.PLAYING;
        }
    }
}
