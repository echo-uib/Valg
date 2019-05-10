// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/Table.java,v 1.7 2002/07/09 10:09:42 royward0 Exp $
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
import java.util.*;

public class Table {
	
	static final int RANK_ORDERING = 1;
	static final int CAND_ORDERING = 2;

    public int[][] ranks;
    String[] mCandidates;
    int theType;
    Vector voteIDs;
	//int[] candkey;
	public int numvoters, numcandidates;
	Status mSys = null;
	
	public Table(LineNumberReader input, Status sys) throws IOException
	{
        mSys = sys;
		// first line is tabletype
        String theLine = input.readLine().trim().substring(0,4).toLowerCase();
        if(theLine.compareTo("rank") == 0) {
            theType = RANK_ORDERING;
        } else if(theLine.compareTo("cand") == 0) {
            theType = CAND_ORDERING;
        }
        else {
            mSys.error("Candidate ordering or Rank ordering expected as first line of input file");
        }
        
        // use second line for getting names calculating # candidates
        theLine = input.readLine().trim();
        StringTokenizer st = new StringTokenizer(theLine,",");
        if(!st.hasMoreTokens())
            mSys.error("Candidate name information expected on second line of input file");
        // throw away the first element
        st.nextToken();
        numcandidates = 0;
        Vector tempCandidates = new Vector();
        while (st.hasMoreTokens()) {
            tempCandidates.addElement(st.nextToken().trim());
            numcandidates++;
        }
        mCandidates = new String[numcandidates];
        for(int i=0; i<numcandidates; i++) {
            mCandidates[i] = (String)tempCandidates.elementAt(i);
        }
        mSys.printlnLog("Number of candidates: "+numcandidates);
       
        // if rank ordering, next line has labels
        Hashtable candLabels = new Hashtable();
        if(RANK_ORDERING == theType) {
            theLine = input.readLine().trim();
            st = new StringTokenizer(theLine,",");
            if(!st.hasMoreTokens())
                mSys.error("Candidate label information expected on third line of input file");
            // throw away the first element
            st.nextToken();
            int numLabels = 0;
            while (st.hasMoreTokens()) {
                String key = st.nextToken().trim();
                if(candLabels.containsKey(key))
                    mSys.error("Repeated candidate label: " + key);
                candLabels.put(key,new Integer(numLabels));
                numLabels++;
            }
            if(numcandidates != numLabels)
                mSys.error("Number of labels must match number of candidates");
        }
        
        // read in all the voter info
        Vector tempRanks = new Vector();
        voteIDs = new Vector();
        numvoters=0;
        //candkey = new int[numcandidates];
        //for(int i=0; i<numcandidates; i++)
        //	candkey[i] = i+1;
        while(null != theLine)
        {
            theLine = input.readLine();
            if((null != theLine) && (0 != theLine.length())) {
                theLine = theLine.trim();
                StringTokenizer stoken = new StringTokenizer(theLine,",",true);
                String voteid = stoken.nextToken().trim();
                mSys.printDebug(voteid);
                for(int v=0; v<voteIDs.size(); v++) {
                    if(voteid.compareTo((String)voteIDs.elementAt(v)) == 0)
                        mSys.error("Voter '" + voteid + "' voted more than once!");
                }
                voteIDs.addElement(voteid);
                int j=0;
                int[] temp = new int[numcandidates];
				
				// to catch nulls, check for repeated commas. We have a flag for that.
				boolean lastComma = false;

                while(stoken.hasMoreTokens()) {
                    String token = stoken.nextToken().trim();
                    if(token.equals(",")) {
						if(lastComma) {
							// we have two commas in a row
							temp[j++] = -1;
							mSys.printDebug(" " + (temp[j-1]+1));
						}
						lastComma=true;
					} else {
						lastComma=false;
						if((CAND_ORDERING == theType) || (token.trim().equals("0"))) {
							temp[j++] = new Integer(token).intValue();
							if(temp[j-1]==0) {
								temp[j-1]=-1;
							}
							mSys.printDebug(" " + (temp[j-1]+1));
						} else {
							Object index = candLabels.get(token);
							if(null == index) {
								mSys.error("For voter '" + voteid + "', label " + token + " not found");
							}
							temp[j++] = ((Integer)index).intValue();
							mSys.printDebug(" " + (temp[j-1]+1));
						}
					}
                }
                for(int k=j; k<numcandidates; k++) {
                    temp[k] = -1;
                }
                mSys.printlnDebug("");
                tempRanks.addElement(temp);
                numvoters++;
            }
        }
        mSys.printlnLog("Number of voters: "+numvoters);
        ranks = new int[numvoters][numcandidates];
        for(int i=0; i<numvoters; i++) {
            for(int j=0; j<numcandidates; j++) {
                ranks[i][j] = ((int[])tempRanks.elementAt(i))[j];
                }
        }
        if(RANK_ORDERING == theType) {
            // should subone be removed from the codebase altogether?
            //subOne();
        } else if(CAND_ORDERING == theType) {
            candOrderToRankOrder();
            theType = RANK_ORDERING;
        } else
            mSys.error("Candidate ordering or Rank ordering expected");
		validateRankOrder();
	}

    public void validateRankOrder() {
		int invalid=0;
		boolean[] valid = new boolean[numvoters];
        int temp[] = new int[numcandidates+1];
        for(int i=0; i<numvoters; i++) {
            for(int j=0; j<numcandidates+1; j++) {
                temp[j] = -1;
            }
			int firstfail=numcandidates;
			boolean reportonce=false;
			for(int k=0; k<numcandidates; k++) {
				if(ranks[i][k] == -1) {
					if(firstfail==numcandidates)firstfail=k;
				} else if(firstfail<numcandidates) {
					if(!reportonce)
						mSys.warning(mSys.mStrictLegal, "Voter '" + voteIDs.elementAt(i) + "' has spurious ranks starting at " + (k+1));
					reportonce=true;
					ranks[i][k] = -1;
				} else if(temp[ranks[i][k]] != -1) {
					if(!reportonce)
						mSys.warning(mSys.mStrictLegal, "Voter '" + voteIDs.elementAt(i) + "' has voted for candidate " + (ranks[i][k]+1) + " twice");
					reportonce=true;
					if(firstfail==numcandidates)firstfail=k;
					ranks[i][k] = -1;
				}
			}
			valid[i] = true;
			if(firstfail<mSys.mRankMin) {
				// we have an invalid vote
				valid[i] = false;
				invalid++;
				mSys.warning(mSys.mStrictRankMin, "Voter '" + voteIDs.elementAt(i) + "' only ranked " + firstfail + " candidates when they were expected to rank " + mSys.mRankMin);
			}
		}
		
		// go through and remove all the invalid votes
		int to=0;
		int[][] ranks2 = new int[numvoters-invalid][numcandidates];
		for(int from=0; from<numvoters; from++) {
			if(valid[from]) {
				for(int j=0; j<numcandidates; j++) {
					ranks2[to][j] = ranks[from][j];
				}
				to++;
			} else {
				voteIDs.remove(to);
			}
		}
		numvoters=to;
		ranks=ranks2;
	}

    public void candOrderToRankOrder() {
        int temp[] = new int[numcandidates];
        for(int i=0; i<numvoters; i++) {
            for(int j=0; j<numcandidates; j++) {
                temp[j] = -1;
            }
            int lastr = numcandidates;
            for(int r=0; r<numcandidates; r++) {
                boolean found = false;
                boolean duplicate = false;
                for(int k=0; k<numcandidates; k++) {
                    if(ranks[i][k] == r+1) {
                        if(found) {
							duplicate = true;
							mSys.warning(mSys.mStrictLegal, "Voter '" + voteIDs.elementAt(i) + "' used the rank " + (r+1) + " twice");
                        }
						found = true;
                        ranks[i][k] = 0;
                        temp[r] = k;
                    }
                }
                if(duplicate || !found) {
                    // gap or duplicate so terminate
                    lastr = r;
					// we might have an invalid vote, but check that in the validation
                    r = numcandidates;
				}
            }
			for(int k=0; k<numcandidates; k++) {
				if(ranks[i][k]>=1) {
					mSys.warning(mSys.mStrictLegal, "Voter '" + voteIDs.elementAt(i) + "' had ranks after the last valid rank of " + lastr);
				}
			}
            //System.out.println("Voter " + i + " ranked " + lastr);
            for(int j=0; j<numcandidates; j++) {
                 ranks[i][j] = temp[j];
                 //System.out.print(" " + temp[j]);
            }
            //System.out.println("");
        }
    }
    
    String getCandidate(int candidate) {
        return mCandidates[candidate];
    }
    
    public void subOne() {
        for(int i=0; i<numvoters; i++) {
            for(int j=0; j<numcandidates; j++) {
                 ranks[i][j]-=1;
            }
        }
    }
    
	public void remove(int candidate) {
		for(int v=0; v<numvoters; v++)
		{
			int rank = ranks[v][candidate];
			for(int i=candidate; i<numcandidates-1; i++)
				ranks[v][i] = ranks[v][i+1];
			for(int j=0; j<numcandidates-1; j++)
			{
				if(ranks[v][j] > rank)
					ranks[v][j]--;
			}		
		}
		//for(int k=candidate; k<numcandidates-1; k++)
		//	candkey[k] = candkey[k+1];		
	}
}
