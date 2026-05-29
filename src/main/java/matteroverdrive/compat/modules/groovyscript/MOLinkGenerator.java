package matteroverdrive.compat.modules.groovyscript;

import com.cleanroommc.groovyscript.documentation.linkgenerator.BasicLinkGenerator;

import matteroverdrive.Reference;

/**
 * Generates source-code links pointing to the MatterOverdrive GitHub repository.
 *
 * <p>Registered in {@link MOGroovyPlugin#onCompatLoaded} and referenced via
 * {@code linkGenerator = Reference.MOD_ID} on each {@code @RegistryDescription}.
 */
public class MOLinkGenerator extends BasicLinkGenerator {

    @Override
    public String id() {
        return Reference.MOD_ID;
    }

    @Override
    protected String domain() {
        return "https://github.com/Refitbench/MatterOverdrive/";
    }

    @Override
    protected String defaultBranch() {
        return "master";
    }

    @Override
    protected String version() {
        return "master";
    }
}
