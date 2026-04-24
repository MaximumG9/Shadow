package com.maximumg9.shadow.ducks;

import com.maximumg9.shadow.util.FakeStructureWorldAccess;
import net.minecraft.structure.StrongholdGenerator;
import org.spongepowered.asm.mixin.Unique;

public interface StrongholdMixinDucks {
    interface StrongholdStartMixinDuck {
        @Unique
        void shadowFabric$setFakeStructureWorld(FakeStructureWorldAccess fakeAccess);
        @Unique
        FakeStructureWorldAccess shadowFabric$getFakeStructureWorld();
    }

    interface PieceMixinDuck {
        @Unique
        StrongholdGenerator.Piece shadowFabric$getPreviousPiece();
        @Unique
        void shadowFabric$setPreviousPiece(StrongholdGenerator.Piece previousPiece);
    }
}
