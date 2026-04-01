
package matteroverdrive.client.render.tileentity;

import matteroverdrive.machines.replicator.TileEntityMachineReplicator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class TileEntityRendererReplicator extends TileEntitySpecialRenderer<TileEntityMachineReplicator> {
	EntityItem itemEntity;

	@Override
	public void render(TileEntityMachineReplicator replicator, double x, double y, double z, float ticks,
			int destoryStage, float a) {
		if (!replicator.shouldRender())
			return;
		GlStateManager.pushMatrix();
		renderItem(replicator, x, y, z);
		GlStateManager.popMatrix();
	}

	private void renderItem(TileEntityMachineReplicator replicator, double x, double y, double z) {
		ItemStack ghost = replicator.getGhostItem();
		boolean isAnimating = !ghost.isEmpty();
		ItemStack stack = isAnimating ? ghost : replicator.getStackInSlot(replicator.OUTPUT_SLOT_ID);

		if (stack.isEmpty()) return;

		if (itemEntity == null) {
			itemEntity = new EntityItem(replicator.getWorld(), x, y, z, stack);
		} else if (!ItemStack.areItemStacksEqual(itemEntity.getItem(), stack)) {
			itemEntity.setItem(stack);
		}
		itemEntity.hoverStart = (float) (Math.PI / 2);

		if (!isAnimating) {
			Minecraft.getMinecraft().getRenderManager().renderEntity(
					itemEntity, x + 0.5d, y + 0.05, z + 0.5, 0, 0, false);
			return;
		}

		float progress = replicator.getReplicationProgress();
		float alpha = progress * progress * (3.0f - 2.0f * progress); // smoothstep 0→1

		// Use GL_CONSTANT_ALPHA (0x8003) blend mode so alpha is fully independent of vertex colors.
		//
		// Two-pass rendering to prevent back-faces showing through semi-transparent front faces:
		//   Pass 1: prime depth buffer with all geometry, suppress color output.
		//   Pass 2: blend with constant alpha. MC's GL_LEQUAL depth func (active during TESRs) means
		//           back faces (greater depth than primed front faces) fail the depth test → hidden.
		GL11.glColorMask(false, false, false, false);
		Minecraft.getMinecraft().getRenderManager().renderEntity(
				itemEntity, x + 0.5d, y + 0.05, z + 0.5, 0, 0, false);
		GL11.glColorMask(true, true, true, true);

		GlStateManager.enableBlend();
		GL14.glBlendColor(0.0f, 0.0f, 0.0f, alpha);
		GL11.glBlendFunc(0x8003, 0x8004); // GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA
		GlStateManager.disableAlpha(); // disable GL_ALPHA_TEST so low-alpha fragments aren't discarded

		Minecraft.getMinecraft().getRenderManager().renderEntity(
				itemEntity, x + 0.5d, y + 0.05, z + 0.5, 0, 0, false);

		// Restore GL state — must use raw GL calls to match how we set them,
		// because GlStateManager's cache doesn't track glBlendFunc/glBlendColor.
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL14.glBlendColor(0.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}
}