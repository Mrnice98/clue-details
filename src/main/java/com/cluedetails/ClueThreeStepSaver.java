package com.cluedetails;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.config.ConfigManager;
import javax.inject.Inject;

@Slf4j
public class ClueThreeStepSaver {

    @Inject
    private Client client;

    @Inject
    private ClueDetailsConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private Gson gson;

    private ClueInventoryManager cim;

    private ClueInstance activeMaster;

    @Getter
    private ClueInstance savedThreeStepper;

    private boolean removeEntries = false;

    public static final int MASTER_CLUE_ID = 19835;
    private static final String CONFIG_GROUP = "clue-details";
    private static final String THREE_STEP_MASTER_KEY = "three-step-master";

    public void scanInventory()
    {
        if (!config.threeStepperSaver()) return;

        activeMaster = cim.getTrackedClueByClueItemId(MASTER_CLUE_ID);
        if(activeMaster == null || savedThreeStepper == null)
        {
            removeEntries = false;
            return;
        }

        //removes entries if we don't know what clue is in their inv, can be made a toggle.
        removeEntries = activeMaster.equals(savedThreeStepper) || activeMaster.getClueIds().isEmpty();
    }

    public boolean cluesMatch()
    {
        if (activeMaster == null || savedThreeStepper == null) return false;
        else return activeMaster.equals(savedThreeStepper);
    }

    public void onMenuOpened(MenuOpened event)
    {
        if (!config.threeStepperSaver()) return;
        if (activeMaster == null) return;

        MenuEntry firstEntry = event.getFirstEntry();
        //only menus generated from a clue in inventory pass this widget check.
        if (activeMaster.getClueIds().size() == 3 && firstEntry.getWidget() != null && firstEntry.getTarget().contains("Clue scroll (master)"))
        {
            MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
            if (activeMaster.equals(savedThreeStepper))
            {
                client.getMenu().createMenuEntry(-menuEntries.length)
                        .setOption("Unset three-stepper")
                        .setTarget(event.getFirstEntry().getTarget())
                        .setType(MenuAction.RUNELITE)
                        .onClick(e -> removeThreeStepper());
            }
            else
            {
                client.getMenu().createMenuEntry(-menuEntries.length)
                        .setOption("Set three-stepper")
                        .setTarget(event.getFirstEntry().getTarget())
                        .setType(MenuAction.RUNELITE)
                        .onClick(e -> saveThreeStepper());
            }
        }

    }

    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!config.threeStepperSaver()) return;

        MenuEntry menuEntry = event.getMenuEntry();
        if (menuEntry.getTarget().contains("Torn clue scroll") && removeEntries)
        {
            if (menuEntry.getOption().contains("Use") || menuEntry.getOption().contains("Combine"))
            {
                client.getMenu().removeMenuEntry(menuEntry);
            }
        }
    }

    public void saveThreeStepper()
    {
        String clueInstanceJson = gson.toJson(activeMaster);
        configManager.setConfiguration(CONFIG_GROUP, THREE_STEP_MASTER_KEY, clueInstanceJson);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Successfully set clue as your three-stepper.","");
        updateThreeStepper();
        scanInventory();
    }

    public void removeThreeStepper()
    {
        configManager.setConfiguration(CONFIG_GROUP, THREE_STEP_MASTER_KEY, "");
        client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Successfully unset clue as your three-stepper.","");
        updateThreeStepper();
        scanInventory();
    }

    public void updateThreeStepper()
    {
        String threeStepMasterJson = configManager.getConfiguration(CONFIG_GROUP,THREE_STEP_MASTER_KEY);
        if (threeStepMasterJson == null) return;
        savedThreeStepper = gson.fromJson(threeStepMasterJson,ClueInstance.class);
    }

    public void startUp(ClueInventoryManager clueInventoryManager)
    {
        this.cim = clueInventoryManager;
        updateThreeStepper();
        scanInventory();
    }
}
