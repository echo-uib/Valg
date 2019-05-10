// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/Util.java,v 1.3 2002/07/05 14:59:43 royward0 Exp $
//
// Voting Systems Toolbox is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 2 of the License.
//
// Voting Systems Toolbox is distributed in the hope that it will be useful,
// but WITHOUT ANY/ WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.
//
// You should have received a copy of the GNU General Public License along with
// Voting Systems Toolbox; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307.

package vote;

import java.math.BigDecimal;

class Util {

    static final BigDecimal one = BigDecimal.valueOf(1);

    public static String toStringIntPadded(int i, int width) {
        String s= "" + i;
        while(s.length() < width)
            s = " " + s;
        return s;
    }

     public static String toStringBigDecPadded(BigDecimal x, int width, int scale) {
        String s= "" + forceScale(x, scale);
        while(s.length() < width)
            s = " " + s;
        return s;
    }

   public static String toStringPadded(String s, int width) {
        while(s.length() < width)
            s += "                                        ";
        return s.substring(0,width);
    }
    
    public static BigDecimal forceScale(BigDecimal x, int scale) {
        return x.divide(one,scale,BigDecimal.ROUND_UP);
    }
}
