package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.coremodel.SSOId;

/**
 * Created by wooj7232 on 31/07/2015.
 */
public interface SecurityContextUtil {
    SSOId currentUser();

    boolean currentUserIsAdmin();
}
