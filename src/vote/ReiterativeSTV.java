// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/ReiterativeSTV.java,v 1.4 2002/07/05 14:59:43 royward0 Exp $
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

public class ReiterativeSTV extends Results {
    
    Table table;
    
    public ReiterativeSTV(Table input) {
        super(input.numcandidates);
        table = input;
    }
    
    public void vote(Status sys) {
        for(int rank=numCand-1;rank>=0;rank--) {
            STV stv = new STV(table, sys);
            for(int i=0; i< numCand; i++) {
                if(candList[i] != -1)
                    stv.eliminateOne(candList[i]);
            }
            stv.vote(rank);
            for(int i=0; i< numCand; i++) {
                if(stv.candStatus[i]!=stv.ELECTED) {
                    boolean alreadyRanked = false;
                    for(int j=0; j< numCand; j++) {
                        if(candList[j] == i)
                            alreadyRanked = true;
                    }
                    if(!alreadyRanked) {
                        sys.printlnLog("$Eliminated " + table.getCandidate(i));
                        candList[rank] = i;
                        candStrings[rank] = table.getCandidate(i);
                    }
                }
            }
        }
    }
}
