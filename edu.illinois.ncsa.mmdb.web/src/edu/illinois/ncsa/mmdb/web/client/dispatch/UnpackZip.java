/*******************************************************************************
 * Copyright 2014 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class UnpackZip extends SubjectAction<UnpackZipResult> {

    /**
     *
     */
    private static final long serialVersionUID = 2677177208028407129L;

    private String            name             = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
