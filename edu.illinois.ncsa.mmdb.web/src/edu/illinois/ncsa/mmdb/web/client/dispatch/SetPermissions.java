/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;

/**
 * This dispatch allows permissions to be set in bulk. This is more desirable
 * then setting them one dispatch at a time,
 * since an administrator may not want permissions to be in an intermediate
 * state, when several of them are intended to be
 * changed together.
 * 
 * @author futrelle
 * 
 */
@SuppressWarnings("serial")
public class SetPermissions implements Action<SetPermissionsResult> {
    List<PermissionSetting> settings;

    public SetPermissions() {
    }

    public SetPermissions(Collection<PermissionSetting> settings) {
        for (PermissionSetting s : settings ) {
            addSetting(s);
        }
    }

    public SetPermissions(PermissionSetting... setting) {
        for (PermissionSetting s : setting ) {
            addSetting(s);
        }
    }

    public SetPermissions(String roleUri, Permission permission, PermissionValue value) {
        addSetting(roleUri, permission, value);
    }

    public void addSetting(String roleUri, Permission permission, PermissionValue value) {
        addSetting(new PermissionSetting(roleUri, permission, value));
    }

    public void addSetting(PermissionSetting setting) {
        if (settings == null) {
            settings = new LinkedList<PermissionSetting>();
        }
        settings.add(setting);
    }

    public List<PermissionSetting> getSettings() {
        return settings;
    }
}
