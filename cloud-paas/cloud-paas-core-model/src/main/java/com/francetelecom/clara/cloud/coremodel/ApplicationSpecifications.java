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
package com.francetelecom.clara.cloud.coremodel;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Created by sbortolussi on 20/07/2015.
 */
public class ApplicationSpecifications {

    public static Specification<Application> isActive() {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(applications.get("state"), ApplicationStateEnum.CREATED);
            }
        };
    }

    public static Specification<Application> isRemoved() {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(applications.get("state"), ApplicationStateEnum.REMOVED);
            }
        };
    }

    public static Specification<Application> isPublic() {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(applications.get("isPublic"), Boolean.TRUE);
            }
        };
    }

    public static Specification<Application> hasForMember(SSOId ssoId) {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(applications.join("members").get("value"),ssoId.getValue());
            }
        };
    }

    public static Specification<Application> isPublicOrHasForMember(SSOId ssoId) {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                final Join<Object, Object> members = applications.join("members", JoinType.LEFT);
                final Predicate isPublic = criteriaBuilder.equal(applications.get("isPublic"), Boolean.TRUE);
                final Predicate hasForMember = criteriaBuilder.equal(members.get("value"), ssoId.getValue());
                return criteriaBuilder.or(isPublic, hasForMember);
            }
        };
    }

    public static Specification<Application> hasCode(String code) {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(applications.get("code"), code);
            }
        };
    }

    public static Specification<Application> hasLabel(String label) {
        return new Specification<Application>() {
            @Override
            public Predicate toPredicate(Root<Application> applications, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(applications.get("label"), label);
            }
        };
    }

}
