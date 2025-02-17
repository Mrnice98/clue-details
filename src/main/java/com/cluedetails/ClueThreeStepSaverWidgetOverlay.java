package com.cluedetails;

import lombok.Setter;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import javax.inject.Inject;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static com.cluedetails.ClueDetailsConfig.SavedThreeStepperEnum.BOTH;
import static com.cluedetails.ClueDetailsConfig.SavedThreeStepperEnum.INVENTORY;
import static com.cluedetails.ClueThreeStepSaver.MASTER_CLUE_ID;

public class ClueThreeStepSaverWidgetOverlay extends WidgetItemOverlay {

    private final ItemManager itemManager;
    private final ClueDetailsConfig config;

    @Setter
    private ClueThreeStepSaver clueThreeStepSaver;

    @Inject
    private ClueThreeStepSaverWidgetOverlay(ItemManager itemManager, ClueThreeStepSaver clueThreeStepSaver, ClueDetailsConfig config)
    {
        this.itemManager = itemManager;
        this.clueThreeStepSaver = clueThreeStepSaver;
        this.config = config;
        showOnInventory();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (itemId != MASTER_CLUE_ID || !clueThreeStepSaver.cluesMatch())
        {
            return;
        }

        Rectangle bounds = widgetItem.getCanvasBounds();
        if (config.threeStepperSaver() && (config.highlightSavedThreeStepper() == BOTH || config.highlightSavedThreeStepper() == INVENTORY))
        {
            final BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), config.invThreeStepperHighlightColor());
            graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
        }
    }
}
