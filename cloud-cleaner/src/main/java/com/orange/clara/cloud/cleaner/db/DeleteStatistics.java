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
package com.orange.clara.cloud.cleaner.db;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by WOOJ7232 on 04/02/2015.
 */
public class DeleteStatistics {

    AtomicInteger tables;

    AtomicInteger sequences;


    public DeleteStatistics(){
        tables = new AtomicInteger();
        sequences = new AtomicInteger();
    }


    public void reset(){
        tables.set(0);
        sequences.set(0);
    }


    public int getTablesCount(){
        return tables.get();
    }

    public int getSequencesCount(){
        return sequences.get();
    }

    public int deleteTable(){
        return tables.incrementAndGet();
    }

    public int deleteSequence(){
        return sequences.incrementAndGet();
    }


    @Override
    public String toString() {
        return "DeleteStatistics{" +
                "tables=" + tables +
                ", sequences=" + sequences +
                '}';
    }
}
