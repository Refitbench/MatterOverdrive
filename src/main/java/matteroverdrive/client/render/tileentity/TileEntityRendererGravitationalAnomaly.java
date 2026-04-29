
package matteroverdrive.client.render.tileentity;

import matteroverdrive.Reference;
import matteroverdrive.tile.TileEntityGravitationalAnomaly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileEntityRendererGravitationalAnomaly extends TileEntitySpecialRenderer<TileEntityGravitationalAnomaly> {
	public static final ResourceLocation core = new ResourceLocation(
			Reference.PATH_BLOCKS + "gravitational_anomaly_core.png");
	public static final ResourceLocation anti = new ResourceLocation(
			Reference.PATH_BLOCKS + "anti_gravitational_anomaly_core.png");
	public static final ResourceLocation glow = new ResourceLocation(
			Reference.PATH_BLOCKS + "gravitational_anomaly_glow.png");
	public static final ResourceLocation black = new ResourceLocation(Reference.PATH_BLOCKS + "black.png");

	public TileEntityRendererGravitationalAnomaly() {
	}

	@Override
	public void render(TileEntityGravitationalAnomaly tileEntity, double x, double y, double z, float partialTicks,
			int destroyStage, float alpha) {

		float radius = (float) tileEntity.getEventHorizon();
		renderSphere(tileEntity, x, y, z, radius);
		renderDisk(tileEntity, x, y, z, partialTicks, radius);
	}

	public void renderSphere(TileEntityGravitationalAnomaly tileEntity, double x, double y, double z,
			float visualSize) {
		if (!tileEntity.shouldRender())
			return;
		long time = Minecraft.getMinecraft().world.getWorldTime();
		float speed = 1;
		double resonateSpeed = 0.1;
		double radius = tileEntity.getEventHorizon();
		radius = radius * Math.sin(time * resonateSpeed) * 0.1 + radius * 0.9;

		// Inner
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.rotate(time * speed, 0, 0, 1);
		bindTexture(black);
		GlStateManager.scale(radius, radius, radius);
		drawSphere(0.33f, 8, 8, 0.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();

		// Outer
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.depthMask(false);
		GlStateManager.disableLighting();
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.rotate(time * speed, 0, 1, 0);
		bindTexture(black);
		GlStateManager.scale(radius + 0.5, radius + 0.5, radius + 0.5);
		drawSphere(0.33f, 8, 8, 0.0f, 0.0f, 0.2f, 0.8f);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();

	}

	public void renderDisk(TileEntityGravitationalAnomaly tileEntity, double x, double y, double z, float partialTicks,
			float visualSize) {
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		long time = Minecraft.getMinecraft().world.getWorldTime();
		float speed = 1;
		double resonateSpeed = 0.1;
		double radius = tileEntity.getEventHorizon();
		radius = radius * Math.sin(time * resonateSpeed) * 0.1 + radius * 0.9;

		// Setup
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		//GL11.glDepthMask(true);
		GlStateManager.disableLighting();

		// Translate
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.rotate(time * speed, 0, 1, 0);
		// Assign texture
		this.bindTexture(core);
		GlStateManager.color(1, 0, 0, 1);

		// top render
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(-radius, 0, -radius).tex(0, 0).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		bufferbuilder.pos(-radius, 0, +radius).tex(0, 1).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		bufferbuilder.pos(+radius, 0, +radius).tex(1, 1).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		bufferbuilder.pos(+radius, 0, -radius).tex(1, 0).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		Tessellator.getInstance().draw();

		// bottom render
		GlStateManager.rotate(180, 1, 0, 0);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(-radius, 0, -radius).tex(1, 1).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		bufferbuilder.pos(-radius, 0, +radius).tex(1, 0).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		bufferbuilder.pos(+radius, 0, +radius).tex(0, 0).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		bufferbuilder.pos(+radius, 0, -radius).tex(0, 1).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
		Tessellator.getInstance().draw();

		// Reset
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		//GL11.glDepthMask(false);
		GlStateManager.popMatrix();
	}

	private void drawSphere(float radius, int slices, int stacks, float r, float g, float b, float a) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		for (int i = 0; i < stacks; i++) {
			double phi0 = Math.PI * i / stacks - Math.PI / 2.0;
			double phi1 = Math.PI * (i + 1) / stacks - Math.PI / 2.0;
			double cosPhi0 = Math.cos(phi0), sinPhi0 = Math.sin(phi0);
			double cosPhi1 = Math.cos(phi1), sinPhi1 = Math.sin(phi1);
			float v0 = (float) i / stacks;
			float v1 = (float) (i + 1) / stacks;
			for (int j = 0; j < slices; j++) {
				double theta0 = 2.0 * Math.PI * j / slices;
				double theta1 = 2.0 * Math.PI * (j + 1) / slices;
				double cosTheta0 = Math.cos(theta0), sinTheta0 = Math.sin(theta0);
				double cosTheta1 = Math.cos(theta1), sinTheta1 = Math.sin(theta1);
				float u0 = (float) j / slices;
				float u1 = (float) (j + 1) / slices;
				buf.pos(radius * cosPhi0 * cosTheta0, radius * sinPhi0, radius * cosPhi0 * sinTheta0).tex(u0, v0).color(r, g, b, a).endVertex();
				buf.pos(radius * cosPhi0 * cosTheta1, radius * sinPhi0, radius * cosPhi0 * sinTheta1).tex(u1, v0).color(r, g, b, a).endVertex();
				buf.pos(radius * cosPhi1 * cosTheta1, radius * sinPhi1, radius * cosPhi1 * sinTheta1).tex(u1, v1).color(r, g, b, a).endVertex();
				buf.pos(radius * cosPhi1 * cosTheta0, radius * sinPhi1, radius * cosPhi1 * sinTheta0).tex(u0, v1).color(r, g, b, a).endVertex();
			}
		}
		tess.draw();
	}

}
