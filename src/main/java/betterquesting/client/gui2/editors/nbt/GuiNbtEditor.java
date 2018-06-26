package betterquesting.client.gui2.editors.nbt;

import betterquesting.api.misc.ICallback;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class GuiNbtEditor extends GuiScreenCanvas implements IPEventListener
{
    private final NBTBase nbt;
    private final ICallback<NBTTagCompound> comCallback;
    private final ICallback<NBTTagList> lstCallback;
    
    public GuiNbtEditor(GuiScreen parent, NBTTagCompound tag, ICallback<NBTTagCompound> callback)
    {
        super(parent);
        
        this.nbt = tag;
        this.comCallback = callback;
        this.lstCallback = null;
    }
    
    public GuiNbtEditor(GuiScreen parent, NBTTagList tag, ICallback<NBTTagList> callback)
    {
        super(parent);
        
        this.nbt = tag;
        this.comCallback = null;
        this.lstCallback = callback;
    }
    
    public void initPanel()
    {
        super.initPanel();
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));
    
        PanelTextBox txTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate(nbt.getId() == 9 ? "betterquesting.title.json_array" : "betterquesting.title.json_object")).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);
        
        PanelScrollingNBT pnEdit;
        if(nbt.getId() == 10)
        {
            pnEdit = new PanelScrollingNBT(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 24, 32), 0), (NBTTagCompound)nbt, 1, 2, 3, 4);
        } else
        {
            pnEdit = new PanelScrollingNBT(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 24, 32), 0), (NBTTagList)nbt, 1, 2, 3, 4);
        }
        cvBackground.addPanel(pnEdit);
    
        PanelVScrollBar scEdit = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-24, 32, 16, 32), 0));
        cvBackground.addPanel(scEdit);
        pnEdit.setScrollDriverY(scEdit);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
    
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
            
            if(nbt.getId() == 10 && comCallback != null)
            {
                comCallback.setValue((NBTTagCompound)nbt);
            } else if(nbt.getId() == 9 && lstCallback != null)
            {
                lstCallback.setValue((NBTTagList)nbt);
            }
        }
    }
}
