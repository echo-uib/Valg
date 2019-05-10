// Voting Systems Toolbox
// Copyright (C) 2001,2002 Roy Ward
// $Header: /cvsroot/votesystem/votesystem/src/vote/RandomAS183.java,v 1.3 2002/07/05 14:59:43 royward0 Exp $
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

class RandomAS183 {

    int x,y,z;
    
    public RandomAS183(int px, int py, int pz) {
        x = px;
        y = py;
        z = pz;
    }
    
    int random() {
        x = (171*x)%30269;
        y = (172*y)%30307;
        z = (170*z)%30323;
        return 10000 - ((10000*x)/30269 + (10000*y)/30307 + (10000*z)/30323) % 10000;
    }
}
