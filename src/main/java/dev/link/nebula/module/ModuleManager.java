package dev.link.nebula.module;

import dev.link.nebula.module.modules.combat.Velocity;
import dev.link.nebula.module.modules.movement.Fly;
import dev.link.nebula.module.modules.movement.Speed;
import dev.link.nebula.module.modules.movement.Sprint;
import dev.link.nebula.module.modules.player.NoFall;
import dev.link.nebula.module.modules.render.ESP;
import dev.link.nebula.module.modules.render.Fullbright;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private static ModuleManager instance;
    private final List<Module> modules;

    public ModuleManager() {
        instance = this;
        modules = new ArrayList<>();

        // Combat modules
        registerModule(new Velocity());

        // Movement modules
        registerModule(new Sprint());
        registerModule(new Fly());
        registerModule(new Speed());

        // Player modules
        registerModule(new NoFall());

        // Render modules
        registerModule(new Fullbright());
        registerModule(new ESP());

        System.out.println("Registered modules:");
        for (Module module : modules) {
            System.out.println(" - " + module.getName() + " (" + module.getCategory().getName() + ")");
        }
    }

    private void registerModule(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        return modules.stream()
                .filter(module -> module.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static ModuleManager getInstance() {
        return instance;
    }
}