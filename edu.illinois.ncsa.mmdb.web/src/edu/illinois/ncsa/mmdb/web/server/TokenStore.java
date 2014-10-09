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
package edu.illinois.ncsa.mmdb.web.server;

import java.io.ByteArrayInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.server.util.BeanFiller;

public class TokenStore {

    /** Commons logging **/
    private static Log log     = LogFactory.getLog(TokenStore.class);

    static String      newSalt = null;
    static String      oldSalt = null;

    public static void initialize() {
        generateSalt();
        generateSalt();
    }

    public static void generateSalt() {
        oldSalt = newSalt;
        newSalt = UUID.randomUUID().toString();
    }

    public static String generateToken(String method) {
        return generateToken(method, newSalt);
    }

    public static String generateToken(String method, String salt) {

        String token = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream((method + ":" + salt).getBytes("UTF-8"));
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            DigestInputStream dis = new DigestInputStream(is, sha1);
            int i = 0;
            while ((i = dis.available()) > 0) {
                byte[] temp = new byte[i];

                dis.read(temp);
            }

            dis.close();
            is.close();

            token = BeanFiller.asHex(sha1.digest());
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return token;
    }

    public static boolean isValidToken(String token, String method) {

        //Kludge - Google docs (or some of our code) double decodes "@" and "," chars in our IDs
        method = method.replaceAll("@", "%40");
        method = method.replaceAll(",", "%2C");
        if (token.equals(generateToken(method, newSalt)) || token.equals(generateToken(method, oldSalt))) {
            return true;
        } else {
            return false;
        }
    }

}
