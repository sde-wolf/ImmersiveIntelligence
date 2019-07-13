package pl.pabilo8.immersiveintelligence.common.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.pabilo8.immersiveintelligence.ImmersiveIntelligence;
import pl.pabilo8.immersiveintelligence.common.CommonProxy;
import pl.pabilo8.immersiveintelligence.common.blocks.types.IIBlockTypes_MetalDevice;

/**
 * Created by Pabilo8 on 2019-06-01.
 */
public class EntityMinecartCrateSteel extends EntityMinecartContainer
{
	public EntityMinecartCrateSteel(World worldIn)
	{
		super(worldIn);
	}

	public EntityMinecartCrateSteel(World worldIn, double x, double y, double z)
	{
		super(worldIn, x, y, z);
	}

	public static void registerFixesMinecartChest(DataFixer fixer)
	{
		EntityMinecartContainer.addDataFixers(fixer, net.minecraft.entity.item.EntityMinecartChest.class);
	}

	@Override
	public void killMinecart(DamageSource source)
	{
		//super.killMinecart(source);

		if(!world.isRemote&&this.world.getGameRules().getBoolean("doEntityDrops"))
		{
			Item drop = Item.getItemFromBlock(CommonProxy.block_metal_device);
			ItemStack drop2 = new ItemStack(drop, 1, IIBlockTypes_MetalDevice.METAL_CRATE.getMeta());

			IItemHandler cap = drop2.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

			for(int i = 0; i < itemHandler.getSlots(); i++)
			{
				ItemStack stack = itemHandler.extractItem(i, itemHandler.getStackInSlot(i).getCount(), false);
				cap.insertItem(i, stack, false);
			}

			entityDropItem(drop2, 1);
		}
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory()
	{
		return 27;
	}

	@Override
	public Type getType()
	{
		return Type.CHEST;
	}

	@Override
	public IBlockState getDefaultDisplayTile()
	{
		return CommonProxy.block_metal_device.getStateFromMeta(IIBlockTypes_MetalDevice.METAL_CRATE.getMeta());
	}

	@Override
	public int getDefaultDisplayTileOffset()
	{
		return 8;
	}

	@Override
	public String getGuiID()
	{
		return ImmersiveIntelligence.MODID+":steel_crate";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
	{
		this.addLoot(playerIn);
		return new ContainerChest(playerInventory, this, playerIn);
	}

	@Override
	public void setDead()
	{
		this.isDead = true;
	}
}
