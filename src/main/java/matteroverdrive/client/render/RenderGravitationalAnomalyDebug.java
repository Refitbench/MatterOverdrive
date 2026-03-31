package matteroverdrive.client.render;

import matteroverdrive.client.RenderHandler;
import matteroverdrive.tile.TileEntityGravitationalAnomaly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/**
 * Debug renderer: draws three wireframe ring-sets around each loaded
 * gravitational anomaly showing its inner/mid/outer scan band boundaries.
 * Enabled only when DEBUG_SCAN_WIREFRAME is true (set via config).
 */
public class RenderGravitationalAnomalyDebug implements IWorldLastRenderer {

    // Segments per circle — 48 gives a smooth circle at low cost
    private static final int SEGMENTS = 48;

    @Override
    public void onRenderWorldLast(RenderHandler handler, RenderWorldLastEvent event) {
        // Cheapest possible early-out when the feature is disabled
        if (!TileEntityGravitationalAnomaly.DEBUG_SCAN_WIREFRAME) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;

        Vec3d view = mc.getRenderViewEntity().getPositionEyes(event.getPartialTicks());

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        GL11.glLineWidth(1.5f);

        // Translate so that world coords map directly to GL coords relative to camera
        GlStateManager.translate(-view.x, -view.y, -view.z);

        for (TileEntity te : mc.world.loadedTileEntityList) {
            if (!(te instanceof TileEntityGravitationalAnomaly)) continue;

            TileEntityGravitationalAnomaly anomaly = (TileEntityGravitationalAnomaly) te;
            double scanRange = anomaly.getBlockBreakRange();
            if (scanRange <= 0) continue;

            BlockPos pos = te.getPos();
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;

            // Inner band boundary (range * 0.25) — bright green
            drawRings(cx, cy, cz, scanRange * 0.25, 0.2f, 1.0f, 0.3f, 0.8f);
            // Mid band boundary (range * 0.55) — yellow
            drawRings(cx, cy, cz, scanRange * 0.55, 1.0f, 0.9f, 0.1f, 0.8f);
            // Full scan range — red
            drawRings(cx, cy, cz, scanRange, 1.0f, 0.2f, 0.2f, 0.8f);
        }

        GL11.glLineWidth(1.0f);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Draws three great-circle rings (XZ, XY, YZ planes) at radius {@code r}
     * centred on (cx, cy, cz) in the given RGBA colour.
     */
    private static void drawRings(double cx, double cy, double cz, double r,
                                  float red, float green, float blue, float alpha) {
        if (r <= 0) return;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.color(red, green, blue, alpha);

        // Horizontal ring — XZ plane
        buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i < SEGMENTS; i++) {
            double a = 2.0 * Math.PI * i / SEGMENTS;
            buf.pos(cx + r * Math.cos(a), cy, cz + r * Math.sin(a)).endVertex();
        }
        tess.draw();

        // Vertical ring — XY plane (faces Z axis)
        buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i < SEGMENTS; i++) {
            double a = 2.0 * Math.PI * i / SEGMENTS;
            buf.pos(cx + r * Math.cos(a), cy + r * Math.sin(a), cz).endVertex();
        }
        tess.draw();

        // Vertical ring — YZ plane (faces X axis)
        buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i < SEGMENTS; i++) {
            double a = 2.0 * Math.PI * i / SEGMENTS;
            buf.pos(cx, cy + r * Math.sin(a), cz + r * Math.cos(a)).endVertex();
        }
        tess.draw();
    }
}
