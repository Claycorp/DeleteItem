package net.doubledoordev.itemdelete;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("deleteitem")
public class DeleteItem
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public DeleteItem()
    {
        MinecraftForge.EVENT_BUS.register(ItemdeleteConfig.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ItemdeleteConfig.spec);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () ->
                new IExtensionPoint.DisplayTest(() -> "Forge making stupid decisions 101. Morons", (remote, isServer) -> true));
    }


    @SubscribeEvent
    public void clearContainer(PlayerContainerEvent event)
    {
        if (ItemdeleteConfig.GENERAL.isItOn.get())
        {
            AbstractContainerMenu container = event.getContainer();
            for (ItemStack itemStack : container.getItems())
            {
                ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
                if (resourceLocation == null)
                    return;

                String itemName = resourceLocation.toString();
                if (ItemdeleteConfig.GENERAL.itemsToDelete.get().contains(itemName))
                {
                    Player playerEntity = event.getPlayer();
                    HitResult hitResult = playerEntity.pick(playerEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get()), 0, false);
                    BlockPos blockPos = new BlockPos(hitResult.getLocation());

                    ResourceLocation resourceLocationMenu = null;
                    try
                    {
                        resourceLocationMenu = ForgeRegistries.CONTAINERS.getKey(container.getType());
                    }
                    catch (UnsupportedOperationException ignored)
                    {
                        // Just eat this error because there's no other way to filter it out, and it doesn't do anything of value.
                    }

                    String containerName;
                    if (resourceLocationMenu == null)
                    {
                        containerName = "Unknown Container/Player Inventory";
                    }
                    else containerName = resourceLocationMenu.toString();

                    LOGGER.info("Delete Item has removed ItemStack: " + itemName + "(x" + itemStack.getCount() + ") from the game! Inside container: \"" +
                            containerName + "\" at " + blockPos);

                    itemStack.setCount(0);
                }
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void entitySpawnEvent(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        ItemEntity itemEntity;
        if (entity instanceof ItemEntity)
        {
            itemEntity = (ItemEntity) entity;
        }
        else return;

        if (ItemdeleteConfig.GENERAL.isItOn.get() && !itemEntity.getItem().isEmpty())
        {
            ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(itemEntity.getItem().getItem());
            if (resourceLocation == null)
                return;

            String itemName = resourceLocation.toString();
            if (ItemdeleteConfig.GENERAL.itemsToDelete.get().contains(itemName))
            {
                String playerName = "Unknown Origin: Block break?";
                Player player = null;
                if (itemEntity.getThrower() != null)
                    player = event.getWorld().getPlayerByUUID(itemEntity.getThrower());
                if (player != null)
                    playerName = player.getDisplayName().getString();

                LOGGER.info("Delete Item has removed " + itemName + "(x " + itemEntity.getItem().getCount() + ") from the game! Thrown by: \"" +
                        playerName + "\" at " + itemEntity.getOnPos());

                event.setCanceled(true);
            }
        }
    }
}
