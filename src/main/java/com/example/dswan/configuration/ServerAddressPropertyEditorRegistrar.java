package com.example.dswan.configuration;

import com.mongodb.ServerAddress;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.data.mongodb.config.ServerAddressPropertyEditor;

public final class ServerAddressPropertyEditorRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(ServerAddress[].class, new ServerAddressPropertyEditor());
    }
}
