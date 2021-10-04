package net.doubledoordev.itemdelete;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;

public class ItemdeleteConfig
{
    public static final ItemdeleteConfig.General GENERAL;
    static final ForgeConfigSpec spec;

    static
    {
        final Pair<General, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ItemdeleteConfig.General::new);
        spec = specPair.getRight();
        GENERAL = specPair.getLeft();
    }

    public static class General
    {
        public static List<? extends String> itemsToDelete()
        {
            return new ArrayList<>();
        }

        public ForgeConfigSpec.ConfigValue<List<? extends String>> itemsToDelete;
        public ForgeConfigSpec.BooleanValue isItOn;

        General(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General configuration settings")
                    .push("General");

            itemsToDelete = builder
                    .comment("Use F3+H to show tooltips and get the name of the item, place it here. It get delete. Example: [\"minecraft:stone\"]",
                            "or if you need to ban multiple [\"minecraft:stone\", \"minecraft:dirt\"]")
                    .defineList("Items To Delete", ItemdeleteConfig.General.itemsToDelete(), p -> p instanceof String);

            isItOn = builder
                    .comment("Enables/Disables the constant removal of items.")
                    .define("Is it on?", true);

        }
    }
}
