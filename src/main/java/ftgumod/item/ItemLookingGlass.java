package ftgumod.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import org.lwjgl.input.Keyboard;
import ftgumod.Decipher;
import ftgumod.Decipher.DecipherGroup;
import ftgumod.ResearchRecipe;
import ftgumod.TechnologyHandler;
import ftgumod.TechnologyUtil;

public class ItemLookingGlass extends Item {

	public ItemLookingGlass(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.TOOLS);
		setMaxStackSize(1);
	}

	public static List<String> getItems(ItemStack item) {
		List<String> list = new ArrayList<String>();
		NBTTagList blocks = TechnologyUtil.getItemData(item).getTagList("FTGU", NBT.TAG_STRING);
		for (int i = 0; i < blocks.tagCount(); i++) {
			list.add(blocks.getStringTagAt(i));
		}
		return list;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float f1, float f2, float f3) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));

			boolean need = false;
			for (ResearchRecipe r : TechnologyHandler.unlock.keySet()) {
				Decipher d = TechnologyHandler.unlock.get(r);
				if (r.output.canResearch(player))
					for (DecipherGroup g : d.list)
						for (ItemStack s : g.unlock)
							if (ItemStack.areItemStacksEqual(s, stack))
								need = true;
			}

			if (!need) {
				if (!world.isRemote) {
					player.addChatMessage(new TextComponentString(I18n.translateToLocal("technology.decipher.understand")));
					world.playSound(null, player.getPosition(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
				return EnumActionResult.SUCCESS;
			}

			Item b_item = Item.getItemFromBlock(block);
			String name = block.getUnlocalizedName();
			if (b_item != null)
				name = Item.getItemFromBlock(block).getUnlocalizedName(stack);
			if (!I18n.canTranslate(name + ".name"))
				name = name.replace("tile.", "item.");

			NBTTagCompound tag = TechnologyUtil.getItemData(item);
			NBTTagList blocks = tag.getTagList("FTGU", NBT.TAG_STRING);
			for (int i = 0; i < blocks.tagCount(); i++)
				if (blocks.getStringTagAt(i).equalsIgnoreCase(name)) {
					if (!world.isRemote) {
						player.addChatMessage(new TextComponentString("\"" + I18n.translateToLocal(name + ".name") + "\" " + I18n.translateToLocal("technology.decipher.already")));
						world.playSound(null, player.getPosition(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
					}
					return EnumActionResult.SUCCESS;
				}
			if (!world.isRemote) {
				player.addChatMessage(new TextComponentString(I18n.translateToLocal("technology.decipher.flawless")));
				world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			blocks.appendTag(new NBTTagString(name));
			tag.setTag("FTGU", blocks);

			return EnumActionResult.SUCCESS;
		} else {
			return EnumActionResult.PASS;
		}
	}

}
