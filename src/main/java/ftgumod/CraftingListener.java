package ftgumod;

import ftgumod.event.PlayerLockEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;

public class CraftingListener implements IContainerListener {

	private EntityPlayer player;

	CraftingListener(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void sendSlotContents(Container container, int index, ItemStack stack) {
		Slot slot = container.getSlot(index);
		if (slot.inventory instanceof InventoryCraftResult) {
			PlayerLockEvent event = new PlayerLockEvent(player, stack, ((InventoryCraftResult) slot.inventory).getRecipeUsed());
			if (!stack.isEmpty())
				MinecraftForge.EVENT_BUS.post(event);

			slot.inventory.setInventorySlotContents(0, event.isCanceled() ? stack : ItemStack.EMPTY);
		}
	}

	@Override
	public void sendAllWindowProperties(Container arg0, IInventory arg1) {
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
	}

}
