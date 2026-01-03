package dev.link.nebula.module;

import dev.link.nebula.module.modules.movement.Sprint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private static ModuleManager instance;
    private final List<Module> modules;

    public ModuleManager() {
        instance = this;
        modules = new ArrayList<>();

        // Register modules here
        registerModule(new Sprint());

        // Add more modules here as you create them
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