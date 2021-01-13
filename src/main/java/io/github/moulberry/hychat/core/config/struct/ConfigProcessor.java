package io.github.moulberry.hychat.core.config.struct;

import com.google.gson.annotations.Expose;
import io.github.moulberry.hychat.core.config.Config;
import io.github.moulberry.hychat.core.config.annotations.Category;
import io.github.moulberry.hychat.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.hychat.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.hychat.core.config.annotations.ConfigOption;
import io.github.moulberry.hychat.core.config.gui.GuiOptionEditor;
import io.github.moulberry.hychat.core.config.gui.GuiOptionEditorBoolean;
import io.github.moulberry.hychat.core.config.gui.GuiOptionEditorDropdown;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class ConfigProcessor {

    public static class ProcessedCategory {
        public final String name;
        public final String desc;
        public final LinkedHashMap<String, ProcessedOption> options = new LinkedHashMap<>();

        public ProcessedCategory(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    public static class ProcessedOption {
        public final String name;
        public final String desc;
        public final int subcategoryId;
        public GuiOptionEditor editor;

        private final Field field;
        private final Object container;

        public ProcessedOption(String name, String desc, int subcategoryId, Field field, Object container) {
            this.name = name;
            this.desc = desc;
            this.subcategoryId = subcategoryId;

            this.field = field;
            this.container = container;
        }

        public Object get() {
            try {
                return field.get(container);
            } catch(Exception e) {
                e.printStackTrace();
                System.err.println("Failed to get config value!");
                return null;
            }
        }

        public boolean set(Object value) {
            try {
                field.set(container, value);
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                System.err.println("Failed to set config value!");
                return false;
            }
        }
    }

    public static LinkedHashMap<String, ProcessedCategory> create(Config config) {
        LinkedHashMap<String, ProcessedCategory> processedConfig = new LinkedHashMap<>();
        for(Field categoryField : config.getClass().getDeclaredFields()) {
            boolean exposePresent = categoryField.isAnnotationPresent(Expose.class);
            boolean categoryPresent = categoryField.isAnnotationPresent(Category.class);

            if(exposePresent && categoryPresent) {
                Object categoryObj;
                try {
                    categoryObj = categoryField.get(config);
                } catch(Exception e) {
                    System.err.printf("Failed to load config category %s. Field was not accessible.\n", categoryField.getName());
                    continue;
                }

                Category categoryAnnotation = categoryField.getAnnotation(Category.class);
                ProcessedCategory cat = new ProcessedCategory(
                        categoryAnnotation.name(),
                        categoryAnnotation.desc()
                );
                processedConfig.put(categoryField.getName(), cat);

                for(Field optionField : categoryObj.getClass().getDeclaredFields()) {
                    boolean optionExposePresent = optionField.isAnnotationPresent(Expose.class);
                    boolean optionPresent = optionField.isAnnotationPresent(ConfigOption.class);

                    if(optionExposePresent && optionPresent) {
                        ConfigOption optionAnnotation = optionField.getAnnotation(ConfigOption.class);
                        ProcessedOption option = new ProcessedOption(
                                optionAnnotation.name(),
                                optionAnnotation.desc(),
                                optionAnnotation.subcategoryId(),
                                optionField,
                                categoryObj
                        );

                        GuiOptionEditor editor = null;
                        Class<?> optionType = optionField.getType();
                        if(optionType.isAssignableFrom(boolean.class) &&
                                optionField.isAnnotationPresent(ConfigEditorBoolean.class)) {
                            editor = new GuiOptionEditorBoolean(option);
                        }
                        if(optionType.isAssignableFrom(int.class)) {
                            if(optionField.isAnnotationPresent(ConfigEditorDropdown.class)) {
                                ConfigEditorDropdown configEditorAnnotation = optionField.getAnnotation(ConfigEditorDropdown.class);
                                editor = new GuiOptionEditorDropdown(option, configEditorAnnotation.values(), (int)option.get(), true);
                            }
                        }
                        if(optionType.isAssignableFrom(String.class)) {
                            if(optionField.isAnnotationPresent(ConfigEditorDropdown.class)) {
                                ConfigEditorDropdown configEditorAnnotation = optionField.getAnnotation(ConfigEditorDropdown.class);
                                editor = new GuiOptionEditorDropdown(option, configEditorAnnotation.values(), 0,false);
                            }
                        }
                        if(editor == null) {
                            System.err.printf("Failed to load config option %s. Could not find suitable editor.\n", optionField.getName());
                            continue;
                        }
                        option.editor = editor;
                        cat.options.put(optionField.getName(), option);
                    } else if(optionExposePresent || optionPresent) {
                        System.err.printf("Failed to load config option %s. Both @Expose and @ConfigOption must be present.\n", optionField.getName());
                    }
                }
            } else if(exposePresent || categoryPresent) {
                System.err.printf("Failed to load config category %s. Both @Expose and @Category must be present.\n", categoryField.getName());
            }
        }
        return processedConfig;
    }

}
