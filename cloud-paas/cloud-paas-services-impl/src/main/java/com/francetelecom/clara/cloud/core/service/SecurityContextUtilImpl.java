package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

/**
 * Created by wooj7232 on 31/07/2015.
 */
public class SecurityContextUtilImpl implements SecurityContextUtil {

    @Override
    public  SSOId currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new TechnicalException("User is not authenticated. No authentication token found.");
        }
        return new SSOId(authentication.getName());
    }

    @Override
    public boolean currentUserIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            // FIXME raise a specific exception
            throw new TechnicalException("User is not authenticated. No authentication token found.");
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals(PaasRoleEnum.ROLE_ADMIN.toString())) {
                return true;
            }
        }
        return false;
    }
}
