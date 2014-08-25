package military.gui;

import java.util.HashMap;

/**
 *
 * @author Nate
 */
public class ModelManager {
    private static HashMap<String, Model> entries;
    
    public static Model getModel(String name){
        if(entries == null){
            entries = new HashMap<>();
        }
        if(entries.containsKey(name)){
            return entries.get(name);
        }
        else{
            Model m = new Model(name);
            entries.put(name, m);
            return m;
        }
    }
}
