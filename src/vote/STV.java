// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/STV.java,v 1.7 2002/07/05 14:59:43 royward0 Exp $
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

public class STV {

    static final int ELECTED = 1;
    static final int HOPEFUL = 2;
    static final int ELIMINATED = 4;
    static final int ALMOST = 8;
    static final BigDecimal billionth = new BigDecimal("0.000000001");
    static final BigDecimal zero = BigDecimal.valueOf(0);
    static final BigDecimal one = BigDecimal.valueOf(1);
    static final BigDecimal quotaErrorMargin = new BigDecimal("0.00001");
    static final BigDecimal reallyBig = new BigDecimal("1000000000000000000");
    static final int DP = 9;
    static final int NUMWIDTH = 15;
    static final int ROUND_UP = BigDecimal.ROUND_UP;
    static final int ROUND_DOWN = BigDecimal.ROUND_DOWN;

    Table table;
    int[] candStatus;
    int[] candRand;
    int[] ahead;
    BigDecimal[] candWeight;
    BigDecimal[] candVote;
    Status sys;
    BigDecimal excess;
    BigDecimal quota;
    BigDecimal surplus;
    RandomAS183 rand;
    boolean changed;
    
    // local copies of variables from the table
    int numCandidates, numVoters;
    

    public STV(Table input, Status psys) {
        sys = psys;
        table = input;
        numCandidates = table.numcandidates;
        numVoters = table.numvoters;
        candStatus = new int[numCandidates];
        candRand = new int[numCandidates];
        ahead = new int[numCandidates];
        for(int i=0; i<numCandidates; i++)
            ahead[i] = numCandidates-1;
        candWeight = new BigDecimal[numCandidates];
        candVote = new BigDecimal[numCandidates];
        for(int i=0; i<numCandidates; i++) {
            candStatus[i] = HOPEFUL;
            candWeight[i] = one;
        }
        surplus = one;
    }
    
    public void print() {
    	for(int i=0; i<numCandidates; i++) {
            sys.printlnOut(Util.toStringPadded(table.getCandidate(i),40) + " "
                + ((candStatus[i]==ELECTED)?"elected":"not elected"));
        }
    }

    public void vote(int seats) {
        rand = new RandomAS183(numCandidates+5, seats, (numVoters+1000*(numVoters%10)) % 30323);
        rand.random();
        rand.random();
        rand.random();
        rand.random();
        for(int i=0; i<numCandidates; i++) {
            boolean repeated = true;
            int n = 0;
                while(repeated) {
                repeated = false;
                n = rand.random();
                for(int j=0; ((j<i) && !repeated) ; j++) {
                    if(n == candRand[i]) {
                        sys.printlnDebug("Repeated random number");
                        repeated = true;
                    }
                }
            }
            candRand[i] = n;
        }
        
        if(count(ELECTED+HOPEFUL) <= seats) {
            sys.printlnDebug("All candidates elected");
            changeState(HOPEFUL,ELECTED);
            return;
        }
        if(seats <= 0) {
            changeState(HOPEFUL,ELIMINATED);
            return;
        }
        changed = false;
        int elected = 0;
        int iteration = 1;
        while(count(ELECTED) < seats) {
            sys.printlnDebug("Iteration " + iteration);
            iteration++;
            if(iteration == 1)
                reCalc(seats);
            else {
                iterateOne(seats);
            }
            reverseRandom();
        }
        sys.printlnDebug("All seats full");
        changeState(HOPEFUL,ELIMINATED);
    }
    
    void iterateOne(int seats) {
        // assume that: (count(ELECTED) < seats)
        if(count(ELECTED+HOPEFUL) <= seats) {
            sys.printlnDebug("All remaining candidates elected");
            changeState(HOPEFUL,ELECTED);
            return;
        }
        
        changed = false;
        convergeOne(seats);
        if((!changed) && surplus.compareTo(quotaErrorMargin)<0) {
            sys.printlnDebug("Remove Lowest (forced)");
            excludeLowest();
        }
    }
    
    void reCalc(int seats) {
        calcTotals();
        calcAheads();
        if(sys.debug())displayTables();
        calcQuota(seats);
        elect(seats);
        calcSurplus();
        if(tryRemoveLowest(seats)) {
            return;
        }
    }

    void calcTotals() {
        for(int i=0; i<numCandidates; i++) {
            candVote[i] = zero;
        }
        excess = zero;
        for(int i=0; i<numVoters; i++) {
            BigDecimal vote = one;
            for(int rank=0; (rank<numCandidates)
                            && (table.ranks[i][rank] >= 0)
                            && (vote.compareTo(zero)>0); rank++) {
                int c = table.ranks[i][rank];
                // we don't do the next operations of the candidate is eliminated
                // there is also a shortened form if the candidate is hopeful
                // this makes no logical difference, but avoids slow BigDecimal operations
                // This is the slowest part of the whole program, so it is worth taking some pains
                if(HOPEFUL == candStatus[c]) {
                    candVote[c] = candVote[c].add(vote);
                    vote = zero;
                    rank = numCandidates; // done, so bail out of next iteration of loop
                } else if(ELIMINATED != candStatus[c])
                {
                    BigDecimal w = candWeight[c];
                    BigDecimal wv = w.multiply(vote).setScale(DP,ROUND_UP);
                    candVote[c] = candVote[c].add(wv);
                    vote = vote.subtract(wv);
                }
            }
            excess = excess.add(vote);
        }
    }

    void calcAheads() {
        
        class SortAtom {
            int index;
            int ahead;
            BigDecimal vote;

            SortAtom(int i, int a, BigDecimal v) {
                index = i;
                ahead = a;
                vote = v;
            }
            
            int compareTo(SortAtom b) {
                if(ahead < b.ahead)
                    return -1;
                else if(ahead == b.ahead)
                    return vote.compareTo(b.vote);
                else
                    return 1;
            }
        }

        // Do insertion sort here - horribly inefficient, but this is not time critical
        // Anyone suggest a nice way of deaing with sorting that still works with JDK 1.1?
        SortAtom[] sort = new SortAtom[numCandidates];
        for(int i=0; i<numCandidates; i++) {
            SortAtom thisatom = new SortAtom(i,ahead[i],candVote[i]);
            // find the place to insert
            int place=0;
            while((place<i) && (thisatom.compareTo(sort[place])>0))
                place++;
            
            // make sure that space is clear
            for(int j=i-1; j>=place; j--)
                sort[j+1] = sort[j];
            sort[place] = thisatom;
        }
        
        // now recalculate the ahead values
        if(numCandidates>0)
        {
            int lastplace = 0;
            SortAtom lastvalue = sort[0];
            for(int i=0; i<=numCandidates; i++) {
                if((i == numCandidates) || (sort[i].compareTo(lastvalue)!=0)) {
                    for(int j=lastplace; j<i; j++)
                        ahead[sort[j].index] = (i-1)+lastplace;
                    if(i < numCandidates) {
                        lastplace = i;
                        lastvalue = sort[i];
                    }
                }
            }
        }
    }
    
    void displayTables() {
        BigDecimal total = excess;
        for(int i=0; i<numCandidates; i++) {
            sys.printlnDebug(
                Util.toStringPadded(table.getCandidate(i),20)
                + " " + Util.toStringBigDecPadded(candWeight[i],NUMWIDTH,DP)
                + " " + Util.toStringBigDecPadded(candVote[i],NUMWIDTH,DP)
                //+ " " + Util.toStringIntPadded(ahead[i],5)
                //+ " " + Util.toStringIntPadded(candRand[i],5)
                );
            total = total.add(candVote[i]);
        }
        sys.printlnDebug("Non-transferable                     "
            + Util.toStringBigDecPadded(excess,NUMWIDTH,DP));
        sys.printlnDebug("Total                                "
            + Util.toStringBigDecPadded(total,NUMWIDTH,DP));
    }

    void calcQuota(int seats) {
        if(seats>2)
            quota = (BigDecimal.valueOf(numVoters).subtract(excess))
                    .divide(BigDecimal.valueOf(seats+1),DP,ROUND_DOWN).add(billionth);
        else
            quota = (BigDecimal.valueOf(numVoters).subtract(excess))
                    .divide(BigDecimal.valueOf(seats+1),DP,ROUND_UP);
        if(quota.compareTo(quotaErrorMargin)<0)
            throw new IllegalStateException("Internal Error - very low quota");
        sys.printlnDebug("Quota = " + quota);
    }

    boolean elect(int seats) {
        boolean electOne = false;
        for(int i=0; i<numCandidates; i++) {
            if((HOPEFUL == candStatus[i]) && (candVote[i].compareTo(quota) >= 0)) {
                candStatus[i] = ALMOST;
                electOne = true;
            }
        }        
        while(count(ELECTED+ALMOST) > seats) {
            sys.printlnDebug("Vote tiebreaker! voters:" + count(ELECTED+ALMOST) + " seats:" + seats);
            changeState(HOPEFUL,ELIMINATED);
            excludeLowest();
        }
        changeState(ALMOST,ELECTED);
        return electOne;
    }
        
    void calcSurplus() {
        surplus = zero;
        for(int i=0; i<numCandidates; i++) {
            if(ELECTED == candStatus[i])
                surplus = surplus.add(candVote[i].subtract(quota));
        }
        sys.printlnDebug("Total Surplus = " + surplus);
    }
    
    boolean tryRemoveLowest(int seats) {
        BigDecimal lowest=reallyBig;
        int index = -1;
        for(int i=0; i<numCandidates; i++) {
            if((candStatus[i] == HOPEFUL) && (candVote[i].compareTo(lowest)<0)) {
                lowest = candVote[i];
                index = i;
            }
        }
        BigDecimal lowest2nd=reallyBig;
        for(int i=0; i<numCandidates; i++) {
            if((i != index) && (candStatus[i] != ELIMINATED) && (candVote[i].compareTo(lowest2nd)<0)) {
                lowest2nd = candVote[i];
            }
        }
        BigDecimal lowestDifference = lowest2nd.subtract(lowest);
        if(lowestDifference.compareTo(zero)>=0) {
            sys.printlnDebug("Lowest Difference = " + lowest2nd + " - " + lowest + " = " + lowestDifference);
        }
        if(lowestDifference.compareTo(surplus)>0) {
            sys.printlnDebug("Remove Lowest (unforced)");
            eliminateOne(index);
            return true;
        }
        else
            return false;
    }

    void convergeOne(int seats) {
        for(int i=0; i<numCandidates; i++) {
            if(ELECTED == candStatus[i]) {
                candWeight[i] = (candWeight[i].multiply(quota).setScale(DP,ROUND_UP))
                                    .divide(candVote[i],DP,ROUND_UP);
            }
        }
        reCalc(seats);
    }
    
    void excludeLowest() {
        int aheadsf = 1000000000;
        int randsf = 10000;
        int excludesf = -1;
        boolean useRandom = false;
        for(int i=0; i<numCandidates; i++) {
            if((HOPEFUL == candStatus[i]) || (ALMOST == candStatus[i])) {
                if(ahead[i] < aheadsf) {
                    aheadsf = ahead[i];
                    randsf = candRand[i];
                    excludesf = i;
                    useRandom = false;
                }
                else if(ahead[i] == aheadsf) {
                    useRandom = true;
                    if(candRand[i] < randsf) {
                        randsf = candRand[i];
                        excludesf = i;
                    }
                }
            }
        }
        if(useRandom) {
            sys.printlnDebug("Random choice used!");
        }
        eliminateOne(excludesf);
    }

    void reverseRandom() {
        for(int i=0; i<numCandidates; i++) {
            candRand[i] = 10000-candRand[i];
        }
    }

    int count(int state) {
        int count = 0;
        for(int i=0; i<numCandidates; i++) {
            if(0 != (state & candStatus[i]))
                count++;
        }
        return count;
    }
    
    void changeState(int from, int to) {
        for(int i=0; i<numCandidates; i++) {
            if(0 != (from & candStatus[i])) {
                candStatus[i] = to;
                // the next two ifs just for the messages
                if(ELECTED == to) {
                    electOne(i);
                }
                if(ELIMINATED == to) {
                    eliminateOne(i);
                }
            }
        }
    }

    public void eliminateOne(int i) {
        candStatus[i] = ELIMINATED;
        candWeight[i] = zero;
        changed = true;
        sys.printlnDebug("Eliminated: " + table.getCandidate(i));
    }
        
    public void electOne(int i) {
        candStatus[i] = ELECTED;
        changed = true;
        sys.printlnDebug("Elected: " + table.getCandidate(i));
    }
}
