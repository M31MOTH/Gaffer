/*
 * Copyright 2016-2017 Crown Copyright
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

package uk.gov.gchq.gaffer.operation.export.graph;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.export.ExportTo;
import uk.gov.gchq.gaffer.operation.serialisation.TypeReferenceImpl;
import uk.gov.gchq.gaffer.store.StoreProperties;
import java.io.InputStream;
import java.nio.file.Path;

public class ExportToOtherGraph<T> implements
        Operation,
        ExportTo<T> {
    private T input;
    private StoreProperties storeProperties;

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setKey(final String key) {
        // key is not used
    }

    @Override
    public T getInput() {
        return input;
    }

    @Override
    public void setInput(final T input) {
        this.input = input;
    }

    public StoreProperties getStoreProperties() {
        return storeProperties;
    }

    public void setStoreProperties(final StoreProperties storeProperties) {
        this.storeProperties = storeProperties;
    }

    @Override
    public TypeReference<T> getOutputTypeReference() {
        return (TypeReference) new TypeReferenceImpl.Object();
    }

    public static final class Builder<T> extends BaseBuilder<ExportToOtherGraph<T>, Builder<T>>
            implements ExportTo.Builder<ExportToOtherGraph<T>, T, Builder<T>> {
        public Builder() {
            super(new ExportToOtherGraph<>());
        }

        public Builder<T> storeProperties(final StoreProperties properties) {
            _getOp().setStoreProperties(properties);
            return _self();
        }

        public Builder<T> storeProperties(final InputStream propertiesStream) {
            _getOp().setStoreProperties(StoreProperties.loadStoreProperties(propertiesStream));
            return _self();
        }

        public Builder<T> storeProperties(final String propertiesPath) {
            _getOp().setStoreProperties(StoreProperties.loadStoreProperties(propertiesPath));
            return _self();
        }

        public Builder<T> storeProperties(final Path propertiesPath) {
            _getOp().setStoreProperties(StoreProperties.loadStoreProperties(propertiesPath));
            return _self();
        }
    }
}