// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/Results.java,v 1.4 2002/07/05 14:59:43 royward0 Exp $
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

import java.io.*;

public class Results {
    int[] candList;
    String[] candStrings;
    int numCand;
    
    Results(int number) {
        numCand = number;
        candList = new int[numCand];
        candStrings = new String[numCand];
        for(int i=0; i< numCand; i++) {
            candList[i] = -1;
        }
    }
    
    public void print(Status sys) {
        sys.printlnOut("Results: (Rank,Candidate)");
	    for(int i=0; i<numCand; i++) {
            if(candList[i] != -1)
                sys.printlnOut(Util.toStringIntPadded(i+1,5) + " " + candStrings[i]);
        }
    }
}
