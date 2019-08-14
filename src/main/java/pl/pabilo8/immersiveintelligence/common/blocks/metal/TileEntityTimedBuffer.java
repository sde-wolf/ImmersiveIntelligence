package pl.pabilo8.immersiveintelligence.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import pl.pabilo8.immersiveintelligence.ImmersiveIntelligence;
import pl.pabilo8.immersiveintelligence.api.data.DataPacket;
import pl.pabilo8.immersiveintelligence.api.data.IDataConnector;
import pl.pabilo8.immersiveintelligence.api.data.IDataDevice;
import pl.pabilo8.immersiveintelligence.api.data.types.DataPacketTypeInteger;

import javax.annotation.Nonnull;

/**
 * Created by Pabilo8 on 2019-05-17.
 */
public class TileEntityTimedBuffer extends TileEntityIEBase implements IPlayerInteraction, ITickable, IRotationAcceptor, IBlockBounds, IDirectionalTile, IDataDevice
{
	public EnumFacing facing = EnumFacing.NORTH;
	public int timer = 0, maxtimer = 20;
	DataPacket packet = new DataPacket();

	@Override
	public void inputRotation(double rotation, @Nonnull EnumFacing side)
	{
		if(side!=this.facing.getOpposite())
			return;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		timer = nbt.getInteger("timer");
		maxtimer = nbt.getInteger("maxtimer");
		packet = new DataPacket();
		if(nbt.hasKey("packet"))
			packet.fromNBT(nbt.getCompoundTag("packet"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("timer", timer);
		nbt.setInteger("maxtimer", maxtimer);
		nbt.setTag("packet", packet.toNBT());
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		super.receiveMessageFromServer(message);
		if(message.hasKey("timer"))
			timer = message.getInteger("timer");
		if(message.hasKey("maxtimer"))
			maxtimer = message.getInteger("maxtimer");
	}

	@Override
	public void update()
	{

		if(timer > 0&&timer < maxtimer+1)
		{
			timer += 1;
			if(timer%5==0||timer==1)
			{
				ImmersiveIntelligence.logger.info("test");
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("timer", timer);
				ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, nbt), pl.pabilo8.immersiveintelligence.api.Utils.targetPointFromTile(this, 24));
			}
		}
		else if(timer >= maxtimer)
		{
			timer = 0;
			if(!world.isRemote)
			{
				BlockPos newpos = pos.offset(facing);
				if(world.isBlockLoaded(newpos)&&world.getTileEntity(newpos) instanceof IDataConnector&&world.getTileEntity(newpos) instanceof IDirectionalTile)
				{
					IDataConnector con = ((IDataConnector)world.getTileEntity(newpos));
					con.sendPacket(packet);
				}
			}

		}
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0f, 0, 0f, 1f, .875f, 1f};
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return !axis.getAxis().isVertical();
	}

	@Override
	public void onReceive(DataPacket packet)
	{
		this.packet = packet;
		timer = 1;
		if(packet.getPacketVariable('0').getName().equals("integer"))
		{
			maxtimer = ((DataPacketTypeInteger)packet.getPacketVariable('0')).value;

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("maxtimer", maxtimer);
			nbt.setInteger("timer", timer);
			ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, nbt), pl.pabilo8.immersiveintelligence.api.Utils.targetPointFromTile(this, 24));
		}
	}

	@Override
	public void onSend()
	{

	}
}
