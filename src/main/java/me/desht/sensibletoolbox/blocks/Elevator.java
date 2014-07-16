package me.desht.sensibletoolbox.blocks;

import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

public class Elevator extends BaseSTBBlock implements Colorable {
    private DyeColor color;

    public Elevator() {
        color = DyeColor.WHITE;
    }

    public Elevator(ConfigurationSection conf) {
        super(conf);
        color = DyeColor.valueOf(conf.getString("color"));
    }

    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("color", color.toString());
        return conf;
    }

    public DyeColor getColor() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
        update(true);
    }

    @Override
    public MaterialData getMaterialData() {
        return STBUtil.makeColouredMaterial(Material.STAINED_CLAY, color);
    }

    @Override
    public String getItemName() {
        return "Elevator";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Links to other elevators",
                " directly above or below",
                "Press Space to go up",
                "Press Shift to go down"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("WWW", "WPW", "WWW");
        recipe.setIngredient('W', Material.WOOL);
        recipe.setIngredient('P', Material.ENDER_PEARL);
        return recipe;
    }

    public Elevator findOtherElevator(BlockFace direction) {
        Validate.isTrue(direction == BlockFace.UP || direction == BlockFace.DOWN, "direction must be UP or DOWN");

        Block b = getLocation().getBlock();
        Elevator res = null;
        while (b.getY() > 0 && b.getY() < b.getWorld().getMaxHeight()) {
            b = b.getRelative(direction);


            if (b.getType().isSolid()) {
                res = LocationManager.getManager().get(b.getLocation(), Elevator.class);
                break;
            }
        }
        return (res != null && res.getColor() == getColor()) ? res : null;
    }
}
