/*
 * Copyright 2016 Crown Copyright
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
package uk.gov.gchq.gaffer.function.filter;

import org.junit.Test;
import uk.gov.gchq.gaffer.commonutil.JsonUtil;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.koryphe.predicate.PredicateTest;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IsFalseTest extends PredicateTest {
    @Test
    public void shouldAcceptTheValueWhenFalse() {
        // Given
        final IsFalse filter = new IsFalse();

        // When
        boolean accepted = filter.test(false);

        // Then
        assertTrue(accepted);
    }

    @Test
    public void shouldAcceptTheValueWhenObjectFalse() {
        // Given
        final IsFalse filter = new IsFalse();

        // When
        boolean accepted = filter.test(Boolean.FALSE);

        // Then
        assertTrue(accepted);
    }

    @Test
    public void shouldRejectTheValueWhenNull() {
        // Given
        final IsFalse filter = new IsFalse();

        // When
        boolean accepted = filter.test((Boolean) null);

        // Then
        assertFalse(accepted);
    }

    @Test
    public void shouldRejectTheValueWhenTrue() {
        // Given
        final IsFalse filter = new IsFalse();

        // When
        boolean accepted = filter.test(true);

        // Then
        assertFalse(accepted);
    }

    @Test
    public void shouldJsonSerialiseAndDeserialise() throws SerialisationException {
        // Given
        final IsFalse filter = new IsFalse();

        // When
        final String json = new String(new JSONSerialiser().serialise(filter, true));

        // Then
        JsonUtil.assertEquals(String.format("{%n" +
                "  \"class\" : \"uk.gov.gchq.gaffer.function.filter.IsFalse\"%n" +
                "}"), json);

        // When 2
        final IsFalse deserialisedFilter = new JSONSerialiser().deserialise(json.getBytes(), IsFalse.class);

        // Then 2
        assertNotNull(deserialisedFilter);
    }

    @Override
    protected Class<IsFalse> getPredicateClass() {
        return IsFalse.class;
    }

    @Override
    protected IsFalse getInstance() {
        return new IsFalse();
    }
}