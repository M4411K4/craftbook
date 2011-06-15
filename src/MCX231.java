

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Takes in a clock input, and outputs whether the time is day or night.
 *
 * @author Shaun (sturmeh)
 */
public class MCX231 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "IS IT A STORM";
    }
    
    public String validateEnvironment(int worldType, Vector pos, SignText sign) {
        if (sign.getLine3().length() != 0) {
        	return "Third line needs to be blank";
        }

        if (sign.getLine4().length() != 0) {
            return "Fourth line needs to be blank";
        }

        return null;
    }

    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip)
    {
    	World world = CraftBook.getWorld(chip.getWorldType());
    	chip.getOut(1).set(world.isThundering());
    }
}
