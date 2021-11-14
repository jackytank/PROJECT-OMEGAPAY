/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.util.List;

/**
 *
 * @author balis
 * @param <EntityType>
 * @param <KeyType>
 */
abstract public class OmegaPayDAO<EntityType, KeyType> {

    abstract public void insert(EntityType entity);

    abstract public void update(EntityType entity);

    abstract public void delete(KeyType id);

    abstract public EntityType selectByID(KeyType id);

    abstract public List<EntityType> selectAll();

    abstract public List<EntityType> selectBySQL(String SQL, Object... args);

}
