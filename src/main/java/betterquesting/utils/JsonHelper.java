package betterquesting.utils;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Used to read JSON data with pre-made checks for null entries and casting.</br>
 * In the event the requested value is missing, it will be added to the JSON object
 */
public class JsonHelper
{
	public static JsonArray GetArray(JsonObject json, String id)
	{
		if(json == null)
		{
			return new JsonArray();
		}
		
		if(json.has(id) && json.get(id).isJsonArray())
		{
			return json.get(id).getAsJsonArray();
		} else
		{
			JsonArray array = new JsonArray();
			json.add(id, array);
			return array;
		}
	}
	
	public static JsonObject GetObject(JsonObject json, String id)
	{
		if(json == null)
		{
			return new JsonObject();
		}
		
		if(json.has(id) && json.get(id).isJsonObject())
		{
			return json.get(id).getAsJsonObject();
		} else
		{
			JsonObject obj = new JsonObject();
			json.add(id, obj);
			return obj;
		}
	}
	
	public static String GetString(JsonObject json, String id, String def)
	{
		if(json == null)
		{
			return def;
		}
		
		if(json.has(id) && json.get(id).isJsonPrimitive() && json.get(id).getAsJsonPrimitive().isString())
		{
			return json.get(id).getAsString();
		} else
		{
			JsonPrimitive prim = new JsonPrimitive(def);
			json.add(id, prim);
			return def;
		}
	}
	
	public static Number GetNumber(JsonObject json, String id, Number def)
	{
		if(json == null)
		{
			return def;
		}
		
		if(json.has(id) && json.get(id).isJsonPrimitive() && json.get(id).getAsJsonPrimitive().isNumber())
		{
			return json.get(id).getAsInt();
		} else
		{
			JsonPrimitive prim = new JsonPrimitive(def);
			json.add(id, prim);
			return def;
		}
	}
	
	public static boolean GetBoolean(JsonObject json, String id, boolean def)
	{
		if(json == null)
		{
			return def;
		}
		
		if(json.has(id) && json.get(id).isJsonPrimitive())
		{
			try // Booleans can be stored as strings so there is no quick way of determining whether it is valid or not
			{
				return json.get(id).getAsBoolean();
			} catch(Exception e)
			{
				JsonPrimitive prim = new JsonPrimitive(def);
				json.add(id, prim);
				return def;
			}
		} else
		{
			JsonPrimitive prim = new JsonPrimitive(def);
			json.add(id, prim);
			return def;
		}
	}
	
	/**
	 * Converts a JsonObject to an ItemStack. May return a placeholder if the correct mods are not installed</br>
	 * This should be the standard way to load items into quests in order to retain all potential data
	 */
	public static BigItemStack JsonToItemStack(JsonObject json)
	{
		if(json == null || !json.has("id") || !json.get("id").isJsonPrimitive())
		{
			return new BigItemStack(BetterQuesting.placeholder);
		}
		
		JsonPrimitive jID = json.get("id").getAsJsonPrimitive();
		int count = JsonHelper.GetNumber(json, "Count", 1).intValue();
		int damage = JsonHelper.GetNumber(json, "Damage", 0).intValue();
		
		Item item;
		
		if(jID.isNumber())
		{
			item = (Item)Item.itemRegistry.getObjectById(jID.getAsInt()); // Old format (numbers)
		} else
		{
			item = (Item)Item.itemRegistry.getObject(jID.getAsString()); // New format (names)
		}
		
		NBTTagCompound tags = null;
		if(json.has("tag"))
		{
			tags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "tag"), new NBTTagCompound());
		}
		
		if(item == null)
		{
			BetterQuesting.logger.log(Level.WARN, "Unable to locate item " + jID.toString() + ". This has been converter to a placeholder to prevent data loss");
			BigItemStack stack = new BigItemStack(BetterQuesting.placeholder, count, damage);
			stack.SetTagCompound(new NBTTagCompound());
			stack.GetTagCompound().setString("orig_id", jID.getAsString());
			if(tags != null)
			{
				stack.GetTagCompound().setTag("orig_tag", tags);
			}
			return stack;
		} else if(item == BetterQuesting.placeholder)
		{
			if(tags != null)
			{
				Item restored = (Item)Item.itemRegistry.getObject(tags.getString("orig_id"));
				
				if(restored != null)
				{
					BigItemStack stack = new BigItemStack(restored, count, damage);
					
					if(tags.hasKey("orig_tag"))
					{
						stack.SetTagCompound(tags.getCompoundTag("orig_tag"));
					}
					
					return stack;
				}
			}
		}
		
		BigItemStack stack = new BigItemStack(item, count, damage);
		
		if(tags != null)
		{
			stack.SetTagCompound(tags);
		}
		
		return stack;
	}
	
	/**
	 * Use this for quests instead of converter NBT because this doesn't use ID numbers
	 */
	public static JsonObject ItemStackToJson(BigItemStack stack, JsonObject json)
	{
		json.addProperty("id", Item.itemRegistry.getNameForObject(stack.getBaseStack().getItem()));
		json.addProperty("Count", stack.stackSize);
		json.addProperty("Damage", stack.getBaseStack().getItemDamage());
		if(stack.HasTagCompound())
		{
			json.add("tag", NBTConverter.NBTtoJSON_Compound(stack.GetTagCompound(), new JsonObject()));
		}
		return json;
	}
	
	public static FluidStack JsonToFluidStack(JsonObject json)
	{
		String name = GetString(json, "FluidName", "water");
		int amount = GetNumber(json, "Amount", 1000).intValue();
		NBTTagCompound tags = null;
		
		if(json.has("Tag"))
		{
			tags = NBTConverter.JSONtoNBT_Object(GetObject(json, "Tag"), new NBTTagCompound());
		}
		
		Fluid fluid = FluidRegistry.getFluid(name);
		
		if(fluid == null)
		{
			FluidStack stack = new FluidStack(BetterQuesting.fluidPlaceholder, amount);
			NBTTagCompound orig = new NBTTagCompound();
			orig.setString("orig_id", name);
			if(tags != null)
			{
				orig.setTag("orig_tag", tags);
			}
			stack.tag = orig;
			return stack;
		} else if(fluid == BetterQuesting.fluidPlaceholder && tags != null)
		{
			Fluid restored = FluidRegistry.getFluid(tags.getString("orig_id"));
			
			if(restored != null)
			{
				FluidStack stack = new FluidStack(restored, amount);
				
				if(tags.hasKey("orig_tag"))
				{
					stack.tag = tags.getCompoundTag("orig_tag");
				}
				
				return stack;
			}
		}
		
		FluidStack stack = new FluidStack(fluid, amount);
		
		if(tags != null)
		{
			stack.tag = tags;
		}
		
		return stack;
	}
	
	public static JsonObject FluidStackToJson(FluidStack stack, JsonObject json)
	{
		json.addProperty("FluidName", FluidRegistry.getFluidName(stack));
		json.addProperty("Amount", stack.amount);
		if(stack.tag != null)
		{
			json.add("Tag", NBTConverter.NBTtoJSON_Compound(stack.tag, new JsonObject()));
		}
		return json;
	}
}