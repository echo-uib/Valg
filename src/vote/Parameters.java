// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/Parameters.java,v 1.4 2002/07/05 14:59:43 royward0 Exp $
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

import java.util.*;

public class Parameters extends java.util.Properties {

    public Parameters() {
        super();
    }
    
    public void insert(String[] cmd, String[] extras, Hashtable singles) {
        int extraindex = 0;
        int i = 0;
        while(i < cmd.length) {
            while((extraindex < extras.length) && (containsKey(extras[extraindex])))
                extraindex++;
            if(cmd[i].charAt(0) == '-') {
                String cmdname = cmd[i].substring(1).toLowerCase();
                if((i+1 == cmd.length) || (singles.containsKey(cmdname)) /*(cmd[i+1].charAt(0) == '-')*/) {
                    add(cmdname, "true");
                    i++;
                }
                else {
                    add(cmdname, cmd[i+1]);
                    i+=2;
                }
            }
            else {
                if(extraindex >= extras.length)
                    throw new IllegalArgumentException("Unexpected argument: " + cmd[i]);
                else
                    add(extras[extraindex], cmd[i]);
                i++;
                extraindex++;
            }
        }
    }
    
    public String extract(String key, String defaults) {
        String ret = getProperty(key.toLowerCase(),defaults);
        remove(key.toLowerCase());
        return ret;
    }
    
    public String extract(String key) {
        String ret = getProperty(key.toLowerCase());
        if(null == ret)
            throw new IllegalArgumentException("Missing argument: " + key);
        remove(key.toLowerCase());
        return ret;
    }
    
    public void add(String key, String value) {
        if(containsKey(key.toLowerCase())) {
            throw new IllegalArgumentException("Repeated argument: " + key);
        }
        put(key.toLowerCase(),value);
    }

    public void checkEmpty() {
        if(!isEmpty()) {
            System.err.println(keys());
            throw new IllegalArgumentException("Illegal arguments found");
        }
    }
}
