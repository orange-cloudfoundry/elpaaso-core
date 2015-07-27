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

import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import org.springframework.stereotype.Service;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: lzxv3002
 * Date: 08/06/11
 * Time: 14:37
 * Generic Mock for services
 */
@XmlRootElement
@Service()
public class LogicalModelServiceMock<T extends LogicalModelItem> {

    private AtomicInteger sequence = new AtomicInteger();
//    protected List<T> entities = new ArrayList<T>();
    protected Map<Integer, T> entities = new HashMap<Integer, T>();

    protected Collection<T> findAll() {
        return new ArrayList<T>(entities.values());
    }

    protected Collection<T> find(int first, int count) {
        return new ArrayList<T>(entities.values()).subList(first,count);
    }

    protected Collection<T> find(String search, int first, int count) {
        List<T>  items = new ArrayList();
        for(T entity : entities.values()){
            if(entity.getName().equalsIgnoreCase(search))
                items.add(entity);
        }
        return items.subList(first,count);
    }

    protected void create(T entity) {

        boolean exist = false;

        for (T singleEntity : entities.values()) {
            if ((singleEntity.getName()).equals(entity.getName())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            entity.setId(sequence.incrementAndGet());
            entities.put(entity.getId(), entity);
        }
    }

    protected void delete(int id) throws ObjectNotFoundException {

          entities.remove(id);

//        List<T>  items = new ArrayList(entities);
//        for(T item : items){
//            if(item.getId()== id) {
//                int index = entities.indexOf(item);
//                entities.remove(index);
//            }
//        }
    }

    protected T find(int id) throws ObjectNotFoundException {
//       for(T entity : entities){
//            if(entity.getId()==id)
//                return entity;
//        }
        return entities.get(id);
    }

    protected T update(T item) throws ObjectNotFoundException {
        delete(item.getId());
        create(item);
        return item;
    }

    protected Long count() {
        return (long) entities.size();
    }

    protected Long count(String search) {
        List<T>  items = new ArrayList<T>();
        for(T entity : entities.values()){
            if(entity.getName().equalsIgnoreCase(search))
                items.add(entity);
        }
        return (long) items.size();
    }

    public T find(String name) {
       for(T entity : entities.values()){
            if(entity.getName().equals(name))
                return entity;
        }
        return null;
    }

}
