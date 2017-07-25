/*
 * Copyright 2017. Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.parquetstore.utils;

import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.convert.Wrappers;
import scala.collection.mutable.WrappedArray;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.parquetstore.serialisation.ParquetSerialiser;
import uk.gov.gchq.gaffer.serialisation.Serialiser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GafferGroupObjectConverter implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GafferGroupObjectConverter.class);
    private static final long serialVersionUID = -8098393761925808135L;
    private final Map<String, Serialiser> columnToSerialiser;
    private final Map<String, String[]> columnToPaths;
    private final String group;


    public GafferGroupObjectConverter(final String group, final Map<String, String> columnToSerialiserName,
                                      final Map<String, Serialiser> serialiserNameToSerialiser,
                                      final Map<String, String[]> columnToPaths) throws SerialisationException {
        this.group = group;
        this.columnToSerialiser = new HashMap<>();
        for (final Map.Entry<String, String> entry : columnToSerialiserName.entrySet()) {
            this.columnToSerialiser.put(entry.getKey(), serialiserNameToSerialiser.get(entry.getValue()));
        }
        this.columnToPaths = columnToPaths;
    }

    public Object[] gafferObjectToParquetObjects(final String gafferColumn, final Object value) throws SerialisationException {
        final Serialiser serialiser = columnToSerialiser.get(gafferColumn);
        if (value != null) {
            if (serialiser instanceof ParquetSerialiser) {
                return (Object[]) serialiser.serialise(value);
            } else if (serialiser != null) {
                return new Object[]{serialiser.serialise(value)};
            } else {
                return new Object[]{value};
            }
        } else {
            return new Object[]{null};
        }
    }

    public Object parquetObjectsToGafferObject(final String gafferColumn, final Object[] value) throws SerialisationException {
        final Serialiser serialiser = columnToSerialiser.get(gafferColumn);
        if (serialiser instanceof ParquetSerialiser) {
            return ((ParquetSerialiser) serialiser).deserialise(value);
        } else {
            if (value[0] == null) {
                return null;
            } else if (value[0] instanceof byte[]) {
                return serialiser.deserialise(value[0]);
            } else {
                throw new SerialisationException("Cannot deserialise object");
            }
        }
    }

    /**
     * Extracts an object corresponding to column <code>gafferColumn</code> from the provided {@link GenericRowWithSchema}.
     *
     * @param gafferColumn the column to extract
     * @param row the row to extract from
     * @return the extracted {@link Object}
     * @throws SerialisationException if the conversion from Parquet objects to the original object throws a
     * {@link SerialisationException}
     */
    public Object sparkRowToGafferObject(final String gafferColumn, final GenericRowWithSchema row) throws SerialisationException {
        final ArrayList<Object> objectsList = new ArrayList<>();
        final String[] paths = columnToPaths.get(gafferColumn);
        if (paths[0].contains(".")) {
            final Object nestedRow = row.getAs(gafferColumn);
            if (nestedRow != null) {
                if (nestedRow instanceof GenericRowWithSchema) {
                    getObjectsFromNestedRow(objectsList, (GenericRowWithSchema) nestedRow);
                } else if (nestedRow instanceof WrappedArray) {
                    objectsList.add(((WrappedArray) nestedRow).array());
                } else if (nestedRow instanceof scala.collection.Map) {
                    objectsList.add(scala.collection.JavaConversions.mapAsJavaMap((scala.collection.Map) nestedRow));
                } else {
                    throw new SerialisationException("sparkRowToGafferObject does not know how to deal with a " + nestedRow.getClass().getCanonicalName());
                }
            } else {
                objectsList.add(null);
            }
        } else {
            for (final String path : paths) {
                final Object obj = row.getAs(path);
                objectsList.add(obj);
            }
        }
        final Object[] objects;
        if (paths[0].endsWith("key_value.key")) {
            objects = new Object[1];
        } else {
           objects = new Object[paths.length];
        }
        objectsList.toArray(objects);
        final Object gafferObject = parquetObjectsToGafferObject(gafferColumn, objects);
        if (gafferObject == null) {
            LOGGER.debug("Failed to get the Gaffer Object from the Spark Row for the column: {}", gafferColumn);
        }
        return gafferObject;
    }

    private void getObjectsFromNestedRow(final ArrayList<Object> objects, final GenericRowWithSchema row) {
        for (final StructField field : row.schema().fields()) {
            final Object fieldValue = row.getAs(field.name());
            if (fieldValue instanceof GenericRowWithSchema) {
                getObjectsFromNestedRow(objects, (GenericRowWithSchema) fieldValue);
            } else {
                objects.add(fieldValue);
            }
        }
    }

    /**
     * Converts the provided <code>object</code> into objects as specified by the <code>sparkSchema</code>.
     *
     * @param column the column that the object has come from
     * @param object the object to be converted
     * @param recordBuilder the {@link ArrayList} to add the objects resulting from the conversion to
     * @param sparkSchema the {@link StructType} that defines the Spark schema
     * @throws SerialisationException if the object cannot be serialised
     */
    public void addGafferObjectToSparkRow(final String column,
                                          final Object object,
                                          final ArrayList<Object> recordBuilder,
                                          final StructType sparkSchema) throws SerialisationException {
        final String[] paths = columnToPaths.get(column);
        if (object != null) {
            final Iterator<Object> parquetObjects;
            parquetObjects = Arrays.asList(gafferObjectToParquetObjects(column, object)).iterator();
            final ArrayList<Object> records = new ArrayList<>();
            if (paths[0].contains(".")) { // it is a nested structure
                recusivelyGenerateSparkObjects(parquetObjects, sparkSchema.apply(column).dataType(), records);
            } else {
                for (final String path : paths) {
                    recusivelyGenerateSparkObjects(parquetObjects, sparkSchema.apply(path).dataType(), records);
                }
            }
            recordBuilder.addAll(records);
        } else {
            LOGGER.trace("Gaffer Object: null");
            if (paths[0].contains(".")) {
                recordBuilder.add(null);
            } else {
                Arrays.asList(paths).forEach(x -> recordBuilder.add(null));
            }
        }
    }

    private void recusivelyGenerateSparkObjects(final Iterator<Object> parquetObjects,
                                                final DataType fieldType,
                                                final ArrayList<Object> recordBuilder) throws SerialisationException {
        if (fieldType instanceof StructType) {
            final ArrayList<Object> nestedRecordBuilder = new ArrayList<>();
            for (final String field : ((StructType) fieldType).fieldNames()) {
                final DataType innerDataType = ((StructType) fieldType).apply(field).dataType();
                recusivelyGenerateSparkObjects(parquetObjects, innerDataType, nestedRecordBuilder);
            }
            final Object[] rowObjects = new Object[nestedRecordBuilder.size()];
            nestedRecordBuilder.toArray(rowObjects);
            recordBuilder.add(new GenericRowWithSchema(rowObjects, (StructType) fieldType));
        } else {
            // must be a primitive type
            final Object parquetObject = parquetObjects.next();
            if (parquetObject instanceof Map) {
                recordBuilder.add(scala.collection.JavaConversions.mapAsScalaMap((Map<Object, Object>) parquetObject));
            } else {
                recordBuilder.add(parquetObject);
            }
        }
    }

    /**
     * Builds up a Gaffer element using a map of parquetColumn to Object[] containing the relevant objects stored in that column
     *
     * @param parquetColumnToObject is a map from parquet column path to a list of the objects stored on that path which
     *                              only contains more then 1 if the column is storing an array or part of a map
     * @param isEntity is it an Entity that needs building
     * @return an Element containing the objects from the parquetColumnToObject
     * @throws SerialisationException if the parquet objects can not be de-serialised
     */
    public Element buildElementFromParquetObjects(final Map<String, Object[]> parquetColumnToObject, final boolean isEntity) throws SerialisationException {
        final Element e;
        if (isEntity) {
            e = new Entity(group);
        } else {
            e = new Edge(group);
        }
        Object src = null;
        Object dst = null;
        boolean isDir = false;
        for (final Map.Entry<String, String[]> columnToPaths : columnToPaths.entrySet()) {
            final String column = columnToPaths.getKey();
            final String[] paths = columnToPaths.getValue();
            final Object[] parquetObjectsForColumn = new Object[paths.length];
            boolean isMap = false;
            for (int i = 0; i < paths.length; i++) {
                final String path = paths[i];
                if (path.endsWith("key_value.key")) {
                    isMap = true;
                }
                Object[] parquetColumnObjects = parquetColumnToObject.getOrDefault(paths[i], null);
                if (parquetColumnObjects != null) {
                    if (path.endsWith("list.element")) {
                        final boolean expectsList = columnToSerialiser.get(column).canHandle(List.class);
                        if (expectsList) {
                            final List<Object> list = new ArrayList<>(parquetColumnObjects.length);
                            for (final Object listObject : parquetColumnObjects) {
                                list.add(listObject);
                            }
                            parquetObjectsForColumn[i] = list;
                        } else {
                            parquetObjectsForColumn[i] = parquetColumnObjects;
                        }
                    } else {
                        if (isMap) {
                            parquetObjectsForColumn[i] = parquetColumnObjects;
                        } else {
                            parquetObjectsForColumn[i] = parquetColumnObjects[0];
                        }
                    }
                }
            }
            final Object gafferObject;
            if (isMap) {
                final Object[] keys = (Object[]) parquetObjectsForColumn[0];
                if (keys != null) {
                    final Object[] values = (Object[]) parquetObjectsForColumn[1];
                    final Map<Object, Object> map = new HashMap<>(keys.length);
                    for (int i = 0; i < keys.length; i++) {
                        map.put(keys[i], values[i]);
                    }
                    gafferObject = parquetObjectsToGafferObject(column, new Object[]{map});
                } else {
                    gafferObject = null;
                }
            } else {
                gafferObject = parquetObjectsToGafferObject(column, parquetObjectsForColumn);
            }
            if (gafferObject != null) {
                if (isEntity) {
                    if (ParquetStoreConstants.VERTEX.equals(column)) {
                        ((Entity) e).setVertex(gafferObject);
                    } else {
                        e.putProperty(column, gafferObject);
                    }
                } else {
                    if (ParquetStoreConstants.SOURCE.equals(column)) {
                        src = gafferObject;
                    } else if (ParquetStoreConstants.DESTINATION.equals(column)) {
                        dst = gafferObject;
                    } else if (ParquetStoreConstants.DIRECTED.equals(column)) {
                        isDir = (boolean) gafferObject;
                    } else {
                        e.putProperty(column, gafferObject);
                    }
                }
            }
        }
        if (!isEntity) {
            ((Edge) e).setIdentifiers(src, dst, isDir);
        }
        return e;
    }
}
