package com.example.clientutils.mixin;

import com.example.clientutils.ClientUtilsClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	// Big items: haath mein pakda item bada dikhao
	@ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true)
	private MatrixStack clientutils$bigItems(MatrixStack matrices) {
		float s = ClientUtilsClient.BIG_ITEM_SCALE;
		matrices.scale(s, s, s);
		return matrices;
	}
}
