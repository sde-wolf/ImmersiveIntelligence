package pl.pabilo8.immersiveintelligence.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import pl.pabilo8.immersiveintelligence.ImmersiveIntelligence;
import pl.pabilo8.immersiveintelligence.client.model.multiblock.metal.ModelRadioStation;
import pl.pabilo8.immersiveintelligence.common.blocks.multiblocks.metal.TileEntityRadioStation;

/**
 * Created by Pabilo8 on 21-06-2019.
 */
public class RadioStationRenderer extends TileEntitySpecialRenderer<TileEntityRadioStation>
{
	private static ModelRadioStation model = new ModelRadioStation();

	private static String texture = ImmersiveIntelligence.MODID+":textures/blocks/multiblock/radio_station.png";

	@Override
	public void render(TileEntityRadioStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(te!=null&&!te.isDummy())
		{
			ClientUtils.bindTexture(texture);
			GlStateManager.pushMatrix();
			GlStateManager.translate((float)x+3, (float)y-2, (float)z+2);
			GlStateManager.rotate(180F, 0F, 1F, 0F);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

			if(te.hasWorld())
			{
				GlStateManager.translate(0f, 1f, 1f);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
			}

			model.getBlockRotation(te.facing, model);
			model.render();

			GlStateManager.popMatrix();

		}
	}
}
