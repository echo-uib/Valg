// Voting Systems Toolbox
// Copyright (C) 2001 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/Score.java,v 1.1.1.1 2001/10/24 01:51:40 royward0 Exp $
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

class Score {
	double[] scores;
	int numcandidates;
	double diff;
	Table raw;
	
	Score(Table rawinput)
	{
		raw = rawinput;
		numcandidates = raw.numcandidates;
		scores = new double[raw.numcandidates];
		for(int cinit=0; cinit<raw.numcandidates; cinit++)
			scores[cinit] = 0;
		for(int v=0; v<raw.numvoters; v++)
			for(int c=0; c<raw.numcandidates; c++)
			{
				scores[c] += f(raw.ranks[v][c]);
			}
	}
	
	double f(int rank) {
		double rankf = (double)rank;
		return 1/Math.sqrt(rankf) - rankf/560;
		//return 62-rank;
	}
	
	int highest()
	{
		double max = 0.0;
		int hi = -1;
		for(int c=0; c<raw.numcandidates; c++)
		{
			if(scores[c] > max)
			{
				max = scores[c];
				hi = c;
			}
		}
		double max2 = 0.0;
		for(int c2=0; c2<raw.numcandidates; c2++)
		{
			if((scores[c2] > max2) && (c2!=hi))
			{
				max2 = scores[c2];
			}
		}
		diff = max-max2;
		return hi;
	}
	
}
