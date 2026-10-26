package com.scpretro.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

@Mod(SCPRetroMod.MODID)
public class SCPRetroMod {
    public static final String MODID = "scpretro";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> SCP_173_DOCUMENT = ITEMS.register("scp_173_document", 
        () -> new Item(new Item.Properties().stacksTo(1)) {
            @Override
            public InteractionResult useOn(UseOnContext context) {
                Level world = context.getLevel();
                if (!world.isClientSide()) {
                    BlockPos spawnPos = context.getClickedPos().above();
                    ServerLevel serverWorld = (ServerLevel) world;
                    StructureTemplateManager manager = serverWorld.getStructureManager();
                    
                    ResourceLocation structureLocation = new ResourceLocation(MODID, "scp_173_chamber");
                    Optional<StructureTemplate> templateOpt = manager.get(structureLocation);
                    
                    if (templateOpt.isPresent()) {
                        StructureTemplate template = templateOpt.get();
                        
                        // Robust placement settings to force modern 1.20.1 grid compliance
                        StructurePlaceSettings settings = new StructurePlaceSettings()
                                .setMirror(Mirror.NONE)
                                .setRotation(Rotation.NONE)
                                .setIgnoreEntities(false)
                                .setKeepLiquids(false);
                        
                        boolean success = template.placeInWorld(serverWorld, spawnPos, spawnPos, settings, serverWorld.random, 3);
                        
                        if (success) {
                            context.getPlayer().sendSystemMessage(Component.literal("§aFacility spawned successfully!"));
                            if (!context.getPlayer().isCreative()) {
                                context.getItemInHand().shrink(1);
                            }
                        } else {
                            context.getPlayer().sendSystemMessage(Component.literal("§cMinecraft refused to place the structure blocks. The NBT file might be corrupted or out-of-bounds."));
                        }
                    } else {
                        context.getPlayer().sendSystemMessage(Component.literal("§cError: Could not find 'scp_173_chamber.nbt' inside the mod files!"));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        });

    public SCPRetroMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SCP_173_DOCUMENT);
        }
    }
}
