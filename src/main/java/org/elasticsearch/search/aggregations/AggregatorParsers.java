begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|MapBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * A registry for all the aggregator parser, also servers as the main parser for the aggregations module  */
end_comment

begin_class
DECL|class|AggregatorParsers
specifier|public
class|class
name|AggregatorParsers
block|{
DECL|field|VALID_AGG_NAME
specifier|public
specifier|static
specifier|final
name|Pattern
name|VALID_AGG_NAME
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[^\\[\\]>]+"
argument_list|)
decl_stmt|;
DECL|field|parsers
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Aggregator
operator|.
name|Parser
argument_list|>
name|parsers
decl_stmt|;
comment|/**      * Constructs the AggregatorParsers out of all the given parsers      *      * @param parsers The available aggregator parsers (dynamically injected by the {@link org.elasticsearch.search.aggregations.AggregationModule}).      */
annotation|@
name|Inject
DECL|method|AggregatorParsers
specifier|public
name|AggregatorParsers
parameter_list|(
name|Set
argument_list|<
name|Aggregator
operator|.
name|Parser
argument_list|>
name|parsers
parameter_list|)
block|{
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Aggregator
operator|.
name|Parser
argument_list|>
name|builder
init|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Aggregator
operator|.
name|Parser
name|parser
range|:
name|parsers
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|parser
operator|.
name|type
argument_list|()
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|parsers
operator|=
name|builder
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the parser that is registered under the given aggregation type.      *      * @param type  The aggregation type      * @return      The parser associated with the given aggregation type.      */
DECL|method|parser
specifier|public
name|Aggregator
operator|.
name|Parser
name|parser
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
name|parsers
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Parses the aggregation request recursively generating aggregator factories in turn.      *      * @param parser    The input xcontent that will be parsed.      * @param context   The search context.      *      * @return          The parsed aggregator factories.      *      * @throws IOException When parsing fails for unknown reasons.      */
DECL|method|parseAggregators
specifier|public
name|AggregatorFactories
name|parseAggregators
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|parseAggregators
argument_list|(
name|parser
argument_list|,
name|context
argument_list|,
literal|0
argument_list|)
return|;
block|}
DECL|method|parseAggregators
specifier|private
name|AggregatorFactories
name|parseAggregators
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|int
name|level
parameter_list|)
throws|throws
name|IOException
block|{
name|Matcher
name|validAggMatcher
init|=
name|VALID_AGG_NAME
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|AggregatorFactories
operator|.
name|Builder
name|factories
init|=
operator|new
name|AggregatorFactories
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in [aggs]: aggregations definitions must start with the name of the aggregation."
argument_list|)
throw|;
block|}
specifier|final
name|String
name|aggregationName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|validAggMatcher
operator|.
name|reset
argument_list|(
name|aggregationName
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Invalid aggregation name ["
operator|+
name|aggregationName
operator|+
literal|"]. Aggregation names must be alpha-numeric and can only contain '_' and '-'"
argument_list|)
throw|;
block|}
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Aggregation definition for ["
operator|+
name|aggregationName
operator|+
literal|" starts with a ["
operator|+
name|token
operator|+
literal|"], expected a ["
operator|+
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
operator|+
literal|"]."
argument_list|)
throw|;
block|}
name|AggregatorFactory
name|factory
init|=
literal|null
decl_stmt|;
name|AggregatorFactories
name|subFactories
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Expected ["
operator|+
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
operator|+
literal|"] under a ["
operator|+
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
operator|+
literal|"], but got a ["
operator|+
name|token
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|fieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Expected ["
operator|+
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
operator|+
literal|"] under ["
operator|+
name|fieldName
operator|+
literal|"], but got a ["
operator|+
name|token
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|fieldName
condition|)
block|{
case|case
literal|"aggregations"
case|:
case|case
literal|"aggs"
case|:
if|if
condition|(
name|subFactories
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Found two sub aggregation definitions under ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|subFactories
operator|=
name|parseAggregators
argument_list|(
name|parser
argument_list|,
name|context
argument_list|,
name|level
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
default|default:
if|if
condition|(
name|factory
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Found two aggregation type definitions in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|factory
operator|.
name|type
operator|+
literal|"] and ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Aggregator
operator|.
name|Parser
name|aggregatorParser
init|=
name|parser
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggregatorParser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Could not find aggregator type ["
operator|+
name|fieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|factory
operator|=
name|aggregatorParser
operator|.
name|parse
argument_list|(
name|aggregationName
argument_list|,
name|parser
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|factory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Missing definition for aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|subFactories
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|subFactories
argument_list|(
name|subFactories
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|level
operator|==
literal|0
condition|)
block|{
name|factory
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
name|factories
operator|.
name|add
argument_list|(
name|factory
argument_list|)
expr_stmt|;
block|}
return|return
name|factories
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

