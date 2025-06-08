package com.Webtube.site.Exception;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(String errormsg) {
        super(errormsg);
    }
}
