/*
 * Copyright 2017 Crown Copyright
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

package uk.gov.gchq.gaffer.federatedstore.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import org.junit.Assert;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationTest;
import uk.gov.gchq.gaffer.store.StoreProperties;
import uk.gov.gchq.gaffer.store.schema.Schema;
import java.util.Set;

public class AddGraphTest extends OperationTest {


    public static final String expectedKey = "gaffer.store.class";

    @Override
    protected Class<? extends Operation> getOperationClass() {
        return AddGraph.class;
    }

    @Override
    public void shouldSerialiseAndDeserialiseOperation() throws SerialisationException, JsonProcessingException {

        Schema expectedSchema = new Schema.Builder().build();
        String expectedGraphId = "testGraphID";

        StoreProperties storeProperties = new StoreProperties();
        String expectedValue = "uk.gov.gchq.gaffer.federatedstore.FederatedStore";
        storeProperties.set(expectedKey, expectedValue);


        AddGraph op = new AddGraph.Builder()
                .setGraphId(expectedGraphId)
                .setSchema(expectedSchema)
                .setStoreProperties(storeProperties.getProperties())
                .build();

        byte[] serialise = JSON_SERIALISER.serialise(op, true);
        AddGraph deserialise = JSON_SERIALISER.deserialise(serialise, AddGraph.class);

        Assert.assertEquals(expectedGraphId, deserialise.getGraphId());
        Assert.assertEquals(expectedSchema, deserialise.getSchema());
        Assert.assertTrue(deserialise.getProperties().containsKey(expectedKey));
        Assert.assertEquals(expectedValue, deserialise.getProperties().getProperty(expectedKey));
    }

    @Override
    protected Set<String> getRequiredFields() {
        return Sets.newHashSet("graphId", "properties", "schema");
    }

    @Override
    public void builderShouldCreatePopulatedOperation() {
        Schema expectedSchema = new Schema.Builder().build();
        String expectedGraphId = "testGraphID";
        StoreProperties storeProperties = new StoreProperties();
        String expectedValue = "uk.gov.gchq.gaffer.federatedstore.FederatedStore";
        storeProperties.set(expectedKey, expectedValue);
        AddGraph op = new AddGraph.Builder()
                .setGraphId(expectedGraphId)
                .setSchema(expectedSchema)
                .setStoreProperties(storeProperties.getProperties())
                .build();

        Assert.assertEquals(expectedGraphId, op.getGraphId());
        Assert.assertEquals(expectedSchema, op.getSchema());
        Assert.assertTrue(op.getProperties().containsKey(expectedKey));
        Assert.assertEquals(expectedValue, op.getProperties().getProperty(expectedKey));
    }
}