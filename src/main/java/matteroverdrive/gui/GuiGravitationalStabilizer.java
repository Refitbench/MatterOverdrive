package matteroverdrive.gui;

import java.text.DecimalFormat;

import matteroverdrive.Reference;
import matteroverdrive.client.data.Color;
import matteroverdrive.container.ContainerGravitationalStabilizer;
import matteroverdrive.gui.element.ElementDoubleCircleBar;
import matteroverdrive.tile.TileEntityMachineGravitationalStabilizer;
import matteroverdrive.util.MOEnergyHelper;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiGravitationalStabilizer
		extends MOGuiMachine<TileEntityMachineGravitationalStabilizer> {

	ElementDoubleCircleBar powerBar;
	DecimalFormat format;
	private Color currentStressColor = Reference.COLOR_HOLO;

	public GuiGravitationalStabilizer(InventoryPlayer inventoryPlayer,
			TileEntityMachineGravitationalStabilizer machine) {
		super(new ContainerGravitationalStabilizer(inventoryPlayer, machine), machine, 256, 230);
		format = new DecimalFormat("#.#");
		name = "gravitational_stabilizer";
		powerBar = new ElementDoubleCircleBar(this, 70, 40, 135, 135, Reference.COLOR_GUI_ENERGY);
		powerBar.setColorRight(Reference.COLOR_HOLO);
	}

	@Override
	public void initGui() {
		super.initGui();
		pages.get(0).addElement(powerBar);
		AddHotbarPlayerSlots(this.inventorySlots, this);
	}

	@Override
	protected void updateElementInformation() {
		super.updateElementInformation();
		float energyFill = (float) machine.getEnergyStorage().getEnergyStored()
				/ (float) machine.getEnergyStorage().getMaxEnergyStored();
		powerBar.setProgressLeft(energyFill);

		float stress = machine.getPercentage();
		// Right arc = stress level (0 = calm, 1 = maximum); color shifts holo→green→red
		powerBar.setProgressRight(stress >= 0 ? stress : 0.0f);
		currentStressColor = stress >= 0 ? stressColor(stress) : Reference.COLOR_HOLO;
		powerBar.setColorRight(currentStressColor);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		if (pages.get(0).isVisible()) {
				// Centre label
			String info = "Stress";
			int width = fontRenderer.getStringWidth(info);
			fontRenderer.drawString(info, 140 - width / 2, 132, Reference.COLOR_GUI_DARKER.getColor());

			// Power % on the left arc tip
			double leftAngle = -(Math.PI * 0.87) * powerBar.getProgressLeft() - (Math.PI * 2 * 0.03);
			int xPos = 137 + (int) Math.round(Math.sin(leftAngle) * 76);
			int yPos = 104 + (int) Math.round(Math.cos(leftAngle) * 74);
			drawCenteredString(fontRenderer, format.format(powerBar.getProgressLeft() * 100) + "%",
					xPos, yPos, Reference.COLOR_HOLO_RED.getColor());

			// Stress % on the right arc tip — colored to match the arc
			double rightAngle = (Math.PI * 0.87) * powerBar.getProgressRight() + (Math.PI * 2 * 0.03);
			xPos = 137 + (int) Math.round(Math.sin(rightAngle) * 76);
			yPos = 104 + (int) Math.round(Math.cos(rightAngle) * 74);
			drawCenteredString(fontRenderer, format.format(powerBar.getProgressRight() * 100) + "%",
					xPos, yPos, currentStressColor.getColor());

			// Drain/tick label in center
			int drain = machine.getEnergyStorage().getEnergyStored() > 0
					? TileEntityMachineGravitationalStabilizer.BASE_ENERGY_PER_TICK
							+ (int) ((TileEntityMachineGravitationalStabilizer.MAX_ENERGY_PER_TICK
									- TileEntityMachineGravitationalStabilizer.BASE_ENERGY_PER_TICK)
									* Math.max(0, machine.getPercentage()))
					: 0;
			info = "-" + drain + MOEnergyHelper.ENERGY_UNIT + "/t";
			width = fontRenderer.getStringWidth(info);
			fontRenderer.drawStringWithShadow(info, 140 - width / 2, 110, Reference.COLOR_HOLO_RED.getColor());
		}
	}

	private static Color stressColor(float stress) {
		Color low = Color.WHITE;                 // 255,255,255 — pure white
		Color mid = Reference.COLOR_HOLO_YELLOW; // 252,223,116 — yellow
		Color hi  = Reference.COLOR_HOLO_RED;    // 230, 80, 20 — orange-red
		Color a, b;
		float t;
		if (stress <= 0.5f) {
			a = low; b = mid; t = stress * 2f;
		} else {
			a = mid; b = hi;  t = (stress - 0.5f) * 2f;
		}
		return new Color(
				a.getIntR() + (int) ((b.getIntR() - a.getIntR()) * t),
				a.getIntG() + (int) ((b.getIntG() - a.getIntG()) * t),
				a.getIntB() + (int) ((b.getIntB() - a.getIntB()) * t)
		);
	}
}
