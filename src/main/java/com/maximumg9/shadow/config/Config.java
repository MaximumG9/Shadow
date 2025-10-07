package com.maximumg9.shadow.config;

import com.maximumg9.shadow.Shadow;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class Config {
    public final RoleManager roleManager;
    public final ModifierManager modifierManager;
    public final MaxCooldownManager maxCooldownManager;
    private final Path saveFile;
    
    public int worldBorderSize = 150;
    public int roleSlotCount = 25;
    public int overworldEyes = 8;
    public int netherEyes = 8;
    public int netherRoofEyes = 8;
    public Food food = Food.BREAD;
    public int foodAmount = 16;
    public double additionalTimePerTickDuringNight = 1;
    public boolean debug = false;
    public int chatMessageCooldown = 30 * 20;
    public double cullRadius = 18.0;
    public boolean disableChat = false;
    public int disconnectTime = 20 * 60 * 10;
    public int gracePeriodTicks = 20 * 60 * 3;
    public boolean poolShadowRoles = false;
    
    public Config(Shadow shadow, Path saveFile) {
        this.roleManager = new RoleManager(shadow, this);
        this.modifierManager = new ModifierManager(shadow);
        this.maxCooldownManager = new MaxCooldownManager();
        this.saveFile = saveFile;
    }
    private void readNbt(NbtCompound nbt) {
        this.worldBorderSize = nbt.getInt("worldBorderSize");
        this.roleSlotCount = nbt.getInt("roleSlotCount");
        this.overworldEyes = nbt.getInt("overworldEyes");
        this.netherEyes = nbt.getInt("netherEyes");
        this.netherRoofEyes = nbt.getInt("netherRoofEyes");
        this.food = Food.valueOf(nbt.getString("food"));
        this.foodAmount = nbt.getInt("foodAmount");
        this.additionalTimePerTickDuringNight = nbt.getDouble("additionalTimePerTickDuringNight");
        this.debug = nbt.getBoolean("debug");
        this.chatMessageCooldown = nbt.getInt("chatMessageCooldown");
        this.cullRadius = nbt.getDouble("cullRadius");
        this.disableChat = nbt.getBoolean("disableChat");
        this.disconnectTime = nbt.getInt("disconnectTime");
        this.gracePeriodTicks = nbt.getInt("gracePeriodTicks");
        this.poolShadowRoles = nbt.getBoolean("poolShadowRoles");
        
        this.maxCooldownManager.readNbt(nbt.getCompound("maxCooldownManager"));
        this.roleManager.readNbt(nbt.getCompound("roleManager"));
        this.modifierManager.readNbt(nbt.getCompound("modifierManager"));
    }
    private NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("worldBorderSize", this.worldBorderSize);
        nbt.putInt("roleSlotCount", this.roleSlotCount);
        nbt.putInt("overworldEyes", this.overworldEyes);
        nbt.putInt("netherEyes", this.netherEyes);
        nbt.putInt("netherRoofEyes", this.netherRoofEyes);
        nbt.putString("food", this.food.name());
        nbt.putInt("foodAmount", this.foodAmount);
        nbt.putDouble("additionalTimePerTickDuringNight", this.additionalTimePerTickDuringNight);
        nbt.putBoolean("debug", this.debug);
        nbt.putInt("chatMessageCooldown", this.chatMessageCooldown);
        nbt.putDouble("cullRadius", this.cullRadius);
        nbt.putBoolean("disableChat", this.disableChat);
        nbt.putInt("disconnectTime", this.disconnectTime);
        nbt.putInt("gracePeriodTicks", this.gracePeriodTicks);
        nbt.putBoolean("poolShadowRoles", this.poolShadowRoles);
        
        nbt.put("maxCooldownManager", this.maxCooldownManager.writeNbt(new NbtCompound()));
        nbt.put("roleManager", this.roleManager.writeNbt(new NbtCompound()));
        nbt.put("modifierManager", this.modifierManager.writeNbt(new NbtCompound()));
        return nbt;
    }
    public Config copy(Shadow shadow) {
        Config newConfig = new Config(shadow, this.saveFile);
        
        newConfig.readNbt(this.writeNbt(new NbtCompound()));
        
        return newConfig;
    }
    public void load() throws IOException {
        NbtCompound compound = NbtIo.readCompressed(saveFile, new NbtSizeTracker(0xffffffffffffL, 256));
        if (compound == null) throw new FileNotFoundException("Could not find config file");
        this.readNbt(compound);
    }
    public void save() throws IOException {
        NbtCompound data = this.writeNbt(new NbtCompound());
        NbtIo.writeCompressed(data, this.saveFile);
    }
}
