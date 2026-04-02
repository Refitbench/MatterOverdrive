package matteroverdrive.client.render.tileentity;

import matteroverdrive.Reference;
import matteroverdrive.tile.TileEntityMachineChargingStation;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_QUADS;

public class TileEntityRendererChargingStation extends TileEntitySpecialRenderer<TileEntityMachineChargingStation> {

    private static final ResourceLocation PLASMA_COLOR_TEX  = new ResourceLocation(Reference.PATH_FX + "forcefield_plasma.png");
    private static final ResourceLocation PLASMA_DETAIL_TEX = new ResourceLocation(Reference.PATH_FX + "forcefield_plasma_2.png");

    private static final float ROD_OFFSET_X = 0.245f;
    private static final float ROD_BOT_Y    = 0.38f;
    private static final float ROD_TOP_Y    = 1.93f;
    private static final float INNER_RADIUS = 0.11f;
    private static final float OUTER_RADIUS = 0.175f;
    private static final int   SEGMENTS     = 16;
    private static final int   PULSE_COUNT  = 3;

    @Override
    public void render(TileEntityMachineChargingStation machine, double x, double y, double z,
            float partialTicks, int destroyStage, float alpha) {
        if (!machine.isActive()) return;

        long worldTime = machine.getWorld().getWorldTime();
        float t = (worldTime + partialTicks) * 0.02f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        RenderUtils.rotateFromBlock(machine.getWorld(), machine.getPos());
        GlStateManager.translate(0, 0, ROD_OFFSET_X);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_ONE, GL_ONE);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        RenderUtils.disableLightmap();
        GlStateManager.disableCull();

        // Inner sleeve: lightning detail texture scrolling upward
        Minecraft.getMinecraft().renderEngine.bindTexture(PLASMA_DETAIL_TEX);
        GlStateManager.color(0.3f, 0.7f, 1.0f, 1.0f);
        renderCylinder(INNER_RADIUS, ROD_BOT_Y, ROD_TOP_Y, t, 1.0f);

        // Outer sleeve: color plasma texture scrolling downward slower
        Minecraft.getMinecraft().renderEngine.bindTexture(PLASMA_COLOR_TEX);
        GlStateManager.color(0.1f, 0.4f, 0.9f, 1.0f);
        renderCylinder(OUTER_RADIUS, ROD_BOT_Y, ROD_TOP_Y, -t * 0.35f, 1.0f);

        // Ascending pulse rings
        Minecraft.getMinecraft().renderEngine.bindTexture(PLASMA_DETAIL_TEX);
        renderPulseRings(t);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        RenderUtils.enableLightmap();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderCylinder(float radius, float botY, float topY, float timeOffset, float uvScale) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        float scrollV = timeOffset % 1.0f;
        buf.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (int i = 0; i < SEGMENTS; i++) {
            double a1 = i       * 2.0 * Math.PI / SEGMENTS;
            double a2 = (i + 1) * 2.0 * Math.PI / SEGMENTS;
            float u1 = (float) i       / SEGMENTS * uvScale;
            float u2 = (float) (i + 1) / SEGMENTS * uvScale;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float z2 = (float) Math.sin(a2) * radius;
            buf.pos(x1, botY, z1).tex(u1, 1.0 + scrollV).endVertex();
            buf.pos(x2, botY, z2).tex(u2, 1.0 + scrollV).endVertex();
            buf.pos(x2, topY, z2).tex(u2,       scrollV).endVertex();
            buf.pos(x1, topY, z1).tex(u1,       scrollV).endVertex();
        }
        tess.draw();
    }

    private void renderPulseRings(float t) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        float height = ROD_TOP_Y - ROD_BOT_Y;
        for (int p = 0; p < PULSE_COUNT; p++) {
            float phase       = ((t * 0.4f + p * (1.0f / PULSE_COUNT)) % 1.0f);
            float pulseY      = ROD_BOT_Y + phase * height;
            float pulseRadius = INNER_RADIUS * 0.9f + phase * 0.12f;
            float brightness  = 0.55f * (1.0f - phase);
            GlStateManager.color(brightness * 1.5f, brightness * 2.5f, brightness * 5.0f, 1.0f);
            buf.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            for (int i = 0; i < SEGMENTS; i++) {
                double a1 = i       * 2.0 * Math.PI / SEGMENTS;
                double a2 = (i + 1) * 2.0 * Math.PI / SEGMENTS;
                float x1 = (float) Math.cos(a1) * pulseRadius;
                float z1 = (float) Math.sin(a1) * pulseRadius;
                float x2 = (float) Math.cos(a2) * pulseRadius;
                float z2 = (float) Math.sin(a2) * pulseRadius;
                buf.pos(x1, pulseY - 0.025f, z1).tex(0.0, 1.0).endVertex();
                buf.pos(x2, pulseY - 0.025f, z2).tex(1.0, 1.0).endVertex();
                buf.pos(x2, pulseY + 0.025f, z2).tex(1.0, 0.0).endVertex();
                buf.pos(x1, pulseY + 0.025f, z1).tex(0.0, 0.0).endVertex();
            }
            tess.draw();
        }
    }
}
