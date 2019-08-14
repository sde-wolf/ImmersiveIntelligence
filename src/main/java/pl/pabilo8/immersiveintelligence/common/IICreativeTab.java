package pl.pabilo8.immersiveintelligence.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.immersiveintelligence.ImmersiveIntelligence;
import pl.pabilo8.immersiveintelligence.common.blocks.types.IIBlockTypes_MetalDecoration;

/**
 * Created by Pabilo8 on 2019-05-07.
 */
public class IICreativeTab extends CreativeTabs
{
	public IICreativeTab(String name)
	{
		super(name);
	}

	@Override
	public ItemStack getTabIconItem()
	{
		return null;
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return new ItemStack(CommonProxy.block_metal_decoration, 1, IIBlockTypes_MetalDecoration.COIL_DATA.ordinal());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list)
	{
		super.displayAllRelevantItems(list);

		addFluidBucket(ImmersiveIntelligence.proxy.fluid_ink_black, list);
		addFluidBucket(ImmersiveIntelligence.proxy.fluid_ink_cyan, list);
		addFluidBucket(ImmersiveIntelligence.proxy.fluid_ink_magenta, list);
		addFluidBucket(ImmersiveIntelligence.proxy.fluid_ink_yellow, list);
		addFluidBucket(ImmersiveIntelligence.proxy.fluid_etching_acid, list);
	}

	public void addFluidBucket(Fluid fluid, NonNullList list)
	{
		UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
		ItemStack stack = new ItemStack(bucket);
		FluidStack fs = new FluidStack(fluid, bucket.getCapacity());
		IFluidHandlerItem fluidHandler = new FluidBucketWrapper(stack);
		if(fluidHandler.fill(fs, true)==fs.amount)
		{
			list.add(fluidHandler.getContainer());
		}
	}
}
