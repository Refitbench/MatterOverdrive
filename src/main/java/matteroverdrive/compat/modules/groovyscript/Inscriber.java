package matteroverdrive.compat.modules.groovyscript;

import java.util.Collection;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Comp;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.Property;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderMethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderRegistrationMethod;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.StandardListRegistry;

import matteroverdrive.Reference;
import matteroverdrive.data.recipes.InscriberRecipe;
import matteroverdrive.init.MatterOverdriveRecipes;
import net.minecraft.item.ItemStack;

/**
 * GroovyScript registry for Matter Overdrive's Inscriber recipes.
 *
 * <pre>{@code
 * mods.matteroverdrive.inscriber.recipeBuilder()
 *     .input(item('minecraft:redstone'))
 *     .input(item('minecraft:gold_ingot'))
 *     .output(item('matteroverdrive:circuit_basic'))
 *     .energy(1000).time(80)
 *     .register()
 * }</pre>
 */
@RegistryDescription(linkGenerator = Reference.MOD_ID)
public class Inscriber extends StandardListRegistry<InscriberRecipe> {

    @Override
    public Collection<InscriberRecipe> getRecipes() {
        return MatterOverdriveRecipes.INSCRIBER.getRecipesMutable();
    }

    @MethodDescription(example = @Example(commented = true))
    public void removeByOutput(IIngredient output) {
        for (ItemStack stack : output.getMatchingStacks()) {
            for (InscriberRecipe r : MatterOverdriveRecipes.INSCRIBER.removeByOutput(stack)) {
                addBackup(r);
            }
        }
    }

    @MethodDescription(example = @Example("item('matteroverdrive:isolinear_circuit'), item('minecraft:gold_ingot')"))
    public void removeByInputs(IIngredient main, IIngredient sec) {
        for (ItemStack mainStack : main.getMatchingStacks()) {
            for (ItemStack secStack : sec.getMatchingStacks()) {
                for (InscriberRecipe r : MatterOverdriveRecipes.INSCRIBER.removeByInputPair(mainStack, secStack)) {
                    addBackup(r);
                }
            }
        }
    }

    @RecipeBuilderDescription(example = @Example(".input(item('minecraft:redstone'))\n.input(item('minecraft:gold_ingot'))\n.output(item('matteroverdrive:circuit_basic'))\n.energy(32000).time(120)"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", comp = @Comp(eq = 2))
    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<InscriberRecipe> {

        @Property(defaultValue = "64000")
        private int energy = 64000;
        @Property(defaultValue = "300")
        private int time = 300;

        @RecipeBuilderMethodDescription
        public RecipeBuilder energy(int energy) {
            this.energy = energy;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder time(int time) {
            this.time = time;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Matter Overdrive inscriber recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 2, 2, 1, 1);
            msg.add(energy <= 0, "energy must be > 0, got {}", energy);
            msg.add(time <= 0, "time must be > 0, got {}", time);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public InscriberRecipe register() {
            if (!validate()) return null;
            ItemStack out = output.get(0);
            InscriberRecipe recipe = null;
            for (ItemStack main : input.get(0).getMatchingStacks()) {
                for (ItemStack sec : input.get(1).getMatchingStacks()) {
                    recipe = new InscriberRecipe(main, sec, out, energy, time);
                    MatterOverdriveContainer.instance.inscriber.add(recipe);
                }
            }
            return recipe;
        }
    }
}
