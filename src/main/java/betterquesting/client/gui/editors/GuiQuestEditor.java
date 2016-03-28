package betterquesting.client.gui.editors;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.ITextEditor;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestInstance.QuestLogic;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

@SideOnly(Side.CLIENT)
public class GuiQuestEditor extends GuiQuesting implements ITextEditor, IVolatileScreen
{
	JsonObject lastEdit;
	QuestInstance quest;
	
	GuiTextField titleField;
	GuiBigTextField descField;
	
	public GuiQuestEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, I18n.translateToLocalFormatted("betterquesting.title.edit_quest", I18n.translateToLocalFormatted(quest.name)));
		this.quest = quest;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.title = I18n.translateToLocalFormatted("betterquesting.title.edit_quest", I18n.translateToLocalFormatted(quest.name));
		
		if(lastEdit != null)
		{
			quest.readFromJSON(lastEdit);
			lastEdit = null;
			SendChanges();
		}
		
		titleField = new GuiTextField(0, this.fontRendererObj, width/2 - 99, height/2 - 68 + 1, 198, 18);
		titleField.setMaxStringLength(Integer.MAX_VALUE);
		titleField.setText(quest.name);
		
		descField = new GuiBigTextField(this.fontRendererObj, width/2 - 99, height/2 - 28 + 1, 198, 18).enableBigEdit(this, 0);
		descField.setMaxStringLength(Integer.MAX_VALUE);
		descField.setText(quest.description);
		
		GuiButtonQuesting btn = new GuiButtonQuesting(1, width/2, height/2 + 28, 100, 20, I18n.translateToLocalFormatted("betterquesting.btn.rewards"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(2, width/2 - 100, height/2 + 28, 100, 20, I18n.translateToLocalFormatted("betterquesting.btn.tasks"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(3, width/2 - 100, height/2 + 48, 100, 20, I18n.translateToLocalFormatted("betterquesting.btn.requirements"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(4, width/2 - 100, height/2 + 68, 200, 20, I18n.translateToLocalFormatted("betterquesting.btn.advanced"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(5, width/2 - 100, height/2 + 8, 200, 20, I18n.translateToLocalFormatted("betterquesting.btn.is_main") + ": " + quest.isMain);
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(6, width/2, height/2 + 48, 100, 20, I18n.translateToLocalFormatted("betterquesting.btn.logic") + ": " + quest.logic);
		this.buttonList.add(btn);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			QuestDatabase.updateUI = false;
			lastEdit = null;
			initGui();
		}
		
		titleField.drawTextBox();
		descField.drawTextBox();

		mc.fontRendererObj.drawString(I18n.translateToLocalFormatted("betterquesting.gui.name"), width/2 - 100, height/2 - 80, ThemeRegistry.curTheme().textColor().getRGB(), false);
		mc.fontRendererObj.drawString(I18n.translateToLocalFormatted("betterquesting.gui.description"), width/2 - 100, height/2 - 40, ThemeRegistry.curTheme().textColor().getRGB(), false);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1) // Rewards
		{
			mc.displayGuiScreen(new GuiRewardEditor(this, quest));
		} else if(button.id == 2) // Tasks
		{
			mc.displayGuiScreen(new GuiTaskEditor(this, quest));
		} else if(button.id == 3) // Prerequisites
		{
			mc.displayGuiScreen(new GuiPrerequisiteEditor(this, quest));
		} else if(button.id == 4) // Raw JSON
		{
			this.lastEdit = new JsonObject();
			quest.writeToJSON(lastEdit);
			mc.displayGuiScreen(new GuiJsonObject(this, lastEdit));
		} else if(button.id == 5)
		{
			quest.isMain = !quest.isMain;
			button.displayString = I18n.translateToLocalFormatted("betterquesting.btn.is_main") + ": " + quest.isMain;
			SendChanges();
		} else if(button.id == 6)
		{
			QuestLogic[] logic = QuestLogic.values();
			quest.logic = logic[(quest.logic.ordinal() + 1)%logic.length];
			button.displayString = I18n.translateToLocalFormatted("betterquesting.btn.logic") + ": " + quest.logic;
			SendChanges();
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode) throws IOException
    {
        super.keyTyped(character, keyCode);
        
        titleField.textboxKeyTyped(character, keyCode);
        descField.textboxKeyTyped(character, keyCode);
    }
	
    /**
     * Called when the mouse is clicked.
     * @throws IOException 
     */
	@Override
    protected void mouseClicked(int mx, int my, int click) throws IOException
    {
		super.mouseClicked(mx, my, click);
		
		titleField.mouseClicked(mx, my, click);
		descField.mouseClicked(mx, my, click);
		
		boolean flag = false; // Just in case measure to prevent multiple update calls
		
		if(!titleField.isFocused() && !titleField.getText().equals(quest.name))
		{
			// Apply changes, this way is automatic and doesn't require pressing Enter
			quest.name = titleField.getText();
			flag = true;
		}
		
		if(!descField.isFocused() && !descField.getText().equals(quest.description))
		{
			// Apply changes, this way is automatic and doesn't require pressing Enter
			quest.description = descField.getText();
			flag = true;
		}
		
		if(flag)
		{
			SendChanges();
		}
    }
	
	// If the changes are approved by the server, it will be broadcast to all players including the editor
	public void SendChanges()
	{
		JsonObject json = new JsonObject();
		quest.writeToJSON(json);
		NBTTagCompound tags = new NBTTagCompound();
		//tags.setInteger("ID", 5);
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
		BetterQuesting.instance.network.sendToServer(PacketDataType.QUEST_EDIT.makePacket(tags));
	}

	@Override
	public void setText(int id, String text)
	{
		if(id == 0)
		{
			if(descField != null)
			{
				descField.setText(text);
			}
			
			quest.description = text;
			SendChanges();
		}
	}
}
