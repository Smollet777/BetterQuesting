package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import adv_director.api.network.IPacketHandler;
import adv_director.blocks.TileSubmitStation;
import adv_director.network.PacketTypeNative;

public class PktHandlerTileEdit implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.EDIT_STATION.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		NBTTagCompound tileData = data.getCompoundTag("tile");
		TileEntity tile = sender.worldObj.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		
		if(tile != null && tile instanceof TileSubmitStation)
		{
			((TileSubmitStation)tile).SyncTile(tileData);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}