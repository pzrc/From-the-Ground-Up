package ftgumod.client.gui;

import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import ftgumod.ItemList;
import ftgumod.ItemListWildcard;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.packet.server.RequestTechMessage;
import ftgumod.packet.server.UnlockTechMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.PAGE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Set;

public class GuiResearchBook extends GuiScreen {

	private static final ResourceLocation ACHIEVEMENT_BACKGROUND = new ResourceLocation(FTGU.MODID + ":textures/gui/achievement/achievement_background.png");

	private int x_min;
	private int y_min;
	private int x_max;
	private int y_max;

	private int imageWidth;
	private int imageHeight;
	private float zoom;
	private int currentPage;
	private double xScrollO;
	private double yScrollO;
	private double xScrollP;
	private double yScrollP;
	private double xScrollTarget;
	private double yScrollTarget;
	private int scrolling;
	private double xLastScroll;
	private double yLastScroll;
	private int state = 0;
	private int scroll = 1;

	private EntityPlayer player;
	private Technology selected;
	private NonNullList<ItemListWildcard> unlock;
	private int num = 4;
	private int pages;

	public GuiResearchBook(EntityPlayer player) {
		this.player = player;

		imageWidth = 256;
		imageHeight = 202;
		zoom = 1.0F;
		currentPage = 0;

		int i = 141;
		xScrollO = xScrollP = xScrollTarget = -i / 2 - 12;
		yScrollO = yScrollP = yScrollTarget = -i / 2 - 12;

		PacketDispatcher.sendToServer(new RequestTechMessage());
	}

	@Override
	public void initGui() {
		PAGE p = PAGE.get(currentPage);

		buttonList.clear();
		if (state == 0) {
			x_min = p.minX * 24 - 112;
			y_min = p.minY * 24 - 112;
			x_max = p.maxX * 24 - 77;
			y_max = p.maxY * 24 - 77;

			GuiButton page = new GuiButton(2, (width - imageWidth) / 2 + 24, height / 2 + 74, 125, 20, p.name);
			if (PAGE.size() < 2)
				page.enabled = false;

			buttonList.add(new GuiButton(1, width / 2 + 24, height / 2 + 74, 80, 20, I18n.format("gui.done")));
			buttonList.add(page);
		} else {
			GuiButton copy = new GuiButton(2, (width - imageWidth) / 2 + 24, height / 2 + 74, 125, 20, I18n.format("gui.copy"));
			copy.enabled = false;
			for (int i = 0; i < player.inventory.getSizeInventory(); i++)
				if (!player.inventory.getStackInSlot(i).isEmpty() && player.inventory.getStackInSlot(i).getItem() == FTGUAPI.i_parchmentEmpty)
					copy.enabled = true;

			buttonList.add(new GuiButton(1, width / 2 + 24, height / 2 + 74, 80, 20, I18n.format("gui.done")));
			buttonList.add(copy);
			scroll = 1;

			// Load Items
			unlock = NonNullList.create();
			for (ItemList s : selected.getUnlock()) {
				ItemListWildcard l = new ItemListWildcard(s);
				if (l.size() > 0)
					unlock.add(l);
			}

			pages = (int) Math.max(Math.ceil(((double) unlock.size()) / num), 1);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			if (state == 0) {
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
			} else {
				state = 0;
				initGui();
			}
		} else if (button.id == 2) {
			if (state == 0) {
				currentPage++;
				if (currentPage >= PAGE.size())
					currentPage = 0;
				button.displayString = PAGE.get(currentPage).name;
			} else {
				PacketDispatcher.sendToServer(new CopyTechMessage(state));
			}
		}
	}

	@Override
	protected void keyTyped(char key, int id) throws IOException {
		if (mc.gameSettings.keyBindInventory.isActiveAndMatches(id)) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		} else {
			super.keyTyped(key, id);
		}
	}

	@Override
	public void drawScreen(int x, int y, float z) {
		if (state == 0) {
			if (Mouse.isButtonDown(0)) {
				int i = (width - imageWidth) / 2;
				int j = (height - imageHeight) / 2;
				int k = i + 8;
				int l = j + 17;

				if ((scrolling == 0 || scrolling == 1) && x >= k && x < k + 224 && y >= l && y < l + 155) {
					if (scrolling == 0) {
						scrolling = 1;
					} else {
						xScrollP -= (float) (x - xLastScroll) * zoom;
						yScrollP -= (float) (y - yLastScroll) * zoom;
						xScrollTarget = xScrollO = xScrollP;
						yScrollTarget = yScrollO = yScrollP;
					}
					xLastScroll = x;
					yLastScroll = y;
				}
			} else {
				scrolling = 0;
			}

			int i1 = Mouse.getDWheel();
			float f3 = zoom;
			if (i1 < 0)
				zoom += 0.25F;
			else if (i1 > 0)
				zoom -= 0.25F;
			zoom = MathHelper.clamp(zoom, 1.0F, 2.0F);

			if (zoom != f3) {
				float f4 = f3 * imageWidth;
				float f = f3 * imageHeight;
				float f1 = zoom * imageWidth;
				float f2 = zoom * imageHeight;

				xScrollP -= (f1 - f4) * 0.5F;
				yScrollP -= (f2 - f) * 0.5F;
				xScrollTarget = xScrollO = xScrollP;
				yScrollTarget = yScrollO = yScrollP;
			}

			if (xScrollTarget < x_min)
				xScrollTarget = x_min;
			if (yScrollTarget < y_min)
				yScrollTarget = y_min;
			if (xScrollTarget >= x_max)
				xScrollTarget = x_max - 1;
			if (yScrollTarget >= y_max)
				yScrollTarget = y_max - 1;
		}

		drawDefaultBackground();
		drawResearchScreen(x, y, z);

		GlStateManager.disableLighting();
		GlStateManager.disableDepth();

		drawTitle();

		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
	}

	@Override
	public void mouseClicked(int x, int y, int b) throws IOException {
		if (b == 1 && player.capabilities.isCreativeMode && selected != null)
			PacketDispatcher.sendToServer(new UnlockTechMessage(selected.getID()));
		if (b == 0 && selected != null && selected.isResearched(player)) {
			state = selected.getID();
			initGui();
		}
		super.mouseClicked(x, y, b);
	}

	@Override
	public void updateScreen() {
		xScrollO = xScrollP;
		yScrollO = yScrollP;
		double d0 = xScrollTarget - xScrollP;
		double d1 = yScrollTarget - yScrollP;
		if (d0 * d0 + d1 * d1 < 4D) {
			xScrollP += d0;
			yScrollP += d1;
		} else {
			xScrollP += d0 * 0.85D;
			yScrollP += d1 * 0.85D;
		}
	}

	private void drawTitle() {
		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		fontRenderer.drawString(I18n.format("item.research_book.name"), i + 15, j + 5, 0x404040);
	}

	@SuppressWarnings("deprecation")
	private TextureAtlasSprite getTexture(ItemStack itemStack) {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(((ItemBlock) itemStack.getItem()).getBlock().getStateFromMeta(itemStack.getMetadata()));
	}

	private void drawResearchScreen(int x, int y, float z) {
		int split = 211;

		int i = MathHelper.floor(xScrollO + (xScrollP - xScrollO) * z);
		int j = MathHelper.floor(yScrollO + (yScrollP - yScrollO) * z);

		if (i < x_min)
			i = x_min;
		if (j < y_min)
			j = y_min;
		if (i >= x_max)
			i = x_max - 1;
		if (j >= y_max)
			j = y_max - 1;

		int k = (width - imageWidth) / 2;
		int l = (height - imageHeight) / 2;
		int i1 = k + 16;
		int j1 = l + 17;

		GlStateManager.depthFunc(518);
		GlStateManager.pushMatrix();
		GlStateManager.translate(i1, j1, -200F);
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();

		for (int l3 = 0; l3 < 10; l3++) {
			for (int i4 = 0; i4 < 14; i4++) {
				TextureAtlasSprite textureatlassprite = getTexture(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, 9));
				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(i4 * 16, l3 * 16, textureatlassprite, 16, 16);
			}
		}
		mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		if (state == 0) {
			GlStateManager.scale(1.0F / zoom, 1.0F / zoom, 1.0F);
			Set<Technology> tech = TechnologyHandler.technologies.get(PAGE.get(currentPage));

			if (tech != null) {
				for (Technology t1 : tech) {
					if (t1.hasCustomUnlock() && !t1.isResearched(player) && !t1.isUnlocked(player))
						continue;
					if (t1.isHidden() && !t1.hasCustomUnlock() && !t1.isResearched(player))
						continue;
					if (t1.getPrevious() == null || !tech.contains(t1.getPrevious()))
						continue;
					int xStart = (t1.getX() * 24 - i) + 11;
					int yStart = (t1.getY() * 24 - j) + 11;
					int xStop = (t1.getPrevious().getX() * 24 - i) + 11;
					int yStop = (t1.getPrevious().getY() * 24 - j) + 11;

					boolean flag = t1.isResearched(player);
					boolean flag1 = t1.canResearchIgnoreResearched(player);
					int k4 = t1.requirementsUntilAvailable(player);

					if (k4 > 2)
						continue;

					int l4 = 0xff000000;
					if (flag)
						l4 = 0xffa0a0a0;
					else if (flag1)
						l4 = 0xff00ff00;

					drawHorizontalLine(xStart, xStop, yStart, l4);
					drawVerticalLine(xStop, yStart, yStop, l4);

					if (xStart > xStop)
						drawTexturedModalRect(xStart - 11 - 7, yStart - 5, 114, 234, 7, 11);
					else if (xStart < xStop)
						drawTexturedModalRect(xStart + 11, yStart - 5, 107, 234, 7, 11);
					else if (yStart > yStop)
						drawTexturedModalRect(xStart - 5, yStart - 11 - 7, 96, 234, 11, 7);
					else if (yStart < yStop)
						drawTexturedModalRect(xStart - 5, yStart + 11, 96, 241, 11, 7);
				}

				selected = null;

				float f3 = (x - i1) * zoom;
				float f4 = (y - j1) * zoom;

				RenderHelper.enableGUIStandardItemLighting();
				GlStateManager.disableLighting();
				GlStateManager.enableRescaleNormal();
				GlStateManager.enableColorMaterial();

				for (Technology t2 : tech) {
					if (t2.hasCustomUnlock() && !t2.isResearched(player) && !t2.isUnlocked(player))
						continue;
					if (t2.isHidden() && !t2.hasCustomUnlock() && !t2.isResearched(player))
						continue;
					int l6 = t2.getX() * 24 - i;
					int j7 = t2.getY() * 24 - j;
					if (l6 < -24 || j7 < -24 || l6 > 224F * zoom || j7 > 155F * zoom)
						continue;

					int l7 = t2.requirementsUntilAvailable(player);
					if (l7 > 2)
						continue;

					boolean flag = t2.canResearchIgnoreResearched(player);
					if (t2.isResearched(player))
						GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
					else if (flag)
						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					else
						GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);

					mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
					GlStateManager.enableBlend();
					if (t2.hasCustomUnlock())
						drawTexturedModalRect(l6 - 2, j7 - 2, 26, 202, 26, 26);
					else
						drawTexturedModalRect(l6 - 2, j7 - 2, 0, 202, 26, 26);
					GlStateManager.disableBlend();

					if (!flag)
						GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);

					GlStateManager.disableLighting();
					GlStateManager.enableCull();
					itemRender.renderItemAndEffectIntoGUI(t2.getIcon(), l6 + 3, j7 + 3);
					GlStateManager.blendFunc(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA, net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					GlStateManager.disableLighting();

					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					if (f3 >= l6 && f3 <= l6 + 22 && f4 >= j7 && f4 <= j7 + 22 && t2.canResearchIgnoreResearched(player))
						selected = t2;
				}
			}
		} else {
			int wheel = Mouse.getDWheel();
			if (wheel < 0)
				scroll = Math.min(scroll + 1, pages);
			if (wheel > 0)
				scroll = Math.max(scroll - 1, 1);

			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableColorMaterial();
			for (int pos = 0; pos < num; pos++) {
				int n = pos + (num * (scroll - 1));
				if (n >= unlock.size())
					break;

				ItemListWildcard list = unlock.get(n);

				long tick = mc.world.getWorldTime() / 20;
				int index = (int) (tick % list.size());

				ItemStack item = list.get(index);

				mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
				GlStateManager.enableBlend();
				drawTexturedModalRect(6, 37 + (pos * 28), 0, 202, 26, 26);
				GlStateManager.disableBlend();

				GlStateManager.disableLighting();
				GlStateManager.enableCull();
				itemRender.renderItemAndEffectIntoGUI(item, 11, 42 + (pos * 28));
				GlStateManager.blendFunc(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA, net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableLighting();

				fontRenderer.drawStringWithShadow(I18n.format(list.toString() + ".name"), 35, 45 + (pos * 28), 0xFFFFFF);
			}
		}

		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
		drawTexturedModalRect(k, l, 0, 0, imageWidth, imageHeight);
		zLevel = 0.0F;
		GlStateManager.depthFunc(515);
		GlStateManager.disableDepth();
		GlStateManager.enableTexture2D();

		super.drawScreen(x, y, z);
		if (selected != null) {
			if (state == 0) {
				String s = selected.getLocalizedName(true).getFormattedText();
				String s1 = selected.getDescription().getFormattedText();

				int i7 = x + 12;
				int k7 = y - 4;

				int j8 = Math.max(fontRenderer.getStringWidth(s), 120);
				int i9 = fontRenderer.getWordWrappedHeight(s1, j8);
				if (selected.isResearched(player))
					i9 += 12;

				drawGradientRect(i7 - 3, k7 - 3, i7 + j8 + 3, k7 + i9 + 3 + 12, 0xc0000000, 0xc0000000);
				fontRenderer.drawSplitString(s1, i7, k7 + 12, j8, 0xffa0a0a0);
				if (selected.isResearched(player))
					fontRenderer.drawStringWithShadow(I18n.format("technology.researched"), i7, k7 + i9 + 4, 0xff9090ff);
				fontRenderer.drawStringWithShadow(s, i7, k7, -1);
			} else {
				String s1 = selected.getLocalizedName(true).getFormattedText();
				int x1 = (width - fontRenderer.getStringWidth(s1)) / 2;
				int y1 = (height - imageHeight) / 2;
				fontRenderer.drawStringWithShadow(s1, x1, y1 + 22, 0xffffff);

				String s2 = selected.getDescription().getFormattedText();
				int x2 = width / 2;
				int y2 = (height - imageHeight) / 2;
				drawSplitString(s2, x2, y2 + 32, split, 0xffa0a0a0, true);

				String s3 = scroll + "/" + pages;
				int x3 = (width + imageWidth) / 2 - fontRenderer.getStringWidth(s3);
				int y3 = (height + imageHeight) / 2;
				fontRenderer.drawStringWithShadow(s3, x3 - 21, y3 - 44, 0xffa0a0a0);
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
		RenderHelper.disableStandardItemLighting();
	}

	private void drawSplitString(String string, int x, int y, int split, int color, boolean shadow) {
		for (String s : fontRenderer.listFormattedStringToWidth(string, split)) {
			if (shadow)
				fontRenderer.drawStringWithShadow(s, x - (fontRenderer.getStringWidth(s) / 2), y, color);
			else
				fontRenderer.drawString(s, x - (fontRenderer.getStringWidth(s) / 2), y, color);
			y += fontRenderer.FONT_HEIGHT;
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}