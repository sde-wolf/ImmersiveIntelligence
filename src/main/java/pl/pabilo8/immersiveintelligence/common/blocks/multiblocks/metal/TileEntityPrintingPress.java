package pl.pabilo8.immersiveintelligence.common.blocks.multiblocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import pl.pabilo8.immersiveintelligence.Config.IIConfig.Machines.PrintingPress;
import pl.pabilo8.immersiveintelligence.ImmersiveIntelligence;
import pl.pabilo8.immersiveintelligence.api.IBooleanAnimatedPartsBlock;
import pl.pabilo8.immersiveintelligence.api.data.DataPacket;
import pl.pabilo8.immersiveintelligence.api.data.IDataDevice;
import pl.pabilo8.immersiveintelligence.api.data.operators.arithmetic.DataOperationAdd;
import pl.pabilo8.immersiveintelligence.api.data.types.DataPacketTypeInteger;
import pl.pabilo8.immersiveintelligence.api.data.types.DataPacketTypeString;
import pl.pabilo8.immersiveintelligence.common.IIGuiList;
import pl.pabilo8.immersiveintelligence.common.IISounds;
import pl.pabilo8.immersiveintelligence.common.items.ItemIIPrintedPage;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pabilo8 on 28-06-2019.
 */
public class TileEntityPrintingPress extends TileEntityMultiblockMetal<TileEntityPrintingPress, IMultiblockRecipe> implements IDataDevice, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IGuiTile, IBooleanAnimatedPartsBlock, IConveyorAttachable, ISoundTile
{
	public boolean active = false, hasPaper = false;
	public DataPacket dataToPrint = new DataPacket();
	public DataPacket newDataToPrint = new DataPacket();
	public int pagesLeft = 0;
	public int processTimeLeft = 0;
	public int rollerRotation = 0;
	public ItemStack renderStack0, renderStack1;

	public MultiFluidTank[] tanks = new MultiFluidTank[]{new MultiFluidTank(8000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	IItemHandler inventoryHandler = new IEInventoryHandler(4, this, 0, true, true);


	public TileEntityPrintingPress()
	{
		super(MultiblockPrintingPress.instance, new int[]{3, 5, 3}, PrintingPress.energyCapacity, false);
		renderStack0 = new ItemStack(ImmersiveIntelligence.proxy.item_printed_page, 1, 0);
		renderStack1 = new ItemStack(ImmersiveIntelligence.proxy.item_printed_page, 1, 1);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!isDummy())
		{
			if(!descPacket)
			{
				inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 4);
				dataToPrint = new DataPacket();
				dataToPrint.fromNBT(nbt.getCompoundTag("dataToPrint"));
				newDataToPrint = new DataPacket();
				if(nbt.hasKey("newDataToPrint"))
					newDataToPrint.fromNBT(nbt.getCompoundTag("newDataToPrint"));
			}
			tanks[0].readFromNBT(nbt.getCompoundTag("tank"));
			active = nbt.getBoolean("active");
			processTimeLeft = nbt.getInteger("processTimeLeft");
		}
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		super.receiveMessageFromClient(message);
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		super.receiveMessageFromServer(message);
		if(message.hasKey("processTimeLeft"))
			this.processTimeLeft = message.getInteger("processTimeLeft");
		if(message.hasKey("active"))
			this.active = message.getBoolean("active");
		if(message.hasKey("inventory"))
			inventory = Utils.readInventory(message.getTagList("inventory", 10), 4);
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!isDummy())
		{
			if(!descPacket)
			{
				nbt.setTag("inventory", Utils.writeInventory(inventory));
				nbt.setTag("dataToPrint", dataToPrint.toNBT());
				if(newDataToPrint!=null)
					nbt.setTag("newDataToPrint", newDataToPrint.toNBT());
			}
			nbt.setTag("tank", tanks[0].writeToNBT(new NBTTagCompound()));
			nbt.setBoolean("active", active);
			nbt.setInteger("processTimeLeft", processTimeLeft);
		}
	}

	@Override
	public void update()
	{
		super.update();
		NBTTagCompound tag = new NBTTagCompound();

		if(world.isRemote)
		{
			if(processTimeLeft > 0&&active==true)
			{
				processTimeLeft -= 1;
				ImmersiveEngineering.proxy.handleTileSound(IISounds.printing_press, this, active, .5f, 1);
				rollerRotation += 6;
				if(rollerRotation > 360)
					rollerRotation = 0;
			}
		}

		if(world.isRemote||isDummy())
			return;

		boolean update = false;


		if(pagesLeft > 0)
		{
			if(processTimeLeft%(PrintingPress.printTime/4)==0)
				update = true;

			if(processTimeLeft > 0&&energyStorage.getEnergyStored() >= PrintingPress.energyUsage)
			{
				energyStorage.extractEnergy(PrintingPress.energyUsage, false);
				processTimeLeft -= 1;

				if(processTimeLeft%10==0)
				{
					update = true;
					active = true;
				}
			}
			else if(!inventory.get(0).isEmpty()&&processTimeLeft < 1)
			{
				if(newDataToPrint!=null)
				{
					dataToPrint = newDataToPrint;
					newDataToPrint = null;
				}
				active = true;
				hasPaper = true;
				inventoryHandler.extractItem(0, 1, false);
				processTimeLeft = PrintingPress.printTime;
				update = true;
			}
			else
				active = false;

			if(processTimeLeft < 1&&hasPaper)
			{
				onProcessFinishButInYellow();
				update = true;
				hasPaper = false;
				world.playSound(null, getBlockPosForPos(2), IISounds.paper_eject, SoundCategory.BLOCKS, .3F, 1);

			}
			tag.setTag("inventory", Utils.writeInventory(inventory));

		}
		else
			active = false;

		if(update)
		{
			tag.setInteger("processTimeLeft", processTimeLeft);
			tag.setBoolean("active", active);
		}

		if(inventory.get(2).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)&&inventory.get(2).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).getTankProperties()[0].getContents()!=null)
		{
			String fname = inventory.get(2).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).drain(1000, false).getFluid().getName();

			if(fname.equals("ink")||fname.equals("ink_cyan")||fname.equals("ink_magenta")||fname.equals("ink_yellow"))
			{
				int amount_prev = tanks[0].getFluidAmount();
				ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(2), inventory.get(3), null);
				if(amount_prev!=tanks[0].getFluidAmount())
				{
					if(!inventory.get(3).isEmpty()&&OreDictionary.itemMatches(inventory.get(3), emptyContainer, true))
						inventory.get(3).grow(emptyContainer.getCount());
					else if(inventory.get(3).isEmpty())
						inventory.set(3, emptyContainer.copy());
					inventory.get(2).shrink(1);
					if(inventory.get(2).getCount() <= 0)
						inventory.set(2, ItemStack.EMPTY);

					update = true;
					writeToNBT(tag);
				}
			}
		}

		if(update)
		{
			//this.markDirty();
			//this.markContainingBlockForUpdate(null);
			ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32));
		}


	}

	void onProcessFinishButInYellow()
	{
		//Name is a reference to the meme 'yes / yes but in yellow' (probably a bad practice)
		pagesLeft -= 1;
		processTimeLeft = 0;


		if(pagesLeft < 1)
			active = false;

		DataPacketTypeString item_type = (DataPacketTypeString)DataOperationAdd.getVarInType(DataPacketTypeString.class, dataToPrint.getPacketVariable('t'), dataToPrint);

		switch(item_type.value)
		{
			case "text":
			{
				DataPacketTypeString toPrint = (DataPacketTypeString)DataOperationAdd.getVarInType(DataPacketTypeString.class, dataToPrint.getPacketVariable('s'), dataToPrint);
				String printedChars = "";

				int black_amount = 0, cyan_amount = 0, magenta_amount = 0, yellow_amount = 0;

				for(FluidStack stack : tanks[0].fluids)
				{
					if(stack.getFluid().getName().equals("ink"))
					{
						black_amount += stack.amount;
					}
					else if(stack.getFluid().getName().equals("ink_cyan"))
					{
						cyan_amount += stack.amount;
					}
					else if(stack.getFluid().getName().equals("ink_magenta"))
					{
						magenta_amount += stack.amount;
					}
					else if(stack.getFluid().getName().equals("ink_yellow"))
					{
						yellow_amount += stack.amount;
					}
				}

				ImmersiveIntelligence.logger.info("cyan: "+cyan_amount);
				ImmersiveIntelligence.logger.info("magenta: "+magenta_amount);
				ImmersiveIntelligence.logger.info("yellow: "+yellow_amount);

				int black_amount_start = black_amount, cyan_amount_start = cyan_amount, magenta_amount_start = magenta_amount, yellow_amount_start = yellow_amount;

				ArrayList<Float> black_cost = new ArrayList<>(),
						cyan_cost = new ArrayList<>(),
						magenta_cost = new ArrayList<>(),
						yellow_cost = new ArrayList<>();

				black_cost.add(1.0f);
				cyan_cost.add(0.0f);
				magenta_cost.add(0.0f);
				yellow_cost.add(0.0f);

				int charnum = 0, shouldstartfrom = 0, tag_endings_needed = 0;

				for(char c : toPrint.value.toCharArray())
				{
					charnum += 1;
					if(charnum < shouldstartfrom)
						continue;

					if(c=='<')
					{
						//printedChars+="<";
						String fragment = toPrint.value.substring(charnum-1, charnum+14);
						if(fragment.substring(0, 8).equals("<hexcol="))
						{
							black_cost.get(black_cost.size()-1);

							if(black_cost.get(black_cost.size()-1)*PrintingPress.printInkUsage > black_amount)
							{
								black_cost.set(black_cost.size()-1, (float)black_amount/(float)PrintingPress.printInkUsage);
							}

							if(cyan_cost.get(cyan_cost.size()-1)*PrintingPress.printInkUsage > cyan_amount)
							{
								cyan_cost.set(cyan_cost.size()-1, (float)cyan_amount/(float)PrintingPress.printInkUsage);
							}

							if(magenta_cost.get(magenta_cost.size()-1)*PrintingPress.printInkUsage > magenta_amount)
							{
								magenta_cost.set(magenta_cost.size()-1, (float)magenta_amount/(float)PrintingPress.printInkUsage);
							}

							if(yellow_cost.get(yellow_cost.size()-1)*PrintingPress.printInkUsage > yellow_amount)
							{
								yellow_cost.set(yellow_cost.size()-1, (float)yellow_amount/(float)PrintingPress.printInkUsage);
							}


							int color = Integer.parseInt(fragment.substring(8, 14), 16);
							ImmersiveIntelligence.logger.info("(10)"+color+" = "+"(16)"+fragment.substring(8, 14));
							Color col = new Color(color);
							ImmersiveIntelligence.logger.info("new RGB: "+col.getRed()+" "+col.getGreen()+" "+col.getBlue());
							int[] colors = pl.pabilo8.immersiveintelligence.api.Utils.rgbToCmyk(col.getRed(), col.getGreen(), col.getBlue());
							ImmersiveIntelligence.logger.info("new CMYK: "+colors[0]/255f+" "+colors[1]/255f+" "+colors[2]/255f+" "+colors[3]/255f);
							cyan_cost.add(((float)colors[0])/255f);
							yellow_cost.add(((float)colors[1])/255f);
							magenta_cost.add(((float)colors[2])/255f);
							black_cost.add(((float)colors[3])/255f);

							ImmersiveIntelligence.logger.info("new C: "+cyan_cost.get(cyan_cost.size()-1));


							tag_endings_needed += 1;
							shouldstartfrom = charnum+15;
							printedChars += "<hexcol="+fragment.substring(8, 14)+":";

						}
					}
					else if(tag_endings_needed > 0&&c=='>')
					{
						printedChars += ">";
						tag_endings_needed -= 1;
						black_cost.remove(black_cost.size()-1);
						cyan_cost.remove(cyan_cost.size()-1);
						magenta_cost.remove(magenta_cost.size()-1);
						yellow_cost.remove(yellow_cost.size()-1);
					}
					else
					{
						if(black_cost.get(black_cost.size()-1)==0&&cyan_cost.get(cyan_cost.size()-1)==0&&magenta_cost.get(magenta_cost.size()-1)==0&&yellow_cost.get(yellow_cost.size()-1)==0)
						{
							printedChars += " ";
							continue;
						}
						if(black_cost.get(black_cost.size()-1)*PrintingPress.printInkUsage <= black_amount&&
								cyan_cost.get(cyan_cost.size()-1)*PrintingPress.printInkUsage <= cyan_amount&&
								magenta_cost.get(magenta_cost.size()-1)*PrintingPress.printInkUsage <= magenta_amount&&
								yellow_cost.get(yellow_cost.size()-1)*PrintingPress.printInkUsage <= yellow_amount)
						{
							printedChars += c;
							black_amount -= black_cost.get(black_cost.size()-1)*PrintingPress.printInkUsage;
							cyan_amount -= cyan_cost.get(cyan_cost.size()-1)*PrintingPress.printInkUsage;
							magenta_amount -= magenta_cost.get(magenta_cost.size()-1)*PrintingPress.printInkUsage;
							yellow_amount -= yellow_cost.get(yellow_cost.size()-1)*PrintingPress.printInkUsage;

							ImmersiveIntelligence.logger.info("CMYK color:"+black_cost.get(black_cost.size()-1)+" "+cyan_cost.get(cyan_cost.size()-1)+" "+magenta_cost.get(magenta_cost.size()-1)+" "+yellow_cost.get(yellow_cost.size()-1));

							continue;
						}
						else
						{
							if(black_cost.get(black_cost.size()-1)*PrintingPress.printInkUsage > black_amount)
							{
								black_cost.set(black_cost.size()-1, (float)black_amount/(float)PrintingPress.printInkUsage);
							}

							if(cyan_cost.get(cyan_cost.size()-1)*PrintingPress.printInkUsage > cyan_amount)
							{
								cyan_cost.set(cyan_cost.size()-1, (float)cyan_amount/(float)PrintingPress.printInkUsage);
							}

							if(magenta_cost.get(magenta_cost.size()-1)*PrintingPress.printInkUsage > magenta_amount)
							{
								magenta_cost.set(magenta_cost.size()-1, (float)magenta_amount/(float)PrintingPress.printInkUsage);
							}

							if(yellow_cost.get(yellow_cost.size()-1)*PrintingPress.printInkUsage > yellow_amount)
							{
								yellow_cost.set(yellow_cost.size()-1, (float)yellow_amount/(float)PrintingPress.printInkUsage);
							}

							if(black_cost.get(black_cost.size()-1)==0&&cyan_cost.get(cyan_cost.size()-1)==0&&magenta_cost.get(magenta_cost.size()-1)==0&&yellow_cost.get(yellow_cost.size()-1)==0)
							{
								printedChars += " ";
								continue;
							}
							else
							{
								printedChars += "> <hexcol=";
								int[] colors = pl.pabilo8.immersiveintelligence.api.Utils.cmykToRgb(Math.round(cyan_cost.get(cyan_cost.size()-1)*255), Math.round(magenta_cost.get(magenta_cost.size()-1)*255), Math.round(yellow_cost.get(yellow_cost.size()-1)*255), Math.round(black_cost.get(black_cost.size()-1)*255));
								printedChars += String.format("%02x%02x%02x:", Math.round(colors[0]), Math.round(colors[1]), Math.round(colors[2]));
								//char randomchar = DataPacket.varCharacters[(int)Math.round(Math.random()*DataPacket.varCharacters.length)];
								//ImmersiveIntelligence.logger.info("RandomChar: "+randomchar);
								//printedChars += randomchar;
							}

						}
					}
				}
				while(tag_endings_needed > 0)
				{
					tag_endings_needed -= 1;
					printedChars += ">";
				}

				tanks[0].drain(FluidRegistry.getFluidStack("ink", black_amount_start-black_amount), true);
				tanks[0].drain(FluidRegistry.getFluidStack("ink_cyan", cyan_amount_start-cyan_amount), true);
				tanks[0].drain(FluidRegistry.getFluidStack("ink_magenta", magenta_amount_start-magenta_amount), true);
				tanks[0].drain(FluidRegistry.getFluidStack("ink_yellow", yellow_amount_start-yellow_amount), true);

				//Finally! lol (i wrote this all without any debugging, so i count on you bug reporters ^^)
				ItemStack stack = new ItemStack(ImmersiveIntelligence.proxy.item_printed_page, 1, 1);
				ImmersiveIntelligence.proxy.item_printed_page.setText(stack, printedChars);

				stack = inventoryHandler.insertItem(1, stack, false);
				if(!stack.isEmpty())
					Utils.dropStackAtPos(world, getBlockPosForPos(2), stack, this.facing);
			}
			break;
			case "blueprint":
			{
				inventoryHandler.insertItem(1, new ItemStack(ImmersiveIntelligence.proxy.item_printed_page, 1, 3), false);
			}
			break;
			case "code":
			{
				inventoryHandler.insertItem(1, new ItemStack(ImmersiveIntelligence.proxy.item_printed_page, 1, 2), false);
			}
			break;
			default:
			{
				inventoryHandler.insertItem(1, new ItemStack(ImmersiveIntelligence.proxy.item_printed_page, 1, 0), false);
			}
			break;
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0, 0, 0, 0, 0, 0};
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{34};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{

	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{

	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(slot==0)
			return Utils.compareToOreName(stack, "pageEmpty");
		else if(slot==1)
			return Utils.compareToOreName(stack, "pageWritten");
		else
			return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return slot==1?12: 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[]{1};
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityPrintingPress master = this.master();
		if(master!=null)
		{
			if(pos==29&&side.getAxis()==Axis.Y)
				return master.tanks;
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		String fname = resource.getFluid().getName();
		if(pos==29&&side.getAxis()==Axis.Y&&(fname.equals("ink")||fname.equals("ink_cyan")||fname.equals("ink_magenta")||fname.equals("ink_yellow")))
		{
			TileEntityPrintingPress master = this.master();
			return !(master==null||master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity());
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return (side.getAxis()==Axis.Y&&iTank==0);
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
	}

	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}

	@Override
	public void onSend()
	{

	}

	@Override
	public void onReceive(DataPacket packet)
	{
		if(pos==6)
		{
			master().onReceive(packet);
		}

		if(!this.isDummy()&&energyStorage.getEnergyStored() >= PrintingPress.energyUsage)
		{
			energyStorage.extractEnergy(PrintingPress.energyUsage, false);
			this.pagesLeft = ((DataPacketTypeInteger)DataOperationAdd.getVarInType(DataPacketTypeInteger.class, packet.getPacketVariable('c'), packet)).value;
			this.newDataToPrint = packet;
		}
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List list = new ArrayList<AxisAlignedBB>();

		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return IIGuiList.GUI_PRINTING_PRESS;
	}

	@Nullable
	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master()!=null;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityPrintingPress master = master();
			if(master==null)
				return null;
			return (T)this.inventoryHandler;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void onGuiOpened(EntityPlayer player, boolean clientside)
	{
		if(!clientside)
		{
			NBTTagCompound tag = new NBTTagCompound();
			ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32));
		}
	}

	@Override
	public void onAnimationChangeClient(boolean state, int part)
	{
		/*if (part==0)
			isDoorOpened=state;*/
	}

	@Override
	public void onAnimationChangeServer(boolean state, int part)
	{
		/*if (part==0)
			isDoorOpened=state;*/
		//IIPacketHandler.INSTANCE.sendToAllAround(new MessageBooleanAnimatedPartsSync(isDoorOpened,1, getPos()),pl.pabilo8.immersiveintelligence.api.Utils.targetPointFromPos(this.getPos(),this.world,32));
	}

	@Override
	public EnumFacing[] sigOutputDirections()
	{
		if(pos==2)
			return new EnumFacing[]{mirrored?facing.rotateYCCW(): facing.rotateY()};
		return new EnumFacing[0];
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return active;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(pos==3&&!world.isRemote&&entity!=null&&!entity.isDead&&entity instanceof EntityItem&&!((EntityItem)entity).getItem().isEmpty())
		{
			TileEntityPrintingPress master = master();
			if(master==null)
				return;
			ItemStack stack = ((EntityItem)entity).getItem();
			if(stack.getItem() instanceof ItemIIPrintedPage&&stack.getMetadata()==0)
			{
				//Gib paper plox
				stack = inventoryHandler.insertItem(0, stack, false);
				if(stack.getCount() <= 0)
					entity.setDead();
			}

		}
	}
}
