package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Class managing collision rule for players.
 */
public class CollisionManager extends RefreshableFeature implements JoinListener, Loadable, CustomThreaded {

    @NotNull private final NameTag nameTags;
    @NotNull private final Condition enableCollision;

    /**
     * Constructs new instance.
     *
     * @param   nameTags
     *          Parent feature
     */
    public CollisionManager(@NotNull NameTag nameTags) {
        super(nameTags.getFeatureName(), "Updating collision");
        this.nameTags = nameTags;
        enableCollision = Condition.getCondition(nameTags.getConfiguration().enableCollision);
    }

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.COLLISION, 500, p -> {
            TabPlayer player = (TabPlayer) p;
            if (player.teamData.forcedCollision != null) return Boolean.toString(player.teamData.forcedCollision);
            boolean newCollision = !((TabPlayer)p).isDisguised() && enableCollision.isMet((TabPlayer) p);
            player.teamData.collisionRule = newCollision;
            return Boolean.toString(newCollision);
        });
        addUsedPlaceholder(TabConstants.Placeholder.COLLISION);
        for (TabPlayer all : nameTags.getOnlinePlayers().getPlayers()) {
            onJoin(all);
        }
    }
    
    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.teamData.collisionRule = enableCollision.isMet(connectedPlayer);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.teamData.disabled.get()) return;
        nameTags.updateCollision(p, false);
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return nameTags.getCustomThread();
    }
}