package com.domain.redstonetools.features.commands;

import com.domain.redstonetools.features.Feature;
import com.domain.redstonetools.utils.BlockStateNbtUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Method;

@Feature(name = "Copy State", description = "Gives you a copy of the block you're looking at with its BlockState.", command = "copystate")
public class CopyStateFeature extends PickBlockFeature {
    @Override
    protected ItemStack getItemStack(ServerCommandSource source, BlockHitResult blockHit) {
        MinecraftClient client = MinecraftClient.getInstance();

        BlockPos blockPos = blockHit.getBlockPos();
        BlockState blockState = client.world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        ItemStack itemStack = block.getPickStack(client.world, blockPos, blockState);

        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
            addBlockEntityNbt(itemStack, blockEntity);
        }

        int i = addBlockStateNbt(itemStack,blockState);
        if (i == -1) {
            source.sendError(Text.of("This block doesn't have any BlockState!"));
            return null;
        }

        return itemStack;
    }

    private int addBlockStateNbt(ItemStack itemStack, BlockState blockState) {
        addBlockStateText(itemStack);

        NbtCompound nbt = itemStack.getOrCreateNbt();
        String stringState = BlockStateNbtUtil.blockStateToString(blockState);
        if (stringState == null) return -1;

        nbt.putString("blockstate",stringState);

        return 1;
    }

    private void addBlockStateText(ItemStack itemStack) {
        NbtCompound displayNbt = itemStack.getSubNbt("display");
        NbtList loreList = new NbtList();

        if (displayNbt == null) {
            displayNbt = new NbtCompound();
        } else {
            loreList = (NbtList) displayNbt.get("Lore");
        }

        loreList.add(NbtString.of("\"(+BlockState)\""));
        displayNbt.put("Lore", loreList);
        itemStack.setSubNbt("display", displayNbt);
    }

    private void addBlockEntityNbt(ItemStack itemStack, BlockEntity blockEntity) {
        try {
            Class<? extends MinecraftClient> clientClass = MinecraftClient.class;
            MinecraftClient clientClassInstance = MinecraftClient.getInstance();
            Method m = clientClass.getDeclaredMethod("addBlockEntityNbt", ItemStack.class, BlockEntity.class);

            m.setAccessible(true);
            m.invoke(clientClassInstance, itemStack, blockEntity);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
