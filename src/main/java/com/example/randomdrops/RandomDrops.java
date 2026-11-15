package com.example.randomdrops;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomDrops implements ModInitializer {

    /** Items that should never be handed out (creative-only / technical). */
    private static final Set<String> BLACKLIST = Set.of(
            "air", "barrier", "light", "debug_stick", "knowledge_book",
            "command_block", "chain_command_block", "repeating_command_block",
            "command_block_minecart", "structure_block", "structure_void",
            "jigsaw", "test_block", "test_instance_block", "spawner",
            "trial_spawner", "vault", "reinforced_deepslate", "bedrock",
            "end_portal_frame", "petrified_oak_slab"
    );

    private static List<Item> pool;

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            // Only handle real server-side breaks, and leave creative mode alone (vanilla: no drops)
            if (!(level instanceof ServerLevel) || player.isCreative()) {
                return true;
            }

            // Break the block ourselves WITHOUT its normal drops
            level.destroyBlock(pos, false, player);

            // Damage the held tool like a normal break would
            player.getMainHandItem().mineBlock(level, state, pos, player);

            // Drop a random item
            Block.popResource(level, pos, new ItemStack(randomItem()));

            // Cancel the vanilla break (we already did it)
            return false;
        });
    }

    private static Item randomItem() {
        if (pool == null) {
            List<Item> list = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                String path = BuiltInRegistries.ITEM.getKey(item).getPath();
                if (BLACKLIST.contains(path)) continue;
                list.add(item);
            }
            pool = list;
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}
