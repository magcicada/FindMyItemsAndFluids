package com.hjae.findme.proxy;

import com.hjae.findme.network.FluidPositionRequestMessage;
import com.hjae.findme.network.PositionRequestMessage;
import com.hjae.findme.network.PositionResponseMessage;
import com.hjae.findme.FindMe;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {


    public void preinit(FMLPreInitializationEvent event) {
        FindMe.NETWORK.registerMessage(PositionRequestMessage.Handler.class, PositionRequestMessage.class, 0, Side.SERVER);
        FindMe.NETWORK.registerMessage(PositionResponseMessage.Handler.class, PositionResponseMessage.class, 1, Side.CLIENT);
        FindMe.NETWORK.registerMessage(FluidPositionRequestMessage.Handler.class, FluidPositionRequestMessage.class, 2, Side.SERVER);
    }


    public void init(FMLInitializationEvent event) {

    }


    public void postinit(FMLPostInitializationEvent event) {

    }

    public void loadComplete(FMLLoadCompleteEvent event) {

    }
}
