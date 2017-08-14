package org.ops4j.kaiserkai.rest.model;

import java.util.ArrayList;
import java.util.List;

public class Catalog {
    
    private List<String> repositories = new ArrayList<>();

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }
    
    

}
