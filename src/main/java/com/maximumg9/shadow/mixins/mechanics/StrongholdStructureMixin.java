package com.maximumg9.shadow.mixins.mechanics;

import com.llamalad7.mixinextras.sugar.Local;
import com.maximumg9.shadow.ducks.StrongholdMixinDucks;
import com.maximumg9.shadow.util.FakeStructureWorldAccess;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.StrongholdStructure;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(StrongholdStructure.class)
public class StrongholdStructureMixin {
    @Inject(method = "addPieces",at= @At(value = "INVOKE", target = "Lnet/minecraft/structure/StructurePiecesCollector;addPiece(Lnet/minecraft/structure/StructurePiece;)V",ordinal = 0))
    private static void markStructureStart(StructurePiecesCollector collector, Structure.Context context, CallbackInfo ci, @Local StrongholdGenerator.Start start) {
        if(context.world() instanceof FakeStructureWorldAccess fakeAccess) {
            ((StrongholdMixinDucks.StrongholdStartMixinDuck) start)
                .shadowFabric$setFakeStructureWorld(fakeAccess);
        }
    }


    @Mixin(StrongholdGenerator.Start.class)
    private static class StrongholdStartMixin implements StrongholdMixinDucks.StrongholdStartMixinDuck {
        @Unique
        public FakeStructureWorldAccess fakeWorldAccess;
        @Unique
        @Override
        public void shadowFabric$setFakeStructureWorld(FakeStructureWorldAccess fakeAccess) {
            this.fakeWorldAccess = fakeAccess;
        }

        @Unique
        @Override
        public FakeStructureWorldAccess shadowFabric$getFakeStructureWorld() {
            return this.fakeWorldAccess;
        }
    }

    @Mixin(StrongholdGenerator.Piece.class)
    private static class StrongholdPieceMixin implements StrongholdMixinDucks.PieceMixinDuck {
        @Unique
        StrongholdGenerator.Piece previousPiece;
        @Unique
        @Override
        public StrongholdGenerator.Piece shadowFabric$getPreviousPiece() {
            return this.previousPiece;
        }
        @Unique
        @Override
        public void shadowFabric$setPreviousPiece(StrongholdGenerator.Piece previousPiece) {
            this.previousPiece = previousPiece;
        }

        @Inject(method = "fillForwardOpening",at=@At("RETURN"))
        public void markAsPreviousPieceForwardFill(StrongholdGenerator.Start start, StructurePiecesHolder holder, Random random, int leftRightOffset, int heightOffset, CallbackInfoReturnable<StructurePiece> cir) {
            if(cir.getReturnValue() == null) return;
            ((StrongholdMixinDucks.PieceMixinDuck) cir.getReturnValue())
                .shadowFabric$setPreviousPiece(
                    (StrongholdGenerator.Piece) (Object) this
                );
        }

        @Inject(method = "fillNWOpening",at=@At("RETURN"))
        public void markAsPreviousPieceNorthwestFill(StrongholdGenerator.Start start, StructurePiecesHolder holder, Random random, int leftRightOffset, int heightOffset, CallbackInfoReturnable<StructurePiece> cir) {
            if(cir.getReturnValue() == null) return;
            ((StrongholdMixinDucks.PieceMixinDuck) cir.getReturnValue())
                .shadowFabric$setPreviousPiece(
                    (StrongholdGenerator.Piece) (Object) this
                );
        }

        @Inject(method = "fillSEOpening",at=@At("RETURN"))
        public void markAsPreviousPieceSoutheastFill(StrongholdGenerator.Start start, StructurePiecesHolder holder, Random random, int leftRightOffset, int heightOffset, CallbackInfoReturnable<StructurePiece> cir) {
            if(cir.getReturnValue() == null) return;
            ((StrongholdMixinDucks.PieceMixinDuck) cir.getReturnValue())
                .shadowFabric$setPreviousPiece(
                    (StrongholdGenerator.Piece) (Object) this
                );
        }
    }

    @Mixin(StrongholdGenerator.PortalRoom.class)
    private static class PortalRoomMixin {
        @Inject(method = "fillOpenings",at=@At("HEAD"))
        public void writePath(StructurePiece start, StructurePiecesHolder holder, Random random, CallbackInfo ci) {
            FakeStructureWorldAccess fakeWorld = ((StrongholdMixinDucks.StrongholdStartMixinDuck)start)
                .shadowFabric$getFakeStructureWorld();
            if(fakeWorld != null) {
                List<StrongholdGenerator.Piece> piecePath = new ArrayList<>();
                piecePath.add((StrongholdGenerator.Piece) (Object) this);
                while(piecePath.getLast() != start) {
                    piecePath.add(
                        ((StrongholdMixinDucks.PieceMixinDuck) piecePath.getLast())
                            .shadowFabric$getPreviousPiece()
                    );
                }

                fakeWorld.setPathFromPortalToStart(piecePath);
            }
        }

        @Inject(method = "generate",at= @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/structure/StrongholdGenerator$PortalRoom;addBlock(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/block/BlockState;IIILnet/minecraft/util/math/BlockBox;)V"))
        public void removeEyes(
            StructureWorldAccess world,
            StructureAccessor structureAccessor,
            ChunkGenerator chunkGenerator,
            Random random,
            BlockBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot,
            CallbackInfo ci,
            @Local boolean[] eyeFilled
        ) {
            Arrays.fill(eyeFilled, false);
        }
    }
}
