/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reborncore.api.ToolManager;
import reborncore.common.blocks.BlockWrenchEventHandler;
import reborncore.common.multiblock.MultiblockEventHandler;
import reborncore.common.multiblock.MultiblockServerTickHandler;
import reborncore.common.network.NetworkManager;
import reborncore.common.network.RegisterPacketEvent;
import reborncore.common.network.packet.*;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.registration.RegistrationManager;
import reborncore.common.registration.RegistryConstructionEvent;
import reborncore.common.shields.RebornCoreShields;
import reborncore.common.shields.json.ShieldJsonLoader;
import reborncore.common.util.CalenderUtils;
import reborncore.common.util.CrashHandler;
import reborncore.common.util.GenericWrenchHelper;

import java.io.File;

@Mod(RebornCore.MOD_ID)
public class RebornCore {

	public static final String MOD_NAME = "Reborn Core";
	public static final String MOD_ID = "reborncore";
	public static final String MOD_VERSION = "@MODVERSION@";
	public static final String WEB_URL = "https://files.modmuss50.me/";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static CommonProxy proxy;
	public static File configDir;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CrashReportExtender.registerCrashCallable(new CrashHandler());
		//TODO this may explode, find a better way to get config dir :D
		configDir = new File(new File("config"), "teamreborn");
		if (!configDir.exists()) {
			configDir.mkdir();
		}
		//MinecraftForge.EVENT_BUS.register(ConfigRegistryFactory.class);
		//ConfigRegistryFactory.setConfigDir(configDir);
		RegistrationManager.init(event);
		RegistrationManager.load(new RegistryConstructionEvent());
		//ConfigRegistryFactory.saveAll();
		PowerSystem.selectedFile = (new File(configDir, "reborncore/selected_energy.json"));
		PowerSystem.readFile();
		CalenderUtils.loadCalender(); //Done early as some features need this
		proxy.preInit(event);
		ShieldJsonLoader.load(event);
		MinecraftForge.EVENT_BUS.register(this);

		RegistrationManager.load(event);

		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("ic2:wrench"), true));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("forestry:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("actuallyadditions:item_laser_wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("thermalfoundation:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("charset:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("teslacorelib:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("rftools:smartwrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("intergrateddynamics:smartwrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("correlated:weldthrower"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("chiselsandbits:wrench_wood"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("redstonearsenal:tool.wrench_flux"), false));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		// packets
		NetworkManager.load();

		RebornCoreShields.init();

		// Multiblock events
		MinecraftForge.EVENT_BUS.register(new MultiblockEventHandler());
		MinecraftForge.EVENT_BUS.register(new MultiblockServerTickHandler());
		MinecraftForge.EVENT_BUS.register(BlockWrenchEventHandler.class);

		proxy.init(event);
		RegistrationManager.load(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
		RegistrationManager.load(event);
	}

	@Mod.EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		LOGGER.error("Invalid fingerprint detected for Reborn Core!");
		RebornCore.proxy.invalidFingerprints.add("Invalid fingerprint detected for Reborn Core!");
	}

	@SubscribeEvent
	public void registerPackets(RegisterPacketEvent event) {
		event.registerPacket(CustomDescriptionPacket.class, Distribution.CLIENT);
		event.registerPacket(PacketSlotSave.class, Distribution.SERVER);
		event.registerPacket(PacketFluidConfigSave.class, Distribution.SERVER);
		event.registerPacket(PacketConfigSave.class, Distribution.SERVER);
		event.registerPacket(PacketSlotSync.class, Distribution.CLIENT);
		event.registerPacket(PacketFluidConfigSync.class, Distribution.CLIENT);
		event.registerPacket(PacketIOSave.class, Distribution.SERVER);
		event.registerPacket(PacketFluidIOSave.class, Distribution.SERVER);
		event.registerPacket(PacketSendLong.class, Distribution.CLIENT);
		event.registerPacket(PacketSendObject.class, Distribution.CLIENT);
	}

}
