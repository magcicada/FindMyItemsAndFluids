package com.hjae.findme.proxy;

import com.hjae.findme.FindMe;
import com.hjae.findme.network.FluidPositionRequestMessage;
import com.hjae.findme.network.PositionRequestMessage;
import mezz.jei.Internal;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClientProxy extends CommonProxy {

    public static KeyBinding KEY_ITEM = new KeyBinding("key.findme.search", Keyboard.KEY_Y, "key.findme.category");
    public static KeyBinding KEY_FLUIDS = new KeyBinding("key.findme.search_fluids", Keyboard.KEY_T, "key.findme.category");
    private ItemStack stack = ItemStack.EMPTY;
    private static Method getIngredientUnderMouseForKey;
    private static InputHandler inputHandler;
    private static Field inputHandlerField;

    @Override
    public void preinit(FMLPreInitializationEvent event) {
        super.preinit(event);
        ClientRegistry.registerKeyBinding(KEY_ITEM);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        try {
            getIngredientUnderMouseForKey = InputHandler.class.getDeclaredMethod("getIngredientUnderMouseForKey",
                    int.class, int.class);
            inputHandlerField = Internal.class.getDeclaredField("inputHandler");

        } catch (NoSuchMethodException | NoSuchFieldException e) {
            FindMe.logger.error("Error during reflection of JEI");
        }

        getIngredientUnderMouseForKey.setAccessible(true);
        inputHandlerField.setAccessible(true);
        try {
            inputHandler = (InputHandler) inputHandlerField.get(Internal.class);
        } catch (IllegalAccessException e) {
            FindMe.logger.error(e);
        }

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
        boolean items = KEY_ITEM.isActiveAndMatches(Keyboard.getEventKey());
        boolean fluids = KEY_FLUIDS.isActiveAndMatches(Keyboard.getEventKey());
        if (Keyboard.getEventKeyState() && (items || fluids) && Minecraft.getMinecraft().currentScreen != null) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (!stack.isEmpty()) {
                if (items) {
                    FindMe.NETWORK.sendToServer(new PositionRequestMessage(stack));
                } else if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)){
                    IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                    if (fluidHandler != null) {
                        FluidStack fluidStack = fluidHandler.drain(Integer.MAX_VALUE, false);
                        if (fluidStack != null) {
                            FindMe.NETWORK.sendToServer(new FluidPositionRequestMessage(fluidStack));
                        }
                    }
                }
            }
            if (screen instanceof GuiContainer) {
                final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
                int i1 = scaledresolution.getScaledWidth();
                int j1 = scaledresolution.getScaledHeight();
                final int x = Mouse.getX() * i1 / Minecraft.getMinecraft().displayWidth;
                final int y = j1 - Mouse.getY() * j1 / Minecraft.getMinecraft().displayHeight - 1;
                Object o = null;
                try {
                    o = getIngredientUnderMouseForKey.invoke(inputHandler, x, y);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    FindMe.logger.error("Error during reflection of getIngredientUnderMouseForKey" + e);
                }
                if (o != null) {
                    if (items && ((ClickedIngredient<?>) o).getValue() instanceof ItemStack) {
                        FindMe.NETWORK.sendToServer(new PositionRequestMessage((ItemStack) ((ClickedIngredient<?>) o).getValue()));
                    } else if (fluids && ((ClickedIngredient<?>) o).getValue() instanceof FluidStack) {
                        FindMe.NETWORK.sendToServer(new FluidPositionRequestMessage((FluidStack) ((ClickedIngredient<?>) o).getValue()));
                    }
                }
            }
        }
    }

}
