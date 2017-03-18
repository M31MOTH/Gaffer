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

package uk.gov.gchq.gaffer.operation.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.gchq.gaffer.data.GroupCounts;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.io.IterableInputOutput;
import uk.gov.gchq.gaffer.operation.serialisation.TypeReferenceImpl;

/**
 * A <code>CountGroups</code> operation takes in {@link Element}s and collects
 * counts for the number of entity and edge groups used. To avoid counting all
 * elements in the store, this operation has a limit, which can be set to
 * skip counting the remaining groups.
 *
 * @see CountGroups.Builder
 */
public class CountGroups implements
        Operation,
        IterableInputOutput<Element, GroupCounts> {
    private Iterable<Element> input;
    private Integer limit;

    public CountGroups() {
    }

    public CountGroups(final Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    @Override
    public TypeReference<GroupCounts> getOutputTypeReference() {
        return new TypeReferenceImpl.CountGroups();
    }

    @Override
    public Iterable<Element> getInput() {
        return input;
    }

    @Override
    public void setInput(final Iterable<Element> input) {
        this.input = input;
    }

    public static class Builder
            extends Operation.BaseBuilder<CountGroups, Builder>
            implements IterableInputOutput.Builder<CountGroups, Element, GroupCounts, Builder> {

        public Builder() {
            super(new CountGroups());
        }

        /**
         * @param limit the limit of group counts to calculate.
         * @return this Builder
         * @see CountGroups#setLimit(Integer)
         */
        public Builder limit(final Integer limit) {
            _getOp().setLimit(limit);
            return this;
        }
    }
}