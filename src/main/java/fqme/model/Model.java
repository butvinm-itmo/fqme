package fqme.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import fqme.column.Column;
import fqme.column.common.NumericColumn;
import fqme.connection.DBConfig;
import lombok.Getter;

/**
 * ORM model realization.
 */
public abstract class Model<T extends Model<T>> {
    /**
     * Autogenerated id.
     */
    @Getter
    private Optional<Integer> id = Optional.empty();
    public static final NumericColumn<Integer> id_ = new NumericColumn<>("id");

    /**
     * Map with meta info (table names, fields, etc.)
     * for Model subclasses.
     */
    private static HashMap<Class<? extends Model<?>>, ModelMetaInfo> modelsMetaInfo = new HashMap<>();

    /**
     * Build and store model subclass meta info.
     *
     * @param modelClass model subclass
     * @param configPath path to config file
     */
    public static void register(Class<? extends Model<?>> modelClass, DBConfig dbConfig) throws NoSuchFieldException {
        // generate table name from class name
        String tableName = modelClass.getSimpleName().replaceFirst("Model$", "").toLowerCase();

        // get names of model columns (@see Column) and suppliers for model fields
        Field[] classFields = modelClass.getDeclaredFields();
        ArrayList<String> columnsNames = new ArrayList<>();
        HashMap<String, Function<? extends Model<?>, ?>> fieldsSuppliers = new HashMap<>();
        for (Field field : classFields) {
            if (Column.class.isAssignableFrom(field.getType())) {
                // add name of Column
                columnsNames.add(field.getName());

                // add supplier for field associated with Column
                Field dataField = modelClass.getField(field.getName());
                Function<? extends Model<?>, ?> supplier = (model) -> {
                    try {
                        return dataField.get(model);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                };
                fieldsSuppliers.put(field.getName(), supplier);
            }
        }

        // store model subclass meta info
        ModelMetaInfo metaInfo = new ModelMetaInfo(tableName, columnsNames, fieldsSuppliers, dbConfig);
        modelsMetaInfo.put(modelClass, metaInfo);
    }

    /**
     * Get model subclass meta info.
     *
     * @param modelClass model subclass
     * @return model subclass meta info
     */
    public static ModelMetaInfo getModelMetaInfo(Class<? extends Model<?>> modelClass) {
        return Model.modelsMetaInfo.get(modelClass);
    }
}
