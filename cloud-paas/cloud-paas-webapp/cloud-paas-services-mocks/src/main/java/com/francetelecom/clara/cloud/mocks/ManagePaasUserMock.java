/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.mocks;

import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.PaasUserNotFoundException;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lzxv3002
 * Date: 09/06/11
 * Time: 11:05
 */
@Service("managePaasUser")
public class ManagePaasUserMock implements ManagePaasUser {
    private HashMap<String, PaasUser> users;

    public ManagePaasUserMock() {
        PaasUser bob =  new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
        users = new HashMap<String, PaasUser>();
        users.put("1", bob);
    }

    @Override
    public PaasUser findPaasUser(String s) throws ObjectNotFoundException {
        if (users.containsKey(s)) {
            return users.get(s);
        } else {
            throw new PaasUserNotFoundException();
        }
    }

    public void createPaasUser(PaasUser paasUser) {
        users.put(paasUser.getSsoId().getValue(), paasUser);
    }

    @Override
    public void deletePaasUser(int i) throws ObjectNotFoundException {
        String key = String.valueOf(i);
        if (users.containsKey(key)) {
            users.remove(key);
        } else {
            throw new PaasUserNotFoundException();
        }
    }

    @Override
    public PaasUser findPaasUser(int i) throws ObjectNotFoundException {
        String key = String.valueOf(i);
        if (users.containsKey(key)) {
            return users.get(key);
        } else {
            throw new PaasUserNotFoundException();
        }
    }

    @Override
    public void updatePaasUser(PaasUser paasUser) throws ObjectNotFoundException {
        String key = paasUser.getSsoId().getValue();
        if (users.containsKey(key)) {
            users.put(key, paasUser);
        } else {
            throw new PaasUserNotFoundException();
        }
    }

    @Override
    public List<PaasUser> findAllPaasUsers() {
        return  new ArrayList<PaasUser>(users.values());
    }

    @Override
    public void checkBeforeCreatePaasUser(PaasUser pUsr) {
        try {
            findPaasUser(pUsr.getSsoId().getValue());
        } catch (ObjectNotFoundException ex) {
            createPaasUser(pUsr);
        }
    }
}
