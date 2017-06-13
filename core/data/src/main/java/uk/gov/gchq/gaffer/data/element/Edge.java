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

package uk.gov.gchq.gaffer.data.element;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.gchq.gaffer.data.element.Edge.Builder;
import uk.gov.gchq.gaffer.data.element.id.EdgeId;
import java.util.Map;

/**
 * An <code>Edge</code> in an {@link uk.gov.gchq.gaffer.data.element.Element} containing a source, destination and a directed flag.
 * The source and destination vertices can be any type of {@link java.lang.Object}.
 * There is no requirement for these vertices to connect to an {@link uk.gov.gchq.gaffer.data.element.Entity} vertex -
 * for example you could have a 'graph' of just edges.
 * Edges are designed so that multiple edges can share the same identifiers but are distinguished via their
 * group.
 *
 * @see uk.gov.gchq.gaffer.data.element.Edge.Builder
 */
@JsonDeserialize(builder = Builder.class)
public class Edge extends Element implements EdgeId {
    private static final Logger LOGGER = LoggerFactory.getLogger(Edge.class);
    private static final long serialVersionUID = -5596452468277807842L;
    private Object source;
    private Object destination;
    private boolean directed;
    private MatchedVertex matchedVertex = null;

    private Edge() {
        // Required for Jackson
    }

    public Edge(final String group, final Object source, final Object destination, final boolean directed) {
        super(group);
        this.source = source;
        this.destination = destination;
        this.directed = directed;
        orderVertices();
    }

    public Edge(final String group, final Object source, final Object destination, final boolean directed, final MatchedVertex matchedVertex) {
        super(group);
        this.source = source;
        this.destination = destination;
        this.directed = directed;
        this.matchedVertex = matchedVertex;
        orderVertices();
    }

    private Edge(final Builder builder) {
        super(builder.group);
        this.source = builder.source;
        this.destination = builder.destination;
        this.directed = builder.directed;
        this.matchedVertex = builder.matchedVertex;
        this.properties = builder.properties;
        orderVertices();
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getDestination() {
        return destination;
    }

    @Override
    public boolean isDirected() {
        return directed;
    }

    @Override
    public Object getIdentifier(final IdentifierType identifierType) {
        switch (identifierType) {
            case SOURCE:
                return getSource();
            case DESTINATION:
                return getDestination();
            case DIRECTED:
                return isDirected();
            default:
                return null;
        }
    }

    @Override
    public void putIdentifier(final IdentifierType identifierType, final Object propertyToBeSet) {
        switch (identifierType) {
            case SOURCE:
                source = propertyToBeSet;
                break;
            case DESTINATION:
                destination = propertyToBeSet;
                break;
            case DIRECTED:
                directed = (boolean) propertyToBeSet;
                break;
            default:
                LOGGER.error("Unknown identifier type: {} detected.", identifierType);
        }
    }

    public MatchedVertex getMatchedVertex() {
        return matchedVertex;
    }

    public void reinitialise(final String group, final Object source, final Object destination, final boolean directed) {
        super.setGroup(group);
        this.source = source;
        this.destination = destination;
        this.directed = directed;
        this.properties.clear();
        orderVertices();
    }

    private void orderVertices() {
        if (null != source && null != destination) {
            if (!directed) {
                if (source instanceof Comparable && destination.getClass().equals(source
                        .getClass())) {
                    if (((Comparable) source).compareTo((Comparable) destination) > 0) {
                        swapVertices();
                    }
                } else if (source.toString()
                                 .compareTo(destination.toString()) > 0) {
                    swapVertices();
                }
            }
        }
    }

    private void swapVertices() {
        final Object tmp = this.source;
        this.source = this.destination;
        this.destination = tmp;
        if (matchedVertex != null) {
            if (matchedVertex == MatchedVertex.DESTINATION) {
                matchedVertex = MatchedVertex.SOURCE;
            } else {
                matchedVertex = MatchedVertex.DESTINATION;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash;
        if (directed) {
            hash = new HashCodeBuilder(21, 3)
                    .appendSuper(super.hashCode())
                    .append(source)
                    .append(destination)
                    .append(directed)
                    .toHashCode();
        } else {
            hash = super.hashCode();
            if (null != source) {
                hash ^= source.hashCode();
            }
            if (null != destination) {
                hash ^= destination.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        return null != obj
                && (obj instanceof Edge)
                && equals((Edge) obj);
    }

    public boolean equals(final Edge edge) {
        return null != edge
                && (new EqualsBuilder()
                .append(directed, edge.isDirected())
                .append(source, edge.getSource())
                .append(destination, edge.getDestination())
                .appendSuper(super.equals(edge))
                .isEquals()
                || new EqualsBuilder()
                .append(directed, false)
                .append(source, edge.getDestination())
                .append(destination, edge.getSource())
                .appendSuper(super.equals(edge))
                .isEquals()
        );
    }

    @Override
    public Edge emptyClone() {
        return new Edge(
                this.getGroup(),
                this.getSource(),
                this.getDestination(),
                this.isDirected()
        );
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("destination", destination)
                .append("directed", directed)
                .appendSuper(super.toString())
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Object source;
        private Object destination;
        private boolean directed;
        private MatchedVertex matchedVertex = null;
        private String group = "UNKNOWN";
        private final Properties properties = new Properties();

        public Builder group(final String group) {
            this.group = group;
            return this;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
        public Builder source(final Object source) {
            this.source = source;
            return this;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
        public Builder destination(final Object destination) {
            this.destination = destination;
            return this;
        }

        public Builder directed(final boolean directed) {
            this.directed = directed;
            return this;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
        public Builder properties(final Map<String, Object> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public Builder matchedVertex(final MatchedVertex matchedVertex) {
            this.matchedVertex = matchedVertex;
            return this;
        }

        public Builder property(final String name, final Object value) {
           this.properties.put(name, value);
            return this;
        }

        @JsonProperty("class")
        public Builder className(final String className) {
            // ignore the className as it will be picked up by the JsonTypeInfo annotation.
            return this;
        }

        public Edge build() {
            return new Edge(this);
        }
    }

    public enum MatchedVertex {
        SOURCE,
        DESTINATION
    }
}

