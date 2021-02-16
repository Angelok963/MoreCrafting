package fr.mrfantivideo.morecrafting.Recipes.RecipesLoaders;

import fr.mrfantivideo.morecrafting.Configuration.Configs.ConfigSettings;
import fr.mrfantivideo.morecrafting.Main;
import fr.mrfantivideo.morecrafting.UnrealCoreImports.Flag;
import fr.mrfantivideo.morecrafting.UnrealCoreImports.ItemStackUtils;
import fr.mrfantivideo.morecrafting.Utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractRecipesLoader
{
    private String m_BasePath;

    public AbstractRecipesLoader(String basePath)
    {
        m_BasePath = basePath;
        if (!m_BasePath.endsWith("."))
            m_BasePath += ".";
    }

    protected abstract void OnRecipeLoaded(Object recipe, int bookInventorySlot, String recipeName);

    /**
     * Get base item path
     *
     * @return base path
     */
    public String GetBasePath()
    {
        return m_BasePath;
    }

    /**
     * Get config settings
     *
     * @return Config Settings
     */
    public ConfigSettings GetConfig()
    {
        return Main.getInstance().getConfigSettings();
    }

    /**
     * Load Recipes
     */
    public void LoadRecipe()
    {
        for (String recipe : GetConfig().GetRecipes(GetBasePath()))
        {
            if (!GetConfig().GetBool(GetFormattedPath(recipe, "enabled")))
                continue;
            //TODO Angelok963 code REPLACE
            //ItemStack resultItem = GetRecipeResult(recipe);
            ItemStack resultItem = getConfigItem(recipe, -1);
            //TODO Angelok963 code END
            if (resultItem == null)
                continue;
            Object shapedRecipe = GetRecipe(recipe, resultItem);
            if (shapedRecipe == null)
                continue;
            int bookInventorySlot = GetConfig().GetInt(GetFormattedPath(recipe, "craft.result.id-book"));
            OnRecipeLoaded(shapedRecipe, bookInventorySlot, recipe);
        }
    }

    protected ItemStack GetPlayerHead(UUID playerUUID)
    {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) ItemStackUtils.getMeta(head);
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        head.setItemMeta(meta);
        return head;
    }

    /**
     * Get Recipe
     *
     * @param recipeName Recipe Name
     * @param stack      Recipe Result
     * @return Recipe
     */
    protected Object GetRecipe(String recipeName, ItemStack stack)
    {
        //TODO Angelok963 code REPLACE
        //ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.randomKey(), stack);
        NamespacedKey key = new NamespacedKey(Main.getInstance(), recipeName);
        Bukkit.removeRecipe(key);
        ShapedRecipe recipe = new ShapedRecipe(key, stack);
        //TODO Angelok963 code END
        recipe.shape("123", "456", "789");
        for (int i = 1; i <= 9; i++)
        {
            /*TODO Angelok963 code REPLACE
            String itemMaterial = GetConfig().GetString(GetFormattedPath(recipeName, "craft.slots." + i));
            if (itemMaterial == null || itemMaterial.isEmpty())
                continue;
            Material material = Material.getMaterial(itemMaterial);
            if (material == null)
                continue;
            recipe.setIngredient(Integer.toString(i).charAt(0), material);
            */
            if(!GetConfig().GetConfiguration().contains(GetFormattedPath(recipeName, "craft.slots."+i)))
                continue;
            ItemStack ingredient = this.getConfigItem(recipeName, i);
            if(ingredient == null)
                continue;
            recipe.setIngredient(Integer.toString(i).charAt(0), new RecipeChoice.ExactChoice(ingredient));
            //TODO Angelok963 code END
        }
        //TODO Angelok963 code START
        if(GetConfig().GetConfiguration().contains(GetFormattedPath(recipeName, "craft.result.group")))
        {
            String group = GetConfig().GetString(GetFormattedPath(recipeName, "craft.result.group"));
            if(group != null && !group.isEmpty())
                recipe.setGroup(group);
        }
        //TODO Angelok963 code END
        return recipe;
    }

    /**
     * Get the result of the specified recipe
     *
     * @param recipeName Recipe Name
     * @return CustomStack
     */
    //TODO Angelok963 code Add param itemID and rename metod GetRecipeResult to getConfigItem
    protected ItemStack getConfigItem(String recipeName, int itemID)
    {
        //TODO Angelok963 code START
        final String path = "craft."+(itemID == -1 ? "result" : "slots."+itemID);
        //TODO Angelok963 code END

        ItemStack resultItem;
        //TODO Angelok963 code REPLACE
        //if (GetConfig().GetConfiguration().contains(GetFormattedPath(recipeName, "craft.result.uuid")))
        if (GetConfig().GetConfiguration().contains(GetFormattedPath(recipeName, path+".uuid")))
        //TODO Angelok963 code END
        {
            //TODO Angelok963 code REPLACE
            //UUID uuid = GetConfig().GetUUID(GetFormattedPath(recipeName, "craft.result.uuid"));
            UUID uuid = GetConfig().GetUUID(GetFormattedPath(recipeName, path+".uuid"));
            //TODO Angelok963 code END
            if (uuid == null)
            {
                Bukkit.getServer().getConsoleSender().sendMessage("Couldn't load recipe '" + recipeName + "', Invalid UUID");
                return new ItemStack(Material.AIR);
            }
            resultItem = GetPlayerHead(uuid);
        }
        else
        {
            //TODO Angelok963 code REPLACE

            //String craftMaterial = GetConfig().GetString(GetFormattedPath(recipeName, "craft.result.id"));
            String craftMaterial = GetConfig().GetString(GetFormattedPath(recipeName, path+".id"));
            //TODO Angelok963 code END
            if (craftMaterial == null || Material.getMaterial(craftMaterial) == null)
                return null;
            //TODO Angelok963 code REPLACE
            //int resultItemAmount = GetConfig().GetInt(GetFormattedPath(recipeName, "craft.result.id-amount"));int resultItemAmount = GetConfig().GetInt(GetFormattedPath(recipeName, "craft.result.id-amount"));
            int resultItemAmount = itemID != -1 ? 1 : GetConfig().GetInt(GetFormattedPath(recipeName, path+".id-amount"));
            //TODO Angelok963 code END
            if (resultItemAmount <= 0)
                return null;
            resultItem = new ItemStack(Material.getMaterial(craftMaterial), resultItemAmount);
        }
        //TODO Angelok963 code REPLACE
        //ApplyEnchantments(recipeName, resultItem);
        //ApplyPotions(recipeName, resultItem);
        ApplyEnchantments(recipeName, resultItem, itemID);
        ApplyPotions(recipeName, resultItem, itemID);
        //TODO Angelok963 code END

        //TODO Angelok963 code REPLACE
        //Flag.setFlag(resultItem, "recipeName", recipeName, PersistentDataType.STRING);
        if(itemID == -1)
            Flag.setFlag(resultItem, "recipeName", recipeName, PersistentDataType.STRING);
        //TODO Angelok963 code END
        ItemMeta resultItemMeta = resultItem.getItemMeta();
        //TODO Angelok963 code REPLACE
        //String craftCustomName = GetConfig().GetString(GetFormattedPath(recipeName, "craft.result.name"));
        String craftCustomName = GetConfig().GetString(GetFormattedPath(recipeName, path+".name"));
        //TODO Angelok963 code END
        if (craftCustomName != null && !craftCustomName.isEmpty())
            resultItemMeta.setDisplayName(craftCustomName.replace("&", "§"));
        //TODO Angelok963 code REPLACE
        //String craftCustomLore = GetConfig().GetString(GetFormattedPath(recipeName, "craft.result.lore"));
        String craftCustomLore = GetConfig().GetString(GetFormattedPath(recipeName, path+".lore"));
        //TODO Angelok963 code END
        if (craftCustomLore != null && !craftCustomLore.isEmpty())
            resultItemMeta.setLore(Arrays.asList((craftCustomLore).replace("&", "§")));
        //TODO Angelok963 code START
        String customData = GetFormattedPath(recipeName, path+".custom-model-data");
        if(resultItemMeta != null && GetConfig().GetConfiguration().contains(customData))
        {
            resultItemMeta.setCustomModelData(GetConfig().GetInt(customData));
        }
        for(EquipmentSlot slot : EquipmentSlot.values())
            this.applyAtributes(recipeName, slot, resultItemMeta, itemID);
        //TODO Angelok963 code END
        resultItem.setItemMeta(resultItemMeta);
        return resultItem;
    }
    //TODO Angelok963 code START
    /**
     * Apply AttributeModifies to the the specified itemMeta
     * @param recipeName Recipe name
     * @param stackMeta ItemMeta
     * @param itemID Ingradient ID (-1 if it is recipeResult)
     */
    private void applyAtributes(String recipeName, EquipmentSlot slot, ItemMeta stackMeta, int itemID)
    {
        String type = slot.name();
        String path = GetFormattedPath(recipeName, "craft."+(itemID == -1 ? "result" : "slots."+itemID)+".attribute_modifiers");
        if(GetConfig().GetConfiguration().contains(path+"."+type))
        {
            for(String key : GetConfig().GetSection(path+"."+type))
            {
                Attribute atr;
                try {
                    atr = Attribute.valueOf(key);
                } catch (IllegalArgumentException error)
                {
                    LogUtils.LogError("Attribute "+key.toUpperCase() + " not found and skip!");
                    continue;
                }
                double value = 0.0D;
                try{
                    value = GetConfig().GetDouble(path+"."+type+"."+key);
                } catch (ClassCastException e)
                {
                    try{
                        value = GetConfig().GetInt(path+"."+type+"."+key);
                    } catch (ClassCastException e1)
                    {
                        LogUtils.LogError("("+recipeName+" | "+key+")Value \""+path+"."+type+"."+key+"\" must be Integer or Double. Using 0.0 value.");
                    }
                }
                AttributeModifier atrModifier = new AttributeModifier(UUID.randomUUID(), atr.getKey().getKey(), value, AttributeModifier.Operation.ADD_NUMBER, slot);
                stackMeta.addAttributeModifier(atr, atrModifier);
            }
        }
    }
    //TODO Angelok963 code END

    /**
     * Apply available enchantments to the specified customstack
     *
     * @param recipeName Recipe Name
     * @param stack      Custom Stack
     */
    //TODO Angelok963 code Add param itemID
    protected void ApplyEnchantments(String recipeName, ItemStack stack, int itemID)
    {
        //TODO Angelok963 code START
        final String path = "craft."+(itemID == -1 ? "result" : "slots."+itemID)+".enchantments";
        //TODO Angelok963 code END

        //TODO Angelok963 code REPLACE
        //Set<String> enchantSet = GetConfig().GetSection(GetFormattedPath(recipeName, "craft.result.enchantments"));
        Set<String> enchantSet = GetConfig().GetSection(GetFormattedPath(recipeName, path));
        //TODO Angelok963 code END
        if (enchantSet == null)
            return;
        for (String enchant : enchantSet)
        {
            //TODO Angelok963 code REPLACE
            //String enchantName = GetConfig().GetString(GetFormattedPath(recipeName, "craft.result.enchantments." + enchant + ".enchant"));
            String enchantName = GetConfig().GetString(GetFormattedPath(recipeName, path+"." + enchant + ".enchant"));
            //TODO Angelok963 code END
            if (enchantName == null || enchantName.isEmpty())
                continue;
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
            if (enchantment == null)
                continue;
            //TODO Angelok963 code REPLACE
            //int enchantLevel = GetConfig().GetInt(GetFormattedPath(recipeName, "craft.result.enchantments." + enchant + ".enchant-level"));
            int enchantLevel = GetConfig().GetInt(GetFormattedPath(recipeName, path+"." + enchant + ".enchant-level"));
            //TODO Angelok963 code END
            if (enchantLevel <= 0)
                continue;
            stack.addUnsafeEnchantment(enchantment, enchantLevel);
        }
    }

    //TODO Angelok963 code Add param itemID
    protected void ApplyPotions(String recipeName, ItemStack stack, int itemID)
    {
        //TODO Angelok963 code START
        final String path = "craft."+(itemID == -1 ? "result" : "slots."+itemID)+".potions";
        //TODO Angelok963 code END

        //TODO Angelok963 code REPLACE
        //Set<String> potionSet = GetConfig().GetSection(GetFormattedPath(recipeName, "craft.result.potions"));
        Set<String> potionSet = GetConfig().GetSection(GetFormattedPath(recipeName, path));
        //TODO Angelok963 code END
        if (potionSet == null)
            return;
        for (String potion : potionSet)
        {
        	ItemStack potionmeta = new ItemStack(Material.POTION, 1);
        	PotionMeta meta = (PotionMeta) ItemStackUtils.getMeta(potionmeta);
            //TODO Angelok963 code REPLACE
            //int duration = GetConfig().GetInt(GetFormattedPath(recipeName, "craft.result.potions." + potion + ".duration"));
            int duration = GetConfig().GetInt(GetFormattedPath(recipeName, path+"." + potion + ".duration"));
            //TODO Angelok963 code END
            if (duration <= 0)
                continue;
            //TODO Angelok963 code REPLACE
            //int level = GetConfig().GetInt(GetFormattedPath(recipeName, "craft.result.potions." + potion + ".level"));
            int level = GetConfig().GetInt(GetFormattedPath(recipeName, path+"." + potion + ".level"));
            //TODO Angelok963 code END
            if (level <= 0)
                continue;
            /*TODO Angelok963 code REPLACE
            boolean ambient = GetConfig().GetBool(GetFormattedPath(recipeName, "craft.result.potions." + potion + ".ambient"));
            boolean particles = GetConfig().GetBool(GetFormattedPath(recipeName, "craft.result.potions." + potion + ".particles"));
            boolean icon = GetConfig().GetBool(GetFormattedPath(recipeName, "craft.result.potions." + potion + ".icon"));
            PotionEffect effect = new PotionEffect(PotionEffectType.getByName(GetConfig().GetString(GetFormattedPath(recipeName, "craft.result.potions." + potion + ".effect"))), duration, level, ambient, particles, icon);
            */
            boolean ambient = GetConfig().GetBool(GetFormattedPath(recipeName, path+"." + potion + ".ambient"));
            boolean particles = GetConfig().GetBool(GetFormattedPath(recipeName, path+"." + potion + ".particles"));
            boolean icon = GetConfig().GetBool(GetFormattedPath(recipeName, path+"." + potion + ".icon"));
            PotionEffect effect = new PotionEffect(PotionEffectType.getByName(GetConfig().GetString(GetFormattedPath(recipeName, path+"." + potion + ".effect"))), duration, level, ambient, particles, icon);
            //TODO Angelok963 code END
            meta.addCustomEffect(effect, ambient);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Get formatted path
     *
     * @param recipeName  Recipe Name
     * @param pathToValue Path to Value
     * @return Formatted Path
     */
    protected String GetFormattedPath(String recipeName, String pathToValue)
    {
        String finalPath = GetBasePath();
        if (!finalPath.endsWith("."))
            finalPath += ".";
        finalPath += recipeName;
        if (!pathToValue.startsWith("."))
            finalPath += ".";
        finalPath += pathToValue;
        return finalPath;
    }
}
