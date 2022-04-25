/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen.button;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.network.PacketDistributor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.dries007.tfc.util.Helpers;

public class AnvilStepButton extends Button
{
    private final ForgeStep step;

    public AnvilStepButton(ForgeStep step)
    {
        super(step.buttonX(), step.buttonY(), 16, 16, Helpers.translateEnum(step), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(step.ordinal(), null));
        });

        this.step = step;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        super.renderButton(poseStack, mouseX, mouseY, partialTick);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AnvilScreen.BACKGROUND);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        blit(poseStack, step.buttonX(), step.buttonY(), 16, 16, step.iconX(), step.iconY(), 32, 32, 256, 256);
    }
}