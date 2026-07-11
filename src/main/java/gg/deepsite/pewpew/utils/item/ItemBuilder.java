package gg.deepsite.pewpew.utils.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.PersistentDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.util.*;

public class ItemBuilder {
    private ItemStack is;

    public ItemBuilder(Material m) {
        this(m, 1);
    }

    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    public ItemBuilder(Material m, int amount) {
        is = new ItemStack(m, amount);
    }

    public ItemBuilder clone() {
        return new ItemBuilder(is.clone());
    }

    public ItemBuilder setNBT(String key, Object value) {
        is = PersistentDataUtil.set(is, value, key);
        return this;
    }

    public ItemBuilder setType(Material material) {
        is.setType(material);
        return this;
    }

    public ItemBuilder setMaxStackSize(int size) {
        ItemMeta im = is.getItemMeta();
        im.setMaxStackSize(Math.max(1, Math.min(99, size)));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setDamage(int damage) {
        Damageable im = (Damageable) is.getItemMeta();
        im.setDamage(damage);
        return this;
    }

    public ItemBuilder setItemModel(String itemModel) {
        ItemMeta im = is.getItemMeta();
        String[] split = itemModel.split(":");
        if (split.length != 2) throw new IllegalArgumentException("Item model must be in the format namespace:key");
        NamespacedKey key = new NamespacedKey(split[0], split[1]);
        im.setItemModel(key);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setName(String name) {
        ItemMeta im = is.getItemMeta();
        im.displayName(ChatUtils.format(name).decoration(TextDecoration.ITALIC, false));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setName(Component name) {
        ItemMeta im = is.getItemMeta();
        im.displayName(name.decoration(TextDecoration.ITALIC, false));
        is.setItemMeta(im);
        return this;
    }

    @Deprecated
    public ItemBuilder setGlowing(boolean toggle) {
        if (toggle) {
            addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
            setItemFlag(ItemFlag.HIDE_ENCHANTS);
            return this;
        }
        ItemMeta im = is.getItemMeta();
        removeEnchantment(Enchantment.LUCK_OF_THE_SEA);
        im.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        is.addUnsafeEnchantment(ench, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment ench) {
        is.removeEnchantment(ench);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment ench, int level) {
        ItemMeta im = is.getItemMeta();
        im.addEnchant(ench, level, true);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        is.addUnsafeEnchantments(enchantments);
        return this;
    }

    public ItemBuilder setGlintOverride(boolean override) {
        ItemMeta im = is.getItemMeta();
        im.setEnchantmentGlintOverride(override);
        return this;
    }

    public ItemBuilder removeGlintOverride() {
        ItemMeta im = is.getItemMeta();
        im.setEnchantmentGlintOverride(null);
        return this;
    }

    public ItemBuilder setInfinityDurability() {
        is.setDurability(Short.MAX_VALUE);
        return this;
    }

    public ItemBuilder setLore(Component... lore) {
        ItemMeta im = is.getItemMeta();
        im.lore(Arrays.asList(lore));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(List<Component> lore) {
        ItemMeta im = is.getItemMeta();
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        ItemMeta im = is.getItemMeta();
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder removeLoreLine(Component line) {
        ItemMeta im = is.getItemMeta();
        List<Component> lore = new ArrayList<>(im.lore());
        if (!lore.contains(line))
            return this;
        lore.remove(line);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        ItemMeta im = is.getItemMeta();
        List<Component> lore = new ArrayList<>(im.lore());
        if (index < 0 || index > lore.size())
            return this;
        lore.remove(index);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        this.addLoreLine(ChatUtils.format(line).decoration(TextDecoration.ITALIC, false));
        return this;
    }

    public ItemBuilder addLoreLine(Component line) {
        ItemMeta im = is.getItemMeta();
        List<Component> lore = new ArrayList<>(Optional.ofNullable(im.lore()).orElse(Collections.emptyList()));
        lore.add(line);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLeatherArmorColor(Color color) {
        try {
            LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
            im.setColor(color);
            is.setItemMeta(im);
        } catch (ClassCastException ignored) {
        }
        return this;
    }

    public ItemBuilder setSkullOwner(Player owner) {
        SkullMeta im = (SkullMeta) is.getItemMeta();
        im.setOwningPlayer(owner);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setSkullOwner(OfflinePlayer owner) {
        SkullMeta im = (SkullMeta) is.getItemMeta();
        im.setOwningPlayer(owner);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        SkullMeta im = (SkullMeta) is.getItemMeta();
        im.setOwner(owner);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setItemFlag(ItemFlag itemFlag) {
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(itemFlag);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setItemFlag(ItemFlag[] itemFlag) {
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(itemFlag);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setCustomModelData(Integer data) {
        ItemMeta im = is.getItemMeta();
        im.setCustomModelData(data);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setEquippableSlot(EquipmentSlot slot) {
        ItemMeta im = is.getItemMeta();
        EquippableComponent equippable = im.getEquippable();
        if (equippable == null) return this;
        equippable.setSlot(slot);
        im.setEquippable(equippable);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setEquippableModel(NamespacedKey model) {
        ItemMeta im = is.getItemMeta();
        EquippableComponent equippable = im.getEquippable();
        if (equippable == null) return this;
        equippable.setModel(model);
        im.setEquippable(equippable);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setSkinURL(String url) {
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "skull");
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(URI.create(url).toURL());
            profile.setTextures(textures);

            SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
            skullMeta.setPlayerProfile(profile);
            is.setItemMeta(skullMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    public ItemStack toItemStack() {
        return is;
    }
}
