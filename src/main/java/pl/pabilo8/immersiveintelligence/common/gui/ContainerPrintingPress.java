package pl.pabilo8.immersiveintelligence.common.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pl.pabilo8.immersiveintelligence.common.blocks.multiblocks.metal.TileEntityPrintingPress;

/**
 * Created by Pabilo8 on 10-07-2019.
 */
public class ContainerPrintingPress extends ContainerIEBase
{
	public ContainerPrintingPress(InventoryPlayer inventoryPlayer, TileEntityPrintingPress tile)
	{
		super(inventoryPlayer, tile);
		//Empty pages slot

		this.addSlotToContainer(new Slot(this.inv, 0, 13, 39)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return Utils.compareToOreName(stack, "pageEmpty");
			}
		});

		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 88, 39));

		//Fluid Container Slots

		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 147, 21, 0));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 3, 147, 57, 0));

		this.slotCount = tile.getInventory().size();
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 86+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 144));
	}
}
