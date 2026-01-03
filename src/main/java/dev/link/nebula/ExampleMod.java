package dev.link.nebula;

import dev.link.nebula.handler.KeyBindHandler;
import dev.link.nebula.module.ModuleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "nebula", useMetadata=true)
public class ExampleMod {

    @Mod.Instance("nebula")
    public static ExampleMod INSTANCE;

    private ModuleManager moduleManager;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Dirt: " + Blocks.dirt.getUnlocalizedName());
        // Below is a demonstration of an access-transformed class access.
        System.out.println("Color State: " + new GlStateManager.Color());

        // Initialize module system
        System.out.println("Initializing Nebula Client...");
        moduleManager = new ModuleManager();
        System.out.println("Loaded " + moduleManager.getModules().size() + " modules");

        // Register keybind handler
        MinecraftForge.EVENT_BUS.register(new KeyBindHandler());
        System.out.println("Press RIGHT SHIFT to open the client menu");
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}