package com.example.clientutils.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

	@Shadow
	private boolean fogEnabled;

	// No fog: fog hamesha off (F3+F wala fog-off mode force)
	@Inject(method = "getFogBuffer", at = @At("HEAD"))
	private void clientutils$noFog(FogRenderer.FogType type,
								   CallbackInfoReturnable<GpuBufferSlice> cir) {
		this.fogEnabled = false;
	}
}
