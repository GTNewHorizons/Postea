package com.colen.postea.mixins.early;

import com.colen.postea.API.TileEntityReplacementManager;
import com.colen.postea.API.TileTransformer;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(TileEntity.class)
@SuppressWarnings("unused")
public class MixinTileEntity {

    @Shadow
    private static Logger logger;

    @Shadow
    private static Map nameToClassMap;

    @Shadow
    protected World worldObj;


    /**
     * @author Colen
     * @reason Stuff
     */
    @Overwrite
    @SuppressWarnings("unused")
    public static TileEntity createAndLoadEntity(NBTTagCompound nbt) {
        TileEntity tileentity = null;

        Class oclass = null;
        try
        {
            String tileEntityStringName = nbt.getString("id");
            TileTransformer tileTransformer = TileEntityReplacementManager.getTileReplacementTransformer(tileEntityStringName);


            if (tileTransformer == null) {
                oclass = (Class)nameToClassMap.get(tileEntityStringName);
            } else {
                oclass = tileTransformer.getTileClass();
                tileTransformer.modifyNBT(nbt);
            }

            if (oclass != null)
            {
                tileentity = (TileEntity)oclass.newInstance();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (tileentity != null)
        {
            try
            {
                tileentity.readFromNBT(nbt);
            }
            catch (Exception ex)
            {
                FMLLog.log(Level.ERROR, ex,
                    "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                    nbt.getString("id"), oclass.getName());
                tileentity = null;
            }
        }
        else
        {
            logger.warn("Skipping BlockEntity with id " + nbt.getString("id"));
        }

        return tileentity;
    }

}
