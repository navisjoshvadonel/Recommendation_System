package com.companion.navis;

import com.companion.gokhul.Item;

// Intermediate representation for CSV parsing before DB insert
public class CsvItem extends Item {
    
    // We can add validation logic or transform logic here before saving to Database
    public boolean isValid() {
        return getName() != null && !getName().isEmpty();
    }
}
