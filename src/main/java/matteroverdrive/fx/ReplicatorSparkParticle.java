package matteroverdrive.fx;

import matteroverdrive.client.render.RenderParticlesHandler;
import matteroverdrive.proxy.ClientProxy;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Small additive-blended star-sprite particle for the TNG-style replication
 * materialisation effect. Rendered via RenderParticlesHandler (Additive layer)
 * so overlapping sparks accumulate brightness like real glowing light.
 *
 * Fading is done by ramping particleRed/Green/Blue — alpha is meaningless in
 * GL_ONE / GL_ONE additive blending.
 */
@SideOnly(Side.CLIENT)
public class ReplicatorSparkParticle extends MOEntityFX {

	private final float baseRed;
	private final float baseGreen;
	private final float baseBlue;
	private final float baseScale;

	private double centerX, centerY, centerZ;

	public ReplicatorSparkParticle(World world, double x, double y, double z,
			double vx, double vy, double vz,
			float r, float g, float b) {
		super(world, x, y, z);
		this.motionX = vx;
		this.motionY = vy;
		this.motionZ = vz;

		this.baseRed   = r;
		this.baseGreen = g;
		this.baseBlue  = b;

		// Start invisible; brightness is set per-frame in renderParticle
		this.particleRed   = 0;
		this.particleGreen = 0;
		this.particleBlue  = 0;
		this.particleAlpha = 1.0F; // Unused by additive blend, but set to 1 for safety

		// Small but visible — particleScale * 0.1 = world-unit half-size
		this.baseScale    = 0.15f + (float) Math.random() * 0.15f; // 0.15–0.30
		this.particleScale = this.baseScale;

		this.particleMaxAge = 8 + (int) (Math.random() * 10); // 8–17 ticks

		this.particleTexture = ClientProxy.renderHandler.getRenderParticlesHandler()
				.getSprite(RenderParticlesHandler.star);
	}

	public void setCenter(double x, double y, double z) {
		this.centerX = x;
		this.centerY = y;
		this.centerZ = z;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge) {
			setExpired();
			return;
		}

		// Weak pull toward the replicator centre so sparks converge inward
		double pull = 0.035;
		motionX += (centerX - posX) * pull;
		motionY += (centerY - posY) * pull;
		motionZ += (centerZ - posZ) * pull;

		// Air drag
		motionX *= 0.82;
		motionY *= 0.82;
		motionZ *= 0.82;

		setBoundingBox(getBoundingBox().offset(motionX, motionY, motionZ));
		posX = (getBoundingBox().minX + getBoundingBox().maxX) / 2.0;
		posY = getBoundingBox().minY;
		posZ = (getBoundingBox().minZ + getBoundingBox().maxZ) / 2.0;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks,
			float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		float ageF = ((float) particleAge + partialTicks) / (float) particleMaxAge;

		// Fade in over first 25%, hold, fade out over last 25%
		float brightness;
		if (ageF < 0.25f) {
			brightness = ageF / 0.25f;
		} else if (ageF > 0.75f) {
			brightness = Math.max(0f, (1.0f - ageF) / 0.25f);
		} else {
			brightness = 1.0f;
		}

		// Twinkling scale pulse
		float twinkle = 0.65f + 0.35f * (float) Math.abs(Math.sin(ageF * Math.PI * 7.0));
		particleScale = baseScale * twinkle;

		// Set RGB brightness (additive blending — alpha is irrelevant)
		particleRed   = baseRed   * brightness;
		particleGreen = baseGreen * brightness;
		particleBlue  = baseBlue  * brightness;

		super.renderParticle(buffer, entity, partialTicks,
				rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	// Max brightness — override to always show at full light level
	@Override
	public int getBrightnessForRender(float f) {
		return 0xF000F0;
	}
}
