package matteroverdrive.compat.modules.groovyscript;

import java.util.Collection;
import java.util.Collections;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.documentation.linkgenerator.LinkGeneratorHooks;

import matteroverdrive.Reference;

/**
 * GroovyScript external compat plugin for Matter Overdrive. Auto-discovered by
 * GroovyScript via classpath scan when the {@code groovyscript} mod is present.
 *
 * <p>Exposes the property container under {@code mods.matteroverdrive}.
 */
public class MOGroovyPlugin implements GroovyPlugin {

    @Override
    public String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    public String getContainerName() {
        return Reference.MOD_NAME;
    }

    @Override
    public Collection<String> getAliases() {
        return Collections.singletonList(Reference.MOD_ID);
    }

    @Override
    public GroovyPropertyContainer createGroovyPropertyContainer() {
        return new MatterOverdriveContainer();
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        LinkGeneratorHooks.registerLinkGenerator(new MOLinkGenerator());
        MOObjectMappers.register(container);
    }
}
