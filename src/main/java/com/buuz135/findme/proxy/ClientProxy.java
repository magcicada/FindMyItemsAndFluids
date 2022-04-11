package com.buuz135.findme.proxy;

import com.buuz135.findme.FindMe;
import com.buuz135.findme.jei.JEIPlugin;
import com.buuz135.findme.network.PositionRequestMessage;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.gui.widgets.TankWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ClientProxy extends CommonProxy {

    public static KeyBinding KEY = new KeyBinding("key.findme.search", Keyboard.KEY_Y, "key.findme.category");
    public static KeyBinding KEY_FLUIDS = new KeyBinding("key.findme.search_fluids", Keyboard.KEY_T, "key.findme.category");
    private ItemStack stack = ItemStack.EMPTY;

    @Override
    public void preinit(FMLPreInitializationEvent event) {
        super.preinit(event);
        ClientRegistry.registerKeyBinding(KEY);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTooltip(RenderTooltipEvent.Pre event) {
        stack = event.getStack();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            stack = ItemStack.EMPTY;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void keyPress(GuiScreenEvent.KeyboardInputEvent.Post event) {
        if (Keyboard.getEventKeyState() && KEY.isActiveAndMatches(Keyboard.getEventKey()) && Minecraft.getMinecraft().currentScreen != null) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (!stack.isEmpty()) {
                FindMe.NETWORK.sendToServer(new PositionRequestMessage(stack));
            }
            if (screen instanceof GuiContainer) {
                Object o = JEIPlugin.runtime.getIngredientListOverlay().getIngredientUnderMouse();
                if (o != null) {
                    if (o instanceof ItemStack) {
                        FindMe.NETWORK.sendToServer(new PositionRequestMessage((ItemStack) o));
                    } else if (o instanceof FluidStack) {

                    }
                } else {
                    Slot slot = ((GuiContainer) screen).getSlotUnderMouse();
                    if (slot != null) {
                        ItemStack stack = slot.getStack();
                        if (!stack.isEmpty()) {
                            FindMe.NETWORK.sendToServer(new PositionRequestMessage(stack));
                        }
                    }
                }
            }
            if (screen instanceof ModularUIGui) {
                final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
                int i1 = scaledresolution.getScaledWidth();
                int j1 = scaledresolution.getScaledHeight();
                final int x = Mouse.getX() * i1 / Minecraft.getMinecraft().displayWidth;
                final int y = j1 - Mouse.getY() * j1 / Minecraft.getMinecraft().displayHeight - 1;
                for (Widget widget : ((ModularUIGui) screen).getModularUI().getFlatVisibleWidgetCollection()) {
                    if (widget instanceof TankWidget) {
                        if (widget.isMouseOverElement(x, y)) {

                        }
                    }
                }
            }
        }
    }

}
