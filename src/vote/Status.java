// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/Status.java,v 1.3 2002/07/05 14:59:43 royward0 Exp $
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

public class Status {
    boolean mDebug;
    boolean mProgress;
	boolean mStrictLegal;
	boolean mStrictRankMin;
	int mRankMin;
    public PrintWriter out;
    public PrintWriter err;
    char[] b = new char[1];
    
    public Status(Parameters params) {
        String s_debug = params.extract("debug","false").toLowerCase();
        String s_progress = params.extract("log","false").toLowerCase();
        String s_nostrict_legal = params.extract("nostrict-legal","false").toLowerCase();
        String s_nostrict_rankmin = params.extract("nostrict-min-ranks","false").toLowerCase();
        mDebug = (s_debug.compareTo("true")==0);
        mProgress = (s_progress.compareTo("true")==0);
        mStrictLegal = !(s_nostrict_legal.compareTo("true")==0);
        mStrictRankMin = !(s_nostrict_rankmin.compareTo("true")==0);
        mRankMin = Integer.valueOf(params.extract("min-ranks","1")).intValue();
		if(mRankMin<1)
			mRankMin=1;
		String outputfile = params.extract("output","");
        String logfile = params.extract("logfile","");
        out = setOutput(outputfile,System.out);
        err = setOutput(logfile,System.err);
    }
    
    public PrintWriter setOutput(String filename, OutputStream outstream) {
        try {
            if(new File(filename).exists()) {
                b[0] = 'n';
                System.out.print("File " + filename + " exists, overwrite (y/n)?");
                new InputStreamReader(System.in).read(b,0,1);
                if(('y' != b[0]) && ('Y' != b[0]))
                    filename = "";
            }
            if(filename.compareTo("") == 0)
                return new PrintWriter(outstream,true);
            else
                return new PrintWriter(new FileWriter(filename),true);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean debug() {
        return mDebug;
    }
    
    public boolean progress() {
        return mProgress;
    }
    
    public void warning(boolean fatal, String s) {
		err.println(s);
		if(fatal)
			throw(new IllegalArgumentException(s));
	}
	
    public void error(String s) {
		warning(true,s);
	}
	
	public void printlnDebug(String s) {
        if(debug())
            err.println(s);
    }

    public void printDebug(String s) {
        if(debug())
            err.print(s);
    }

    public void printlnLog(String s) {
        if(progress())
            err.println(s);
    }

    public void printlnOut(String s) {
        out.println(s);
    }
}
