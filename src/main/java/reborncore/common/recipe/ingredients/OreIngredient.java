package reborncore.common.recipe.ingredients;

import net.minecraft.util.ResourceLocation;
import reborncore.api.newRecipe.IMachine;
import reborncore.common.recipe.registry.IngredientRegistry;
import reborncore.common.registration.RebornRegistry;

@RebornRegistry
@IngredientRegistry
public class OreIngredient extends BaseIngredient {

	String oreDict;

	public OreIngredient(ResourceLocation type, String oreDict) {
		super(type);
		this.oreDict = oreDict;
	}

	@Override
	public boolean canCraft(IMachine machine) {
		return false; //TODO
	}
}