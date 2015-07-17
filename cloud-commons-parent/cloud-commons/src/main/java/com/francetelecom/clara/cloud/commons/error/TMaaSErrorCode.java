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
package com.francetelecom.clara.cloud.commons.error;

/**
 * TMaaSErrorCode
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public enum TMaaSErrorCode implements ErrorCode {
    // 20xxx
    TMAAS_ERROR(20000),
    TMAAS_UNAVAILABLE_SERVICE_ERROR(20100),
    TMAAS_INVALID_ACCOUNT_ERROR(20200),
    TMAAS_INTERNAL_ERROR(20500);

    private final int number;

    private TMaaSErrorCode(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }
}
