// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/VoteMain.java,v 1.7 2002/07/09 10:09:42 royward0 Exp $
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

import java.io.*;
import java.util.*;
import vote.*;

public class VoteMain {

	static Table votes = null;

    public static void main(String[] args){
        Status sys = null;
        try {
            String[] extras = {"input", "output"};
            Hashtable singles = new Hashtable();
            singles.put("log",singles);
            singles.put("debug",singles);
            singles.put("nostrict-legal",singles);
            singles.put("nostrict-min-ranks",singles);
            Parameters params = new Parameters();
            params.insert(args,extras,singles);
            String filename = params.extract("input");
            sys = new Status(params);
            try {
                LineNumberReader lines = new LineNumberReader(new FileReader(filename));
                votes = new Table(lines,sys);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
    
            String type = params.extract("system").toLowerCase();
            if(type.compareTo("stv-meek")==0) {
                STV s = new STV(votes, sys);
                int seats = Integer.valueOf(params.extract("seats")).intValue();
                params.checkEmpty();
                s.vote(seats);
                s.print();
            } else if(type.compareTo("stvse-meek")==0) {
                ReiterativeSTV rstv = new ReiterativeSTV(votes);
                String seats = params.extract("seats","ordered");
                if(seats.toLowerCase().charAt(0) != 'o')
                    throw new IllegalArgumentException("Ordering orders all the seats - don't specify a number");
                params.checkEmpty();
                rstv.vote(sys);
                rstv.print(sys);
            }
            else
                throw new IllegalArgumentException("Unknown voting type: " + type);
            sys.printlnOut("Done!");
        } catch(Exception e) {
            System.err.println("Error:" + e.getMessage());
            System.err.println("Program aborted");
            if(sys.debug()) e.printStackTrace();
            sys.printlnLog("Error:" + e.getMessage());
            sys.printlnLog("Program aborted");
        }
	}
	
/*
	static void rankstep(int n)
	{
	    for(int i=0; i<n; i++)
	    {
	   	    Score scores = new Score(votes);
			int sc = scores.highest();
	    	System.out.println("Rank: " + (i+1) + " Candidate# " + votes.getCandidate(sc) + " " + scores.diff);
	    	votes.remove(sc);
	    }
	}
	
	static void rank(int n)
	{
   	    Score scores = new Score(votes);
   	    boolean[] done = new boolean[scores.numcandidates];
   	    for(int b=0; b<scores.numcandidates; b++)
   	    	done[b] = false;
		for(int i=0; i<n; i++)
		{
			double max = 0;
			int cchoice = 0;
			for(int j=0; j<scores.numcandidates; j++)
			{
				if((scores.scores[j]>max) && !done[j])
				{
					max = scores.scores[j];
					cchoice = j;
				}
			}
			done[cchoice] = true;
	    	System.out.println("Rank: " + (i+1) + " Candidate# " + votes.getCandidate(cchoice) + " " + max);
		}
			
	}
    */
}
