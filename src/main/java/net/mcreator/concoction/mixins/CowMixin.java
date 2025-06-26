package net.mcreator.concoction.mixins;

import net.mcreator.concoction.init.ConcoctionModItems;
import net.mcreator.concoction.interfaces.ICowMilkLevel;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public class CowMixin implements ICowMilkLevel {
    @Unique
    private int concoction$milkLevel = 3;
    @Unique
    private long concoction$lastMilkedTime = 0L;

    @Override
    public int concoction$getMilkLevel() { return concoction$milkLevel; }
    @Override
    public void concoction$setMilkLevel(int level) { concoction$milkLevel = Math.max(0, Math.min(3, level)); }
    @Override
    public void concoction$incrementMilkLevel() { concoction$setMilkLevel(concoction$milkLevel + 1); }
    @Override
    public void concoction$decrementMilkLevel() { concoction$setMilkLevel(concoction$milkLevel - 1); }
    @Override
    public long concoction$getLastMilkedTime() { return concoction$lastMilkedTime; }
    @Override
    public void concoction$setLastMilkedTime(long time) { concoction$lastMilkedTime = time; }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void concoction$onMilk(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Cow cow = (Cow)(Object)this;
        Level level = cow.level();
        long now = level.getGameTime();
        if (concoction$milkLevel < 3) {
            long minutesPassed = (now - concoction$lastMilkedTime) / 2400L;
            if (minutesPassed > 0) {
                concoction$setMilkLevel(Math.min(3, concoction$milkLevel + (int)minutesPassed));
                concoction$setLastMilkedTime(now - (now - concoction$lastMilkedTime) % 2400L);
            }
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.BUCKET) {
            if (concoction$milkLevel == 3) {
                concoction$setMilkLevel(0);
                concoction$setLastMilkedTime(now);
                if (!player.isCreative()) stack.shrink(1);
                player.addItem(new ItemStack(Items.MILK_BUCKET));
                cir.setReturnValue(InteractionResult.SUCCESS);
            } else {
                if (level.isClientSide) player.displayClientMessage(Component.translatable("message.concoction.cow_not_ready_bucket"), true);
                cir.setReturnValue(InteractionResult.FAIL);
            }
        } else if (stack.getItem() == Items.GLASS_BOTTLE) {
            if (concoction$milkLevel > 0) {
                concoction$decrementMilkLevel();
                concoction$setLastMilkedTime(now);
                if (!player.isCreative()) stack.shrink(1);
                player.addItem(new ItemStack(ConcoctionModItems.MILK_BOTTLE.asItem()));
                cir.setReturnValue(InteractionResult.SUCCESS);
            } else {
                if (level.isClientSide) player.displayClientMessage(Component.translatable("message.concoction.cow_not_ready_bottle"), true);
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
} 