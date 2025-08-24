package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.items.Eye;
import com.maximumg9.shadow.items.ParticipationEye;
import com.maximumg9.shadow.modifiers.Modifier;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.TimeUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$start")
                .then(
                    literal("force").executes(StartCommand::start)
                )
                .executes(StartCommand::checkAndStart)
        );
    }
    
    public static int checkAndStart(CommandContext<ServerCommandSource> ctx) {
        if (!check(ctx)) {
            ctx.getSource().sendError(Text.literal("Phase is not location selected"));
            return -1;
        }
        return start(ctx);
    }
    
    public static boolean check(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);
        
        return shadow.state.phase == GamePhase.LOCATION_SELECTED;
    }
    
    public static int start(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);
        
        shadow.addTickable(new StartTicker(shadow));
        
        for (IndirectPlayer player : shadow.getOnlinePlayers()) {
            ServerPlayerEntity entity = player.getPlayerOrThrow();
            
            player.clearPlayerData(CancelPredicates.NEVER_CANCEL);
            
            ItemStack eyeStack = new ItemStack(Items.ENDER_EYE);
            
            ParticipationEye.EnderEyeData data = new ParticipationEye.EnderEyeData(player.participating);
            
            data.write(eyeStack);
            
            NBTUtil.flagRestrictMovement(eyeStack);
            
            entity.giveItemStack(eyeStack);
        }
        
        return 0;
    }
}

class StartTicker implements Tickable {
    private static final int NETHER_LAVA_HEIGHT = 32;
    final Shadow shadow;
    
    // We give ~3s for the server to catch up
    int ticksLeft = 13 * 20;
    
    StartTicker(Shadow shadow) {
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

        shadow.addTickable(new GracePeriodTicker(shadow));
        
        for (IndirectPlayer player : shadow.getOnlinePlayers()) {
            if (player.role == null) {
                shadow.ERROR("Null role chosen");
                shadow.resetState();
                return;
            }
            
            if (player.role.getFaction() == Faction.SPECTATOR) {
                player.getPlayerOrThrow().changeGameMode(GameMode.SPECTATOR);
            } else {
                player.getPlayerOrThrow().changeGameMode(GameMode.SURVIVAL);
            }
            
            player.clearPlayerData(CancelPredicates.NEVER_CANCEL);
            
            player.role.init();
            player.modifiers.forEach(Modifier::init);
            player.sendMessage(TextUtil.gray("Your modifiers: ").append(
                player.modifiers.isEmpty() ? TextUtil.error("None") : Texts.join(
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
        
        shadow.init();
        
        shadow.saveAsync();
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
                int y = this.shadow.random.nextBetween(NETHER_LAVA_HEIGHT, nether.getHeight());
                int z = (int) (center.getZ() * netherScaleFactor) + this.shadow.random.nextBetween(-radius, radius);
                
                pos.set(x, y, z);
                
                BlockState state;
                
                while ((state = nether.getBlockState(pos)).isAir()) { pos.move(Direction.DOWN); }
                
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
}


class GracePeriodTicker implements Tickable {
    final Shadow shadow;
    
    int ticksLeft = 0;
    
    GracePeriodTicker(Shadow shadow) {
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
        shadow.getServer().setPvpEnabled(true);
        for (IndirectPlayer player : this.shadow.getOnlinePlayers()) {
            player.sendMessageNow(
                Text.literal("The grace period has ended!").styled(style -> style.withColor(Formatting.GOLD))
            );
        }
    }
    
    @Override
    public boolean shouldEnd() {
        return ticksLeft <= 0;
    }
}
