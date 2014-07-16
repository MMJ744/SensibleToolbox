package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.api.gui.AccessControlGadget;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.gui.NumericGadget;
import me.desht.sensibletoolbox.api.gui.RedstoneBehaviourGadget;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class BlockUpdateDetector extends BaseSTBBlock {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.PURPLE);
    private long lastPulse;
    private int duration;

    private int quiet;

    public BlockUpdateDetector() {
        quiet = 1;
        duration = 2;
    }

    public BlockUpdateDetector(ConfigurationSection conf) {
        super(conf);
        setDuration(conf.getInt("duration"));
        setQuiet(conf.getInt("quiet"));
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("duration", getDuration());
        conf.set("quiet", getQuiet());
        return conf;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        update(false);
    }

    public int getQuiet() {
        return quiet;
    }

    public void setQuiet(int quiet) {
        this.quiet = quiet;
        update(false);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Block Update Detector";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Emits a redstone pulse when",
                " an adjacent block updates",
                "R-click block: " + ChatColor.RESET + "configure BUD"
        };
    }

    @Override
    public String[] getExtraLore() {
        return new String[]{
                "Pulse duration: " + ChatColor.GOLD + getDuration() + " ticks",
                "Sleep time after pulse: " + ChatColor.GOLD + getQuiet() + " ticks",
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe res = new ShapedRecipe(toItemStack());
        res.shape("SRS", "SPS", "STS");
        res.setIngredient('S', Material.STONE);
        res.setIngredient('P', Material.PISTON_STICKY_BASE);
        res.setIngredient('R', Material.REDSTONE);
        res.setIngredient('T', Material.REDSTONE_TORCH_ON);
        return res;
    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        final Block b = event.getBlock();
        long timeNow = getLocation().getWorld().getFullTime();
        Debugger.getInstance().debug(this + ": BUD physics: time=" + timeNow + ", lastPulse=" + lastPulse + ", duration=" + getDuration());
        if (timeNow - lastPulse > getDuration() + getQuiet() && isRedstoneActive()) {
            // emit a signal for one or more ticks
            lastPulse = timeNow;
            final BlockState state = b.getState();
            b.setType(Material.REDSTONE_BLOCK);
            Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    state.update(true, false);
                }
            }, duration);
        }
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            getGUI().show(event.getPlayer());
        }
        super.onInteractBlock(event);
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = new InventoryGUI(this, 9, ChatColor.DARK_PURPLE + getItemName());
        gui.addGadget(new NumericGadget(gui, 1, "Pulse Duration", new IntRange(1, Integer.MAX_VALUE), getDuration(), 10, 1, new NumericGadget.NumericListener() {
            @Override
            public boolean run(int newValue) {
                setDuration(newValue);
                return true;
            }
        }));
        gui.addGadget(new NumericGadget(gui, 0, "Sleep Time after Pulse", new IntRange(0, Integer.MAX_VALUE), getQuiet(), 10, 1, new NumericGadget.NumericListener() {
            @Override
            public boolean run(int newValue) {
                setQuiet(newValue);
                return true;
            }
        }));
        gui.addGadget(new RedstoneBehaviourGadget(gui, 8));
        gui.addGadget(new AccessControlGadget(gui, 7));
        return gui;
    }

    @Override
    public boolean supportsRedstoneBehaviour(RedstoneBehaviour behaviour) {
        return behaviour != RedstoneBehaviour.PULSED;
    }
}
