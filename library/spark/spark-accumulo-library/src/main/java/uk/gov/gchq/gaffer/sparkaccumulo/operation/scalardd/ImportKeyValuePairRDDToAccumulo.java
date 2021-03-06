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
package uk.gov.gchq.gaffer.sparkaccumulo.operation.scalardd;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.Options;
import uk.gov.gchq.gaffer.operation.io.Input;
import java.util.Map;

public class ImportKeyValuePairRDDToAccumulo implements
        Operation,
        Input<RDD<Tuple2<Key, Value>>>,
        Options {

    private RDD<Tuple2<Key, Value>> input;
    private String outputPath;
    private String failurePath;
    private Map<String, String> options;

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(final String outputPath) {
        this.outputPath = outputPath;
    }

    public void setFailurePath(final String failurePath) {
        this.failurePath = failurePath;
    }

    public String getFailurePath() {
        return failurePath;
    }

    @Override
    public RDD<Tuple2<Key, Value>> getInput() {
        return input;
    }

    @Override
    public void setInput(final RDD<Tuple2<Key, Value>> input) {
        this.input = input;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    public static class Builder extends Operation.BaseBuilder<ImportKeyValuePairRDDToAccumulo, Builder>
            implements Input.Builder<ImportKeyValuePairRDDToAccumulo, RDD<Tuple2<Key, Value>>, Builder>,
            Options.Builder<ImportKeyValuePairRDDToAccumulo, Builder> {
        public Builder() {
            super(new ImportKeyValuePairRDDToAccumulo());
        }

        public Builder outputPath(final String outputPath) {
            _getOp().setOutputPath(outputPath);
            return _self();
        }

        public Builder failurePath(final String failurePath) {
            _getOp().setFailurePath(failurePath);
            return _self();
        }
    }
}
