/*******************************************************************************
 * University of Michigan
 * Open Source License
 *
 * Copyright (c) 2013, University of Michigan.  All rights reserved.
 *
 * Developed by:
 * http://sead-data.net
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author Jim Myers
 * 
 */
@SuppressWarnings("serial")
public class ChangeUserPID implements Action<EmptyResult> {

    private String userUri;
    private String oldPID;
    private String newPID;

    public ChangeUserPID() {
    }

    public ChangeUserPID(String userUri, String oldPID, String newPID) {
        this.userUri = userUri;
        this.oldPID = oldPID;
        this.newPID = newPID;
    }

    /**
     * @return the userUri
     */
    public String getUserUri() {
        return userUri;
    }

    /**
     * Sets the uri of the user
     * 
     * @param userUri
     */
    public void setUserUri(String userUri) {
        this.userUri = userUri;
    }

    public String getOldPID() {
        return oldPID;
    }

    public void setOldPID(String oldPID) {
        this.oldPID = oldPID;
    }

    public String getNewPID() {
        return newPID;
    }

    public void setNewPID(String newPID) {
        this.newPID = newPID;
    }
}
