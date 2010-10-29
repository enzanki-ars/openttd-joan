/* $Id$ */
package org.openttd;

import org.openttd.enums.Colour;
import org.openttd.pool.Poolable;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Company representation of the companies in OpenTTD
 * @author Nathanael Rebsch
 */
public class Company extends Poolable<Integer>
{

    public static final int INVALID_COMPANY   = 255;
    public static final int COMPANY_SPECTATOR = 255;
    public static final int MAX_COMPANIES     = 16;

    public String     name;
    public String     president;
    public long       inaugurated;
    public BigInteger value;
    public BigInteger income;
    public int        performance;
    public boolean    passworded  = false;
    public boolean    ai          = false;
    public int        vehicles[];
    public int        stations[];
    public Colour     colour;

    public int        shares[] = new int[4];

    public int        bankruptcy = 0;

    public Economy             current_economy = new Economy();
    public LinkedList<Economy> history_economy = new LinkedList<Economy>();

    public Company (int company_id)
    {
        this.id = company_id;

        if (this.id == COMPANY_SPECTATOR){
            this.name = "Spectator";
        }
    }

    /**
     * Check if the Company is Spectator
     * @return
     */
    public boolean isSpectator()
    {
        return isSpectator(this.id);
    }

    /**
     * Check if the Company is Spectator
     * static representation of isSpectator()
     * @param index
     * @return
     */
    public static boolean isSpectator(int index)
    {
        return (index == COMPANY_SPECTATOR);
    }

    /**
     * Is the given index a valid Company ID
     * @param index Company ID to check.
     * @return true if the Company ID is valid.
     */
    public static boolean IsValid(int index)
    {
        return (index < MAX_COMPANIES);
    }

    /**
     * Check if an index is either valid or the ID of the spectator.
     * @param index Company ID to check
     * @return true if the index is either valid or spectator.
     */
    public static boolean IsValidOrSpectator(int index)
    {
        return (IsValid(index) || isSpectator(index));
    }
}
