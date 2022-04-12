package com.hjae.findme.network;

import com.hjae.findme.FindMe;
import com.hjae.findme.proxy.FindMeConfig;
import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FluidPositionRequestMessage implements IMessage {

    private FluidStack fluid;

    public FluidPositionRequestMessage(FluidStack fluid) {
        this.fluid = fluid;
    }

    public FluidPositionRequestMessage() {
    }

    public static List<BlockPos> getBlockPosInAABB(AxisAlignedBB axisAlignedBB) {
        List<BlockPos> blocks = new ArrayList<BlockPos>();
        for (double y = axisAlignedBB.minY; y < axisAlignedBB.maxY; ++y) {
            for (double x = axisAlignedBB.minX; x < axisAlignedBB.maxX; ++x) {
                for (double z = axisAlignedBB.minZ; z < axisAlignedBB.maxZ; ++z) {
                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }
        return blocks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        NBTTagCompound fluidStackTag = null;
        try {
            fluidStackTag = packetBuffer.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fluidStackTag != null)
            fluid = FluidStack.loadFluidStackFromNBT(fluidStackTag);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        NBTTagCompound fluidStackTag = fluid.writeToNBT(new NBTTagCompound());
        packetBuffer.writeCompoundTag(fluidStackTag);
    }

    public static class Handler implements IMessageHandler<FluidPositionRequestMessage, PositionResponseMessage> {

        @Override
        public PositionResponseMessage onMessage(FluidPositionRequestMessage message, MessageContext ctx) {
            ctx.getServerHandler().player.world.getMinecraftServer().addScheduledTask(() -> {
                AxisAlignedBB box = new AxisAlignedBB(ctx.getServerHandler().player.getPosition()).grow(FindMeConfig.RADIUS_RANGE);
                List<BlockPos> blockPosList = new ArrayList<>();
                for (BlockPos blockPos : getBlockPosInAABB(box)) {
                    TileEntity tileEntity = ctx.getServerHandler().player.world.getTileEntity(blockPos);
                    if (tileEntity != null) {
                        if (tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                            IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                            if (handler != null) {
                                for (IFluidTankProperties property: handler.getTankProperties()) {
                                    if (property.getContents() != null) {
                                        if (property.getContents().isFluidEqual(message.fluid)) {
                                            blockPosList.add(blockPos);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!blockPosList.isEmpty())
                    FindMe.NETWORK.sendTo(new PositionResponseMessage(blockPosList), ctx.getServerHandler().player);
            });
            return null;
        }
    }
}
