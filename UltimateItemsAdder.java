package com.ultimateitemsadder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.*;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.io.FileUtils;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.lang.management.ManagementFactory;
import java.util.Date;

public class UltimateItemsAdder extends JavaPlugin implements Listener {

    // Ana değişkenler
    private static UltimateItemsAdder instance;
    private final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&d[UltimateItems] &7");
    private final Map<String, CustomItem> customItems = new HashMap<>();
    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final Map<String, CustomBlock> customBlocks = new HashMap<>();
    private final Map<String, CustomRecipe> customRecipes = new HashMap<>();
    private final Map<String, CustomEnchant> customEnchants = new HashMap<>();
    private final Map<String, CustomEffect> customEffects = new HashMap<>();
    private final Map<String, CustomAbility> customAbilities = new HashMap<>();
    private final Map<String, CustomArmor> customArmors = new HashMap<>();
    private final Map<String, CustomWeapon> customWeapons = new HashMap<>();
    private final Map<String, CustomTool> customTools = new HashMap<>();
    private final Map<String, CustomFood> customFoods = new HashMap<>();
    private final Map<String, CustomPotion> customPotions = new HashMap<>();
    private final Map<String, CustomProjectile> customProjectiles = new HashMap<>();
    private final Map<String, CustomFuel> customFuels = new HashMap<>();
    private final Map<String, CustomOrb> customOrbs = new HashMap<>();
    private final Map<String, CustomGUI> customGUIs = new HashMap<>();
    private final Map<String, LootTable> lootTables = new HashMap<>();
    private final Map<String, CustomCommand> customCommands = new HashMap<>();
    private final Map<String, CustomPermission> customPermissions = new HashMap<>();
    private final Map<String, CustomWorld> customWorlds = new HashMap<>();
    private final Map<String, CustomBiome> customBiomes = new HashMap<>();
    private final Map<String, CustomStructure> customStructures = new HashMap<>();
    private final Map<String, CustomMob> customMobs = new HashMap<>();
    private final Map<String, CustomVillagerTrade> customTrades = new HashMap<>();
    private final Map<String, CustomAdvancement> customAdvancements = new HashMap<>();
    private final Map<String, CustomDimension> customDimensions = new HashMap<>();
    private final Map<String, CustomEvent> customEvents = new HashMap<>();
    private final Map<String, CustomVariable> customVariables = new HashMap<>();
    private final Map<String, CustomSound> customSounds = new HashMap<>();
    private final Map<String, CustomParticle> customParticles = new HashMap<>();
    private final Map<String, CustomFont> customFonts = new HashMap<>();
    private final Map<String, CustomModel> customModels = new HashMap<>();
    private final Map<String, CustomTexture> customTextures = new HashMap<>();
    private final Map<String, AnimatedTexture> animatedTextures = new HashMap<>();
    private final Map<String, CustomShader> customShaders = new HashMap<>();
    private final Map<String, CustomLanguage> customLanguages = new HashMap<>();
    private final Map<String, CustomCategory> customCategories = new HashMap<>();
    private final Map<String, CustomRarity> customRarities = new HashMap<>();
    private final Map<String, CustomTier> customTiers = new HashMap<>();
    private final Map<String, CustomSet> customSets = new HashMap<>();
    private final Map<String, CustomQuest> customQuests = new HashMap<>();
    private final Map<String, CustomSkill> customSkills = new HashMap<>();
    private final Map<String, CustomClass> customClasses = new HashMap<>();
    private final Map<String, CustomAttribute> customAttributes = new HashMap<>();
    private final Map<String, CustomStat> customStats = new HashMap<>();
    private final Map<String, CustomCurrency> customCurrencies = new HashMap<>();
    private final Map<String, CustomEconomy> customEconomies = new HashMap<>();
    private final Map<String, CustomShop> customShops = new HashMap<>();
    private final Map<String, CustomAuction> customAuctions = new HashMap<>();
    private final Map<String, CustomMarket> customMarkets = new HashMap<>();
    private final Map<String, CustomBank> customBanks = new HashMap<>();
    private final Map<String, CustomVault> customVaults = new HashMap<>();
    private final Map<String, CustomStorage> customStorages = new HashMap<>();
    private final Map<String, CustomBackpack> customBackpacks = new HashMap<>();
    private final Map<String, CustomEnderchest> customEnderchests = new HashMap<>();
    private final Map<String, CustomHome> customHomes = new HashMap<>();
    private final Map<String, CustomWarp> customWarps = new HashMap<>();
    private final Map<String, CustomTeleport> customTeleports = new HashMap<>();
    private final Map<String, CustomPortal> customPortals = new HashMap<>();
    private final Map<String, CustomGate> customGates = new HashMap<>();
    private final Map<String, CustomBridge> customBridges = new HashMap<>();
    private final Map<String, CustomElevator> customElevators = new HashMap<>();
    private final Map<String, CustomVehicle> customVehicles = new HashMap<>();
    private final Map<String, CustomMount> customMounts = new HashMap<>();
    private final Map<String, CustomPet> customPets = new HashMap<>();
    private final Map<String, CustomCompanion> customCompanions = new HashMap<>();
    private final Map<String, CustomMinion> customMinions = new HashMap<>();
    private final Map<String, CustomGolem> customGolems = new HashMap<>();
    private final Map<String, CustomTurret> customTurrets = new HashMap<>();
    private final Map<String, CustomTrap> customTraps = new HashMap<>();
    private final Map<String, CustomBarrier> customBarriers = new HashMap<>();
    private final Map<String, CustomShield> customShields = new HashMap<>();
    private final Map<String, CustomForceField> customForceFields = new HashMap<>();
    private final Map<String, CustomGenerator> customGenerators = new HashMap<>();
    private final Map<String, CustomSpawner> customSpawners = new HashMap<>();
    private final Map<String, CustomFarm> customFarms = new HashMap<>();
    private final Map<String, CustomMachine> customMachines = new HashMap<>();
    private final Map<String, CustomFactory> customFactories = new HashMap<>();
    private final Map<String, CustomReactor> customReactors = new HashMap<>();
    private final Map<String, CustomPower> customPowers = new HashMap<>();
    private final Map<String, CustomEnergy> customEnergies = new HashMap<>();
    private final Map<String, CustomMana> customManas = new HashMap<>();
    private final Map<String, CustomStamina> customStaminas = new HashMap<>();
    private final Map<String, CustomHealth> customHealths = new HashMap<>();
    private final Map<String, CustomHunger> customHungers = new HashMap<>();
    private final Map<String, CustomThirst> customThirsts = new HashMap<>();
    private final Map<String, CustomTemperature> customTemperatures = new HashMap<>();
    private final Map<String, CustomRadiation> customRadiations = new HashMap<>();
    private final Map<String, CustomInfection> customInfections = new HashMap<>();
    private final Map<String, CustomCurse> customCurses = new HashMap<>();
    private final Map<String, CustomBless> customBlesses = new HashMap<>();
    private final Map<String, CustomBuff> customBuffs = new HashMap<>();
    private final Map<String, CustomDebuff> customDebuffs = new HashMap<>();
    private final Map<String, CustomAura> customAuras = new HashMap<>();
    private final Map<String, CustomField> customFields = new HashMap<>();
    private final Map<String, CustomZone> customZones = new HashMap<>();
    private final Map<String, CustomRegion> customRegions = new HashMap<>();
    private final Map<String, CustomTerritory> customTerritories = new HashMap<>();
    private final Map<String, CustomKingdom> customKingdoms = new HashMap<>();
    private final Map<String, CustomEmpire> customEmpires = new HashMap<>();
    private final Map<String, CustomNation> customNations = new HashMap<>();
    private final Map<String, CustomFaction> customFactions = new HashMap<>();
    private final Map<String, CustomGuild> customGuilds = new HashMap<>();
    private final Map<String, CustomClan> customClans = new HashMap<>();
    private final Map<String, CustomParty> customParties = new HashMap<>();
    private final Map<String, CustomTeam> customTeams = new HashMap<>();
    private final Map<String, CustomAlliance> customAlliances = new HashMap<>();
    private final Map<String, CustomCoalition> customCoalitions = new HashMap<>();
    private final Map<String, CustomRank> customRanks = new HashMap<>();
    private final Map<String, CustomTitle> customTitles = new HashMap<>();
    private final Map<String, CustomBadge> customBadges = new HashMap<>();
    private final Map<String, CustomAchievement> customAchievements = new HashMap<>();
    private final Map<String, CustomReward> customRewards = new HashMap<>();
    private final Map<String, CustomLoot> customLoots = new HashMap<>();
    private final Map<String, CustomTreasure> customTreasures = new HashMap<>();
    private final Map<String, CustomChest> customChests = new HashMap<>();
    private final Map<String, CustomCrate> customCrates = new HashMap<>();
    private final Map<String, CustomBox> customBoxes = new HashMap<>();
    private final Map<String, CustomContainer> customContainers = new HashMap<>();
    private final Map<String, CustomPackage> customPackages = new HashMap<>();
    private final Map<String, CustomBundle> customBundles = new HashMap<>();
    private final Map<String, CustomKit> customKits = new HashMap<>();
    private final Map<String, CustomLoadout> customLoadouts = new HashMap<>();
    private final Map<String, CustomPreset> customPresets = new HashMap<>();
    private final Map<String, CustomTemplate> customTemplates = new HashMap<>();
    private final Map<String, CustomSchematic> customSchematics = new HashMap<>();
    private final Map<String, CustomBlueprint> customBlueprints = new HashMap<>();
    private final Map<String, CustomDesign> customDesigns = new HashMap<>();
    private final Map<String, CustomPattern> customPatterns = new HashMap<>();
    private final Map<String, CustomFormula> customFormulas = new HashMap<>();
    private final Map<String, CustomEquation> customEquations = new HashMap<>();
    private final Map<String, CustomAlgorithm> customAlgorithms = new HashMap<>();
    private final Map<String, CustomScript> customScripts = new HashMap<>();
    private final Map<String, CustomMacro> customMacros = new HashMap<>();
    private final Map<String, CustomFunction> customFunctions = new HashMap<>();
    private final Map<String, CustomProcedure> customProcedures = new HashMap<>();
    private final Map<String, CustomRoutine> customRoutines = new HashMap<>();
    private final Map<String, CustomSequence> customSequences = new HashMap<>();
    private final Map<String, CustomChain> customChains = new HashMap<>();
    private final Map<String, CustomCombo> customCombos = new HashMap<>();
    private final Map<String, CustomLink> customLinks = new HashMap<>();
    private final Map<String, CustomBond> customBonds = new HashMap<>();
    private final Map<String, CustomConnection> customConnections = new HashMap<>();
    private final Map<String, CustomRelation> customRelations = new HashMap<>();
    private final Map<String, CustomAssociation> customAssociations = new HashMap<>();
    private final Map<String, CustomCorrelation> customCorrelations = new HashMap<>();
    private final Map<String, CustomDependency> customDependencies = new HashMap<>();
    private final Map<String, CustomRequirement> customRequirements = new HashMap<>();
    private final Map<String, CustomCondition> customConditions = new HashMap<>();
    private final Map<String, CustomConstraint> customConstraints = new HashMap<>();
    private final Map<String, CustomRestriction> customRestrictions = new HashMap<>();
    private final Map<String, CustomLimitation> customLimitations = new HashMap<>();
    private final Map<String, CustomBoundary> customBoundaries = new HashMap<>();
    private final Map<String, CustomThreshold> customThresholds = new HashMap<>();
    private final Map<String, CustomLimit> customLimits = new HashMap<>();
    private final Map<String, CustomCapacity> customCapacities = new HashMap<>();
    private final Map<String, CustomMaximum> customMaximums = new HashMap<>();
    private final Map<String, CustomMinimum> customMinimums = new HashMap<>();
    private final Map<String, CustomRange> customRanges = new HashMap<>();
    private final Map<String, CustomScale> customScales = new HashMap<>();
    private final Map<String, CustomRatio> customRatios = new HashMap<>();
    private final Map<String, CustomProportion> customProportions = new HashMap<>();
    private final Map<String, CustomPercentage> customPercentages = new HashMap<>();
    private final Map<String, CustomFraction> customFractions = new HashMap<>();
    private final Map<String, CustomDecimal> customDecimals = new HashMap<>();
    private final Map<String, CustomInteger> customIntegers = new HashMap<>();
    private final Map<String, CustomFloat> customFloats = new HashMap<>();
    private final Map<String, CustomDouble> customDoubles = new HashMap<>();
    private final Map<String, CustomLong> customLongs = new HashMap<>();
    private final Map<String, CustomShort> customShorts = new HashMap<>();
    private final Map<String, CustomByte> customBytes = new HashMap<>();
    private final Map<String, CustomBoolean> customBooleans = new HashMap<>();
    private final Map<String, CustomString> customStrings = new HashMap<>();
    private final Map<String, CustomCharacter> customCharacters = new HashMap<>();
    private final Map<String, CustomArray> customArrays = new HashMap<>();
    private final Map<String, CustomList> customLists = new HashMap<>();
    private final Map<String, CustomSet> customSets2 = new HashMap<>();
    private final Map<String, CustomMap> customMaps = new HashMap<>();
    private final Map<String, CustomQueue> customQueues = new HashMap<>();
    private final Map<String, CustomStack> customStacks = new HashMap<>();
    private final Map<String, CustomTree> customTrees = new HashMap<>();
    private final Map<String, CustomGraph> customGraphs = new HashMap<>();
    private final Map<String, CustomMatrix> customMatrices = new HashMap<>();
    private final Map<String, CustomVector> customVectors = new HashMap<>();
    private final Map<String, CustomTensor> customTensors = new HashMap<>();
    private final Map<String, CustomTable> customTables = new HashMap<>();
    private final Map<String, CustomDatabase> customDatabases = new HashMap<>();
    private final Map<String, CustomCache> customCaches = new HashMap<>();
    private final Map<String, CustomBuffer> customBuffers = new HashMap<>();
    private final Map<String, CustomStream> customStreams = new HashMap<>();
    private final Map<String, CustomChannel> customChannels = new HashMap<>();
    private final Map<String, CustomPipeline> customPipelines = new HashMap<>();
    private final Map<String, CustomFlow> customFlows = new HashMap<>();
    private final Map<String, CustomProcess> customProcesses = new HashMap<>();
    private final Map<String, CustomOperation> customOperations = new HashMap<>();
    private final Map<String, CustomAction> customActions = new HashMap<>();
    private final Map<String, CustomTask> customTasks = new HashMap<>();
    private final Map<String, CustomJob> customJobs = new HashMap<>();
    private final Map<String, CustomWork> customWorks = new HashMap<>();
    private final Map<String, CustomService> customServices = new HashMap<>();
    private final Map<String, CustomSystem> customSystems = new HashMap<>();
    private final Map<String, CustomPlatform> customPlatforms = new HashMap<>();
    private final Map<String, CustomFramework> customFrameworks = new HashMap<>();
    private final Map<String, CustomLibrary> customLibraries = new HashMap<>();
    private final Map<String, CustomModule> customModules = new HashMap<>();
    private final Map<String, CustomComponent> customComponents = new HashMap<>();
    private final Map<String, CustomElement> customElements = new HashMap<>();
    private final Map<String, CustomUnit> customUnits = new HashMap<>();
    private final Map<String, CustomPart> customParts = new HashMap<>();
    private final Map<String, CustomPiece> customPieces = new HashMap<>();
    private final Map<String, CustomFragment> customFragments = new HashMap<>();
    private final Map<String, CustomSegment> customSegments = new HashMap<>();
    private final Map<String, CustomSection> customSections = new HashMap<>();
    private final Map<String, CustomChapter> customChapters = new HashMap<>();
    private final Map<String, CustomVolume> customVolumes = new HashMap<>();
    private final Map<String, CustomEdition> customEditions = new HashMap<>();
    private final Map<String, CustomVersion> customVersions = new HashMap<>();
    private final Map<String, CustomRelease> customReleases = new HashMap<>();
    private final Map<String, CustomUpdate> customUpdates = new HashMap<>();
    private final Map<String, CustomPatch> customPatches = new HashMap<>();
    private final Map<String, CustomFix> customFixes = new HashMap<>();
    private final Map<String, CustomImprovement> customImprovements = new HashMap<>();
    private final Map<String, CustomEnhancement> customEnhancements = new HashMap<>();
    private final Map<String, CustomOptimization> customOptimizations = new HashMap<>();
    private final Map<String, CustomRefactor> customRefactors = new HashMap<>();

    // Resource pack değişkenleri
    private HttpServer httpServer;
    private String resourcePackUrl;
    private byte[] resourcePackHash;
    private final String namespace = "ultimateitems";
    private final Path resourcePackPath = Paths.get(getDataFolder().getAbsolutePath(), "resource_pack");
    private final Path texturesPath = Paths.get(getDataFolder().getAbsolutePath(), "textures");
    private final Path configsPath = Paths.get(getDataFolder().getAbsolutePath(), "configs");
    private final Path generatedPackPath = Paths.get(getDataFolder().getAbsolutePath(), "generated_pack.zip");

    // Yapılandırma dosyaları
    private FileConfiguration mainConfig;
    private FileConfiguration itemsConfig;
    private FileConfiguration blocksConfig;
    private FileConfiguration recipesConfig;
    private FileConfiguration enchantsConfig;
    private FileConfiguration effectsConfig;
    private FileConfiguration abilitiesConfig;
    private FileConfiguration languageConfig;
    private FileConfiguration permissionsConfig;

    // Sistem değişkenleri
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Random random = new Random();
    private final Set<UUID> resourcePackPendingPlayers = new HashSet<>();
    private int serverPort;
    private boolean debugMode;
    private boolean autoUpdate;
    private boolean metricsEnabled;
    private String databaseType;
    private Connection databaseConnection;

    // İç sınıflar
    private static class CustomItem {
        public HashMap bonuses;
        String id;
        String displayName;
        List<String> lore;
        Material baseMaterial;
        String texture;
        String model;
        int customModelData;
        Map<String, Object> attributes;
        Map<String, Integer> enchantments;
        List<String> flags;
        List<String> abilities;
        List<String> effects;
        String category;
        String rarity;
        int tier;
        double damage;
        double defense;
        double speed;
        double health;
        double mana;
        boolean unbreakable;
        boolean glowing;
        Map<String, Object> nbt;
        Map<String, Object> metadata;
        List<String> recipes;
        Map<String, Object> requirements;
        Map<String, Object> permissions;
        List<String> commands;
        Map<String, Object> events;
        Map<String, Object> triggers;
        Map<String, Object> conditions;
        Map<String, Object> actions;
        Map<String, Object> variables;
        Map<String, Object> placeholders;
        Map<String, String> sounds;
        Map<String, String> particles;
        Map<String, Object> animations;
        Map<String, Object> tooltips;
        Map<String, Object> interactions;
        boolean stackable;
        int maxStack;
        double weight;
        double value;
        String currency;
        boolean tradeable;
        boolean droppable;
        boolean destroyable;
        boolean repairable;
        int durability;
        int maxDurability;
        double repairCost;
        List<String> repairMaterials;
        Map<String, Object> upgrades;
        Map<String, Object> modifications;
        Map<String, Object> augments;
        Map<String, Object> gems;
        Map<String, Object> runes;
        Map<String, Object> inscriptions;
        Map<String, Object> imbuements;
        Map<String, Object> infusions;
        Map<String, Object> enchantmentSlots;
        Map<String, Object> socketSlots;
        Map<String, Object> upgradeSlots;
        boolean soulbound;
        boolean questItem;
        boolean unique;
        boolean legendary;
        boolean mythic;
        boolean artifact;
        boolean relic;
        boolean ancient;
        boolean divine;
        boolean cursed;
        boolean blessed;
        String set;
        Map<String, Object> setBonuses;
        List<String> classes;
        List<String> races;
        int levelRequirement;
        Map<String, Integer> statRequirements;
        Map<String, Integer> skillRequirements;
        Map<String, Object> cooldowns;
        Map<String, Object> charges;
        Map<String, Object> ammunition;
        Map<String, Object> fuel;
        Map<String, Object> energy;
        Map<String, Object> heat;
        Map<String, Object> radiation;
        boolean twoHanded;
        String slot;
        String weaponType;
        String armorType;
        String toolType;
        String accessoryType;
        double attackSpeed;
        double critChance;
        double critDamage;
        double blockChance;
        double dodgeChance;
        double parryChance;
        double lifesteal;
        double manasteal;
        double expBonus;
        double lootBonus;
        double goldBonus;
        Map<String, Double> resistances;
        Map<String, Double> penetrations;
        Map<String, Double> amplifications;
        Map<String, Object> specialEffects;
        Map<String, Object> onHit;
        Map<String, Object> onKill;
        Map<String, Object> onDeath;
        Map<String, Object> onEquip;
        Map<String, Object> onUnequip;
        Map<String, Object> onUse;
        Map<String, Object> onBreak;
        Map<String, Object> onRepair;
        Map<String, Object> onUpgrade;
        Map<String, Object> onEnchant;
        Map<String, Object> onSocket;
        Map<String, Object> onCraft;
        Map<String, Object> onSmelt;
        Map<String, Object> onBrew;
        long createdAt;
        long updatedAt;
        String creator;
        String lastModifier;
        Map<String, Object> statistics;
        Map<String, Object> analytics;
        boolean enabled;

        public ItemStack createItemStack() {
            ItemStack item = new ItemStack(baseMaterial);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                // İsim
                if (displayName != null && !displayName.isEmpty()) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                }

                // Lore
                if (lore != null && !lore.isEmpty()) {
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                    meta.setLore(coloredLore);
                }

                // Custom Model Data
                if (customModelData > 0) {
                    meta.setCustomModelData(customModelData);
                }

                // Enchantments
                if (enchantments != null) {
                    for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey().toLowerCase()));
                        if (enchant != null) {
                            meta.addEnchant(enchant, entry.getValue(), true);
                        }
                    }
                }

                // Flags
                if (flags != null) {
                    for (String flag : flags) {
                        try {
                            meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }

                // Unbreakable
                if (unbreakable) {
                    meta.setUnbreakable(true);
                }

        /* ---------------------------------------------------------
           AttributeModifier API, 1.21+
           Tüm eski ctor'lar deprecated → NamespacedKey + SlotGroup
        ---------------------------------------------------------- */
                if (attributes != null) {
                    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                        try {
                            Attribute attribute = Attribute.valueOf(entry.getKey().toUpperCase());
                            double value = Double.parseDouble(entry.getValue().toString());

                            AttributeModifier modifier = new AttributeModifier(
                                    new NamespacedKey(UltimateItemsAdder.instance,
                                            "ultimateitems." + id + "." + attribute.name().toLowerCase()),
                                    value,
                                    AttributeModifier.Operation.ADD_NUMBER,
                                    EquipmentSlotGroup.HAND          // MAIN + OFF hand
                            );
                            meta.addAttributeModifier(attribute, modifier);
                        } catch (Exception ignored) {
                        }
                    }
                }

                // Persistent Data
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(UltimateItemsAdder.instance, "custom_item_id"),
                        PersistentDataType.STRING,
                        id
                );

                // NBT Data
                if (nbt != null) {
                    for (Map.Entry<String, Object> entry : nbt.entrySet()) {
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(UltimateItemsAdder.instance, entry.getKey()),
                                PersistentDataType.STRING,
                                entry.getValue().toString()
                        );
                    }
                }

                item.setItemMeta(meta);
            }

            return item;
        }
    }

    private static class PlayerData {
        UUID uuid;
        String name;
        Map<String, Integer> itemCooldowns = new HashMap<>();
        Map<String, Long> abilityCooldowns = new HashMap<>();
        Map<String, Integer> charges = new HashMap<>();
        Map<String, Object> variables = new HashMap<>();
        Set<String> unlockedItems = new HashSet<>();
        Set<String> activeEffects = new HashSet<>();
        Map<String, Integer> statistics = new HashMap<>();
        boolean resourcePackLoaded = false;
        long lastSeen;
    }

    private static class CustomBlock {
        String id;
        String displayName;
        Material baseMaterial;
        String texture;
        String model;
        double hardness;
        double explosionResistance;
        String tool;
        int toolLevel;
        List<String> drops;
        Map<String, Object> properties;
        boolean solid;
        boolean transparent;
        boolean gravity;
        int lightLevel;
        Map<String, Object> events;
    }

    private static class CustomRecipe {
        String id;
        String type;
        String result;
        int amount;
        Map<String, String> ingredients;
        String[] pattern;
        int cookingTime;
        float experience;
        String group;
        Map<String, Object> conditions;
        Map<String, Object> rewards;
    }

    private static class CustomEnchant {
        String id;
        String displayName;
        int maxLevel;
        Map<Integer, Map<String, Object>> levelEffects;
        List<String> applicableItems;
        List<String> conflicts;
        boolean treasure;
        boolean cursed;
        String rarity;
        Map<String, Object> effects;
    }

    private static class CustomEffect {
        public String particle;
        String id;
        String displayName;
        PotionEffectType type;
        int duration;
        int amplifier;
        boolean ambient;
        boolean particles;
        boolean icon;
        Color color;
        Map<String, Object> modifiers;
        List<String> triggers;
    }

    private static class CustomAbility {
        String id;
        String displayName;
        String description;
        int cooldown;
        int manaCost;
        int staminaCost;
        int healthCost;
        int charges;
        double range;
        double damage;
        double radius;
        String activation;
        List<String> effects;
        Map<String, Object> conditions;
        Map<String, Object> actions;
        String animation;
        String sound;
        String particle;
    }

    private static class CustomArmor {
        String id;
        String slot;
        double defense;
        double toughness;
        double knockbackResistance;
        Map<String, Double> resistances;
        Map<String, Object> setBonuses;
        List<String> abilities;
    }

    private static class CustomWeapon {
        String id;
        String type;
        double damage;
        double attackSpeed;
        double reach;
        double critChance;
        double critMultiplier;
        String damageType;
        List<String> abilities;
        Map<String, Object> specialAttacks;
    }

    private static class CustomTool {
        String id;
        String type;
        double efficiency;
        int harvestLevel;
        List<String> harvestableBlocks;
        Map<String, Double> bonuses;
        boolean silkTouch;
        int fortune;
        List<String> abilities;
    }

    private static class CustomFood {
        String id;
        int hunger;
        float saturation;
        List<PotionEffect> effects;
        double eatSpeed;
        boolean alwaysEdible;
        boolean meat;
        Map<String, Object> bonuses;
    }

    private static class CustomPotion {
        String id;
        String displayName;
        List<PotionEffect> effects;
        Color color;
        boolean splash;
        boolean lingering;
        int duration;
        Map<String, Object> modifiers;
    }

    private static class CustomProjectile {
        String id;
        String type;
        double velocity;
        double damage;
        double accuracy;
        boolean gravity;
        boolean piercing;
        int pierceLevel;
        String trail;
        Map<String, Object> effects;
    }

    private static class CustomFuel {
        String id;
        int burnTime;
        double efficiency;
        List<String> applicableFurnaces;
        Map<String, Object> bonuses;
    }

    private static class CustomOrb {
        String id;
        int experience;
        String type;
        Map<String, Object> bonuses;
    }

    private static class CustomGUI {
        String id;
        String title;
        int size;
        Map<Integer, GuiItem> items;
        Map<String, Object> properties;
    }

    private static class GuiItem {
        ItemStack item;
        String action;
        Map<String, Object> data;
    }

    private static class LootTable {
        String id;
        List<LootEntry> entries;
        Map<String, Object> conditions;
        double bonusChance;
    }

    private static class LootEntry {
        String item;
        double chance;
        int minAmount;
        int maxAmount;
        Map<String, Object> conditions;
    }

    private static class CustomCommand {
        String command;
        String permission;
        List<String> aliases;
        String description;
        String usage;
        Map<String, Object> actions;
    }

    private static class CustomPermission {
        String permission;
        String description;
        boolean defaultValue;
        Map<String, Object> children;
    }

    private static class CustomWorld {
        String id;
        String name;
        String type;
        Map<String, Object> settings;
        List<String> biomes;
        Map<String, Object> structures;
    }

    private static class CustomBiome {
        String id;
        String name;
        double temperature;
        double humidity;
        Map<String, Object> features;
        List<String> mobs;
    }

    private static class CustomStructure {
        String id;
        String name;
        String schematic;
        Map<String, Object> loot;
        List<String> spawns;
    }

    private static class CustomMob {
        String id;
        String displayName;
        EntityType type;
        double health;
        double damage;
        double speed;
        List<String> drops;
        Map<String, Object> attributes;
        List<String> abilities;
    }

    private static class CustomVillagerTrade {
        String id;
        Villager.Profession profession;
        int level;
        ItemStack input1;
        ItemStack input2;
        ItemStack output;
        int maxUses;
        int villagerExperience;
        float priceMultiplier;
    }

    private static class CustomAdvancement {
        String id;
        String parent;
        String icon;
        String title;
        String description;
        Map<String, Object> criteria;
        Map<String, Object> rewards;
    }

    private static class CustomDimension {
        String id;
        String name;
        String type;
        Map<String, Object> properties;
    }

    private static class CustomEvent {
        String id;
        String trigger;
        Map<String, Object> conditions;
        Map<String, Object> actions;
    }

    private static class CustomVariable {
        String id;
        Object value;
        String type;
        boolean persistent;
    }

    private static class CustomSound {
        String id;
        String sound;
        float volume;
        float pitch;
        String category;
    }

    private static class CustomParticle {
        String id;
        Particle particle;
        int count;
        double offsetX;
        double offsetY;
        double offsetZ;
        double speed;
        Object data;
    }

    private static class CustomFont {
        String id;
        String name;
        Map<Character, String> characters;
    }

    private static class CustomModel {
        String id;
        String parent;
        Map<String, String> textures;
        List<ModelElement> elements;
        Map<String, Object> display;
        List<Override> overrides;
    }

    private static class ModelElement {
        double[] from;
        double[] to;
        Map<String, Face> faces;
        Rotation rotation;
        boolean shade;
    }

    private static class Face {
        String texture;
        double[] uv;
        String cullface;
        int rotation;
        int tintindex;
    }

    private static class Rotation {
        double[] origin;
        String axis;
        double angle;
        boolean rescale;
    }

    private static class Override {
        Map<String, Object> predicate;
        String model;
    }

    private static class CustomTexture {
        String id;
        String path;
        BufferedImage image;
        Map<String, Object> properties;
    }

    private static class AnimatedTexture extends CustomTexture {
        List<BufferedImage> frames;
        int frameTime;
        boolean interpolate;
    }

    private static class CustomShader {
        String id;
        String vertex;
        String fragment;
        Map<String, Object> uniforms;
    }

    private static class CustomLanguage {
        String code;
        String name;
        Map<String, String> translations;
    }

    private static class CustomCategory {
        String id;
        String name;
        String icon;
        int priority;
        List<String> items;
    }

    private static class CustomRarity {
        String id;
        String name;
        String color;
        double dropChance;
        Map<String, Object> bonuses;
    }

    private static class CustomTier {
        String id;
        int level;
        String name;
        Map<String, Object> requirements;
        Map<String, Object> bonuses;
    }

    private static class CustomSet {
        String id;
        String name;
        List<String> items;
        Map<Integer, Map<String, Object>> bonuses;
    }

    private static class CustomQuest {
        String id;
        String name;
        String description;
        Map<String, Object> objectives;
        Map<String, Object> rewards;
        Map<String, Object> requirements;
    }

    private static class CustomSkill {
        String id;
        String name;
        int maxLevel;
        Map<Integer, Map<String, Object>> levels;
        String type;
    }

    private static class CustomClass {
        String id;
        String name;
        String description;
        Map<String, Object> startingItems;
        Map<String, Object> abilities;
        Map<String, Object> stats;
    }

    private static class CustomAttribute {
        String id;
        String name;
        double baseValue;
        double maxValue;
        String formula;
    }

    private static class CustomStat {
        String id;
        String name;
        double value;
        String type;
        Map<String, Object> modifiers;
    }

    private static class CustomCurrency {
        String id;
        String name;
        String symbol;
        double exchangeRate;
        int decimals;
    }

    private static class CustomEconomy {
        String id;
        Map<String, Double> balances;
        Map<String, Object> settings;
    }

    private static class CustomShop {
        String id;
        String name;
        Map<String, ShopItem> items;
        String currency;
        Map<String, Object> settings;
    }

    private static class ShopItem {
        String item;
        double buyPrice;
        double sellPrice;
        int stock;
        Map<String, Object> conditions;
    }

    private static class CustomAuction {
        String id;
        Map<String, AuctionItem> items;
        Map<String, Object> settings;
    }

    private static class AuctionItem {
        String seller;
        ItemStack item;
        double price;
        double buyoutPrice;
        long endTime;
        Map<String, Double> bids;
    }

    private static class CustomMarket {
        String id;
        Map<String, MarketListing> listings;
        Map<String, Object> settings;
    }

    private static class MarketListing {
        String seller;
        String item;
        int amount;
        double pricePerUnit;
        long listingTime;
    }

    private static class CustomBank {
        String id;
        Map<String, BankAccount> accounts;
        Map<String, Object> settings;
    }

    private static class BankAccount {
        String owner;
        double balance;
        Map<String, Integer> items;
        int level;
        int slots;
    }

    private static class CustomVault {
        String id;
        Map<String, VaultStorage> vaults;
        Map<String, Object> settings;
    }

    private static class VaultStorage {
        String owner;
        Map<Integer, ItemStack> items;
        int size;
        String permission;
    }

    private static class CustomStorage {
        String id;
        Map<Integer, ItemStack> items;
        int size;
        String type;
        Map<String, Object> properties;
    }

    private static class CustomBackpack {
        String id;
        String name;
        int size;
        Map<Integer, ItemStack> contents;
        Map<String, Object> properties;
    }

    private static class CustomEnderchest {
        String id;
        Map<String, EnderChestData> data;
        int baseSize;
        Map<String, Object> upgrades;
    }

    private static class EnderChestData {
        Map<Integer, ItemStack> items;
        int size;
    }

    private static class CustomHome {
        String id;
        String owner;
        String name;
        Location location;
        Map<String, Object> settings;
    }

    private static class CustomWarp {
        String id;
        String name;
        Location location;
        String permission;
        double cost;
        Map<String, Object> settings;
    }

    private static class CustomTeleport {
        String id;
        Location from;
        Location to;
        Map<String, Object> conditions;
        Map<String, Object> effects;
    }

    private static class CustomPortal {
        String id;
        String name;
        Location location1;
        Location location2;
        String destination;
        Map<String, Object> settings;
    }

    private static class CustomGate {
        String id;
        String name;
        Location location;
        String network;
        Map<String, Object> settings;
    }

    private static class CustomBridge {
        String id;
        Location point1;
        Location point2;
        Material material;
        boolean active;
    }

    private static class CustomElevator {
        String id;
        List<Location> floors;
        Material platform;
        Map<String, Object> settings;
    }

    private static class CustomVehicle {
        String id;
        String type;
        double speed;
        double fuel;
        Map<String, Object> properties;
    }

    private static class CustomMount {
        String id;
        EntityType type;
        double speed;
        double jumpHeight;
        Map<String, Object> attributes;
    }

    private static class CustomPet {
        String id;
        String name;
        EntityType type;
        String owner;
        int level;
        Map<String, Object> stats;
        List<String> abilities;
    }

    private static class CustomCompanion {
        String id;
        String name;
        String type;
        Map<String, Object> abilities;
        Map<String, Object> stats;
    }

    private static class CustomMinion {
        String id;
        String type;
        String owner;
        Location location;
        int level;
        Map<String, Object> upgrades;
        Map<String, Object> production;
    }

    private static class CustomGolem {
        String id;
        String type;
        double health;
        double damage;
        Map<String, Object> abilities;
    }

    private static class CustomTurret {
        String id;
        Location location;
        String type;
        double damage;
        double range;
        double fireRate;
        String target;
    }

    private static class CustomTrap {
        String id;
        Location location;
        String type;
        double damage;
        double range;
        Map<String, Object> effects;
    }

    private static class CustomBarrier {
        String id;
        Location center;
        double radius;
        double health;
        Map<String, Object> properties;
    }

    private static class CustomShield {
        String id;
        String owner;
        double strength;
        double regeneration;
        Map<String, Object> properties;
    }

    private static class CustomForceField {
        String id;
        Location center;
        double radius;
        String type;
        Map<String, Object> effects;
    }

    private static class CustomGenerator {
        String id;
        Location location;
        String type;
        double rate;
        String output;
        Map<String, Object> upgrades;
    }

    private static class CustomSpawner {
        String id;
        Location location;
        String entity;
        int delay;
        int maxEntities;
        double range;
        Map<String, Object> properties;
    }

    private static class CustomFarm {
        String id;
        Location center;
        int radius;
        List<String> crops;
        Map<String, Object> bonuses;
    }

    private static class CustomMachine {
        String id;
        String type;
        Location location;
        Map<String, Integer> input;
        Map<String, Integer> output;
        double efficiency;
        double energy;
    }

    private static class CustomFactory {
        String id;
        Location location;
        Map<String, ProductionLine> lines;
        Map<String, Object> upgrades;
    }

    private static class ProductionLine {
        String input;
        String output;
        int rate;
        double efficiency;
    }

    private static class CustomReactor {
        String id;
        Location location;
        double power;
        double heat;
        double efficiency;
        Map<String, Object> components;
    }

    private static class CustomPower {
        String id;
        String type;
        double amount;
        double maxAmount;
        double regeneration;
    }

    private static class CustomEnergy {
        String id;
        double amount;
        double capacity;
        double input;
        double output;
    }

    private static class CustomMana {
        String id;
        double amount;
        double maxAmount;
        double regeneration;
        Map<String, Object> modifiers;
    }

    private static class CustomStamina {
        String id;
        double amount;
        double maxAmount;
        double regeneration;
        double consumption;
    }

    private static class CustomHealth {
        String id;
        double amount;
        double maxAmount;
        double regeneration;
        Map<String, Object> modifiers;
    }

    private static class CustomHunger {
        String id;
        int foodLevel;
        float saturation;
        float exhaustion;
        Map<String, Object> modifiers;
    }

    private static class CustomThirst {
        String id;
        double level;
        double maxLevel;
        double dehydrationRate;
        Map<String, Object> effects;
    }

    private static class CustomTemperature {
        String id;
        double value;
        double min;
        double max;
        Map<String, Object> effects;
    }

    private static class CustomRadiation {
        String id;
        double level;
        double resistance;
        Map<String, Object> effects;
    }

    private static class CustomInfection {
        String id;
        String type;
        double severity;
        double spreadChance;
        Map<String, Object> symptoms;
    }

    private static class CustomCurse {
        String id;
        String name;
        int level;
        Map<String, Object> effects;
        Map<String, Object> removal;
    }

    private static class CustomBless {
        String id;
        String name;
        int level;
        Map<String, Object> benefits;
        int duration;
    }

    private static class CustomBuff {
        String id;
        String name;
        Map<String, Double> modifiers;
        int duration;
        String icon;
    }

    private static class CustomDebuff {
        String id;
        String name;
        Map<String, Double> modifiers;
        int duration;
        String icon;
    }

    private static class CustomAura {
        String id;
        String owner;
        double radius;
        Map<String, Object> effects;
        String particle;
    }

    private static class CustomField {
        String id;
        Location center;
        double radius;
        String type;
        Map<String, Object> properties;
    }

    private static class CustomZone {
        String id;
        String name;
        Location corner1;
        Location corner2;
        Map<String, Object> rules;
        Map<String, Object> effects;
    }

    private static class CustomRegion {
        String id;
        String name;
        List<Location> points;
        Map<String, Object> flags;
        List<String> members;
    }

    private static class CustomTerritory {
        String id;
        String owner;
        List<CustomRegion> regions;
        Map<String, Object> settings;
    }

    private static class CustomKingdom {
        String id;
        String name;
        String king;
        List<String> members;
        Map<String, Object> territories;
        double treasury;
    }

    private static class CustomEmpire {
        String id;
        String name;
        String emperor;
        List<CustomKingdom> kingdoms;
        Map<String, Object> policies;
    }

    private static class CustomNation {
        String id;
        String name;
        String leader;
        List<String> citizens;
        Map<String, Object> laws;
        double economy;
    }

    private static class CustomFaction {
        String id;
        String name;
        String leader;
        List<String> members;
        Map<String, Object> ranks;
        Map<String, Object> relations;
        double power;
    }

    private static class CustomGuild {
        String id;
        String name;
        String master;
        List<String> members;
        int level;
        Map<String, Object> perks;
        Map<String, Object> bank;
    }

    private static class CustomClan {
        String id;
        String name;
        String chief;
        List<String> members;
        Map<String, Object> stats;
        String tag;
    }

    private static class CustomParty {
        String id;
        String leader;
        List<String> members;
        Map<String, Object> settings;
        Map<String, Object> loot;
    }

    private static class CustomTeam {
        String id;
        String name;
        List<String> players;
        String color;
        Map<String, Object> stats;
    }

    private static class CustomAlliance {
        String id;
        String name;
        List<String> members;
        Map<String, Object> treaties;
    }

    private static class CustomCoalition {
        String id;
        List<CustomAlliance> alliances;
        Map<String, Object> goals;
    }

    private static class CustomRank {
        String id;
        String name;
        int level;
        Map<String, Object> permissions;
        Map<String, Object> benefits;
    }

    private static class CustomTitle {
        String id;
        String name;
        String prefix;
        String suffix;
        Map<String, Object> requirements;
    }

    private static class CustomBadge {
        String id;
        String name;
        String icon;
        String description;
        Map<String, Object> criteria;
    }

    private static class CustomAchievement {
        String id;
        String name;
        String description;
        Map<String, Object> requirements;
        Map<String, Object> rewards;
    }

    private static class CustomReward {
        String id;
        String type;
        Map<String, Object> items;
        Map<String, Object> currencies;
        Map<String, Object> experience;
    }

    private static class CustomLoot {
        String id;
        List<LootEntry> items;
        Map<String, Object> conditions;
    }

    private static class CustomTreasure {
        String id;
        Location location;
        List<ItemStack> contents;
        Map<String, Object> guardians;
    }

    private static class CustomChest {
        String id;
        String type;
        List<LootEntry> loot;
        int respawnTime;
    }

    private static class CustomCrate {
        String id;
        String name;
        String key;
        List<CrateReward> rewards;
        Map<String, Object> animation;
    }

    private static class CrateReward {
        ItemStack item;
        double chance;
        List<String> commands;
        Map<String, Object> effects;
    }

    private static class CustomBox {
        String id;
        String name;
        int size;
        List<ItemStack> possibleItems;
        Map<String, Object> settings;
    }

    private static class CustomContainer {
        String id;
        String type;
        int capacity;
        Map<String, Integer> contents;
        Map<String, Object> properties;
    }

    private static class CustomPackage {
        String id;
        String name;
        List<ItemStack> items;
        Map<String, Object> metadata;
    }

    private static class CustomBundle {
        String id;
        String name;
        List<String> items;
        double discount;
        Map<String, Object> bonuses;
    }

    private static class CustomKit {
        String id;
        String name;
        List<ItemStack> items;
        int cooldown;
        String permission;
        Map<String, Object> commands;
    }

    private static class CustomLoadout {
        String id;
        String name;
        Map<String, ItemStack> equipment;
        List<ItemStack> inventory;
        Map<String, Object> stats;
    }

    private static class CustomPreset {
        String id;
        String name;
        Map<String, Object> settings;
        Map<String, Object> configurations;
    }

    private static class CustomTemplate {
        String id;
        String name;
        String type;
        Map<String, Object> structure;
        Map<String, Object> variables;
    }

    private static class CustomSchematic {
        String id;
        String name;
        byte[] data;
        int width;
        int height;
        int length;
        Map<String, Object> metadata;
    }

    private static class CustomBlueprint {
        String id;
        String name;
        Map<String, Object> components;
        Map<String, Object> requirements;
        Map<String, Object> output;
    }

    private static class CustomDesign {
        String id;
        String name;
        String category;
        Map<String, Object> specifications;
        Map<String, Object> materials;
    }

    private static class CustomPattern {
        String id;
        String name;
        String[][] pattern;
        Map<Character, String> key;
    }

    private static class CustomFormula {
        String id;
        String name;
        String expression;
        Map<String, Double> variables;
        String result;
    }

    private static class CustomEquation {
        String id;
        String formula;
        Map<String, Object> variables;
        double solve(Map<String, Double> values) {
            return 0; // Implementation needed
        }
    }

    private static class CustomAlgorithm {
        String id;
        String name;
        String type;
        Map<String, Object> parameters;
        String implementation;
    }

    private static class CustomScript {
        String id;
        String name;
        String language;
        String code;
        Map<String, Object> variables;
    }

    private static class CustomMacro {
        String id;
        String name;
        List<String> commands;
        Map<String, Object> parameters;
    }

    private static class CustomFunction {
        String id;
        String name;
        List<String> parameters;
        String body;
        String returnType;
    }

    private static class CustomProcedure {
        String id;
        String name;
        List<String> steps;
        Map<String, Object> variables;
    }

    private static class CustomRoutine {
        String id;
        String name;
        int interval;
        List<String> actions;
        boolean active;
    }

    private static class CustomSequence {
        String id;
        String name;
        List<String> steps;
        int currentStep;
        Map<String, Object> state;
    }

    private static class CustomChain {
        String id;
        List<String> links;
        Map<String, Object> connections;
    }

    private static class CustomCombo {
        String id;
        String name;
        List<String> inputs;
        int timeWindow;
        Map<String, Object> result;
    }

    private static class CustomLink {
        String id;
        String from;
        String to;
        String type;
        Map<String, Object> data;
    }

    private static class CustomBond {
        String id;
        String entity1;
        String entity2;
        double strength;
        Map<String, Object> effects;
    }

    private static class CustomConnection {
        String id;
        String source;
        String target;
        String protocol;
        Map<String, Object> settings;
    }

    private static class CustomRelation {
        String id;
        String subject;
        String object;
        String type;
        double value;
    }

    private static class CustomAssociation {
        String id;
        List<String> members;
        String type;
        Map<String, Object> properties;
    }

    private static class CustomCorrelation {
        String id;
        String variable1;
        String variable2;
        double coefficient;
        String type;
    }

    private static class CustomDependency {
        String id;
        String dependent;
        String dependency;
        String version;
        boolean required;
    }

    private static class CustomRequirement {
        String id;
        String type;
        Object value;
        String comparator;
        String message;
    }

    private static class CustomCondition {
        String id;
        String expression;
        Map<String, Object> variables;
        boolean evaluate() {
            return true; // Implementation needed
        }
    }

    private static class CustomConstraint {
        String id;
        String type;
        Object min;
        Object max;
        String message;
    }

    private static class CustomRestriction {
        String id;
        String target;
        String type;
        Object value;
        Map<String, Object> exceptions;
    }

    private static class CustomLimitation {
        String id;
        String resource;
        double limit;
        String period;
        Map<String, Object> modifiers;
    }

    private static class CustomBoundary {
        String id;
        Location min;
        Location max;
        String type;
        Map<String, Object> properties;
    }

    private static class CustomThreshold {
        String id;
        String variable;
        double value;
        String action;
        boolean triggered;
    }

    private static class CustomLimit {
        String id;
        String type;
        double value;
        String scope;
        Map<String, Object> overrides;
    }

    private static class CustomCapacity {
        String id;
        double current;
        double maximum;
        String unit;
        Map<String, Object> modifiers;
    }

    private static class CustomMaximum {
        String id;
        String variable;
        double value;
        Map<String, Object> conditions;
    }

    private static class CustomMinimum {
        String id;
        String variable;
        double value;
        Map<String, Object> conditions;
    }

    private static class CustomRange {
        String id;
        double min;
        double max;
        String unit;
        boolean inclusive;
    }

    private static class CustomScale {
        String id;
        String name;
        List<Double> values;
        String unit;
        String type;
    }

    private static class CustomRatio {
        String id;
        double numerator;
        double denominator;
        String format;
    }

    private static class CustomProportion {
        String id;
        Map<String, Double> parts;
        double total;
    }

    private static class CustomPercentage {
        String id;
        double value;
        double base;
        String format;
    }

    private static class CustomFraction {
        String id;
        long numerator;
        long denominator;
        String display;
    }

    private static class CustomDecimal {
        String id;
        double value;
        int precision;
        String format;
    }

    private static class CustomInteger {
        String id;
        int value;
        String format;
    }

    private static class CustomFloat {
        String id;
        float value;
        String format;
    }

    private static class CustomDouble {
        String id;
        double value;
        String format;
    }

    private static class CustomLong {
        String id;
        long value;
        String format;
    }

    private static class CustomShort {
        String id;
        short value;
        String format;
    }

    private static class CustomByte {
        String id;
        byte value;
        String format;
    }

    private static class CustomBoolean {
        String id;
        boolean value;
        String trueText;
        String falseText;
    }

    private static class CustomString {
        String id;
        String value;
        int maxLength;
        String pattern;
    }

    private static class CustomCharacter {
        String id;
        char value;
        Set<Character> allowedChars;
    }

    private static class CustomArray {
        String id;
        Object[] elements;
        Class<?> type;
        int maxSize;
    }

    private static class CustomList {
        String id;
        List<Object> elements;
        Class<?> type;
        int maxSize;
    }

    private static class CustomMap {
        String id;
        Map<Object, Object> elements;
        Class<?> keyType;
        Class<?> valueType;
    }

    private static class CustomQueue {
        String id;
        Queue<Object> elements;
        int capacity;
        String type;
    }

    private static class CustomStack {
        String id;
        Stack<Object> elements;
        int maxSize;
        String type;
    }

    private static class CustomTree {
        String id;
        TreeNode root;
        String type;
        int maxDepth;
    }

    private static class TreeNode {
        Object value;
        List<TreeNode> children;
    }

    private static class CustomGraph {
        String id;
        Map<String, GraphNode> nodes;
        List<GraphEdge> edges;
        String type;
    }

    private static class GraphNode {
        String id;
        Object value;
        Map<String, Object> properties;
    }

    private static class GraphEdge {
        String from;
        String to;
        double weight;
        boolean directed;
    }

    private static class CustomMatrix {
        String id;
        double[][] values;
        int rows;
        int columns;
    }

    private static class CustomVector {
        String id;
        double[] values;
        int dimension;
    }

    private static class CustomTensor {
        String id;
        Object data;
        int[] shape;
        String dtype;
    }

    private static class CustomTable {
        String id;
        List<String> columns;
        List<Map<String, Object>> rows;
        Map<String, String> types;
    }

    private static class CustomDatabase {
        String id;
        Map<String, CustomTable> tables;
        String type;
        Map<String, Object> settings;
    }

    private static class CustomCache {
        String id;
        Map<String, CacheEntry> entries;
        long ttl;
        int maxSize;
    }

    private static class CacheEntry {
        Object value;
        long timestamp;
        long expiry;
    }

    private static class CustomBuffer {
        String id;
        byte[] data;
        int position;
        int limit;
        int capacity;
    }

    private static class CustomStream {
        String id;
        String type;
        Object source;
        Map<String, Object> properties;
    }

    private static class CustomChannel {
        String id;
        String name;
        List<String> subscribers;
        Queue<Object> messages;
    }

    private static class CustomPipeline {
        String id;
        List<PipelineStage> stages;
        Map<String, Object> context;
    }

    private static class PipelineStage {
        String name;
        String processor;
        Map<String, Object> config;
    }

    private static class CustomFlow {
        String id;
        Map<String, FlowNode> nodes;
        String startNode;
        Map<String, Object> variables;
    }

    private static class FlowNode {
        String id;
        String type;
        Map<String, Object> data;
        List<String> next;
    }

    private static class CustomProcess {
        String id;
        String name;
        String state;
        Map<String, Object> data;
        long startTime;
    }

    private static class CustomOperation {
        String id;
        String type;
        Map<String, Object> parameters;
        String status;
        Object result;
    }

    private static class CustomAction {
        String id;
        String name;
        String type;
        Map<String, Object> parameters;
        List<String> effects;
    }

    private static class CustomTask {
        String id;
        String name;
        String status;
        Runnable action;
        long scheduledTime;
    }

    private static class CustomJob {
        String id;
        String name;
        String cron;
        Runnable task;
        Map<String, Object> data;
    }

    private static class CustomWork {
        String id;
        String type;
        Map<String, Object> input;
        Map<String, Object> output;
        String status;
    }

    private static class CustomService {
        String id;
        String name;
        boolean running;
        Map<String, Object> config;
        Map<String, Object> stats;
    }

    private static class CustomSystem {
        String id;
        String name;
        Map<String, CustomComponent> components;
        Map<String, Object> configuration;
    }

    private static class CustomPlatform {
        String id;
        String name;
        String version;
        Map<String, CustomService> services;
        Map<String, Object> settings;
    }

    private static class CustomFramework {
        String id;
        String name;
        String version;
        Map<String, CustomModule> modules;
        Map<String, Object> config;
    }

    private static class CustomLibrary {
        String id;
        String name;
        String version;
        Map<String, CustomFunction> functions;
        Map<String, Object> constants;
    }

    private static class CustomModule {
        String id;
        String name;
        Map<String, Object> exports;
        List<String> dependencies;
    }

    private static class CustomComponent {
        String id;
        String type;
        Map<String, Object> properties;
        Map<String, Object> state;
    }

    private static class CustomElement {
        String id;
        String type;
        Map<String, Object> attributes;
        List<CustomElement> children;
    }

    private static class CustomUnit {
        String id;
        String name;
        String symbol;
        double conversionFactor;
        String baseUnit;
    }

    private static class CustomPart {
        String id;
        String name;
        String parent;
        Map<String, Object> properties;
    }

    private static class CustomPiece {
        String id;
        String type;
        Location position;
        Map<String, Object> data;
    }

    private static class CustomFragment {
        String id;
        String content;
        String type;
        Map<String, Object> metadata;
    }

    private static class CustomSegment {
        String id;
        int start;
        int end;
        String type;
        Map<String, Object> properties;
    }

    private static class CustomSection {
        String id;
        String title;
        String content;
        int order;
        Map<String, Object> settings;
    }

    private static class CustomChapter {
        String id;
        String title;
        List<CustomSection> sections;
        int number;
    }

    private static class CustomVolume {
        String id;
        String title;
        List<CustomChapter> chapters;
        int number;
    }

    private static class CustomEdition {
        String id;
        String name;
        int number;
        String date;
        List<String> changes;
    }

    private static class CustomVersion {
        String id;
        String number;
        String stage;
        long timestamp;
        List<String> features;
    }

    private static class CustomRelease {
        String id;
        String version;
        String date;
        List<String> notes;
        Map<String, String> downloads;
    }

    private static class CustomUpdate {
        String id;
        String fromVersion;
        String toVersion;
        List<String> changes;
        Map<String, Object> migrations;
    }

    private static class CustomPatch {
        String id;
        String version;
        List<String> fixes;
        Map<String, Object> files;
    }

    private static class CustomFix {
        String id;
        String issue;
        String solution;
        String version;
        Map<String, Object> details;
    }

    private static class CustomImprovement {
        String id;
        String area;
        String description;
        double impact;
        Map<String, Object> metrics;
    }

    private static class CustomEnhancement {
        String id;
        String feature;
        String description;
        Map<String, Object> additions;
    }

    private static class CustomOptimization {
        String id;
        String target;
        String method;
        double improvement;
        Map<String, Object> benchmarks;
    }

    private static class CustomRefactor {
        String id;
        String component;
        String reason;
        Map<String, Object> changes;
    }

    public void onEnable() {
        instance = this;

        // Klasörleri oluştur
        createDirectories();

        // Yapılandırmaları yükle
        loadConfigurations();

        // Ögeleri yükle
        loadAllItems();

        // Resource pack oluştur
        generateResourcePack();

        // HTTP sunucusunu başlat
        startHttpServer();

        // Eventleri kaydet
        getServer().getPluginManager().registerEvents(this, this);

        // Komutları kaydet
        registerCommands();

        // Görevleri başlat
        startTasks();

        // Metrikler
        if (metricsEnabled) {
            startMetrics();
        }

        getLogger().info(PREFIX + "Plugin başarıyla aktif edildi!");
        getLogger().info(PREFIX + "Toplam " + customItems.size() + " özel item yüklendi!");
        getLogger().info(PREFIX + "Resource pack URL: " + resourcePackUrl);
    }

    public void onDisable() {
        // HTTP sunucusunu durdur
        if (httpServer != null) {
            httpServer.stop(0);
        }

        // Executor'ları kapat
        executor.shutdown();
        scheduler.shutdown();

        // Verileri kaydet
        saveAllData();

        getLogger().info(PREFIX + "Plugin devre dışı bırakıldı!");
    }

    private void createDirectories() {
        // Ana klasörler
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Alt klasörler
        String[] dirs = {
                "configs", "textures", "models", "sounds", "lang", "data",
                "resource_pack", "resource_pack/assets/" + namespace + "/textures/item",
                "resource_pack/assets/" + namespace + "/models/item",
                "resource_pack/assets/" + namespace + "/items",
                "resource_pack/assets/" + namespace + "/sounds",
                "resource_pack/assets/" + namespace + "/lang",
                "resource_pack/assets/" + namespace + "/font",
                "resource_pack/assets/" + namespace + "/particles",
                "resource_pack/assets/" + namespace + "/shaders",
                "backups", "logs", "temp", "cache"
        };

        for (String dir : dirs) {
            File folder = new File(getDataFolder(), dir);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
    }

    private void loadConfigurations() {
        // Ana yapılandırma
        saveDefaultConfig();
        mainConfig = getConfig();

        // Ayarları yükle
        serverPort = mainConfig.getInt("server.port", 8080);
        debugMode = mainConfig.getBoolean("debug", false);
        autoUpdate = mainConfig.getBoolean("auto-update", true);
        metricsEnabled = mainConfig.getBoolean("metrics", true);
        databaseType = mainConfig.getString("database.type", "sqlite");

        // Diğer yapılandırmalar
        itemsConfig = loadConfig("items.yml");
        blocksConfig = loadConfig("blocks.yml");
        recipesConfig = loadConfig("recipes.yml");
        enchantsConfig = loadConfig("enchants.yml");
        effectsConfig = loadConfig("effects.yml");
        abilitiesConfig = loadConfig("abilities.yml");
        languageConfig = loadConfig("lang/en_US.yml");
        permissionsConfig = loadConfig("permissions.yml");

        // Varsayılan değerleri yaz
        writeDefaultConfigs();
    }

    private FileConfiguration loadConfig(String filename) {
        File file = new File(getDataFolder(), filename);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void writeDefaultConfigs() {
        // Items.yml örneği
        if (itemsConfig.getKeys(false).isEmpty()) {
            itemsConfig.set("example_sword.displayName", "&6Örnek Kılıç");
            itemsConfig.set("example_sword.material", "DIAMOND_SWORD");
            itemsConfig.set("example_sword.texture", "example_sword");
            itemsConfig.set("example_sword.customModelData", 1001);
            itemsConfig.set("example_sword.damage", 15.0);
            itemsConfig.set("example_sword.lore", Arrays.asList(
                    "&7Efsanevi bir kılıç",
                    "",
                    "&6Hasar: &f15",
                    "&6Kritik: &f%20"
            ));
            itemsConfig.set("example_sword.enchantments.sharpness", 5);
            itemsConfig.set("example_sword.abilities", Arrays.asList("lightning_strike"));
            itemsConfig.set("example_sword.rarity", "LEGENDARY");
            saveConfig(itemsConfig, "items.yml");
        }

        // Recipes.yml örneği
        if (recipesConfig.getKeys(false).isEmpty()) {
            recipesConfig.set("example_sword.type", "SHAPED");
            recipesConfig.set("example_sword.result", "example_sword");
            recipesConfig.set("example_sword.amount", 1);
            recipesConfig.set("example_sword.pattern", Arrays.asList(
                    " D ",
                    " D ",
                    " S "
            ));
            recipesConfig.set("example_sword.ingredients.D", "DIAMOND");
            recipesConfig.set("example_sword.ingredients.S", "STICK");
            saveConfig(recipesConfig, "recipes.yml");
        }

        // Abilities.yml örneği
        if (abilitiesConfig.getKeys(false).isEmpty()) {
            abilitiesConfig.set("lightning_strike.displayName", "&eŞimşek Çarpması");
            abilitiesConfig.set("lightning_strike.description", "Baktığın yere şimşek düşürür");
            abilitiesConfig.set("lightning_strike.cooldown", 10);
            abilitiesConfig.set("lightning_strike.manaCost", 50);
            abilitiesConfig.set("lightning_strike.range", 30);
            abilitiesConfig.set("lightning_strike.damage", 20);
            abilitiesConfig.set("lightning_strike.activation", "RIGHT_CLICK");
            abilitiesConfig.set("lightning_strike.sound", "ENTITY_LIGHTNING_BOLT_THUNDER");
            abilitiesConfig.set("lightning_strike.particle", "ELECTRIC_SPARK");
            saveConfig(abilitiesConfig, "abilities.yml");
        }
    }

    private void saveConfig(FileConfiguration config, String filename) {
        try {
            config.save(new File(getDataFolder(), filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAllItems() {
        // Items
        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                loadCustomItem(key, itemsSection.getConfigurationSection(key));
            }
        }

        // Blocks
        ConfigurationSection blocksSection = blocksConfig.getConfigurationSection("");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                loadCustomBlock(key, blocksSection.getConfigurationSection(key));
            }
        }

        // Recipes
        ConfigurationSection recipesSection = recipesConfig.getConfigurationSection("");
        if (recipesSection != null) {
            for (String key : recipesSection.getKeys(false)) {
                loadCustomRecipe(key, recipesSection.getConfigurationSection(key));
            }
        }

        // Enchants
        ConfigurationSection enchantsSection = enchantsConfig.getConfigurationSection("");
        if (enchantsSection != null) {
            for (String key : enchantsSection.getKeys(false)) {
                loadCustomEnchant(key, enchantsSection.getConfigurationSection(key));
            }
        }

        // Effects
        ConfigurationSection effectsSection = effectsConfig.getConfigurationSection("");
        if (effectsSection != null) {
            for (String key : effectsSection.getKeys(false)) {
                loadCustomEffect(key, effectsSection.getConfigurationSection(key));
            }
        }

        // Abilities
        ConfigurationSection abilitiesSection = abilitiesConfig.getConfigurationSection("");
        if (abilitiesSection != null) {
            for (String key : abilitiesSection.getKeys(false)) {
                loadCustomAbility(key, abilitiesSection.getConfigurationSection(key));
            }
        }

        // Register recipes
        registerAllRecipes();
    }

    private void loadCustomItem(String id, ConfigurationSection section) {
        if (section == null) return;

        CustomItem item = new CustomItem();
        item.id = id;
        item.displayName = section.getString("displayName", id);
        item.lore = section.getStringList("lore");

        String materialName = section.getString("material", "STONE");
        try {
            item.baseMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            item.baseMaterial = Material.STONE;
            getLogger().warning("Geçersiz materyal: " + materialName + " için " + id);
        }

        item.texture = section.getString("texture", id);
        item.model = section.getString("model", id);
        item.customModelData = section.getInt("customModelData", 0);

        // Attributes
        ConfigurationSection attrSection = section.getConfigurationSection("attributes");
        if (attrSection != null) {
            item.attributes = new HashMap<>();
            for (String key : attrSection.getKeys(false)) {
                item.attributes.put(key, attrSection.get(key));
            }
        }

        // Enchantments
        ConfigurationSection enchSection = section.getConfigurationSection("enchantments");
        if (enchSection != null) {
            item.enchantments = new HashMap<>();
            for (String key : enchSection.getKeys(false)) {
                item.enchantments.put(key, enchSection.getInt(key));
            }
        }

        item.flags = section.getStringList("flags");
        item.abilities = section.getStringList("abilities");
        item.effects = section.getStringList("effects");
        item.category = section.getString("category", "misc");
        item.rarity = section.getString("rarity", "COMMON");
        item.tier = section.getInt("tier", 1);
        item.damage = section.getDouble("damage", 0);
        item.defense = section.getDouble("defense", 0);
        item.speed = section.getDouble("speed", 0);
        item.health = section.getDouble("health", 0);
        item.mana = section.getDouble("mana", 0);
        item.unbreakable = section.getBoolean("unbreakable", false);
        item.glowing = section.getBoolean("glowing", false);
        item.stackable = section.getBoolean("stackable", true);
        item.maxStack = section.getInt("maxStack", 64);
        item.weight = section.getDouble("weight", 1.0);
        item.value = section.getDouble("value", 0);
        item.currency = section.getString("currency", "default");
        item.tradeable = section.getBoolean("tradeable", true);
        item.droppable = section.getBoolean("droppable", true);
        item.destroyable = section.getBoolean("destroyable", true);
        item.repairable = section.getBoolean("repairable", true);
        item.durability = section.getInt("durability", -1);
        item.maxDurability = section.getInt("maxDurability", -1);
        item.repairCost = section.getDouble("repairCost", 0);
        item.repairMaterials = section.getStringList("repairMaterials");
        item.soulbound = section.getBoolean("soulbound", false);
        item.questItem = section.getBoolean("questItem", false);
        item.unique = section.getBoolean("unique", false);
        item.legendary = section.getBoolean("legendary", false);
        item.mythic = section.getBoolean("mythic", false);
        item.artifact = section.getBoolean("artifact", false);
        item.relic = section.getBoolean("relic", false);
        item.ancient = section.getBoolean("ancient", false);
        item.divine = section.getBoolean("divine", false);
        item.cursed = section.getBoolean("cursed", false);
        item.blessed = section.getBoolean("blessed", false);
        item.set = section.getString("set");
        item.classes = section.getStringList("classes");
        item.races = section.getStringList("races");
        item.levelRequirement = section.getInt("levelRequirement", 0);
        item.twoHanded = section.getBoolean("twoHanded", false);
        item.slot = section.getString("slot");
        item.weaponType = section.getString("weaponType");
        item.armorType = section.getString("armorType");
        item.toolType = section.getString("toolType");
        item.accessoryType = section.getString("accessoryType");
        item.attackSpeed = section.getDouble("attackSpeed", 1.0);
        item.critChance = section.getDouble("critChance", 0);
        item.critDamage = section.getDouble("critDamage", 1.5);
        item.blockChance = section.getDouble("blockChance", 0);
        item.dodgeChance = section.getDouble("dodgeChance", 0);
        item.parryChance = section.getDouble("parryChance", 0);
        item.lifesteal = section.getDouble("lifesteal", 0);
        item.manasteal = section.getDouble("manasteal", 0);
        item.expBonus = section.getDouble("expBonus", 0);
        item.lootBonus = section.getDouble("lootBonus", 0);
        item.goldBonus = section.getDouble("goldBonus", 0);
        item.enabled = section.getBoolean("enabled", true);
        item.createdAt = System.currentTimeMillis();
        item.creator = "system";

        // Load all maps
        item.nbt = loadMap(section, "nbt");
        item.metadata = loadMap(section, "metadata");
        item.requirements = loadMap(section, "requirements");
        item.permissions = loadMap(section, "permissions");
        item.events = loadMap(section, "events");
        item.triggers = loadMap(section, "triggers");
        item.conditions = loadMap(section, "conditions");
        item.actions = loadMap(section, "actions");
        item.variables = loadMap(section, "variables");
        item.placeholders = loadMap(section, "placeholders");
        item.sounds = loadStringMap(section, "sounds");
        item.particles = loadStringMap(section, "particles");
        item.animations = loadMap(section, "animations");
        item.tooltips = loadMap(section, "tooltips");
        item.interactions = loadMap(section, "interactions");
        item.upgrades = loadMap(section, "upgrades");
        item.modifications = loadMap(section, "modifications");
        item.augments = loadMap(section, "augments");
        item.gems = loadMap(section, "gems");
        item.runes = loadMap(section, "runes");
        item.inscriptions = loadMap(section, "inscriptions");
        item.imbuements = loadMap(section, "imbuements");
        item.infusions = loadMap(section, "infusions");
        item.enchantmentSlots = loadMap(section, "enchantmentSlots");
        item.socketSlots = loadMap(section, "socketSlots");
        item.upgradeSlots = loadMap(section, "upgradeSlots");
        item.setBonuses = loadMap(section, "setBonuses");
        item.statRequirements = loadIntMap(section, "statRequirements");
        item.skillRequirements = loadIntMap(section, "skillRequirements");
        item.cooldowns = loadMap(section, "cooldowns");
        item.charges = loadMap(section, "charges");
        item.ammunition = loadMap(section, "ammunition");
        item.fuel = loadMap(section, "fuel");
        item.energy = loadMap(section, "energy");
        item.heat = loadMap(section, "heat");
        item.radiation = loadMap(section, "radiation");
        item.resistances = loadDoubleMap(section, "resistances");
        item.penetrations = loadDoubleMap(section, "penetrations");
        item.amplifications = loadDoubleMap(section, "amplifications");
        item.specialEffects = loadMap(section, "specialEffects");
        item.onHit = loadMap(section, "onHit");
        item.onKill = loadMap(section, "onKill");
        item.onDeath = loadMap(section, "onDeath");
        item.onEquip = loadMap(section, "onEquip");
        item.onUnequip = loadMap(section, "onUnequip");
        item.onUse = loadMap(section, "onUse");
        item.onBreak = loadMap(section, "onBreak");
        item.onRepair = loadMap(section, "onRepair");
        item.onUpgrade = loadMap(section, "onUpgrade");
        item.onEnchant = loadMap(section, "onEnchant");
        item.onSocket = loadMap(section, "onSocket");
        item.onCraft = loadMap(section, "onCraft");
        item.onSmelt = loadMap(section, "onSmelt");
        item.onBrew = loadMap(section, "onBrew");
        item.statistics = loadMap(section, "statistics");
        item.analytics = loadMap(section, "analytics");

        customItems.put(id, item);

        if (debugMode) {
            getLogger().info("Özel item yüklendi: " + id);
        }
    }

    private Map<String, Object> loadMap(ConfigurationSection section, String path) {
        ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (mapSection != null) {
            Map<String, Object> map = new HashMap<>();
            for (String key : mapSection.getKeys(false)) {
                map.put(key, mapSection.get(key));
            }
            return map;
        }
        return null;
    }

    private Map<String, String> loadStringMap(ConfigurationSection section, String path) {
        ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (mapSection != null) {
            Map<String, String> map = new HashMap<>();
            for (String key : mapSection.getKeys(false)) {
                map.put(key, mapSection.getString(key));
            }
            return map;
        }
        return null;
    }

    private Map<String, Integer> loadIntMap(ConfigurationSection section, String path) {
        ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (mapSection != null) {
            Map<String, Integer> map = new HashMap<>();
            for (String key : mapSection.getKeys(false)) {
                map.put(key, mapSection.getInt(key));
            }
            return map;
        }
        return null;
    }

    private Map<String, Double> loadDoubleMap(ConfigurationSection section, String path) {
        ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (mapSection != null) {
            Map<String, Double> map = new HashMap<>();
            for (String key : mapSection.getKeys(false)) {
                map.put(key, mapSection.getDouble(key));
            }
            return map;
        }
        return null;
    }

    private void loadCustomBlock(String id, ConfigurationSection section) {
        if (section == null) return;

        CustomBlock block = new CustomBlock();
        block.id = id;
        block.displayName = section.getString("displayName", id);

        String materialName = section.getString("material", "STONE");
        try {
            block.baseMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            block.baseMaterial = Material.STONE;
        }

        block.texture = section.getString("texture", id);
        block.model = section.getString("model", id);
        block.hardness = section.getDouble("hardness", 1.0);
        block.explosionResistance = section.getDouble("explosionResistance", 1.0);
        block.tool = section.getString("tool");
        block.toolLevel = section.getInt("toolLevel", 0);
        block.drops = section.getStringList("drops");
        block.solid = section.getBoolean("solid", true);
        block.transparent = section.getBoolean("transparent", false);
        block.gravity = section.getBoolean("gravity", false);
        block.lightLevel = section.getInt("lightLevel", 0);
        block.properties = loadMap(section, "properties");
        block.events = loadMap(section, "events");

        customBlocks.put(id, block);
    }

    private void loadCustomRecipe(String id, ConfigurationSection section) {
        if (section == null) return;

        CustomRecipe recipe = new CustomRecipe();
        recipe.id = id;
        recipe.type = section.getString("type", "SHAPED");
        recipe.result = section.getString("result");
        recipe.amount = section.getInt("amount", 1);

        // Pattern for shaped recipes
        List<String> patternList = section.getStringList("pattern");
        if (!patternList.isEmpty()) {
            recipe.pattern = patternList.toArray(new String[0]);
        }

        // Ingredients
        ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
        if (ingredientsSection != null) {
            recipe.ingredients = new HashMap<>();
            for (String key : ingredientsSection.getKeys(false)) {
                recipe.ingredients.put(key, ingredientsSection.getString(key));
            }
        }

        recipe.cookingTime = section.getInt("cookingTime", 200);
        recipe.experience = (float) section.getDouble("experience", 0.0);
        recipe.group = section.getString("group");
        recipe.conditions = loadMap(section, "conditions");
        recipe.rewards = loadMap(section, "rewards");

        customRecipes.put(id, recipe);
    }

    private void loadCustomEnchant(String id, ConfigurationSection section) {
        if (section == null) return;

        CustomEnchant enchant = new CustomEnchant();
        enchant.id = id;
        enchant.displayName = section.getString("displayName", id);
        enchant.maxLevel = section.getInt("maxLevel", 1);
        enchant.applicableItems = section.getStringList("applicableItems");
        enchant.conflicts = section.getStringList("conflicts");
        enchant.treasure = section.getBoolean("treasure", false);
        enchant.cursed = section.getBoolean("cursed", false);
        enchant.rarity = section.getString("rarity", "COMMON");
        enchant.effects = loadMap(section, "effects");

        // Level effects
        ConfigurationSection levelsSection = section.getConfigurationSection("levels");
        if (levelsSection != null) {
            enchant.levelEffects = new HashMap<>();
            for (String level : levelsSection.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(level);
                    enchant.levelEffects.put(lvl, loadMap(levelsSection, level));
                } catch (NumberFormatException ignored) {}
            }
        }

        customEnchants.put(id, enchant);
    }

    private void loadCustomEffect(String id, ConfigurationSection section) {
        if (section == null) return;

        CustomEffect effect = new CustomEffect();
        effect.id = id;
        effect.displayName = section.getString("displayName", id);

        String typeName = section.getString("type");
        if (typeName != null) {
            effect.type = PotionEffectType.getByName(typeName);
            if (effect.type == null) {
                Bukkit.getLogger().warning("[CustomEffects] Bilinmeyen efekt türü: " + typeName + " (id: " + id + ")");
            }
        }

        effect.duration  = section.getInt("duration", 100);
        effect.amplifier = section.getInt("amplifier", 0);
        effect.ambient   = section.getBoolean("ambient", false);
        effect.particles = section.getBoolean("particles", true);
        effect.icon      = section.getBoolean("icon", true);

        // Renk isimleriyle tanım
        String colorName = section.getString("color");
        if (colorName != null) {
            switch (colorName.trim().toUpperCase()) {
                case "RED":      effect.color = Color.RED; break;
                case "GREEN":    effect.color = Color.GREEN; break;
                case "BLUE":     effect.color = Color.BLUE; break;
                case "WHITE":    effect.color = Color.WHITE; break;
                case "BLACK":    effect.color = Color.BLACK; break;
                case "GRAY":     effect.color = Color.GRAY; break;
                case "PINK":     effect.color = Color.PINK; break;
                case "ORANGE":   effect.color = Color.ORANGE; break;
                case "YELLOW":   effect.color = Color.YELLOW; break;
                default:
                    Bukkit.getLogger().warning("[CustomEffects] Bilinmeyen renk adı: " + colorName + " (id: " + id + ")");
                    break;
            }
        }

        effect.modifiers = loadMap(section, "modifiers");
        effect.triggers = section.getStringList("triggers");

        customEffects.put(id, effect);
    }
    
    private void loadCustomAbility(String id, ConfigurationSection section) {
        if (section == null) return;

        CustomAbility ability = new CustomAbility();
        ability.id = id;
        ability.displayName = section.getString("displayName", id);
        ability.description = section.getString("description", "");
        ability.cooldown = section.getInt("cooldown", 0);
        ability.manaCost = section.getInt("manaCost", 0);
        ability.staminaCost = section.getInt("staminaCost", 0);
        ability.healthCost = section.getInt("healthCost", 0);
        ability.charges = section.getInt("charges", -1);
        ability.range = section.getDouble("range", 0);
        ability.damage = section.getDouble("damage", 0);
        ability.radius = section.getDouble("radius", 0);
        ability.activation = section.getString("activation", "RIGHT_CLICK");
        ability.effects = section.getStringList("effects");
        ability.conditions = loadMap(section, "conditions");
        ability.actions = loadMap(section, "actions");
        ability.animation = section.getString("animation");
        ability.sound = section.getString("sound");
        ability.particle = section.getString("particle");

        customAbilities.put(id, ability);
    }

    private void registerAllRecipes() {
        for (CustomRecipe recipe : customRecipes.values()) {
            registerRecipe(recipe);
        }
    }

    private void registerRecipe(CustomRecipe recipe) {
        if (recipe.result == null || !customItems.containsKey(recipe.result)) {
            return;
        }

        CustomItem resultItem = customItems.get(recipe.result);
        ItemStack result = resultItem.createItemStack();
        result.setAmount(recipe.amount);

        NamespacedKey key = new NamespacedKey(this, recipe.id);

        switch (recipe.type.toUpperCase()) {
            case "SHAPED":
                if (recipe.pattern != null && recipe.ingredients != null) {
                    ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
                    shapedRecipe.shape(recipe.pattern);

                    for (Map.Entry<String, String> entry : recipe.ingredients.entrySet()) {
                        char symbol = entry.getKey().charAt(0);
                        Material material = Material.getMaterial(entry.getValue());
                        if (material != null) {
                            shapedRecipe.setIngredient(symbol, material);
                        }
                    }

                    Bukkit.addRecipe(shapedRecipe);
                }
                break;

            case "SHAPELESS":
                if (recipe.ingredients != null) {
                    ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);

                    for (String ingredient : recipe.ingredients.values()) {
                        Material material = Material.getMaterial(ingredient);
                        if (material != null) {
                            shapelessRecipe.addIngredient(material);
                        }
                    }

                    Bukkit.addRecipe(shapelessRecipe);
                }
                break;

            case "FURNACE":
                if (recipe.ingredients != null && recipe.ingredients.containsKey("input")) {
                    Material input = Material.getMaterial(recipe.ingredients.get("input"));
                    if (input != null) {
                        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(key, result, input, recipe.experience, recipe.cookingTime);
                        Bukkit.addRecipe(furnaceRecipe);
                    }
                }
                break;

            case "BLASTING":
                if (recipe.ingredients != null && recipe.ingredients.containsKey("input")) {
                    Material input = Material.getMaterial(recipe.ingredients.get("input"));
                    if (input != null) {
                        BlastingRecipe blastingRecipe = new BlastingRecipe(key, result, input, recipe.experience, recipe.cookingTime);
                        Bukkit.addRecipe(blastingRecipe);
                    }
                }
                break;

            case "SMOKING":
                if (recipe.ingredients != null && recipe.ingredients.containsKey("input")) {
                    Material input = Material.getMaterial(recipe.ingredients.get("input"));
                    if (input != null) {
                        SmokingRecipe smokingRecipe = new SmokingRecipe(key, result, input, recipe.experience, recipe.cookingTime);
                        Bukkit.addRecipe(smokingRecipe);
                    }
                }
                break;

            case "CAMPFIRE":
                if (recipe.ingredients != null && recipe.ingredients.containsKey("input")) {
                    Material input = Material.getMaterial(recipe.ingredients.get("input"));
                    if (input != null) {
                        CampfireRecipe campfireRecipe = new CampfireRecipe(key, result, input, recipe.experience, recipe.cookingTime);
                        Bukkit.addRecipe(campfireRecipe);
                    }
                }
                break;

            case "STONECUTTING":
                if (recipe.ingredients != null && recipe.ingredients.containsKey("input")) {
                    Material input = Material.getMaterial(recipe.ingredients.get("input"));
                    if (input != null) {
                        StonecuttingRecipe stonecuttingRecipe = new StonecuttingRecipe(key, result, input);
                        Bukkit.addRecipe(stonecuttingRecipe);
                    }
                }
                break;

            case "SMITHING":
                if (recipe.ingredients != null && recipe.ingredients.containsKey("base") && recipe.ingredients.containsKey("addition")) {
                    Material base = Material.getMaterial(recipe.ingredients.get("base"));
                    Material addition = Material.getMaterial(recipe.ingredients.get("addition"));
                    if (base != null && addition != null) {
                        RecipeChoice baseChoice = new RecipeChoice.MaterialChoice(base);
                        RecipeChoice additionChoice = new RecipeChoice.MaterialChoice(addition);
                        SmithingRecipe smithingRecipe = new SmithingRecipe(key, result, baseChoice, additionChoice);
                        Bukkit.addRecipe(smithingRecipe);
                    }
                }
                break;
        }
    }

    private void generateResourcePack() {
        executor.submit(() -> {
            try {
                // Pack.mcmeta oluştur
                createPackMeta();

                // Model dosyaları oluştur
                createItemModels();

                // Texture'ları kopyala
                copyTextures();

                // Ses dosyalarını kopyala
                copySounds();

                // Dil dosyalarını oluştur
                createLanguageFiles();

                // Font dosyalarını oluştur
                createFontFiles();

                // Particle dosyalarını oluştur
                createParticleFiles();

                // Shader dosyalarını oluştur
                createShaderFiles();

                // ZIP dosyası oluştur
                createZipFile();

                // Hash oluştur
                generateHash();

                getLogger().info(PREFIX + "Resource pack başarıyla oluşturuldu!");

            } catch (Exception e) {
                getLogger().severe(PREFIX + "Resource pack oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void createPackMeta() throws IOException {
        JsonObject packMeta = new JsonObject();
        JsonObject pack = new JsonObject();

        pack.addProperty("pack_format", 32); // 1.21.4 için
        pack.addProperty("description", ChatColor.translateAlternateColorCodes('&',
                mainConfig.getString("pack.description", "&6Ultimate Items Resource Pack")));

        packMeta.add("pack", pack);

        // Filtre ekle
        JsonObject filter = new JsonObject();
        JsonArray block = new JsonArray();

        JsonObject blockEntry = new JsonObject();
        blockEntry.addProperty("namespace", "minecraft");
        blockEntry.addProperty("path", "atlases/blocks.json");
        block.add(blockEntry);

        filter.add("block", block);
        packMeta.add("filter", filter);

        // Pack.mcmeta dosyasını yaz
        File packMetaFile = new File(resourcePackPath.toFile(), "pack.mcmeta");
        try (FileWriter writer = new FileWriter(packMetaFile)) {
            gson.toJson(packMeta, writer);
        }

        // Pack icon kopyala
        File iconSource = new File(getDataFolder(), "pack.png");
        if (iconSource.exists()) {
            File iconDest = new File(resourcePackPath.toFile(), "pack.png");
            Files.copy(iconSource.toPath(), iconDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Varsayılan icon oluştur
            createDefaultPackIcon();
        }
    }

    private void createDefaultPackIcon() throws IOException {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Arka plan
        g2d.setColor(new java.awt.Color(139, 69, 19)); // Kahverengi
        g2d.fillRect(0, 0, 64, 64);

        // Logo
        g2d.setColor(new java.awt.Color(255, 215, 0)); // Altın
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("UI", 18, 40);

        g2d.dispose();

        File outputFile = new File(resourcePackPath.toFile(), "pack.png");
        ImageIO.write(image, "PNG", outputFile);
    }

    private void createItemModels() throws IOException {
        Path itemModelsPath = resourcePackPath.resolve("assets/" + namespace + "/models/item");
        Path itemsPath = resourcePackPath.resolve("assets/" + namespace + "/items");

        for (CustomItem item : customItems.values()) {
            if (!item.enabled) continue;

            // Item declaration dosyası oluştur
            createItemDeclaration(item, itemsPath);

            // Model dosyası oluştur
            createItemModel(item, itemModelsPath);

            // Custom model data varsa base item'i güncelle
            if (item.customModelData > 0) {
                updateBaseItemModel(item);
            }
        }
    }

    private void createItemDeclaration(CustomItem item, Path itemsPath) throws IOException {
        JsonObject declaration = new JsonObject();
        JsonObject model = new JsonObject();

        model.addProperty("type", "minecraft:model");
        model.addProperty("model", namespace + ":item/" + item.model);

        declaration.add("model", model);

        File declarationFile = new File(itemsPath.toFile(), item.id + ".json");
        try (FileWriter writer = new FileWriter(declarationFile)) {
            gson.toJson(declaration, writer);
        }
    }

    private void createItemModel(CustomItem item, Path modelsPath) throws IOException {
        JsonObject model = new JsonObject();

        // Parent
        String parent = "minecraft:item/generated";
        if (item.weaponType != null) {
            parent = "minecraft:item/handheld";
        }
        model.addProperty("parent", parent);

        // Textures
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", namespace + ":item/" + item.texture);

        // Ek texture katmanları
        for (int i = 1; i <= 4; i++) {
            String layerTexture = item.texture + "_layer" + i;
            File layerFile = new File(texturesPath.toFile(), layerTexture + ".png");
            if (layerFile.exists()) {
                textures.addProperty("layer" + i, namespace + ":item/" + layerTexture);
            }
        }

        model.add("textures", textures);

        // Display ayarları
        if (item.weaponType != null || item.toolType != null) {
            JsonObject display = createDisplaySettings(item);
            model.add("display", display);
        }

        // Overrides (durability, enchantments vb. için)
        JsonArray overrides = createOverrides(item);
        if (overrides.size() > 0) {
            model.add("overrides", overrides);
        }

        File modelFile = new File(modelsPath.toFile(), item.model + ".json");
        try (FileWriter writer = new FileWriter(modelFile)) {
            gson.toJson(model, writer);
        }
    }

    private JsonObject createDisplaySettings(CustomItem item) {
        JsonObject display = new JsonObject();

        // Thirdperson right
        JsonObject thirdpersonRight = new JsonObject();
        JsonArray rotationTR = new JsonArray();
        rotationTR.add(0); rotationTR.add(0); rotationTR.add(0);
        thirdpersonRight.add("rotation", rotationTR);

        JsonArray translationTR = new JsonArray();
        translationTR.add(0); translationTR.add(3); translationTR.add(1);
        thirdpersonRight.add("translation", translationTR);

        JsonArray scaleTR = new JsonArray();
        scaleTR.add(0.55); scaleTR.add(0.55); scaleTR.add(0.55);
        thirdpersonRight.add("scale", scaleTR);

        display.add("thirdperson_righthand", thirdpersonRight);

        // Firstperson right
        JsonObject firstpersonRight = new JsonObject();
        JsonArray rotationFR = new JsonArray();
        rotationFR.add(0); rotationFR.add(-90); rotationFR.add(25);
        firstpersonRight.add("rotation", rotationFR);

        JsonArray translationFR = new JsonArray();
        translationFR.add(1.13); translationFR.add(3.2); translationFR.add(1.13);
        firstpersonRight.add("translation", translationFR);

        JsonArray scaleFR = new JsonArray();
        scaleFR.add(0.68); scaleFR.add(0.68); scaleFR.add(0.68);
        firstpersonRight.add("scale", scaleFR);

        display.add("firstperson_righthand", firstpersonRight);

        // Ground
        JsonObject ground = new JsonObject();
        JsonArray translationG = new JsonArray();
        translationG.add(0); translationG.add(2); translationG.add(0);
        ground.add("translation", translationG);

        JsonArray scaleG = new JsonArray();
        scaleG.add(0.5); scaleG.add(0.5); scaleG.add(0.5);
        ground.add("scale", scaleG);

        display.add("ground", ground);

        // GUI
        JsonObject gui = new JsonObject();
        JsonArray rotationGUI = new JsonArray();
        rotationGUI.add(30); rotationGUI.add(225); rotationGUI.add(0);
        gui.add("rotation", rotationGUI);

        display.add("gui", gui);

        // Fixed
        JsonObject fixed = new JsonObject();
        JsonArray rotationF = new JsonArray();
        rotationF.add(0); rotationF.add(180); rotationF.add(0);
        fixed.add("rotation", rotationF);

        display.add("fixed", fixed);

        return display;
    }

    private JsonArray createOverrides(CustomItem item) {
        JsonArray overrides = new JsonArray();

        // Damaged states
        if (item.durability > 0 || item.maxDurability > 0) {
            for (int i = 1; i <= 10; i++) {
                JsonObject override = new JsonObject();
                JsonObject predicate = new JsonObject();

                predicate.addProperty("damage", i * 0.1);
                override.add("predicate", predicate);
                override.addProperty("model", namespace + ":item/" + item.model + "_damaged_" + i);

                overrides.add(override);
            }
        }

        // Pulling (bow)
        if ("BOW".equals(item.weaponType)) {
            for (int i = 0; i <= 2; i++) {
                JsonObject override = new JsonObject();
                JsonObject predicate = new JsonObject();

                predicate.addProperty("pulling", 1);
                predicate.addProperty("pull", i * 0.5);
                override.add("predicate", predicate);
                override.addProperty("model", namespace + ":item/" + item.model + "_pulling_" + i);

                overrides.add(override);
            }
        }

        // Blocking (shield)
        if ("SHIELD".equals(item.weaponType)) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();

            predicate.addProperty("blocking", 1);
            override.add("predicate", predicate);
            override.addProperty("model", namespace + ":item/" + item.model + "_blocking");

            overrides.add(override);
        }

        // Cast (fishing rod)
        if ("FISHING_ROD".equals(item.toolType)) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();

            predicate.addProperty("cast", 1);
            override.add("predicate", predicate);
            override.addProperty("model", namespace + ":item/" + item.model + "_cast");

            overrides.add(override);
        }

        // Charged (crossbow)
        if ("CROSSBOW".equals(item.weaponType)) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();

            predicate.addProperty("charged", 1);
            override.add("predicate", predicate);
            override.addProperty("model", namespace + ":item/" + item.model + "_charged");

            overrides.add(override);
        }

        return overrides;
    }

    private void updateBaseItemModel(CustomItem item) throws IOException {
        Path minecraftModelsPath = resourcePackPath.resolve("assets/minecraft/models/item");
        minecraftModelsPath.toFile().mkdirs();

        File baseModelFile = new File(minecraftModelsPath.toFile(), item.baseMaterial.name().toLowerCase() + ".json");

        JsonObject baseModel;
        if (baseModelFile.exists()) {
            // Mevcut dosyayı oku
            try (FileReader reader = new FileReader(baseModelFile)) {
                baseModel = gson.fromJson(reader, JsonObject.class);
            }
        } else {
            // Yeni dosya oluştur
            baseModel = new JsonObject();
            baseModel.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", "minecraft:item/" + item.baseMaterial.name().toLowerCase());
            baseModel.add("textures", textures);
        }

        // Custom model data case ekle
        JsonArray cases = baseModel.has("model") && baseModel.getAsJsonObject("model").has("cases")
                ? baseModel.getAsJsonObject("model").getAsJsonArray("cases")
                : new JsonArray();

        boolean caseExists = false;
        for (JsonElement element : cases) {
            JsonObject caseObj = element.getAsJsonObject();
            if (caseObj.has("when") && caseObj.get("when").getAsString().equals(item.id)) {
                caseExists = true;
                break;
            }
        }

        if (!caseExists) {
            JsonObject newCase = new JsonObject();
            newCase.addProperty("when", item.id);

            JsonObject model = new JsonObject();
            model.addProperty("type", "minecraft:model");
            model.addProperty("model", namespace + ":item/" + item.model);
            newCase.add("model", model);

            cases.add(newCase);
        }

        // Model yapısını güncelle
        if (!baseModel.has("model")) {
            JsonObject modelWrapper = new JsonObject();
            modelWrapper.addProperty("type", "minecraft:select");
            modelWrapper.addProperty("property", "minecraft:custom_model_data");

            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", "minecraft:model");
            fallback.addProperty("model", "minecraft:item/" + item.baseMaterial.name().toLowerCase());
            modelWrapper.add("fallback", fallback);

            modelWrapper.add("cases", cases);
            baseModel.add("model", modelWrapper);
        } else {
            JsonObject modelWrapper = baseModel.getAsJsonObject("model");
            modelWrapper.add("cases", cases);
        }

        // Dosyayı yaz
        try (FileWriter writer = new FileWriter(baseModelFile)) {
            gson.toJson(baseModel, writer);
        }
    }

    private void copyTextures() throws IOException {
        Path sourceTextures = texturesPath;
        Path destTextures = resourcePackPath.resolve("assets/" + namespace + "/textures/item");

        if (sourceTextures.toFile().exists()) {
            Files.walk(sourceTextures)
                    .filter(path -> path.toString().endsWith(".png"))
                    .forEach(source -> {
                        try {
                            Path destination = destTextures.resolve(sourceTextures.relativize(source));
                            Files.createDirectories(destination.getParent());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

                            // Animasyon meta dosyası varsa onu da kopyala
                            Path animMeta = Paths.get(source.toString() + ".mcmeta");
                            if (Files.exists(animMeta)) {
                                Path destAnimMeta = Paths.get(destination.toString() + ".mcmeta");
                                Files.copy(animMeta, destAnimMeta, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            getLogger().warning("Texture kopyalanamadı: " + source.getFileName());
                        }
                    });
        }

        // Varsayılan texture'ları oluştur
        createDefaultTextures(destTextures);
    }

    private void createDefaultTextures(Path texturesPath) throws IOException {
        for (CustomItem item : customItems.values()) {
            if (!item.enabled) continue;

            Path texturePath = texturesPath.resolve(item.texture + ".png");
            if (!Files.exists(texturePath)) {
                // Varsayılan texture oluştur
                BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();

                // Arka plan
                g2d.setColor(new java.awt.Color(100, 100, 100, 255));
                g2d.fillRect(0, 0, 16, 16);

                // Item tiplerine göre renk
                java.awt.Color color;
                switch (item.rarity.toUpperCase()) {
                    case "LEGENDARY":
                        color = new java.awt.Color(255, 170, 0); // Altın
                        break;
                    case "EPIC":
                        color = new java.awt.Color(163, 53, 238); // Mor
                        break;
                    case "RARE":
                        color = new java.awt.Color(85, 255, 255); // Aqua
                        break;
                    case "UNCOMMON":
                        color = new java.awt.Color(85, 255, 85); // Yeşil
                        break;
                    default:
                        color = new java.awt.Color(255, 255, 255); // Beyaz
                        break;
                }

                g2d.setColor(color);
                g2d.fillRect(2, 2, 12, 12);

                // İç detay
                g2d.setColor(new java.awt.Color(0, 0, 0, 100));
                g2d.drawRect(4, 4, 8, 8);

                g2d.dispose();

                ImageIO.write(image, "PNG", texturePath.toFile());
            }
        }
    }

    private void copySounds() throws IOException {
        Path soundsSource = Paths.get(getDataFolder().getAbsolutePath(), "sounds");
        Path soundsDest = resourcePackPath.resolve("assets/" + namespace + "/sounds");

        if (soundsSource.toFile().exists()) {
            Files.walk(soundsSource)
                    .filter(path -> path.toString().endsWith(".ogg"))
                    .forEach(source -> {
                        try {
                            Path destination = soundsDest.resolve(soundsSource.relativize(source));
                            Files.createDirectories(destination.getParent());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            getLogger().warning("Ses dosyası kopyalanamadı: " + source.getFileName());
                        }
                    });

            // sounds.json oluştur
            createSoundsJson();
        }
    }

    private void createSoundsJson() throws IOException {
        JsonObject soundsJson = new JsonObject();

        // Custom sounds
        for (CustomSound sound : customSounds.values()) {
            JsonObject soundObj = new JsonObject();
            soundObj.addProperty("category", sound.category != null ? sound.category : "master");

            JsonArray sounds = new JsonArray();
            JsonObject soundEntry = new JsonObject();
            soundEntry.addProperty("name", namespace + ":" + sound.sound);
            soundEntry.addProperty("volume", sound.volume);
            soundEntry.addProperty("pitch", sound.pitch);
            soundEntry.addProperty("stream", false);
            sounds.add(soundEntry);

            soundObj.add("sounds", sounds);
            soundsJson.add(namespace + "." + sound.id, soundObj);
        }

        File soundsJsonFile = new File(resourcePackPath.resolve("assets/" + namespace).toFile(), "sounds.json");
        try (FileWriter writer = new FileWriter(soundsJsonFile)) {
            gson.toJson(soundsJson, writer);
        }
    }

    private void createLanguageFiles() throws IOException {
        Path langPath = resourcePackPath.resolve("assets/" + namespace + "/lang");
        langPath.toFile().mkdirs();

        // Desteklenen diller
        String[] languages = {"en_us", "tr_tr", "de_de", "fr_fr", "es_es", "pt_br", "ru_ru", "zh_cn", "ja_jp"};

        for (String lang : languages) {
            JsonObject langFile = new JsonObject();

            // Items
            for (CustomItem item : customItems.values()) {
                if (!item.enabled) continue;

                String translationKey = "item." + namespace + "." + item.id;
                langFile.addProperty(translationKey, translateToLanguage(item.displayName, lang));

                // Description
                if (item.lore != null && !item.lore.isEmpty()) {
                    langFile.addProperty(translationKey + ".description", translateToLanguage(item.lore.get(0), lang));
                }

                // Tooltip
                if (item.tooltips != null) {
                    for (Map.Entry<String, Object> tooltip : item.tooltips.entrySet()) {
                        langFile.addProperty(translationKey + ".tooltip." + tooltip.getKey(),
                                translateToLanguage(tooltip.getValue().toString(), lang));
                    }
                }
            }

            // Blocks
            for (CustomBlock block : customBlocks.values()) {
                String translationKey = "block." + namespace + "." + block.id;
                langFile.addProperty(translationKey, translateToLanguage(block.displayName, lang));
            }

            // Enchantments
            for (CustomEnchant enchant : customEnchants.values()) {
                String translationKey = "enchantment." + namespace + "." + enchant.id;
                langFile.addProperty(translationKey, translateToLanguage(enchant.displayName, lang));
            }

            // Effects
            for (CustomEffect effect : customEffects.values()) {
                String translationKey = "effect." + namespace + "." + effect.id;
                langFile.addProperty(translationKey, translateToLanguage(effect.displayName, lang));
            }

            // Abilities
            for (CustomAbility ability : customAbilities.values()) {
                String translationKey = "ability." + namespace + "." + ability.id;
                langFile.addProperty(translationKey, translateToLanguage(ability.displayName, lang));
                langFile.addProperty(translationKey + ".description", translateToLanguage(ability.description, lang));
            }

            // GUI
            for (CustomGUI gui : customGUIs.values()) {
                String translationKey = "gui." + namespace + "." + gui.id;
                langFile.addProperty(translationKey + ".title", translateToLanguage(gui.title, lang));
            }

            // Messages
            langFile.addProperty("message." + namespace + ".reload", translateToLanguage("&aPlugin yeniden yüklendi!", lang));
            langFile.addProperty("message." + namespace + ".no_permission", translateToLanguage("&cBunu yapmaya yetkiniz yok!", lang));
            langFile.addProperty("message." + namespace + ".player_only", translateToLanguage("&cBu komut sadece oyuncular tarafından kullanılabilir!", lang));
            langFile.addProperty("message." + namespace + ".item_not_found", translateToLanguage("&cItem bulunamadı!", lang));
            langFile.addProperty("message." + namespace + ".item_given", translateToLanguage("&aItem verildi!", lang));

            File langJsonFile = new File(langPath.toFile(), lang + ".json");
            try (FileWriter writer = new FileWriter(langJsonFile)) {
                gson.toJson(langFile, writer);
            }
        }
    }

    private String translateToLanguage(String text, String lang) {
        // Basit çeviri sistemi - gerçek bir uygulamada API kullanılabilir
        if (text == null) return "";

        String cleanText = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));

        // Türkçe
        if (lang.equals("tr_tr")) {
            return text; // Zaten Türkçe
        }

        // İngilizce varsayılan
        return text.replace("Örnek", "Example")
                .replace("Kılıç", "Sword")
                .replace("Hasar", "Damage")
                .replace("Kritik", "Critical")
                .replace("Efsanevi", "Legendary")
                .replace("Şimşek Çarpması", "Lightning Strike")
                .replace("Baktığın yere şimşek düşürür", "Strikes lightning where you look");
    }

    private void createFontFiles() throws IOException {
        Path fontPath = resourcePackPath.resolve("assets/" + namespace + "/font");
        fontPath.toFile().mkdirs();

        // Default font override
        JsonObject fontJson = new JsonObject();
        JsonArray providers = new JsonArray();

        // Unicode font
        JsonObject unicodeProvider = new JsonObject();
        unicodeProvider.addProperty("type", "uniform");
        unicodeProvider.addProperty("file", "minecraft:font/uniform.png");
        JsonArray sizes = new JsonArray();
        sizes.add(9);
        sizes.add(9);
        sizes.add(9);
        sizes.add(9);
        unicodeProvider.add("sizes", sizes);
        providers.add(unicodeProvider);

        // Custom icons font
        for (CustomFont font : customFonts.values()) {
            JsonObject customProvider = new JsonObject();
            customProvider.addProperty("type", "bitmap");
            customProvider.addProperty("file", namespace + ":font/" + font.id + ".png");
            customProvider.addProperty("ascent", 8);
            customProvider.addProperty("height", 8);

            JsonArray chars = new JsonArray();
            for (Character c : font.characters.keySet()) {
                chars.add(String.valueOf(c));
            }
            customProvider.add("chars", chars);

            providers.add(customProvider);
        }

        fontJson.add("providers", providers);

        File defaultFontFile = new File(fontPath.toFile(), "default.json");
        try (FileWriter writer = new FileWriter(defaultFontFile)) {
            gson.toJson(fontJson, writer);
        }
    }

    private void createParticleFiles() throws IOException {
        Path particlePath = resourcePackPath.resolve("assets/" + namespace + "/particles");
        particlePath.toFile().mkdirs();

        for (CustomParticle particle : customParticles.values()) {
            JsonObject particleJson = new JsonObject();

            JsonArray textures = new JsonArray();
            textures.add(namespace + ":particle/" + particle.id);
            particleJson.add("textures", textures);

            File particleFile = new File(particlePath.toFile(), particle.id + ".json");
            try (FileWriter writer = new FileWriter(particleFile)) {
                gson.toJson(particleJson, writer);
            }
        }
    }

    private void createShaderFiles() throws IOException {
        Path shaderPath = resourcePackPath.resolve("assets/" + namespace + "/shaders");
        shaderPath.toFile().mkdirs();

        // Core shaders
        Path corePath = shaderPath.resolve("core");
        corePath.toFile().mkdirs();

        // Post shaders
        Path postPath = shaderPath.resolve("post");
        postPath.toFile().mkdirs();

        // Program shaders
        Path programPath = shaderPath.resolve("program");
        programPath.toFile().mkdirs();

        for (CustomShader shader : customShaders.values()) {
            // Vertex shader
            if (shader.vertex != null) {
                File vertexFile = new File(programPath.toFile(), shader.id + ".vsh");
                try (FileWriter writer = new FileWriter(vertexFile)) {
                    writer.write(shader.vertex);
                }
            }

            // Fragment shader
            if (shader.fragment != null) {
                File fragmentFile = new File(programPath.toFile(), shader.id + ".fsh");
                try (FileWriter writer = new FileWriter(fragmentFile)) {
                    writer.write(shader.fragment);
                }
            }

            // Shader JSON
            JsonObject shaderJson = new JsonObject();
            shaderJson.addProperty("vertex", namespace + ":program/" + shader.id);
            shaderJson.addProperty("fragment", namespace + ":program/" + shader.id);

            if (shader.uniforms != null) {
                JsonArray uniforms = new JsonArray();
                for (Map.Entry<String, Object> uniform : shader.uniforms.entrySet()) {
                    JsonObject uniformObj = new JsonObject();
                    uniformObj.addProperty("name", uniform.getKey());
                    uniformObj.addProperty("type", uniform.getValue().toString());
                    uniforms.add(uniformObj);
                }
                shaderJson.add("uniforms", uniforms);
            }

            File shaderFile = new File(programPath.toFile(), shader.id + ".json");
            try (FileWriter writer = new FileWriter(shaderFile)) {
                gson.toJson(shaderJson, writer);
            }
        }
    }

    private void createZipFile() throws IOException {
        // Önceki ZIP'i sil
        if (generatedPackPath.toFile().exists()) {
            generatedPackPath.toFile().delete();
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(generatedPackPath.toFile()))) {
            Files.walk(resourcePackPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            ZipEntry entry = new ZipEntry(resourcePackPath.relativize(path).toString().replace("\\", "/"));
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            getLogger().warning("Dosya ZIP'e eklenemedi: " + path.getFileName());
                        }
                    });
        }
    }

    private void generateHash() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream fis = new FileInputStream(generatedPackPath.toFile())) {
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
        }

        resourcePackHash = digest.digest();

        if (debugMode) {
            StringBuilder hashString = new StringBuilder();
            for (byte b : resourcePackHash) {
                hashString.append(String.format("%02x", b));
            }
            getLogger().info("Resource pack SHA-1: " + hashString.toString());
        }
    }

    private void startHttpServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);

            httpServer.createContext("/resourcepack", new HttpHandler() {

                public void handle(HttpExchange exchange) throws IOException {
                    if (!generatedPackPath.toFile().exists()) {
                        exchange.sendResponseHeaders(404, -1);
                        exchange.close();
                        return;
                    }

                    File file = generatedPackPath.toFile();
                    exchange.getResponseHeaders().set("Content-Type", "application/zip");
                    exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"resourcepack.zip\"");
                    exchange.sendResponseHeaders(200, file.length());

                    try (OutputStream os = exchange.getResponseBody();
                         FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }

                    exchange.close();
                }
            });

            httpServer.setExecutor(null);
            httpServer.start();

            // URL'yi ayarla
            String ip = mainConfig.getString("server.ip", "localhost");
            if (ip.equals("auto")) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    ip = "localhost";
                }
            }

            resourcePackUrl = "http://" + ip + ":" + serverPort + "/resourcepack";

            getLogger().info(PREFIX + "HTTP sunucusu başlatıldı: " + resourcePackUrl);

        } catch (IOException e) {
            getLogger().severe(PREFIX + "HTTP sunucusu başlatılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        getCommand("ultimateitems").setExecutor(this);
        getCommand("ui").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);

            case "give":
                return handleGive(sender, args);

            case "list":
                return handleList(sender, args);

            case "info":
                return handleInfo(sender, args);

            case "create":
                return handleCreate(sender, args);

            case "edit":
                return handleEdit(sender, args);

            case "delete":
                return handleDelete(sender, args);

            case "gui":
                return handleGUI(sender, args);

            case "recipe":
                return handleRecipe(sender, args);

            case "enchant":
                return handleEnchant(sender, args);

            case "ability":
                return handleAbility(sender, args);

            case "effect":
                return handleEffect(sender, args);

            case "pack":
                return handlePack(sender, args);

            case "debug":
                return handleDebug(sender, args);

            case "stats":
                return handleStats(sender, args);

            case "backup":
                return handleBackup(sender, args);

            case "restore":
                return handleRestore(sender, args);

            case "export":
                return handleExport(sender, args);

            case "import":
                return handleImport(sender, args);

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "Ultimate Items Yardım" + ChatColor.DARK_PURPLE + " ==========");
        sender.sendMessage(ChatColor.GOLD + "/ui reload" + ChatColor.GRAY + " - Plugin'i yeniden yükle");
        sender.sendMessage(ChatColor.GOLD + "/ui give <oyuncu> <item> [miktar]" + ChatColor.GRAY + " - Item ver");
        sender.sendMessage(ChatColor.GOLD + "/ui list [kategori]" + ChatColor.GRAY + " - Itemleri listele");
        sender.sendMessage(ChatColor.GOLD + "/ui info <item>" + ChatColor.GRAY + " - Item bilgilerini göster");
        sender.sendMessage(ChatColor.GOLD + "/ui create <id> <material>" + ChatColor.GRAY + " - Yeni item oluştur");
        sender.sendMessage(ChatColor.GOLD + "/ui edit <item> <özellik> <değer>" + ChatColor.GRAY + " - Item düzenle");
        sender.sendMessage(ChatColor.GOLD + "/ui delete <item>" + ChatColor.GRAY + " - Item sil");
        sender.sendMessage(ChatColor.GOLD + "/ui gui [tip]" + ChatColor.GRAY + " - GUI aç");
        sender.sendMessage(ChatColor.GOLD + "/ui recipe <item>" + ChatColor.GRAY + " - Recipe bilgisi");
        sender.sendMessage(ChatColor.GOLD + "/ui enchant <enchant> <level>" + ChatColor.GRAY + " - Enchant ekle");
        sender.sendMessage(ChatColor.GOLD + "/ui ability <ability>" + ChatColor.GRAY + " - Yetenek kullan");
        sender.sendMessage(ChatColor.GOLD + "/ui effect <effect> [süre] [güç]" + ChatColor.GRAY + " - Efekt ekle");
        sender.sendMessage(ChatColor.GOLD + "/ui pack <regenerate|reload|send>" + ChatColor.GRAY + " - Resource pack işlemleri");
        sender.sendMessage(ChatColor.GOLD + "/ui debug <on|off>" + ChatColor.GRAY + " - Debug modunu aç/kapat");
        sender.sendMessage(ChatColor.GOLD + "/ui stats [oyuncu]" + ChatColor.GRAY + " - İstatistikleri göster");
        sender.sendMessage(ChatColor.GOLD + "/ui backup" + ChatColor.GRAY + " - Yedekleme oluştur");
        sender.sendMessage(ChatColor.GOLD + "/ui restore <tarih>" + ChatColor.GRAY + " - Yedekten geri yükle");
        sender.sendMessage(ChatColor.GOLD + "/ui export <item|all>" + ChatColor.GRAY + " - Verileri dışa aktar");
        sender.sendMessage(ChatColor.GOLD + "/ui import <dosya>" + ChatColor.GRAY + " - Verileri içe aktar");
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        long startTime = System.currentTimeMillis();

        // Yapılandırmaları yeniden yükle
        reloadConfig();
        loadConfigurations();

        // Ögeleri temizle ve yeniden yükle
        customItems.clear();
        customBlocks.clear();
        customRecipes.clear();
        customEnchants.clear();
        customEffects.clear();
        customAbilities.clear();

        loadAllItems();

        // Resource pack'i yeniden oluştur
        generateResourcePack();

        long endTime = System.currentTimeMillis();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Plugin başarıyla yeniden yüklendi! (" + (endTime - startTime) + "ms)");

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.give")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui give <oyuncu> <item> [miktar]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Oyuncu bulunamadı!");
            return true;
        }

        String itemId = args[2];
        if (!customItems.containsKey(itemId)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Item bulunamadı!");
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz miktar!");
                return true;
            }
        }

        CustomItem customItem = customItems.get(itemId);
        ItemStack item = customItem.createItemStack();
        item.setAmount(amount);

        // Envantere ekle
        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(item);

        // Yere düşür
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }

        sender.sendMessage(PREFIX + ChatColor.GREEN + target.getName() + " adlı oyuncuya " + amount + " adet " +
                ChatColor.translateAlternateColorCodes('&', customItem.displayName) + ChatColor.GREEN + " verildi!");

        if (!sender.equals(target)) {
            target.sendMessage(PREFIX + ChatColor.GREEN + "Size " + amount + " adet " +
                    ChatColor.translateAlternateColorCodes('&', customItem.displayName) + ChatColor.GREEN + " verildi!");
        }

        // Ses efekti
        target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.list")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        String category = args.length >= 2 ? args[1] : null;

        sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "Custom Items" + ChatColor.DARK_PURPLE + " ==========");

        int count = 0;
        for (CustomItem item : customItems.values()) {
            if (category != null && !item.category.equalsIgnoreCase(category)) {
                continue;
            }

            String rarityColor;
            switch (item.rarity.toUpperCase()) {
                case "LEGENDARY":
                    rarityColor = ChatColor.GOLD.toString();
                    break;
                case "EPIC":
                    rarityColor = ChatColor.DARK_PURPLE.toString();
                    break;
                case "RARE":
                    rarityColor = ChatColor.AQUA.toString();
                    break;
                case "UNCOMMON":
                    rarityColor = ChatColor.GREEN.toString();
                    break;
                default:
                    rarityColor = ChatColor.WHITE.toString();
                    break;
            }

            sender.sendMessage(ChatColor.GRAY + "- " + rarityColor + item.id + ChatColor.GRAY + " (" +
                    ChatColor.translateAlternateColorCodes('&', item.displayName) + ChatColor.GRAY + ") - " +
                    item.baseMaterial.name());
            count++;
        }

        sender.sendMessage(ChatColor.GRAY + "Toplam: " + count + " item");

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.info")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui info <item>");
            return true;
        }

        String itemId = args[1];
        if (!customItems.containsKey(itemId)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Item bulunamadı!");
            return true;
        }

        CustomItem item = customItems.get(itemId);

        sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "Item Bilgisi" + ChatColor.DARK_PURPLE + " ==========");
        sender.sendMessage(ChatColor.GOLD + "ID: " + ChatColor.WHITE + item.id);
        sender.sendMessage(ChatColor.GOLD + "İsim: " + ChatColor.translateAlternateColorCodes('&', item.displayName));
        sender.sendMessage(ChatColor.GOLD + "Materyal: " + ChatColor.WHITE + item.baseMaterial.name());
        sender.sendMessage(ChatColor.GOLD + "Nadirlik: " + ChatColor.WHITE + item.rarity);
        sender.sendMessage(ChatColor.GOLD + "Kategori: " + ChatColor.WHITE + item.category);
        sender.sendMessage(ChatColor.GOLD + "Tier: " + ChatColor.WHITE + item.tier);

        if (item.damage > 0) {
            sender.sendMessage(ChatColor.GOLD + "Hasar: " + ChatColor.WHITE + item.damage);
        }
        if (item.defense > 0) {
            sender.sendMessage(ChatColor.GOLD + "Savunma: " + ChatColor.WHITE + item.defense);
        }
        if (item.critChance > 0) {
            sender.sendMessage(ChatColor.GOLD + "Kritik Şansı: " + ChatColor.WHITE + item.critChance + "%");
        }

        if (item.lore != null && !item.lore.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "Açıklama:");
            for (String line : item.lore) {
                sender.sendMessage(ChatColor.GRAY + "  " + ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        if (item.abilities != null && !item.abilities.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "Yetenekler:");
            for (String ability : item.abilities) {
                sender.sendMessage(ChatColor.GRAY + "  - " + ability);
            }
        }

        if (item.enchantments != null && !item.enchantments.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "Büyüler:");
            for (Map.Entry<String, Integer> entry : item.enchantments.entrySet()) {
                sender.sendMessage(ChatColor.GRAY + "  - " + entry.getKey() + " " + entry.getValue());
            }
        }

        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.create")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui create <id> <material>");
            return true;
        }

        String id = args[1];
        if (customItems.containsKey(id)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bu ID zaten kullanılıyor!");
            return true;
        }

        Material material;
        try {
            material = Material.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz materyal!");
            return true;
        }

        // Yeni item oluştur
        CustomItem newItem = new CustomItem();
        newItem.id = id;
        newItem.displayName = "&f" + id;
        newItem.baseMaterial = material;
        newItem.texture = id;
        newItem.model = id;
        newItem.category = "misc";
        newItem.rarity = "COMMON";
        newItem.tier = 1;
        newItem.enabled = true;
        newItem.createdAt = System.currentTimeMillis();
        newItem.creator = sender.getName();

        customItems.put(id, newItem);

        // Yapılandırmaya kaydet
        itemsConfig.set(id + ".displayName", newItem.displayName);
        itemsConfig.set(id + ".material", material.name());
        itemsConfig.set(id + ".texture", newItem.texture);
        itemsConfig.set(id + ".model", newItem.model);
        itemsConfig.set(id + ".category", newItem.category);
        itemsConfig.set(id + ".rarity", newItem.rarity);
        itemsConfig.set(id + ".tier", newItem.tier);
        itemsConfig.set(id + ".enabled", true);
        saveConfig(itemsConfig, "items.yml");

        sender.sendMessage(PREFIX + ChatColor.GREEN + "Yeni item oluşturuldu: " + id);
        sender.sendMessage(ChatColor.GRAY + "Item'i düzenlemek için: /ui edit " + id + " <özellik> <değer>");

        // Resource pack'i güncelle
        scheduler.schedule(() -> generateResourcePack(), 1, TimeUnit.SECONDS);

        return true;
    }

    private boolean handleEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.edit")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui edit <item> <özellik> <değer>");
            return true;
        }

        String itemId = args[1];
        if (!customItems.containsKey(itemId)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Item bulunamadı!");
            return true;
        }

        CustomItem item = customItems.get(itemId);
        String property = args[2].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        switch (property) {
            case "name":
            case "displayname":
                item.displayName = value;
                itemsConfig.set(itemId + ".displayName", value);
                break;

            case "lore":
                if (item.lore == null) item.lore = new ArrayList<>();
                item.lore.add(value);
                itemsConfig.set(itemId + ".lore", item.lore);
                break;

            case "damage":
                try {
                    item.damage = Double.parseDouble(value);
                    itemsConfig.set(itemId + ".damage", item.damage);
                } catch (NumberFormatException e) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz sayı!");
                    return true;
                }
                break;

            case "defense":
                try {
                    item.defense = Double.parseDouble(value);
                    itemsConfig.set(itemId + ".defense", item.defense);
                } catch (NumberFormatException e) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz sayı!");
                    return true;
                }
                break;

            case "rarity":
                item.rarity = value.toUpperCase();
                itemsConfig.set(itemId + ".rarity", item.rarity);
                break;

            case "category":
                item.category = value.toLowerCase();
                itemsConfig.set(itemId + ".category", item.category);
                break;

            case "tier":
                try {
                    item.tier = Integer.parseInt(value);
                    itemsConfig.set(itemId + ".tier", item.tier);
                } catch (NumberFormatException e) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz sayı!");
                    return true;
                }
                break;

            case "material":
                try {
                    item.baseMaterial = Material.valueOf(value.toUpperCase());
                    itemsConfig.set(itemId + ".material", item.baseMaterial.name());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz materyal!");
                    return true;
                }
                break;

            case "modeldata":
            case "custommodeldata":
                try {
                    item.customModelData = Integer.parseInt(value);
                    itemsConfig.set(itemId + ".customModelData", item.customModelData);
                } catch (NumberFormatException e) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz sayı!");
                    return true;
                }
                break;

            case "unbreakable":
                item.unbreakable = Boolean.parseBoolean(value);
                itemsConfig.set(itemId + ".unbreakable", item.unbreakable);
                break;

            case "glowing":
                item.glowing = Boolean.parseBoolean(value);
                itemsConfig.set(itemId + ".glowing", item.glowing);
                break;

            default:
                sender.sendMessage(PREFIX + ChatColor.RED + "Bilinmeyen özellik: " + property);
                sender.sendMessage(ChatColor.GRAY + "Kullanılabilir özellikler: name, lore, damage, defense, rarity, category, tier, material, modeldata, unbreakable, glowing");
                return true;
        }

        saveConfig(itemsConfig, "items.yml");
        item.updatedAt = System.currentTimeMillis();
        item.lastModifier = sender.getName();

        sender.sendMessage(PREFIX + ChatColor.GREEN + "Item güncellendi!");
        sender.sendMessage(ChatColor.GRAY + property + " -> " + value);

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.delete")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui delete <item>");
            return true;
        }

        String itemId = args[1];
        if (!customItems.containsKey(itemId)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Item bulunamadı!");
            return true;
        }

        // Onay iste
        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            sender.sendMessage(PREFIX + ChatColor.YELLOW + "Item silinecek: " + itemId);
            sender.sendMessage(ChatColor.YELLOW + "Onaylamak için: /ui delete " + itemId + " confirm");
            return true;
        }

        customItems.remove(itemId);
        itemsConfig.set(itemId, null);
        saveConfig(itemsConfig, "items.yml");

        sender.sendMessage(PREFIX + ChatColor.GREEN + "Item silindi: " + itemId);

        return true;
    }

    private boolean handleGUI(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ultimateitems.gui")) {
            player.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        String guiType = args.length >= 2 ? args[1] : "main";

        // TODO: GUI sistemini implement et
        player.sendMessage(PREFIX + ChatColor.YELLOW + "GUI sistemi henüz tamamlanmadı!");

        return true;
    }

    private boolean handleRecipe(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.recipe")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui recipe <item>");
            return true;
        }

        String itemId = args[1];
        CustomRecipe recipe = customRecipes.get(itemId);

        if (recipe == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bu item için tarif bulunamadı!");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "Recipe: " + itemId + ChatColor.DARK_PURPLE + " ==========");
        sender.sendMessage(ChatColor.GOLD + "Tip: " + ChatColor.WHITE + recipe.type);
        sender.sendMessage(ChatColor.GOLD + "Sonuç: " + ChatColor.WHITE + recipe.result + " x" + recipe.amount);

        if (recipe.pattern != null) {
            sender.sendMessage(ChatColor.GOLD + "Şablon:");
            for (String line : recipe.pattern) {
                sender.sendMessage(ChatColor.WHITE + "  " + line);
            }
        }

        if (recipe.ingredients != null) {
            sender.sendMessage(ChatColor.GOLD + "Malzemeler:");
            for (Map.Entry<String, String> entry : recipe.ingredients.entrySet()) {
                sender.sendMessage(ChatColor.WHITE + "  " + entry.getKey() + " = " + entry.getValue());
            }
        }

        if (recipe.cookingTime > 0) {
            sender.sendMessage(ChatColor.GOLD + "Pişirme Süresi: " + ChatColor.WHITE + recipe.cookingTime + " tick");
        }

        if (recipe.experience > 0) {
            sender.sendMessage(ChatColor.GOLD + "Deneyim: " + ChatColor.WHITE + recipe.experience);
        }

        return true;
    }

    private boolean handleEnchant(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ultimateitems.enchant")) {
            player.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui enchant <enchant> <level>");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(PREFIX + ChatColor.RED + "Elinizde bir item tutmalısınız!");
            return true;
        }

        String enchantName = args[1];
        int level;

        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Geçersiz seviye!");
            return true;
        }

        // Custom enchant
        if (customEnchants.containsKey(enchantName)) {
            CustomEnchant customEnchant = customEnchants.get(enchantName);

            if (level > customEnchant.maxLevel) {
                player.sendMessage(PREFIX + ChatColor.RED + "Maksimum seviye: " + customEnchant.maxLevel);
                return true;
            }

            // TODO: Custom enchant sistemini implement et
            player.sendMessage(PREFIX + ChatColor.GREEN + "Custom enchant eklendi: " + customEnchant.displayName + " " + level);
        } else {
            // Vanilla enchant
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
            if (enchant == null) {
                player.sendMessage(PREFIX + ChatColor.RED + "Enchant bulunamadı!");
                return true;
            }

            item.addUnsafeEnchantment(enchant, level);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Enchant eklendi: " + enchant.getKey().getKey() + " " + level);
        }

        return true;
    }

    private boolean handleAbility(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ultimateitems.ability")) {
            player.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui ability <ability>");
            return true;
        }

        String abilityId = args[1];
        if (!customAbilities.containsKey(abilityId)) {
            player.sendMessage(PREFIX + ChatColor.RED + "Yetenek bulunamadı!");
            return true;
        }

        executeAbility(player, abilityId);

        return true;
    }

    private boolean handleEffect(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ultimateitems.effect")) {
            player.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui effect <effect> [süre] [güç]");
            return true;
        }

        String effectId = args[1];
        if (!customEffects.containsKey(effectId)) {
            player.sendMessage(PREFIX + ChatColor.RED + "Efekt bulunamadı!");
            return true;
        }

        CustomEffect effect = customEffects.get(effectId);

        int duration = effect.duration;
        int amplifier = effect.amplifier;

        if (args.length >= 3) {
            try {
                duration = Integer.parseInt(args[2]) * 20; // Saniye -> tick
            } catch (NumberFormatException ignored) {}
        }

        if (args.length >= 4) {
            try {
                amplifier = Integer.parseInt(args[3]) - 1;
            } catch (NumberFormatException ignored) {}
        }

        applyCustomEffect(player, effect, duration, amplifier);

        player.sendMessage(PREFIX + ChatColor.GREEN + "Efekt uygulandı: " + effect.displayName);

        return true;
    }

    private boolean handlePack(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui pack <regenerate|reload|send>");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "regenerate":
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "Resource pack yeniden oluşturuluyor...");
                generateResourcePack();
                scheduler.schedule(() ->
                                sender.sendMessage(PREFIX + ChatColor.GREEN + "Resource pack başarıyla oluşturuldu!"),
                        3, TimeUnit.SECONDS
                );
                break;

            case "reload":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    sendResourcePack(player);
                    sender.sendMessage(PREFIX + ChatColor.GREEN + "Resource pack gönderildi!");
                } else {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
                }
                break;

            case "send":
                if (args.length >= 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target != null) {
                        sendResourcePack(target);
                        sender.sendMessage(PREFIX + ChatColor.GREEN + "Resource pack " + target.getName() + " adlı oyuncuya gönderildi!");
                    } else {
                        sender.sendMessage(PREFIX + ChatColor.RED + "Oyuncu bulunamadı!");
                    }
                } else {
                    // Tüm oyunculara gönder
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        sendResourcePack(p);
                    }
                    sender.sendMessage(PREFIX + ChatColor.GREEN + "Resource pack tüm oyunculara gönderildi!");
                }
                break;

            default:
                sender.sendMessage(PREFIX + ChatColor.RED + "Geçersiz alt komut!");
                break;
        }

        return true;
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.YELLOW + "Debug modu: " + (debugMode ? "AÇIK" : "KAPALI"));
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "on":
            case "true":
            case "enable":
                debugMode = true;
                mainConfig.set("debug", true);
                saveConfig();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Debug modu açıldı!");
                break;

            case "off":
            case "false":
            case "disable":
                debugMode = false;
                mainConfig.set("debug", false);
                saveConfig();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Debug modu kapatıldı!");
                break;

            default:
                sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui debug <on|off>");
                break;
        }

        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.stats")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Oyuncu bulunamadı!");
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(PREFIX + ChatColor.RED + "Konsol için oyuncu belirtmelisiniz!");
            return true;
        }

        PlayerData data = playerData.get(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Oyuncu verisi bulunamadı!");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "İstatistikler: " + target.getName() + ChatColor.DARK_PURPLE + " ==========");
        sender.sendMessage(ChatColor.GOLD + "Açılan itemler: " + ChatColor.WHITE + data.statistics.getOrDefault("items_unlocked", 0));
        sender.sendMessage(ChatColor.GOLD + "Kullanılan yetenekler: " + ChatColor.WHITE + data.statistics.getOrDefault("abilities_used", 0));
        sender.sendMessage(ChatColor.GOLD + "Toplanan itemler: " + ChatColor.WHITE + data.statistics.getOrDefault("items_collected", 0));
        sender.sendMessage(ChatColor.GOLD + "Craft edilen itemler: " + ChatColor.WHITE + data.statistics.getOrDefault("items_crafted", 0));
        sender.sendMessage(ChatColor.GOLD + "Resource pack yüklendi: " + ChatColor.WHITE + (data.resourcePackLoaded ? "Evet" : "Hayır"));

        return true;
    }

    private boolean handleBackup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Yedekleme oluşturuluyor...");

        executor.submit(() -> {
            try {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                Path backupPath = Paths.get(getDataFolder().getAbsolutePath(), "backups", "backup_" + timestamp);
                backupPath.toFile().mkdirs();

                // Yapılandırmaları yedekle
                Files.copy(new File(getDataFolder(), "config.yml").toPath(),
                        backupPath.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);

                String[] configFiles = {"items.yml", "blocks.yml", "recipes.yml", "enchants.yml",
                        "effects.yml", "abilities.yml", "permissions.yml"};

                for (String file : configFiles) {
                    File sourceFile = new File(getDataFolder(), file);
                    if (sourceFile.exists()) {
                        Files.copy(sourceFile.toPath(), backupPath.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // Texture'ları yedekle
                if (texturesPath.toFile().exists()) {
                    FileUtils.copyDirectory(texturesPath.toFile(), backupPath.resolve("textures").toFile());
                }

                // ZIP oluştur
                Path zipPath = Paths.get(getDataFolder().getAbsolutePath(), "backups", "backup_" + timestamp + ".zip");
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                    Files.walk(backupPath)
                            .filter(path -> !Files.isDirectory(path))
                            .forEach(path -> {
                                try {
                                    ZipEntry entry = new ZipEntry(backupPath.relativize(path).toString());
                                    zos.putNextEntry(entry);
                                    Files.copy(path, zos);
                                    zos.closeEntry();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }

                // Geçici klasörü sil
                FileUtils.deleteDirectory(backupPath.toFile());

                sender.sendMessage(PREFIX + ChatColor.GREEN + "Yedekleme başarıyla oluşturuldu: backup_" + timestamp + ".zip");

            } catch (Exception e) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Yedekleme oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return true;
    }

    private boolean handleRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            // Mevcut yedeklemeleri listele
            File backupsDir = new File(getDataFolder(), "backups");
            if (!backupsDir.exists() || !backupsDir.isDirectory()) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Yedekleme bulunamadı!");
                return true;
            }

            File[] backups = backupsDir.listFiles((dir, name) -> name.endsWith(".zip"));
            if (backups == null || backups.length == 0) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Yedekleme bulunamadı!");
                return true;
            }

            sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "Mevcut Yedeklemeler" + ChatColor.DARK_PURPLE + " ==========");
            for (File backup : backups) {
                sender.sendMessage(ChatColor.GRAY + "- " + backup.getName());
            }
            sender.sendMessage(ChatColor.YELLOW + "Geri yüklemek için: /ui restore <dosya_adı>");

            return true;
        }

        String backupName = args[1];
        if (!backupName.endsWith(".zip")) {
            backupName += ".zip";
        }

        File backupFile = new File(getDataFolder(), "backups/" + backupName);
        if (!backupFile.exists()) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Yedekleme dosyası bulunamadı!");
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Yedekleme geri yükleniyor...");

        executor.submit(() -> {
            try {
                // Geçici klasöre çıkar
                Path tempPath = Paths.get(getDataFolder().getAbsolutePath(), "temp", "restore_" + System.currentTimeMillis());
                tempPath.toFile().mkdirs();

                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        Path outputPath = tempPath.resolve(entry.getName());

                        if (entry.isDirectory()) {
                            outputPath.toFile().mkdirs();
                        } else {
                            outputPath.getParent().toFile().mkdirs();
                            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }

                        zis.closeEntry();
                    }
                }

                // Dosyaları geri yükle
                Files.walk(tempPath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(source -> {
                            try {
                                Path relative = tempPath.relativize(source);
                                Path destination = getDataFolder().toPath().resolve(relative);
                                destination.getParent().toFile().mkdirs();
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                // Geçici klasörü temizle
                FileUtils.deleteDirectory(tempPath.toFile());

                // Plugin'i yeniden yükle
                Bukkit.getScheduler().runTask(this, () -> {
                    handleReload(sender);
                    sender.sendMessage(PREFIX + ChatColor.GREEN + "Yedekleme başarıyla geri yüklendi!");
                });

            } catch (Exception e) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Yedekleme geri yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return true;
    }

    private boolean handleExport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui export <item|all>");
            return true;
        }

        if (args[1].equalsIgnoreCase("all")) {
            // Tüm itemleri dışa aktar
            JsonObject exportData = new JsonObject();

            // Items
            JsonObject itemsJson = new JsonObject();
            for (Map.Entry<String, CustomItem> entry : customItems.entrySet()) {
                itemsJson.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
            }
            exportData.add("items", itemsJson);

            // Blocks
            JsonObject blocksJson = new JsonObject();
            for (Map.Entry<String, CustomBlock> entry : customBlocks.entrySet()) {
                blocksJson.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
            }
            exportData.add("blocks", blocksJson);

            // Recipes
            JsonObject recipesJson = new JsonObject();
            for (Map.Entry<String, CustomRecipe> entry : customRecipes.entrySet()) {
                recipesJson.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
            }
            exportData.add("recipes", recipesJson);

            // Metadata
            JsonObject metadata = new JsonObject();
            metadata.addProperty("version", getDescription().getVersion());
            metadata.addProperty("exportDate", System.currentTimeMillis());
            metadata.addProperty("itemCount", customItems.size());
            exportData.add("metadata", metadata);

            // Dosyaya yaz
            String filename = "export_all_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json";
            File exportFile = new File(getDataFolder(), "exports/" + filename);
            exportFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(exportFile)) {
                gson.toJson(exportData, writer);
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Veriler dışa aktarıldı: " + filename);
            } catch (IOException e) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Dışa aktarma hatası: " + e.getMessage());
            }

        } else {
            // Belirli bir itemi dışa aktar
            String itemId = args[1];
            if (!customItems.containsKey(itemId)) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Item bulunamadı!");
                return true;
            }

            CustomItem item = customItems.get(itemId);
            JsonObject exportData = new JsonObject();
            exportData.add("item", gson.toJsonTree(item));

            // İlgili recipe
            if (customRecipes.containsKey(itemId)) {
                exportData.add("recipe", gson.toJsonTree(customRecipes.get(itemId)));
            }

            // Metadata
            JsonObject metadata = new JsonObject();
            metadata.addProperty("version", getDescription().getVersion());
            metadata.addProperty("exportDate", System.currentTimeMillis());
            exportData.add("metadata", metadata);

            String filename = "export_" + itemId + "_" + System.currentTimeMillis() + ".json";
            File exportFile = new File(getDataFolder(), "exports/" + filename);
            exportFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(exportFile)) {
                gson.toJson(exportData, writer);
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Item dışa aktarıldı: " + filename);
            } catch (IOException e) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Dışa aktarma hatası: " + e.getMessage());
            }
        }

        return true;
    }

    private boolean handleImport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateitems.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
            return true;
        }

        if (args.length < 2) {
            // Mevcut dosyaları listele
            File exportsDir = new File(getDataFolder(), "exports");
            if (!exportsDir.exists() || !exportsDir.isDirectory()) {
                sender.sendMessage(PREFIX + ChatColor.RED + "İçe aktarılacak dosya bulunamadı!");
                return true;
            }

            File[] exports = exportsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (exports == null || exports.length == 0) {
                sender.sendMessage(PREFIX + ChatColor.RED + "İçe aktarılacak dosya bulunamadı!");
                return true;
            }

            sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "İçe Aktarılabilir Dosyalar" + ChatColor.DARK_PURPLE + " ==========");
            for (File export : exports) {
                sender.sendMessage(ChatColor.GRAY + "- " + export.getName());
            }
            sender.sendMessage(ChatColor.YELLOW + "İçe aktarmak için: /ui import <dosya_adı>");

            return true;
        }

        String filename = args[1];
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        File importFile = new File(getDataFolder(), "exports/" + filename);
        if (!importFile.exists()) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Dosya bulunamadı!");
            return true;
        }

        try (FileReader reader = new FileReader(importFile)) {
            JsonObject importData = gson.fromJson(reader, JsonObject.class);

            int imported = 0;

            // Items
            if (importData.has("items")) {
                JsonObject items = importData.getAsJsonObject("items");
                for (Map.Entry<String, JsonElement> entry : items.entrySet()) {
                    CustomItem item = gson.fromJson(entry.getValue(), CustomItem.class);
                    customItems.put(entry.getKey(), item);
                    saveItemToConfig(entry.getKey(), item);
                    imported++;
                }
            } else if (importData.has("item")) {
                // Tek item
                CustomItem item = gson.fromJson(importData.get("item"), CustomItem.class);
                customItems.put(item.id, item);
                saveItemToConfig(item.id, item);
                imported = 1;
            }

            // Recipes
            if (importData.has("recipes")) {
                JsonObject recipes = importData.getAsJsonObject("recipes");
                for (Map.Entry<String, JsonElement> entry : recipes.entrySet()) {
                    CustomRecipe recipe = gson.fromJson(entry.getValue(), CustomRecipe.class);
                    customRecipes.put(entry.getKey(), recipe);
                    saveRecipeToConfig(entry.getKey(), recipe);
                }
            } else if (importData.has("recipe")) {
                CustomRecipe recipe = gson.fromJson(importData.get("recipe"), CustomRecipe.class);
                customRecipes.put(recipe.id, recipe);
                saveRecipeToConfig(recipe.id, recipe);
            }

            // Resource pack'i güncelle
            scheduler.schedule(() -> generateResourcePack(), 1, TimeUnit.SECONDS);

            sender.sendMessage(PREFIX + ChatColor.GREEN + imported + " adet item içe aktarıldı!");

        } catch (Exception e) {
            sender.sendMessage(PREFIX + ChatColor.RED + "İçe aktarma hatası: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void saveItemToConfig(String id, CustomItem item) {
        itemsConfig.set(id + ".displayName", item.displayName);
        itemsConfig.set(id + ".material", item.baseMaterial.name());
        itemsConfig.set(id + ".texture", item.texture);
        itemsConfig.set(id + ".model", item.model);
        itemsConfig.set(id + ".customModelData", item.customModelData);
        itemsConfig.set(id + ".lore", item.lore);
        itemsConfig.set(id + ".damage", item.damage);
        itemsConfig.set(id + ".defense", item.defense);
        itemsConfig.set(id + ".rarity", item.rarity);
        itemsConfig.set(id + ".category", item.category);
        itemsConfig.set(id + ".tier", item.tier);
        itemsConfig.set(id + ".enabled", item.enabled);

        if (item.enchantments != null) {
            for (Map.Entry<String, Integer> entry : item.enchantments.entrySet()) {
                itemsConfig.set(id + ".enchantments." + entry.getKey(), entry.getValue());
            }
        }

        if (item.abilities != null) {
            itemsConfig.set(id + ".abilities", item.abilities);
        }

        saveConfig(itemsConfig, "items.yml");
    }

    private void saveRecipeToConfig(String id, CustomRecipe recipe) {
        recipesConfig.set(id + ".type", recipe.type);
        recipesConfig.set(id + ".result", recipe.result);
        recipesConfig.set(id + ".amount", recipe.amount);

        if (recipe.pattern != null) {
            recipesConfig.set(id + ".pattern", Arrays.asList(recipe.pattern));
        }

        if (recipe.ingredients != null) {
            for (Map.Entry<String, String> entry : recipe.ingredients.entrySet()) {
                recipesConfig.set(id + ".ingredients." + entry.getKey(), entry.getValue());
            }
        }

        recipesConfig.set(id + ".cookingTime", recipe.cookingTime);
        recipesConfig.set(id + ".experience", recipe.experience);

        saveConfig(recipesConfig, "recipes.yml");
    }

    private void startTasks() {
        // Otomatik kaydetme
        scheduler.scheduleAtFixedRate(() -> {
            saveAllData();
            if (debugMode) {
                getLogger().info("Otomatik kaydetme tamamlandı.");
            }
        }, 5, 5, TimeUnit.MINUTES);

        // Cooldown temizleme
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (PlayerData data : playerData.values()) {
                data.abilityCooldowns.entrySet().removeIf(entry -> entry.getValue() < currentTime);
                data.itemCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
            }
        }, 1, 1, TimeUnit.SECONDS);

        // Particle ve efekt güncelleme
        new BukkitRunnable() {
            public void run() {
                updateActiveEffects();
                updateParticles();
            }
        }.runTaskTimer(this, 0, 1); // Her tick

        // Performans monitörü
        if (debugMode) {
            scheduler.scheduleAtFixedRate(() -> {
                Runtime runtime = Runtime.getRuntime();
                long memory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
                getLogger().info("Bellek kullanımı: " + memory + "MB");
            }, 1, 1, TimeUnit.MINUTES);
        }
    }

    private void startMetrics() {
        // bStats entegrasyonu
        // Metrics metrics = new Metrics(this, PLUGIN_ID);
        // Custom charts eklenebilir
    }

    private void saveAllData() {
        // Player data
        File playerDataFile = new File(getDataFolder(), "data/players.json");
        playerDataFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(playerDataFile)) {
            gson.toJson(playerData, writer);
        } catch (IOException e) {
            getLogger().severe("Player verileri kaydedilemedi: " + e.getMessage());
        }

        // İstatistikler
        saveStatistics();
    }

    private void saveStatistics() {
        JsonObject stats = new JsonObject();
        stats.addProperty("totalPlayers", playerData.size());
        stats.addProperty("totalItems", customItems.size());
        stats.addProperty("totalAbilities", customAbilities.size());
        stats.addProperty("serverStartTime", ManagementFactory.getRuntimeMXBean().getStartTime());

        File statsFile = new File(getDataFolder(), "data/statistics.json");
        try (FileWriter writer = new FileWriter(statsFile)) {
            gson.toJson(stats, writer);
        } catch (IOException e) {
            getLogger().severe("İstatistikler kaydedilemedi: " + e.getMessage());
        }
    }

    // Event handlers
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Player data oluştur/yükle
        if (!playerData.containsKey(player.getUniqueId())) {
            PlayerData data = new PlayerData();
            data.uuid = player.getUniqueId();
            data.name = player.getName();
            data.lastSeen = System.currentTimeMillis();
            playerData.put(player.getUniqueId(), data);
        }

        // Resource pack gönder
        if (mainConfig.getBoolean("resource-pack.auto-send", true)) {
            scheduler.schedule(() -> sendResourcePack(player), 2, TimeUnit.SECONDS);
        }
    }

    private void sendResourcePack(Player player) {
        if (resourcePackUrl != null && resourcePackHash != null) {
            resourcePackPendingPlayers.add(player.getUniqueId());
            player.setResourcePack(resourcePackUrl, resourcePackHash);
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerData data = playerData.get(player.getUniqueId());

        if (data == null) return;

        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                data.resourcePackLoaded = true;
                resourcePackPendingPlayers.remove(player.getUniqueId());
                player.sendMessage(PREFIX + ChatColor.GREEN + "Resource pack başarıyla yüklendi!");
                break;

            case DECLINED:
                resourcePackPendingPlayers.remove(player.getUniqueId());
                if (mainConfig.getBoolean("resource-pack.required", false)) {
                    player.kickPlayer(ChatColor.RED + "Resource pack kabul edilmedi!");
                } else {
                    player.sendMessage(PREFIX + ChatColor.YELLOW + "Resource pack reddedildi. Bazı özellikler düzgün çalışmayabilir.");
                }
                break;

            case FAILED_DOWNLOAD:
                resourcePackPendingPlayers.remove(player.getUniqueId());
                player.sendMessage(PREFIX + ChatColor.RED + "Resource pack indirilemedi!");
                break;

            case ACCEPTED:
                player.sendMessage(PREFIX + ChatColor.YELLOW + "Resource pack indiriliyor...");
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) return;

        // Custom item kontrolü
        String customItemId = getCustomItemId(item);
        if (customItemId == null) return;

        CustomItem customItem = customItems.get(customItemId);
        if (customItem == null) return;

        // Cooldown kontrolü
        PlayerData data = playerData.get(player.getUniqueId());
        if (data.itemCooldowns.containsKey(customItemId)) {
            int remaining = data.itemCooldowns.get(customItemId);
            if (remaining > 0) {
                player.sendMessage(PREFIX + ChatColor.RED + "Bekleme süresi: " + remaining + " saniye");
                event.setCancelled(true);
                return;
            }
        }

        // Ability kontrolü
        if (customItem.abilities != null && !customItem.abilities.isEmpty()) {
            String activation = null;

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                activation = "RIGHT_CLICK";
            } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                activation = "LEFT_CLICK";
            }

            if (activation != null) {
                for (String abilityId : customItem.abilities) {
                    CustomAbility ability = customAbilities.get(abilityId);
                    if (ability != null && ability.activation.equals(activation)) {
                        executeAbility(player, abilityId);

                        // Cooldown uygula
                        if (customItem.cooldowns != null && customItem.cooldowns.containsKey(abilityId)) {
                            int cooldown = ((Number) customItem.cooldowns.get(abilityId)).intValue();
                            data.itemCooldowns.put(customItemId, cooldown);
                        }
                    }
                }
            }
        }

        // Custom events
        if (customItem.events != null && customItem.events.containsKey("on_interact")) {
            executeCustomEvent(player, customItem.events.get("on_interact"));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (weapon == null || weapon.getType() == Material.AIR) return;

        String customItemId = getCustomItemId(weapon);
        if (customItemId == null) return;

        CustomItem customItem = customItems.get(customItemId);
        if (customItem == null) return;

        // Custom damage
        if (customItem.damage > 0) {
            event.setDamage(customItem.damage);
        }

        // Critical hit
        if (customItem.critChance > 0 && random.nextDouble() * 100 < customItem.critChance) {
            event.setDamage(event.getDamage() * customItem.critDamage);

            // Crit effect
            player.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
        }

        // Lifesteal
        if (customItem.lifesteal > 0) {
            double heal = event.getFinalDamage() * (customItem.lifesteal / 100);
            player.setHealth(Math.min(player.getHealth() + heal, player.getAttribute(Attribute.MAX_HEALTH).getValue()));
        }

        // On hit effects
        if (customItem.onHit != null) {
            for (Map.Entry<String, Object> entry : customItem.onHit.entrySet()) {
                executeOnHitEffect(player, event.getEntity(), entry.getKey(), entry.getValue());
            }
        }

        // Special effects
        if (customItem.specialEffects != null && customItem.specialEffects.containsKey("lightning")) {
            if (random.nextDouble() < 0.1) { // %10 şans
                event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());
                if (event.getEntity() instanceof Damageable) {
                    ((Damageable) event.getEntity()).damage(5.0);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool == null || tool.getType() == Material.AIR) return;

        String customItemId = getCustomItemId(tool);
        if (customItemId == null) return;

        CustomItem customItem = customItems.get(customItemId);
        if (customItem == null) return;

        // Custom block kontrolü
        Block block = event.getBlock();
        String blockId = getCustomBlockId(block);

        if (blockId != null) {
            CustomBlock customBlock = customBlocks.get(blockId);
            if (customBlock != null) {
                // Tool requirement
                if (customBlock.tool != null && !customBlock.tool.equals(customItem.toolType)) {
                    event.setCancelled(true);
                    player.sendMessage(PREFIX + ChatColor.RED + "Bu bloğu kırmak için doğru aleti kullanmalısınız!");
                    return;
                }

                // Custom drops
                if (customBlock.drops != null && !customBlock.drops.isEmpty()) {
                    event.setDropItems(false);
                    Location loc = block.getLocation().add(0.5, 0.5, 0.5);

                    for (String drop : customBlock.drops) {
                        if (customItems.containsKey(drop)) {
                            CustomItem dropItem = customItems.get(drop);
                            block.getWorld().dropItemNaturally(loc, dropItem.createItemStack());
                        }
                    }
                }
            }
        }

        // Fortune effect
        if (customItem.bonuses != null && customItem.bonuses.containsKey("fortune")) {
            double fortuneLevel = (Double) customItem.bonuses.get("fortune");
            if (random.nextDouble() < fortuneLevel / 10) {
                // Ekstra drop
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                        new ItemStack(event.getBlock().getType())
                );
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();

        String customItemId = getCustomItemId(result);
        if (customItemId == null) return;

        CustomItem customItem = customItems.get(customItemId);
        if (customItem == null) return;

        // Requirements kontrolü
        if (customItem.requirements != null) {
            if (!checkRequirements(player, customItem.requirements)) {
                event.setCancelled(true);
                player.sendMessage(PREFIX + ChatColor.RED + "Bu itemi craft etmek için gereksinimleri karşılamıyorsunuz!");
                return;
            }
        }

        // İstatistik güncelle
        PlayerData data = playerData.get(player.getUniqueId());
        data.statistics.merge("items_crafted", 1, Integer::sum);

        // Event tetikle
        if (customItem.onCraft != null) {
            executeCustomEvent(player, customItem.onCraft);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        String customItemId = getCustomItemId(item);
        if (customItemId == null) return;

        CustomItem customItem = customItems.get(customItemId);
        if (customItem == null) return;

        // Soulbound kontrolü
        if (customItem.soulbound) {
            if (!item.getItemMeta().getPersistentDataContainer().has(
                    new NamespacedKey(this, "soulbound_owner"), PersistentDataType.STRING)) {
                // İlk kez alınıyor
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(this, "soulbound_owner"),
                        PersistentDataType.STRING,
                        player.getUniqueId().toString()
                );
                item.setItemMeta(meta);
            } else {
                // Sahip kontrolü
                String owner = item.getItemMeta().getPersistentDataContainer().get(
                        new NamespacedKey(this, "soulbound_owner"),
                        PersistentDataType.STRING
                );

                if (!owner.equals(player.getUniqueId().toString())) {
                    event.setCancelled(true);
                    player.sendMessage(PREFIX + ChatColor.RED + "Bu item size ait değil!");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        String customItemId = getCustomItemId(item);

        if (customItemId == null) return;

        CustomItem customItem = customItems.get(customItemId);
        if (customItem == null) return;

        // Droppable kontrolü
        if (!customItem.droppable) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(PREFIX + ChatColor.RED + "Bu item düşürülemez!");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Soulbound itemleri koru
        List<ItemStack> itemsToKeep = new ArrayList<>();

        for (ItemStack item : event.getDrops()) {
            String customItemId = getCustomItemId(item);
            if (customItemId != null) {
                CustomItem customItem = customItems.get(customItemId);
                if (customItem != null && customItem.soulbound) {
                    itemsToKeep.add(item);
                }
            }
        }

        event.getDrops().removeAll(itemsToKeep);

        // Respawn'da geri ver
        if (!itemsToKeep.isEmpty()) {
            scheduler.schedule(() -> {
                if (player.isOnline()) {
                    for (ItemStack item : itemsToKeep) {
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                        if (!leftover.isEmpty()) {
                            for (ItemStack drop : leftover.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), drop);
                            }
                        }
                    }
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        String customItemId = getCustomItemId(item);
        if (customItemId == null) return;

        // Custom food kontrolü
        if (customFoods.containsKey(customItemId)) {
            CustomFood food = customFoods.get(customItemId);

            // Custom hunger/saturation
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + food.hunger));
            player.setSaturation(Math.min(20, player.getSaturation() + food.saturation));

            // Effects
            if (food.effects != null) {
                for (PotionEffect effect : food.effects) {
                    player.addPotionEffect(effect);
                }
            }

            // Bonuses
            if (food.bonuses != null) {
                applyFoodBonuses(player, food.bonuses);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player shooter = (Player) event.getEntity().getShooter();
        Projectile projectile = event.getEntity();

        // Custom projectile kontrolü
        if (projectile.hasMetadata("custom_projectile")) {
            String projectileId = projectile.getMetadata("custom_projectile").get(0).asString();
            CustomProjectile customProj = customProjectiles.get(projectileId);

            if (customProj != null) {
                Location hitLoc = event.getHitEntity() != null
                        ? event.getHitEntity().getLocation()
                        : event.getHitBlock() != null
                        ? event.getHitBlock().getLocation()
                        : projectile.getLocation();

                // Area damage
                if (customProj.effects != null && customProj.effects.containsKey("explosion")) {
                    double radius = ((Number) customProj.effects.get("explosion")).doubleValue();
                    hitLoc.getWorld().createExplosion(hitLoc, (float) radius, false, false);
                }

                // Effects
                if (customProj.effects != null) {
                    for (Map.Entry<String, Object> effect : customProj.effects.entrySet()) {
                        applyProjectileEffect(shooter, hitLoc, effect.getKey(), effect.getValue());
                    }
                }
            }
        }
    }

    // Helper methods
    private String getCustomItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "custom_item_id"), PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(new NamespacedKey(this, "custom_item_id"), PersistentDataType.STRING);
        }

        return null;
    }

    private String getCustomBlockId(Block block) {
        // Block metadata sistemi implement edilmeli
        return null;
    }

    private void executeAbility(Player player, String abilityId) {
        CustomAbility ability = customAbilities.get(abilityId);
        if (ability == null) return;

        PlayerData data = playerData.get(player.getUniqueId());

        // Cooldown kontrolü
        if (data.abilityCooldowns.containsKey(abilityId)) {
            long remaining = data.abilityCooldowns.get(abilityId) - System.currentTimeMillis();
            if (remaining > 0) {
                player.sendMessage(PREFIX + ChatColor.RED + "Bekleme süresi: " + (remaining / 1000) + " saniye");
                return;
            }
        }

        // Mana kontrolü
        if (ability.manaCost > 0) {
            // Mana sistemi implement edilmeli
        }

        // Lightning Strike örnek ability
        if (abilityId.equals("lightning_strike")) {
            Block target = player.getTargetBlock(null, (int) ability.range);
            if (target != null && target.getType() != Material.AIR) {
                target.getWorld().strikeLightningEffect(target.getLocation());

                // Area damage
                for (Entity entity : target.getWorld().getNearbyEntities(target.getLocation(), ability.radius, ability.radius, ability.radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        ((LivingEntity) entity).damage(ability.damage, player);
                    }
                }

                // Effects
                if (ability.particle != null) {
                    try {
                        Particle particle = Particle.valueOf(ability.particle.toUpperCase());
                        target.getWorld().spawnParticle(particle, target.getLocation(), 50, ability.radius, 1, ability.radius);
                    } catch (IllegalArgumentException ignored) {}
                }

                if (ability.sound != null) {
                    try {
                        Sound sound = Sound.valueOf(ability.sound.toUpperCase());
                        target.getWorld().playSound(target.getLocation(), sound, 1.0f, 1.0f);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        // Cooldown uygula
        if (ability.cooldown > 0) {
            data.abilityCooldowns.put(abilityId, System.currentTimeMillis() + (ability.cooldown * 1000L));
        }

        // İstatistik güncelle
        data.statistics.merge("abilities_used", 1, Integer::sum);
    }

    private void applyCustomEffect(Player player, CustomEffect effect, int duration, int amplifier) {
        if (effect.type != null) {
            player.addPotionEffect(new PotionEffect(effect.type, duration, amplifier,
                    effect.ambient, effect.particles, effect.icon));
        }

        // Custom modifiers
        if (effect.modifiers != null) {
            for (Map.Entry<String, Object> modifier : effect.modifiers.entrySet()) {
                applyModifier(player, modifier.getKey(), modifier.getValue());
            }
        }

        // Active effect listesine ekle
        PlayerData data = playerData.get(player.getUniqueId());
        data.activeEffects.add(effect.id);

        // Süre sonunda kaldır
        scheduler.schedule(() -> {
            if (player.isOnline()) {
                data.activeEffects.remove(effect.id);
                removeModifier(player, effect.modifiers);
            }
        }, duration / 20, TimeUnit.SECONDS);
    }

    private void executeCustomEvent(Player player, Object eventData) {
        if (eventData instanceof Map) {
            Map<String, Object> event = (Map<String, Object>) eventData;

            // Commands
            if (event.containsKey("commands")) {
                List<String> commands = (List<String>) event.get("commands");
                for (String command : commands) {
                    String processedCommand = command
                            .replace("{player}", player.getName())
                            .replace("{uuid}", player.getUniqueId().toString())
                            .replace("{world}", player.getWorld().getName())
                            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                }
            }

            // Messages
            if (event.containsKey("messages")) {
                List<String> messages = (List<String>) event.get("messages");
                for (String message : messages) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }

            // Sounds
            if (event.containsKey("sounds")) {
                List<String> sounds = (List<String>) event.get("sounds");
                for (String sound : sounds) {
                    try {
                        player.playSound(player.getLocation(), Sound.valueOf(sound.toUpperCase()), 1.0f, 1.0f);
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            // Particles
            if (event.containsKey("particles")) {
                List<String> particles = (List<String>) event.get("particles");
                for (String particle : particles) {
                    try {
                        player.getWorld().spawnParticle(Particle.valueOf(particle.toUpperCase()),
                                player.getLocation(), 10, 0.5, 0.5, 0.5);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    private void executeOnHitEffect(Player attacker, Entity victim, String effect, Object data) {
        switch (effect.toLowerCase()) {
            case "poison":
                if (victim instanceof LivingEntity) {
                    int duration = data instanceof Integer ? (Integer) data : 100;
                    ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, 0));
                }
                break;

            case "fire":
                int fireTicks = data instanceof Integer ? (Integer) data : 100;
                victim.setFireTicks(fireTicks);
                break;

            case "freeze":
                if (victim instanceof LivingEntity) {
                    int duration = data instanceof Integer ? (Integer) data : 60;
                    ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 2));
                }
                break;

            case "knockback":
                double power = data instanceof Number ? ((Number) data).doubleValue() : 1.0;
                Vector knockback = attacker.getLocation().getDirection().multiply(power);
                victim.setVelocity(knockback);
                break;

            case "steal_health":
                if (victim instanceof LivingEntity) {
                    double amount = data instanceof Number ? ((Number) data).doubleValue() : 2.0;
                    LivingEntity living = (LivingEntity) victim;
                    living.damage(amount);
                    attacker.setHealth(Math.min(attacker.getHealth() + amount,
                            attacker.getAttribute(Attribute.MAX_HEALTH).getValue()));
                }
                break;
        }
    }

    private boolean checkRequirements(Player player, Map<String, Object> requirements) {
        for (Map.Entry<String, Object> req : requirements.entrySet()) {
            switch (req.getKey().toLowerCase()) {
                case "level":
                    int requiredLevel = ((Number) req.getValue()).intValue();
                    if (player.getLevel() < requiredLevel) {
                        return false;
                    }
                    break;

                case "permission":
                    String permission = (String) req.getValue();
                    if (!player.hasPermission(permission)) {
                        return false;
                    }
                    break;

                case "items":
                    Map<String, Integer> requiredItems = (Map<String, Integer>) req.getValue();
                    for (Map.Entry<String, Integer> itemReq : requiredItems.entrySet()) {
                        if (!hasItem(player, itemReq.getKey(), itemReq.getValue())) {
                            return false;
                        }
                    }
                    break;
            }
        }

        return true;
    }

    private boolean hasItem(Player player, String itemId, int amount) {
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            String id = getCustomItemId(item);
            if (id != null && id.equals(itemId)) {
                count += item.getAmount();
            } else if (item.getType().name().equals(itemId)) {
                count += item.getAmount();
            }

            if (count >= amount) {
                return true;
            }
        }

        return false;
    }

    private void applyModifier(Player player, String modifier, Object value) {
        // Modifier sistemi implement edilmeli
    }

    private void removeModifier(Player player, Map<String, Object> modifiers) {
        // Modifier kaldırma sistemi implement edilmeli
    }

    private void applyFoodBonuses(Player player, Map<String, Object> bonuses) {
        for (Map.Entry<String, Object> bonus : bonuses.entrySet()) {
            switch (bonus.getKey().toLowerCase()) {
                case "health":
                    double health = ((Number) bonus.getValue()).doubleValue();
                    player.setHealth(Math.min(player.getHealth() + health,
                            player.getAttribute(Attribute.MAX_HEALTH).getValue()));
                    break;

                case "speed":
                    int duration = 600; // 30 saniye
                    int amplifier = ((Number) bonus.getValue()).intValue() - 1;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
                    break;

                case "strength":
                    duration = 600;
                    amplifier = ((Number) bonus.getValue()).intValue() - 1;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, duration, amplifier));
                    break;
            }
        }
    }

    private void applyProjectileEffect(Player shooter, Location location, String effect, Object data) {
        switch (effect.toLowerCase()) {
            case "lightning":
                location.getWorld().strikeLightningEffect(location);
                break;

            case "ice_field":
                double radius = data instanceof Number ? ((Number) data).doubleValue() : 5.0;
                for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(shooter)) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                    }
                }
                location.getWorld().spawnParticle(Particle.SNOWFLAKE, location, 50, radius, 1, radius);
                break;

            case "poison_cloud":
                radius = data instanceof Number ? ((Number) data).doubleValue() : 3.0;
                for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(shooter)) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    }
                }
                location.getWorld().spawnParticle(Particle.SMOKE, location, 30, radius, 1, radius);
                break;
        }
    }

    private void updateActiveEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerData.get(player.getUniqueId());
            if (data == null || data.activeEffects.isEmpty()) continue;

            // Effect particles
            for (String effectId : data.activeEffects) {
                CustomEffect effect = customEffects.get(effectId);
                if (effect != null && effect.particle != null) {
                    try {
                        Particle particle = Particle.valueOf(effect.particle.toUpperCase());
                        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0),
                                1, 0.2, 0.2, 0.2, 0);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    private void updateParticles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) continue;

            String customItemId = getCustomItemId(item);
            if (customItemId == null) continue;

            CustomItem customItem = customItems.get(customItemId);
            if (customItem == null || customItem.particles == null) continue;

            // Item particles
            for (Map.Entry<String, String> particleEntry : customItem.particles.entrySet()) {
                if (particleEntry.getKey().equals("idle")) {
                    try {
                        Particle particle = Particle.valueOf(particleEntry.getValue().toUpperCase());
                        player.getWorld().spawnParticle(particle,
                                player.getLocation().add(0, 1, 0), 1, 0.1, 0.1, 0.1, 0);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    // Singleton instance getter
    public static UltimateItemsAdder getInstance() {
        return instance;
    }
}